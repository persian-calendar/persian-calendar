package com.byagowi.persiancalendar.ui.about

import android.app.Activity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ui.utils.showComposeDialog
import com.google.android.material.composethemeadapter.MdcTheme

fun showEmailDialog(activity: Activity, onSuccess: (String) -> Unit) =
    showComposeDialog(activity) { EmailAlertDialog(it, onSuccess) }

@Composable
private fun EmailAlertDialog(closeDialog: () -> Unit, onSuccess: (String) -> Unit) {
    val message = remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = { closeDialog() },
        confirmButton = {
            TextButton(onClick = {
                closeDialog()
                onSuccess(message.value)
            }) { Text(stringResource(R.string.continue_button)) }
        },
        dismissButton = {
            TextButton(onClick = { closeDialog() }) { Text(stringResource(R.string.cancel)) }
        },
        title = { Text(stringResource(R.string.about_email_sum)) },
        text = {
            Column {
                TextField(
                    value = message.value,
                    onValueChange = { message.value = it },
                    singleLine = false,
                    modifier = Modifier.defaultMinSize(minHeight = 200.dp)
                )
                Text(
                    text = message.value.length.toString(),
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    )
}

@Preview
@Composable
private fun EmailAlertDialogPreview() = MdcTheme { EmailAlertDialog({}, {}) }
