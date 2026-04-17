package com.adbutler.sdk.interstitial

import android.app.Activity
import android.content.Intent
import com.adbutler.sdk.core.AdButler
import com.adbutler.sdk.core.AdButlerError
import com.adbutler.sdk.core.AdButlerInstance
import com.adbutler.sdk.core.AdRequest
import com.adbutler.sdk.core.AdResponse

/**
 * A fullscreen interstitial ad.
 *
 * ```kotlin
 * val ad = AdButlerInterstitialAd.load(AdRequest(zoneId = 67890))
 * ad.listener = object : AdButlerInterstitialAd.Listener { ... }
 * ad.show(activity)
 * ```
 */
class AdButlerInterstitialAd private constructor(
    internal val adResponse: AdResponse,
    internal val sdk: AdButlerInstance
) {
    /**
     * Listener for receiving interstitial ad events.
     */
    interface Listener {
        /** Called when the interstitial is presented fullscreen. */
        fun onPresented(ad: AdButlerInterstitialAd) {}

        /** Called when the interstitial is dismissed. */
        fun onDismissed(ad: AdButlerInterstitialAd) {}

        /** Called when a viewable impression has been recorded. */
        fun onImpressionRecorded(ad: AdButlerInterstitialAd) {}

        /** Called when the user clicked the ad. */
        fun onClickRecorded(ad: AdButlerInterstitialAd) {}
    }

    /** Listener for receiving interstitial events. */
    var listener: Listener? = null

    /** Whether the ad is ready to be presented. */
    var isReady: Boolean = true
        private set

    companion object {
        /** Currently showing interstitial ad (used by InterstitialActivity). */
        internal var currentAd: AdButlerInterstitialAd? = null

        /**
         * Load an interstitial ad.
         *
         * @param request The ad request configuration.
         * @return A loaded interstitial ad ready to present.
         * @throws AdButlerError if loading fails.
         */
        suspend fun load(request: AdRequest): AdButlerInterstitialAd {
            val sdk = AdButler.instance
            val response = sdk.client.fetchAd(request, sdk.accountId)

            // Fire impression pixel on load
            sdk.trackingManager.fireImpression(response)

            return AdButlerInterstitialAd(response, sdk)
        }
    }

    /**
     * Present the interstitial ad fullscreen.
     *
     * @param activity The activity to launch the interstitial from.
     */
    fun show(activity: Activity) {
        if (!isReady) return
        isReady = false

        currentAd = this

        val intent = Intent(activity, InterstitialActivity::class.java)
        activity.startActivity(intent)
    }

    internal fun notifyPresented() {
        listener?.onPresented(this)
    }

    internal fun notifyDismissed() {
        currentAd = null
        listener?.onDismissed(this)
    }

    internal fun notifyImpression() {
        listener?.onImpressionRecorded(this)
    }

    internal fun notifyClick() {
        listener?.onClickRecorded(this)
    }
}
