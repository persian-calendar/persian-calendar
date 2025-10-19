package com.byagowi.persiancalendar.ui.calendar.calendarpager

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import androidx.collection.mutableIntSetOf
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.zIndex
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Calendar
import com.byagowi.persiancalendar.entities.CalendarEvent
import com.byagowi.persiancalendar.entities.DeviceCalendarEventsStore
import com.byagowi.persiancalendar.entities.EventsStore
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.entities.Language
import com.byagowi.persiancalendar.global.eventsRepository
import com.byagowi.persiancalendar.global.isHighTextContrastEnabled
import com.byagowi.persiancalendar.global.isShowWeekOfYearEnabled
import com.byagowi.persiancalendar.global.isTalkBackEnabled
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.global.mainCalendarDigits
import com.byagowi.persiancalendar.ui.calendar.AddEventData
import com.byagowi.persiancalendar.ui.icons.MaterialIconDimension
import com.byagowi.persiancalendar.ui.theme.appMonthColors
import com.byagowi.persiancalendar.ui.theme.resolveFontFile
import com.byagowi.persiancalendar.ui.utils.AppBlendAlpha
import com.byagowi.persiancalendar.utils.applyWeekStartOffsetToWeekDay
import com.byagowi.persiancalendar.utils.formatNumber
import com.byagowi.persiancalendar.utils.getA11yDaySummary
import com.byagowi.persiancalendar.utils.getInitialOfWeekDay
import com.byagowi.persiancalendar.utils.getShiftWorkTitle
import com.byagowi.persiancalendar.utils.getWeekDayName
import com.byagowi.persiancalendar.utils.isTamilDigitSelected
import com.byagowi.persiancalendar.utils.revertWeekStartOffsetFromWeekDay
import io.github.persiancalendar.calendar.AbstractDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.ceil
import kotlin.math.min

