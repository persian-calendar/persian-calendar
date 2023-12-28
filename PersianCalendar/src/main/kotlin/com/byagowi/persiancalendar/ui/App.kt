package com.byagowi.persiancalendar.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapVerticalCircle
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigation.suite.ExperimentalMaterial3AdaptiveNavigationSuiteApi
import androidx.compose.material3.adaptive.navigation.suite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigation.suite.NavigationSuiteType
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.byagowi.persiancalendar.PREF_HOLIDAY_TYPES
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.entities.Season
import com.byagowi.persiancalendar.global.coordinates
import com.byagowi.persiancalendar.global.theme
import com.byagowi.persiancalendar.ui.about.AboutScreen
import com.byagowi.persiancalendar.ui.about.DeviceInformationScreen
import com.byagowi.persiancalendar.ui.about.LicensesScreen
import com.byagowi.persiancalendar.ui.astronomy.AstronomyScreen
import com.byagowi.persiancalendar.ui.astronomy.AstronomyViewModel
import com.byagowi.persiancalendar.ui.calendar.CalendarScreen
import com.byagowi.persiancalendar.ui.calendar.CalendarViewModel
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
import com.byagowi.persiancalendar.ui.utils.isDynamicGrayscale
import com.byagowi.persiancalendar.utils.THIRTY_SECONDS_IN_MILLIS
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Date

