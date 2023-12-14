package com.byagowi.persiancalendar.service

import android.animation.ValueAnimator
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
import android.service.dreams.DreamService
import android.view.ContextThemeWrapper
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import androidx.core.view.isVisible
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ui.athan.PatternDrawable
import com.byagowi.persiancalendar.ui.theme.Theme
import com.byagowi.persiancalendar.ui.utils.dp
import com.byagowi.persiancalendar.utils.logException
import kotlin.random.Random

class PersianCalendarDreamService : DreamService() {

    private val valueAnimator = ValueAnimator.ofFloat(0f, 1f).also {
        it.duration = 360000L
        it.interpolator = LinearInterpolator()
        it.repeatMode = ValueAnimator.RESTART
        it.repeatCount = ValueAnimator.INFINITE
    }

    private val audioTrack = run {
        val sampleRate = 22050 // Hz (maximum frequency is 7902.13Hz (B8))
        val numSamples = sampleRate * 10
        val buffer = (0..numSamples).runningFold(.0) { lastOut, _ ->
            // Brown noise https://github.com/zacharydenton/noise.js/blob/master/noise.js#L45
            (((Random.nextDouble() * 2 - 1) * .02 + lastOut) / 1.02)
        }.map { (it * Short.MAX_VALUE).toInt().toShort() }.toShortArray()
        val audioTrack = AudioTrack(
            AudioManager.STREAM_MUSIC, sampleRate, AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT, buffer.size, AudioTrack.MODE_STATIC
        )
        audioTrack.write(buffer, 0, buffer.size)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            audioTrack.setLoopPoints(0, audioTrack.bufferSizeInFrames, -1)
        }
        audioTrack
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isInteractive = true
        isFullscreen = true

        val backgroundView = View(this).also {
            val isNightMode = Theme.isNightMode(this)
            val accentColor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) getColor(
                if (isNightMode) android.R.color.system_accent1_200
                else android.R.color.system_accent1_400
            ) else null
            val pattern = PatternDrawable(
                preferredTintColor = accentColor,
                darkBaseColor = Theme.isNightMode(this),
                dp = resources.dp,
            )
            it.background = pattern
            valueAnimator.addUpdateListener {
                pattern.rotationDegree = valueAnimator.animatedFraction * 360f
                pattern.invalidateSelf()
            }
            it.setOnClickListener { wakeUp() }
        }

        val button = ImageView(ContextThemeWrapper(this, R.style.LightTheme))
        run {
            button.setImageResource(R.drawable.ic_play)
            var play = false
            button.setOnClickListener { _ ->
                runCatching {
                    play = !play
                    if (play) audioTrack.play() else audioTrack.pause()
                    button.setImageResource(if (play) R.drawable.ic_stop else R.drawable.ic_play)
                }.onFailure(logException).onFailure { button.isVisible = false }
            }
        }

//        // Make the play/stop button movable using ViewDragHelper and ViewGroup
//        val screen = object : ViewGroup(this) {
//            init {
//                addView(backgroundView)
//                addView(button)
//            }
//
//            override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {}
//            override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
//                super.onSizeChanged(w, h, oldw, oldh)
//                backgroundView.layout(0, 0, w, h)
//                button.layout(0, 0, min(w, h) / 5, min(w, h) / 5)
//            }
//
//            private val centroids by lazy(LazyThreadSafetyMode.NONE) {
//                listOf(0 to 0, 0 to height, width to 0, width to height, width / 2 to height / 2)
//            }
//
//            // Make play button of the screen movable
//            private val callback = object : ViewDragHelper.Callback() {
//                override fun tryCaptureView(child: View, pointerId: Int) = child == button
//                override fun onViewPositionChanged(
//                    changedView: View, left: Int, top: Int, dx: Int, dy: Int
//                ) = invalidate()
//
//                override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
//                    val centerX = releasedChild.left + releasedChild.width / 2.0
//                    val centerY = releasedChild.top + releasedChild.height / 2.0
//                    val (x, y) = centroids.minByOrNull { (centroidX, centroidY) ->
//                        hypot(centroidX - centerX, centroidY - centerY)
//                    } ?: return
//                    dragHelper.settleCapturedViewAt(
//                        (x - releasedChild.width / 2).coerceIn(0, width - releasedChild.width),
//                        (y - releasedChild.height / 2).coerceIn(0, height - releasedChild.height)
//                    )
//                    invalidate()
//                }
//
//                override fun getViewHorizontalDragRange(child: View): Int = width
//                override fun getViewVerticalDragRange(child: View): Int = height
//                override fun clampViewPositionHorizontal(child: View, left: Int, dx: Int): Int =
//                    left.coerceIn(0, width - child.width)
//
//                override fun clampViewPositionVertical(child: View, top: Int, dy: Int): Int =
//                    top.coerceIn(0, height - child.height)
//
//                override fun onViewCaptured(capturedChild: View, activePointerId: Int) =
//                    bringChildToFront(capturedChild)
//            }
//            private val dragHelper: ViewDragHelper = ViewDragHelper.create(this, callback)
//
//
//            override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
//                val action = event.action
//                if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
//                    dragHelper.cancel()
//                    return false
//                }
//                return dragHelper.shouldInterceptTouchEvent(event)
//            }
//
//            @SuppressLint("ClickableViewAccessibility")
//            override fun onTouchEvent(event: MotionEvent): Boolean {
//                dragHelper.processTouchEvent(event)
//                return true
//            }
//
//            override fun computeScroll() {
//                if (dragHelper.continueSettling(true)) invalidate()
//            }
//        }

        setContentView(backgroundView)

        listOf(valueAnimator::start, valueAnimator::reverse).random()()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (audioTrack.state == AudioTrack.STATE_INITIALIZED) audioTrack.stop()
        valueAnimator.removeAllUpdateListeners()
        valueAnimator.cancel()
    }
}
