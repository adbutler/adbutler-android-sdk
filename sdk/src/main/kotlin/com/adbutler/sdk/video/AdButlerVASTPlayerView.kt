package com.adbutler.sdk.video

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.util.AttributeSet
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.adbutler.sdk.core.AdButler
import com.adbutler.sdk.core.AdButlerError
import com.adbutler.sdk.core.AdButlerInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * A VAST video ad player view backed by Media3 ExoPlayer.
 * Supports VAST 2.0 and 4.2 with linear video, skip button,
 * quartile tracking, and companion ads.
 *
 * ```kotlin
 * val playerView = AdButlerVASTPlayerView(context)
 * playerView.listener = object : AdButlerVASTPlayerView.VASTPlayerListener { ... }
 * playerView.load(zoneId = 99999)
 * ```
 */
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
class AdButlerVASTPlayerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    /**
     * Listener for receiving video ad events.
     */
    interface VASTPlayerListener {
        /** Called when the VAST ad has been loaded and is ready to play. */
        fun onLoaded(player: AdButlerVASTPlayerView, ad: VASTAd) {}

        /** Called when playback starts. */
        fun onStarted(player: AdButlerVASTPlayerView) {}

        /** Called when a quartile milestone is reached. */
        fun onQuartileReached(player: AdButlerVASTPlayerView, quartile: VASTQuartile) {}

        /** Called when playback completes. */
        fun onCompleted(player: AdButlerVASTPlayerView) {}

        /** Called when the user clicks the video ad. */
        fun onClicked(player: AdButlerVASTPlayerView) {}

        /** Called when the user skips the ad. */
        fun onSkipped(player: AdButlerVASTPlayerView) {}

        /** Called when an error occurs. */
        fun onError(player: AdButlerVASTPlayerView, error: AdButlerError) {}

        /** Called when a companion ad should be displayed. */
        fun onCompanionAvailable(player: AdButlerVASTPlayerView, companion: VASTCompanion) {}
    }

    /** Listener for receiving video ad events. */
    var listener: VASTPlayerListener? = null

    /** Whether a video is currently playing. */
    var isPlaying: Boolean = false
        private set

    /** Video duration in seconds. */
    var duration: Double = 0.0
        private set

    /** Current playback position in seconds. */
    val currentTime: Double
        get() {
            val pos = exoPlayer?.currentPosition ?: 0L
            return pos / 1000.0
        }

    // --- Private ---

    private var exoPlayer: ExoPlayer? = null
    private var playerView: PlayerView? = null
    private var vastAd: VASTAd? = null
    private var sdk: AdButlerInstance? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var loadJob: Job? = null

    // Quartile tracking
    private val firedQuartiles = mutableSetOf<VASTQuartile>()

    // Skip button
    private var skipButton: Button? = null
    private var skipOffset: Double? = null

    // Progress polling
    private var progressRunnable: Runnable? = null
    private val progressHandler = android.os.Handler(android.os.Looper.getMainLooper())

    init {
        setBackgroundColor(Color.BLACK)
        clipChildren = true
    }

    // --- Public API ---

    /**
     * Load a VAST ad from an AdButler VAST zone.
     *
     * @param zoneId The VAST zone ID.
     */
    fun load(zoneId: Int) {
        val sdk = try {
            AdButler.instance
        } catch (e: AdButlerError) {
            listener?.onError(this, e)
            return
        }
        this.sdk = sdk

        loadJob?.cancel()
        loadJob = scope.launch {
            try {
                val tagUrl = "${sdk.options.baseUrl}/vast.spark?setID=$zoneId;ID=${sdk.accountId}"
                val xmlData = sdk.client.fetchData(tagUrl)
                parseAndLoad(xmlData, sdk)
            } catch (e: AdButlerError) {
                listener?.onError(this@AdButlerVASTPlayerView, e)
            } catch (e: Exception) {
                listener?.onError(this@AdButlerVASTPlayerView, AdButlerError.NetworkError(e))
            }
        }
    }

    /**
     * Load a VAST ad from a direct VAST XML URL.
     *
     * @param vastUrl URL string to the VAST XML.
     */
    fun load(vastUrl: String) {
        val sdk = try {
            AdButler.instance
        } catch (e: AdButlerError) {
            listener?.onError(this, e)
            return
        }
        this.sdk = sdk

        loadJob?.cancel()
        loadJob = scope.launch {
            try {
                val xmlData = sdk.client.fetchData(vastUrl)
                parseAndLoad(xmlData, sdk)
            } catch (e: AdButlerError) {
                listener?.onError(this@AdButlerVASTPlayerView, e)
            } catch (e: Exception) {
                listener?.onError(this@AdButlerVASTPlayerView, AdButlerError.NetworkError(e))
            }
        }
    }

    /**
     * Start or resume playback.
     */
    fun play() {
        exoPlayer?.play()
        isPlaying = true
        startProgressPolling()
    }

    /**
     * Pause playback.
     */
    fun pause() {
        exoPlayer?.pause()
        isPlaying = false
        stopProgressPolling()
        fireTrackingEvent("pause")
    }

    /**
     * Present the video fullscreen in a new Activity.
     *
     * @param activity The activity to launch from.
     */
    fun presentFullscreen(activity: Activity) {
        // Pause in the current view -- the user can resume via the fullscreen activity
        pause()
        VASTFullscreenActivity.pendingPlayerView = this
        val intent = Intent(activity, VASTFullscreenActivity::class.java)
        activity.startActivity(intent)
    }

    /**
     * Release all resources. Call when the player is no longer needed.
     */
    fun release() {
        stopProgressPolling()
        loadJob?.cancel()
        exoPlayer?.release()
        exoPlayer = null
        playerView = null
        skipButton = null
        vastAd = null
        firedQuartiles.clear()
        removeAllViews()
    }

    override fun onDetachedFromWindow() {
        release()
        scope.cancel()
        super.onDetachedFromWindow()
    }

    // --- Internal ---

    private suspend fun parseAndLoad(xmlData: ByteArray, sdk: AdButlerInstance) {
        val parser = VASTParser()
        val vastResponse = withContext(Dispatchers.Default) {
            parser.parse(xmlData)
        }

        val ad = vastResponse.ads.firstOrNull()
            ?: throw AdButlerError.NoAdAvailable()

        // Handle wrapper (follow redirect)
        if (ad.isWrapper && ad.wrapperUrl != null) {
            val wrappedData = sdk.client.fetchData(ad.wrapperUrl)
            parseAndLoad(wrappedData, sdk)
            return
        }

        val linear = ad.linear
            ?: throw AdButlerError.VastParseError("No linear creative found")

        val mediaFile = MediaFileSelector.selectBest(linear.mediaFiles)
            ?: throw AdButlerError.NoCompatibleMedia()

        if (mediaFile.url.isBlank()) {
            throw AdButlerError.VastParseError("Invalid media file URL")
        }

        // Switch to main thread for UI setup
        withContext(Dispatchers.Main) {
            vastAd = ad
            duration = linear.duration
            skipOffset = linear.skipOffset
            firedQuartiles.clear()

            setupPlayer(mediaFile.url)
            listener?.onLoaded(this@AdButlerVASTPlayerView, ad)
        }
    }

    private fun setupPlayer(mediaUrl: String) {
        // Clean up previous
        release()

        val player = ExoPlayer.Builder(context).build()
        exoPlayer = player

        val pv = PlayerView(context).apply {
            this.player = player
            useController = false
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            setOnClickListener { handleClick() }
        }
        playerView = pv
        addView(pv)

        // Set up skip button if skippable
        if (skipOffset != null) {
            setupSkipButton()
        }

        // Set up player listener
        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    handlePlaybackComplete()
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                val vastError = AdButlerError.NetworkError(error)
                listener?.onError(this@AdButlerVASTPlayerView, vastError)

                // Fire error tracking URL
                vastAd?.errorUrl?.let { sdk?.trackingManager?.fireTrackingUrl(it) }
            }
        })

        // Load media
        val mediaItem = MediaItem.fromUri(Uri.parse(mediaUrl))
        player.setMediaItem(mediaItem)
        player.prepare()

        // Fire impression URLs
        vastAd?.let { ad ->
            for (url in ad.impressionUrls) {
                sdk?.trackingManager?.fireTrackingUrl(url)
            }
        }
        fireTrackingEvent("creativeView")
    }

    private fun startProgressPolling() {
        stopProgressPolling()
        val runnable = object : Runnable {
            override fun run() {
                handleTimeUpdate(currentTime)
                progressHandler.postDelayed(this, 250L)
            }
        }
        progressRunnable = runnable
        progressHandler.postDelayed(runnable, 250L)
    }

    private fun stopProgressPolling() {
        progressRunnable?.let { progressHandler.removeCallbacks(it) }
        progressRunnable = null
    }

    private fun handleTimeUpdate(currentSeconds: Double) {
        if (duration <= 0) return

        val progress = currentSeconds / duration

        // Fire quartile events (one-shot)
        if (progress > 0 && VASTQuartile.START !in firedQuartiles) {
            firedQuartiles.add(VASTQuartile.START)
            fireTrackingEvent("start")
            listener?.onStarted(this)
        }
        if (progress >= 0.25 && VASTQuartile.FIRST_QUARTILE !in firedQuartiles) {
            firedQuartiles.add(VASTQuartile.FIRST_QUARTILE)
            fireTrackingEvent("firstQuartile")
            listener?.onQuartileReached(this, VASTQuartile.FIRST_QUARTILE)
        }
        if (progress >= 0.50 && VASTQuartile.MIDPOINT !in firedQuartiles) {
            firedQuartiles.add(VASTQuartile.MIDPOINT)
            fireTrackingEvent("midpoint")
            listener?.onQuartileReached(this, VASTQuartile.MIDPOINT)
        }
        if (progress >= 0.75 && VASTQuartile.THIRD_QUARTILE !in firedQuartiles) {
            firedQuartiles.add(VASTQuartile.THIRD_QUARTILE)
            fireTrackingEvent("thirdQuartile")
            listener?.onQuartileReached(this, VASTQuartile.THIRD_QUARTILE)
        }

        // Update skip button countdown
        updateSkipButton(currentSeconds)
    }

    private fun handlePlaybackComplete() {
        isPlaying = false
        stopProgressPolling()

        if (VASTQuartile.COMPLETE !in firedQuartiles) {
            firedQuartiles.add(VASTQuartile.COMPLETE)
            fireTrackingEvent("complete")
            listener?.onQuartileReached(this, VASTQuartile.COMPLETE)
        }
        listener?.onCompleted(this)

        // Show companion if available
        showCompanionAd()
    }

    // --- Skip Button ---

    private fun setupSkipButton() {
        val density = resources.displayMetrics.density
        val paddingH = (16 * density).toInt()
        val paddingV = (8 * density).toInt()
        val marginPx = (16 * density).toInt()

        val btn = Button(context).apply {
            setBackgroundColor(Color.argb(153, 0, 0, 0)) // 60% black
            setTextColor(Color.WHITE)
            textSize = 14f
            setPadding(paddingH, paddingV, paddingH, paddingV)
            isEnabled = false
            alpha = 0.7f
            isAllCaps = false

            setOnClickListener { handleSkip() }

            layoutParams = LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.BOTTOM or Gravity.END
                bottomMargin = marginPx
                marginEnd = marginPx
            }
        }

        skipButton = btn
        addView(btn)
        updateSkipButton(0.0)
    }

    private fun updateSkipButton(currentSeconds: Double) {
        val offset = skipOffset ?: return
        val btn = skipButton ?: return

        val remaining = Math.ceil(offset - currentSeconds).toInt()

        if (remaining > 0) {
            btn.text = "Skip in ${remaining}s"
            btn.isEnabled = false
            btn.alpha = 0.7f
        } else {
            btn.text = "Skip Ad \u25B6"
            btn.isEnabled = true
            btn.alpha = 1.0f
        }
    }

    private fun handleSkip() {
        exoPlayer?.pause()
        isPlaying = false
        stopProgressPolling()
        fireTrackingEvent("skip")
        listener?.onSkipped(this)
    }

    // --- Click Handling ---

    private fun handleClick() {
        val linear = vastAd?.linear ?: return
        val clickStr = linear.clickThrough ?: return

        // Fire click tracking
        for (trackingUrl in linear.clickTracking) {
            sdk?.trackingManager?.fireTrackingUrl(trackingUrl)
        }
        fireTrackingEvent("click")

        listener?.onClicked(this)

        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(clickStr))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (_: Exception) {
            // Silently fail
        }
    }

    // --- Companion Ads ---

    private fun showCompanionAd() {
        val companions = vastAd?.companions ?: return
        val companion = companions.firstOrNull() ?: return
        listener?.onCompanionAvailable(this, companion)
    }

    // --- Tracking ---

    private fun fireTrackingEvent(event: String) {
        val linear = vastAd?.linear ?: return
        val urls = linear.trackingEvents[event] ?: return
        for (url in urls) {
            sdk?.trackingManager?.fireTrackingUrl(url)
        }
    }
}

