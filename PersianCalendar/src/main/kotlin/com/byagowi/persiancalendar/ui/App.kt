package com.byagowi.persiancalendar.ui

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.Crossfade
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
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
import com.byagowi.persiancalendar.ui.settings.SettingsScreen
import com.byagowi.persiancalendar.ui.settings.SettingsTab
import com.byagowi.persiancalendar.ui.theme.animateColor
import com.byagowi.persiancalendar.ui.theme.isDynamicGrayscale
import com.byagowi.persiancalendar.ui.utils.findWindow
import com.byagowi.persiancalendar.ui.utils.isLight
import com.byagowi.persiancalendar.utils.preferences
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.util.Date
import kotlin.time.Duration.Companion.seconds

@Composable
fun App(intentStartDestination: String?, initialJdn: Jdn? = null, finish: () -> Unit) {
    val backStack = rememberNavBackStack(Screen.fromName(intentStartDestination))
    val railState = rememberWideNavigationRailState()
    AppNavigationRail(railState, backStack, finish)
    SharedTransitionLayout {
        var appInitialJdn by remember { mutableStateOf(initialJdn) }
        val coroutineScope = rememberCoroutineScope()
        val openNavigationRail: () -> Unit = { coroutineScope.launch { railState.expand() } }
        fun NavKey.isCurrentDestination() = this == backStack.lastOrNull()
        fun NavKey.navigateUp() {
            // Ignore back button double tap
            if (isCurrentDestination()) {
                // Empty back stack causes crash, what is meant is to finish the activity
                if (backStack.size < 2) finish()
                else backStack.removeLastOrNull()
            }
        }
        // Not the best approach to access calendar screen view model…
        var calendarViewModel by remember { mutableStateOf<CalendarViewModel?>(null) }
        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            predictivePopTransitionSpec = {
                ContentTransform(fadeIn(), fadeOut())
            },
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(
                    removeViewModelStoreOnPop = { true },
                ),
            ),
            entryProvider = entryProvider {
                entry<Screen.Calendar> {
                    val viewModel = viewModel<CalendarViewModel>()
                    calendarViewModel = viewModel
                    appInitialJdn?.let { viewModel.bringDay(it); appInitialJdn = null }
                    CalendarScreen(
                        openNavigationRail = openNavigationRail,
                        navigateToHolidaysSettings = { item ->
                            backStack += Screen.Settings(
                                tab = SettingsTab.InterfaceCalendar,
                                settings = PREF_HOLIDAY_TYPES,
                                settingsItem = item,
                            )
                        },
                        navigateToSettingsLocationTabSetAthanAlarm = {
                            backStack += Screen.Settings(
                                tab = SettingsTab.LocationAthan,
                                settings = PREF_ATHAN_ALARM,
                            )
                        },
                        navigateToSchedule = {
                            backStack += Screen.Schedule(viewModel.selectedDay.value)
                        },
                        navigateToDays = { jdn, isWeek ->
                            backStack += Screen.Days(jdn, isWeek)
                        },
                        navigateToMonthView = {
                            backStack += Screen.Month(viewModel.selectedDay.value)
                        },
                        navigateToSettingsLocationTab = {
                            backStack += Screen.Settings(tab = SettingsTab.LocationAthan)
                        },
                        navigateToAstronomy = { day -> backStack += Screen.Astronomy(day) },
                        viewModel = viewModel,
                        isCurrentDestination = it.isCurrentDestination(),
                    )
                }
                entry<Screen.Month> {
                    MonthScreen(
                        calendarViewModel = calendarViewModel ?: viewModel(),
                        navigateUp = it::navigateUp,
                        initiallySelectedDay = it.selectedDay,
                    )
                }
                entry<Screen.Schedule> {
                    ScheduleScreen(
                        calendarViewModel = calendarViewModel ?: viewModel(),
                        navigateUp = it::navigateUp,
                        initiallySelectedDay = it.selectedDay,
                    )
                }
                entry<Screen.Days> {
                    DaysScreen(
                        calendarViewModel = calendarViewModel ?: viewModel(),
                        initiallySelectedDay = it.selectedDay,
                        isInitiallyWeek = it.isWeek,
                        navigateUp = it::navigateUp,
                    )
                }
                entry<Screen.Converter> {
                    ConverterScreen(
                        openNavigationRail = openNavigationRail,
                        navigateToAstronomy = { day -> backStack += Screen.Astronomy(day) },
                        viewModel = viewModel<ConverterViewModel>(),
                        noBackStackAction = if (backStack.size > 1) null else it::navigateUp,
                    )
                }
                entry<Screen.Compass> {
                    CompassScreen(
                        openNavigationRail = openNavigationRail,
                        navigateToLevel = { backStack += Screen.Level },
                        navigateToMap = { backStack += Screen.Map() },
                        navigateToSettingsLocationTab = {
                            backStack += Screen.Settings(tab = SettingsTab.LocationAthan)
                        },
                        noBackStackAction = if (backStack.size > 1) null else it::navigateUp,
                    )
                }
                entry<Screen.Level> {
                    LevelScreen(
                        navigateUp = it::navigateUp,
                        navigateToCompass = { backStack += Screen.Compass },
                    )
                }
                entry<Screen.Astronomy> {
                    val viewModel = viewModel<AstronomyViewModel>()
                    LaunchedEffect(Unit) {
                        it.day?.let { viewModel.changeToTime(it.toGregorianCalendar().timeInMillis) }
                    }
                    AstronomyScreen(
                        openNavigationRail = openNavigationRail,
                        navigateToMap = {
                            val time = viewModel.astronomyState.value.date.timeInMillis
                            backStack += Screen.Map(time = time)
                        },
                        viewModel = viewModel,
                        noBackStackAction = if (backStack.size > 1) null else it::navigateUp,
                    )
                }
                entry<Screen.Map> {
                    val viewModel = viewModel<MapViewModel>()
                    LaunchedEffect(Unit) {
                        if (it.time != null) viewModel.changeToTime(Date(it.time))
                    }
                    MapScreen(
                        navigateUp = it::navigateUp,
                        fromSettings = it.fromSettings,
                        viewModel = viewModel,
                    )
                }
                entry<Screen.Settings> {
                    SettingsScreen(
                        openNavigationRail = openNavigationRail,
                        navigateToMap = { backStack += Screen.Map(fromSettings = true) },
                        initialTab = it.tab,
                        destination = it.settings,
                        destinationItem = it.settingsItem,
                    )
                }
                entry<Screen.About> {
                    AboutScreen(
                        openNavigationRail = openNavigationRail,
                        navigateToLicenses = { backStack += Screen.Licenses },
                        navigateToDeviceInformation = { backStack += Screen.Device },
                    )
                }
                entry<Screen.Licenses> { LicensesScreen(navigateUp = it::navigateUp) }
                entry<Screen.Device> { DeviceInformationScreen(navigateUp = it::navigateUp) }
            },
        )
    }
}

