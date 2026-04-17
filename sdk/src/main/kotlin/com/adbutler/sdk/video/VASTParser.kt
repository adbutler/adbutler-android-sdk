package com.adbutler.sdk.video

import com.adbutler.sdk.core.AdButlerError
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.ByteArrayInputStream

/**
 * Parses VAST 2.0 and 4.2 XML into a unified [VASTResponse] model.
 * Auto-detects the VAST version from the root element's version attribute.
 * Uses Android's [XmlPullParser] for efficient streaming parsing.
 */
internal class VASTParser {

    /**
     * Parse VAST XML data into a [VASTResponse].
     *
     * @param data Raw XML bytes.
     * @return Parsed VAST response.
     * @throws AdButlerError.VastParseError if parsing fails.
     */
    fun parse(data: ByteArray): VASTResponse {
        try {
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = false
            val parser = factory.newPullParser()
            parser.setInput(ByteArrayInputStream(data), "UTF-8")

            var version = "2.0"
            val ads = mutableListOf<VASTAd>()

            // Current ad state
            var currentAdId: String? = null
            var currentAdSequence: Int? = null
            var currentAdSystem: String? = null
            var currentAdTitle: String? = null
            var currentImpressionUrls = mutableListOf<String>()
            var currentErrorUrl: String? = null
            var currentIsWrapper = false
            var currentWrapperUrl: String? = null

            // Linear state
            var currentLinear: VASTLinear? = null
            var currentDuration: Double = 0.0
            var currentSkipOffset: Double? = null
            var currentMediaFiles = mutableListOf<VASTMediaFile>()
            var currentTrackingEvents = mutableMapOf<String, MutableList<String>>()
            var currentClickThrough: String? = null
            var currentClickTracking = mutableListOf<String>()

            // Media file attributes
            var currentMediaType: String? = null
            var currentMediaWidth = 0
            var currentMediaHeight = 0
            var currentMediaBitrate: Int? = null
            var currentMediaDelivery = "progressive"
            var currentMediaCodec: String? = null

            // Companion state
            var currentCompanions = mutableListOf<VASTCompanion>()
            var currentCompanionWidth = 0
            var currentCompanionHeight = 0
            var currentCompanionResourceType: String? = null
            var currentCompanionContent: String? = null
            var currentCompanionClickThrough: String? = null
            var currentCompanionTrackingUrls = mutableListOf<String>()

            // Tracking event attribute
            var currentTrackingEvent: String? = null

            // Element stack and text buffer
            val elementStack = mutableListOf<String>()
            val textBuffer = StringBuilder()

            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        val name = parser.name
                        elementStack.add(name)
                        textBuffer.setLength(0)

                        when (name) {
                            "VAST" -> {
                                version = parser.getAttributeValue(null, "version") ?: "2.0"
                            }

                            "Ad" -> {
                                currentAdId = parser.getAttributeValue(null, "id")
                                currentAdSequence = parser.getAttributeValue(null, "sequence")?.toIntOrNull()
                                currentAdSystem = null
                                currentAdTitle = null
                                currentImpressionUrls = mutableListOf()
                                currentErrorUrl = null
                                currentLinear = null
                                currentMediaFiles = mutableListOf()
                                currentTrackingEvents = mutableMapOf()
                                currentClickThrough = null
                                currentClickTracking = mutableListOf()
                                currentCompanions = mutableListOf()
                                currentIsWrapper = false
                                currentWrapperUrl = null
                            }

                            "Wrapper" -> {
                                currentIsWrapper = true
                            }

                            "Linear" -> {
                                currentDuration = 0.0
                                currentSkipOffset = null
                                currentMediaFiles = mutableListOf()
                                currentTrackingEvents = mutableMapOf()
                                currentClickThrough = null
                                currentClickTracking = mutableListOf()

                                parser.getAttributeValue(null, "skipoffset")?.let {
                                    currentSkipOffset = parseTimeOffset(it)
                                }
                            }

                            "MediaFile" -> {
                                currentMediaType = parser.getAttributeValue(null, "type") ?: "video/mp4"
                                currentMediaWidth = parser.getAttributeValue(null, "width")?.toIntOrNull() ?: 0
                                currentMediaHeight = parser.getAttributeValue(null, "height")?.toIntOrNull() ?: 0
                                currentMediaBitrate = parser.getAttributeValue(null, "bitrate")?.toIntOrNull()
                                currentMediaDelivery = parser.getAttributeValue(null, "delivery") ?: "progressive"
                                currentMediaCodec = parser.getAttributeValue(null, "codec")
                            }

                            "Tracking" -> {
                                currentTrackingEvent = parser.getAttributeValue(null, "event")
                            }

                            "Companion" -> {
                                currentCompanionWidth = parser.getAttributeValue(null, "width")?.toIntOrNull() ?: 0
                                currentCompanionHeight = parser.getAttributeValue(null, "height")?.toIntOrNull() ?: 0
                                currentCompanionResourceType = null
                                currentCompanionContent = null
                                currentCompanionClickThrough = null
                                currentCompanionTrackingUrls = mutableListOf()
                            }

                            "StaticResource" -> {
                                currentCompanionResourceType = "static"
                            }

                            "IFrameResource" -> {
                                currentCompanionResourceType = "iframe"
                            }

                            "HTMLResource" -> {
                                currentCompanionResourceType = "html"
                            }
                        }
                    }

                    XmlPullParser.TEXT -> {
                        textBuffer.append(parser.text ?: "")
                    }

                    XmlPullParser.CDSECT -> {
                        textBuffer.append(parser.text ?: "")
                    }

                    XmlPullParser.END_TAG -> {
                        val name = parser.name
                        val text = textBuffer.toString().trim()

                        when (name) {
                            "VAST" -> {
                                // Result is built after loop
                            }

                            "Ad" -> {
                                val ad = VASTAd(
                                    id = currentAdId,
                                    sequence = currentAdSequence,
                                    adSystem = currentAdSystem,
                                    adTitle = currentAdTitle,
                                    impressionUrls = currentImpressionUrls.toList(),
                                    errorUrl = currentErrorUrl,
                                    linear = currentLinear,
                                    companions = currentCompanions.toList(),
                                    isWrapper = currentIsWrapper,
                                    wrapperUrl = currentWrapperUrl
                                )
                                ads.add(ad)
                            }

                            "AdSystem" -> {
                                currentAdSystem = text
                            }

                            "AdTitle" -> {
                                currentAdTitle = text
                            }

                            "Impression" -> {
                                if (text.isNotEmpty()) currentImpressionUrls.add(text)
                            }

                            "Error" -> {
                                currentErrorUrl = text
                            }

                            "Duration" -> {
                                currentDuration = parseTimeOffset(text) ?: 0.0
                            }

                            "MediaFile" -> {
                                if (text.isNotEmpty()) {
                                    val mediaFile = VASTMediaFile(
                                        url = text,
                                        mimeType = currentMediaType ?: "video/mp4",
                                        width = currentMediaWidth,
                                        height = currentMediaHeight,
                                        bitrate = currentMediaBitrate,
                                        delivery = currentMediaDelivery,
                                        codec = currentMediaCodec
                                    )
                                    currentMediaFiles.add(mediaFile)
                                }
                            }

                            "Tracking" -> {
                                val event = currentTrackingEvent
                                if (event != null && text.isNotEmpty()) {
                                    currentTrackingEvents.getOrPut(event) { mutableListOf() }.add(text)
                                }
                            }

                            "ClickThrough" -> {
                                if (elementStack.contains("Companion")) {
                                    currentCompanionClickThrough = text
                                } else {
                                    currentClickThrough = text
                                }
                            }

                            "CompanionClickThrough" -> {
                                currentCompanionClickThrough = text
                            }

                            "ClickTracking" -> {
                                if (text.isNotEmpty()) currentClickTracking.add(text)
                            }

                            "Linear" -> {
                                currentLinear = VASTLinear(
                                    duration = currentDuration,
                                    skipOffset = currentSkipOffset,
                                    mediaFiles = currentMediaFiles.toList(),
                                    trackingEvents = currentTrackingEvents.mapValues { it.value.toList() },
                                    clickThrough = currentClickThrough,
                                    clickTracking = currentClickTracking.toList()
                                )
                            }

                            "StaticResource", "IFrameResource", "HTMLResource" -> {
                                currentCompanionContent = text
                            }

                            "Companion" -> {
                                val resourceType = currentCompanionResourceType
                                val content = currentCompanionContent
                                if (resourceType != null && content != null) {
                                    val companion = VASTCompanion(
                                        width = currentCompanionWidth,
                                        height = currentCompanionHeight,
                                        resourceType = resourceType,
                                        content = content,
                                        clickThrough = currentCompanionClickThrough,
                                        trackingUrls = currentCompanionTrackingUrls.toList()
                                    )
                                    currentCompanions.add(companion)
                                }
                            }

                            "VASTAdTagURI" -> {
                                currentWrapperUrl = text
                            }
                        }

                        if (elementStack.isNotEmpty()) {
                            elementStack.removeAt(elementStack.lastIndex)
                        }
                        textBuffer.setLength(0)
                    }
                }

                eventType = parser.next()
            }

            return VASTResponse(version = version, ads = ads)
        } catch (e: AdButlerError) {
            throw e
        } catch (e: Exception) {
            throw AdButlerError.VastParseError("Failed to parse VAST XML: ${e.message}")
        }
    }

    /**
     * Parse a VAST time offset string (HH:MM:SS or HH:MM:SS.mmm).
     */
    private fun parseTimeOffset(str: String): Double? {
        val trimmed = str.trim()

        // Percentage (e.g., "50%") -- not supported standalone
        if (trimmed.endsWith("%")) return null

        // HH:MM:SS or HH:MM:SS.mmm
        val parts = trimmed.split(":")
        if (parts.size != 3) return null

        val hours = parts[0].toDoubleOrNull() ?: 0.0
        val minutes = parts[1].toDoubleOrNull() ?: 0.0
        val seconds = parts[2].toDoubleOrNull() ?: 0.0

        return hours * 3600.0 + minutes * 60.0 + seconds
    }
}
