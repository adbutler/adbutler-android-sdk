package com.adbutler.sdk.video

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.adbutler.sdk.core.AdButlerError

/**
 * Sealed class representing VAST player events for the Compose API.
 */
sealed class VASTPlayerEvent {
    /** VAST ad loaded and ready to play. */
    data class Loaded(val ad: VASTAd) : VASTPlayerEvent()

    /** Video playback started. */
    data object Started : VASTPlayerEvent()

    /** A quartile milestone was reached. */
    data class QuartileReached(val quartile: VASTQuartile) : VASTPlayerEvent()

    /** Video playback completed. */
    data object Completed : VASTPlayerEvent()

    /** User clicked the video ad. */
    data object Clicked : VASTPlayerEvent()

    /** User skipped the ad. */
    data object Skipped : VASTPlayerEvent()

    /** An error occurred. */
    data class Error(val error: AdButlerError) : VASTPlayerEvent()

    /** A companion ad is available to display. */
    data class CompanionAvailable(val companion: VASTCompanion) : VASTPlayerEvent()
}

/**
 * Jetpack Compose composable that wraps [AdButlerVASTPlayerView].
 *
 * Load by zone ID:
 * ```kotlin
 * AdButlerVASTPlayer(
 *     zoneId = 99999,
 *     modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f),
 *     autoPlay = true,
 *     onEvent = { event ->
 *         when (event) {
 *             is VASTPlayerEvent.Loaded -> { }
 *             is VASTPlayerEvent.Started -> { }
 *             is VASTPlayerEvent.Completed -> { }
 *             else -> { }
 *         }
 *     }
 * )
 * ```
 *
 * Load by VAST URL:
 * ```kotlin
 * AdButlerVASTPlayer(
 *     vastUrl = "https://example.com/vast.xml",
 *     modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f),
 *     onEvent = { event -> }
 * )
 * ```
 *
 * @param zoneId The VAST zone ID to load from (mutually exclusive with vastUrl).
 * @param vastUrl Direct VAST XML URL to load (mutually exclusive with zoneId).
 * @param modifier Modifier for sizing and layout.
 * @param autoPlay Whether to start playback automatically after loading.
 * @param onEvent Callback for player events.
 */
@Composable
fun AdButlerVASTPlayer(
    modifier: Modifier = Modifier,
    zoneId: Int? = null,
    vastUrl: String? = null,
    autoPlay: Boolean = true,
    onEvent: ((VASTPlayerEvent) -> Unit)? = null
) {
    val playerViewRef = remember { arrayOfNulls<AdButlerVASTPlayerView>(1) }

    DisposableEffect(Unit) {
        onDispose {
            playerViewRef[0]?.release()
        }
    }

    Box(modifier = modifier) {
        AndroidView(
            factory = { context ->
                AdButlerVASTPlayerView(context).also { view ->
                    playerViewRef[0] = view

                    view.listener = object : AdButlerVASTPlayerView.VASTPlayerListener {
                        override fun onLoaded(
                            player: AdButlerVASTPlayerView,
                            ad: VASTAd
                        ) {
                            onEvent?.invoke(VASTPlayerEvent.Loaded(ad))
                            if (autoPlay) {
                                player.play()
                            }
                        }

                        override fun onStarted(player: AdButlerVASTPlayerView) {
                            onEvent?.invoke(VASTPlayerEvent.Started)
                        }

                        override fun onQuartileReached(
                            player: AdButlerVASTPlayerView,
                            quartile: VASTQuartile
                        ) {
                            onEvent?.invoke(VASTPlayerEvent.QuartileReached(quartile))
                        }

                        override fun onCompleted(player: AdButlerVASTPlayerView) {
                            onEvent?.invoke(VASTPlayerEvent.Completed)
                        }

                        override fun onClicked(player: AdButlerVASTPlayerView) {
                            onEvent?.invoke(VASTPlayerEvent.Clicked)
                        }

                        override fun onSkipped(player: AdButlerVASTPlayerView) {
                            onEvent?.invoke(VASTPlayerEvent.Skipped)
                        }

                        override fun onError(
                            player: AdButlerVASTPlayerView,
                            error: AdButlerError
                        ) {
                            onEvent?.invoke(VASTPlayerEvent.Error(error))
                        }

                        override fun onCompanionAvailable(
                            player: AdButlerVASTPlayerView,
                            companion: VASTCompanion
                        ) {
                            onEvent?.invoke(VASTPlayerEvent.CompanionAvailable(companion))
                        }
                    }

                    // Load the ad
                    when {
                        zoneId != null -> view.load(zoneId)
                        vastUrl != null -> view.load(vastUrl)
                    }
                }
            },
            update = { /* no-op: ad loaded on create */ }
        )
    }
}
