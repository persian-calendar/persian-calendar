package com.byagowi.persiancalendar.ui.common

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.CalendarType
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.enabledCalendars
import com.byagowi.persiancalendar.ui.theme.AppTheme
import com.byagowi.persiancalendar.ui.utils.copyToClipboard
import com.byagowi.persiancalendar.utils.formatDate
import com.byagowi.persiancalendar.utils.formatNumber
import com.byagowi.persiancalendar.utils.monthName
import com.byagowi.persiancalendar.utils.toLinearDate

class CalendarsFlow(context: Context, attrs: AttributeSet? = null) : FrameLayout(context, attrs) {
    private val root = ComposeView(context)

    init {
        addView(root)
    }

    fun update(calendarsToShow: List<CalendarType>, jdn: Jdn) {
        @OptIn(ExperimentalLayoutApi::class) root.setContent {
            AppTheme {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalArrangement = Arrangement.SpaceEvenly,
                ) {
                    val animationTime = integerResource(android.R.integer.config_mediumAnimTime)
                    enabledCalendars.forEach { calendarType ->
                        AnimatedVisibility(
                            visible = calendarType in calendarsToShow,
                            enter = fadeIn() + slideInVertically() + expandHorizontally(),
                            exit = fadeOut() + slideOutVertically() + shrinkHorizontally(),
                        ) {
                            AnimatedContent(
                                targetState = jdn,
                                label = "jdn",
                                transitionSpec = {
                                    fadeIn(animationSpec = tween(animationTime))
                                        .togetherWith(fadeOut(animationSpec = tween(animationTime)))
                                },
                            ) { state ->
                                val date = state.toCalendar(calendarType)
                                Column(
                                    modifier = Modifier.defaultMinSize(
                                        minWidth = dimensionResource(R.dimen.calendar_item_size),
                                    ), horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .clickable { context.copyToClipboard(formatDate(date)) }
                                            .semantics {
                                                this.contentDescription = formatDate(date)
                                            },
                                    ) {
                                        Text(
                                            formatNumber(date.dayOfMonth),
                                            style = MaterialTheme.typography.displayMedium,
                                        )
                                        Text(date.monthName)
                                    }
                                    val linear = date.toLinearDate()
                                    Text(
                                        linear,
                                        modifier = Modifier
                                            .clickable { context.copyToClipboard(linear) },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
