package com.adbutler.sdk.banner

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.adbutler.sdk.core.AdButler
import com.adbutler.sdk.core.AdButlerError
import com.adbutler.sdk.core.AdRequest
import com.adbutler.sdk.core.AdResponse

/**
 * Android View (FrameLayout) that displays a banner ad.
 * Drop into your layout and call [load] with an [AdRequest].
 *
 * ```kotlin
 * val bannerView = AdButlerBannerView(context)
 * bannerView.listener = object : AdButlerBannerView.Listener {
 *     override fun onAdLoaded(view: AdButlerBannerView, response: AdResponse) { }
 *     override fun onAdFailed(view: AdButlerBannerView, error: AdButlerError) { }
 * }
 * bannerView.load(AdRequest(zoneId = 12345))
 * container.addView(bannerView)
 * ```
 */
class AdButlerBannerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    /**
     * Listener for receiving banner ad events.
     */
    interface Listener {
        /** Called when an ad has been loaded successfully. */
        fun onAdLoaded(view: AdButlerBannerView, response: AdResponse) {}

        /** Called when the ad request failed. */
        fun onAdFailed(view: AdButlerBannerView, error: AdButlerError) {}

        /** Called when a viewable impression has been recorded. */
        fun onImpressionRecorded(view: AdButlerBannerView) {}

        /** Called when the user clicked the ad. */
        fun onClickRecorded(view: AdButlerBannerView) {}
    }

    /** Listener for receiving ad events. */
    var listener: Listener? = null

    /** Whether an ad is currently loaded. */
    var isLoaded: Boolean = false
        private set

    private var controller: BannerAdController? = null

    init {
        clipChildren = true
        setBackgroundColor(android.graphics.Color.TRANSPARENT)
    }

    /**
     * Load an ad for the given request.
     */
    fun load(request: AdRequest) {
        val sdk = try {
            AdButler.instance
        } catch (e: AdButlerError) {
            listener?.onAdFailed(this, e)
            return
        }

        val ctrl = BannerAdController(sdk)
        this.controller = ctrl

        ctrl.onAdLoaded = { response ->
            isLoaded = true

            // Clear previous ad
            removeAllViews()
            ctrl.renderAd(this)

            listener?.onAdLoaded(this, response)
        }

        ctrl.onAdFailed = { error ->
            isLoaded = false
            listener?.onAdFailed(this, error)
        }

        ctrl.onImpression = {
            listener?.onImpressionRecorded(this)
        }

        ctrl.onClick = {
            listener?.onClickRecorded(this)
        }

        ctrl.loadAd(request)
    }

    /**
     * Stop auto-refresh.
     */
    fun stopAutoRefresh() {
        controller?.stopRefresh()
    }

    override fun onDetachedFromWindow() {
        controller?.destroy()
        controller = null
        super.onDetachedFromWindow()
    }
}
