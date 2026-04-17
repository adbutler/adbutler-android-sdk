package com.adbutler.sdk.core

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

/**
 * Internal HTTP client for making ad serve requests.
 */
internal class AdButlerClient(
    private val baseUrl: String,
    private val logLevel: AdButlerLogLevel
) {
    /** Fetch an ad for the given request. */
    suspend fun fetchAd(request: AdRequest, accountId: Int): AdResponse {
        val url = request.buildUrl(accountId, baseUrl)
        log(AdButlerLogLevel.DEBUG, "Fetching ad: $url")

        val body = performGet(url)
        val response = AdResponse.parseAdServeResponse(body)
        log(AdButlerLogLevel.INFO, "Ad loaded: banner_id=${response.bannerId}, ${response.width}x${response.height}")
        return response
    }

    /** Fetch raw data from a URL (VAST XML, images, etc.). */
    suspend fun fetchData(url: String): ByteArray = withContext(Dispatchers.IO) {
        log(AdButlerLogLevel.DEBUG, "Fetching data: $url")
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.connectTimeout = 10_000
        conn.readTimeout = 15_000
        try {
            if (conn.responseCode != 200) {
                throw AdButlerError.ServerError(conn.responseCode, null)
            }
            conn.inputStream.readBytes()
        } finally {
            conn.disconnect()
        }
    }

    /** Fire a tracking pixel (fire-and-forget GET request). */
    fun firePixel(url: String) {
        log(AdButlerLogLevel.DEBUG, "Firing pixel: $url")
        @Suppress("OPT_IN_USAGE")
        kotlinx.coroutines.GlobalScope.launch(Dispatchers.IO) {
            try {
                val conn = URL(url).openConnection() as HttpURLConnection
                conn.connectTimeout = 5_000
                conn.readTimeout = 5_000
                try {
                    conn.inputStream.readBytes()
                } finally {
                    conn.disconnect()
                }
            } catch (e: Exception) {
                log(AdButlerLogLevel.WARNING, "Pixel fire failed: ${e.message}")
            }
        }
    }

    private suspend fun performGet(url: String): String = withContext(Dispatchers.IO) {
        try {
            val conn = URL(url).openConnection() as HttpURLConnection
            conn.connectTimeout = 10_000
            conn.readTimeout = 15_000
            try {
                if (conn.responseCode != 200) {
                    val errorBody = try { conn.errorStream?.bufferedReader()?.readText() } catch (_: Exception) { null }
                    throw AdButlerError.ServerError(conn.responseCode, errorBody)
                }
                conn.inputStream.bufferedReader().readText()
            } finally {
                conn.disconnect()
            }
        } catch (e: AdButlerError) {
            throw e
        } catch (e: Exception) {
            throw AdButlerError.NetworkError(e)
        }
    }

    private fun log(level: AdButlerLogLevel, message: String) {
        if (level.level > logLevel.level) return
        when (level) {
            AdButlerLogLevel.ERROR -> Log.e("AdButler", message)
            AdButlerLogLevel.WARNING -> Log.w("AdButler", message)
            AdButlerLogLevel.INFO -> Log.i("AdButler", message)
            AdButlerLogLevel.DEBUG -> Log.d("AdButler", message)
            AdButlerLogLevel.NONE -> {}
        }
    }
}
