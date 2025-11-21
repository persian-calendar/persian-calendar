package com.byagowi.persiancalendar.ui

import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ModeNight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapVerticalCircle
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalWideNavigationRail
import androidx.compose.material3.Text
import androidx.compose.material3.WideNavigationRailDefaults
import androidx.compose.material3.WideNavigationRailItem
import androidx.compose.material3.WideNavigationRailItemDefaults
import androidx.compose.material3.WideNavigationRailState
import androidx.compose.material3.rememberWideNavigationRailState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import androidx.core.os.bundleOf
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.byagowi.persiancalendar.PREF_ATHAN_ALARM
import com.byagowi.persiancalendar.PREF_HOLIDAY_TYPES
import com.byagowi.persiancalendar.PREF_SYSTEM_DARK_THEME
import com.byagowi.persiancalendar.PREF_SYSTEM_LIGHT_THEME
import com.byagowi.persiancalendar.PREF_THEME
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.entities.Season
import com.byagowi.persiancalendar.global.coordinates
import com.byagowi.persiancalendar.global.spacedColon
import com.byagowi.persiancalendar.global.systemDarkTheme
import com.byagowi.persiancalendar.global.systemLightTheme
import com.byagowi.persiancalendar.global.userSetTheme
import com.byagowi.persiancalendar.ui.about.AboutScreen
import com.byagowi.persiancalendar.ui.about.DeviceInformationScreen
import com.byagowi.persiancalendar.ui.about.LicensesScreen
import com.byagowi.persiancalendar.ui.astronomy.AstronomyScreen
import com.byagowi.persiancalendar.ui.astronomy.AstronomyViewModel
import com.byagowi.persiancalendar.ui.calendar.CalendarScreen
import com.byagowi.persiancalendar.ui.calendar.CalendarViewModel
import com.byagowi.persiancalendar.ui.calendar.DaysScreen
import com.byagowi.persiancalendar.ui.calendar.ScheduleScreen
import com.byagowi.persiancalendar.ui.calendar.monthview.MonthScreen
import com.byagowi.persiancalendar.ui.common.ScrollShadow
import com.byagowi.persiancalendar.ui.compass.CompassScreen
import com.byagowi.persiancalendar.ui.converter.ConverterScreen
import com.byagowi.persiancalendar.ui.converter.ConverterViewModel
import com.byagowi.persiancalendar.ui.icons.AstrologyIcon
import com.byagowi.persiancalendar.ui.level.LevelScreen
import com.byagowi.persiancalendar.ui.map.MapScreen
import com.byagowi.persiancalendar.ui.map.MapViewModel
import com.byagowi.persiancalendar.ui.settings.INTERFACE_CALENDAR_TAB
import com.byagowi.persiancalendar.ui.settings.LOCATION_ATHAN_TAB
import com.byagowi.persiancalendar.ui.settings.SettingsScreen
import com.byagowi.persiancalendar.ui.theme.animateColor
import com.byagowi.persiancalendar.ui.theme.isDynamicGrayscale
import com.byagowi.persiancalendar.utils.preferences
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Date
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun App(intentStartDestination: String?, initialJdn: Jdn? = null, finish: () -> Unit) {
    val navController = rememberNavController()
    val railState = rememberWideNavigationRailState()
    AppNavigationRail(railState, navController, finish)
    SharedTransitionLayout {
        var appInitialJdn by remember { mutableStateOf(initialJdn) }
        val coroutineScope = rememberCoroutineScope()
        val openNavigationRail: () -> Unit = { coroutineScope.launch { railState.expand() } }
        NavHost(
            navController = navController,
            startDestination = Screen.fromName(intentStartDestination).name,
        ) {
            fun Screen.navigate() = navController.navigate(this.name)
            fun Screen.navigate(vararg pairs: Pair<String, Any?>) {
                val destination = navController.graph.findNode(this.name) ?: return
                navController.navigate(destination.id, bundleOf(*pairs))
            }

            fun isCurrentDestination(backStackEntry: NavBackStackEntry) =
                navController.currentDestination == backStackEntry.destination

            fun navigateUp(backStackEntry: NavBackStackEntry) {
                // If we aren't in the screen that this wasn't supposed to be called, just ignore, happens while transition
                if (!isCurrentDestination(backStackEntry)) return
                // if there wasn't anything to pop, just exit the app, happens if the app is entered from the map widget
                if (!navController.popBackStack()) finish()
            }

            val selectedDayKey = "SELECTED_DAY"
            val isWeekKey = "IS_WEEK"
            val tabKey = "TAB"
            val settingsKey = "SETTINGS"
            val daysOffsetKey = "DAYS_OFFSET"

            fun navigateToSettingsLocationTab() =
                Screen.SETTINGS.navigate(tabKey to LOCATION_ATHAN_TAB)

            fun navigateToAstronomy(jdn: Jdn) =
                Screen.ASTRONOMY.navigate(daysOffsetKey to jdn - Jdn.today())

            composable(Screen.CALENDAR.name) { backStackEntry ->
                val viewModel = viewModel<CalendarViewModel>()
                appInitialJdn?.let { viewModel.bringDay(it); appInitialJdn = null }
                CalendarScreen(
                    openNavigationRail = openNavigationRail,
                    navigateToHolidaysSettings = {
                        Screen.SETTINGS.navigate(
                            tabKey to INTERFACE_CALENDAR_TAB,
                            settingsKey to PREF_HOLIDAY_TYPES,
                        )
                    },
                    navigateToSettingsLocationTabSetAthanAlarm = {
                        Screen.SETTINGS.navigate(
                            tabKey to LOCATION_ATHAN_TAB,
                            settingsKey to PREF_ATHAN_ALARM,
                        )
                    },
                    navigateToSchedule = Screen.SCHEDULE::navigate,
                    navigateToDays = { jdn, isWeek ->
                        Screen.DAYS.navigate(
                            selectedDayKey to jdn.value,
                            isWeekKey to isWeek
                        )
                    },
                    navigateToMonthView = Screen.MONTH::navigate,
                    navigateToSettingsLocationTab = ::navigateToSettingsLocationTab,
                    navigateToAstronomy = ::navigateToAstronomy,
                    viewModel = viewModel,
                    animatedContentScope = this,
                    isCurrentDestination = isCurrentDestination(backStackEntry),
                )
            }

            composable(Screen.MONTH.name) { backStackEntry ->
                val previousEntry = navController.previousBackStackEntry
                val previousRoute = previousEntry?.destination?.route
                val viewModel = if (previousRoute == Screen.CALENDAR.name) {
                    viewModel<CalendarViewModel>(previousEntry)
                } else viewModel<CalendarViewModel>()

                val jdn =
                    backStackEntry.arguments?.getLong(selectedDayKey, 0)?.takeIf { it != 0L }
                        ?.let(::Jdn) ?: remember { viewModel.selectedDay.value }
                MonthScreen(
                    calendarViewModel = viewModel,
                    animatedContentScope = this,
                    navigateUp = { navigateUp(backStackEntry) },
                    initiallySelectedDay = jdn,
                )
            }

            composable(Screen.SCHEDULE.name) { backStackEntry ->
                val previousEntry = navController.previousBackStackEntry
                val previousRoute = previousEntry?.destination?.route
                val viewModel = if (previousRoute == Screen.CALENDAR.name) {
                    viewModel<CalendarViewModel>(previousEntry)
                } else viewModel<CalendarViewModel>()

                val jdn =
                    backStackEntry.arguments?.getLong(selectedDayKey, 0)?.takeIf { it != 0L }
                        ?.let(::Jdn) ?: remember { viewModel.selectedDay.value }
                ScheduleScreen(
                    calendarViewModel = viewModel,
                    animatedContentScope = this,
                    navigateUp = { navigateUp(backStackEntry) },
                    initiallySelectedDay = jdn,
                )
            }

            composable(Screen.DAYS.name) { backStackEntry ->
                val previousEntry = navController.previousBackStackEntry
                val previousRoute = previousEntry?.destination?.route
                val viewModel = if (previousRoute == Screen.CALENDAR.name) {
                    viewModel<CalendarViewModel>(previousEntry)
                } else viewModel<CalendarViewModel>()
                val arguments = backStackEntry.arguments
                val isWeek = arguments?.getBoolean(isWeekKey) ?: false
                val jdn = arguments?.getLong(selectedDayKey, 0)?.takeIf { it != 0L }?.let(::Jdn)
                    ?: Jdn.today()
                DaysScreen(
                    calendarViewModel = viewModel,
                    initiallySelectedDay = jdn,
                    appAnimatedContentScope = this,
                    isInitiallyWeek = isWeek,
                    navigateUp = { navigateUp(backStackEntry) },
                )
            }

            composable(Screen.CONVERTER.name) { backStackEntry ->
                ConverterScreen(
                    animatedContentScope = this,
                    openNavigationRail = openNavigationRail,
                    navigateToAstronomy = ::navigateToAstronomy,
                    viewModel = viewModel<ConverterViewModel>(),
                    noBackStackAction = if (navController.previousBackStackEntry != null) null
                    else ({ navigateUp(backStackEntry) }),
                )
            }

            composable(Screen.COMPASS.name) { backStackEntry ->
                CompassScreen(
                    animatedContentScope = this,
                    openNavigationRail = openNavigationRail,
                    navigateToLevel = Screen.LEVEL::navigate,
                    navigateToMap = Screen.MAP::navigate,
                    navigateToSettingsLocationTab = ::navigateToSettingsLocationTab,
                    noBackStackAction = if (navController.previousBackStackEntry != null) null
                    else ({ navigateUp(backStackEntry) }),
                )
            }

            composable(Screen.LEVEL.name) { backStackEntry ->
                LevelScreen(
                    animatedContentScope = this,
                    navigateUp = { navigateUp(backStackEntry) },
                    navigateToCompass = Screen.COMPASS::navigate,
                )
            }

            composable(Screen.ASTRONOMY.name) { backStackEntry ->
                val viewModel = viewModel<AstronomyViewModel>()
                backStackEntry.arguments?.getInt(daysOffsetKey, 0)?.takeIf { it != 0 }?.let {
                    viewModel.changeToTime((Jdn.today() + it).toGregorianCalendar().timeInMillis)
                }
                AstronomyScreen(
                    animatedContentScope = this,
                    openNavigationRail = openNavigationRail,
                    navigateToMap = Screen.MAP::navigate,
                    viewModel = viewModel,
                    noBackStackAction = if (navController.previousBackStackEntry != null) null
                    else ({ navigateUp(backStackEntry) }),
                )
            }

            composable(Screen.MAP.name) { backStackEntry ->
                val viewModel = viewModel<MapViewModel>()
                val previousEntry = navController.previousBackStackEntry
                val previousRoute = previousEntry?.destination?.route
                if (previousRoute == Screen.ASTRONOMY.name) {
                    val astronomyViewModel = viewModel<AstronomyViewModel>(previousEntry)
                    LaunchedEffect(Unit) {
                        viewModel.changeToTime(astronomyViewModel.astronomyState.value.date.time)
                        viewModel.state.collectLatest { astronomyViewModel.changeToTime(it.time) }
                    }
                }
                MapScreen(
                    animatedContentScope = this,
                    navigateUp = { navigateUp(backStackEntry) },
                    fromSettings = previousRoute == Screen.SETTINGS.name,
                    viewModel = viewModel,
                )
            }

            composable(Screen.SETTINGS.name) { backStackEntry ->
                SettingsScreen(
                    animatedContentScope = this,
                    openNavigationRail = openNavigationRail,
                    navigateToMap = Screen.MAP::navigate,
                    initialPage = backStackEntry.arguments?.getInt(tabKey, 0) ?: 0,
                    destination = backStackEntry.arguments?.getString(settingsKey).orEmpty(),
                )
            }

            composable(Screen.ABOUT.name) {
                AboutScreen(
                    animatedContentScope = this,
                    openNavigationRail = openNavigationRail,
                    navigateToLicenses = Screen.LICENSES::navigate,
                    navigateToDeviceInformation = Screen.DEVICE::navigate,
                )
            }

            composable(Screen.LICENSES.name) { backStackEntry ->
                LicensesScreen(animatedContentScope = this) { navigateUp(backStackEntry) }
            }

            composable(Screen.DEVICE.name) { backStackEntry ->
                DeviceInformationScreen(
                    navigateUp = { navigateUp(backStackEntry) },
                    animatedContentScope = this,
                )
            }
        }
    }
}