private sealed interface Screen : NavKey {
    @Serializable
    data object Calendar : Screen

    @Serializable
    data class Schedule(val selectedDay: Jdn) : Screen

    @Serializable
    data class Days(val selectedDay: Jdn, val isWeek: Boolean = false) : Screen

    @Serializable
    data class Month(val selectedDay: Jdn) : Screen

    @Serializable
    data object Converter : Screen

    @Serializable
    data object Compass : Screen

    @Serializable
    data object Level : Screen

    @Serializable
    data class Astronomy(val day: Jdn? = null) : Screen

    @Serializable
    data class Map(val fromSettings: Boolean = false, val time: Long? = null) : Screen

    @Serializable
    data class Settings(
        val tab: SettingsTab = SettingsTab.entries[0],
        val settings: String? = null,
        val settingsItem: String? = null,
    ) : Screen

    @Serializable
    data object About : Screen

    @Serializable
    data object Licenses : Screen

    @Serializable
    data object Device : Screen

    @Serializable
    data object Exit : Screen // Not a screen but is on the navigation rail, so

    companion object {
        val navEntries = listOf(
            Triple(Calendar, Icons.Default.DateRange, R.string.calendar),
            Triple(Converter, Icons.Default.SwapVerticalCircle, R.string.date_converter),
            Triple(Compass, Icons.Default.Explore, R.string.compass),
            Triple(Astronomy(), AstrologyIcon, R.string.astronomy),
            Triple(Settings(), Icons.Default.Settings, R.string.settings),
            Triple(About, Icons.Default.Info, R.string.about),
            Triple(Exit, Icons.Default.Cancel, R.string.exit)
        )

        fun fromName(value: String?): Screen = when (Shortcut.fromName(value)) {
            Shortcut.CONVERTER -> Converter
            Shortcut.COMPASS -> Compass
            Shortcut.LEVEL -> Level
            Shortcut.ASTRONOMY -> Astronomy()
            Shortcut.MAP -> Map()
            null -> Calendar
        }
    }
}

/** [androidx.compose.material3.tokens.NavigationRailExpandedTokens.ContainerWidthMinimum] **/
private val railWidth = 220.dp

@Composable
private fun AppNavigationRail(
    railState: WideNavigationRailState,
    backStack: NavBackStack<NavKey>,
    finish: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val startPadding = WindowInsets.displayCutout.asPaddingValues()
        .calculateStartPadding(layoutDirection = LocalLayoutDirection.current)
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
        windowInsets = WindowInsets(),
    ) {
        Box(Modifier.navigationRailTopGradient()) {
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
                Screen.navEntries.forEach { (screen, icon, titleId) ->
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
                        selected = screen.javaClass == backStack.lastOrNull()?.javaClass,
                        onClick = {
                            if (screen == Screen.Exit) finish() else coroutineScope.launch {
                                railState.collapse()
                                if (backStack.lastOrNull() != screen) backStack += screen
                            }
                        },
                    )
                }
            }
            ScrollShadow(scrollState)
        }
    }
}

@Composable
private fun Modifier.navigationRailTopGradient(): Modifier {
    val isBackgroundColorLight = MaterialTheme.colorScheme.background.isLight
    val view = LocalView.current
    LaunchedEffect(isBackgroundColorLight) {
        view.findWindow()?.let { window ->
            WindowInsetsControllerCompat(window, view).isAppearanceLightStatusBars =
                isBackgroundColorLight
        }
    }
    val isSurfaceColorLight = MaterialTheme.colorScheme.surface.isLight
    val needsVisibleStatusBarPlaceHolder = !isBackgroundColorLight && isSurfaceColorLight
    val topColor by animateColor(
        if (needsVisibleStatusBarPlaceHolder) Color(0x70000000) else Color.Transparent
    )
    var height by remember { mutableFloatStateOf(0f) }
    return this
        .onGloballyPositioned { height = it.positionInRoot().y }
        .drawBehind {
            val colors = listOf(topColor, Color.Transparent)
            drawRect(
                Brush.verticalGradient(colors, startY = -height, endY = 0f),
                size = this.size.copy(height = -height),
            )
        }
}

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
