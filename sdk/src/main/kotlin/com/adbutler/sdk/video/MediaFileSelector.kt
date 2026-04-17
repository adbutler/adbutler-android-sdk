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

        val dm = Resources.getSystem().displayMetrics
        val screenWidth = (dm.widthPixels * dm.density).toInt()
        val screenHeight = (dm.heightPixels * dm.density).toInt()

        // Filter to supported MIME types
        val compatible = mediaFiles.filter { file ->
            PREFERRED_TYPES.contains(file.mimeType.lowercase())
        }

        if (compatible.isEmpty()) {
            // Fall back to any progressive media file
            return mediaFiles.firstOrNull { it.delivery == "progressive" } ?: mediaFiles.first()
        }

        // Filter by bitrate if max specified
        val bitrateFiltered: List<VASTMediaFile>
        if (maxBitrate != null) {
            bitrateFiltered = compatible.filter { (it.bitrate ?: 0) <= maxBitrate }
            if (bitrateFiltered.isEmpty()) {
                // All files exceed bitrate, use the lowest bitrate one
                return compatible.minByOrNull { it.bitrate ?: 0 }
            }
        } else {
            bitrateFiltered = compatible
        }

        // Score by closeness to screen dimensions + prefer higher bitrate
        return bitrateFiltered.minByOrNull { file ->
            val dimScore = Math.abs(file.width - screenWidth) + Math.abs(file.height - screenHeight)
            val bitrateBonus = file.bitrate ?: 0
            // Lower dimScore = better fit. Higher bitrate = better quality (subtract from score).
            dimScore - bitrateBonus / 10
        }
    }
}
