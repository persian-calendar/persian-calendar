package com.byagowi.persiancalendar.ui.about

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.global.eventsRepository
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.numeral
import com.byagowi.persiancalendar.ui.common.AppDialog

@Composable
fun EmailDialog(onDismissRequest: () -> Unit) {
    var firstPass by rememberSaveable {
        mutableStateOf(language.isUserAbleToReadPersian || eventsRepository.iranOthers)
    }
    if (firstPass) return CompositionLocalProvider(
        LocalLayoutDirection provides LayoutDirection.Rtl,
    ) {
        AppDialog(
            confirmButton = {
                TextButton(onClick = onDismissRequest) { Text("متوجه شدم") }
            },
            neutralButton = {
                TextButton(onClick = { firstPass = false }) { Text("سایر امکانات") }
            },
            onDismissRequest = onDismissRequest,
        ) {
            Text(
                """هدف اصلی این برنامه نمایش تعطیلی‌های رسمی کشورهای حمایت شده است و سایر مناسبت‌ها بعدها توسط سایر افراد به برنامه اضافه شده است که به‌صورت پیش‌فرض در برنامه فعال نیست و مانند سایر امکانات این برنامه دارای تضمین کامل بودن یا صحت عملکرد نیست.

اگر مناسبت‌های این برنامه مناسب یا کافی نیست لطفاً منابع یا برنامه‌های دیگر را بیازمایید چرا که این برنامه امکانات محدودی در نمایش مناسبت‌ها دارد و تمرکز آن بر تعطیلی‌هاست.

همچنین می‌توان در تقویم دستگاه مناسبت‌های کشورهای مختلف را فعال کرد که در این برنامه نیز نمایش داده می‌شود که راهنمایی در زیر دکمهٔ زیر قرار دارد.
""".trim(),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(all = 16.dp),
            )
            EnableInDeviceCalendar(onDismissRequest)
        }
    }
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

//@Composable
//fun More() {
//    AnimatedVisibility(eventsRepository.iranOthers && language.isPersian) {
//        var showDialog by rememberSaveable { mutableStateOf(false) }
//        if (showDialog) AppDialog(onDismissRequest = { showDialog = false }) {
//            Text(
//                "منابع آنلاین برای سایر مناسبت‌های تقویمی",
//                style = MaterialTheme.typography.bodyLarge,
//                textAlign = TextAlign.Center,
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(top = 24.dp, bottom = 16.dp)
//                    .align(Alignment.CenterHorizontally),
//            )
//            val urlHandler = LocalUriHandler.current
//            persistentListOf(
//                "مرکز تقویم دانشگاه تهران" to "https://calendar.ut.ac.ir/",
//                "باحساب" to "https://www.bahesab.ir/",
//                "تایم.آی‌آر" to "https://www.time.ir/",
//            ).shuffled().forEach { (title, url) ->
//                FilledTonalButton(
//                    onClick = {
//                        urlHandler.openUri(url)
//                        showDialog = false
//                    },
//                    modifier = Modifier
//                        .align(Alignment.CenterHorizontally)
//                        .fillMaxWidth(.7f),
//                ) { Text(title) }
//            }
//            EnableInDeviceCalendar { showDialog = false }
//            TextButton(
//                onClick = { showDialog = false },
//                modifier = Modifier
//                    .padding(top = 8.dp, bottom = 12.dp)
//                    .align(Alignment.CenterHorizontally),
//            ) { Text(stringResource(R.string.close)) }
//        }
//        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
//            Box(
//                Modifier
//                    .padding(top = 8.dp)
//                    .alpha(AppBlendAlpha)
//                    .clip(MaterialTheme.shapes.large)
//                    .background(MaterialTheme.colorScheme.surfaceVariant)
//                    .clickable { showDialog = true }
//                    .padding(horizontal = 12.dp, vertical = 1.dp),
//            ) { Text("سایر مناسبت‌ها") }
//        }
//    }
//}

@Composable
fun ColumnScope.EnableInDeviceCalendar(onDismissRequest: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    FilledTonalButton(
        modifier = Modifier
            .align(Alignment.CenterHorizontally)
            .fillMaxWidth(.7f),
        onClick = {
            uriHandler.openUri("https://support.google.com/calendar/answer/13748345?hl=fa")
            onDismissRequest()
        },
    ) {
        Text(
            "فعال‌سازی مناسبت‌ها در تقویم دستگاه",
            maxLines = 1,
            autoSize = TextAutoSize.StepBased(9.sp, LocalTextStyle.current.fontSize),
            textAlign = TextAlign.Center,
        )
    }
}

@Preview
@Composable
internal fun EmailDialogPreview() = EmailDialog {}
