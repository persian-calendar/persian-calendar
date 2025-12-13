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
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Calendar
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.ui.theme.animateColor
import com.byagowi.persiancalendar.ui.utils.AppBlendAlpha
import com.byagowi.persiancalendar.ui.utils.performHapticFeedbackVirtualKey
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.min

@Composable
fun CalendarsTypesPicker(
    value: Calendar,
    calendarsList: List<Calendar>,
    inactiveButtonColor: Color,
    modifier: Modifier = Modifier,
    betterToUseShortCalendarName: Boolean = false,
    onValueChange: (Calendar) -> Unit,
) = BoxWithConstraints(modifier = modifier) {
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    fun visualIndex(item: Calendar): Int {
        val selectedIndex = calendarsList.indexOf(item)
        return if (isRtl) calendarsList.size - 1 - selectedIndex else selectedIndex
    }

    val selectDateTypeString = stringResource(R.string.select_type_date)
    val language by language.collectAsState()
    val view = LocalView.current
    val height = calendarTypesHeight()
    val density = LocalDensity.current
    val capsuleShape = RoundedCornerShape(height / 2)
    val cornerRadius = CornerRadius(with(density) { height.toPx() / 2 })
    val maxWidth = this.maxWidth
    val cellColor by animateColor(MaterialTheme.colorScheme.primary.copy(alpha = .85f))
    val inactiveButtonColor by animateColor(inactiveButtonColor)
    val activeContentColor by animateColor(MaterialTheme.colorScheme.onPrimary)
    val inactiveContentColor by animateColor(MaterialTheme.colorScheme.onSurface)
    val outlineColor by animateColor(MaterialTheme.colorScheme.outlineVariant)
    val coroutineScope = rememberCoroutineScope()
    val cellWidth = with(density) { (maxWidth / calendarsList.size).toPx() }
    val currentVisualIndex = visualIndex(value)
    val cellLeft = remember { Animatable(cellWidth * currentVisualIndex) }
    val cellRight = remember { Animatable(cellWidth * (currentVisualIndex + 1)) }
    SingleChoiceSegmentedButtonRow(
        modifier = Modifier
            .shadow(
                elevation = 8.dp,
                shape = capsuleShape,
                ambientColor = LocalContentColor.current,
                spotColor = LocalContentColor.current,
            )
            .fillMaxWidth()
            .semantics { this.contentDescription = selectDateTypeString }
            .drawBehind {
                drawRoundRect(inactiveButtonColor, cornerRadius = cornerRadius)
                (1..<calendarsList.size).forEach { i ->
                    val x = cellWidth * i
                    drawLine(
                        color = outlineColor.copy(
                            alpha = min(
                                abs(i - cellLeft.value / cellWidth),
                                abs(i - cellRight.value / cellWidth),
                            ).coerceAtMost(AppBlendAlpha)
                        ),
                        strokeWidth = 1.dp.toPx(),
                        start = Offset(x, 0f),
                        end = Offset(x, size.height),
                    )
                }
                drawRoundRect(
                    cellColor,
                    topLeft = Offset(x = cellLeft.value, y = 0f),
                    size = Size(cellRight.value - cellLeft.value, this.size.height),
                    cornerRadius = cornerRadius,
                )
            },
    ) {
        calendarsList.forEachIndexed { index, item ->
            val title = stringResource(
                if (language.betterToUseShortCalendarName || betterToUseShortCalendarName) {
                    item.shortTitle
                } else item.title
            )
            SegmentedButton(
                border = BorderStroke(0.dp, Color.Transparent),
                selected = value == item,
                onClick = {
                    onValueChange(item)
                    view.performHapticFeedbackVirtualKey()
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
                },
                contentPadding = PaddingValues(
                    start = 12.dp,
                    top = 0.dp,
                    end = 12.dp,
                    bottom = 0.dp,
                ),
                icon = {},
                colors = SegmentedButtonDefaults.colors(
                    inactiveContainerColor = Color.Transparent,
                    inactiveContentColor = inactiveContentColor,
                    activeContainerColor = Color.Transparent,
                    activeContentColor = activeContentColor,
                ),
                shape = SegmentedButtonDefaults.itemShape(index, calendarsList.size),
                modifier = Modifier
                    .requiredHeight(height)
                    .clip(capsuleShape)
                    .weight(1f),
            ) {
                Text(
                    title,
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

@Composable
fun calendarTypesHeight() = 40.dp//LocalMinimumInteractiveComponentSize.current.coerceAtLeast(48.dp)
