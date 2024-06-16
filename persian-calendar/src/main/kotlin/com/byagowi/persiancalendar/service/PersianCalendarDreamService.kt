package com.byagowi.persiancalendar.service

import android.animation.ValueAnimator
import android.graphics.Canvas
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
import android.service.dreams.DreamService
import android.view.View
import android.view.animation.LinearInterpolator
import com.byagowi.persiancalendar.global.dreamNoise
import com.byagowi.persiancalendar.ui.athan.PatternDrawable
import com.byagowi.persiancalendar.ui.utils.dp
import com.byagowi.persiancalendar.ui.utils.isSystemInDarkTheme
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
        isFullscreen = true

        val isNightMode = isSystemInDarkTheme(resources.configuration)
        val accentColor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) getColor(
            if (isNightMode) android.R.color.system_accent1_200
            else android.R.color.system_accent1_400
        ) else null
        val pattern = PatternDrawable(
            preferredTintColor = accentColor,
            darkBaseColor = isSystemInDarkTheme(resources.configuration),
            dp = resources.dp,
        )

        val view = object : View(this) {
            override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) = pattern.setSize(w, h)
            override fun onDraw(canvas: Canvas) = pattern.draw(canvas)
            // it.setOnClickListener { wakeUp() }
        }
        // isInteractive = true

        valueAnimator.addUpdateListener {
            pattern.rotationDegree = valueAnimator.animatedFraction * 360f
            view.invalidate()
        }

        runCatching { if (dreamNoise.value) audioTrack.play() }.onFailure(logException)

        // ComposeView can't be used in DreamService in my tries
        // Even if worked someday, please test older devices also
        setContentView(view)

        listOf(valueAnimator::start, valueAnimator::reverse).random()()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (audioTrack.state == AudioTrack.STATE_INITIALIZED) audioTrack.stop()
        valueAnimator.removeAllUpdateListeners()
        valueAnimator.cancel()
    }
}
