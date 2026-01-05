package com.byagowi.persiancalendar.ui.calendar.times

import androidx.compose.animation.Crossfade
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.edit
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import com.byagowi.persiancalendar.EXPANDED_TIME_STATE_KEY
import com.byagowi.persiancalendar.PREF_ATHAN_ALARM
import com.byagowi.persiancalendar.PREF_DISMISSED_OWGHAT
import com.byagowi.persiancalendar.PREF_NOTIFICATION_ATHAN
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.SHARED_CONTENT_KEY_MOON
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.cityName
import com.byagowi.persiancalendar.global.coordinates
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.ui.calendar.CalendarViewModel
import com.byagowi.persiancalendar.ui.calendar.EncourageActionLayout
import com.byagowi.persiancalendar.ui.common.ExpandArrow
import com.byagowi.persiancalendar.ui.common.MoonView
import com.byagowi.persiancalendar.ui.theme.appSunViewColors
import com.byagowi.persiancalendar.ui.theme.resolveAndroidCustomTypeface
import com.byagowi.persiancalendar.ui.utils.appBoundsTransform
import com.byagowi.persiancalendar.utils.calculatePrayTimes
import com.byagowi.persiancalendar.utils.preferences
import io.github.persiancalendar.praytimes.PrayTimes

@Composable
fun SharedTransitionScope.TimesTab(
    navigateToSettingsLocationTab: () -> Unit,
    navigateToSettingsLocationTabSetAthanAlarm: () -> Unit,
    navigateToAstronomy: (Jdn) -> Unit,
    viewModel: CalendarViewModel,
    interactionSource: MutableInteractionSource,
    minHeight: Dp,
    bottomPadding: Dp,
) {
    val context = LocalContext.current
    val coordinates = coordinates ?: return Column(Modifier.fillMaxWidth()) {
        EncourageActionLayout(
            modifier = Modifier.padding(top = 24.dp),
            header = stringResource(R.string.ask_user_to_set_location),
            discardAction = {
                context.preferences.edit { putBoolean(PREF_DISMISSED_OWGHAT, true) }
                viewModel.removeThirdTab()
            },
            acceptAction = navigateToSettingsLocationTab,
            hideOnAccept = false,
        )
        Spacer(Modifier.height(bottomPadding))
    }
    var isExpanded by rememberSaveable {
        mutableStateOf(context.preferences.getBoolean(EXPANDED_TIME_STATE_KEY, false))
    }
    DisposableEffect(Unit) {
        onDispose { context.preferences.edit { putBoolean(EXPANDED_TIME_STATE_KEY, isExpanded) } }
    }

    val prayTimes = coordinates.calculatePrayTimes(viewModel.selectedDay.toGregorianCalendar())
    val now = viewModel.now
    val today = viewModel.today

    Column(
        Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = minHeight)
            .clickable(
                indication = null,
                interactionSource = interactionSource,
                onClickLabel = stringResource(R.string.more),
                onClick = { isExpanded = !isExpanded },
            ),
    ) {
        Spacer(Modifier.height(16.dp))
        val isToday = viewModel.selectedDay == today
        AstronomicalOverview(viewModel, prayTimes, now, isToday, navigateToAstronomy)
        Spacer(Modifier.height(16.dp))
        Times(isExpanded, prayTimes, now, isToday)
        Spacer(Modifier.height(8.dp))
        Row(
            Modifier.align(Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (cityName != null) Text(
                text = cityName.orEmpty(),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.combinedClickable(
                    indication = null,
                    interactionSource = null,
                    onClickLabel = stringResource(R.string.more),
                    onClick = { isExpanded = !isExpanded },
                    onLongClickLabel = if (language.isPersianOrDari) "تنظیم مکان" else {
                        stringResource(R.string.location)
                    },
                    onLongClick = { navigateToSettingsLocationTab() },
                ),
            )
            ExpandArrow(
                modifier = Modifier.size(with(LocalDensity.current) { 20.sp.toDp() }),
                isExpanded = isExpanded,
                tint = MaterialTheme.colorScheme.primary,
            )
        }
        if (showEnableAthanForPersianUsers()) EncourageActionLayout(
            header = "مایلید برنامه اذان پخش کند؟",
            discardAction = { context.preferences.edit { putString(PREF_ATHAN_ALARM, "") } },
            acceptAction = navigateToSettingsLocationTabSetAthanAlarm,
        )
        Spacer(Modifier.height(bottomPadding))
    }
}

@Composable
private fun showEnableAthanForPersianUsers(): Boolean {
    // As the message is only translated in Persian
    if (!language.isPersianOrDari) return false
    val context = LocalContext.current
    return PREF_ATHAN_ALARM !in context.preferences && PREF_NOTIFICATION_ATHAN !in context.preferences
}

@Composable
private fun SharedTransitionScope.AstronomicalOverview(
    viewModel: CalendarViewModel,
    prayTimes: PrayTimes,
    now: Long,
    isToday: Boolean,
    navigateToAstronomy: (Jdn) -> Unit,
) {
    var needsAnimation by remember(isToday) { mutableStateOf(isToday) }

    Crossfade(
        targetState = isToday,
        label = "heading",
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
    ) { state ->
        val sunViewColors = appSunViewColors()
        val typeface = resolveAndroidCustomTypeface()
        if (state) AndroidView(
            factory = ::SunView,
            update = {
                it.setFont(typeface)
                it.colors = sunViewColors
                it.prayTimes = prayTimes
                it.setTime(now)
                if (needsAnimation) {
                    it.startAnimate()
                    needsAnimation = false
                } else it.initiate()
            },
            modifier = Modifier.fillMaxHeight(),
        ) else Box(Modifier.fillMaxSize()) {
            AndroidView(
                factory = ::MoonView,
                update = { if (!isToday) it.jdn = viewModel.selectedDay.value.toFloat() },
                modifier = Modifier
                    .size(70.dp)
                    .align(Alignment.Center)
                    .semantics { this.hideFromAccessibility() }
                    .sharedBounds(
                        rememberSharedContentState(key = SHARED_CONTENT_KEY_MOON),
                        animatedVisibilityScope = LocalNavAnimatedContentScope.current,
                        boundsTransform = appBoundsTransform,
                    )
                    .clickable(
                        indication = ripple(bounded = false),
                        interactionSource = null,
                    ) { navigateToAstronomy(viewModel.selectedDay) },
            )
        }
    }
}