// Don't ever change the name of the ones that are mentioned xml/shortcuts.xml and ShortcutActivity.kt
private enum class Screen(val navigationRailEntry: Pair<ImageVector, Int>? = null) {
    CALENDAR(Icons.Default.DateRange to R.string.calendar), SCHEDULE, DAYS, MONTH,
    CONVERTER(Icons.Default.SwapVerticalCircle to R.string.date_converter),
    COMPASS(Icons.Default.Explore to R.string.compass), LEVEL,
    ASTRONOMY(AstrologyIcon to R.string.astronomy), MAP,
    SETTINGS(Icons.Default.Settings to R.string.settings),
    ABOUT(Icons.Default.Info to R.string.about), LICENSES, DEVICE,
    EXIT(Icons.Default.Cancel to R.string.exit); // Not a screen but is on the navigation rail, so

    // Which item needs to be highlighted when user is on the screen
    val parent
        get() = when (this) {
            CALENDAR, SCHEDULE, DAYS, MONTH -> CALENDAR
            CONVERTER -> CONVERTER
            COMPASS, LEVEL -> COMPASS
            ASTRONOMY, MAP -> ASTRONOMY
            SETTINGS -> SETTINGS
            ABOUT, LICENSES, DEVICE -> ABOUT
            EXIT -> EXIT
        }

