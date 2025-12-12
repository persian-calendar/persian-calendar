package com.byagowi.persiancalendar.ui.common

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
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
import com.byagowi.persiancalendar.ui.utils.performHapticFeedbackVirtualKey

@Composable
fun CalendarsTypesPicker(
    value: Calendar,
    calendarsList: List<Calendar>,
    inactiveButtonColor: Color,
    modifier: Modifier = Modifier,
    onValueChange: (Calendar) -> Unit,
) {
    val selectDateTypeString = stringResource(R.string.select_type_date)
    val language by language.collectAsState()
    val view = LocalView.current
    val height = 40.dp
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val selectedIndex = calendarsList.indexOf(value)
    val animatedIndex by animateFloatAsState(
        targetValue = 0f + if (isRtl) calendarsList.size - 1 - selectedIndex else selectedIndex,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow,
        ),
    )
    val capsuleShape = RoundedCornerShape(height / 2)
    val cellColor by animateColor(MaterialTheme.colorScheme.primary.copy(alpha = .85f))
    val inactiveButtonColor by animateColor(inactiveButtonColor)
    val activeContentColor by animateColor(MaterialTheme.colorScheme.onPrimary)
    val inactiveContentColor by animateColor(MaterialTheme.colorScheme.onSurface)
    SingleChoiceSegmentedButtonRow(
        modifier = modifier
            .shadow(
                elevation = 8.dp,
                shape = capsuleShape,
                ambientColor = LocalContentColor.current,
                spotColor = LocalContentColor.current,
            )
            .fillMaxWidth()
            .semantics { this.contentDescription = selectDateTypeString }
            .drawBehind {
                val cornerRadius = CornerRadius(height.toPx() / 2)
                drawRoundRect(inactiveButtonColor, cornerRadius = cornerRadius)
                val cellWidth = this.size.width / calendarsList.size
                val cellSize = Size(width = cellWidth, height = this.size.height)
                val topLeft = Offset(x = animatedIndex * cellWidth, y = 0f)
                drawRoundRect(
                    cellColor,
                    topLeft = topLeft,
                    size = cellSize,
                    cornerRadius = cornerRadius,
                )
            },
    ) {
        calendarsList.forEachIndexed { index, calendar ->
            val title = stringResource(
                if (language.betterToUseShortCalendarName) calendar.shortTitle else calendar.title
            )
            SegmentedButton(
                border = BorderStroke(0.dp, Color.Transparent),
                selected = value == calendar,
                onClick = {
                    onValueChange(calendar)
                    view.performHapticFeedbackVirtualKey()
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
