package com.byagowi.persiancalendar.ui.common

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Calendar
import com.byagowi.persiancalendar.global.isGradient
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.ui.theme.animateColor
import com.byagowi.persiancalendar.ui.utils.AppBlendAlpha
import com.byagowi.persiancalendar.ui.utils.performHapticFeedbackVirtualKey
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.min

@Composable
fun CalendarPicker(
    value: Calendar,
    items: List<Calendar>,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    betterToUseShortCalendarName: Boolean = false,
    onValueChange: (Calendar) -> Unit,
) {
    SegmentedButtonItemsPicker(
        value = value,
        items = items,
        backgroundColor = backgroundColor,
        height = calendarPickerHeight(),
        modifier = modifier,
        onValueChange = onValueChange,
    ) {
        stringResource(
            if (language.betterToUseShortCalendarName || betterToUseShortCalendarName) {
                it.shortTitle
            } else it.title,
        )
    }
}

@Composable
private fun <T> SegmentedButtonItemsPicker(
    value: T,
    onValueChange: (T) -> Unit,
    items: List<T>,
    backgroundColor: Color,
    height: Dp,
    modifier: Modifier = Modifier,
    content: @Composable (T) -> String,
) {
    BoxWithConstraints(modifier = modifier) {
        val isGradient = isGradient
        val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
        fun visualIndex(item: T) =
            if (isRtl) items.size - 1 - items.indexOf(item) else items.indexOf(item)

        val selectDateTypeString = stringResource(R.string.select_type_date)
        val view = LocalView.current
        val density = LocalDensity.current
        val capsuleShape = RoundedCornerShape(height / 2)
        val cornerRadius = CornerRadius(with(density) { height.toPx() / 2 })
        val cellColor by animateColor(MaterialTheme.colorScheme.primary.copy(alpha = .85f))
        val backgroundColor by animateColor(backgroundColor)
        val inactiveContentColor by animateColor(MaterialTheme.colorScheme.onSurface)
        val outlineColor by animateColor(MaterialTheme.colorScheme.outlineVariant)
        val coroutineScope = rememberCoroutineScope()
        val maxWidth = with(density) { this@BoxWithConstraints.maxWidth.toPx() }
        val cellWidth = maxWidth / items.size
        val currentVisualIndex = visualIndex(value)
        val cellLeft = remember { Animatable(cellWidth * currentVisualIndex) }
        val cellRight = remember { Animatable(cellWidth * (currentVisualIndex + 1)) }
        fun updateCellPosition(item: T) {
            val destinationVisualIndex = visualIndex(item)
            val isForward = visualIndex(item) > currentVisualIndex
            val first = spring<Float>(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = 150f,
            )
            val second = spring<Float>(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = 300f,
            )
            coroutineScope.launch {
                cellLeft.animateTo(
                    targetValue = cellWidth * destinationVisualIndex,
                    animationSpec = if (isForward) first else second,
                )
            }
            coroutineScope.launch {
                cellRight.animateTo(
                    targetValue = cellWidth * (destinationVisualIndex + 1),
                    animationSpec = if (isForward) second else first,
                )
            }
        }
        LaunchedEffect(items) { updateCellPosition(value) }
        SingleChoiceSegmentedButtonRow(
            space = 0.dp,
            modifier = Modifier
                .dropShadow(shape = capsuleShape) {
                    this.color = outlineColor
                    if (isGradient) {
                        this.offset = Offset(0f, with(this.density) { 4.dp.toPx() })
                        this.alpha = .325f
                        this.spread = with(this.density) { 8.dp.toPx() }
                        this.radius = with(this.density) { 8.dp.toPx() }
                    } else {
                        this.offset = Offset.Zero
                        this.alpha = AppBlendAlpha
                        this.spread = with(this.density) { 1.dp.toPx() }
                    }
                }
                .fillMaxWidth()
                .semantics { this.contentDescription = selectDateTypeString }
                .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
                .drawWithCache {
                    onDrawWithContent {
                        drawContent()
                        drawRoundRect(
                            color = cellColor,
                            topLeft = Offset(x = cellLeft.value.coerceAtLeast(0f), y = 0f),
                            size = this.size.copy(
                                width = cellRight.value.coerceAtMost(maxWidth) - cellLeft.value
                            ),
                            cornerRadius = cornerRadius,
                            blendMode = BlendMode.SrcOut,
                        )
                        (1..<items.size).forEach { i ->
                            val x = cellWidth * i
                            drawLine(
                                color = outlineColor.copy(
                                    alpha = min(
                                        abs(i - cellLeft.value / cellWidth),
                                        abs(i - cellRight.value / cellWidth),
                                    ).coerceAtMost(AppBlendAlpha),
                                ),
                                strokeWidth = 1.dp.toPx(),
                                start = Offset(x, 0f),
                                end = Offset(x, size.height),
                                blendMode = BlendMode.DstOver,
                            )
                        }
                        drawRoundRect(
                            color = backgroundColor,
                            cornerRadius = cornerRadius,
                            blendMode = BlendMode.DstOver,
                        )
                    }
                },
        ) {
            CompositionLocalProvider(
                LocalRippleConfiguration provides when {
                    cellLeft.isRunning || cellRight.isRunning -> null
                    else -> LocalRippleConfiguration.current
                },
            ) {
                items.forEachIndexed { index, item ->
                    SegmentedButton(
                        border = BorderStroke(0.dp, Color.Transparent),
                        selected = value == item,
                        onClick = {
                            onValueChange(item)
                            view.performHapticFeedbackVirtualKey()
                            updateCellPosition(item)
                        },
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        icon = {},
                        colors = SegmentedButtonDefaults.colors(
                            inactiveContainerColor = Color.Transparent,
                            inactiveContentColor = inactiveContentColor,
                            activeContainerColor = Color.Transparent,
                            activeContentColor = inactiveContentColor,
                        ),
                        shape = SegmentedButtonDefaults.itemShape(index, items.size),
                        modifier = Modifier
                            .requiredHeight(height)
                            .clip(capsuleShape)
                            .weight(1f),
                    ) {
                        Text(
                            text = content(item),
                            maxLines = 1,
                            softWrap = false,
                            autoSize = TextAutoSize.StepBased(
                                minFontSize = 6.sp,
                                maxFontSize = LocalTextStyle.current.fontSize,
                            ),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun calendarPickerHeight() =
    40.dp // LocalMinimumInteractiveComponentSize.current.coerceAtLeast(48.dp)
