package com.adbutler.sdk.core

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONObject

/**
 * Manages per-banner frequency caps using SharedPreferences.
 */
internal class FrequencyCapManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("adbutler_frequency_caps", Context.MODE_PRIVATE)

    /** Record an impression for the given banner ID. */
    fun recordImpression(bannerId: Int) {
        val key = bannerId.toString()
        val count = prefs.getInt(key, 0)
        prefs.edit().putInt(key, count + 1).apply()
    }

    /** Get the impression count for the given banner ID. */
    fun impressionCount(bannerId: Int): Int {
        return prefs.getInt(bannerId.toString(), 0)
    }

    /** Clear all frequency data. */
    fun clearAll() {
        prefs.edit().clear().apply()
    }
}
