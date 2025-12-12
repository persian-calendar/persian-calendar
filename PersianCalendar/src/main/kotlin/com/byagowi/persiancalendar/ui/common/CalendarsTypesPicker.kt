package com.byagowi.persiancalendar.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredHeight
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Calendar
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.ui.utils.AppBlendAlpha
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
    val outlineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = AppBlendAlpha)
    SingleChoiceSegmentedButtonRow(
        modifier = modifier
            .shadow(
                elevation = 8.dp,
                shape = MaterialTheme.shapes.extraLarge,
                ambientColor = LocalContentColor.current,
                spotColor = LocalContentColor.current,
            )
            .fillMaxWidth()
            .semantics { this.contentDescription = selectDateTypeString }
            .drawWithContent {
                drawContent()
                (1..<calendarsList.size).forEach {
                    val x = size.width / calendarsList.size * it
                    drawLine(
                        color = outlineColor,
                        strokeWidth = 1.dp.toPx(),
                        start = Offset(x, 0f),
                        end = Offset(x, size.height),
                    )
                }
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
                colors = SegmentedButtonDefaults.colors().copy(
                    inactiveContainerColor = inactiveButtonColor,
                ),
                shape = SegmentedButtonDefaults.itemShape(index, calendarsList.size),
                modifier = Modifier
                    .requiredHeight(40.dp)
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
