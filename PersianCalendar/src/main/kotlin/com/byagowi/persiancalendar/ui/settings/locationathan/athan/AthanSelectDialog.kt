package com.byagowi.persiancalendar.ui.settings.locationathan.athan

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Environment
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.core.content.IntentCompat
import androidx.core.content.edit
import androidx.core.os.bundleOf
import com.byagowi.persiancalendar.PREF_ATHAN_NAME
import com.byagowi.persiancalendar.PREF_ATHAN_URI
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.service.AthanNotification
import com.byagowi.persiancalendar.ui.common.AppDialog
import com.byagowi.persiancalendar.ui.utils.SettingsHorizontalPaddingItem
import com.byagowi.persiancalendar.ui.utils.SettingsItemHeight
import com.byagowi.persiancalendar.ui.utils.getFileName
import com.byagowi.persiancalendar.utils.debugAssertNotNull
import com.byagowi.persiancalendar.utils.getRawUri
import com.byagowi.persiancalendar.utils.logException
import com.byagowi.persiancalendar.utils.preferences
import java.io.File

@Composable
fun AthanSelectDialog(onDismissRequest: () -> Unit) {
    val context = LocalContext.current
    val language by language.collectAsState()

    fun commonDialogCallback(uri: Uri?, action: (Uri) -> (Pair<String, Uri>?)) {
        onDismissRequest()
        uri ?: return
        AthanNotification.invalidateChannel(context)
        val (title, uri) = runCatching { action(uri) }.onFailure(logException).getOrNull()
            .debugAssertNotNull ?: return
        context.preferences.edit {
            putString(PREF_ATHAN_NAME, title)
            putString(PREF_ATHAN_URI, uri.toString())
        }
        Toast.makeText(context, R.string.custom_notification_is_set, Toast.LENGTH_SHORT).show()
    }

    val deviceRingtone = rememberLauncherForActivityResult(PickRingtoneContract()) {
        commonDialogCallback(it) callback@{ uri ->
            // If no ringtone has been found better to skip touching preferences store
            val ringtone = RingtoneManager.getRingtone(context, uri) ?: return@callback null
            ringtone.getTitle(context).orEmpty() to uri
        }
        Toast.makeText(context, R.string.custom_notification_is_set, Toast.LENGTH_SHORT).show()
    }
    val soundFilePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) {
        commonDialogCallback(it) callback@{ uri ->
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val fileName = getFileName(context, uri) ?: return@callback null
                fileName to FileProvider.getUriForFile(
                    context.applicationContext,
                    "${context.packageName}.provider",
                    File(
                        context.getExternalFilesDir(Environment.DIRECTORY_ALARMS),
                        fileName,
                    ).also { it.outputStream().use(inputStream::copyTo) },
                )
            }
        }
    }

    AppDialog(
        onDismissRequest = onDismissRequest,
        dismissButton = {
            TextButton(onClick = onDismissRequest) { Text(stringResource(R.string.cancel)) }
        },
        title = { Text(stringResource(R.string.custom_athan)) },
    ) {
        val resources = LocalResources.current
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
                    runCatching {
                        deviceRingtone.launch(Unit)
                    }.onFailure(logException).onFailure { onDismissRequest() }
                },
                R.string.more to {
                    if (language.isPersianOrDari) Toast.makeText(
                        context,
                        "پرونده‌ای صوتی، برای نمونه «mp3»، انتخاب کنید",
                        Toast.LENGTH_LONG
                    ).show()
                    runCatching {
                        soundFilePicker.launch(
                            arrayOf(
                                "audio/mpeg", // mpga mpega mp1 mp2 mp3
                                "audio/aac", // adts aac ass
                                "audio/midi", // midi
                                "audio/ac3", // ac3
                                "audio/flac", // flac
                                "audio/ogg", // oga ogg opus spx
                                "audio/mp4", // m4a
                                "audio/x-wav", // wav
                            )
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

private class PickRingtoneContract : ActivityResultContract<Unit, Uri?>() {
    override fun createIntent(context: Context, input: Unit): Intent =
        Intent(RingtoneManager.ACTION_RINGTONE_PICKER).putExtras(
            bundleOf(
                RingtoneManager.EXTRA_RINGTONE_TYPE to RingtoneManager.TYPE_ALL,
                RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT to true,
                RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT to true,
                RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI to Settings.System.DEFAULT_NOTIFICATION_URI,
            )
        )

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        return if (resultCode == Activity.RESULT_OK) IntentCompat.getParcelableExtra(
            intent ?: return null,
            RingtoneManager.EXTRA_RINGTONE_PICKED_URI,
            Uri::class.java,
        ) else null
    }
}