// For performance reasons it has two phases, a initiation phase that should be called outside
// the pager which creates a callback that should be invoked inside the pager.
@Composable
fun daysTable(
    suggestedPagerSize: DpSize,
    addEvent: (AddEventData) -> Unit,
    today: Jdn,
    refreshToken: Int,
    setSelectedDay: (Jdn) -> Unit,
    pagerState: PagerState,
    modifier: Modifier = Modifier,
    secondaryCalendar: Calendar? = null,
    onWeekClick: ((Jdn, Boolean) -> Unit)? = null,
    isWeekMode: Boolean = false,
): @Composable (
    page: Int, monthStartDate: AbstractDate, monthStartJdn: Jdn,
    deviceEvents: DeviceCalendarEventsStore, onlyWeek: Int?,
    isHighlighted: Boolean, selectedDay: Jdn,
) -> Unit {
    val isShowWeekOfYearEnabled by isShowWeekOfYearEnabled.collectAsState()
    val density = LocalDensity.current
    val (width, suggestedHeight) = suggestedPagerSize
    val cellWidth = (width - (pagerArrowSizeAndPadding * 2).dp) / 7
    val cellWidthPx = with(density) { cellWidth.toPx() }
    val cellHeight = suggestedHeight / if (isWeekMode) 2 else 7
    val cellHeightPx = with(density) { cellHeight.toPx() }
    val cellRadius = min(cellWidthPx, cellHeightPx) / 2 - with(density) { .5f.dp.toPx() }
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val pagerArrowSizeAndPaddingPx = with(density) { pagerArrowSizeAndPadding.dp.toPx() }
    val fontFile = resolveFontFile()
    val language by language.collectAsState()
    val monthColors = appMonthColors()
    val coroutineScope = rememberCoroutineScope()

    val resources = LocalResources.current
    val diameter = min(cellWidth, cellHeight)
    val context = LocalContext.current
    val dayPainter = remember(
        cellWidthPx, suggestedHeight, refreshToken, monthColors, resources, fontFile
    ) {
        DayPainter(
            context = context,
            resources = resources,
            width = cellWidthPx,
            height = cellHeightPx,
            isRtl = isRtl,
            colors = monthColors,
            fontFile = fontFile,
        )
    }
    val mainCalendarDigitsIsArabic = mainCalendarDigits === Language.ARABIC_DIGITS
    val daysTextSize = diameter * when {
        mainCalendarDigitsIsArabic || fontFile != null -> 18
        isTamilDigitSelected -> 16
        else -> 25
    } / 40
    val daysStyle = LocalTextStyle.current.copy(
        fontSize = with(density) { daysTextSize.toSp() },
    )
    val contentColor = LocalContentColor.current
    val cellsSizeModifier = Modifier.size(cellWidth, cellHeight)
    val isTalkBackEnabled by isTalkBackEnabled.collectAsState()
    val isHighTextContrastEnabled by isHighTextContrastEnabled.collectAsState()

    // Initialize the RuntimeShader with the AGSL source code.
    val shader = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val shader = RuntimeShader(SHADER_SRC)
            shader.setFloatUniform(
                "iResolution",
                with(density) { width.toPx() },
                with(density) { suggestedHeight.toPx() },
            )
            // State to store the Index of Refraction (IOR) value, defaulting to 1.33 for water/glass.
            val iorValue = 1.33f
            val highlightStrengthValue = 1f
            // State to store the normalized width of the bevel highlight band.
            val bevelWidthValue = 0.04f
            // State to store the perceived thickness of the glass for distortion scaling.
            val thicknessValue = 0.1f
            // State to store the intensity of the cast shadow.
            val shadowIntensityValue = 0.1f
            // State to store the chromatic aberration strength.
            val chromaticAberrationStrength = 0.002f
            // State to store the frosted glass blur radius.
            val frostedGlassBlurRadius = 0.001f
            shader.setFloatUniform(
                "ior", iorValue
            ) // Pass the current IOR value to the shader.
            shader.setFloatUniform(
                "highlightStrength", highlightStrengthValue
            ) // Pass the highlight strength.
            shader.setFloatUniform(
                "bevelWidth", bevelWidthValue
            ) // Pass the bevel width.
            shader.setFloatUniform(
                "thickness", thicknessValue
            ) // Pass the thickness.
            shader.setFloatUniform(
                "shadowIntensity", shadowIntensityValue
            ) // Pass the shadow intensity.
            shader.setFloatUniform(
                "chromaticAberrationStrength", chromaticAberrationStrength
            ) // Pass the chromatic aberration strength.
            shader.setFloatUniform(
                "frostedGlassBlurRadius", frostedGlassBlurRadius
            ) // Pass the frosted glass blur radius.

            shader
        } else null
    }

    return { page, monthStartDate, monthStartJdn, deviceEvents, onlyWeek, isHighlighted,
             selectedDay ->
        val previousMonthLength =
            if (onlyWeek == null) null else (monthStartJdn - 1).on(mainCalendar).dayOfMonth

        val startingWeekDay = applyWeekStartOffsetToWeekDay(monthStartJdn.weekDay)
        val monthLength = mainCalendar.getMonthLength(monthStartDate.year, monthStartDate.month)
        val startOfYearJdn = Jdn(mainCalendar, monthStartDate.year, 1, 1)
        val monthStartWeekOfYear = monthStartJdn.getWeekOfYear(startOfYearJdn)
        val daysRowsCount = ceil((monthLength + startingWeekDay) / 7f).toInt()

        Box(
            modifier
                .height(
                    if (onlyWeek != null) suggestedHeight + 8.dp
                    else (cellHeight * (daysRowsCount + 1) + 12.dp)
                )
                .semantics { this.isTraversalGroup = true }
        ) {
            val highlightedDayOfMonth = selectedDay - monthStartJdn
            val indicatorCenter = if (isHighlighted && highlightedDayOfMonth in 0..<monthLength) {
                val cellIndex = selectedDay - monthStartJdn + startingWeekDay
                Offset(
                    x = cellWidthPx * (cellIndex % 7).let {
                        .5f + if (isRtl) 6 - it else it
                    } + pagerArrowSizeAndPaddingPx,
                    // +1 for weekday names initials row, .5f for center of the circle
                    y = cellHeightPx * (1.5f + if (onlyWeek == null) cellIndex / 7 else 0),
                )
            } else null

            val animatedCenter = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
            val animatedRadius = remember { Animatable(if (indicatorCenter == null) 0f else 1f) }

            // Handles circle radius change animation, initial selection reveal and hide
            LaunchedEffect(key1 = indicatorCenter != null) {
                if (indicatorCenter != null) animatedCenter.snapTo(indicatorCenter)
                val target = if (indicatorCenter != null) 1f else 0f
                if (animatedRadius.value != target || animatedRadius.isRunning) animatedRadius.animateTo(
                    targetValue = target,
                    animationSpec = spring(Spring.DampingRatioLowBouncy, Spring.StiffnessLow),
                )
            }

            // Handles circle moves animation, change of the selected day
            LaunchedEffect(key1 = indicatorCenter) {
                if (indicatorCenter != null) animatedCenter.animateTo(
                    targetValue = indicatorCenter,
                    animationSpec = spring(Spring.DampingRatioLowBouncy, Spring.StiffnessLow),
                )
            }

            val arrowOffsetY =
                (cellHeight + (if (language.isArabicScript) 4 else 0).dp - MaterialIconDimension.dp) / 2
            PagerArrow(arrowOffsetY, coroutineScope, pagerState, page, width, true, onlyWeek)

            repeat(7) { column ->
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = cellsSizeModifier.offset(
                        x = pagerArrowSizeAndPadding.dp + cellWidth * column
                    )
                ) {
                    val weekDayPosition = revertWeekStartOffsetFromWeekDay(column)
                    val description = stringResource(
                        R.string.week_days_name_column, getWeekDayName(weekDayPosition)
                    )
                    Text(
                        getInitialOfWeekDay(weekDayPosition),
                        fontSize = with(density) { (diameter * .5f).toSp() },
                        modifier = Modifier
                            .alpha(AppBlendAlpha)
                            .semantics { this.contentDescription = description },
                    )
                }
            }

            val holidaysPositions = remember { mutableIntSetOf() }
            Box(
                Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        if (shader != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            val normalizedCenterX = animatedCenter.value.x / width.toPx()
                            val normalizedCenterY = animatedCenter.value.y / suggestedHeight.toPx()
                            shader.setFloatUniform("center", normalizedCenterX, normalizedCenterY)
                            shader.setFloatUniform("radius", animatedRadius.value * .06f)
                            renderEffect = RenderEffect.createRuntimeShaderEffect(shader, "content")
                                .asComposeRenderEffect()
                        }
                    },
            ) {
                // Invalidate the indicator state on table size changes
                if (shader == null) key(width, suggestedHeight) {
                    Canvas(Modifier.fillMaxSize()) {
                        val radiusFraction = animatedRadius.value
                        if (radiusFraction > 0f) drawCircle(
                            color = monthColors.indicator,
                            center = animatedCenter.value,
                            radius = cellRadius * radiusFraction,
                        )
                    }
                }

                repeat(daysRowsCount * 7) { dayOffset ->
                    if (onlyWeek != null && monthStartWeekOfYear + dayOffset / 7 != onlyWeek) return@repeat
                    val row = if (onlyWeek == null) dayOffset / 7 else 0
                    val day = monthStartJdn + dayOffset - startingWeekDay
                    val isToday = day == today
                    val isBeforeMonth = dayOffset < startingWeekDay
                    val isAfterMonth = dayOffset + 1 > startingWeekDay + monthLength
                    val column = dayOffset % 7
                    Box(
                        Modifier
                            .offset(y = cellHeight * (row + 1))
                            .semantics { this.isTraversalGroup = true }) {
                        if (column == 0) AnimatedVisibility(
                            isShowWeekOfYearEnabled,
                            modifier = Modifier
                                .offset(x = (16 - 4).dp)
                                .size((24 + 8).dp, cellHeight),
                            label = "week number",
                        ) {
                            val weekNumber = onlyWeek ?: (monthStartWeekOfYear + row)
                            Box(
                                Modifier
                                    .fillMaxSize()
                                    .then(
                                        if (onWeekClick != null) Modifier.clickable(
                                            onClickLabel = stringResource(R.string.week_view),
                                            indication = ripple(bounded = false),
                                            interactionSource = null,
                                        ) {
                                            onWeekClick(
                                                when {
                                                    selectedDay - day in 0..<7 -> selectedDay
                                                    onlyWeek != null -> day
                                                    row == 0 -> monthStartJdn
                                                    // Select first non weekend day of the week
                                                    else -> day + ((0..6).firstOrNull { !(day + it).isWeekEnd }
                                                        ?: 0)
                                                },
                                                true,
                                            )
                                        } else Modifier,
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                val formattedWeekNumber = formatNumber(weekNumber)
                                val description =
                                    stringResource(R.string.nth_week_of_year, formattedWeekNumber)
                                Text(
                                    formattedWeekNumber,
                                    fontSize = with(density) { (daysTextSize * .625f).toSp() },
                                    modifier = Modifier
                                        .alpha(AppBlendAlpha)
                                        .semantics { this.contentDescription = description },
                                )
                            }
                        }
                        if (previousMonthLength != null || (!isBeforeMonth && !isAfterMonth)) Box(
                            contentAlignment = Alignment.Center,
                            modifier = cellsSizeModifier
                                .offset(x = pagerArrowSizeAndPadding.dp + cellWidth * column)
                                .combinedClickable(
                                    indication = null,
                                    interactionSource = null,
                                    onClick = { setSelectedDay(day) },
                                    onClickLabel = stringResource(R.string.select_day),
                                    onLongClickLabel = stringResource(R.string.add_event),
                                    onLongClick = {
                                        setSelectedDay(day)
                                        addEvent(AddEventData.fromJdn(day))
                                    },
                                )
                                .then(if (isBeforeMonth || isAfterMonth) Modifier.alpha(.5f) else Modifier),
                        ) {
                            val isSelected = isHighlighted && selectedDay == day
                            val events =
                                eventsRepository?.getEvents(day, deviceEvents) ?: emptyList()
                            val isHoliday = events.any { it.isHoliday } || day.isWeekEnd
                            if (isHoliday) holidaysPositions.add(
                                TablePositionPair(
                                    column, row
                                ).value
                            )
                            Canvas(cellsSizeModifier) {
                                val hasEvents =
                                    events.any { it !is CalendarEvent.DeviceCalendarEvent }
                                val hasAppointments =
                                    events.any { it is CalendarEvent.DeviceCalendarEvent }
                                val shiftWorkTitle = getShiftWorkTitle(day, true)
                                dayPainter.setDayOfMonthItem(
                                    isToday = false,
                                    isSelected = isSelected,
                                    hasEvent = hasEvents,
                                    hasAppointment = hasAppointments,
                                    isHoliday = isHoliday,
                                    jdn = day,
                                    dayOfMonth = "",
                                    header = shiftWorkTitle,
                                    secondaryCalendar = secondaryCalendar,
                                )
                                drawIntoCanvas { dayPainter.drawDay(it.nativeCanvas) }
                                if (isToday) drawCircle(
                                    monthColors.currentDay,
                                    radius = cellRadius,
                                    style = Stroke(width = (if (isHighTextContrastEnabled) 4 else 2).dp.toPx()),
                                )
                            }
                            Text(
                                text = formatNumber(
                                    if (previousMonthLength != null && isBeforeMonth) {
                                        previousMonthLength - (startingWeekDay - dayOffset) + 1
                                    } else if (onlyWeek != null && isAfterMonth) {
                                        dayOffset + 1 - monthLength - startingWeekDay
                                    } else dayOffset + 1 - startingWeekDay,
                                    mainCalendarDigits,
                                ),
                                color = when {
                                    isHoliday -> monthColors.holidays
                                    isSelected -> monthColors.textDaySelected
                                    else -> contentColor
                                },
                                style = daysStyle,
                                modifier = Modifier
                                    .padding(top = cellHeight / 15)
                                    .semantics {
                                        if (isTalkBackEnabled) this.contentDescription =
                                            getA11yDaySummary(
                                                resources = resources,
                                                jdn = day,
                                                isToday = isToday,
                                                deviceCalendarEvents = EventsStore.empty(),
                                                withZodiac = isToday,
                                                withOtherCalendars = false,
                                                withTitle = true,
                                                withWeekOfYear = false,
                                            )
                                    },
                            )
                        }
                    }
                }

                Canvas(
                    Modifier
                        .fillMaxSize()
                        .zIndex(-1f)
                ) {
                    holidaysPositions.forEach {
                        val (column, row) = TablePositionPair(it)
                        val center = Offset(
                            x = (.5f + if (isRtl) 6 - column else column) * cellWidthPx + pagerArrowSizeAndPaddingPx,
                            // +1 for weekday names initials row, .5f for center of the circle
                            y = cellHeightPx * (1.5f + if (onlyWeek == null) row else 0),
                        )
                        drawCircle(monthColors.holidaysCircle, center = center, radius = cellRadius)
                    }
                }
            }

            PagerArrow(arrowOffsetY, coroutineScope, pagerState, page, width, false, onlyWeek)
        }
    }
}

