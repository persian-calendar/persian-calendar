package com.byagowi.persiancalendar.ui.about

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.enabledCalendars
import com.byagowi.persiancalendar.global.isBoldFont
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.numeral
import com.byagowi.persiancalendar.service.AlarmWorker
import com.byagowi.persiancalendar.ui.common.AppDialog
import com.byagowi.persiancalendar.ui.common.AppDialogWithLazyColumn
import com.byagowi.persiancalendar.ui.theme.resolveFontFile
import com.byagowi.persiancalendar.ui.utils.performHapticFeedbackVirtualKey
import com.byagowi.persiancalendar.utils.createStatusIcon
import com.byagowi.persiancalendar.utils.getDayIconResource
import com.byagowi.persiancalendar.utils.monthName
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.seconds

@Composable
fun ColorSchemeDemoDialog(onDismissRequest: () -> Unit) {
    AppDialog(onDismissRequest = onDismissRequest) {
        listOf(
            "primary" to MaterialTheme.colorScheme.primary,
            "onPrimary" to MaterialTheme.colorScheme.onPrimary,
            "primaryContainer" to MaterialTheme.colorScheme.primaryContainer,
            "onPrimaryContainer" to MaterialTheme.colorScheme.onPrimaryContainer,
            "inversePrimary" to MaterialTheme.colorScheme.inversePrimary,
            "secondary" to MaterialTheme.colorScheme.secondary,
            "onSecondary" to MaterialTheme.colorScheme.onSecondary,
            "secondaryContainer" to MaterialTheme.colorScheme.secondaryContainer,
            "onSecondaryContainer" to MaterialTheme.colorScheme.onSecondaryContainer,
            "tertiary" to MaterialTheme.colorScheme.tertiary,
            "onTertiary" to MaterialTheme.colorScheme.onTertiary,
            "tertiaryContainer" to MaterialTheme.colorScheme.tertiaryContainer,
            "onTertiaryContainer" to MaterialTheme.colorScheme.onTertiaryContainer,
            "background" to MaterialTheme.colorScheme.background,
            "onBackground" to MaterialTheme.colorScheme.onBackground,
            "surface" to MaterialTheme.colorScheme.surface,
            "onSurface" to MaterialTheme.colorScheme.onSurface,
            "surfaceVariant" to MaterialTheme.colorScheme.surfaceVariant,
            "onSurfaceVariant" to MaterialTheme.colorScheme.onSurfaceVariant,
            "surfaceTint" to MaterialTheme.colorScheme.surfaceTint,
            "inverseSurface" to MaterialTheme.colorScheme.inverseSurface,
            "inverseOnSurface" to MaterialTheme.colorScheme.inverseOnSurface,
            "error" to MaterialTheme.colorScheme.error,
            "onError" to MaterialTheme.colorScheme.onError,
            "errorContainer" to MaterialTheme.colorScheme.errorContainer,
            "onErrorContainer" to MaterialTheme.colorScheme.onErrorContainer,
            "outline" to MaterialTheme.colorScheme.outline,
            "outlineVariant" to MaterialTheme.colorScheme.outlineVariant,
            "scrim" to MaterialTheme.colorScheme.scrim,
        ).map { (title, color) ->
            Text(
                text = title,
                color = MaterialTheme.colorScheme.contentColorFor(color),
                modifier = Modifier.background(color, MaterialTheme.shapes.extraSmall),
            )
        }
    }
}

