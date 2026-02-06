package com.byagowi.persiancalendar.ui.about

import android.app.Activity
import android.graphics.RuntimeShader
import android.media.MediaPlayer
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.LocalActivity
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
import androidx.compose.foundation.text.selection.SelectionContainer
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.RenderEffect
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
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
import com.byagowi.persiancalendar.ui.common.BaseAppDialog
import com.byagowi.persiancalendar.ui.common.ZoomableCanvas
import com.byagowi.persiancalendar.ui.theme.resolveFontFile
import com.byagowi.persiancalendar.ui.utils.getResourcesColor
import com.byagowi.persiancalendar.ui.utils.performHapticFeedbackVirtualKey
import com.byagowi.persiancalendar.utils.createStatusIcon
import com.byagowi.persiancalendar.utils.getDayIconResource
import com.byagowi.persiancalendar.utils.logException
import com.byagowi.persiancalendar.utils.monthName
import org.intellij.lang.annotations.Language
import java.util.concurrent.TimeUnit
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.random.Random
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
            Text(
                buildAnnotatedString {
                    withStyle(style.toSpanStyle()) {
                        append(title)
                    }
                    append(" w=${style.fontWeight?.weight} ")
                    when {
                        style.fontSize.isSp -> append("${style.fontSize.value}sp")
                        style.fontSize.isEm -> append("${style.fontSize.value}em")
                        else -> append("${style.fontSize.value} UNKNOWN!")
                    }
                },
            )
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
                                shape.bottomStart,
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
        "0", "10", "50", "100", "200", "300", "400", "500", "600", "700", "800", "900", "1000",
    )
    val cols = listOf("", "accent1", "accent2", "accent3", "neutral1", "neutral2")
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
                                .background(getResourcesColor(dynamicColors[rows.size * j + i]))
                                .weight(1f),
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
            val isBoldFont = isBoldFont
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
                            value.seconds.inWholeMilliseconds, TimeUnit.MILLISECONDS,
                        ).build()
                        WorkManager.getInstance(context).beginUniqueWork(
                            "TestAlarm", ExistingWorkPolicy.REPLACE, alarmWorker,
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
        },
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
                    ),
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
        Text(
            text = numeral.format(weight.roundToInt()),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
fun createIconRandomEffects(): () -> RenderEffect? {
    var clickCount = 0
    val colorShader by lazy(LazyThreadSafetyMode.NONE) { RuntimeShader(COLOR_SHIFT_EFFECT) }
    return {
        if (clickCount++ % 2 == 0) {
            colorShader.setFloatUniform("colorShift", Random.nextFloat())
            android.graphics.RenderEffect.createRuntimeShaderEffect(colorShader, "content")
                .asComposeRenderEffect()
        } else {
            val r = Random.nextFloat() * 30
            BlurEffect(r, r)
        }
    }
}

@Language("AGSL")
private const val COLOR_SHIFT_EFFECT = """
uniform shader content;

uniform float colorShift;

// https://gist.github.com/983/e170a24ae8eba2cd174f
half3 rgb2hsv(half3 c) {
    half4 K = half4(0.0, -1.0 / 3.0, 2.0 / 3.0, -1.0);
    half4 p = mix(half4(c.bg, K.wz), half4(c.gb, K.xy), step(c.b, c.g));
    half4 q = mix(half4(p.xyw, c.r), half4(c.r, p.yzx), step(p.x, c.r));

    float d = q.x - min(q.w, q.y);
    float e = 1.0e-10;
    return half3(abs(q.z + (q.w - q.y) / (6.0 * d + e)), d / (q.x + e), q.x);
}

half3 hsv2rgb(half3 c) {
    half4 K = half4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    half3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

half4 main(float2 fragCoord) {
    half4 color = content.eval(fragCoord);
    half3 hsv = rgb2hsv(color.rgb);
    hsv.x = mod(hsv.x + colorShift, 1);
    return half4(hsv2rgb(hsv), color.a);
}
"""

@Composable
fun createEasterEggClickHandler(callback: (Activity) -> Unit): () -> Unit {
    var clickCount by rememberSaveable { mutableIntStateOf(0) }
    val activity = LocalActivity.current
    return {
        if (activity != null) runCatching {
            when (++clickCount % 10) {
                0 -> callback(activity)
                9 -> Toast.makeText(activity, "One more to go!", Toast.LENGTH_SHORT).show()
            }
        }.onFailure(logException)
    }
}

@Composable
fun PeriodicTableDialog(onDismissRequest: () -> Unit) {
    var showRawData by rememberSaveable { mutableStateOf(false) }
    if (showRawData) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            val result =
                elements.asReversed().mapIndexed { index, s -> "${elements.size - index},$s" }
                    .joinToString("\n")
            AppDialog(onDismissRequest = onDismissRequest) {
                SelectionContainer { Text(text = result) }
            }
        }
        return
    }

    var title by rememberSaveable {
        mutableStateOf(
            "1s2 | 2s2 2p6 | 3s2 3p6 | 3d10 4s2 4p6 | 4d10 5s2 5p6 | 4f14 5d10 6s2 6p6 | 5f14 6d10 7s2 7p6",
        )
    }
    BaseAppDialog(
        title = {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    lineHeight = 16.sp,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        neutralButton = null,
        confirmButton = null,
        dismissButton = null,
    ) {
        val textMeasurer = rememberTextMeasurer()
        val textStyle = LocalTextStyle.current
        fun cellSize(size: Size) = min(size.width / 18, size.height / 9)
        fun canvasTop(size: Size, cellSize: Float = cellSize(size)) =
            (size.height - cellSize * 9).coerceAtLeast(0f) / 2

        var playMusic by remember { mutableStateOf(false) }
        if (playMusic) {
            val context = LocalContext.current
            DisposableEffect(Unit) {
                // https://commons.wikimedia.org/wiki/File:Ave_Maria_(Bach-Gounod).mid
                val mediaPlayer = MediaPlayer.create(context, R.raw.avemaria)
                runCatching { if (!mediaPlayer.isPlaying) mediaPlayer.start() }.onFailure(
                    logException,
                )
                onDispose { runCatching { mediaPlayer.stop() }.onFailure(logException) }
            }
        }

        ZoomableCanvas(
            modifier = Modifier.fillMaxSize(),
            scaleRange = 1f..64f,
            contentSize = { size ->
                val cellSize = cellSize(size)
                Size(width = cellSize * 18, height = cellSize * 9)
            },
            onClick = { position: Offset, canvasSize: Size ->
                val cellSize = cellSize(canvasSize)
                val index =
                    floor(position.x / cellSize).toInt() + floor((position.y - canvasTop(canvasSize)) / cellSize).toInt() * 18
                elementsIndices.getOrNull(index)?.let { atomicNumber ->
                    val info = elements.getOrNull(atomicNumber - 1)?.split(",") ?: return@let
                    title = "$atomicNumber ${info[0]} ${info[1]}\n${info[2]}"
                }
                if (index == 161) showRawData = true else if (index == 144) playMusic = true
            },
        ) {
            val cellSize = cellSize(this.size)
            val rectTopLeft = Offset(.02f * cellSize, .02f * cellSize)
            val top = canvasTop(this.size, cellSize)
            val rectSize = Size(.98f * cellSize, .98f * cellSize)
            val textStyle = textStyle.copy(fontSize = (cellSize * .35f).toSp())
            val textFullNameStyle = textStyle.copy(fontSize = (cellSize * .15f).toSp())
            (0..<18).forEach { i ->
                (0..<9).forEach { j ->
                    translate(i * cellSize, j * cellSize + top) {
                        val index =
                            elementsIndices.getOrNull(i + j * 18) ?: return@translate
                        val details = elements[index - 1].split(",")
                        val color = elementsColor.getValue(index)
                        drawRect(color, rectTopLeft, rectSize)
                        textMeasurer.measure(text = details[0], textStyle).also {
                            val topLeft = Offset(
                                x = cellSize / 2f - it.size.width / 2f,
                                y = cellSize * .27f - it.size.height / 2f,
                            )
                            drawText(textLayoutResult = it, Color.Black, topLeft)
                        }
                        textMeasurer.measure(text = index.toString(), textStyle).also {
                            val topLeft = Offset(
                                x = cellSize / 2f - it.size.width / 2f,
                                y = cellSize * .6f - it.size.height / 2f,
                            )
                            drawText(textLayoutResult = it, Color.Black, topLeft)
                        }
                        textMeasurer.measure(text = details[1], textFullNameStyle)
                            .also {
                                val topLeft = Offset(
                                    x = cellSize / 2f - it.size.width / 2f,
                                    y = cellSize * .87f - it.size.height / 2f,
                                )
                                drawText(textLayoutResult = it, Color.Black, topLeft)
                            }
                    }
                }
            }
        }
    }
}

