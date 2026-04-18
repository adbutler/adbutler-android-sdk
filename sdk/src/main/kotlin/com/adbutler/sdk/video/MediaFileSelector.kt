package com.adbutler.sdk.video

import android.content.res.Resources

/**
 * Selects the best media file from a VAST response based on device capabilities.
 */
internal object MediaFileSelector {

    /** Supported MIME types in order of preference. */
    private val PREFERRED_TYPES = listOf(
        "video/mp4",
        "video/m4v",
        "video/3gpp",
        "video/webm"
    )

    /**
     * Select the best media file for the current device.
     *
     * @param mediaFiles Available media files from VAST response.
     * @param maxBitrate Maximum acceptable bitrate in kbps (null = no limit).
     * @return The best matching media file, or null if none are compatible.
     */
    fun selectBest(mediaFiles: List<VASTMediaFile>, maxBitrate: Int? = null): VASTMediaFile? {
        if (mediaFiles.isEmpty()) return null

        val screenWidth: Int
        val screenHeight: Int
        try {
            val dm = Resources.getSystem().displayMetrics
            screenWidth = (dm.widthPixels * dm.density).toInt()
            screenHeight = (dm.heightPixels * dm.density).toInt()
        } catch (_: Exception) {
            // Fallback for unit tests or non-Android environments
            return selectBestByPreference(mediaFiles, maxBitrate)
        }

        if (screenWidth == 0 && screenHeight == 0) {
            return selectBestByPreference(mediaFiles, maxBitrate)
        }

        return selectBestForScreen(mediaFiles, screenWidth, screenHeight, maxBitrate)
    }

    private fun selectBestByPreference(
        mediaFiles: List<VASTMediaFile>,
        maxBitrate: Int?
    ): VASTMediaFile? {
        val compatible = mediaFiles.filter { PREFERRED_TYPES.contains(it.mimeType.lowercase()) }
        val pool = compatible.ifEmpty {
            return mediaFiles.firstOrNull { it.delivery == "progressive" } ?: mediaFiles.first()
        }

        val filtered = if (maxBitrate != null) {
            pool.filter { (it.bitrate ?: 0) <= maxBitrate }.ifEmpty {
                return pool.minByOrNull { it.bitrate ?: 0 }
            }
        } else {
            pool
        }

        // Prefer highest bitrate mp4
        return filtered
            .sortedWith(compareBy<VASTMediaFile> {
                PREFERRED_TYPES.indexOf(it.mimeType.lowercase()).let { idx -> if (idx < 0) 999 else idx }
            }.thenByDescending { it.bitrate ?: 0 })
            .first()
    }

    private fun selectBestForScreen(
        mediaFiles: List<VASTMediaFile>,
        screenWidth: Int,
        screenHeight: Int,
        maxBitrate: Int?
    ): VASTMediaFile? {
        val compatible = mediaFiles.filter { PREFERRED_TYPES.contains(it.mimeType.lowercase()) }
        if (compatible.isEmpty()) {
            return mediaFiles.firstOrNull { it.delivery == "progressive" } ?: mediaFiles.first()
        }

        val bitrateFiltered = if (maxBitrate != null) {
            compatible.filter { (it.bitrate ?: 0) <= maxBitrate }.ifEmpty {
                return compatible.minByOrNull { it.bitrate ?: 0 }
            }
        } else {
            compatible
        }

        return bitrateFiltered.minByOrNull { file ->
            val dimScore = Math.abs(file.width - screenWidth) + Math.abs(file.height - screenHeight)
            val bitrateBonus = file.bitrate ?: 0
            dimScore - bitrateBonus / 10
        }
    }
}
