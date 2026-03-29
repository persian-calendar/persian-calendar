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
import kotlinx.collections.immutable.persistentListOf

@Composable
fun EmailDialog(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
) {
    var firstPass by rememberSaveable {
        mutableStateOf(eventsRepository.iranOthers || language.isUserAbleToReadPersian)
    }
    if (firstPass) return NoteOnAppointments(onDismissRequest) { firstPass = false }
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
fun NoteOnAppointments(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    nextStep: (() -> Unit)? = null,
) {
    CompositionLocalProvider(
        LocalLayoutDirection provides LayoutDirection.Rtl,
    ) {
        AppDialog(
            modifier = modifier,
            confirmButton = { TextButton(onClick = onDismissRequest) { Text("متوجه شدم") } },
            neutralButton = { if (nextStep != null) TextButton(onClick = nextStep) { Text("سایر امکانات") } },
            onDismissRequest = onDismissRequest,
        ) {
            Text(
                """هدف اصلی توسعهٔ این برنامه نمایش صحیح تعطیلی‌های رسمی بوده است. به دلیل محدودیت‌هایی از جمله در حجم، آفلاین‌بودن، اختلاف در منابع و احتمال عدم تطابق با رویدادها در زمان رخ‌دادن آن‌ها در این برناهه همواره اولویت نمایش نمایش صحیح تعطیلی‌های رسمی به دلیل اهمیت آن‌ها در برنامه‌ریزی کارها در زندگی بوده است. در نسخه‌های سال‌ها پیش این برنامه مناسبت‌های غیرتعطیل قرار نداده شده بود که بعدها توسط سایر توسعه‌دهندگان به پروژه اضافه شدند که به همین دلیل ناقص و بصورت پیش‌فرض غیرفعال است. فعال‌سازی هر بخشی از مناسبت‌ها به جز تعطیلات رسمی به عهده و علاقهٔ کاربر است.

اگر مناسبت‌های غیرتعطیل این برنامه را ناقص یا اشتباه یافته‌اید پیشنهاد می‌شود از منابع آنلاین زیر به مناسبت‌های بیشتر دسترسی یابید.
""".trim(),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(all = 16.dp),
            )
            val urlHandler = LocalUriHandler.current
            persistentListOf(
                "باحساب" to "https://www.bahesab.ir/time/",
                "دانشگاه تهران" to "https://calendar.ut.ac.ir/",
                "تایم.آی‌آر" to "https://www.time.ir/",
            ).forEach { (title, url) ->
                FilledTonalButton(
                    onClick = {
                        urlHandler.openUri(url)
                        onDismissRequest()
                    },
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .fillMaxWidth(.7f),
                ) { Text(title) }
            }
            Text(
                "همچنین می‌توان در تقویم دستگاه مناسبت‌های کشورهای مختلف را فعال کرد که در این برنامه نیز نمایش داده می‌شود که راهنمای آن با زدن دکمهٔ زیر قابل مشاهده است.",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(all = 16.dp),
            )
            EnableInDeviceCalendar(onDismissRequest = onDismissRequest)
        }
    }
}

@Composable
fun ColumnScope.EnableInDeviceCalendar(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    FilledTonalButton(
        modifier = modifier
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