@OptIn(
    ExperimentalMaterial3AdaptiveNavigationSuiteApi::class,
    ExperimentalMaterial3AdaptiveApi::class
)
@Composable
fun App(intentStartDestination: String?, finish: () -> Unit) {
    // See xml/shortcuts.xml
    val startDestination = when (intentStartDestination) {
        "COMPASS" -> compassRoute
        "LEVEL" -> levelRoute
        "CONVERTER" -> converterRoute
        "ASTRONOMY" -> astronomyRoute
        "MAP" -> mapRoute
        else -> calendarRoute
    }
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    BackHandler(enabled = drawerState.isOpen) { scope.launch { drawerState.close() } }

    val navBackStackEntry by navController.currentBackStackEntryAsState()

    val adaptiveInfo = currentWindowAdaptiveInfo()
    val customNavSuiteType = with(adaptiveInfo) {
        when (windowSizeClass.widthSizeClass) {
            WindowWidthSizeClass.Expanded -> NavigationSuiteType.NavigationRail
            WindowWidthSizeClass.Medium -> NavigationSuiteType.NavigationRail
            WindowWidthSizeClass.Compact -> NavigationSuiteType.NavigationBar
            else -> NavigationSuiteType.None
        }
    }

    NavigationSuiteScaffold(
        layoutType = customNavSuiteType,
        navigationSuiteItems = {
            navItems.forEach { (id, icon, title) ->
                item(
                    icon = { Icon(icon, contentDescription = stringResource(title)) },
                    label = { Text(stringResource(title)) },
                    selected = navBackStackEntry?.destination?.route == id,
                    onClick = {
                        if (id == null) return@item finish()
                        scope.launch {
                            drawerState.close()
                            if (navBackStackEntry?.destination?.route != id) {
                                navController.navigate(id)
                            }
                        }
                    }
                )
            }
        }
//        drawerState = drawerState,
//        drawerContent = {
//            ModalDrawerSheet(windowInsets = WindowInsets(0, 0, 0, 0)) {
//                run {
//                    val isBackgroundColorLight = MaterialTheme.colorScheme.background.isLight
//                    val isSurfaceColorLight = MaterialTheme.colorScheme.surface.isLight
//                    val needsVisibleStatusBarPlaceHolder =
//                        !isBackgroundColorLight && isSurfaceColorLight
//                    Spacer(
//                        Modifier
//                            .fillMaxWidth()
//                            .then(
//                                if (needsVisibleStatusBarPlaceHolder) Modifier.background(
//                                    Brush.verticalGradient(
//                                        0f to Color(0x70000000), 1f to Color.Transparent
//                                    )
//                                ) else Modifier
//                            )
//                            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top)),
//                    )
//                }
//
//                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
//                    DrawerSeasonsPager(drawerState)
//                    DrawerItems().forEach { (id, icon, title) ->
//                        NavigationDrawerItem(
//                            modifier = Modifier.padding(horizontal = 16.dp),
//                            icon = { Icon(icon, contentDescription = null) },
//                            label = { Text(stringResource(title)) },
//                            selected = when (val route = navBackStackEntry?.destination?.route) {
//                                levelRoute -> compassRoute
//                                mapRoute -> astronomyRoute
//                                deviceInformationRoute, licensesRoute -> aboutRoute
//                                else -> route ?: calendarRoute
//                            } == id,
//                            onClick = {
//                                if (id == null) return@NavigationDrawerItem finish()
//                                scope.launch {
//                                    drawerState.close()
//                                    if (navBackStackEntry?.destination?.route != id) {
//                                        navController.navigate(id)
//                                    }
//                                }
//                            },
//                        )
//                    }
//                    Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.systemBars))
//                }
//            }
//        },
    ) {
        NavHost(
            navController = navController,
            startDestination = startDestination,
        ) {
            val tabKey = "TAB"
            val settingsKey = "SETTINGS"
            val daysOffsetKey = "DAYS_OFFSET"

            fun navigateToSettingsLocationTab() {
                navController.graph.findNode(settingsRoute)?.let { destination ->
                    navController.navigate(
                        destination.id, bundleOf(tabKey to LOCATION_ATHAN_TAB)
                    )
                }
            }

            fun navigateUp(currentRoute: String) {
                // If we aren't in the screen that this was supposed to be called, just ignore, happens while transition
                if (navController.currentDestination?.route != currentRoute) return
                // if there wasn't anything to pop, just exit the app, happens if the app is entered from the map widget
                if (!navController.popBackStack()) finish()
            }

            composable(calendarRoute) {
                CalendarScreen(
                    openDrawer = { scope.launch { drawerState.open() } },
                    navigateToHolidaysSettings = {
                        navController.graph.findNode(settingsRoute)?.let { destination ->
                            navController.navigate(
                                destination.id, bundleOf(
                                    tabKey to INTERFACE_CALENDAR_TAB,
                                    settingsKey to PREF_HOLIDAY_TYPES
                                )
                            )
                        }
                    },
                    navigateToSettingsLocationTab = ::navigateToSettingsLocationTab,
                    navigateToAstronomy = { daysOffset ->
                        navController.graph.findNode(astronomyRoute)?.let { destination ->
                            navController.navigate(
                                destination.id, bundleOf(daysOffsetKey to daysOffset)
                            )
                        }
                    },
                    viewModel = viewModel<CalendarViewModel>(),
                )
            }

            composable(converterRoute) {
                ConverterScreen(
                    openDrawer = { scope.launch { drawerState.open() } },
                    viewModel = viewModel<ConverterViewModel>()
                )
            }

            composable(compassRoute) {
                CompassScreen(
                    openDrawer = { scope.launch { drawerState.open() } },
                    navigateToLevel = { navController.navigate(levelRoute) },
                    navigateToMap = { navController.navigate(mapRoute) },
                    navigateToSettingsLocationTab = ::navigateToSettingsLocationTab,
                )
            }

            composable(levelRoute) {
                LevelScreen(
                    navigateUp = { navigateUp(levelRoute) },
                    navigateToCompass = {
                        // If compass wasn't in backstack (level is brought from shortcut), navigate to it
                        if (!navController.popBackStack(compassRoute, false)) {
                            navController.navigate(levelRoute)
                        }
                    },
                )
            }

            composable(astronomyRoute) {
                val viewModel = viewModel<AstronomyViewModel>()
                it.arguments?.getInt(daysOffsetKey, 0)?.takeIf { it != 0 }?.let {
                    viewModel.changeToTime((Jdn.today() + it).toGregorianCalendar().timeInMillis)
                }
                AstronomyScreen(
                    openDrawer = { scope.launch { drawerState.open() } },
                    navigateToMap = { navController.navigate(mapRoute) },
                    viewModel = viewModel,
                )
            }

            composable(mapRoute) {
                val viewModel = viewModel<MapViewModel>()
                val previousEntry = navController.previousBackStackEntry
                if (previousEntry?.destination?.route == astronomyRoute) {
                    val astronomyViewModel = viewModel<AstronomyViewModel>(previousEntry)
                    viewModel.changeToTime(astronomyViewModel.astronomyState.value.date.time)
                    LaunchedEffect(Unit) {
                        viewModel.state.collectLatest { astronomyViewModel.changeToTime(it.time) }
                    }
                }
                MapScreen(
                    navigateUp = { navigateUp(mapRoute) },
                    viewModel = viewModel,
                )
            }

            composable(settingsRoute) {
                SettingsScreen(
                    openDrawer = { scope.launch { drawerState.open() } },
                    navigateToMap = { navController.navigate(mapRoute) },
                    initialPage = it.arguments?.getInt(tabKey, 0) ?: 0,
                    destination = it.arguments?.getString(settingsKey) ?: ""
                )
            }

            composable(aboutRoute) {
                AboutScreen(
                    openDrawer = { scope.launch { drawerState.open() } },
                    navigateToLicenses = { navController.navigate(licensesRoute) },
                    navigateToDeviceInformation = {
                        navController.navigate(deviceInformationRoute)
                    },
                )
            }

            composable(licensesRoute) {
                LicensesScreen { navigateUp(licensesRoute) }
            }

            composable(deviceInformationRoute) {
                DeviceInformationScreen { navigateUp(deviceInformationRoute) }
            }
        }
    }
}

