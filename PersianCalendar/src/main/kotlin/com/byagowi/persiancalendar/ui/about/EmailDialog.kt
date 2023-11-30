package com.byagowi.persiancalendar.ui.about

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.utils.formatNumber

@Composable
fun EmailDialog(closeDialog: () -> Unit) {
    var message by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = { closeDialog() },
        confirmButton = {
            val context = LocalContext.current
            TextButton(onClick = {
                closeDialog()
                launchEmailIntent(context, message)
            }) { Text(stringResource(R.string.continue_button)) }
        },
        dismissButton = {
            TextButton(onClick = { closeDialog() }) { Text(stringResource(R.string.cancel)) }
        },
        title = { Text(stringResource(R.string.about_email_sum)) },
        text = {
            Column {
                TextField(
                    value = message,
                    onValueChange = { message = it },
                    singleLine = false,
                    modifier = Modifier.defaultMinSize(minHeight = 200.dp)
                )
                Text(
                    text = formatNumber(message.length),
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    )
}

@Preview
@Composable
private fun EmailDialogPreview() = EmailDialog {}
