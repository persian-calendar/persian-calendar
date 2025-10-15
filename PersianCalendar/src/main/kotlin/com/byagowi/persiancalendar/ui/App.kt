package com.byagowi.persiancalendar.ui

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
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
import com.byagowi.persiancalendar.global.mainCalendar
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
import com.byagowi.persiancalendar.ui.utils.isLight
import com.byagowi.persiancalendar.utils.preferences
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Date
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun App(intentStartDestination: String?, initialJdn: Jdn? = null, finish: () -> Unit) {
    var appInitialJdn by remember { mutableStateOf(initialJdn) }
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    if (drawerState.isOpen) {
        BackHandler(enabled = true) { coroutineScope.launch { drawerState.close() } }
    }

    val selectedDayKey = "SELECTED_DAY"
    val isWeekKey = "IS_WEEK"
    val tabKey = "TAB"
    val settingsKey = "SETTINGS"
    val daysOffsetKey = "DAYS_OFFSET"

    SharedTransitionLayout {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    drawerContainerColor = animateColor(MaterialTheme.colorScheme.surface).value,
                    drawerContentColor = animateColor(MaterialTheme.colorScheme.onSurface).value,
                    modifier = Modifier.renderInSharedTransitionScopeOverlay(),
                ) {
                    Box {
                        val scrollState = rememberScrollState()
                        DrawerContent(drawerState, navController, finish, scrollState)
                        DrawerTopGradient()
                        ScrollShadow(scrollState, top = true)
                        ScrollShadow(scrollState, top = false)
                    }
                }
            },
        ) {
            NavHost(
                navController = navController,
                startDestination = Route.fromName(intentStartDestination).name,
            ) {
                fun Route.navigate() = navController.navigate(this.name)
                fun Route.navigate(bundle: Bundle) {
                    val destination = navController.graph.findNode(this.name) ?: return
                    navController.navigate(destination.id, bundle)
                }

                fun isCurrentDestination(backStackEntry: NavBackStackEntry) =
                    navController.currentDestination == backStackEntry.destination

                fun navigateUp(backStackEntry: NavBackStackEntry) {
                    // If we aren't in the screen that this wasn't supposed to be called, just ignore, happens while transition
                    if (!isCurrentDestination(backStackEntry)) return
                    // if there wasn't anything to pop, just exit the app, happens if the app is entered from the map widget
                    if (!navController.popBackStack()) finish()
                }

                fun navigateToSettingsLocationTab() =
                    Route.SETTINGS.navigate(bundleOf(tabKey to LOCATION_ATHAN_TAB))

                fun navigateToAstronomy(jdn: Jdn) =
                    Route.ASTRONOMY.navigate(bundleOf(daysOffsetKey to jdn - Jdn.today()))

                composable(Route.CALENDAR.name) { backStackEntry ->
                    val viewModel = viewModel<CalendarViewModel>()
                    appInitialJdn?.let {
                        viewModel.changeSelectedMonthOffsetCommand(
                            mainCalendar.getMonthsDistance(Jdn.today(), it)
                        )
                        viewModel.changeSelectedDay(it)
                        appInitialJdn = null
                    }
                    CalendarScreen(
                        openDrawer = { coroutineScope.launch { drawerState.open() } },
                        navigateToHolidaysSettings = {
                            Route.SETTINGS.navigate(
                                bundleOf(
                                    tabKey to INTERFACE_CALENDAR_TAB,
                                    settingsKey to PREF_HOLIDAY_TYPES,
                                )
                            )
                        },
                        navigateToSettingsLocationTabSetAthanAlarm = {
                            Route.SETTINGS.navigate(
                                bundleOf(
                                    tabKey to LOCATION_ATHAN_TAB,
                                    settingsKey to PREF_ATHAN_ALARM,
                                )
                            )
                        },
                        navigateToSchedule = { Route.SCHEDULE.navigate() },
                        navigateToDays = { jdn, isWeek ->
                            Route.DAYS.navigate(
                                bundleOf(selectedDayKey to jdn.value, isWeekKey to isWeek)
                            )
                        },
                        navigateToMonthView = { Route.MONTH.navigate() },
                        navigateToSettingsLocationTab = ::navigateToSettingsLocationTab,
                        navigateToAstronomy = ::navigateToAstronomy,
                        viewModel = viewModel,
                        animatedContentScope = this,
                        isCurrentDestination = isCurrentDestination(backStackEntry),
                    )
                }

                composable(Route.MONTH.name) { backStackEntry ->
                    val previousEntry = navController.previousBackStackEntry
                    val previousRoute = previousEntry?.destination?.route
                    val viewModel = if (previousRoute == Route.CALENDAR.name) {
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

                composable(Route.SCHEDULE.name) { backStackEntry ->
                    val previousEntry = navController.previousBackStackEntry
                    val previousRoute = previousEntry?.destination?.route
                    val viewModel = if (previousRoute == Route.CALENDAR.name) {
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

                composable(Route.DAYS.name) { backStackEntry ->
                    val previousEntry = navController.previousBackStackEntry
                    val previousRoute = previousEntry?.destination?.route
                    val viewModel = if (previousRoute == Route.CALENDAR.name) {
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

                composable(Route.CONVERTER.name) { backStackEntry ->
                    ConverterScreen(
                        animatedContentScope = this,
                        openDrawer = { coroutineScope.launch { drawerState.open() } },
                        navigateToAstronomy = ::navigateToAstronomy,
                        viewModel = viewModel<ConverterViewModel>(),
                        noBackStackAction = if (navController.previousBackStackEntry != null) null
                        else ({ navigateUp(backStackEntry) }),
                    )
                }

                composable(Route.COMPASS.name) { backStackEntry ->
                    CompassScreen(
                        animatedContentScope = this,
                        openDrawer = { coroutineScope.launch { drawerState.open() } },
                        navigateToLevel = { Route.LEVEL.navigate() },
                        navigateToMap = { Route.MAP.navigate() },
                        navigateToSettingsLocationTab = ::navigateToSettingsLocationTab,
                        noBackStackAction = if (navController.previousBackStackEntry != null) null
                        else ({ navigateUp(backStackEntry) }),
                    )
                }

                composable(Route.LEVEL.name) { backStackEntry ->
                    LevelScreen(
                        animatedContentScope = this,
                        navigateUp = { navigateUp(backStackEntry) },
                        navigateToCompass = { Route.COMPASS.navigate() },
                    )
                }

                composable(Route.ASTRONOMY.name) { backStackEntry ->
                    val viewModel = viewModel<AstronomyViewModel>()
                    backStackEntry.arguments?.getInt(daysOffsetKey, 0)?.takeIf { it != 0 }?.let {
                        viewModel.changeToTime((Jdn.today() + it).toGregorianCalendar().timeInMillis)
                    }
                    AstronomyScreen(
                        animatedContentScope = this,
                        openDrawer = { coroutineScope.launch { drawerState.open() } },
                        navigateToMap = { Route.MAP.navigate() },
                        viewModel = viewModel,
                        noBackStackAction = if (navController.previousBackStackEntry != null) null
                        else ({ navigateUp(backStackEntry) }),
                    )
                }

                composable(Route.MAP.name) { backStackEntry ->
                    val viewModel = viewModel<MapViewModel>()
                    val previousEntry = navController.previousBackStackEntry
                    val previousRoute = previousEntry?.destination?.route
                    if (previousRoute == Route.ASTRONOMY.name) {
                        val astronomyViewModel = viewModel<AstronomyViewModel>(previousEntry)
                        LaunchedEffect(Unit) {
                            viewModel.changeToTime(astronomyViewModel.astronomyState.value.date.time)
                            viewModel.state.collectLatest { astronomyViewModel.changeToTime(it.time) }
                        }
                    }
                    MapScreen(
                        animatedContentScope = this,
                        navigateUp = { navigateUp(backStackEntry) },
                        fromSettings = previousRoute == Route.SETTINGS.name,
                        viewModel = viewModel,
                    )
                }

                composable(Route.SETTINGS.name) { backStackEntry ->
                    SettingsScreen(
                        animatedContentScope = this,
                        openDrawer = { coroutineScope.launch { drawerState.open() } },
                        navigateToMap = { Route.MAP.navigate() },
                        initialPage = backStackEntry.arguments?.getInt(tabKey, 0) ?: 0,
                        destination = backStackEntry.arguments?.getString(settingsKey).orEmpty()
                    )
                }

                composable(Route.ABOUT.name) {
                    AboutScreen(
                        animatedContentScope = this,
                        openDrawer = { coroutineScope.launch { drawerState.open() } },
                        navigateToLicenses = { Route.LICENSES.navigate() },
                        navigateToDeviceInformation = { Route.DEVICE.navigate() },
                    )
                }

                composable(Route.LICENSES.name) { backStackEntry ->
                    LicensesScreen(animatedContentScope = this) { navigateUp(backStackEntry) }
                }

                composable(Route.DEVICE.name) { backStackEntry ->
                    DeviceInformationScreen(
                        navigateUp = { navigateUp(backStackEntry) },
                        animatedContentScope = this,
                    )
                }
            }
        }
    }
}

// Don't ever change the name of the ones that are mentioned xml/shortcuts.xml and ShortcutActivity.kt
private enum class Route(val drawerEntry: Pair<ImageVector, Int>? = null) {
    CALENDAR(Icons.Default.DateRange to R.string.calendar), MONTH, SCHEDULE, DAYS,
    CONVERTER(Icons.Default.SwapVerticalCircle to R.string.date_converter),
    COMPASS(Icons.Default.Explore to R.string.compass), LEVEL,
    ASTRONOMY(AstrologyIcon to R.string.astronomy), MAP,
    SETTINGS(Icons.Default.Settings to R.string.settings),
    ABOUT(Icons.Default.Info to R.string.about), LICENSES, DEVICE,
    EXIT(Icons.Default.Cancel to R.string.exit);

    companion object {
        fun fromName(value: String?) = entries.firstOrNull { it.name == value } ?: entries[0]
    }
}

// Make system status bar icons visible when surface is light but theme has dark background
@Composable
private fun DrawerTopGradient() {
    val isBackgroundColorLight = MaterialTheme.colorScheme.background.isLight
    val isSurfaceColorLight = MaterialTheme.colorScheme.surface.isLight
    val needsVisibleStatusBarPlaceHolder = !isBackgroundColorLight && isSurfaceColorLight
    val topColor by animateColor(
        if (needsVisibleStatusBarPlaceHolder) Color(0x70000000) else Color.Transparent
    )
    Spacer(
        Modifier
            .fillMaxWidth()
            .windowInsetsTopHeight(WindowInsets.systemBars)
            .background(Brush.verticalGradient(listOf(topColor, Color.Transparent))),
    )
}

@Composable
private fun DrawerContent(
    drawerState: DrawerState,
    navController: NavHostController,
    finish: () -> Unit,
    scrollState: ScrollState,
) {
    Column(
        modifier = Modifier
            .verticalScroll(scrollState)
            .windowInsetsPadding(WindowInsets.systemBars)
            .windowInsetsPadding(WindowInsets.displayCutout.only(WindowInsetsSides.Start))
    ) {
        Box(
            Modifier
                .semantics(mergeDescendants = true) { this.hideFromAccessibility() }
                .clearAndSetSemantics {},
        ) {
            DrawerSeasonsPager(drawerState)
            DrawerDarkModeToggle()
        }
        val onSecondaryContainerColor by animateColor(MaterialTheme.colorScheme.onSecondaryContainer)
        val navItemColors = NavigationDrawerItemDefaults.colors(
            unselectedContainerColor = Color.Transparent,
            unselectedTextColor = LocalContentColor.current,
            unselectedIconColor = LocalContentColor.current,
            selectedContainerColor = animateColor(MaterialTheme.colorScheme.secondaryContainer).value,
            selectedTextColor = onSecondaryContainerColor,
            selectedIconColor = onSecondaryContainerColor,
        )
        val coroutineScope = rememberCoroutineScope()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        Route.entries.forEach { item ->
            val (icon, titleId) = item.drawerEntry ?: return@forEach
            NavigationDrawerItem(
                modifier = Modifier.padding(horizontal = 16.dp),
                icon = { Icon(icon, contentDescription = null) },
                colors = navItemColors,
                label = { Text(stringResource(titleId)) },
                selected = when (val route = navBackStackEntry?.destination?.route) {
                    Route.LEVEL.name -> Route.COMPASS.name
                    Route.MAP.name -> Route.ASTRONOMY.name
                    Route.DEVICE.name, Route.LICENSES.name -> Route.ABOUT.name
                    else -> route ?: Route.CALENDAR.name
                } == item.name,
                onClick = onClick@{
                    if (item == Route.EXIT) return@onClick finish()
                    coroutineScope.launch {
                        drawerState.close()
                        if (navBackStackEntry?.destination?.route != item.name) {
                            navController.navigate(item.name)
                        }
                    }
                },
            )
        }
    }
}

@Composable
private fun DrawerSeasonsPager(drawerState: DrawerState) {
    var actualSeason by remember {
        mutableIntStateOf(Season.fromDate(Date(), coordinates.value).ordinal)
    }
    val pageSize = 200
    val pagerState = rememberPagerState(
        initialPage = pageSize / 2 + actualSeason - 3, // minus 3 so it does an initial animation
        pageCount = { pageSize },
    )
    if (drawerState.isOpen) {
        LaunchedEffect(Unit) {
            pagerState.animateScrollToPage(pageSize / 2 + actualSeason)
            while (true) {
                delay(30.seconds)
                val seasonIndex = Season.fromDate(Date(), coordinates.value).ordinal
                if (seasonIndex != actualSeason) {
                    actualSeason = seasonIndex
                    pagerState.animateScrollToPage(pageSize / 2 + actualSeason)
                }
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
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp)
            .height(196.dp)
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
                .fillMaxSize()
                .clip(MaterialTheme.shapes.extraLarge),
        )
    }
}

@Composable
private fun BoxScope.DrawerDarkModeToggle() {
    val userSetTheme by userSetTheme.collectAsState()
    // If current theme is default theme, isDark is null so no toggle is shown also
    val isDark = userSetTheme.isDark ?: return
    val context = LocalContext.current
    Crossfade(
        label = "dark mode toggle",
        targetState = if (isDark) Icons.Outlined.LightMode else Icons.Default.ModeNight,
        modifier = Modifier
            .semantics { this.hideFromAccessibility() }
            .padding(32.dp)
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
