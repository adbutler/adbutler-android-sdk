package com.adbutler.sdk.core

import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.view.View

/**
 * Tracks viewability of an ad view using MRC standard:
 * 50%+ of the ad must be visible for 1+ continuous second.
 */
internal class ViewabilityTracker {
    private var handler: Handler? = null
    private var checkRunnable: Runnable? = null
    private var visibleStartTime: Long? = null
    private var hasFiredViewable = false
    private var onViewable: (() -> Unit)? = null

    private companion object {
        const val CHECK_INTERVAL_MS = 100L
        const val VIEWABLE_THRESHOLD_MS = 1000L
        const val VISIBLE_PERCENTAGE_THRESHOLD = 0.5f
    }

    /** Start tracking viewability for the given view. */
    fun startTracking(view: View, onViewable: () -> Unit) {
        stopTracking()
        this.onViewable = onViewable
        this.hasFiredViewable = false
        this.visibleStartTime = null

        val h = Handler(Looper.getMainLooper())
        this.handler = h

        val runnable = object : Runnable {
            override fun run() {
                if (hasFiredViewable) {
                    stopTracking()
                    return
                }
                checkVisibility(view)
                h.postDelayed(this, CHECK_INTERVAL_MS)
            }
        }
        this.checkRunnable = runnable
        h.postDelayed(runnable, CHECK_INTERVAL_MS)
    }

    /** Stop tracking viewability. */
    fun stopTracking() {
        checkRunnable?.let { handler?.removeCallbacks(it) }
        handler = null
        checkRunnable = null
        visibleStartTime = null
        onViewable = null
    }

    private fun checkVisibility(view: View) {
        if (!view.isShown || !view.isAttachedToWindow) {
            visibleStartTime = null
            return
        }

        val visiblePercent = calculateVisiblePercentage(view)

        if (visiblePercent >= VISIBLE_PERCENTAGE_THRESHOLD) {
            val now = System.currentTimeMillis()
            if (visibleStartTime == null) {
                visibleStartTime = now
            } else if (now - (visibleStartTime ?: now) >= VIEWABLE_THRESHOLD_MS) {
                hasFiredViewable = true
                onViewable?.invoke()
                stopTracking()
            }
        } else {
            visibleStartTime = null
        }
    }

    private fun calculateVisiblePercentage(view: View): Float {
        val visibleRect = Rect()
        if (!view.getGlobalVisibleRect(visibleRect)) return 0f

        val viewArea = view.width.toLong() * view.height.toLong()
        if (viewArea <= 0) return 0f

        val visibleArea = visibleRect.width().toLong() * visibleRect.height().toLong()
        return visibleArea.toFloat() / viewArea.toFloat()
    }
}
