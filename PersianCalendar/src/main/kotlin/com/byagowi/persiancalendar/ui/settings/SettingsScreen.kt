package com.byagowi.persiancalendar.ui.settings

import android.app.StatusBarManager
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.byagowi.persiancalendar.BuildConfig
import com.byagowi.persiancalendar.PREF_ATHAN_NAME
import com.byagowi.persiancalendar.PREF_ATHAN_URI
import com.byagowi.persiancalendar.PREF_HAS_EVER_VISITED
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.NumericBinding
import com.byagowi.persiancalendar.service.AlarmWorker
import com.byagowi.persiancalendar.service.PersianCalendarTileService
import com.byagowi.persiancalendar.ui.MainActivity
import com.byagowi.persiancalendar.ui.about.showCarouselDialog
import com.byagowi.persiancalendar.ui.about.showDynamicColorsDialog
import com.byagowi.persiancalendar.ui.about.showIconsDemoDialog
import com.byagowi.persiancalendar.ui.about.showTypographyDemoDialog
import com.byagowi.persiancalendar.ui.settings.interfacecalendar.InterfaceCalendarSettings
import com.byagowi.persiancalendar.ui.settings.locationathan.LocationAthanSettings
import com.byagowi.persiancalendar.ui.settings.widgetnotification.WidgetNotificationSettings
import com.byagowi.persiancalendar.ui.utils.AppBlendAlpha
import com.byagowi.persiancalendar.ui.utils.ExtraLargeShapeCornerSize
import com.byagowi.persiancalendar.ui.utils.MaterialCornerExtraLargeTop
import com.byagowi.persiancalendar.ui.utils.considerSystemBarsInsets
import com.byagowi.persiancalendar.ui.utils.getCompatDrawable
import com.byagowi.persiancalendar.ui.utils.resolveColor
import com.byagowi.persiancalendar.ui.utils.shareTextFile
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.logException
import com.byagowi.persiancalendar.variants.debugAssertNotNull
import com.google.accompanist.themeadapter.material3.Mdc3Theme
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class SettingsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val root = ComposeView(inflater.context)
        val activity = activity ?: return root
        val args by navArgs<SettingsFragmentArgs>()
        root.setContent {
            Mdc3Theme {
                SettingsScreen(
                    activity,
                    args.tab,
                    args.preferenceKey,
                    pickRingtone = { pickRingtone.launch(Unit) },
                )
            }
        }
        return root
    }

    private class PickRingtoneContract : ActivityResultContract<Unit, String?>() {
        override fun createIntent(context: Context, input: Unit): Intent =
            Intent(RingtoneManager.ACTION_RINGTONE_PICKER).putExtra(
                RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALL
            ).putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                .putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true).putExtra(
                    RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI,
                    Settings.System.DEFAULT_NOTIFICATION_URI
                )

        override fun parseResult(resultCode: Int, intent: Intent?): String? =
            if (resultCode == AppCompatActivity.RESULT_OK) intent?.getParcelableExtra<Parcelable?>(
                RingtoneManager.EXTRA_RINGTONE_PICKED_URI
            )?.toString()
            else null
    }

    private val pickRingtone = registerForActivityResult(PickRingtoneContract()) { uri ->
        uri ?: return@registerForActivityResult
        val ringtone = RingtoneManager.getRingtone(context, uri.toUri())
        // If no ringtone has been found better to skip touching preferences store
        ringtone ?: return@registerForActivityResult
        val ringtoneTitle = ringtone.getTitle(context) ?: ""
        context?.appPrefs?.edit {
            putString(PREF_ATHAN_NAME, ringtoneTitle)
            putString(PREF_ATHAN_URI, uri)
        }
        view?.let { view ->
            Snackbar.make(
                view, R.string.custom_notification_is_set, Snackbar.LENGTH_SHORT
            ).also { snackBar -> snackBar.considerSystemBarsInsets() }.show()
        }
    }
}

const val INTERFACE_CALENDAR_TAB = 0
const val WIDGET_NOTIFICATION_TAB = 1
const val LOCATION_ATHAN_TAB = 2

@Composable
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
fun SettingsScreen(
    activity: ComponentActivity,
    initialPage: Int,
    destination: String,
    pickRingtone: () -> Unit,
) = Column {
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
            IconButton(onClick = { (context as? MainActivity)?.openDrawer() }) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = stringResource(R.string.open_drawer)
                )
            }
        },
        actions = {
            var showMenu by remember { mutableStateOf(false) }
            IconButton(onClick = { showMenu = !showMenu }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = stringResource(R.string.more_options),
                )
            }
            DropdownMenu(
                expanded = showMenu, onDismissRequest = { showMenu = false },
            ) { MenuItems(activity) { showMenu = false } }
        },
    )

    val tabs = remember {
        listOf(
            @Composable {
                InterfaceCalendarSettings(activity, destination)
            } to listOf(R.string.pref_interface, R.string.calendar),

            @Composable {
                WidgetNotificationSettings(activity)
            } to listOf(R.string.pref_notification, R.string.pref_widget),

            @Composable {
                LocationAthanSettings(activity, pickRingtone)
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
                TabRowDefaults.Indicator(
                    Modifier
                        .tabIndicatorOffset(tabPositions[selectedTabIndex])
                        .padding(horizontal = ExtraLargeShapeCornerSize.dp),
                    color = Color(context.resolveColor(R.attr.colorOnAppBar))
                        .copy(alpha = AppBlendAlpha),
                    height = 2.dp,
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
            Column(
                Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp),
            ) {
                tabs[index].first()
                Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.systemBars))
            }
        }
    }
}

