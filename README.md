# AdButler Android SDK

Native Android SDK for serving display, native, and VAST video ads from [AdButler](https://www.adbutler.com).

[![CI](https://github.com/adbutler/adbutler-android-sdk/actions/workflows/ci.yml/badge.svg)](https://github.com/adbutler/adbutler-android-sdk/actions/workflows/ci.yml)

## Features

- **Banner Ads** — Inline display ads with auto-refresh (XML Views + Jetpack Compose)
- **Interstitial Ads** — Fullscreen ads with coroutine-based load/show pattern
- **Native Ads** — HTML-rendered native ads in WebView
- **VAST Video Ads** — Built-in video player supporting VAST 2.0 and 4.2
  - Quartile tracking (start, 25%, 50%, 75%, complete)
  - Skip button with countdown
  - Companion ads
  - Wrapper/redirect chain following
- **Viewability Tracking** — MRC standard (50%+ visible for 1+ second)
- **Impression & Click Tracking** — Automatic pixel firing

## Requirements

- Android API 24+ (Android 7.0)
- Kotlin 1.9+
- Jetpack Compose (optional, for Compose APIs)

## Installation

### Gradle (Maven Central)

```kotlin
// build.gradle.kts
dependencies {
    implementation("com.adbutler:adbutler-android-sdk:1.0.0")
}
```

### Gradle (Groovy)

```groovy
// build.gradle
dependencies {
    implementation 'com.adbutler:adbutler-android-sdk:1.0.0'
}
```

## Quick Start

### 1. Configure the SDK

Call this once at app launch, before using any ad components.

```kotlin
import com.adbutler.sdk.core.AdButler
import com.adbutler.sdk.core.AdButlerOptions
import com.adbutler.sdk.core.AdButlerLogLevel

// In your Application.onCreate()
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AdButler.initialize(
            context = this,
            accountId = 182804
        )

        // With options
        AdButler.initialize(
            context = this,
            accountId = 182804,
            options = AdButlerOptions(
                testMode = false,
                logLevel = AdButlerLogLevel.DEBUG
            )
        )
    }
}
```

### 2. Show a Banner Ad

#### Jetpack Compose

```kotlin
import com.adbutler.sdk.banner.AdButlerBanner
import com.adbutler.sdk.banner.BannerAdEvent
import com.adbutler.sdk.core.AdRequest

@Composable
fun MyScreen() {
    Column {
        Text("My App")

        AdButlerBanner(
            request = AdRequest(zoneId = 12345),
            modifier = Modifier.fillMaxWidth().height(250.dp),
            onEvent = { event ->
                when (event) {
                    is BannerAdEvent.Loaded -> Log.d("Ad", "Loaded: ${event.response.width}x${event.response.height}")
                    is BannerAdEvent.Failed -> Log.e("Ad", "Failed: ${event.error.message}")
                    is BannerAdEvent.Impression -> Log.d("Ad", "Viewable impression")
                    is BannerAdEvent.Click -> Log.d("Ad", "Clicked")
                }
            }
        )
    }
}
```

#### XML Layout

```kotlin
import com.adbutler.sdk.banner.AdButlerBannerView
import com.adbutler.sdk.core.AdRequest

class MainActivity : AppCompatActivity() {
    private lateinit var bannerView: AdButlerBannerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bannerView = AdButlerBannerView(this)
        bannerView.listener = object : AdButlerBannerView.Listener {
            override fun onAdLoaded(view: AdButlerBannerView, response: AdResponse) {
                Log.d("Ad", "Loaded: ${response.bannerId}")
            }
            override fun onAdFailed(view: AdButlerBannerView, error: AdButlerError) {
                Log.e("Ad", "Failed: ${error.message}")
            }
            override fun onImpressionRecorded(view: AdButlerBannerView) {
                Log.d("Ad", "Viewable impression")
            }
            override fun onClickRecorded(view: AdButlerBannerView) {
                Log.d("Ad", "Clicked")
            }
        }

        // Add to layout
        val container = findViewById<FrameLayout>(R.id.ad_container)
        container.addView(bannerView, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, 250.dpToPx()
        ))

        bannerView.load(AdRequest(zoneId = 12345))
    }
}
```

### 3. Show an Interstitial Ad

```kotlin
import com.adbutler.sdk.interstitial.AdButlerInterstitialAd
import com.adbutler.sdk.core.AdRequest

class GameActivity : AppCompatActivity() {
    private var interstitialAd: AdButlerInterstitialAd? = null

    fun loadAd() {
        lifecycleScope.launch {
            try {
                interstitialAd = AdButlerInterstitialAd.load(
                    context = this@GameActivity,
                    request = AdRequest(zoneId = 67890)
                )
                interstitialAd?.listener = object : AdButlerInterstitialAd.Listener {
                    override fun onAdPresented(ad: AdButlerInterstitialAd) {
                        Log.d("Ad", "Shown")
                    }
                    override fun onAdDismissed(ad: AdButlerInterstitialAd) {
                        Log.d("Ad", "Dismissed")
                        loadAd() // Pre-load next
                    }
                }
                Log.d("Ad", "Interstitial ready")
            } catch (e: Exception) {
                Log.e("Ad", "Failed: ${e.message}")
            }
        }
    }

    fun showAd() {
        interstitialAd?.takeIf { it.isReady }?.show(this)
    }
}
```

### 4. Show a Native Ad

```kotlin
import com.adbutler.sdk.nativead.AdButlerNativeAd
import com.adbutler.sdk.core.AdRequest

class ArticleActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_article)

        val adContainer = findViewById<FrameLayout>(R.id.native_ad_container)

        lifecycleScope.launch {
            try {
                val nativeAd = AdButlerNativeAd.load(
                    request = AdRequest(zoneId = 11111)
                )

                nativeAd.onImpression = { Log.d("Ad", "Native impression") }
                nativeAd.onClick = { Log.d("Ad", "Native clicked") }

                // Render HTML into the container
                nativeAd.present(adContainer)

                // Access raw data
                Log.d("Ad", "Banner ID: ${nativeAd.bannerId}")
                Log.d("Ad", "HTML: ${nativeAd.rawHtml}")
                Log.d("Ad", "Click URL: ${nativeAd.clickUrl}")
            } catch (e: Exception) {
                Log.e("Ad", "Failed: ${e.message}")
            }
        }
    }
}
```

### 5. Play a VAST Video Ad

#### Jetpack Compose

```kotlin
import com.adbutler.sdk.video.AdButlerVASTPlayer
import com.adbutler.sdk.video.VASTPlayerEvent

@Composable
fun VideoScreen() {
    AdButlerVASTPlayer(
        zoneId = 99999,
        modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f),
        onEvent = { event ->
            when (event) {
                is VASTPlayerEvent.Loaded -> Log.d("Ad", "VAST loaded: ${event.ad.adTitle}")
                is VASTPlayerEvent.Started -> Log.d("Ad", "Playing")
                is VASTPlayerEvent.Quartile -> Log.d("Ad", "Quartile: ${event.quartile}")
                is VASTPlayerEvent.Completed -> Log.d("Ad", "Complete")
                is VASTPlayerEvent.Skipped -> Log.d("Ad", "Skipped")
                is VASTPlayerEvent.Clicked -> Log.d("Ad", "Clicked")
                is VASTPlayerEvent.Error -> Log.e("Ad", "Error: ${event.error.message}")
                is VASTPlayerEvent.CompanionShown -> Log.d("Ad", "Companion: ${event.companion.width}x${event.companion.height}")
            }
        }
    )
}
```

#### XML View

```kotlin
import com.adbutler.sdk.video.AdButlerVASTPlayerView

class VideoActivity : AppCompatActivity() {
    private lateinit var vastPlayer: AdButlerVASTPlayerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        vastPlayer = AdButlerVASTPlayerView(this)
        vastPlayer.listener = object : AdButlerVASTPlayerView.VASTPlayerListener {
            override fun onLoaded(player: AdButlerVASTPlayerView, ad: VASTAd) {
                Log.d("Ad", "Loaded: ${ad.adTitle}")
                player.play()
            }
            override fun onStarted(player: AdButlerVASTPlayerView) { Log.d("Ad", "Started") }
            override fun onQuartile(player: AdButlerVASTPlayerView, quartile: VASTQuartile) {
                Log.d("Ad", "Quartile: $quartile")
            }
            override fun onCompleted(player: AdButlerVASTPlayerView) { Log.d("Ad", "Complete") }
            override fun onSkipped(player: AdButlerVASTPlayerView) { Log.d("Ad", "Skipped") }
            override fun onClicked(player: AdButlerVASTPlayerView) { Log.d("Ad", "Clicked") }
            override fun onError(player: AdButlerVASTPlayerView, error: AdButlerError) {
                Log.e("Ad", "Error: ${error.message}")
            }
        }

        setContentView(vastPlayer)

        // Load from VAST zone
        vastPlayer.load(zoneId = 99999)

        // Or load from direct VAST URL
        // vastPlayer.load(vastUrl = "https://example.com/vast.xml")
    }

    // Fullscreen presentation
    fun goFullscreen() {
        vastPlayer.presentFullscreen(this)
    }
}
```

## Ad Request Options

```kotlin
val request = AdRequest.Builder(zoneId = 12345)
    .keywords(listOf("sports", "basketball", "nba"))
    .size(width = 300, height = 250)
    .dataKeyTargeting(mapOf("category" to "electronics", "page_type" to "product"))
    .referrer("https://myapp.com/article/123")
    .uniqueDelivery(pageId = 1, place = 0)
    .build()
```

Or use the simple constructor for basic requests:

```kotlin
val request = AdRequest(zoneId = 12345)
```

## Tracking

The SDK automatically handles all tracking:

| Event | When It Fires | URL Field |
|-------|--------------|-----------|
| Impression | When ad data is received (before rendering) | `accupixel_url` |
| Eligible | When ad view renders on screen | `eligible_url` |
| Viewable | When 50%+ of ad is visible for 1+ second (MRC standard) | `viewable_url` |
| Third-party | Alongside impression | `tracking_pixel` |
| Click | When user taps the ad | Opens `redirect_url` |
| VAST quartiles | At 0%, 25%, 50%, 75%, 100% of video playback | VAST tracking events |

## ProGuard / R8

If you use ProGuard or R8, add to your `proguard-rules.pro`:

```
-keep class com.adbutler.sdk.** { *; }
```

## Architecture

```
com.adbutler.sdk/
├── core/           — Configuration, networking, tracking, viewability
├── banner/         — Inline banner ads (XML View + Compose)
├── interstitial/   — Fullscreen display ads
├── nativead/       — HTML-rendered native ads
└── video/          — VAST 2.0 + 4.2 video player (Media3/ExoPlayer)
```

Dependencies: Kotlin Coroutines, Kotlin Serialization, AndroidX Media3 (ExoPlayer), AndroidX Compose, AndroidX WebKit.

## License

MIT
