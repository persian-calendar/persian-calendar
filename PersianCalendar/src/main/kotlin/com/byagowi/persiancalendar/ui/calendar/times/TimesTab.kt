package com.byagowi.persiancalendar.ui.calendar.times

import androidx.compose.animation.Crossfade
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    selectedDay: Jdn,
    navigateToSettingsLocationTab: () -> Unit,
    navigateToSettingsLocationTabSetAthanAlarm: () -> Unit,
    navigateToAstronomy: (Jdn) -> Unit,
    removeThirdTab: () -> Unit,
    interactionSource: MutableInteractionSource,
    minHeight: Dp,
    bottomPadding: Dp,
    now: Long,
    today: Jdn,
) {
    val context = LocalContext.current
    val coordinates = coordinates ?: return Column(Modifier.fillMaxWidth()) {
        EncourageActionLayout(
            modifier = Modifier.padding(top = 24.dp),
            header = stringResource(R.string.ask_user_to_set_location),
            discardAction = {
                context.preferences.edit { putBoolean(PREF_DISMISSED_OWGHAT, true) }
                removeThirdTab()
            },
            acceptAction = navigateToSettingsLocationTab,
            hideOnAccept = false,
        )
        Spacer(Modifier.height(bottomPadding))
    }
    var isExpanded by remember {
        mutableStateOf(context.preferences.getBoolean(EXPANDED_TIME_STATE_KEY, false))
    }
    LaunchedEffect(isExpanded) {
        context.preferences.edit { putBoolean(EXPANDED_TIME_STATE_KEY, isExpanded) }
    }

    val prayTimes = coordinates.calculatePrayTimes(selectedDay.toGregorianCalendar())

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
        val isToday = selectedDay == today
        AstronomicalOverview(selectedDay, prayTimes, now, isToday, navigateToAstronomy)
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
    selectedDay: Jdn,
    prayTimes: PrayTimes,
    now: Long,
    isToday: Boolean,
    navigateToAstronomy: (Jdn) -> Unit,
) {
    Crossfade(
        targetState = isToday,
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
    ) { state ->
        val sunViewColors = appSunViewColors()
        val typeface = resolveAndroidCustomTypeface()
        val resources = LocalResources.current
        val density = LocalDensity.current
        if (state) BoxWithConstraints {
            val width = this.maxWidth
            val height = this.maxHeight
            val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
            val sunView = remember(
                resources, prayTimes, sunViewColors, density, now, typeface, isRtl,
            ) {
                SunView(
                    resources = resources,
                    prayTimes = prayTimes,
                    colors = sunViewColors,
                    width = with(density) { width.roundToPx() },
                    height = with(density) { height.roundToPx() },
                    timeInMillis = now,
                    typeface = typeface,
                    isRtl = isRtl,
                )
            }
            val fraction = remember { Animatable(0f) }
            LaunchedEffect(Unit) {
                fraction.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessVeryLow,
                    ),
                )
            }
            Canvas(Modifier.fillMaxSize()) {
                sunView.draw(this.drawContext.canvas.nativeCanvas, fraction.value)
            }
        } else Box(Modifier.fillMaxSize()) {
            MoonView(
                jdn = selectedDay,
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
                    ) { navigateToAstronomy(selectedDay) },
            )
        }
    }
}
