package com.adbutler.sdk.core

import org.json.JSONObject

/**
 * Parsed response from the AdButler ad serving endpoint.
 */
data class AdResponse(
    /** The ad item (banner) ID. */
    val bannerId: Int,
    /** Direct image URL for display ads. */
    val imageUrl: String?,
    /** Click tracking redirect URL. */
    val redirectUrl: String?,
    /** Impression tracking pixel URL. Fire before rendering. */
    val accupixelUrl: String?,
    /** Viewability callback — fire when ad renders on screen. */
    val eligibleUrl: String?,
    /** Viewability callback — fire when 50%+ visible for 1+ second. */
    val viewableUrl: String?,
    /** Raw HTML body for rich media, custom HTML, or native ads. */
    val body: String?,
    /** Ad width in pixels. */
    val width: Int,
    /** Ad height in pixels. */
    val height: Int,
    /** Third-party tracking pixel URL. */
    val trackingPixel: String?,
    /** URL for auto-refresh. */
    val refreshUrl: String?,
    /** Auto-refresh interval in seconds. */
    val refreshTime: Int?,
    /** Alt text for image ads. */
    val altText: String?,
    /** Link target (_blank, _self, etc.). */
    val target: String?
) {
    /** Whether the ad has HTML body content (rich media / native). */
    val isHtmlAd: Boolean get() = !body.isNullOrEmpty()

    /** Whether the ad has an image URL (display ad). */
    val isImageAd: Boolean get() = !imageUrl.isNullOrEmpty() && !isHtmlAd

    companion object {
        /** Parse a single ad placement from a JSON object. */
        internal fun parse(json: JSONObject): AdResponse {
            if (!json.has("banner_id")) {
                throw AdButlerError.ParseError("Missing banner_id in ad response")
            }

            return AdResponse(
                bannerId = json.getInt("banner_id"),
                imageUrl = json.optString("image_url").takeIf { it.isNotEmpty() },
                redirectUrl = json.optString("redirect_url").takeIf { it.isNotEmpty() },
                accupixelUrl = json.optString("accupixel_url").takeIf { it.isNotEmpty() },
                eligibleUrl = json.optString("eligible_url").takeIf { it.isNotEmpty() },
                viewableUrl = json.optString("viewable_url").takeIf { it.isNotEmpty() },
                body = json.optString("body").takeIf { it.isNotEmpty() },
                width = json.optInt("width", 0),
                height = json.optInt("height", 0),
                trackingPixel = json.optString("tracking_pixel").takeIf { it.isNotEmpty() },
                refreshUrl = json.optString("refresh_url").takeIf { it.isNotEmpty() },
                refreshTime = json.optInt("refresh_time", 0).takeIf { it > 0 },
                altText = json.optString("alt_text").takeIf { it.isNotEmpty() },
                target = json.optString("target").takeIf { it.isNotEmpty() }
            )
        }

        /** Parse the top-level adserve response and extract the first placement. */
        internal fun parseAdServeResponse(responseBody: String): AdResponse {
            val json = JSONObject(responseBody)
            val status = json.optString("status")
            if (status != "SUCCESS") {
                throw AdButlerError.NoAdAvailable()
            }

            val placements = json.optJSONObject("placements")
                ?: throw AdButlerError.ParseError("Missing placements in ad response")

            val firstKey = placements.keys().asSequence().toList().sorted().firstOrNull()
                ?: throw AdButlerError.NoAdAvailable()

            val placement = placements.get(firstKey)

            return when (placement) {
                is JSONObject -> parse(placement)
                is org.json.JSONArray -> {
                    if (placement.length() == 0) throw AdButlerError.NoAdAvailable()
                    parse(placement.getJSONObject(0))
                }
                else -> throw AdButlerError.NoAdAvailable()
            }
        }
    }
}
