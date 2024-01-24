package com.byagowi.persiancalendar.ui.calendar.times

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.edit
import com.byagowi.persiancalendar.PREF_ATHAN_ALARM
import com.byagowi.persiancalendar.PREF_DISABLE_OWGHAT
import com.byagowi.persiancalendar.PREF_NOTIFICATION_ATHAN
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.cityName
import com.byagowi.persiancalendar.global.coordinates
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.ui.calendar.CalendarViewModel
import com.byagowi.persiancalendar.ui.calendar.EncourageActionLayout
import com.byagowi.persiancalendar.ui.common.ExpandArrow
import com.byagowi.persiancalendar.ui.common.MoonView
import com.byagowi.persiancalendar.ui.theme.appSunViewColors
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.calculatePrayTimes
import io.github.persiancalendar.praytimes.PrayTimes

@Composable
fun TimesTab(
    navigateToSettingsLocationTab: () -> Unit,
    navigateToSettingsLocationTabSetAthanAlarm: () -> Unit,
    navigateToAstronomy: (Int) -> Unit,
    viewModel: CalendarViewModel
) {
    val context = LocalContext.current
    val cityName by cityName.collectAsState()
    val coordinates = coordinates.collectAsState().value ?: return EncourageActionLayout(
        modifier = Modifier.padding(top = 24.dp),
        header = stringResource(R.string.ask_user_to_set_location),
        discardAction = {
            context.appPrefs.edit { putBoolean(PREF_DISABLE_OWGHAT, true) }
            viewModel.removeThirdTab()
        },
        acceptAction = navigateToSettingsLocationTab,
    )
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    val jdn by viewModel.selectedDay.collectAsState()
    val prayTimes = coordinates.calculatePrayTimes(jdn.toGregorianCalendar())

    Column {
        Column(
            Modifier.clickable(
                indication = rememberRipple(bounded = false),
                interactionSource = remember { MutableInteractionSource() },
                onClickLabel = stringResource(R.string.more),
                onClick = { isExpanded = !isExpanded },
            ),
        ) {
            Spacer(Modifier.height(16.dp))
            AstronomicalOverview(viewModel, prayTimes, navigateToAstronomy)
            Spacer(Modifier.height(16.dp))
            Times(isExpanded, prayTimes)
            Spacer(Modifier.height(8.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (cityName != null) Text(
                    cityName ?: "", style = MaterialTheme.typography.bodyLarge
                )
                ExpandArrow(isExpanded = isExpanded, tint = MaterialTheme.colorScheme.primary)
            }
        }

        val language by language.collectAsState()
        if ((language.isPersian || language.isDari) && PREF_ATHAN_ALARM !in context.appPrefs && PREF_NOTIFICATION_ATHAN !in context.appPrefs) {
            EncourageActionLayout(
                modifier = Modifier.padding(top = 16.dp),
                header = "مایلید برنامه اذان پخش کند؟",
                discardAction = { context.appPrefs.edit { putString(PREF_ATHAN_ALARM, "") } },
                acceptAction = navigateToSettingsLocationTabSetAthanAlarm,
            )
        }
    }
}

@Composable
private fun AstronomicalOverview(
    viewModel: CalendarViewModel,
    prayTimes: PrayTimes,
    navigateToAstronomy: (Int) -> Unit,
) {
    val today by viewModel.today.collectAsState()
    val jdn by viewModel.selectedDay.collectAsState()
    val sunViewNeedsAnimation by viewModel.sunViewNeedsAnimation.collectAsState()
    val now by viewModel.now.collectAsState()
    LaunchedEffect(Unit) { viewModel.astronomicalOverviewLaunched() }

    Crossfade(
        jdn == today,
        label = "heading",
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .semantics { @OptIn(ExperimentalComposeUiApi::class) this.invisibleToUser() },
    ) { state ->
        val sunViewColors = appSunViewColors()
        if (state) AndroidView(
            factory = ::SunView,
            update = {
                it.colors = sunViewColors
                it.prayTimes = prayTimes
                it.setTime(now)
                if (sunViewNeedsAnimation) {
                    it.startAnimate()
                    viewModel.clearNeedsAnimation()
                } else it.initiate()
            },
            modifier = Modifier.fillMaxHeight(),
        ) else Box(Modifier.fillMaxWidth()) {
            AndroidView(
                factory = ::MoonView,
                update = { if (jdn != today) it.jdn = jdn.value.toFloat() },
                modifier = Modifier
                    .size(70.dp)
                    .align(Alignment.Center)
                    .clickable(
                        indication = rememberRipple(bounded = false),
                        interactionSource = remember { MutableInteractionSource() },
                    ) { navigateToAstronomy(jdn - Jdn.today()) },
            )
        }
    }
}
