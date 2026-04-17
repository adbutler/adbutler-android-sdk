package com.adbutler.sdk.core

import android.content.res.Resources
import org.json.JSONObject
import java.net.URLEncoder

/**
 * A request for an ad from AdButler.
 * Use [Builder] to construct with targeting parameters.
 */
class AdRequest private constructor(
    val zoneId: Int,
    val keywords: List<String>?,
    val width: Int?,
    val height: Int?,
    val dataKeyTargeting: Map<String, String>?,
    val referrer: String?,
    val pageId: Int?,
    val place: Int?
) {
    /**
     * Convenience constructor for simple requests.
     */
    constructor(zoneId: Int) : this(zoneId, null, null, null, null, null, null, null)

    class Builder(private val zoneId: Int) {
        private var keywords: List<String>? = null
        private var width: Int? = null
        private var height: Int? = null
        private var dataKeyTargeting: Map<String, String>? = null
        private var referrer: String? = null
        private var pageId: Int? = null
        private var place: Int? = null

        fun keywords(keywords: List<String>) = apply { this.keywords = keywords }
        fun size(width: Int, height: Int) = apply { this.width = width; this.height = height }
        fun dataKeyTargeting(targeting: Map<String, String>) = apply { this.dataKeyTargeting = targeting }
        fun referrer(url: String) = apply { this.referrer = url }
        fun uniqueDelivery(pageId: Int, place: Int) = apply { this.pageId = pageId; this.place = place }

        fun build() = AdRequest(zoneId, keywords, width, height, dataKeyTargeting, referrer, pageId, place)
    }

    internal fun buildUrl(accountId: Int, baseUrl: String): String {
        val sb = StringBuilder("$baseUrl/adserve/;ID=$accountId;setID=$zoneId;type=json")

        keywords?.takeIf { it.isNotEmpty() }?.let {
            sb.append(";kw=${it.joinToString(",")}")
        }

        if (width != null && height != null) {
            sb.append(";size=${width}x${height}")
        }

        pageId?.let { sb.append(";pid=$it") }
        place?.let { sb.append(";place=$it") }

        val params = mutableListOf<String>()

        val dm = Resources.getSystem().displayMetrics
        params.add("sw=${dm.widthPixels}")
        params.add("sh=${dm.heightPixels}")
        params.add("spr=${dm.density.toInt()}")

        referrer?.let {
            params.add("referrer=${URLEncoder.encode(it, "UTF-8")}")
        }

        dataKeyTargeting?.takeIf { it.isNotEmpty() }?.let {
            val json = JSONObject(it as Map<*, *>).toString()
            params.add("_abdk_json=${URLEncoder.encode(json, "UTF-8")}")
        }

        if (params.isNotEmpty()) {
            sb.append("?${params.joinToString("&")}")
        }

        return sb.toString()
    }
}