    companion object {
        fun fromName(value: String?) = entries.firstOrNull { it.name == value } ?: entries[0]
    }
}

/** [androidx.compose.material3.tokens.NavigationRailExpandedTokens.ContainerWidthMinimum] **/
private val railWidth = 220.dp

@Composable
private fun AppNavigationRail(
    railState: WideNavigationRailState,
    navController: NavHostController,
    finish: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val dir = LocalLayoutDirection.current
    val startPadding = WindowInsets.displayCutout.asPaddingValues().calculateStartPadding(dir)
    ModalWideNavigationRail(
        hideOnCollapse = true,
        colors = run {
            val colors = WideNavigationRailDefaults.colors()
            colors.copy(
                containerColor = animateColor(colors.containerColor).value,
                contentColor = animateColor(colors.contentColor).value,
                modalContainerColor = animateColor(colors.modalContainerColor).value,
                modalScrimColor = animateColor(colors.modalScrimColor).value,
                modalContentColor = animateColor(colors.modalContentColor).value,
            )
        },
        state = railState,
        modifier = Modifier.width(railWidth + startPadding),
        windowInsets = WindowInsets(0, 0, 0, 0),
    ) {
        Box {
            val scrollState = rememberScrollState()
            Column(
                Modifier
                    .padding(start = startPadding)
                    .verticalScroll(scrollState),
            ) {
                Box(
                    Modifier
                        .semantics(mergeDescendants = true) { this.hideFromAccessibility() }
                        .clearAndSetSemantics {},
                ) {
                    NavigationRailSeasonsPager()
                    NavigationRailDarkModeToggle()
                }
                val defaultColors = WideNavigationRailItemDefaults.colors()
                val colors = defaultColors.copy(
                    selectedIconColor = animateColor(defaultColors.selectedIconColor).value,
                    selectedTextColor = animateColor(defaultColors.selectedTextColor).value,
                    selectedIndicatorColor = animateColor(defaultColors.selectedIndicatorColor).value,
                    unselectedIconColor = animateColor(defaultColors.unselectedIconColor).value,
                    unselectedTextColor = animateColor(defaultColors.unselectedTextColor).value,
                    disabledIconColor = animateColor(defaultColors.disabledIconColor).value,
                    disabledTextColor = animateColor(defaultColors.disabledTextColor).value,
                )
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                Screen.entries.forEach { item ->
                    val (icon, titleId) = item.navigationRailEntry ?: return@forEach
                    WideNavigationRailItem(
                        icon = { Icon(imageVector = icon, contentDescription = null) },
                        colors = colors,
                        railExpanded = true,
                        label = {
                            Text(
                                text = stringResource(titleId),
                                modifier = Modifier.width(railWidth - 104.dp),
                                maxLines = 1,
                                fontSize = LocalTextStyle.current.fontSize,
                                autoSize = TextAutoSize.StepBased(
                                    minFontSize = 6.sp,
                                    maxFontSize = LocalTextStyle.current.fontSize,
                                ),
                            )
                        },
                        selected = item == Screen.fromName(navBackStackEntry?.destination?.route).parent,
                        onClick = {
                            if (item == Screen.EXIT) finish() else coroutineScope.launch {
                                railState.collapse()
                                if (navBackStackEntry?.destination?.route != item.name) {
                                    navController.navigate(item.name)
                                }
                            }
                        },
                    )
                }
            }
            ScrollShadow(scrollState)
        }
    }
}

