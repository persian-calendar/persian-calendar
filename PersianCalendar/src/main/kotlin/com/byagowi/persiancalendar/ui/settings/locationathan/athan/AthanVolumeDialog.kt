package com.byagowi.persiancalendar.ui.settings.locationathan.athan

import android.media.AudioManager
import android.media.RingtoneManager
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.core.content.getSystemService
import com.byagowi.persiancalendar.PREF_ATHAN_VOLUME
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ui.common.AppDialog
import com.byagowi.persiancalendar.ui.utils.SettingsHorizontalPaddingItem
import com.byagowi.persiancalendar.utils.athanVolume
import com.byagowi.persiancalendar.utils.getAthanUri
import com.byagowi.persiancalendar.utils.logException
import com.byagowi.persiancalendar.utils.preferences
import com.byagowi.persiancalendar.variants.debugAssertNotNull

@Composable
fun AthanVolumeDialog(onDismissRequest: () -> Unit) {
    val context = LocalContext.current
    var volume by rememberSaveable { mutableIntStateOf(context.preferences.athanVolume) }

    val audioManager =
        remember { context.getSystemService<AudioManager>() } ?: return onDismissRequest()
    val originalAlarmVolume = remember { audioManager.getStreamVolume(AudioManager.STREAM_ALARM) }
    LaunchedEffect(Unit) { audioManager.setStreamVolume(AudioManager.STREAM_ALARM, volume, 0) }
    val maxValue = remember { audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM) }

    val ringtone = remember {
        val ringtone = RingtoneManager.getRingtone(context, getAthanUri(context))
        ringtone?.streamType = AudioManager.STREAM_ALARM
        ringtone?.play()
        ringtone
    }

    DisposableEffect(Unit) {
        onDispose {
            runCatching { ringtone?.stop() }.onFailure(logException).getOrNull().debugAssertNotNull
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, originalAlarmVolume, 0)
        }
    }

    AppDialog(
        title = { Text(stringResource(R.string.athan_volume)) },
        dismissButton = {
            TextButton(onClick = onDismissRequest) { Text(stringResource(R.string.cancel)) }
        },
        confirmButton = {
            TextButton(onClick = {
                onDismissRequest()
                context.preferences.edit { putInt(PREF_ATHAN_VOLUME, volume) }
            }) { Text(stringResource(R.string.accept)) }
        },
        onDismissRequest = onDismissRequest,
    ) {
        Slider(
            value = volume.toFloat(),
            steps = maxValue,
            valueRange = 0f..maxValue.toFloat(),
            onValueChange = { value ->
                if (ringtone?.isPlaying == false) ringtone.play()
                volume = value.toInt()
                audioManager.setStreamVolume(AudioManager.STREAM_ALARM, volume, 0)
            },
            modifier = Modifier.padding(horizontal = SettingsHorizontalPaddingItem.dp),
        )
    }
}
