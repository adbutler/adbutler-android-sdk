package com.adbutler.sdk.nativead

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.adbutler.sdk.core.AdButler
import com.adbutler.sdk.core.AdButlerError
import com.adbutler.sdk.core.AdButlerInstance
import com.adbutler.sdk.core.AdRequest
import com.adbutler.sdk.core.AdResponse
import com.adbutler.sdk.core.ViewabilityTracker

/**
 * A native ad that renders HTML content in a WebView.
 *
 * ```kotlin
 * val nativeAd = AdButlerNativeAd.load(AdRequest(zoneId = 11111))
 * nativeAd.onClick = { /* handle click */ }
 * nativeAd.onImpression = { /* handle impression */ }
 * nativeAd.present(containerView)
 * ```
 */
class AdButlerNativeAd private constructor(
    private val adResponse: AdResponse,
    private val sdk: AdButlerInstance
) {
    /** The raw HTML body of the native ad. */
    val rawHtml: String? = adResponse.body

    /** The click-through redirect URL. */
    val clickUrl: String? = adResponse.redirectUrl

    /** The banner/ad item ID. */
    val bannerId: Int = adResponse.bannerId

    /** Ad width. */
    val width: Int = adResponse.width

    /** Ad height. */
    val height: Int = adResponse.height

    /** Callback when the ad is clicked. */
    var onClick: (() -> Unit)? = null

    /** Callback when a viewable impression is recorded. */
    var onImpression: (() -> Unit)? = null

    private var webView: WebView? = null
    private var viewabilityTracker: ViewabilityTracker? = null

    companion object {
        /**
         * Load a native ad.
         *
         * @param request The ad request configuration.
         * @return A loaded native ad ready to present.
         * @throws AdButlerError if loading fails.
         */
        suspend fun load(request: AdRequest): AdButlerNativeAd {
            val sdk = AdButler.instance
            val response = sdk.client.fetchAd(request, sdk.accountId)

            // Fire impression pixel
            sdk.trackingManager.fireImpression(response)

            return AdButlerNativeAd(response, sdk)
        }
    }

    /**
     * Render the native ad into the given container view using a WebView.
     *
     * @param container The ViewGroup to render the ad into.
     * @return The WebView rendering the ad.
     */
    fun present(container: ViewGroup): WebView {
        // Remove previous
        webView?.let { (it.parent as? ViewGroup)?.removeView(it) }

        val wv = WebView(container.context).apply {
            setBackgroundColor(Color.TRANSPARENT)
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
                    recordClick()
                    return true
                }
            }

            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        container.addView(wv)

        val html = rawHtml ?: ""
        val wrappedHtml = """
            <!DOCTYPE html>
            <html><head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <style>* { margin: 0; padding: 0; box-sizing: border-box; } body { background: transparent; }</style>
            </head><body>$html</body></html>
        """.trimIndent()

        wv.loadDataWithBaseURL(null, wrappedHtml, "text/html", "UTF-8", null)
        this.webView = wv

        // Fire eligible
        sdk.trackingManager.fireEligible(adResponse)

        // Viewability tracking
        val tracker = ViewabilityTracker()
        tracker.startTracking(container) {
            sdk.trackingManager.fireViewable(adResponse)
            onImpression?.invoke()
        }
        viewabilityTracker = tracker

        return wv
    }

    /**
     * Manually record a click (if not using the built-in WebView click handling).
     */
    fun recordClick() {
        val urlStr = clickUrl ?: return

        onClick?.invoke()

        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlStr))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            sdk.context.startActivity(intent)
        } catch (_: Exception) {
            // Silently fail if no browser available
        }
    }

    /**
     * Clean up resources. Call when the native ad is no longer needed.
     */
    fun destroy() {
        viewabilityTracker?.stopTracking()
        viewabilityTracker = null
        webView?.destroy()
        webView = null
    }
}