@JvmInline
private value class TablePositionPair(val value: Int) {
    constructor(x: Int, y: Int) : this(x * 16 + y)

    operator fun component1() = value / 16
    operator fun component2() = value % 16
}

private const val pagerArrowSize = MaterialIconDimension + 8 * 2
const val pagerArrowSizeAndPadding = pagerArrowSize + 4

@Composable
private fun PagerArrow(
    arrowOffsetY: Dp,
    coroutineScope: CoroutineScope,
    pagerState: PagerState,
    page: Int,
    screenWidth: Dp,
    isPrevious: Boolean,
    week: Int?,
) {
    val stringId = if (isPrevious) R.string.previous_x else R.string.next_x
    Icon(
        if (isPrevious) Icons.AutoMirrored.Default.KeyboardArrowLeft
        else Icons.AutoMirrored.Default.KeyboardArrowRight,
        contentDescription = if (week == null) {
            stringResource(stringId, stringResource(R.string.month))
        } else stringResource(R.string.nth_week_of_year, week + if (isPrevious) -1 else 1),
        modifier = Modifier
            .offset(
                x = if (isPrevious) 16.dp else (screenWidth - pagerArrowSize.dp),
                y = arrowOffsetY,
            )
            .then(
                if (week == null) Modifier.combinedClickable(
                    indication = ripple(bounded = false),
                    interactionSource = null,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(page + 1 * if (isPrevious) -1 else 1)
                        }
                    },
                    onClickLabel = stringResource(R.string.select_month),
                    onLongClick = {
                        coroutineScope.launch {
                            pagerState.scrollToPage(page + 12 * if (isPrevious) -1 else 1)
                        }
                    },
                    onLongClickLabel = stringResource(stringId, stringResource(R.string.year)),
                ) else Modifier.clickable(
                    indication = ripple(bounded = false),
                    interactionSource = null,
                ) {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(page + 1 * if (isPrevious) -1 else 1)
                    }
                },
            )
            .alpha(.9f),
    )
}

