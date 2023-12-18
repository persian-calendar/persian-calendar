package com.byagowi.persiancalendar.ui

import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
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
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapVerticalCircle
import androidx.compose.material3.DrawerDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.core.os.bundleOf
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.byagowi.persiancalendar.DEFAULT_NOTIFY_DATE
import com.byagowi.persiancalendar.LAST_CHOSEN_TAB_KEY
import com.byagowi.persiancalendar.PREF_APP_LANGUAGE
import com.byagowi.persiancalendar.PREF_EASTERN_GREGORIAN_ARABIC_MONTHS
import com.byagowi.persiancalendar.PREF_ENGLISH_GREGORIAN_PERSIAN_MONTHS
import com.byagowi.persiancalendar.PREF_HAS_EVER_VISITED
import com.byagowi.persiancalendar.PREF_HOLIDAY_TYPES
import com.byagowi.persiancalendar.PREF_ISLAMIC_OFFSET
import com.byagowi.persiancalendar.PREF_ISLAMIC_OFFSET_SET_DATE
import com.byagowi.persiancalendar.PREF_LAST_APP_VISIT_VERSION
import com.byagowi.persiancalendar.PREF_MIDNIGHT_METHOD
import com.byagowi.persiancalendar.PREF_NOTIFY_DATE
import com.byagowi.persiancalendar.PREF_PRAY_TIME_METHOD
import com.byagowi.persiancalendar.PREF_THEME
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.entities.Season
import com.byagowi.persiancalendar.global.configureCalendarsAndLoadEvents
import com.byagowi.persiancalendar.global.coordinates
import com.byagowi.persiancalendar.global.initGlobal
import com.byagowi.persiancalendar.global.loadLanguageResources
import com.byagowi.persiancalendar.global.updateStoredPreference
import com.byagowi.persiancalendar.service.ApplicationService
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
import com.byagowi.persiancalendar.ui.level.LevelScreen
import com.byagowi.persiancalendar.ui.map.MapScreen
import com.byagowi.persiancalendar.ui.map.MapViewModel
import com.byagowi.persiancalendar.ui.settings.INTERFACE_CALENDAR_TAB
import com.byagowi.persiancalendar.ui.settings.LOCATION_ATHAN_TAB
import com.byagowi.persiancalendar.ui.settings.SettingsScreen
import com.byagowi.persiancalendar.ui.theme.AppTheme
import com.byagowi.persiancalendar.ui.theme.Theme
import com.byagowi.persiancalendar.ui.utils.SystemBarsTransparency
import com.byagowi.persiancalendar.ui.utils.isDynamicGrayscale
import com.byagowi.persiancalendar.ui.utils.transparentSystemBars
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.applyAppLanguage
import com.byagowi.persiancalendar.utils.applyLanguageToConfiguration
import com.byagowi.persiancalendar.utils.putJdn
import com.byagowi.persiancalendar.utils.readAndStoreDeviceCalendarEventsOfTheDay
import com.byagowi.persiancalendar.utils.startWorker
import com.byagowi.persiancalendar.utils.update
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Date

class MainActivity : ComponentActivity(), SharedPreferences.OnSharedPreferenceChangeListener {

    private var creationDateJdn = Jdn.today()
    private var settingHasChanged = false

    override fun onCreate(savedInstanceState: Bundle?) {
        Theme.apply(this)
        applyAppLanguage(this)
        super.onCreate(savedInstanceState)
        transparentSystemBars()

        initGlobal(this)

        startWorker(this)

        readAndStoreDeviceCalendarEventsOfTheDay(applicationContext)
        update(applicationContext, false)

        val intentStartDestination = intent?.action
        intent?.action = ""

        setContent { AppTheme { App(intentStartDestination, ::finish) } }

        appPrefs.registerOnSharedPreferenceChangeListener(this)

        applyAppLanguage(this)
    }

