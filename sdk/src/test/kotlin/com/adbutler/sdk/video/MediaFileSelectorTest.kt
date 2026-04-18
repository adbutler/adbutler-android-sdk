package com.adbutler.sdk.video

import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MediaFileSelectorTest {

    private val files = listOf(
        VASTMediaFile("https://example.com/1080p.mp4", "video/mp4", 1920, 1080, 2000, "progressive", "H.264"),
        VASTMediaFile("https://example.com/720p.mp4", "video/mp4", 1280, 720, 1200, "progressive", null),
        VASTMediaFile("https://example.com/360p.mp4", "video/mp4", 640, 360, 600, "progressive", null),
        VASTMediaFile("https://example.com/720p.webm", "video/webm", 1280, 720, 1000, "progressive", null),
    )

    @Test
    fun `selects best fit mp4`() {
        val selected = MediaFileSelector.selectBest(files)
        assertNotNull(selected)
        assertEquals("video/mp4", selected?.mimeType)
    }

    @Test
    fun `respects bitrate filter`() {
        val selected = MediaFileSelector.selectBest(files, maxBitrate = 800)
        assertNotNull(selected)
        assertEquals("https://example.com/360p.mp4", selected?.url)
    }

    @Test
    fun `returns null for empty files`() {
        val selected = MediaFileSelector.selectBest(emptyList())
        assertNull(selected)
    }

    @Test
    fun `handles unsupported mime types`() {
        val unsupported = listOf(
            VASTMediaFile("https://example.com/ad.flv", "video/x-flv", 640, 480, 800, "progressive", null)
        )
        val selected = MediaFileSelector.selectBest(unsupported)
        // Falls back to any progressive file
        assertNotNull(selected)
    }

    @Test
    fun `prefers mp4 over webm`() {
        val mixed = listOf(
            VASTMediaFile("https://example.com/720p.webm", "video/webm", 1280, 720, 1200, "progressive", null),
            VASTMediaFile("https://example.com/720p.mp4", "video/mp4", 1280, 720, 1200, "progressive", null),
        )
        val selected = MediaFileSelector.selectBest(mixed)
        assertEquals("video/mp4", selected?.mimeType)
    }
}