/**
 * Internal Activity for fullscreen VAST video playback.
 * Not meant to be used directly -- call [AdButlerVASTPlayerView.presentFullscreen] instead.
 */
internal class VASTFullscreenActivity : Activity() {
    companion object {
        /** Reference to the player view requesting fullscreen. */
        var pendingPlayerView: AdButlerVASTPlayerView? = null
    }

    private var playerView: AdButlerVASTPlayerView? = null

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)

        val pv = pendingPlayerView
        if (pv == null) {
            finish()
            return
        }
        pendingPlayerView = null
        playerView = pv

        // Remove from parent temporarily
        (pv.parent as? ViewGroup)?.removeView(pv)

        val root = FrameLayout(this).apply {
            setBackgroundColor(Color.BLACK)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        pv.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        root.addView(pv)

        // Close button
        val density = resources.displayMetrics.density
        val sizePx = (40 * density).toInt()
        val marginPx = (16 * density).toInt()

        val closeBtn = TextView(this).apply {
            text = "\u2715"
            textSize = 24f
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.argb(153, 0, 0, 0))
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 0)

            layoutParams = FrameLayout.LayoutParams(sizePx, sizePx).apply {
                gravity = Gravity.TOP or Gravity.END
                topMargin = marginPx
                marginEnd = marginPx
            }

            setOnClickListener {
                finish()
            }
        }
        root.addView(closeBtn)

        setContentView(root)

        pv.play()
    }

    override fun onDestroy() {
        // Re-attach is the caller's responsibility; just remove from this container
        val pv = playerView
        if (pv != null) {
            (pv.parent as? ViewGroup)?.removeView(pv)
        }
        super.onDestroy()
    }
}
