package com.adbutler.sdk.core

/**
 * Errors thrown by the AdButler SDK.
 */
sealed class AdButlerError(message: String, cause: Throwable? = null) : Exception(message, cause) {
    /** SDK has not been initialized. Call AdButler.initialize() first. */
    class NotConfigured : AdButlerError("AdButler SDK not initialized. Call AdButler.initialize() first.")

    /** Network request failed. */
    class NetworkError(cause: Throwable) : AdButlerError("Network error: ${cause.message}", cause)

    /** Server returned a non-success HTTP status. */
    class ServerError(val statusCode: Int, val body: String?) :
        AdButlerError("Server error ($statusCode): ${body ?: "no details"}")

    /** Failed to parse the ad response JSON. */
    class ParseError(detail: String) : AdButlerError("Parse error: $detail")

    /** No ad available for the requested zone. */
    class NoAdAvailable : AdButlerError("No ad available for the requested zone.")

    /** VAST XML parsing failed. */
    class VastParseError(detail: String) : AdButlerError("VAST parse error: $detail")

    /** No compatible media file found in VAST response. */
    class NoCompatibleMedia : AdButlerError("No compatible media file found in VAST response.")

    /** Ad request was cancelled. */
    class Cancelled : AdButlerError("Ad request was cancelled.")

    /** An invalid argument was provided. */
    class InvalidArgument(detail: String) : AdButlerError("Invalid argument: $detail")
}
