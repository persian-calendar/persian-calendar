package com.byagowi.persiancalendar.service

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.global.dreamNoise
import com.byagowi.persiancalendar.ui.athan.DrawBackground
import com.byagowi.persiancalendar.ui.athan.PatternDrawable
import com.byagowi.persiancalendar.utils.logException
import kotlin.random.Random

@Composable
fun DreamContent() {
    val isNightMode = isSystemInDarkTheme()
    val resources = LocalResources.current
    val dpAsPx = with(LocalDensity.current) { 1.dp.toPx() }
    val patternDrawable = remember(isNightMode, resources, LocalConfiguration.current) {
        val accentColor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) resources.getColor(
            if (isNightMode) android.R.color.system_accent1_200
            else android.R.color.system_accent1_400,
            null,
        ) else null
        PatternDrawable(
            preferredTintColor = accentColor,
            darkBaseColor = isNightMode,
            dp = dpAsPx,
        )
    }
    Box(Modifier.onSizeChanged { patternDrawable.setSize(it.width, it.height) }) {
        DrawBackground(patternDrawable, durationMillis = 360_000)
    }
    if (dreamNoise) DisposableEffect(Unit) {
        val brownNoise = brownNoise()
        runCatching { brownNoise.play() }.onFailure(logException)
        onDispose { runCatching { brownNoise.stop() }.onFailure(logException) }
    }
}

@Preview
@Composable
internal fun DreamContentPreview() = DreamContent()

private fun brownNoise(): AudioTrack {
    val sampleRate = 22050 // Hz (maximum frequency is 7902.13Hz (B8))
    val numSamples = sampleRate * 10
    val buffer = (0..numSamples).runningFold(.0) { lastOut, _ ->
        // Brown noise https://github.com/zacharydenton/noise.js/blob/master/noise.js#L45
        (((Random.nextDouble() * 2 - 1) * .02 + lastOut) / 1.02)
    }.map { (it * Short.MAX_VALUE).toInt().toShort() }.toShortArray()
    val audioTrack = AudioTrack.Builder().setAudioAttributes(
        AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build(),
    ).setAudioFormat(
        AudioFormat.Builder().setSampleRate(sampleRate).setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build(),
    ).setBufferSizeInBytes(buffer.size * 2).setTransferMode(AudioTrack.MODE_STATIC).build()
    audioTrack.write(buffer, 0, buffer.size)
    audioTrack.setLoopPoints(0, audioTrack.bufferSizeInFrames, -1)
    return audioTrack
}
