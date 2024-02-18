package com.byagowi.persiancalendar.ui.settings.locationathan.athan

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.content.edit
import com.byagowi.persiancalendar.PREF_ATHAN_GAP
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ui.common.AppDialog
import com.byagowi.persiancalendar.ui.utils.stringResource
import com.byagowi.persiancalendar.utils.appPrefs

@Composable
fun AthanGapDialog(onDismissRequest: () -> Unit) {
    val context = LocalContext.current
    var minutes by rememberSaveable {
        mutableStateOf(context.appPrefs.getString(PREF_ATHAN_GAP, null) ?: "0")
    }
    AppDialog(
        title = { Text(stringResource(R.string.athan_gap_summary)) },
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = {
                onDismissRequest()
                if (minutes.toDoubleOrNull() != null)
                    context.appPrefs.edit { putString(PREF_ATHAN_GAP, minutes) }
            }) { Text(stringResource(R.string.accept)) }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) { Text(stringResource(R.string.cancel)) }
        },
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = minutes,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                onValueChange = { minutes = it },
            )
        }
    }
}
