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
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.edit
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.fragment.navArgs
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.byagowi.persiancalendar.BuildConfig
import com.byagowi.persiancalendar.LOG_TAG
import com.byagowi.persiancalendar.PREF_ATHAN_NAME
import com.byagowi.persiancalendar.PREF_ATHAN_URI
import com.byagowi.persiancalendar.PREF_HAS_EVER_VISITED
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.AppBarBinding
import com.byagowi.persiancalendar.databinding.NumericBinding
import com.byagowi.persiancalendar.service.AlarmWorker
import com.byagowi.persiancalendar.service.PersianCalendarTileService
import com.byagowi.persiancalendar.ui.about.showCarouselDialog
import com.byagowi.persiancalendar.ui.about.showDynamicColorsDialog
import com.byagowi.persiancalendar.ui.about.showIconsDemoDialog
import com.byagowi.persiancalendar.ui.about.showTypographyDemoDialog
import com.byagowi.persiancalendar.ui.settings.interfacecalendar.InterfaceCalendarSettings
import com.byagowi.persiancalendar.ui.settings.locationathan.LocationAthanSettings
import com.byagowi.persiancalendar.ui.settings.widgetnotification.WidgetNotificationSettings
import com.byagowi.persiancalendar.ui.utils.ExtraLargeShapeCornerSize
import com.byagowi.persiancalendar.ui.utils.MaterialCornerExtraLargeTop
import com.byagowi.persiancalendar.ui.utils.considerSystemBarsInsets
import com.byagowi.persiancalendar.ui.utils.getCompatDrawable
import com.byagowi.persiancalendar.ui.utils.layoutInflater
import com.byagowi.persiancalendar.ui.utils.onClick
import com.byagowi.persiancalendar.ui.utils.resolveColor
import com.byagowi.persiancalendar.ui.utils.setupMenuNavigation
import com.byagowi.persiancalendar.ui.utils.shareTextFile
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.logException
import com.byagowi.persiancalendar.variants.debugAssertNotNull
import com.byagowi.persiancalendar.variants.debugLog
import com.google.accompanist.themeadapter.material3.Mdc3Theme
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class SettingsScreen : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val root = ComposeView(inflater.context)
        val activity = activity ?: return root
        val args by navArgs<SettingsScreenArgs>()
        root.setContent {
            Mdc3Theme {
                Column {
                    SettingsScreenContent(
                        activity, viewLifecycleOwner, args.tab, args.preferenceKey, pickRingtone
                    )
                }
            }
        }
        root.post { root.context.appPrefs.edit { putBoolean(PREF_HAS_EVER_VISITED, true) } }
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

    companion object {
        const val INTERFACE_CALENDAR_TAB = 0
        const val WIDGET_NOTIFICATION_TAB = 1
        const val LOCATION_ATHAN_TAB = 2
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun SettingsScreenContent(
    activity: FragmentActivity,
    viewLifecycleOwner: LifecycleOwner,
    initialPage: Int,
    destination: String,
    pickRingtone: ActivityResultLauncher<Unit>,
) {
    Spacer(Modifier.windowInsetsTopHeight(WindowInsets.safeDrawing))

    AndroidView(modifier = Modifier.fillMaxWidth(), factory = {
        val appBar = AppBarBinding.inflate(it.layoutInflater)
        appBar.toolbar.setTitle(R.string.settings)
        appBar.toolbar.setupMenuNavigation()
        setupMenu(activity, appBar.toolbar)
        appBar.root
    })

    val tabs = remember {
        listOf(
            @Composable {
                InterfaceCalendarSettings(activity, destination)
            } to listOf(R.string.pref_interface, R.string.calendar),

            @Composable {
                WidgetNotificationSettings(activity)
            } to listOf(R.string.pref_notification, R.string.pref_widget),

            @Composable {
                LocationAthanSettings(activity, viewLifecycleOwner, pickRingtone)
            } to listOf(R.string.location, R.string.athan),
        )
    }

    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = tabs::size)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

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
                    color = MaterialTheme.colorScheme.surface,
                )
            }
        },
    ) {
        tabs.forEachIndexed { index, (_, titlesResId) ->
            val title = titlesResId.joinToString(stringResource(R.string.spaced_and)) {
                context.getString(it)
            }
            Tab(text = { Text(title) },
                selected = pagerState.currentPage == index,
                onClick = { scope.launch { pagerState.animateScrollToPage(index) } })
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

private fun setupMenu(activity: FragmentActivity?, toolbar: Toolbar) {
    activity ?: return
    toolbar.menu.add(R.string.live_wallpaper_settings).onClick {
        runCatching {
            activity.startActivity(Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER))
        }.onFailure(logException).getOrNull().debugAssertNotNull
    }
    toolbar.menu.add(R.string.screensaver_settings).onClick {
        runCatching { activity.startActivity(Intent(Settings.ACTION_DREAM_SETTINGS)) }
            .onFailure(logException).getOrNull().debugAssertNotNull
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        toolbar.menu.add(R.string.add_quick_settings_tile).onClick {
            val context = toolbar.context
            context.getSystemService<StatusBarManager>()?.requestAddTileService(
                ComponentName(
                    context.packageName,
                    PersianCalendarTileService::class.qualifiedName ?: ""
                ),
                context.getString(R.string.app_name),
                Icon.createWithResource(context, R.drawable.day19),
                {},
                {}
            )
        }
    }

    if (!BuildConfig.DEVELOPMENT) return // Rest are development only functionalities
    toolbar.menu.add("Static vs generated icons").onClick { showIconsDemoDialog(activity) }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        toolbar.menu.add("Dynamic Colors").onClick { showDynamicColorsDialog(activity) }
    }
    toolbar.menu.add("Typography").onClick { showTypographyDemoDialog(activity) }
    toolbar.menu.add("Clear preferences store and exit").onClick {
        activity.appPrefs.edit { clear() }
        activity.finish()
    }
    toolbar.menu.add("Schedule an alarm").onClick {
        val numericBinding = NumericBinding.inflate(activity.layoutInflater)
        numericBinding.edit.setText("5")
        MaterialAlertDialogBuilder(activity)
            .setTitle("Enter seconds to schedule alarm")
            .setView(numericBinding.root)
            .setPositiveButton(R.string.accept) { _, _ ->
                val seconds = numericBinding.edit.text.toString().toLongOrNull() ?: 0L
                val alarmWorker = OneTimeWorkRequest.Builder(AlarmWorker::class.java)
                    .setInitialDelay(
                        TimeUnit.SECONDS.toMillis(seconds), TimeUnit.MILLISECONDS
                    )
                    .build()
                WorkManager.getInstance(activity)
                    .beginUniqueWork(
                        "TestAlarm", ExistingWorkPolicy.REPLACE, alarmWorker
                    )
                    .enqueue()
                Toast.makeText(activity, "Alarm in ${seconds}s", Toast.LENGTH_SHORT)
                    .show()
            }
            .show()
    }
    fun viewCommandResult(command: String) {
        val dialogBuilder = MaterialAlertDialogBuilder(activity)
        val result = Runtime.getRuntime().exec(command).inputStream.bufferedReader().readText()
        val button = ImageButton(activity).also { button ->
            button.setImageDrawable(activity.getCompatDrawable(R.drawable.ic_baseline_share))
            button.setOnClickListener {
                activity.shareTextFile(result, "log.txt", "text/plain")
            }
        }
        dialogBuilder.setCustomTitle(
            LinearLayout(activity).also {
                it.layoutDirection = View.LAYOUT_DIRECTION_LTR
                it.addView(button)
            }
        )
        dialogBuilder.setView(
            ScrollView(activity).also { scrollView ->
                scrollView.addView(TextView(activity).also {
                    it.text = result
                    it.textDirection = View.TEXT_DIRECTION_LTR
                })
                // Scroll to bottom, https://stackoverflow.com/a/3080483
                scrollView.post { scrollView.fullScroll(View.FOCUS_DOWN) }
            }
        )
        dialogBuilder.show()
    }
    toolbar.menu.addSubMenu("Log Viewer").also {
        it.add("Filtered").onClick {
            viewCommandResult("logcat -v raw -t 500 *:S $LOG_TAG:V AndroidRuntime:E")
        }
        it.add("Unfiltered").onClick { viewCommandResult("logcat -v raw -t 500") }
    }
    toolbar.menu.addSubMenu("Log").also {
        it.add("Log 'Hello'").onClick { debugLog("Hello!") }
        it.add("Handled Crash").onClick { logException(Exception("Logged Crash!")) }
        it.add("Crash!").onClick { error("Unhandled Crash!") }
    }
    toolbar.menu.add("Start Dream").onClick {
        // https://stackoverflow.com/a/23112947
        runCatching {
            activity.startActivity(
                Intent(Intent.ACTION_MAIN).setClassName(
                    "com.android.systemui",
                    "com.android.systemui.Somnambulator"
                )
            )
        }.onFailure(logException).getOrNull().debugAssertNotNull
    }
    toolbar.menu.add("Start Carousel").onClick { showCarouselDialog(activity) }
}
