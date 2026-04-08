package com.byagowi.persiancalendar.ui.about

import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.global.numeral
import com.byagowi.persiancalendar.ui.common.AppDialog

@Composable
fun EmailDialog(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
) {
    var message by rememberSaveable { mutableStateOf("") }
    AppDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            val context = LocalContext.current
            TextButton(
                onClick = {
                    onDismissRequest()
                    launchEmailIntent(context, message)
                },
            ) { Text(stringResource(R.string.continue_button)) }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) { Text(stringResource(R.string.cancel)) }
        },
        title = { Text(stringResource(R.string.about_email_sum)) },
        modifier = modifier,
    ) {
        TextField(
            value = message,
            onValueChange = { message = it },
            singleLine = false,
            modifier = Modifier
                .defaultMinSize(minHeight = 200.dp)
                .fillMaxWidth(),
        )
        Text(
            text = numeral.format(message.length),
            modifier = Modifier
                .align(Alignment.End)
                .padding(end = 8.dp),
        )
    }
}

@Composable
fun EnableInDeviceCalendar(modifier: Modifier = Modifier) {
    val uriHandler = LocalUriHandler.current
    FilledTonalButton(
        modifier = modifier.fillMaxWidth(.7f),
        onClick = { uriHandler.openUri("https://support.google.com/calendar/answer/13748345?hl=fa") },
    ) {
        Text(
            text = "راهنمای فعال‌سازی مناسبت‌ها از تقویم دستگاه",
            maxLines = 1,
            autoSize = TextAutoSize.StepBased(9.sp, LocalTextStyle.current.fontSize),
            textAlign = TextAlign.Center,
        )
    }
}

@Preview
@Composable
internal fun EmailDialogPreview() = EmailDialog {}
