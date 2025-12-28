package com.byagowi.persiancalendar.ui.settings

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.StatusBarManager
import android.app.WallpaperManager
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.content.res.Configuration
import android.graphics.drawable.Icon
import android.os.Build
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.compose.LocalActivity
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.core.content.getSystemService
import com.byagowi.persiancalendar.BuildConfig
import com.byagowi.persiancalendar.DEFAULT_THEME_CYBERPUNK
import com.byagowi.persiancalendar.LOG_TAG
import com.byagowi.persiancalendar.PREF_DYNAMIC_ICON_ENABLED
import com.byagowi.persiancalendar.PREF_THEME_CYBERPUNK
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.global.coordinates
import com.byagowi.persiancalendar.global.customImageName
import com.byagowi.persiancalendar.global.isCyberpunk
import com.byagowi.persiancalendar.global.isDynamicIconEnabled
import com.byagowi.persiancalendar.global.isTalkBackEnabled
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.service.PersianCalendarTileService
import com.byagowi.persiancalendar.service.PersianCalendarWallpaperService
import com.byagowi.persiancalendar.ui.about.ColorSchemeDemoDialog
import com.byagowi.persiancalendar.ui.about.ConverterDialog
import com.byagowi.persiancalendar.ui.about.DynamicColorsDialog
import com.byagowi.persiancalendar.ui.about.FontWeightsDialog
import com.byagowi.persiancalendar.ui.about.IconsDemoDialog
import com.byagowi.persiancalendar.ui.about.ScheduleAlarm
import com.byagowi.persiancalendar.ui.about.ShapesDemoDialog
import com.byagowi.persiancalendar.ui.about.TypographyDemoDialog
import com.byagowi.persiancalendar.ui.common.AppDropdownMenuCheckableItem
import com.byagowi.persiancalendar.ui.common.AppDropdownMenuItem
import com.byagowi.persiancalendar.ui.common.NavigationOpenNavigationRailIcon
import com.byagowi.persiancalendar.ui.common.ScreenSurface
import com.byagowi.persiancalendar.ui.common.ScrollShadow
import com.byagowi.persiancalendar.ui.common.ThreeDotsDropdownMenu
import com.byagowi.persiancalendar.ui.settings.interfacecalendar.CalendarSettings
import com.byagowi.persiancalendar.ui.settings.interfacecalendar.InterfaceSettings
import com.byagowi.persiancalendar.ui.settings.locationathan.AthanSettings
import com.byagowi.persiancalendar.ui.settings.locationathan.LocationSettings
import com.byagowi.persiancalendar.ui.settings.wallpaper.WallpaperConfigurationActivity
import com.byagowi.persiancalendar.ui.settings.widgetnotification.AddWidgetDialog
import com.byagowi.persiancalendar.ui.settings.widgetnotification.NotificationSettings
import com.byagowi.persiancalendar.ui.settings.widgetnotification.WidgetSettings
import com.byagowi.persiancalendar.ui.theme.appCrossfadeSpec
import com.byagowi.persiancalendar.ui.theme.appTopAppBarColors
import com.byagowi.persiancalendar.ui.utils.AppBlendAlpha
import com.byagowi.persiancalendar.ui.utils.shareTextFile
import com.byagowi.persiancalendar.utils.debugAssertNotNull
import com.byagowi.persiancalendar.utils.debugLog
import com.byagowi.persiancalendar.utils.logException
import com.byagowi.persiancalendar.utils.preferences
import com.byagowi.persiancalendar.utils.showUnsupportedActionToast
import com.byagowi.persiancalendar.utils.supportsDynamicIcon
import kotlinx.coroutines.launch

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SharedTransitionScope.SettingsScreen(
    openNavigationRail: () -> Unit,
    navigateToMap: () -> Unit,
    initialTab: SettingsTab,
    destination: String?,
    destinationItem: String?,
) {
//    var isAtTop by remember { mutableStateOf(true) }
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            Column(Modifier.windowInsetsPadding(TopAppBarDefaults.windowInsets)) {
//                AnimatedVisibility(isAtTop) {
                TopAppBar(
                    windowInsets = WindowInsets(),
                    title = {
                        AnimatedContent(
                            targetState = stringResource(R.string.settings),
                            label = "title",
                            transitionSpec = appCrossfadeSpec,
                        ) { state -> Text(state) }
                    },
                    colors = appTopAppBarColors(),
                    navigationIcon = {
                        NavigationOpenNavigationRailIcon(openNavigationRail)
                    },
                    actions = {
                        var showAddWidgetDialog by rememberSaveable { mutableStateOf(false) }
                        Box(
                            Modifier
                                .semantics(mergeDescendants = true) { this.hideFromAccessibility() }
                                .clearAndSetSemantics {},
                        ) {
                            ThreeDotsDropdownMenu { closeMenu ->
                                MenuItems(
                                    openAddWidgetDialog = {
                                        closeMenu(); showAddWidgetDialog = true
                                    },
                                    closeMenu = closeMenu,
                                )
                            }
                        }
                        if (showAddWidgetDialog) AddWidgetDialog { showAddWidgetDialog = false }
                    },
                )
//                }
            }
        },
    ) { paddingValues ->
        Column(Modifier.padding(top = paddingValues.calculateTopPadding())) {
            val pagerState = rememberPagerState(
                initialPage = initialTab.ordinal,
                pageCount = SettingsTab.entries::size,
            )
            val coroutineScope = rememberCoroutineScope()

            PrimaryTabRow(
                selectedTabIndex = pagerState.currentPage,
                contentColor = LocalContentColor.current,
                containerColor = Color.Transparent,
                divider = {},
                indicator = {
                    val isLandscape =
                        LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
                    TabRowDefaults.PrimaryIndicator(
                        Modifier.tabIndicatorOffset(pagerState.currentPage),
                        width = if (isLandscape) 92.dp else 64.dp,
                        color = LocalContentColor.current.copy(alpha = AppBlendAlpha),
                    )
                },
            ) {
                SettingsTab.entries.forEachIndexed { index, tab ->
                    val isLandscape =
                        LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
                    val modifier = Modifier.clip(MaterialTheme.shapes.large)
                    val isSelected = pagerState.currentPage == index
                    fun onClick() {
                        coroutineScope.launch { pagerState.animateScrollToPage(index) }
                    }
                    if (isLandscape) Tab(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                tab.Icon(isSelected)
                                Spacer(Modifier.width(8.dp))
                                tab.Title()
                            }
                        },
                        modifier = modifier,
                        selected = isSelected,
                        onClick = ::onClick,
                    ) else Tab(
                        icon = { tab.Icon(isSelected) },
                        text = { tab.Title() },
                        modifier = modifier,
                        selected = isSelected,
                        onClick = ::onClick,
                    )
                }
            }

            ScreenSurface {
                val disableStickyHeader = isTalkBackEnabled || customImageName != null
                HorizontalPager(state = pagerState) { index ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .windowInsetsPadding(
                                WindowInsets.displayCutout.only(WindowInsetsSides.Horizontal),
                            ),
                    ) {
                        val listState = rememberLazyListState()
                        LazyColumn(
                            state = listState,
                            contentPadding = PaddingValues(bottom = paddingValues.calculateBottomPadding()),
                        ) {
                            SettingsTab.entries.getOrNull(index)?.content(
                                this@LazyColumn,
                                listState,
                                disableStickyHeader,
                                destination,
                                destinationItem,
                                navigateToMap,
                            )
                        }
//                        if (pagerState.currentPage == index) {
//                            isAtTop = !listState.canScrollBackward
//                        }
                        ScrollShadow(listState)
                    }
                }
            }
        }
    }
}