@org.intellij.lang.annotations.Language("AGSL")
const val SHADER_SRC = """
uniform float2 iResolution;   // layer size in px
uniform shader content;       // original composable content
uniform float  radius;        // normalized (0..1), e.g., 0.3
uniform float2 center;        // normalized (0..1), e.g., (0.5, 0.5)
uniform float  ior;           // Index of Refraction, e.g., 1.33 for water/glass
uniform float  highlightStrength; // Intensity of the specular highlight
uniform float  bevelWidth;    // Normalized width of the bevel highlight band
uniform float  thickness;     // Perceived thickness of the glass for distortion scaling
uniform float  shadowIntensity; // Intensity of the cast shadow
uniform float  chromaticAberrationStrength; // Strength of chromatic aberration effect
uniform float  frostedGlassBlurRadius; // Radius for the frosted glass blur effect

const float PI = 3.14159265359;
// Light direction for casting the shadow (from top-right-front)
const vec3 LIGHT_DIR_SHADOW = normalize(vec3(0.5, 0.5, 1.0));
// Light direction for the specular highlight (from top-right-front)
const vec3 LIGHT_DIR_HIGHLIGHT = normalize(vec3(0.5, 0.5, 1.0));
// Viewer direction (looking straight down onto the surface)
const vec3 VIEW_DIR = vec3(0.0, 0.0, -1.0);

// Helper function for blurring content samples (used for frosted glass)
half4 getBlurredColor(float2 coord, float blur_radius_px) {
    if (blur_radius_px <= 0.0) {
        return content.eval(coord); // No blur if radius is zero or less
    }

    // Number of samples per dimension for the box blur (e.g., 2 means a 5x5 grid)
    const int num_samples_per_dim = 12; // Changed to const int
    float step_size = blur_radius_px / float(num_samples_per_dim); // Distance between samples

    half4 sum_color = half4(0.0);
    float total_weight = 0.0;

    // Loop through a grid of samples around the given coordinate
    for (int i = -num_samples_per_dim; i <= num_samples_per_dim; i++) {
        for (int j = -num_samples_per_dim; j <= num_samples_per_dim; j++) {
            float2 offset = float2(float(i), float(j)) * step_size;
            sum_color += content.eval(coord + offset); // Sample content at offset
            total_weight += 1.0;
        }
    }
    return sum_color / total_weight; // Return averaged color
}

half4 main(float2 fragCoord) {
    // Convert fragment coordinate to normalized UV (0..1)
    float2 uv = fragCoord / iResolution;

    // Calculate position relative to the circle's center, and aspect-correct it
    // This ensures the circle appears round on non-square surfaces.
    float2 p = uv - center;
    p.x *= iResolution.x / iResolution.y;

    // Calculate the distance from the center of the circle (in aspect-corrected space)
    float d = length(p);

    // --- Shadow Calculation ---
    // Calculate the 2D light direction for the shadow, aspect-corrected
    float2 shadow_light_dir_2d = normalize(LIGHT_DIR_SHADOW.xy);
    shadow_light_dir_2d.x *= iResolution.x / iResolution.y;

    // Determine the shadow offset based on light direction and a fixed magnitude
    float shadow_offset_magnitude = 0.04;
    float2 shadowOffset_p_space = -shadow_light_dir_2d * shadow_offset_magnitude;

    // Calculate the distance from the center of the shadow circle
    float shadowDist = length(p - shadowOffset_p_space) - radius;

    // Smoothly apply the shadow based on its distance and intensity
    float shadowAmount = smoothstep(0.0, 0.15, shadowDist) * shadowIntensity; // 0.15 is softness
    half4 bgColor = content.eval(fragCoord); // Original background color
    half4 shadowColor = half4(0.0, 0.0, 0.0, bgColor.a); // Black shadow
    half4 backgroundWithShadow = mix(bgColor, shadowColor, shadowAmount);

    half4 finalColor;

    // --- Glass Effect Calculation (only inside the circle) ---
    if (d < radius) {
        float inner_radius = radius - bevelWidth;

        // Determine the 3D normal vector based on position within the circle:
        // Flat normal for the center, and a smoothly rounded normal for the bevel.
        vec3 normal_3d;
        if (d < inner_radius) {
            normal_3d = vec3(0.0, 0.0, 1.0); // Flat normal (pointing straight out of screen)
        } else {
            // Bevel region: smooth transition from flat to angled normal
            float t = (d - inner_radius) / bevelWidth;
            t = clamp(t, 0.0, 1.0); // 't' goes from 0 (inner bevel) to 1 (outer bevel)

            // Use trigonometric functions to create a smooth, quarter-circle profile for the normal
            float z_comp = cos(t * PI * 0.5); // Z component goes from 1 (flat) to 0 (horizontal)
            vec2 xy_comp = normalize(p) * sin(t * PI * 0.5); // XY component scales radially
            normal_3d = normalize(vec3(xy_comp, z_comp));
        }

        // Calculate the cosine of the angle of incidence between the view direction and the normal
        float cos_theta_i = dot(-VIEW_DIR, normal_3d);

        // Ensure the normal always points towards the view direction for correct refraction/reflection
        if (cos_theta_i < 0.0) {
            normal_3d = -normal_3d;
            cos_theta_i = dot(-VIEW_DIR, normal_3d);
        }

        // Refraction calculation using Snell's Law
        float eta = 1.0 / ior; // Ratio of refractive indices (air to glass)
        float k = 1.0 - eta * eta * (1.0 - cos_theta_i * cos_theta_i);
        vec3 refracted_ray_3d = (k < 0.0) ? vec3(0.0) : eta * VIEW_DIR + (eta * cos_theta_i - sqrt(k)) * normal_3d;
        refracted_ray_3d = normalize(refracted_ray_3d);

        // Reflection calculation using the built-in 'reflect' function
        vec3 reflected_ray_3d = reflect(VIEW_DIR, normal_3d);

        // Fresnel term (Schlick's approximation) to blend between refraction and reflection
        // Glass appears more reflective at grazing angles.
        float R0 = ((1.0 - ior) / (1.0 + ior));
        R0 *= R0; // Reflectance at normal incidence
        float fresnel_term = R0 + (1.0 - R0) * pow((1.0 - cos_theta_i), 5.0);

        // Calculate distorted coordinates for sampling the background content for refraction.
        // Distortion is scaled by 'thickness', 'iResolution.y', and 'normal_3d.z' for perspective.
        // 'distortion_factor' ensures distortion is primarily at the bevel.
        float distortion_factor = smoothstep(inner_radius, radius, d); // 0 in center, 1 at edge
        float2 distorted_fragCoord_refract = fragCoord + refracted_ray_3d.xy * thickness * iResolution.y / max(0.001, normal_3d.z) * distortion_factor;

        half4 refracted_color;
        // Apply chromatic aberration if strength is greater than 0
        if (chromaticAberrationStrength > 0.0) {
            float2 ab_offset_px = refracted_ray_3d.xy * chromaticAberrationStrength * iResolution.y; // Pixel offset based on ray direction

            // Sample content for R, G, B channels with slight offsets, applying blur if frosted glass is active
            half4 r_sample = getBlurredColor(distorted_fragCoord_refract + ab_offset_px, frostedGlassBlurRadius * iResolution.y);
            half4 g_sample = getBlurredColor(distorted_fragCoord_refract, frostedGlassBlurRadius * iResolution.y);
            half4 b_sample = getBlurredColor(distorted_fragCoord_refract - ab_offset_px, frostedGlassBlurRadius * iResolution.y);

            refracted_color.rgb = half3(r_sample.r, g_sample.g, b_sample.b);
            refracted_color.a = g_sample.a; // Maintain alpha from green channel
        } else {
            // If no chromatic aberration, just get the (potentially blurred) color
            refracted_color = getBlurredColor(distorted_fragCoord_refract, frostedGlassBlurRadius * iResolution.y);
        }

        // Calculate distorted coordinates for sampling the background content for reflection.
        float2 distorted_fragCoord_reflect = fragCoord + reflected_ray_3d.xy * thickness * iResolution.y / max(0.001, normal_3d.z) * distortion_factor;
        half4 reflected_color = content.eval(distorted_fragCoord_reflect); // Reflection is not blurred or aberrated

        // Simulate a specular highlight to give the illusion of a flat bevel/thickness.
        float shininess = 200.0; // Controls the sharpness of the highlight (higher for sharper bevel)
        // Calculate base specular intensity based on light reflection off the normal
        float specular_base = pow(max(0.0, dot(reflect(-LIGHT_DIR_HIGHLIGHT, normal_3d), -VIEW_DIR)), shininess);

        // Create a mask to concentrate the highlight at the edge (bevel effect).
        // This smoothstep makes the highlight primarily visible in a narrow band at the circle's edge.
        float bevel_mask = smoothstep(radius - bevelWidth, radius, d);
        float specular = specular_base * highlightStrength * bevel_mask;
        half3 highlight_color = half3(1.0, 1.0, 1.0) * specular; // White highlight

        // Combine refracted and reflected colors using the Fresnel term, then add the highlight.
        half3 glass_rgb = mix(refracted_color.rgb, reflected_color.rgb, fresnel_term);
        glass_rgb += highlight_color;

        // Final color inside the circle
        finalColor = half4(glass_rgb, refracted_color.a);

    } else {
        // Outside the circle, apply the background with the cast shadow
        finalColor = backgroundWithShadow;
    }

    // Smoothstep for anti-aliasing the circle's edge, blending the inside glass effect
    // with the outside background+shadow.
    float edgeSoftness = 0.01; // Small value for a sharp visual edge
    finalColor = mix(
        backgroundWithShadow, // Color outside the circle
        finalColor,           // Color inside the circle
        smoothstep(edgeSoftness, -edgeSoftness, d - radius) // 'd - radius' serves as the SDF value
    );

    return finalColor;
}
"""
