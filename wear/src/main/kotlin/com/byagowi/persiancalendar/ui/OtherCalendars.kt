package com.byagowi.persiancalendar.ui

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.CurvedDirection
import androidx.wear.compose.foundation.CurvedLayout
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.curvedText
import com.byagowi.persiancalendar.Jdn
import com.byagowi.persiancalendar.LocaleUtils

@Composable
fun BoxScope.OtherCalendars(
    localeUtils: LocaleUtils,
    day: Jdn,
    onTop: Boolean = false,
    withWeekDayName: Boolean = true,
    calendarIndex: Int = 0,
) {
    val weekDayName = localeUtils.weekDayName(day)

    val firstText = localeUtils.format(
        when (calendarIndex) {
            0 -> day.toCivilDate()
            1 -> day.toPersianDate()
            else -> day.toPersianDate()
        }
    )
    val secondText = localeUtils.format(
        when (calendarIndex) {
            0 -> day.toIslamicDate()
            1 -> day.toIslamicDate()
            else -> day.toCivilDate()
        }
    )
    val weekDayColor = MaterialTheme.colorScheme.primaryDim
    val othersColor = MaterialTheme.colorScheme.secondaryDim
    val isRound = LocalConfiguration.current.isScreenRound
    val nonCurvedStyle = MaterialTheme.typography.bodySmall
    if (isRound || onTop) {
        val curvedStyle = MaterialTheme.typography.arcSmall
            .copy(fontSize = with(LocalDensity.current) { 12.dp.toPx().toSp() })
        if (withWeekDayName) {
            if (isRound) {
                CurvedLayout(
                    anchor = 90f,
                    angularDirection = CurvedDirection.Angular.CounterClockwise,
                ) { curvedText(text = weekDayName, style = curvedStyle, color = weekDayColor) }
            } else Text(
                weekDayName,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(top = 20.dp),
                color = weekDayColor,
                style = nonCurvedStyle,
            )
        }
        CurvedLayout(
            anchor = if (onTop) 315f else 45f,
            angularDirection = if (onTop) CurvedDirection.Angular.Clockwise else
                CurvedDirection.Angular.CounterClockwise,
        ) { curvedText(text = firstText, style = curvedStyle, color = othersColor) }
        CurvedLayout(
            anchor = if (onTop) 225f else 135f,
            angularDirection = if (onTop) CurvedDirection.Angular.Clockwise else
                CurvedDirection.Angular.CounterClockwise,
        ) { curvedText(text = secondText, style = curvedStyle, color = othersColor) }
    } else {
        if (withWeekDayName) Text(
            weekDayName,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 20.dp),
            color = weekDayColor,
            style = nonCurvedStyle,
        )
        Column(
            Modifier.align(Alignment.BottomCenter),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(firstText, color = othersColor, style = nonCurvedStyle)
            Text(secondText, color = othersColor, style = nonCurvedStyle)
        }
    }
}
