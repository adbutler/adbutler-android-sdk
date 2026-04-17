package com.adbutler.sdk.video

/**
 * Parsed VAST response (supports VAST 2.0 and 4.2).
 */
data class VASTResponse(
    /** VAST version detected ("2.0", "3.0", "4.0", "4.2", etc.) */
    val version: String,
    /** Array of ads in the response. */
    val ads: List<VASTAd>
)

/**
 * A single VAST ad.
 */
data class VASTAd(
    /** Ad identifier. */
    val id: String?,
    /** Sequence number for ad pods. */
    val sequence: Int?,
    /** Ad system name (e.g., "AdButler"). */
    val adSystem: String?,
    /** Ad title. */
    val adTitle: String?,
    /** Impression tracking URLs (fire when ad starts). */
    val impressionUrls: List<String>,
    /** Error tracking URL (fire on playback error). */
    val errorUrl: String?,
    /** Linear (video) creative. */
    val linear: VASTLinear?,
    /** Companion ads. */
    val companions: List<VASTCompanion>,
    /** Whether this is a wrapper (redirect to another VAST URL). */
    val isWrapper: Boolean,
    /** Wrapper VAST URL (if isWrapper is true). */
    val wrapperUrl: String?
)

/**
 * Linear (video) creative.
 */
data class VASTLinear(
    /** Video duration in seconds. */
    val duration: Double,
    /** Skip offset in seconds (null = not skippable). */
    val skipOffset: Double?,
    /** Available media files. */
    val mediaFiles: List<VASTMediaFile>,
    /** Tracking events: event name -> list of URLs. */
    val trackingEvents: Map<String, List<String>>,
    /** Click-through destination URL. */
    val clickThrough: String?,
    /** Click tracking URLs. */
    val clickTracking: List<String>
)

/**
 * A single media file option.
 */
data class VASTMediaFile(
    /** Media file URL. */
    val url: String,
    /** MIME type (e.g., "video/mp4"). */
    val mimeType: String,
    /** Video width. */
    val width: Int,
    /** Video height. */
    val height: Int,
    /** Bitrate in kbps (null if not specified). */
    val bitrate: Int?,
    /** Delivery method. */
    val delivery: String,
    /** Video codec. */
    val codec: String?
)

/**
 * Companion ad.
 */
data class VASTCompanion(
    /** Companion width. */
    val width: Int,
    /** Companion height. */
    val height: Int,
    /** Resource type: "static", "iframe", or "html". */
    val resourceType: String,
    /** Resource content (image URL, iframe URL, or HTML string). */
    val content: String,
    /** Click-through URL. */
    val clickThrough: String?,
    /** Tracking pixels for the companion. */
    val trackingUrls: List<String>
)

/**
 * VAST quartile events.
 */
enum class VASTQuartile(val eventName: String) {
    START("start"),
    FIRST_QUARTILE("firstQuartile"),
    MIDPOINT("midpoint"),
    THIRD_QUARTILE("thirdQuartile"),
    COMPLETE("complete")
}