private val elementsColor = buildMap {
    listOf(3, 11, 19, 37, 55, 87).forEach { put(it, Color(0xffff9d9d)) } // Alkali metals
    listOf(4, 12, 20, 38, 56, 88).forEach { put(it, Color(0xffffdead)) } // Alkaline earth metals
    (57..71).forEach { put(it, Color(0xffffbfff)) } // Lanthanides
    (89..103).forEach { put(it, Color(0xffff99cc)) } // Actinides
    listOf(1, 6, 7, 8, 15, 16, 34).forEach { put(it, Color(0xffa0ffa0)) } // Other nonmetals
    listOf(5, 14, 32, 33, 51, 52).forEach { put(it, Color(0xffcccc99)) } // Metalloids
    // Other nonmetals
    listOf(13, 31, 49, 50, 81, 82, 83, 84, 113, 114, 115, 116).forEach {
        put(it, Color(0xffcccccc))
    }
    listOf(9, 17, 35, 53, 85, 117).forEach { put(it, Color(0xffffff99)) } // Halogens
    listOf(2, 10, 18, 36, 54, 86, 118).forEach { put(it, Color(0xffc0ffff)) } // Noble gases
}.withDefault { Color(0xffffc0c0) } // Transition metals

private val elementsIndices = buildList {
    var i = 1
    add(i++)
    addAll(arrayOfNulls(16))
    add(i++)
    repeat(2) {
        addAll(List(2) { i++ })
        addAll(arrayOfNulls(10))
        addAll(List(6) { i++ })
    }
    repeat(2) { addAll(List(18) { i++ }) }
    repeat(2) {
        addAll(List(2) { i++ })
        i += 14
        addAll(List(16) { i++ })
    }
    repeat(2) {
        i = if (it == 0) 57 else 89
        addAll(arrayOfNulls(2))
        addAll(List(14) { i++ })
        addAll(arrayOfNulls(2))
    }
}

