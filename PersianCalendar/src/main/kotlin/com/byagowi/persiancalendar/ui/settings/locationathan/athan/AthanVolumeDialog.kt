package com.byagowi.persiancalendar.ui.settings.locationathan.athan

import android.media.AudioManager
import android.media.RingtoneManager
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.edit
import androidx.core.content.getSystemService
import com.byagowi.persiancalendar.PREF_ATHAN_VOLUME
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.athanVolume
import com.byagowi.persiancalendar.utils.getAthanUri

@Composable
fun AthanVolumeDialog(onDismissRequest: () -> Unit) {
    val context = LocalContext.current
    var volume by rememberSaveable { mutableStateOf(context.athanVolume) }

    val audioManager =
        remember { context.getSystemService<AudioManager>() } ?: return onDismissRequest()
    val originalAlarmVolume = remember { audioManager.getStreamVolume(AudioManager.STREAM_ALARM) }
    remember { audioManager.setStreamVolume(AudioManager.STREAM_ALARM, volume, 0) }
    val maxValue = remember { audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM) }

    val ringtone = remember { RingtoneManager.getRingtone(context, getAthanUri(context)) }
    remember {
        ringtone?.streamType = AudioManager.STREAM_ALARM
        ringtone?.play()
    }

    DisposableEffect(null) {
        onDispose {
            ringtone?.stop()
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, originalAlarmVolume, 0)
        }
    }

    AlertDialog(
        title = { Text(stringResource(R.string.athan_volume)) },
        dismissButton = {
            TextButton(onClick = onDismissRequest) { Text(stringResource(R.string.cancel)) }
        },
        confirmButton = {
            TextButton(onClick = {
                onDismissRequest()
                context.appPrefs.edit { putInt(PREF_ATHAN_VOLUME, volume) }
            }) { Text(stringResource(R.string.accept)) }
        },
        text = {
            Slider(
                value = volume.toFloat(),
                steps = maxValue,
                valueRange = 0f..maxValue.toFloat(),
                onValueChange = { value ->
                    if (ringtone?.isPlaying == false) ringtone.play()
                    volume = value.toInt()
                    audioManager.setStreamVolume(AudioManager.STREAM_ALARM, volume, 0)
                },
            )
        },
        onDismissRequest = onDismissRequest,
    )
}
