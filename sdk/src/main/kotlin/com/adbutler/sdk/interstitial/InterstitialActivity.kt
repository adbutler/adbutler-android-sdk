package com.adbutler.sdk.interstitial

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import androidx.activity.ComponentActivity
import com.adbutler.sdk.core.AdResponse
import com.adbutler.sdk.core.ViewabilityTracker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Fullscreen Activity that renders an interstitial ad.
 * Launched internally by [AdButlerInterstitialAd.show].
 */
class InterstitialActivity : ComponentActivity() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var viewabilityTracker: ViewabilityTracker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val interstitialAd = AdButlerInterstitialAd.currentAd
        if (interstitialAd == null) {
            finish()
            return
        }

        val response = interstitialAd.adResponse
        val sdk = interstitialAd.sdk

        // Root container
        val root = FrameLayout(this).apply {
            setBackgroundColor(Color.BLACK)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        // Ad content
        val contentView: android.view.View = if (response.isHtmlAd) {
            createWebView(response, interstitialAd)
        } else if (response.isImageAd) {
            createImageView(response, interstitialAd)
        } else {
            // No renderable content
            finish()
            return
        }

        contentView.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        root.addView(contentView)

        // Close button (top-right)
        val closeButton = createCloseButton()
        closeButton.setOnClickListener {
            interstitialAd.notifyDismissed()
            finish()
        }
        root.addView(closeButton)

        setContentView(root)

        // Fire eligible URL
        sdk.trackingManager.fireEligible(response)

        // Start viewability tracking
        val tracker = ViewabilityTracker()
        tracker.startTracking(root) {
            sdk.trackingManager.fireViewable(response)
            interstitialAd.notifyImpression()
        }
        viewabilityTracker = tracker

        interstitialAd.notifyPresented()
    }

    @Deprecated("Use onBackPressedDispatcher")
    override fun onBackPressed() {
        AdButlerInterstitialAd.currentAd?.notifyDismissed()
        super.onBackPressed()
    }

    override fun onDestroy() {
        viewabilityTracker?.stopTracking()
        scope.cancel()
        super.onDestroy()
    }

    private fun createWebView(
        response: AdResponse,
        interstitialAd: AdButlerInterstitialAd
    ): WebView {
        return WebView(this).apply {
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
                    interstitialAd.notifyClick()
                    val targetUrl = response.redirectUrl ?: request.url.toString()
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl))
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                    } catch (_: Exception) {
                        // Silently fail
                    }
                    return true
                }
            }

            val html = response.body ?: ""
            val wrappedHtml = """
                <!DOCTYPE html>
                <html>
                <head>
                <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
                <style>
                * { margin: 0; padding: 0; box-sizing: border-box; }
                body { width: 100%; height: 100%; overflow: hidden; background: transparent;
                       display: flex; align-items: center; justify-content: center; }
                </style>
                </head>
                <body>$html</body>
                </html>
            """.trimIndent()

            loadDataWithBaseURL(null, wrappedHtml, "text/html", "UTF-8", null)
        }
    }

    private fun createImageView(
        response: AdResponse,
        interstitialAd: AdButlerInterstitialAd
    ): ImageView {
        val imageView = ImageView(this).apply {
            scaleType = ImageView.ScaleType.FIT_CENTER
            isClickable = true
            isFocusable = true
            contentDescription = response.altText ?: "Interstitial Ad"
            setOnClickListener {
                interstitialAd.notifyClick()
                val redirectUrl = response.redirectUrl ?: return@setOnClickListener
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(redirectUrl))
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                } catch (_: Exception) {
                    // Silently fail
                }
            }
        }

        val imageUrl = response.imageUrl
        if (imageUrl != null) {
            scope.launch {
                try {
                    val data = interstitialAd.sdk.client.fetchData(imageUrl)
                    val bitmap = withContext(Dispatchers.Default) {
                        BitmapFactory.decodeByteArray(data, 0, data.size)
                    }
                    if (bitmap != null) {
                        imageView.setImageBitmap(bitmap)
                    }
                } catch (_: Exception) {
                    // Silently fail
                }
            }
        }

        return imageView
    }

    private fun createCloseButton(): ImageButton {
        val sizePx = (40 * resources.displayMetrics.density).toInt()
        val marginPx = (16 * resources.displayMetrics.density).toInt()

        return ImageButton(this).apply {
            // Draw an X using a simple approach
            setBackgroundColor(Color.TRANSPARENT)
            setImageDrawable(CloseDrawable())
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            contentDescription = "Close"

            layoutParams = FrameLayout.LayoutParams(sizePx, sizePx).apply {
                gravity = Gravity.TOP or Gravity.END
                topMargin = marginPx
                marginEnd = marginPx
            }
        }
    }
}

/**
 * Simple drawable that draws an X (close button) icon.
 */
private class CloseDrawable : android.graphics.drawable.Drawable() {
    private val paint = android.graphics.Paint().apply {
        color = Color.WHITE
        strokeWidth = 6f
        isAntiAlias = true
        style = android.graphics.Paint.Style.STROKE
        strokeCap = android.graphics.Paint.Cap.ROUND
    }

    private val bgPaint = android.graphics.Paint().apply {
        color = Color.argb(153, 0, 0, 0) // 60% black
        isAntiAlias = true
        style = android.graphics.Paint.Style.FILL
    }

    override fun draw(canvas: android.graphics.Canvas) {
        val cx = bounds.exactCenterX()
        val cy = bounds.exactCenterY()
        val radius = minOf(bounds.width(), bounds.height()) / 2f

        // Background circle
        canvas.drawCircle(cx, cy, radius, bgPaint)

        // X lines
        val inset = radius * 0.35f
        canvas.drawLine(cx - inset, cy - inset, cx + inset, cy + inset, paint)
        canvas.drawLine(cx + inset, cy - inset, cx - inset, cy + inset, paint)
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
    }

    override fun setColorFilter(colorFilter: android.graphics.ColorFilter?) {
        paint.colorFilter = colorFilter
    }

    @Suppress("DEPRECATION")
    override fun getOpacity(): Int = android.graphics.PixelFormat.TRANSLUCENT
}