// Based on https://en.wikipedia.org/wiki/Template:Infobox_element/symbol-to-electron-configuration
// Algorithmic atomic configuration won't be perfect, see also https://github.com/xanecs/aufbau-principle
private val elements = """
H,Hydrogen,1s1
He,Helium,1s2
Li,Lithium,[He] 2s1
Be,Beryllium,[He] 2s2
B,Boron,[He] 2s2 2p1
C,Carbon,[He] 2s2 2p2
N,Nitrogen,[He] 2s2 2p3
O,Oxygen,[He] 2s2 2p4
F,Fluorine,[He] 2s2 2p5
Ne,Neon,[He] 2s2 2p6
Na,Sodium,[Ne] 3s1
Mg,Magnesium,[Ne] 3s2
Al,Aluminium,[Ne] 3s2 3p1
Si,Silicon,[Ne] 3s2 3p2
P,Phosphorus,[Ne] 3s2 3p3
S,Sulfur,[Ne] 3s2 3p4
Cl,Chlorine,[Ne] 3s2 3p5
Ar,Argon,[Ne] 3s2 3p6
K,Potassium,[Ar] 4s1
Ca,Calcium,[Ar] 4s2
Sc,Scandium,[Ar] 3d1 4s2
Ti,Titanium,[Ar] 3d2 4s2
V,Vanadium,[Ar] 3d3 4s2
Cr,Chromium,[Ar] 3d5 4s1
Mn,Manganese,[Ar] 3d5 4s2
Fe,Iron,[Ar] 3d6 4s2
Co,Cobalt,[Ar] 3d7 4s2
Ni,Nickel,[Ar] 3d8 4s2 or [Ar] 3d9 4s1
Cu,Copper,[Ar] 3d10 4s1
Zn,Zinc,[Ar] 3d10 4s2
Ga,Gallium,[Ar] 3d10 4s2 4p1
Ge,Germanium,[Ar] 3d10 4s2 4p2
As,Arsenic,[Ar] 3d10 4s2 4p3
Se,Selenium,[Ar] 3d10 4s2 4p4
Br,Bromine,[Ar] 3d10 4s2 4p5
Kr,Krypton,[Ar] 3d10 4s2 4p6
Rb,Rubidium,[Kr] 5s1
Sr,Strontium,[Kr] 5s2
Y,Yttrium,[Kr] 4d1 5s2
Zr,Zirconium,[Kr] 4d2 5s2
Nb,Niobium,[Kr] 4d4 5s1
Mo,Molybdenum,[Kr] 4d5 5s1
Tc,Technetium,[Kr] 4d5 5s2
Ru,Ruthenium,[Kr] 4d7 5s1
Rh,Rhodium,[Kr] 4d8 5s1
Pd,Palladium,[Kr] 4d10
Ag,Silver,[Kr] 4d10 5s1
Cd,Cadmium,[Kr] 4d10 5s2
In,Indium,[Kr] 4d10 5s2 5p1
Sn,Tin,[Kr] 4d10 5s2 5p2
Sb,Antimony,[Kr] 4d10 5s2 5p3
Te,Tellurium,[Kr] 4d10 5s2 5p4
I,Iodine,[Kr] 4d10 5s2 5p5
Xe,Xenon,[Kr] 4d10 5s2 5p6
Cs,Caesium,[Xe] 6s1
Ba,Barium,[Xe] 6s2
La,Lanthanum,[Xe] 5d1 6s2
Ce,Cerium,[Xe] 4f1 5d1 6s2
Pr,Praseodymium,[Xe] 4f3 6s2
Nd,Neodymium,[Xe] 4f4 6s2
Pm,Promethium,[Xe] 4f5 6s2
Sm,Samarium,[Xe] 4f6 6s2
Eu,Europium,[Xe] 4f7 6s2
Gd,Gadolinium,[Xe] 4f7 5d1 6s2
Tb,Terbium,[Xe] 4f9 6s2
Dy,Dysprosium,[Xe] 4f10 6s2
Ho,Holmium,[Xe] 4f11 6s2
Er,Erbium,[Xe] 4f12 6s2
Tm,Thulium,[Xe] 4f13 6s2
Yb,Ytterbium,[Xe] 4f14 6s2
Lu,Lutetium,[Xe] 4f14 5d1 6s2
Hf,Hafnium,[Xe] 4f14 5d2 6s2
Ta,Tantalum,[Xe] 4f14 5d3 6s2
W,Tungsten,[Xe] 4f14 5d4 6s2
Re,Rhenium,[Xe] 4f14 5d5 6s2
Os,Osmium,[Xe] 4f14 5d6 6s2
Ir,Iridium,[Xe] 4f14 5d7 6s2
Pt,Platinum,[Xe] 4f14 5d9 6s1
Au,Gold,[Xe] 4f14 5d10 6s1
Hg,Mercury,[Xe] 4f14 5d10 6s2
Tl,Thallium,[Xe] 4f14 5d10 6s2 6p1 (to check)
Pb,Lead,[Xe] 4f14 5d10 6s2 6p2
Bi,Bismuth,[Xe] 4f14 5d10 6s2 6p3
Po,Polonium,[Xe] 4f14 5d10 6s2 6p4
At,Astatine,[Xe] 4f14 5d10 6s2 6p5
Rn,Radon,[Xe] 4f14 5d10 6s2 6p6
Fr,Francium,[Rn] 7s1
Ra,Radium,[Rn] 7s2
Ac,Actinium,[Rn] 6d1 7s2
Th,Thorium,[Rn] 6d2 7s2
Pa,Protactinium,[Rn] 5f2 6d1 7s2
U,Uranium,[Rn] 5f3 6d1 7s2
Np,Neptunium,[Rn] 5f4 6d1 7s2
Pu,Plutonium,[Rn] 5f6 7s2
Am,Americium,[Rn] 5f7 7s2
Cm,Curium,[Rn] 5f7 6d1 7s2
Bk,Berkelium,[Rn] 5f9 7s2
Cf,Californium,[Rn] 5f10 7s2
Es,Einsteinium,[Rn] 5f11 7s2
Fm,Fermium,[Rn] 5f12 7s2
Md,Mendelevium,[Rn] 5f13 7s2
No,Nobelium,[Rn] 5f14 7s2
Lr,Lawrencium,[Rn] 5f14 7s2 7p1 (modern calculations all favour the 7p1)
Rf,Rutherfordium,[Rn] 5f14 6d2 7s2
Db,Dubnium,[Rn] 5f14 6d3 7s2
Sg,Seaborgium,[Rn] 5f14 6d4 7s2
Bh,Bohrium,[Rn] 5f14 6d5 7s2
Hs,Hassium,[Rn] 5f14 6d6 7s2
Mt,Meitnerium,[Rn] 5f14 6d7 7s2
Ds,Darmstadtium,[Rn] 5f14 6d8 7s2
Rg,Roentgenium,[Rn] 5f14 6d9 7s2
Cn,Copernicium,[Rn] 5f14 6d10 7s2
Nh,Nihonium,[Rn] 5f14 6d10 7s2 7p1
Fl,Flerovium,[Rn] 5f14 6d10 7s2 7p2
Mc,Moscovium,[Rn] 5f14 6d10 7s2 7p3
Lv,Livermorium,[Rn] 5f14 6d10 7s2 7p4
Ts,Tennessine,[Rn] 5f14 6d10 7s2 7p5
Og,Oganesson,[Rn] 5f14 6d10 7s2 7p6
Uue,Ununennium,[Og] 8s1 (predicted)
Ubn,Unbinilium,[Og] 8s2 (predicted)
Ubu,Unbiunium,[Og] 8s2 8p1 (predicted)
Ubb,Unbibium,[Og] 7d1 8s2 8p1
Ubt,Unbitrium,
Ubq,Unbiquadium,[Og] 6f3 8s2 8p1
Ubc,Unbipentium,
Ubh,Unbihexium,[Og] 5g2 6f3 8s2 8p1
""".trim().split("\n")
