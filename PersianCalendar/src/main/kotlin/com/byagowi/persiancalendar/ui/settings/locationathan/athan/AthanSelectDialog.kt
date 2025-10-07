package com.byagowi.persiancalendar.ui.settings.locationathan.athan

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Parcelable
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.core.net.toUri
import com.byagowi.persiancalendar.PREF_ATHAN_NAME
import com.byagowi.persiancalendar.PREF_ATHAN_URI
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.STORED_ATHAN_NAME
import com.byagowi.persiancalendar.service.AthanNotification
import com.byagowi.persiancalendar.ui.common.AppDialog
import com.byagowi.persiancalendar.ui.utils.SettingsHorizontalPaddingItem
import com.byagowi.persiancalendar.ui.utils.SettingsItemHeight
import com.byagowi.persiancalendar.ui.utils.getFileName
import com.byagowi.persiancalendar.ui.utils.saveAsFile
import com.byagowi.persiancalendar.utils.getRawUri
import com.byagowi.persiancalendar.utils.logException
import com.byagowi.persiancalendar.utils.preferences

@Composable
fun AthanSelectDialog(onDismissRequest: () -> Unit) {
    val context = LocalContext.current
    val resources = LocalResources.current
    val deviceRingtone = rememberLauncherForActivityResult(PickRingtoneContract()) callback@{ uri ->
        onDismissRequest()
        uri ?: return@callback
        AthanNotification.invalidateChannel(context)
        // If no ringtone has been found better to skip touching preferences store
        val ringtone = RingtoneManager.getRingtone(context, uri.toUri()) ?: return@callback
        val ringtoneTitle = ringtone.getTitle(context).orEmpty()
        context.preferences.edit {
            putString(PREF_ATHAN_NAME, ringtoneTitle)
            putString(PREF_ATHAN_URI, uri)
        }
        Toast.makeText(context, R.string.custom_notification_is_set, Toast.LENGTH_SHORT).show()
    }
    val soundFilePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) callback@{ uri ->
        onDismissRequest()
        uri ?: return@callback
        AthanNotification.invalidateChannel(context)
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            val storedUri = context.saveAsFile(STORED_ATHAN_NAME) {
                it.outputStream().use { inputStream.copyTo(it) }
            }
            context.preferences.edit {
                putString(PREF_ATHAN_NAME, getFileName(context, uri))
                putString(PREF_ATHAN_URI, storedUri.toString())
            }
        }
        Toast.makeText(context, R.string.custom_notification_is_set, Toast.LENGTH_SHORT).show()
    }

    AppDialog(
        onDismissRequest = onDismissRequest,
        dismissButton = {
            TextButton(onClick = onDismissRequest) { Text(stringResource(R.string.cancel)) }
        },
        title = { Text(stringResource(R.string.custom_athan)) }
    ) {
        remember<List<Pair<Int, () -> Unit>>> {
            listOf(
                R.string.default_athan to R.raw.special,
                R.string.abdulbasit to R.raw.abdulbasit,
                R.string.moazzenzadeh to R.raw.moazzenzadeh,
                R.string.entezar to R.raw.entezar
            ).map { (stringId, rawId) ->
                stringId to {
                    AthanNotification.invalidateChannel(context)
                    context.preferences.edit {
                        putString(PREF_ATHAN_URI, resources.getRawUri(rawId))
                        putString(PREF_ATHAN_NAME, context.getString(stringId))
                    }
                    onDismissRequest()
                }
            } + listOf(
                R.string.theme_default to {
                    runCatching { deviceRingtone.launch(Unit) }
                        .onFailure(logException).onFailure { onDismissRequest() }
                },
                R.string.more to {
                    runCatching {
                        soundFilePicker.launch(
                            arrayOf("audio/mpeg")
                        )
                    }.onFailure(logException).onFailure { onDismissRequest() }
                },
            )
        }.forEach { (stringId, callback) ->
            Box(
                contentAlignment = Alignment.CenterStart,
                modifier = Modifier
                    .clickable(onClick = callback)
                    .padding(horizontal = SettingsHorizontalPaddingItem.dp)
                    .height(SettingsItemHeight.dp)
                    .fillMaxWidth(),
            ) { Text(stringResource(stringId)) }
        }
    }
}

private class PickRingtoneContract : ActivityResultContract<Unit, String?>() {
    override fun createIntent(context: Context, input: Unit): Intent =
        Intent(RingtoneManager.ACTION_RINGTONE_PICKER).putExtra(
            RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALL
        ).putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
            .putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true).putExtra(
                RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI,
                Settings.System.DEFAULT_NOTIFICATION_URI
            )

    override fun parseResult(resultCode: Int, intent: Intent?): String? =
        if (resultCode == Activity.RESULT_OK) intent?.getParcelableExtra<Parcelable?>(
            RingtoneManager.EXTRA_RINGTONE_PICKED_URI
        )?.toString()
        else null
}
