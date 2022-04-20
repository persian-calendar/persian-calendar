package com.byagowi.persiancalendar.ui.level

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.view.MotionEvent
import android.view.View
import androidx.fragment.app.FragmentActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlin.math.min
import kotlin.math.sin

private const val sampleRate = 22050 // Hz (maximum frequency is 7902.13Hz (B8))
private const val numSamples = sampleRate
private val buffer = ShortArray(numSamples)

fun showSignalGeneratorDialog(activity: FragmentActivity) {
    buffer.indices.forEach {
        buffer[it] =
            (sin(2 * Math.PI * it / (sampleRate / 440)) * Short.MAX_VALUE).toInt().toShort()
    }
    val audioTrack = AudioTrack(
        AudioManager.STREAM_MUSIC, sampleRate, AudioFormat.CHANNEL_OUT_MONO,
        AudioFormat.ENCODING_PCM_16BIT, buffer.size, AudioTrack.MODE_STREAM
    )

    val view = object : View(activity) {
        val paint = Paint().also {
            it.color = Color.GREEN
            it.style = Paint.Style.FILL
        }

        var r = 1f
        override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
            r = min(w, h) / 2f
        }

        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            val width = MeasureSpec.getSize(widthMeasureSpec)
            super.onMeasure(
                MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY)
            )
        }

        override fun onDraw(canvas: Canvas) {
            canvas.drawCircle(r, r, r / 1.1f, paint)
        }

        @SuppressLint("ClickableViewAccessibility")
        override fun onTouchEvent(event: MotionEvent): Boolean {
            if (audioTrack.state == AudioTrack.STATE_INITIALIZED) {
                audioTrack.pause()
                audioTrack.flush()
            }
            audioTrack.play()
            audioTrack.write(buffer, 0, buffer.size)
            return true
        }
    }

    MaterialAlertDialogBuilder(activity)
        .setView(view)
        .show()
}
