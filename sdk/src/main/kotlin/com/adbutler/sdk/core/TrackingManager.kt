package com.adbutler.sdk.core

/**
 * Manages firing tracking pixels for impressions, viewability, and clicks.
 */
internal class TrackingManager(private val client: AdButlerClient) {
    private val firedPixels = mutableSetOf<String>()

    /** Fire the impression pixel (accupixel_url). Should be called before rendering. */
    fun fireImpression(response: AdResponse) {
        response.accupixelUrl?.let { fireOnce(it) }
        response.trackingPixel?.let { fireOnce(it) }
    }

    /** Fire the eligible URL when the ad view renders on screen. */
    fun fireEligible(response: AdResponse) {
        response.eligibleUrl?.let { fireOnce(it) }
    }

    /** Fire the viewable URL when 50%+ of the ad is visible for 1+ second. */
    fun fireViewable(response: AdResponse) {
        response.viewableUrl?.let { fireOnce(it) }
    }

    /** Fire a tracking URL (VAST events, custom tracking, etc.). */
    fun fireTrackingUrl(url: String) {
        fireOnce(url)
    }

    /** Reset tracked pixels (e.g., on ad refresh). */
    fun reset() {
        firedPixels.clear()
    }

    private fun fireOnce(url: String) {
        if (url in firedPixels) return
        firedPixels.add(url)
        client.firePixel(url)
    }
}
