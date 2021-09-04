package com.byagowi.persiancalendar.ui.about

import android.app.Activity
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ui.ComposeTheme

fun showEmailDialog(activity: Activity, onSuccess: (String) -> Unit) {
    (activity.window.decorView as? ViewGroup)?.addView(ComposeView(activity).also { composeView ->
        composeView.setContent { ComposeTheme { EmailAlertDialog(onSuccess) } }
    })
}

@Composable
fun EmailAlertDialog(onSuccess: (String) -> Unit) {
    val isDialogOpen = remember { mutableStateOf(true) }
    if (!isDialogOpen.value) return
    Surface(color = Color.Transparent) {
        val message = remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { isDialogOpen.value = false },
            confirmButton = {
                TextButton(onClick = {
                    isDialogOpen.value = false
                    onSuccess(message.value)
                }) { Text(stringResource(R.string.continue_button)) }
            },
            dismissButton = {
                TextButton(onClick = { isDialogOpen.value = false }) {
                    Text(stringResource(R.string.cancel))
                }
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
}

@Preview(showBackground = true)
@Composable
fun EmailAlertDialogPreview() {
    ComposeTheme { EmailAlertDialog {} }
}