// Can't be enabled in ModalWideNavigationRail, sadly
//@Composable
//private fun NavigationRailTopGradient() {
//    val isBackgroundColorLight = MaterialTheme.colorScheme.background.isLight
//    val isSurfaceColorLight = MaterialTheme.colorScheme.surface.isLight
//    val needsVisibleStatusBarPlaceHolder = !isBackgroundColorLight && isSurfaceColorLight
//    val topColor by animateColor(
//        if (needsVisibleStatusBarPlaceHolder) Color(0x70000000) else Color.Transparent
//    )
//    Spacer(
//        Modifier
//            .fillMaxWidth()
//            .windowInsetsTopHeight(WindowInsets.systemBars)
//            .background(Brush.verticalGradient(listOf(topColor, Color.Transparent))),
//    )
//}

@Composable
private fun NavigationRailSeasonsPager() {
    var actualSeason by remember {
        mutableIntStateOf(Season.fromDate(Date(), coordinates.value).ordinal)
    }
    val pageSize = 200
    val pagerState = rememberPagerState(pageSize / 2 + actualSeason, pageCount = { pageSize })
    LaunchedEffect(Unit) {
        while (true) {
            delay(30.seconds)
            val seasonIndex = Season.fromDate(Date(), coordinates.value).ordinal
            if (actualSeason != seasonIndex) {
                actualSeason = seasonIndex
                pagerState.animateScrollToPage(pageSize / 2 + actualSeason)
            }
        }
    }

    val isDynamicGrayscale = isDynamicGrayscale()
    val imageFilter = remember(isDynamicGrayscale) {
        if (!isDynamicGrayscale) null
        // Consider gray scale themes of Android 14
        // And apply a gray scale filter https://stackoverflow.com/a/75698731
        else ColorFilter.colorMatrix(ColorMatrix().also { it.setToSaturation(0f) })
    }

    HorizontalPager(
        state = pagerState,
        modifier = Modifier
            .padding(vertical = 10.dp, horizontal = 20.dp)
            .clip(MaterialTheme.shapes.extraLarge),
        pageSpacing = 8.dp,
    ) {
        val season = Season.entries[it % 4]
        Image(
            ImageBitmap.imageResource(season.imageId),
            contentScale = ContentScale.FillWidth,
            contentDescription = """${stringResource(R.string.season)}$spacedColon${
                stringResource(season.nameStringId)
            }""",
            colorFilter = imageFilter,
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.extraLarge),
        )
    }
}

