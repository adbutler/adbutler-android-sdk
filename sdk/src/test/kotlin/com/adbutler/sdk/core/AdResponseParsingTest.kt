package com.adbutler.sdk.core

import org.junit.Assert.*
import org.junit.Test

class AdResponseParsingTest {

    @Test
    fun `parse successful image response`() {
        val json = """
        {
          "status": "SUCCESS",
          "placements": {
            "placement_1": {
              "banner_id": 123456,
              "width": 300,
              "height": 250,
              "image_url": "https://example.com/ad.png",
              "redirect_url": "https://example.com/click",
              "accupixel_url": "https://example.com/impression",
              "eligible_url": "https://example.com/eligible",
              "viewable_url": "https://example.com/viewable",
              "body": "",
              "tracking_pixel": "https://example.com/pixel",
              "refresh_time": 30,
              "alt_text": "Test Ad"
            }
          }
        }
        """.trimIndent()

        val response = AdResponse.parseAdServeResponse(json)

        assertEquals(123456, response.bannerId)
        assertEquals(300, response.width)
        assertEquals(250, response.height)
        assertEquals("https://example.com/ad.png", response.imageUrl)
        assertEquals("https://example.com/click", response.redirectUrl)
        assertEquals("https://example.com/impression", response.accupixelUrl)
        assertEquals("https://example.com/eligible", response.eligibleUrl)
        assertEquals("https://example.com/viewable", response.viewableUrl)
        assertEquals("https://example.com/pixel", response.trackingPixel)
        assertEquals(30, response.refreshTime)
        assertTrue(response.isImageAd)
        assertFalse(response.isHtmlAd)
    }

    @Test
    fun `parse html body response`() {
        val json = """
        {
          "status": "SUCCESS",
          "placements": {
            "placement_1": {
              "banner_id": 789,
              "width": 320,
              "height": 50,
              "image_url": "",
              "body": "<div>Native Ad Content</div>",
              "redirect_url": "https://example.com/click"
            }
          }
        }
        """.trimIndent()

        val response = AdResponse.parseAdServeResponse(json)

        assertEquals(789, response.bannerId)
        assertTrue(response.isHtmlAd)
        assertFalse(response.isImageAd)
        assertEquals("<div>Native Ad Content</div>", response.body)
    }

    @Test(expected = AdButlerError.NoAdAvailable::class)
    fun `parse no ad available`() {
        val json = """{"status": "NO_ADS", "placements": {}}"""
        AdResponse.parseAdServeResponse(json)
    }

    @Test
    fun `parse array placement`() {
        val json = """
        {
          "status": "SUCCESS",
          "placements": {
            "placement_1": [
              {
                "banner_id": 111,
                "width": 728,
                "height": 90,
                "image_url": "https://example.com/leaderboard.png",
                "redirect_url": "https://example.com/click"
              }
            ]
          }
        }
        """.trimIndent()

        val response = AdResponse.parseAdServeResponse(json)
        assertEquals(111, response.bannerId)
        assertEquals(728, response.width)
        assertEquals(90, response.height)
    }

    @Test(expected = Exception::class)
    fun `parse invalid json`() {
        AdResponse.parseAdServeResponse("not json")
    }
}