@Composable
fun TypographyDemoDialog(onDismissRequest: () -> Unit) {
    AppDialog(onDismissRequest = onDismissRequest) {
        listOf(
            "DisplayLarge" to MaterialTheme.typography.displayLarge,
            "DisplayMedium" to MaterialTheme.typography.displayMedium,
            "DisplaySmall" to MaterialTheme.typography.displaySmall,
            "HeadlineLarge" to MaterialTheme.typography.headlineLarge,
            "HeadlineMedium" to MaterialTheme.typography.headlineMedium,
            "HeadlineSmall" to MaterialTheme.typography.headlineSmall,
            "TitleLarge" to MaterialTheme.typography.titleLarge,
            "TitleMedium" to MaterialTheme.typography.titleMedium,
            "TitleSmall" to MaterialTheme.typography.titleSmall,
            "BodyLarge" to MaterialTheme.typography.bodyLarge,
            "BodyMedium" to MaterialTheme.typography.bodyMedium,
            "BodySmall" to MaterialTheme.typography.bodySmall,
            "LabelLarge" to MaterialTheme.typography.labelLarge,
            "LabelMedium" to MaterialTheme.typography.labelMedium,
            "LabelSmall" to MaterialTheme.typography.labelSmall,
        ).map { (title, style) ->
            Text(buildAnnotatedString {
                withStyle(style.toSpanStyle()) {
                    append(title)
                }
                append(" w=${style.fontWeight?.weight} ")
                when {
                    style.fontSize.isSp -> append("${style.fontSize.value}sp")
                    style.fontSize.isEm -> append("${style.fontSize.value}em")
                    else -> append("${style.fontSize.value} UNKNOWN!")
                }
            })
        }
    }
}

@Composable
fun ShapesDemoDialog(onDismissRequest: () -> Unit) {
    fun f(cornerSize: CornerSize): String {
        return cornerSize.toString().replace("CornerSize", "").replace("size = ", "")
    }
    AppDialog(onDismissRequest = onDismissRequest) {
        listOf(
            "extraLarge" to MaterialTheme.shapes.extraLarge,
            "large" to MaterialTheme.shapes.large,
            "medium" to MaterialTheme.shapes.medium,
            "small" to MaterialTheme.shapes.small,
            "extraSmall" to MaterialTheme.shapes.extraSmall,
        ).map { (title, shape) ->
            Surface(
                color = MaterialTheme.colorScheme.onSurface,
                shape = shape,
                modifier = Modifier
                    .size(160.dp)
                    .padding(4.dp),
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = "$title topStart=${f(shape.topStart)} topEnd=${f(shape.topEnd)} bottomStart=${
                            f(
                                shape.bottomStart
                            )
                        } topEnd=${f(shape.bottomEnd)}",
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.surface,
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun DynamicColorsDialog(onDismissRequest: () -> Unit) {
    val dynamicColors = remember {
        listOf(
            android.R.color.system_accent1_0, android.R.color.system_accent1_10,
            android.R.color.system_accent1_50, android.R.color.system_accent1_100,
            android.R.color.system_accent1_200, android.R.color.system_accent1_300,
            android.R.color.system_accent1_400, android.R.color.system_accent1_500,
            android.R.color.system_accent1_600, android.R.color.system_accent1_700,
            android.R.color.system_accent1_800, android.R.color.system_accent1_900,
            android.R.color.system_accent1_1000,
            android.R.color.system_accent2_0, android.R.color.system_accent2_10,
            android.R.color.system_accent2_50, android.R.color.system_accent2_100,
            android.R.color.system_accent2_200, android.R.color.system_accent2_300,
            android.R.color.system_accent2_400, android.R.color.system_accent2_500,
            android.R.color.system_accent2_600, android.R.color.system_accent2_700,
            android.R.color.system_accent2_800, android.R.color.system_accent2_900,
            android.R.color.system_accent2_1000,
            android.R.color.system_accent3_0, android.R.color.system_accent3_10,
            android.R.color.system_accent3_50, android.R.color.system_accent3_100,
            android.R.color.system_accent3_200, android.R.color.system_accent3_300,
            android.R.color.system_accent3_400, android.R.color.system_accent3_500,
            android.R.color.system_accent3_600, android.R.color.system_accent3_700,
            android.R.color.system_accent3_800, android.R.color.system_accent3_900,
            android.R.color.system_accent3_1000,
            android.R.color.system_neutral1_0, android.R.color.system_neutral1_10,
            android.R.color.system_neutral1_50, android.R.color.system_neutral1_100,
            android.R.color.system_neutral1_200, android.R.color.system_neutral1_300,
            android.R.color.system_neutral1_400, android.R.color.system_neutral1_500,
            android.R.color.system_neutral1_600, android.R.color.system_neutral1_700,
            android.R.color.system_neutral1_800, android.R.color.system_neutral1_900,
            android.R.color.system_neutral1_1000,
            android.R.color.system_neutral2_0, android.R.color.system_neutral2_10,
            android.R.color.system_neutral2_50, android.R.color.system_neutral2_100,
            android.R.color.system_neutral2_200, android.R.color.system_neutral2_300,
            android.R.color.system_neutral2_400, android.R.color.system_neutral2_500,
            android.R.color.system_neutral2_600, android.R.color.system_neutral2_700,
            android.R.color.system_neutral2_800, android.R.color.system_neutral2_900,
            android.R.color.system_neutral2_1000,
        )
    }
    val rows = listOf(
        "0", "10", "50", "100", "200", "300", "400", "500", "600", "700", "800", "900", "1000"
    )
    val cols = listOf("", "accent1", "accent2", "accent3", "neutral1", "neutral2")
    val context = LocalContext.current
    AppDialog(onDismissRequest = onDismissRequest) {
        Column {
            Row {
                cols.forEach {
                    Text(it, Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                }
            }
            rows.forEachIndexed { i, title ->
                Row {
                    Text(title, Modifier.weight(1f))
                    cols.drop(1).forEachIndexed { j, _ ->
                        Box(
                            Modifier
                                .background(Color(context.getColor(dynamicColors[rows.size * j + i])))
                                .weight(1f)
                        ) { Text(" ") }
                    }
                }
            }
        }
    }
}

// Debug only dialog to check validity of dynamic icons generation
@Composable
fun IconsDemoDialog(onDismissRequest: () -> Unit) {
    AppDialog(onDismissRequest = onDismissRequest) {
        FlowRow {
            val fontFile = resolveFontFile()
            val isBoldFont by isBoldFont.collectAsState()
            (0..61).forEach {
                val day = it / 2 + 1
                Image(
                    bitmap = if (it % 2 == 0) ImageBitmap.imageResource(getDayIconResource(day))
                    else createStatusIcon(day, fontFile, isBoldFont).asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(all = 4.dp)
                        .background(Color.Gray)
                        .size(36.dp),
                )
            }
        }
    }
}

@Composable
fun ScheduleAlarm(onDismissRequest: () -> Unit) {
    val context = LocalContext.current
    var seconds by rememberSaveable { mutableStateOf("5") }
    AppDialog(
        title = { Text("Enter seconds to schedule alarm") },
        confirmButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                    seconds.toIntOrNull()?.let { value ->
                        val alarmWorker = OneTimeWorkRequestBuilder<AlarmWorker>().setInitialDelay(
                            value.seconds.inWholeMilliseconds, TimeUnit.MILLISECONDS
                        ).build()
                        WorkManager.getInstance(context).beginUniqueWork(
                            "TestAlarm", ExistingWorkPolicy.REPLACE, alarmWorker
                        ).enqueue()
                        Toast.makeText(context, "Alarm in ${value}s", Toast.LENGTH_SHORT).show()
                    }
                },
            ) { Text(stringResource(R.string.accept)) }
        },
        onDismissRequest = onDismissRequest,
    ) {
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = seconds,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            onValueChange = { seconds = it },
        )
    }
}