    //        if (settingHasChanged) { // update when checked menu item is changed
//            applyAppLanguage(this)
//            update(applicationContext, true)
//            settingHasChanged = false // reset for the next time
//        }
    override fun onSharedPreferenceChanged(prefs: SharedPreferences?, key: String?) {
        settingHasChanged = true

        prefs ?: return

        // If it is the first initiation of preference, don't call the rest multiple times
        if (key == PREF_HAS_EVER_VISITED || PREF_HAS_EVER_VISITED !in prefs) return

        when (key) {
            PREF_LAST_APP_VISIT_VERSION -> return // nothing needs to be updated
            LAST_CHOSEN_TAB_KEY -> return // don't run the expensive update and etc on tab changes
            PREF_ISLAMIC_OFFSET -> prefs.edit { putJdn(PREF_ISLAMIC_OFFSET_SET_DATE, Jdn.today()) }

            PREF_THEME -> {
                Theme.apply(this)
                transparentSystemBars()
            }

            PREF_NOTIFY_DATE -> {
                if (!prefs.getBoolean(PREF_NOTIFY_DATE, DEFAULT_NOTIFY_DATE)) {
                    stopService(Intent(this, ApplicationService::class.java))
                    startWorker(applicationContext)
                }
            }

            PREF_PRAY_TIME_METHOD -> prefs.edit { remove(PREF_MIDNIGHT_METHOD) }
        }

        configureCalendarsAndLoadEvents(this)
        updateStoredPreference(this)
        update(applicationContext, true)

        if (key == PREF_EASTERN_GREGORIAN_ARABIC_MONTHS || key == PREF_ENGLISH_GREGORIAN_PERSIAN_MONTHS || key == PREF_APP_LANGUAGE) {
            loadLanguageResources(this)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(applyLanguageToConfiguration(newConfig))
        applyAppLanguage(this)
    }

    override fun onResume() {
        super.onResume()
        applyAppLanguage(this)
        update(applicationContext, false)
        val today = Jdn.today()
        if (creationDateJdn != today) {
            creationDateJdn = today
        }
    }
}

@Composable
fun App(intentStartDestination: String?, finish: () -> Unit) {
    val calendarRoute = "calendar"
    val compassRoute = "compass"
    val levelRoute = "level"
    val mapRoute = "map"
    val converterRoute = "converter"
    val astronomyRoute = "astronomy"
    val settingsRoute = "settings"
    val aboutRoute = "about"
    val licensesRoute = "license"
    val deviceInformationRoute = "device"
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

    ModalNavigationDrawer(
        gesturesEnabled = when (navBackStackEntry?.destination?.route) {
            // As these screens slider drags and interactions collide with drawer, till a better solution
            astronomyRoute, mapRoute -> false
            else -> true
        },
        drawerState = drawerState,
        drawerContent = {
            val isLandscape =
                LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

            @OptIn(ExperimentalFoundationApi::class) ModalDrawerSheet(
                windowInsets = if (isLandscape) DrawerDefaults.windowInsets
                else WindowInsets(0, 0, 0, 0),
            ) {
                val context = LocalContext.current
                val needsVisibleStatusBarPlaceHolder = remember {
                    SystemBarsTransparency(context).needsVisibleStatusBarPlaceHolder
                }
                Box(
                    if (needsVisibleStatusBarPlaceHolder) Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                0f to Color(0x70000000), 1f to Color.Transparent
                            )
                        )
                    else Modifier
                ) { Box(Modifier.windowInsetsTopHeight(WindowInsets.systemBars)) }

                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    val actualSeason =
                        remember { Season.fromDate(Date(), coordinates.value).ordinal }
                    val pageSize = 200
                    val seasonState = rememberPagerState(
                        initialPage = pageSize / 2 + actualSeason - 3, // minus 3 so it does an initial animation
                        pageCount = { pageSize },
                    )
                    if (drawerState.isOpen) {
                        scope.launch { seasonState.animateScrollToPage(100 + actualSeason) }
                    }
                    val imageFilter = remember(LocalConfiguration.current) {
                        // Consider gray scale themes of Android 14
                        // And apply a gray scale filter https://stackoverflow.com/a/75698731
                        if (Theme.isDynamicColor(context.appPrefs) && context.isDynamicGrayscale) ColorFilter.colorMatrix(
                            ColorMatrix().also { it.setToSaturation(0f) })
                        else null
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

                    listOf(
                        Triple(calendarRoute, Icons.Default.DateRange, R.string.calendar),
                        Triple(
                            converterRoute,
                            Icons.Default.SwapVerticalCircle,
                            R.string.date_converter
                        ),
                        Triple(compassRoute, Icons.Default.Explore, R.string.compass),
                        Triple(
                            astronomyRoute,
                            ImageVector.vectorResource(R.drawable.ic_astrology_horoscope),
                            R.string.astronomy
                        ),
                        Triple(settingsRoute, Icons.Default.Settings, R.string.settings),
                        Triple(aboutRoute, Icons.Default.Info, R.string.about),
                        Triple(null, Icons.Default.Cancel, R.string.exit),
                    ).forEach { (id, icon, title) ->
                        NavigationDrawerItem(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            label = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        icon,
                                        modifier = Modifier.size(24.dp),
                                        contentDescription = null
                                    )
                                    Spacer(Modifier.width(16.dp))
                                    Text(stringResource(title))
                                }
                            },
                            selected = when (val route = navBackStackEntry?.destination?.route) {
                                levelRoute -> compassRoute
                                mapRoute -> astronomyRoute
                                deviceInformationRoute, licensesRoute -> aboutRoute
                                else -> route ?: calendarRoute
                            } == id,
                            onClick = {
                                if (id == null) return@NavigationDrawerItem finish()
                                scope.launch {
                                    drawerState.close()
                                    if (navBackStackEntry?.destination?.route != id) {
                                        navController.navigate(id)
                                    }
                                }
                            },
                        )
                    }
                    Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.systemBars))
                }
            }
        },
    ) {
        NavHost(
            navController = navController,
            startDestination = startDestination,
        ) {
            val tabKey = "TAB"
            val settingsKey = "SETTINGS"
            val daysOffsetKey = "DAYS_OFFSET"
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
                    navigateToSettingsLocationTab = {
                        navController.graph.findNode(settingsRoute)?.let { destination ->
                            navController.navigate(
                                destination.id, bundleOf(tabKey to LOCATION_ATHAN_TAB)
                            )
                        }
                    },
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
                )
            }

            composable(levelRoute) {
                LevelScreen(
                    navigateUp = {
                        if (navController.currentDestination?.route == levelRoute) {
                            navController.navigateUp()
                        }
                    },
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
                    LaunchedEffect(null) {
                        viewModel.state.collectLatest { astronomyViewModel.changeToTime(it.time) }
                    }
                }
                MapScreen(
                    navigateUp = {
                        if (navController.currentDestination?.route == mapRoute) {
                            navController.navigateUp()
                        }
                    },
                    viewModel = viewModel,
                )
            }

            composable(settingsRoute) {
                SettingsScreen(
                    openDrawer = { scope.launch { drawerState.open() } },
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
                LicensesScreen {
                    if (navController.currentDestination?.route == licensesRoute) {
                        navController.navigateUp()
                    }
                }
            }

            composable(deviceInformationRoute) {
                DeviceInformationScreen {
                    if (navController.currentDestination?.route == deviceInformationRoute) {
                        navController.navigateUp()
                    }
                }
            }
        }
    }
}
