package com.byagowi.persiancalendar.ui.calendar.times

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ui.theme.AppTheme
import com.byagowi.persiancalendar.ui.utils.AppBlendAlpha
import com.byagowi.persiancalendar.utils.getFromStringId
import com.byagowi.persiancalendar.utils.getTimeNames
import io.github.persiancalendar.praytimes.PrayTimes
import kotlinx.coroutines.flow.MutableStateFlow

class Times(context: Context, attrs: AttributeSet? = null) : FrameLayout(context, attrs) {
    val root = ComposeView(context)

    init {
        addView(root)
    }

    private var isExpanded = MutableStateFlow(false)
    fun toggle() {
        isExpanded.value = !isExpanded.value
    }

    fun update(prayTimes: PrayTimes) {
        @OptIn(ExperimentalLayoutApi::class)
        root.setContent {
            AppTheme {
                val isExpanded by isExpanded.collectAsState()
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalArrangement = Arrangement.SpaceEvenly,
                ) {
                    getTimeNames().forEach { timeId ->
                        AnimatedVisibility(
                            visible = isExpanded || when (timeId) {
                                R.string.fajr, R.string.dhuhr, R.string.maghrib -> true
                                else -> false
                            },
                            enter = fadeIn() + slideInVertically(),
                            exit = fadeOut() + slideOutVertically(),
                        ) {
                            AnimatedContent(
                                targetState = prayTimes.getFromStringId(timeId).toFormattedString(),
                                label = "time"
                            ) { state ->
                                Column(
                                    modifier = Modifier.defaultMinSize(
                                        minWidth = dimensionResource(R.dimen.time_item_size),
                                    ),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(stringResource(timeId))
                                    Text(state, modifier = Modifier.alpha(AppBlendAlpha))
                                    Spacer(Modifier.height(8.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
