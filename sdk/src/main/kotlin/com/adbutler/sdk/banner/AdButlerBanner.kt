package com.adbutler.sdk.banner

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.adbutler.sdk.core.AdButlerError
import com.adbutler.sdk.core.AdRequest
import com.adbutler.sdk.core.AdResponse

/**
 * Sealed class representing banner ad events for the Compose API.
 */
sealed class BannerAdEvent {
    /** Ad loaded successfully. */
    data class Loaded(val response: AdResponse) : BannerAdEvent()

    /** Ad request failed. */
    data class Failed(val error: AdButlerError) : BannerAdEvent()

    /** A viewable impression was recorded. */
    data object ImpressionRecorded : BannerAdEvent()

    /** The user clicked the ad. */
    data object ClickRecorded : BannerAdEvent()
}

/**
 * Jetpack Compose composable that displays a banner ad.
 *
 * ```kotlin
 * AdButlerBanner(
 *     request = AdRequest(zoneId = 12345),
 *     modifier = Modifier.fillMaxWidth().height(50.dp),
 *     onEvent = { event ->
 *         when (event) {
 *             is BannerAdEvent.Loaded -> { }
 *             is BannerAdEvent.Failed -> { }
 *             is BannerAdEvent.ImpressionRecorded -> { }
 *             is BannerAdEvent.ClickRecorded -> { }
 *         }
 *     }
 * )
 * ```
 *
 * @param request The ad request configuration.
 * @param modifier Modifier for sizing and layout.
 * @param onEvent Callback for ad events.
 */
@Composable
fun AdButlerBanner(
    request: AdRequest,
    modifier: Modifier = Modifier,
    onEvent: ((BannerAdEvent) -> Unit)? = null
) {
    var bannerView by remember { mutableStateOf<AdButlerBannerView?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            bannerView?.stopAutoRefresh()
        }
    }

    Box(modifier = modifier) {
        AndroidView(
            factory = { context ->
                AdButlerBannerView(context).also { view ->
                    view.listener = object : AdButlerBannerView.Listener {
                        override fun onAdLoaded(
                            view: AdButlerBannerView,
                            response: AdResponse
                        ) {
                            onEvent?.invoke(BannerAdEvent.Loaded(response))
                        }

                        override fun onAdFailed(
                            view: AdButlerBannerView,
                            error: AdButlerError
                        ) {
                            onEvent?.invoke(BannerAdEvent.Failed(error))
                        }

                        override fun onImpressionRecorded(view: AdButlerBannerView) {
                            onEvent?.invoke(BannerAdEvent.ImpressionRecorded)
                        }

                        override fun onClickRecorded(view: AdButlerBannerView) {
                            onEvent?.invoke(BannerAdEvent.ClickRecorded)
                        }
                    }
                    view.load(request)
                    bannerView = view
                }
            },
            update = { /* no-op: ad loaded on create */ }
        )
    }
}