@Composable
private fun BoxScope.NavigationRailDarkModeToggle() {
    val userSetTheme by userSetTheme.collectAsState()
    // If current theme is default theme, isDark is null so no toggle is shown also
    val isDark = userSetTheme.isDark ?: return
    val context = LocalContext.current
    Crossfade(
        label = "dark mode toggle",
        targetState = if (isDark) Icons.Outlined.LightMode else Icons.Default.ModeNight,
        modifier = Modifier
            .semantics { this.hideFromAccessibility() }
            .padding(bottom = 20.dp, end = 28.dp)
            .align(Alignment.BottomEnd)
            .clickable(
                indication = ripple(bounded = false),
                interactionSource = null,
                onClick = {
                    val systemTheme = if (isDark) systemLightTheme else systemDarkTheme
                    context.preferences.edit {
                        putString(PREF_THEME, systemTheme.value.key)
                        putString(
                            if (isDark) PREF_SYSTEM_DARK_THEME else PREF_SYSTEM_LIGHT_THEME,
                            userSetTheme.key
                        )
                    }
                },
            )
            .background(
                animateColor(MaterialTheme.colorScheme.surface.copy(alpha = .5f)).value,
                MaterialTheme.shapes.extraLarge
            )
            .padding(8.dp),
    ) {
        val tint by animateColor(MaterialTheme.colorScheme.onSurface.copy(alpha = if (isDark) .9f else .6f))
        Icon(
            it,
            stringResource(if (isDark) R.string.theme_dark else R.string.theme_light),
            tint = tint,
        )
    }
}
