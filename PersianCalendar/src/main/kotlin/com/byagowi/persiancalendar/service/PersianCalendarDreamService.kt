package com.byagowi.persiancalendar.service

import android.animation.ValueAnimator
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
import android.service.dreams.DreamService
import android.view.ContextThemeWrapper
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatImageView
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Theme
import com.byagowi.persiancalendar.ui.athan.PatternDrawable
import com.byagowi.persiancalendar.ui.utils.dp
import kotlin.random.Random

class PersianCalendarDreamService : DreamService() {

    private val valueAnimator = ValueAnimator.ofFloat(0f, 360f).also {
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
        setContentView(FrameLayout(this).also { frame ->
            val isNightMode = Theme.isNightMode(this)
            val accentColor = if (Theme.isDynamicColorAvailable()) getColor(
                if (isNightMode) android.R.color.system_accent1_200
                else android.R.color.system_accent1_400
            ) else null
            val pattern = PatternDrawable(
                preferredTintColor = accentColor,
                darkBaseColor = Theme.isNightMode(this)
            )
            frame.background = pattern
            valueAnimator.addUpdateListener {
                pattern.rotationDegree = valueAnimator.animatedValue as? Float ?: 0f
                pattern.invalidateSelf()
            }
            frame.setOnClickListener {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) wakeUp() else finish()
            }
            frame.addView(AppCompatImageView(ContextThemeWrapper(this, R.style.LightTheme)).also {
                it.layoutParams = FrameLayout.LayoutParams(100.dp.toInt(), 100.dp.toInt())
                it.setImageResource(R.drawable.ic_play)
                var play = false
                it.setOnClickListener { _ ->
                    play = !play
                    it.setImageResource(if (play) R.drawable.ic_stop else R.drawable.ic_play)
                    if (play) audioTrack.play() else audioTrack.pause()
                }
            })
        })
        listOf(valueAnimator::start, valueAnimator::reverse).random()()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (audioTrack.state == AudioTrack.STATE_INITIALIZED) audioTrack.stop()
        valueAnimator.removeAllUpdateListeners()
        valueAnimator.cancel()
    }
}
