package com.byagowi.persiancalendar.ui.settings

import android.app.StatusBarManager
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.core.content.getSystemService
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.byagowi.persiancalendar.BuildConfig
import com.byagowi.persiancalendar.PREF_HAS_EVER_VISITED
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.service.AlarmWorker
import com.byagowi.persiancalendar.service.PersianCalendarTileService
import com.byagowi.persiancalendar.ui.about.ColorSchemeDemoDialog
import com.byagowi.persiancalendar.ui.about.DynamicColorsDialog
import com.byagowi.persiancalendar.ui.about.IconsDemoDialog
import com.byagowi.persiancalendar.ui.about.ShapesDemoDialog
import com.byagowi.persiancalendar.ui.about.TypographyDemoDialog
import com.byagowi.persiancalendar.ui.common.AppDialog
import com.byagowi.persiancalendar.ui.settings.interfacecalendar.InterfaceCalendarSettings
import com.byagowi.persiancalendar.ui.settings.locationathan.LocationAthanSettings
import com.byagowi.persiancalendar.ui.settings.widgetnotification.WidgetNotificationSettings
import com.byagowi.persiancalendar.ui.utils.AppBlendAlpha
import com.byagowi.persiancalendar.ui.utils.ExtraLargeShapeCornerSize
import com.byagowi.persiancalendar.ui.utils.MaterialCornerExtraLargeTop
import com.byagowi.persiancalendar.ui.utils.getActivity
import com.byagowi.persiancalendar.ui.utils.resolveColor
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.logException
import com.byagowi.persiancalendar.variants.debugAssertNotNull
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

@Composable
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
fun SettingsScreen(
    openDrawer: () -> Unit,
    initialPage: Int,
    destination: String,
) {
    Column {
        val context = LocalContext.current
        LaunchedEffect(null) { context.appPrefs.edit { putBoolean(PREF_HAS_EVER_VISITED, true) } }
        // TODO: Ideally this should be onPrimary
        val colorOnAppBar = Color(context.resolveColor(R.attr.colorOnAppBar))
        TopAppBar(
            title = { Text(stringResource(R.string.settings)) },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                navigationIconContentColor = colorOnAppBar,
                actionIconContentColor = colorOnAppBar,
                titleContentColor = colorOnAppBar,
            ),
            navigationIcon = {
                IconButton(onClick = { openDrawer() }) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = stringResource(R.string.open_drawer)
                    )
                }
            },
            actions = {
                var showMenu by rememberSaveable { mutableStateOf(false) }
                TooltipBox(
                    positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                    tooltip = {
                        PlainTooltip { Text(text = stringResource(R.string.more_options)) }
                    },
                    state = rememberTooltipState()
                ) {
                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = stringResource(R.string.more_options),
                        )
                    }
                }
                DropdownMenu(
                    expanded = showMenu, onDismissRequest = { showMenu = false },
                ) { MenuItems { showMenu = false } }
            },
        )

        val tabs = remember {
            listOf(
                @Composable {
                    InterfaceCalendarSettings(destination)
                } to listOf(R.string.pref_interface, R.string.calendar),

                @Composable {
                    WidgetNotificationSettings()
                } to listOf(R.string.pref_notification, R.string.pref_widget),

                @Composable {
                    LocationAthanSettings()
                } to listOf(R.string.location, R.string.athan),
            )
        }

        val pagerState = rememberPagerState(initialPage = initialPage, pageCount = tabs::size)
        val scope = rememberCoroutineScope()

        val selectedTabIndex = pagerState.currentPage
        TabRow(
            selectedTabIndex = selectedTabIndex,
            contentColor = Color(context.resolveColor(R.attr.colorOnAppBar)),
            containerColor = Color.Transparent,
            divider = {},
            indicator = @Composable { tabPositions ->
                if (selectedTabIndex < tabPositions.size) {
                    SecondaryIndicator(
                        Modifier
                            .tabIndicatorOffset(tabPositions[selectedTabIndex])
                            .padding(horizontal = ExtraLargeShapeCornerSize.dp),
                        height = 2.dp,
                        color = Color(context.resolveColor(R.attr.colorOnAppBar)).copy(alpha = AppBlendAlpha)
                    )
                }
            },
        ) {
            tabs.forEachIndexed { index, (_, titlesResId) ->
                val title = titlesResId.joinToString(stringResource(R.string.spaced_and)) {
                    context.getString(it)
                }
                Tab(
                    text = { Text(title) },
                    selected = pagerState.currentPage == index,
                    onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.clip(MaterialCornerExtraLargeTop()),
        ) { index ->
            Surface(modifier = Modifier.fillMaxSize()) {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    tabs[index].first()
                    Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.systemBars))
                }
            }
        }
    }
}

const val INTERFACE_CALENDAR_TAB = 0
const val WIDGET_NOTIFICATION_TAB = 1
const val LOCATION_ATHAN_TAB = 2