enum class SettingsTab(
    private val outlinedIcon: ImageVector,
    private val filledIcon: ImageVector,
    @get:StringRes private val firstTitle: Int,
    @get:StringRes private val secondTitle: Int,
    val content: LazyListScope.(
        listState: LazyListState,
        disableStickyHeader: Boolean,
        destination: String?,
        destinationItem: String?,
        navigateToMap: () -> Unit,
    ) -> Unit,
) {
    InterfaceCalendar(
        outlinedIcon = Icons.Outlined.Palette,
        filledIcon = Icons.Default.Palette,
        firstTitle = R.string.pref_interface,
        secondTitle = R.string.calendar,
        content = { listState, disableStickyHeader, destination, destinationItem, _ ->
            settingsSection(
                canScrollBackward = listState.canScrollBackward,
                disableStickyHeader = disableStickyHeader,
                title = R.string.pref_ui,
            ) { InterfaceSettings(destination) }
            settingsSection(
                canScrollBackward = listState.canScrollBackward,
                disableStickyHeader = disableStickyHeader,
                title = R.string.calendar,
            ) { CalendarSettings(destination, destinationItem) }
        },
    ),
    WidgetNotification(
        outlinedIcon = Icons.Outlined.Widgets,
        filledIcon = Icons.Default.Widgets,
        firstTitle = R.string.pref_notification,
        secondTitle = R.string.pref_widget,
        content = { listState, disableStickyHeader, _, _, _ ->
            settingsSection(
                canScrollBackward = listState.canScrollBackward,
                disableStickyHeader = disableStickyHeader,
                title = R.string.pref_notification,
            ) { NotificationSettings() }
            settingsSection(
                canScrollBackward = listState.canScrollBackward,
                disableStickyHeader = disableStickyHeader,
                title = R.string.pref_widget,
            ) { WidgetSettings() }
        },
    ),
    LocationAthan(
        outlinedIcon = Icons.Outlined.LocationOn,
        filledIcon = Icons.Default.LocationOn,
        firstTitle = R.string.location,
        secondTitle = R.string.athan,
        content = { listState, disableStickyHeader, destination, _, navigateToMap ->
            settingsSection(
                canScrollBackward = listState.canScrollBackward,
                disableStickyHeader = disableStickyHeader,
                title = R.string.location,
            ) { LocationSettings(navigateToMap) }
            settingsSection(
                canScrollBackward = listState.canScrollBackward,
                disableStickyHeader = disableStickyHeader,
                title = R.string.athan,
                subtitle = {
                    if (coordinates == null) stringResource(R.string.athan_disabled_summary) else null
                },
            ) { AthanSettings(destination) }
        },
    );

    @Composable
    fun Title() {
        Text(
            stringResource(firstTitle) + stringResource(R.string.spaced_and) + stringResource(
                secondTitle,
            ),
        )
    }

    @Composable
    fun Icon(isSelected: Boolean) {
        Crossfade(isSelected, label = "icon") {
            Icon(if (it) filledIcon else outlinedIcon, contentDescription = null)
        }
    }
}

