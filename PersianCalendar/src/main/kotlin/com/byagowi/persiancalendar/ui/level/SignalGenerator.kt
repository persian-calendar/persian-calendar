package com.byagowi.persiancalendar.ui.level

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.byagowi.persiancalendar.ui.common.EmptySlider
import com.byagowi.persiancalendar.ui.utils.dp
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin

private const val sampleRate = 22050 // Hz (maximum frequency is 7902.13Hz (B8))
private const val numSamples = sampleRate
private val buffer = ShortArray(numSamples)

const val MIDDLE_A_SEMITONE = 69.0
const val MIDDLE_A_FREQUENCY = 440.0 // Hz
fun getStandardFrequency(note: Double): Double {
    return MIDDLE_A_FREQUENCY * 2.0.pow((note - MIDDLE_A_SEMITONE) / 12)
}

fun showSignalGeneratorDialog(activity: FragmentActivity) {
    val audioTrack = AudioTrack(
        AudioManager.STREAM_MUSIC, sampleRate, AudioFormat.CHANNEL_OUT_MONO,
        AudioFormat.ENCODING_PCM_16BIT, buffer.size, AudioTrack.MODE_STREAM
    )
    var currentSemitone = MIDDLE_A_SEMITONE

    val view = object : EmptySlider(activity) {
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

        private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
            it.color = Color.BLACK
            it.textAlign = Paint.Align.CENTER
            it.textSize = 20.dp
        }

        override fun onDraw(canvas: Canvas) {
            canvas.drawCircle(r, r, r / 1.1f, paint)
            canvas.drawText(
                getStandardFrequency(currentSemitone).toInt().toString(), r, r, textPaint
            )
        }
    }

    view.addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            if (audioTrack.state == AudioTrack.STATE_INITIALIZED) {
                audioTrack.pause()
                audioTrack.flush()
            }
            currentSemitone = (currentSemitone + dx / 1000.0)
                .coerceIn(15.0, 135.0) // Clamp it in terms of semitones

            val frequency = getStandardFrequency(currentSemitone)
            buffer.indices.forEach {
                buffer[it] =
                    (sin(2 * Math.PI * it / (sampleRate / frequency)) * Short.MAX_VALUE).toInt()
                        .toShort()
            }
            audioTrack.play()
            audioTrack.write(buffer, 0, buffer.size)
        }
    })

    MaterialAlertDialogBuilder(activity)
        .setView(view)
        .show()
}