@Composable
private fun MenuItems(activity: ComponentActivity, closeMenu: () -> Unit) {
    DropdownMenuItem(
        text = { Text(stringResource(R.string.live_wallpaper_settings)) },
        onClick = {
            closeMenu()
            runCatching {
                activity.startActivity(Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER))
            }.onFailure(logException).getOrNull().debugAssertNotNull
        },
    )
    DropdownMenuItem(
        text = { Text(stringResource(R.string.screensaver_settings)) },
        onClick = {
            closeMenu()
            runCatching { activity.startActivity(Intent(Settings.ACTION_DREAM_SETTINGS)) }.onFailure(
                logException
            ).getOrNull().debugAssertNotNull
        },
    )
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        DropdownMenuItem(
            text = { Text(stringResource(R.string.add_quick_settings_tile)) },
            onClick = {
                closeMenu()
                activity.getSystemService<StatusBarManager>()?.requestAddTileService(
                    ComponentName(
                        activity.packageName, PersianCalendarTileService::class.qualifiedName ?: "",
                    ),
                    activity.getString(R.string.app_name),
                    Icon.createWithResource(activity, R.drawable.day19),
                    {},
                    {},
                )
            },
        )
    }

    if (!BuildConfig.DEVELOPMENT) return // Rest are development only functionalities
    DropdownMenuItem(
        text = { Text("Static vs generated icons") },
        onClick = { showIconsDemoDialog(activity) },
    )
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        DropdownMenuItem(
            text = { Text("Dynamic Colors") },
            onClick = { showDynamicColorsDialog(activity) },
        )
    }
    DropdownMenuItem(
        text = { Text("Typography") },
        onClick = { showTypographyDemoDialog(activity) },
    )
    DropdownMenuItem(
        text = { Text("Clear preferences store and exit") },
        onClick = {
            activity.appPrefs.edit { clear() }
            activity.finish()
        },
    )
    DropdownMenuItem(
        text = { Text("Schedule an alarm") },
        onClick = {
            val numericBinding = NumericBinding.inflate(activity.layoutInflater)
            numericBinding.edit.setText("5")
            androidx.appcompat.app.AlertDialog.Builder(activity)
                .setTitle("Enter seconds to schedule alarm").setView(numericBinding.root)
                .setPositiveButton(R.string.accept) { _, _ ->
                    val seconds = numericBinding.edit.text.toString().toLongOrNull() ?: 0L
                    val alarmWorker =
                        OneTimeWorkRequest.Builder(AlarmWorker::class.java).setInitialDelay(
                            TimeUnit.SECONDS.toMillis(seconds), TimeUnit.MILLISECONDS
                        ).build()
                    WorkManager.getInstance(activity).beginUniqueWork(
                        "TestAlarm", ExistingWorkPolicy.REPLACE, alarmWorker
                    ).enqueue()
                    Toast.makeText(activity, "Alarm in ${seconds}s", Toast.LENGTH_SHORT).show()
                }.show()
        },
    )
    fun viewCommandResult(command: String) {
        val dialogBuilder = androidx.appcompat.app.AlertDialog.Builder(activity)
        val result = Runtime.getRuntime().exec(command).inputStream.bufferedReader().readText()
        val button = ImageButton(activity).also { button ->
            button.setImageDrawable(activity.getCompatDrawable(R.drawable.ic_baseline_share))
            button.setOnClickListener {
                activity.shareTextFile(result, "log.txt", "text/plain")
            }
        }
        dialogBuilder.setCustomTitle(LinearLayout(activity).also {
            it.layoutDirection = View.LAYOUT_DIRECTION_LTR
            it.addView(button)
        })
        dialogBuilder.setView(ScrollView(activity).also { scrollView ->
            scrollView.addView(TextView(activity).also {
                it.text = result
                it.textDirection = View.TEXT_DIRECTION_LTR
            })
            // Scroll to bottom, https://stackoverflow.com/a/3080483
            scrollView.post { scrollView.fullScroll(View.FOCUS_DOWN) }
        })
        dialogBuilder.show()
    }
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
                activity.startActivity(
                    Intent(Intent.ACTION_MAIN).setClassName(
                        "com.android.systemui", "com.android.systemui.Somnambulator"
                    )
                )
            }.onFailure(logException).getOrNull().debugAssertNotNull
        },
    )
    DropdownMenuItem(
        text = { Text("Start Carousel") },
        onClick = { showCarouselDialog(activity) },
    )
}