@Composable
private fun MenuItems(openAddWidgetDialog: () -> Unit, closeMenu: () -> Unit) {
    val context = LocalContext.current
    val resources = LocalResources.current
    AppDropdownMenuItem(
        text = { Text(stringResource(R.string.live_wallpaper_settings)) },
        trailingIcon = {
            val componentName = ComponentName(context, PersianCalendarWallpaperService::class.java)
            val isCurrent = runCatching {
                WallpaperManager.getInstance(context)?.wallpaperInfo?.component == componentName
            }.getOrNull() ?: false
            if (BuildConfig.DEVELOPMENT || isCurrent) Box(
                Modifier.clickable {
                    closeMenu()
                    runCatching {
                        val intent = if (isCurrent) Intent(
                            context,
                            WallpaperConfigurationActivity::class.java,
                        ) else Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).putExtra(
                            WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                            componentName,
                        )
                        context.startActivity(intent)
                    }.onFailure(logException).onFailure { showUnsupportedActionToast(context) }
                },
            ) {
                val icon = if (isCurrent) Icons.Default.Settings else Icons.Default.Check
                Icon(imageVector = icon, contentDescription = stringResource(R.string.accept))
            }
        },
    ) {
        closeMenu()
        runCatching {
            context.startActivity(Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER))
        }.onFailure(logException).onFailure { showUnsupportedActionToast(context) }
    }
    AppDropdownMenuItem({ Text(stringResource(R.string.screensaver_settings)) }) {
        closeMenu()
        runCatching {
            context.startActivity(Intent(Settings.ACTION_DREAM_SETTINGS))
        }.onFailure(logException).onFailure { showUnsupportedActionToast(context) }
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        AppDropdownMenuItem({ Text(stringResource(R.string.add_quick_settings_tile)) }) {
            closeMenu()
            context.getSystemService<StatusBarManager>()?.requestAddTileService(
                ComponentName(
                    context.packageName, PersianCalendarTileService::class.qualifiedName.orEmpty(),
                ),
                resources.getString(R.string.app_name),
                Icon.createWithResource(context, R.drawable.day19),
                {},
                {},
            )
        }
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && runCatching {
            AppWidgetManager.getInstance(
                context,
            ).isRequestPinAppWidgetSupported
        }.getOrNull() == true) {
        AppDropdownMenuItem(
            text = { Text(stringResource(R.string.add_widget)) },
            onClick = openAddWidgetDialog,
        )
    }
    if (supportsDynamicIcon(mainCalendar, language)) AppDropdownMenuCheckableItem(
        text = stringResource(R.string.dynamic_icon),
        isChecked = isDynamicIconEnabled,
    ) {
        closeMenu()
        context.preferences.edit { putBoolean(PREF_DYNAMIC_ICON_ENABLED, !isDynamicIconEnabled) }
    }

    if (!BuildConfig.DEVELOPMENT) return // Rest are development only functionalities
    run {
        var showDialog by rememberSaveable { mutableStateOf(false) }
        AppDropdownMenuItem({ Text("Static vs generated icons") }) { showDialog = true }
        if (showDialog) IconsDemoDialog { showDialog = false }
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        var showDialog by rememberSaveable { mutableStateOf(false) }
        AppDropdownMenuItem({ Text("Dynamic Colors") }) { showDialog = true }
        if (showDialog) DynamicColorsDialog { showDialog = false }
    }
    run {
        var showDialog by rememberSaveable { mutableStateOf(false) }
        AppDropdownMenuItem({ Text("Color Scheme") }) { showDialog = true }
        if (showDialog) ColorSchemeDemoDialog { showDialog = false }
    }
    run {
        var showDialog by rememberSaveable { mutableStateOf(false) }
        AppDropdownMenuItem({ Text("Typography") }) { showDialog = true }
        if (showDialog) TypographyDemoDialog { showDialog = false }
    }
    run {
        var showDialog by rememberSaveable { mutableStateOf(false) }
        AppDropdownMenuItem({ Text("Shapes") }) { showDialog = true }
        if (showDialog) ShapesDemoDialog { showDialog = false }
    }
    run {
        var showDialog by rememberSaveable { mutableStateOf(false) }
        AppDropdownMenuItem({ Text("Font Weights") }) { showDialog = true }
        if (showDialog) FontWeightsDialog { showDialog = false }
    }
    AppDropdownMenuCheckableItem(
        text = "Cyberpunk",
        isChecked = isCyberpunk,
        onValueChange = {
            val preferences = context.preferences
            preferences.edit {
                putBoolean(
                    PREF_THEME_CYBERPUNK,
                    !preferences.getBoolean(PREF_THEME_CYBERPUNK, DEFAULT_THEME_CYBERPUNK),
                )
            }
            closeMenu()
        },
    )
    val activity = LocalActivity.current
    AppDropdownMenuItem({ Text("Clear preferences store and exit") }) {
        context.preferences.edit { clear() }
        activity?.finish()
    }
    run {
        var showDialog by rememberSaveable { mutableStateOf(false) }
        AppDropdownMenuItem({ Text("Schedule an alarm") }) { showDialog = true }
        if (showDialog) ScheduleAlarm { showDialog = false }
    }
    run {
        var showDialog by rememberSaveable { mutableStateOf(false) }
        AppDropdownMenuItem({ Text("Converter") }) { showDialog = true }
        if (showDialog) ConverterDialog { showDialog = false }
    }

    HorizontalDivider()

    fun viewCommandResult(command: String) {
        val dialogBuilder = AlertDialog.Builder(context)
        val result = Runtime.getRuntime().exec(command).inputStream.bufferedReader().readText()
        val button = Button(context).also { button ->
            @SuppressLint("SetTextI18n") run { button.text = "Share" }
            button.setOnClickListener {
                context.shareTextFile(result, "log.txt", "text/plain")
            }
        }
        dialogBuilder.setCustomTitle(
            LinearLayout(context).also {
                it.layoutDirection = View.LAYOUT_DIRECTION_LTR
                it.addView(button)
            },
        )
        dialogBuilder.setView(
            ScrollView(context).also { scrollView ->
                scrollView.addView(
                    TextView(context).also {
                        it.text = result
                        it.textDirection = View.TEXT_DIRECTION_LTR
                    },
                )
                // Scroll to bottom, https://stackoverflow.com/a/3080483
                scrollView.post { scrollView.fullScroll(View.FOCUS_DOWN) }
            },
        )
        dialogBuilder.show()
    }
    listOf(
        "Filtered Log Viewer" to "logcat -v raw -t 500 *:S $LOG_TAG:V AndroidRuntime:E",
        "Unfiltered Log Viewer" to "logcat -v raw -t 500",
    ).forEach { (title, command) ->
        AppDropdownMenuItem({ Text(title) }) { viewCommandResult(command) }
    }

    HorizontalDivider()

    listOf(
        "Log 'Hello'" to { debugLog("Hello!") },
        "Handled Crash" to { logException(Exception("Logged Crash!")) },
        // "Log 'Hello'" to { error("Unhandled Crash!") }
    ).forEach { (text, action) -> AppDropdownMenuItem(text = { Text(text) }, onClick = action) }

    HorizontalDivider()

    AppDropdownMenuItem({ Text("Start Dream") }) {
        // https://stackoverflow.com/a/23112947
        runCatching {
            context.startActivity(
                Intent(Intent.ACTION_MAIN).setClassName(
                    "com.android.systemui", "com.android.systemui.Somnambulator",
                ),
            )
        }.onFailure(logException).getOrNull().debugAssertNotNull
    }
}
