package com.byagowi.persiancalendar.service

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.currentStateAsState
import androidx.lifecycle.compose.rememberLifecycleOwner
import com.byagowi.persiancalendar.global.dreamNoise
import com.byagowi.persiancalendar.ui.common.PatternCanvas
import com.byagowi.persiancalendar.ui.common.PatternDrawable
import com.byagowi.persiancalendar.ui.theme.SystemTheme
import com.byagowi.persiancalendar.utils.logException
import kotlin.random.Random

@Composable
fun DreamContent(
    modifier: Modifier = Modifier,
    finish: () -> Unit,
) {
    val isNightMode = isSystemInDarkTheme()
    val density = LocalDensity.current
    val colorScheme = MaterialTheme.colorScheme
    val patternDrawable = remember(key1 = isNightMode, key2 = colorScheme) {
        PatternDrawable(
            preferredTintColor = colorScheme.primary,
            darkBaseColor = isNightMode,
            dp = with(density) { 1.dp.toPx() },
        )
    }
    Box(
        modifier
            .clickable(indication = null, interactionSource = null, onClick = finish)
            .onSizeChanged { patternDrawable.setSize(it.width, it.height) },
    ) { PatternCanvas(patternDrawable) }
    val brownNoise by remember { lazy { brownNoise() } }
    if (dreamNoise) {
        val lifecycleOwner by rememberLifecycleOwner().lifecycle.currentStateAsState()
        if (lifecycleOwner.isAtLeast(Lifecycle.State.RESUMED)) DisposableEffect(key1 = Unit) {
            runCatching { brownNoise.play() }.onFailure(logException)
            onDispose { runCatching { brownNoise.stop() }.onFailure(logException) }
        }
    }
}

@Preview
@Composable
internal fun DreamContentPreview() = SystemTheme { DreamContent {} }

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
