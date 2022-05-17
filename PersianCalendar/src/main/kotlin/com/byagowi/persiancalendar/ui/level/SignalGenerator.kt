package com.byagowi.persiancalendar.ui.level

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.coroutineScope
import com.byagowi.persiancalendar.ui.common.BaseSlider
import com.byagowi.persiancalendar.ui.utils.dp
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import java.util.*
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sign
import kotlin.math.sin


const val MIDDLE_A_SEMITONE = 69.0
const val MIDDLE_A_FREQUENCY = 440.0 // Hz
fun getStandardFrequency(note: Double): Double {
    return MIDDLE_A_FREQUENCY * 2.0.pow((note - MIDDLE_A_SEMITONE) / 12)
}

//fun getNote(frequency: Double): Double {
//    val note = 12 * (ln(frequency / MIDDLE_A_FREQUENCY) / ln(2.0))
//    return note.roundToInt() + MIDDLE_A_SEMITONE
//}

val ABC_NOTATION = listOf(
    "C", "C♯", "D", "D♯", "E", "F", "F♯", "G", "G♯", "A", "A♯", "B"
)
var SOLFEGE_NOTATION = listOf(
    "Do", "Do♯", "Re", "Re♯", "Mi", "Fa", "Fa♯", "Sol", "Sol♯", "La", "La♯", "Si"
)

fun getAbcNoteLabel(note: Int) = ABC_NOTATION[note % 12] + ((note / 12) - 1)
fun getSolfegeNoteLabel(note: Int) = SOLFEGE_NOTATION[note % 12] + ((note / 12) - 1)

fun showSignalGeneratorDialog(activity: FragmentActivity, viewLifecycle: Lifecycle) {
    val currentSemitone = MutableStateFlow(MIDDLE_A_SEMITONE)

    val view = object : BaseSlider(activity) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).also {
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
            val text = "%d Hz %s %s".format(
                Locale.ENGLISH,
                getStandardFrequency(currentSemitone.value).toInt(),
                getAbcNoteLabel(currentSemitone.value.roundToInt()),
                getSolfegeNoteLabel(currentSemitone.value.roundToInt())
            )
            canvas.drawText(text, r, r, textPaint)
        }
    }

    view.onScrollListener = { dx, _ ->
        currentSemitone.value = (currentSemitone.value - dx / 1000.0)
            .coerceIn(15.0, 135.0) // Clamps it in terms of semitones
    }

    val sampleRate = 44100
    val buffer = ShortArray(sampleRate * 10)
    var previousAudioTrack: AudioTrack? = null

    currentSemitone
        .map { semitone ->
            val frequency = getStandardFrequency(semitone)
            buffer.indices.forEach {
                val phase = 2 * Math.PI * it / (sampleRate / frequency)
                buffer[it] = (when (0) {
                    0 -> sin(phase) // Sine
                    1 -> sign(sin(phase)) // Square
                    2 -> abs(asin(sin(phase / 2))) // Sawtooth
                    else -> .0
                } * Short.MAX_VALUE).toInt().toShort()
            }

            val audioTrack = AudioTrack(
                AudioManager.STREAM_MUSIC, sampleRate,
                AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
                buffer.size, AudioTrack.MODE_STATIC
            )
            audioTrack.write(buffer, 0, buffer.size)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                audioTrack.setLoopPoints(0, audioTrack.bufferSizeInFrames, -1)
            }
            audioTrack.play()
            if (previousAudioTrack?.state == AudioTrack.STATE_INITIALIZED) {
                previousAudioTrack?.stop()
                previousAudioTrack?.release()
            }
            previousAudioTrack = audioTrack
        }
        .flowOn(Dispatchers.Unconfined)
        .launchIn(viewLifecycle.coroutineScope)

    val dialog = MaterialAlertDialogBuilder(activity)
        .setView(view)
        .setOnCancelListener { previousAudioTrack?.stop() }
        .show()

    activity.lifecycle.addObserver(LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_PAUSE) dialog.cancel()
    })
}
