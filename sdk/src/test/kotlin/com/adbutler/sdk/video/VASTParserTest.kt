package com.adbutler.sdk.video

import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class VASTParserTest {

    @Test
    fun `parse VAST 4_2`() {
        val xml = """
        <?xml version="1.0" encoding="UTF-8"?>
        <VAST version="4.2">
          <Ad id="ad-001" sequence="1">
            <InLine>
              <AdSystem>AdButler</AdSystem>
              <AdTitle>Test Ad</AdTitle>
              <Impression><![CDATA[https://example.com/impression]]></Impression>
              <Error><![CDATA[https://example.com/error]]></Error>
              <Creatives>
                <Creative>
                  <Linear skipoffset="00:00:05">
                    <Duration>00:00:30</Duration>
                    <MediaFiles>
                      <MediaFile type="video/mp4" width="1280" height="720" delivery="progressive" bitrate="1200">
                        <![CDATA[https://example.com/video.mp4]]>
                      </MediaFile>
                    </MediaFiles>
                    <TrackingEvents>
                      <Tracking event="start"><![CDATA[https://example.com/start]]></Tracking>
                      <Tracking event="firstQuartile"><![CDATA[https://example.com/q1]]></Tracking>
                      <Tracking event="midpoint"><![CDATA[https://example.com/mid]]></Tracking>
                      <Tracking event="thirdQuartile"><![CDATA[https://example.com/q3]]></Tracking>
                      <Tracking event="complete"><![CDATA[https://example.com/complete]]></Tracking>
                    </TrackingEvents>
                    <VideoClicks>
                      <ClickThrough><![CDATA[https://example.com/click-through]]></ClickThrough>
                      <ClickTracking><![CDATA[https://example.com/click-track]]></ClickTracking>
                    </VideoClicks>
                  </Linear>
                </Creative>
              </Creatives>
            </InLine>
          </Ad>
        </VAST>
        """.trimIndent()

        val parser = VASTParser()
        val response = parser.parse(xml.toByteArray())

        assertEquals("4.2", response.version)
        assertEquals(1, response.ads.size)

        val ad = response.ads[0]
        assertEquals("ad-001", ad.id)
        assertEquals(1, ad.sequence)
        assertEquals("AdButler", ad.adSystem)
        assertEquals("Test Ad", ad.adTitle)
        assertEquals(listOf("https://example.com/impression"), ad.impressionUrls)
        assertEquals("https://example.com/error", ad.errorUrl)
        assertFalse(ad.isWrapper)

        val linear = ad.linear!!
        assertEquals(30.0, linear.duration, 0.01)
        assertEquals(5.0, linear.skipOffset!!, 0.01)
        assertEquals(1, linear.mediaFiles.size)
        assertEquals("https://example.com/click-through", linear.clickThrough)
        assertEquals(listOf("https://example.com/click-track"), linear.clickTracking)

        val media = linear.mediaFiles[0]
        assertEquals("https://example.com/video.mp4", media.url)
        assertEquals("video/mp4", media.mimeType)
        assertEquals(1280, media.width)
        assertEquals(720, media.height)
        assertEquals(1200, media.bitrate)
        assertEquals("progressive", media.delivery)

        assertEquals(1, linear.trackingEvents["start"]?.size)
        assertEquals(1, linear.trackingEvents["firstQuartile"]?.size)
        assertEquals(1, linear.trackingEvents["midpoint"]?.size)
        assertEquals(1, linear.trackingEvents["thirdQuartile"]?.size)
        assertEquals(1, linear.trackingEvents["complete"]?.size)
    }

    @Test
    fun `parse VAST 2_0`() {
        val xml = """
        <?xml version="1.0" encoding="UTF-8"?>
        <VAST version="2.0">
          <Ad id="v2-ad">
            <InLine>
              <AdSystem>LegacyAdServer</AdSystem>
              <AdTitle>VAST 2.0 Ad</AdTitle>
              <Impression><![CDATA[https://example.com/v2/impression]]></Impression>
              <Creatives>
                <Creative>
                  <Linear>
                    <Duration>00:00:15</Duration>
                    <MediaFiles>
                      <MediaFile type="video/mp4" width="640" height="480" delivery="progressive">
                        <![CDATA[https://example.com/v2/video.mp4]]>
                      </MediaFile>
                    </MediaFiles>
                    <TrackingEvents>
                      <Tracking event="start"><![CDATA[https://example.com/v2/start]]></Tracking>
                      <Tracking event="complete"><![CDATA[https://example.com/v2/complete]]></Tracking>
                    </TrackingEvents>
                    <VideoClicks>
                      <ClickThrough><![CDATA[https://example.com/v2/click]]></ClickThrough>
                    </VideoClicks>
                  </Linear>
                </Creative>
              </Creatives>
            </InLine>
          </Ad>
        </VAST>
        """.trimIndent()

        val parser = VASTParser()
        val response = parser.parse(xml.toByteArray())

        assertEquals("2.0", response.version)
        assertEquals(1, response.ads.size)

        val ad = response.ads[0]
        assertEquals("v2-ad", ad.id)
        assertNull(ad.linear?.skipOffset)

        val linear = ad.linear!!
        assertEquals(15.0, linear.duration, 0.01)
        assertEquals(1, linear.mediaFiles.size)
        assertEquals(640, linear.mediaFiles[0].width)
        assertEquals(480, linear.mediaFiles[0].height)
    }

    @Test
    fun `parse wrapper`() {
        val xml = """
        <?xml version="1.0" encoding="UTF-8"?>
        <VAST version="4.2">
          <Ad id="wrapper-ad">
            <Wrapper>
              <AdSystem>Wrapper System</AdSystem>
              <Impression><![CDATA[https://wrapper.com/impression]]></Impression>
              <VASTAdTagURI><![CDATA[https://adserver.com/vast/inline.xml]]></VASTAdTagURI>
            </Wrapper>
          </Ad>
        </VAST>
        """.trimIndent()

        val parser = VASTParser()
        val response = parser.parse(xml.toByteArray())

        val ad = response.ads[0]
        assertTrue(ad.isWrapper)
        assertEquals("https://adserver.com/vast/inline.xml", ad.wrapperUrl)
        assertEquals(listOf("https://wrapper.com/impression"), ad.impressionUrls)
    }

    @Test
    fun `parse companion ads`() {
        val xml = """
        <?xml version="1.0" encoding="UTF-8"?>
        <VAST version="4.2">
          <Ad id="comp-ad">
            <InLine>
              <AdSystem>AdButler</AdSystem>
              <Impression><![CDATA[https://example.com/imp]]></Impression>
              <Creatives>
                <Creative>
                  <Linear>
                    <Duration>00:00:10</Duration>
                    <MediaFiles>
                      <MediaFile type="video/mp4" width="640" height="360" delivery="progressive">
                        <![CDATA[https://example.com/v.mp4]]>
                      </MediaFile>
                    </MediaFiles>
                  </Linear>
                </Creative>
                <Creative>
                  <CompanionAds>
                    <Companion width="300" height="250">
                      <StaticResource creativeType="image/png"><![CDATA[https://example.com/companion.png]]></StaticResource>
                      <CompanionClickThrough><![CDATA[https://example.com/comp-click]]></CompanionClickThrough>
                    </Companion>
                    <Companion width="728" height="90">
                      <HTMLResource><![CDATA[<div>HTML Companion</div>]]></HTMLResource>
                    </Companion>
                  </CompanionAds>
                </Creative>
              </Creatives>
            </InLine>
          </Ad>
        </VAST>
        """.trimIndent()

        val parser = VASTParser()
        val response = parser.parse(xml.toByteArray())

        val ad = response.ads[0]
        assertEquals(2, ad.companions.size)

        val static300 = ad.companions[0]
        assertEquals(300, static300.width)
        assertEquals(250, static300.height)
        assertEquals("static", static300.resourceType)
        assertEquals("https://example.com/companion.png", static300.content)
        assertEquals("https://example.com/comp-click", static300.clickThrough)

        val html728 = ad.companions[1]
        assertEquals(728, html728.width)
        assertEquals(90, html728.height)
        assertEquals("html", html728.resourceType)
        assertEquals("<div>HTML Companion</div>", html728.content)
    }
}
