package com.adbutler.sdk.core

import android.content.Context
import android.util.Log

/**
 * Main entry point for the AdButler SDK.
 * Call [AdButler.initialize] before using any ad components.
 */
object AdButler {
    private var _instance: AdButlerInstance? = null

    internal val instance: AdButlerInstance
        get() = _instance ?: throw AdButlerError.NotConfigured()

    val isInitialized: Boolean get() = _instance != null

    /**
     * Initialize the AdButler SDK.
     * Must be called before creating any ad views, typically in Application.onCreate().
     *
     * @param context Application context.
     * @param accountId Your AdButler account ID.
     * @param options Optional configuration.
     */
    @JvmStatic
    @JvmOverloads
    fun initialize(
        context: Context,
        accountId: Int,
        options: AdButlerOptions = AdButlerOptions()
    ) {
        _instance = AdButlerInstance(
            context = context.applicationContext,
            accountId = accountId,
            options = options
        )
        if (options.logLevel >= AdButlerLogLevel.INFO) {
            Log.i("AdButler", "SDK initialized for account $accountId")
        }
    }
}

internal class AdButlerInstance(
    val context: Context,
    val accountId: Int,
    val options: AdButlerOptions
) {
    val client = AdButlerClient(options.baseUrl, options.logLevel)
    val trackingManager = TrackingManager(client)
    val frequencyCapManager = FrequencyCapManager(context)
}

/**
 * Configuration options for the AdButler SDK.
 */
data class AdButlerOptions(
    /** Base URL for ad serving. Override for testing. */
    val baseUrl: String = "https://servedbyadbutler.com",
    /** Enable test mode (no real impressions tracked). */
    val testMode: Boolean = false,
    /** Log level for SDK diagnostics. */
    val logLevel: AdButlerLogLevel = AdButlerLogLevel.NONE
)

enum class AdButlerLogLevel(val level: Int) {
    NONE(0), ERROR(1), WARNING(2), INFO(3), DEBUG(4)
}
