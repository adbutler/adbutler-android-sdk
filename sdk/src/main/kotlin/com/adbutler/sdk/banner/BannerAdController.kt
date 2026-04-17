package com.adbutler.sdk.banner

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import com.adbutler.sdk.core.AdButlerError
import com.adbutler.sdk.core.AdButlerInstance
import com.adbutler.sdk.core.AdRequest
import com.adbutler.sdk.core.AdResponse
import com.adbutler.sdk.core.ViewabilityTracker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Internal controller that manages the banner ad lifecycle:
 * load -> render -> track -> auto-refresh.
 */
internal class BannerAdController(
    private val sdk: AdButlerInstance
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private var adResponse: AdResponse? = null
    private var refreshHandler: Handler? = null
    private var refreshRunnable: Runnable? = null
    private var viewabilityTracker: ViewabilityTracker? = null
    private var loadJob: Job? = null

    var onAdLoaded: ((AdResponse) -> Unit)? = null
    var onAdFailed: ((AdButlerError) -> Unit)? = null
    var onImpression: (() -> Unit)? = null
    var onClick: (() -> Unit)? = null

    /**
     * Load an ad for the given request.
     */
    fun loadAd(request: AdRequest) {
        loadJob?.cancel()
        loadJob = scope.launch {
            try {
                val response = sdk.client.fetchAd(request, sdk.accountId)
                adResponse = response

                // Fire impression pixel immediately
                sdk.trackingManager.fireImpression(response)

                onAdLoaded?.invoke(response)

                // Set up auto-refresh if configured
                val refreshTime = response.refreshTime
                if (refreshTime != null && refreshTime > 0) {
                    scheduleRefresh(refreshTime.toLong() * 1000L, request)
                }
            } catch (e: AdButlerError) {
                onAdFailed?.invoke(e)
            } catch (e: Exception) {
                onAdFailed?.invoke(AdButlerError.NetworkError(e))
            }
        }
    }

    /**
     * Render the current ad response into the given container view.
     * Returns the rendered view (WebView or ImageView).
     */
    fun renderAd(container: ViewGroup): View? {
        val response = adResponse ?: return null

        // Fire eligible URL when rendered
        sdk.trackingManager.fireEligible(response)

        val renderedView: View

        if (response.isHtmlAd) {
            val html = response.body ?: return null
            val webView = createWebView(container)
            webView.loadDataWithBaseURL(
                null,
                wrapHtml(html, response.width, response.height),
                "text/html",
                "UTF-8",
                null
            )
            renderedView = webView
        } else if (response.isImageAd) {
            val imageUrl = response.imageUrl ?: return null
            val imageView = ImageView(container.context).apply {
                scaleType = ImageView.ScaleType.FIT_CENTER
                isClickable = true
                isFocusable = true
                contentDescription = response.altText ?: "Ad"
                setOnClickListener { handleClick() }
            }
            loadImage(imageUrl, imageView)
            renderedView = imageView
        } else {
            return null
        }

        renderedView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        container.addView(renderedView)

        // Start viewability tracking
        val tracker = ViewabilityTracker()
        tracker.startTracking(container) {
            val resp = adResponse ?: return@startTracking
            sdk.trackingManager.fireViewable(resp)
            onImpression?.invoke()
        }
        viewabilityTracker = tracker

        return renderedView
    }

    /**
     * Handle click-through.
     */
    private fun handleClick() {
        val response = adResponse ?: return
        val redirectUrl = response.redirectUrl ?: return

        onClick?.invoke()

        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(redirectUrl))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            sdk.context.startActivity(intent)
        } catch (_: Exception) {
            // Silently fail if no browser available
        }
    }

    // --- Auto Refresh ---

    private fun scheduleRefresh(intervalMs: Long, request: AdRequest) {
        stopRefresh()
        val handler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                sdk.trackingManager.reset()
                loadAd(request)
                handler.postDelayed(this, intervalMs)
            }
        }
        this.refreshHandler = handler
        this.refreshRunnable = runnable
        handler.postDelayed(runnable, intervalMs)
    }

    fun stopRefresh() {
        refreshRunnable?.let { refreshHandler?.removeCallbacks(it) }
        refreshHandler = null
        refreshRunnable = null
    }

    fun destroy() {
        stopRefresh()
        viewabilityTracker?.stopTracking()
        loadJob?.cancel()
        scope.cancel()
    }

    // --- WebView ---

    private fun createWebView(container: ViewGroup): WebView {
        return WebView(container.context).apply {
            setBackgroundColor(android.graphics.Color.TRANSPARENT)
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.mediaPlaybackRequiresUserGesture = false
            isVerticalScrollBarEnabled = false
            isHorizontalScrollBarEnabled = false

            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView,
                    request: WebResourceRequest
                ): Boolean {
                    // Handle clicks -- open in external browser
                    onClick?.invoke()
                    val response = adResponse
                    val targetUrl = response?.redirectUrl ?: request.url.toString()
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl))
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        sdk.context.startActivity(intent)
                    } catch (_: Exception) {
                        // Silently fail
                    }
                    return true
                }
            }
        }
    }

    private fun wrapHtml(html: String, width: Int, height: Int): String {
        return """
            <!DOCTYPE html>
            <html>
            <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
            <style>
            * { margin: 0; padding: 0; box-sizing: border-box; }
            body { width: 100%; height: 100%; overflow: hidden; background: transparent; }
            </style>
            </head>
            <body>$html</body>
            </html>
        """.trimIndent()
    }

    // --- Image Loading ---

    private fun loadImage(url: String, imageView: ImageView) {
        scope.launch {
            try {
                val data = sdk.client.fetchData(url)
                val bitmap = withContext(Dispatchers.Default) {
                    BitmapFactory.decodeByteArray(data, 0, data.size)
                }
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap)
                }
            } catch (_: Exception) {
                // Silently fail image load
            }
        }
    }
}