@Composable
fun ConverterDialog(onDismissRequest: () -> Unit) {
    val now = Jdn.today()
    val language by language.collectAsState()
    val numeral by numeral.collectAsState()
    val lazyListState = rememberLazyListState(pagesCount / 2)
    val textStyle = LocalTextStyle.current
    val calendars = enabledCalendars.takeIf { it.size > 1 } ?: language.defaultCalendars
    var sourceCalendar by rememberSaveable { mutableStateOf(calendars[0]) }
    val otherCalendars = calendars - sourceCalendar
    var destinationCalendar by rememberSaveable(sourceCalendar) { mutableStateOf(otherCalendars[0]) }
    AppDialogWithLazyColumn(
        lazyListState = lazyListState,
        onDismissRequest = onDismissRequest,
        title = {
            CompositionLocalProvider(LocalTextStyle provides textStyle) {
                Column {
                    PrimaryTabRow(
                        selectedTabIndex = calendars.indexOf(sourceCalendar),
                        divider = {},
                        containerColor = Color.Transparent,
                        indicator = {
                            val index = calendars.indexOf(sourceCalendar)
                            TabRowDefaults.PrimaryIndicator(Modifier.tabIndicatorOffset(index))
                        },
                    ) {
                        val view = LocalView.current
                        calendars.forEach {
                            Tab(
                                text = {
                                    Text(
                                        stringResource(it.shortTitle),
                                        maxLines = 1,
                                        autoSize = TextAutoSize.StepBased(
                                            minFontSize = 5.sp,
                                            maxFontSize = LocalTextStyle.current.fontSize,
                                        ),
                                    )
                                },
                                modifier = Modifier.clip(MaterialTheme.shapes.large),
                                selected = it == sourceCalendar,
                                unselectedContentColor = MaterialTheme.colorScheme.onSurface,
                                onClick = {
                                    view.performHapticFeedbackVirtualKey()
                                    sourceCalendar = it
                                },
                            )
                        }
                    }
                    SecondaryTabRow(
                        selectedTabIndex = otherCalendars.indexOf(destinationCalendar),
                        divider = {},
                        containerColor = Color.Transparent,
                        indicator = {
                            val index = otherCalendars.indexOf(destinationCalendar)
                            TabRowDefaults.SecondaryIndicator(Modifier.tabIndicatorOffset(index))
                        },
                    ) {
                        val view = LocalView.current
                        otherCalendars.forEach {
                            Tab(
                                text = {
                                    Text(
                                        stringResource(it.title),
                                        maxLines = 1,
                                        autoSize = TextAutoSize.StepBased(
                                            minFontSize = 5.sp,
                                            maxFontSize = LocalTextStyle.current.fontSize,
                                        ),
                                    )
                                },
                                modifier = Modifier.clip(MaterialTheme.shapes.large),
                                selected = it == destinationCalendar,
                                unselectedContentColor = MaterialTheme.colorScheme.onSurface,
                                onClick = {
                                    view.performHapticFeedbackVirtualKey()
                                    destinationCalendar = it
                                },
                            )
                        }
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onDismissRequest) { Text(stringResource(R.string.close)) }
        }
    ) {
        items(pagesCount) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                val date = sourceCalendar.getMonthStartFromMonthsDistance(
                    baseJdn = now,
                    monthsDistance = it - pagesCount / 2,
                )
                Text(
                    language.my.format(
                        date.monthName,
                        numeral.format(date.year),
                    ),
                    maxLines = 1,
                    autoSize = TextAutoSize.StepBased(
                        minFontSize = 5.sp,
                        maxFontSize = LocalTextStyle.current.fontSize,
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
                val startOfMonth = Jdn(date) on destinationCalendar
                Text(
                    language.dmy.format(
                        numeral.format(startOfMonth.dayOfMonth),
                        startOfMonth.monthName,
                        numeral.format(startOfMonth.year),
                    ),
                    maxLines = 1,
                    autoSize = TextAutoSize.StepBased(
                        minFontSize = 5.sp,
                        maxFontSize = LocalTextStyle.current.fontSize,
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
                val endOfMonth = (Jdn(
                    sourceCalendar.getMonthStartFromMonthsDistance(
                        baseJdn = now,
                        monthsDistance = it - pagesCount / 2 + 1,
                    )
                ) - 1) on destinationCalendar
                Text(
                    language.dmy.format(
                        numeral.format(endOfMonth.dayOfMonth),
                        endOfMonth.monthName,
                        numeral.format(endOfMonth.year),
                    ),
                    maxLines = 1,
                    autoSize = TextAutoSize.StepBased(
                        minFontSize = 5.sp,
                        maxFontSize = LocalTextStyle.current.fontSize,
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

private const val pagesCount = 20000

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FontWeightsDialog(onDismissRequest: () -> Unit) {
    AppDialog(onDismissRequest = onDismissRequest) {
        val text by remember { mutableStateOf(TextFieldState("Sample text متن نمونه")) }
        var weight by remember { mutableFloatStateOf(400f) }
        TextField(
            state = text,
            textStyle = LocalTextStyle.current.copy(
                fontSize = MaterialTheme.typography.headlineSmall.fontSize,
                fontWeight = FontWeight(weight.roundToInt()),
                textAlign = TextAlign.Center,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
        Slider(
            valueRange = 100f..900f,
            value = weight,
            onValueChange = { weight = it },
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        val numeral by numeral.collectAsState()
        Text(
            text = numeral.format(weight.roundToInt()),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
    }
}