@Composable
private fun MenuItems(closeMenu: () -> Unit) {
    val context = LocalContext.current
    DropdownMenuItem(
        text = { Text(stringResource(R.string.live_wallpaper_settings)) },
        onClick = {
            closeMenu()
            runCatching {
                context.startActivity(Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER))
            }.onFailure(logException).getOrNull().debugAssertNotNull
        },
    )
    DropdownMenuItem(
        text = { Text(stringResource(R.string.screensaver_settings)) },
        onClick = {
            closeMenu()
            runCatching { context.startActivity(Intent(Settings.ACTION_DREAM_SETTINGS)) }.onFailure(
                logException
            ).getOrNull().debugAssertNotNull
        },
    )
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        DropdownMenuItem(
            text = { Text(stringResource(R.string.add_quick_settings_tile)) },
            onClick = {
                closeMenu()
                context.getSystemService<StatusBarManager>()?.requestAddTileService(
                    ComponentName(
                        context.packageName, PersianCalendarTileService::class.qualifiedName ?: "",
                    ),
                    context.getString(R.string.app_name),
                    Icon.createWithResource(context, R.drawable.day19),
                    {},
                    {},
                )
            },
        )
    }

    if (!BuildConfig.DEVELOPMENT) return // Rest are development only functionalities
    run {
        var showDialog by rememberSaveable { mutableStateOf(false) }
        DropdownMenuItem(
            text = { Text("Static vs generated icons") },
            onClick = { showDialog = true },
        )
        if (showDialog) IconsDemoDialog { showDialog = false }
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        var showDialog by rememberSaveable { mutableStateOf(false) }
        DropdownMenuItem(
            text = { Text("Dynamic Colors") },
            onClick = { showDialog = true },
        )
        if (showDialog) DynamicColorsDialog { showDialog = false }
    }
    run {
        var showDialog by rememberSaveable { mutableStateOf(false) }
        DropdownMenuItem(
            text = { Text("Color Scheme") },
            onClick = { showDialog = true },
        )
        if (showDialog) ColorSchemeDemoDialog { showDialog = false }
    }
    run {
        var showDialog by rememberSaveable { mutableStateOf(false) }
        DropdownMenuItem(
            text = { Text("Typography") },
            onClick = { showDialog = true },
        )
        if (showDialog) TypographyDemoDialog { showDialog = false }
    }
    run {
        var showDialog by rememberSaveable { mutableStateOf(false) }
        DropdownMenuItem(
            text = { Text("Shapes") },
            onClick = { showDialog = true },
        )
        if (showDialog) ShapesDemoDialog { showDialog = false }
    }
    DropdownMenuItem(
        text = { Text("Clear preferences store and exit") },
        onClick = {
            context.appPrefs.edit { clear() }
            context.getActivity()?.finish()
        },
    )
    run {
        var showDialog by remember { mutableStateOf(false) }
        DropdownMenuItem(
            text = { Text("Schedule an alarm") },
            onClick = { showDialog = true },
        )
        if (showDialog) {
            var seconds by remember { mutableStateOf("5") }
            AppDialog(title = { Text("Enter seconds to schedule alarm") }, confirmButton = {
                TextButton(onClick = onClick@{
                    closeMenu()
                    val value = seconds.toIntOrNull() ?: return@onClick
                    val alarmWorker =
                        OneTimeWorkRequest.Builder(AlarmWorker::class.java).setInitialDelay(
                            TimeUnit.SECONDS.toMillis(value.toLong()), TimeUnit.MILLISECONDS
                        ).build()
                    WorkManager.getInstance(context).beginUniqueWork(
                        "TestAlarm", ExistingWorkPolicy.REPLACE, alarmWorker
                    ).enqueue()
                    Toast.makeText(context, "Alarm in ${value}s", Toast.LENGTH_SHORT).show()
                }) { Text(stringResource(R.string.accept)) }
            }, onDismissRequest = { closeMenu() }) {
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = seconds,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    onValueChange = { seconds = it },
                )
            }
        }
    }
//    fun viewCommandResult(command: String) {
//        val dialogBuilder = AlertDialog.Builder(activity)
//        val result = Runtime.getRuntime().exec(command).inputStream.bufferedReader().readText()
//        val button = ImageButton(activity).also { button ->
//            button.setImageDrawable(activity.getCompatDrawable(R.drawable.ic_baseline_share))
//            button.setOnClickListener {
//                activity.shareTextFile(result, "log.txt", "text/plain")
//            }
//        }
//        dialogBuilder.setCustomTitle(LinearLayout(activity).also {
//            it.layoutDirection = View.LAYOUT_DIRECTION_LTR
//            it.addView(button)
//        })
//        dialogBuilder.setView(ScrollView(activity).also { scrollView ->
//            scrollView.addView(TextView(activity).also {
//                it.text = result
//                it.textDirection = View.TEXT_DIRECTION_LTR
//            })
//            // Scroll to bottom, https://stackoverflow.com/a/3080483
//            scrollView.post { scrollView.fullScroll(View.FOCUS_DOWN) }
//        })
//        dialogBuilder.show()
//    }
//    toolbar.menu.addSubMenu("Log Viewer").also {
//        it.add("Filtered").onClick {
//            viewCommandResult("logcat -v raw -t 500 *:S $LOG_TAG:V AndroidRuntime:E")
//        }
//        it.add("Unfiltered").onClick { viewCommandResult("logcat -v raw -t 500") }
//    }
//    toolbar.menu.addSubMenu("Log").also {
//        it.add("Log 'Hello'").onClick { debugLog("Hello!") }
//        it.add("Handled Crash").onClick { logException(Exception("Logged Crash!")) }
//        it.add("Crash!").onClick { error("Unhandled Crash!") }
//    }
    DropdownMenuItem(
        text = { Text("Start Dream") },
        onClick = {
            // https://stackoverflow.com/a/23112947
            runCatching {
                context.startActivity(
                    Intent(Intent.ACTION_MAIN).setClassName(
                        "com.android.systemui", "com.android.systemui.Somnambulator"
                    )
                )
            }.onFailure(logException).getOrNull().debugAssertNotNull
        },
    )
}