private const val calendarRoute = "calendar"
private const val compassRoute = "compass"
private const val levelRoute = "level"
private const val mapRoute = "map"
private const val converterRoute = "converter"
private const val astronomyRoute = "astronomy"
private const val settingsRoute = "settings"
private const val aboutRoute = "about"
private const val licensesRoute = "license"
private const val deviceInformationRoute = "device"

@Stable
private val navItems: List<Triple<String?, ImageVector, Int>> = listOf(
    Triple(calendarRoute, Icons.Default.DateRange, R.string.calendar),
    Triple(converterRoute, Icons.Default.SwapVerticalCircle, R.string.date_converter),
    Triple(compassRoute, Icons.Default.Explore, R.string.compass),
    Triple(astronomyRoute, AstrologyIcon, R.string.astronomy),
    Triple(settingsRoute, Icons.Default.Settings, R.string.settings),
//    Triple(aboutRoute, Icons.Default.Info, R.string.about),
//    Triple(null, Icons.Default.Cancel, R.string.exit),
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DrawerSeasonsPager(drawerState: DrawerState) {
    var actualSeason by remember {
        mutableStateOf(Season.fromDate(Date(), coordinates.value).ordinal)
    }
    val pageSize = 200
    val seasonState = rememberPagerState(
        initialPage = pageSize / 2 + actualSeason - 3, // minus 3 so it does an initial animation
        pageCount = { pageSize },
    )
    if (drawerState.isOpen) {
        LaunchedEffect(Unit) {
            seasonState.animateScrollToPage(pageSize / 2 + actualSeason)
            while (true) {
                delay(THIRTY_SECONDS_IN_MILLIS)
                val seasonIndex = Season.fromDate(Date(), coordinates.value).ordinal
                if (seasonIndex != actualSeason) {
                    actualSeason = seasonIndex
                    seasonState.animateScrollToPage(pageSize / 2 + actualSeason)
                }
            }
        }
    }

    val context = LocalContext.current
    val theme by theme.collectAsState()
    val imageFilter = remember(LocalConfiguration.current, theme) {
        // Consider gray scale themes of Android 14
        // And apply a gray scale filter https://stackoverflow.com/a/75698731
        if (theme.isDynamicColors() && context.isDynamicGrayscale) {
            ColorFilter.colorMatrix(ColorMatrix().also { it.setToSaturation(0f) })
        } else null
    }

    HorizontalPager(
        state = seasonState,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp)
            .height(196.dp)
            .clip(MaterialTheme.shapes.extraLarge)
            .semantics {
                @OptIn(ExperimentalComposeUiApi::class) this.invisibleToUser()
            },
        pageSpacing = 8.dp,
    ) {
        Image(
            ImageBitmap.imageResource(Season.entries[it % 4].imageId),
            contentScale = ContentScale.FillWidth,
            contentDescription = null,
            colorFilter = imageFilter,
            modifier = Modifier
                .fillMaxSize()
                .clip(MaterialTheme.shapes.extraLarge),
        )
    }
}
