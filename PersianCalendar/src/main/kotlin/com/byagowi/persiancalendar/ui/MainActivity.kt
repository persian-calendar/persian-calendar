package com.byagowi.persiancalendar.ui

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.IdRes
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.navOptions
import com.byagowi.persiancalendar.CALENDAR_READ_PERMISSION_REQUEST_CODE
import com.byagowi.persiancalendar.DEFAULT_NOTIFY_DATE
import com.byagowi.persiancalendar.DEFAULT_THEME_GRADIENT
import com.byagowi.persiancalendar.LAST_CHOSEN_TAB_KEY
import com.byagowi.persiancalendar.POST_NOTIFICATION_PERMISSION_REQUEST_CODE_ENABLE_ATHAN_NOTIFICATION
import com.byagowi.persiancalendar.POST_NOTIFICATION_PERMISSION_REQUEST_CODE_ENABLE_CALENDAR_NOTIFICATION
import com.byagowi.persiancalendar.PREF_APP_LANGUAGE
import com.byagowi.persiancalendar.PREF_EASTERN_GREGORIAN_ARABIC_MONTHS
import com.byagowi.persiancalendar.PREF_ENGLISH_GREGORIAN_PERSIAN_MONTHS
import com.byagowi.persiancalendar.PREF_HAS_EVER_VISITED
import com.byagowi.persiancalendar.PREF_ISLAMIC_OFFSET
import com.byagowi.persiancalendar.PREF_ISLAMIC_OFFSET_SET_DATE
import com.byagowi.persiancalendar.PREF_LAST_APP_VISIT_VERSION
import com.byagowi.persiancalendar.PREF_MIDNIGHT_METHOD
import com.byagowi.persiancalendar.PREF_NOTIFICATION_ATHAN
import com.byagowi.persiancalendar.PREF_NOTIFY_DATE
import com.byagowi.persiancalendar.PREF_PRAY_TIME_METHOD
import com.byagowi.persiancalendar.PREF_SHOW_DEVICE_CALENDAR_EVENTS
import com.byagowi.persiancalendar.PREF_THEME
import com.byagowi.persiancalendar.PREF_THEME_GRADIENT
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.MainActivityBinding
import com.byagowi.persiancalendar.entities.CalendarType
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.entities.Season
import com.byagowi.persiancalendar.global.configureCalendarsAndLoadEvents
import com.byagowi.persiancalendar.global.coordinates
import com.byagowi.persiancalendar.global.initGlobal
import com.byagowi.persiancalendar.global.isIranHolidaysEnabled
import com.byagowi.persiancalendar.global.isShowDeviceCalendarEvents
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.loadLanguageResources
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.global.updateStoredPreference
import com.byagowi.persiancalendar.service.ApplicationService
import com.byagowi.persiancalendar.ui.calendar.CalendarFragmentDirections
import com.byagowi.persiancalendar.ui.theme.AppTheme
import com.byagowi.persiancalendar.ui.theme.Theme
import com.byagowi.persiancalendar.ui.utils.SystemBarsTransparency
import com.byagowi.persiancalendar.ui.utils.askForCalendarPermission
import com.byagowi.persiancalendar.ui.utils.isDynamicGrayscale
import com.byagowi.persiancalendar.ui.utils.isRtl
import com.byagowi.persiancalendar.ui.utils.navigateSafe
import com.byagowi.persiancalendar.ui.utils.resolveColor
import com.byagowi.persiancalendar.ui.utils.transparentSystemBars
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.applyAppLanguage
import com.byagowi.persiancalendar.utils.enableDeviceCalendar
import com.byagowi.persiancalendar.utils.putJdn
import com.byagowi.persiancalendar.utils.readAndStoreDeviceCalendarEventsOfTheDay
import com.byagowi.persiancalendar.utils.startWorker
import com.byagowi.persiancalendar.utils.supportedYearOfIranCalendar
import com.byagowi.persiancalendar.utils.update
import com.byagowi.persiancalendar.variants.debugAssertNotNull
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Date
import kotlin.math.roundToInt


/**
 * Program activity for android
 */
class MainActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener,
    NavController.OnDestinationChangedListener {

    private var creationDateJdn = Jdn.today()
    private var settingHasChanged = false
    private lateinit var binding: MainActivityBinding

    private val onBackPressedCloseDrawerCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() = binding.root.closeDrawer(GravityCompat.START)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Theme.apply(this)
        applyAppLanguage(this)
        super.onCreate(savedInstanceState)
        transparentSystemBars()

        onBackPressedDispatcher.addCallback(this, onBackPressedCloseDrawerCallback)
        initGlobal(this)

        startWorker(this)

        readAndStoreDeviceCalendarEventsOfTheDay(applicationContext)
        update(applicationContext, false)

        binding = MainActivityBinding.inflate(layoutInflater).also {
            setContentView(it.root)
        }
        ensureDirectionality()
        setNavHostBackground()

        binding.root.addDrawerListener(createDrawerListener())

        binding.navigation.setContent {
            AppTheme {
                AppDrawer(
                    isDrawerOpen,
                    selectedMenuItem,
                ) { id ->
                    when (id) {
                        R.id.exit -> finish()
                        else -> {
                            binding.root.closeDrawer(GravityCompat.START)
                            if (navHostFragment?.navController?.currentDestination?.id != id) {
                                clickedItem = id
                            }
                            selectedMenuItem.value = id
                            applyAppLanguage(this)
                        }
                    }
                }
            }
        }

        navHostFragment?.navController?.addOnDestinationChangedListener(this)
        when (intent?.action) {
            "COMPASS" -> R.id.compass
            "LEVEL" -> R.id.level
            "MAP" -> R.id.map
            "CONVERTER" -> R.id.converter
            "ASTRONOMY" -> R.id.astronomy
            "SETTINGS" -> R.id.settings
            else -> null // unsupported action. ignore
        }?.also {
            navigateTo(it)
            // So it won't happen again if the activity is restarted
            intent?.action = ""
        }

        appPrefs.registerOnSharedPreferenceChangeListener(this)

        if (isShowDeviceCalendarEvents && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.READ_CALENDAR
            ) != PackageManager.PERMISSION_GRANTED
        ) askForCalendarPermission()

        if (mainCalendar == CalendarType.SHAMSI && isIranHolidaysEnabled && creationDateJdn.toPersianDate().year > supportedYearOfIranCalendar) {
            Toast.makeText(
                binding.root.context, getString(R.string.outdated_app), Toast.LENGTH_LONG
            ).show() // it.setAction(getString(R.string.update)) { bringMarketPage() }
        }

        applyAppLanguage(this)

        previousAppThemeValue = appPrefs.getString(PREF_THEME, null)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { root, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            root.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                leftMargin = insets.left
                rightMargin = insets.right
            }
            val transparencyState = SystemBarsTransparency(this@MainActivity)
            binding.navigation.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = if (transparencyState.shouldStatusBarBeTransparent) 0 else insets.top
                bottomMargin =
                    if (transparencyState.shouldNavigationBarBeTransparent) 0 else insets.bottom
            }
            windowInsets
        }
    }

    private val selectedMenuItem = MutableStateFlow(R.id.calendar)
    private val isDrawerOpen = MutableStateFlow(false)
    private var drawerOpenedOnce = false

    private fun setNavHostBackground() {
        if (!appPrefs.getBoolean(
                PREF_THEME_GRADIENT, DEFAULT_THEME_GRADIENT
            )
        ) binding.navHostFragment.setBackgroundColor(resolveColor(R.attr.screenBackgroundColor))
        else binding.navHostFragment.setBackgroundResource(R.drawable.gradient_background)
    }

    // This shouldn't be needed but as a the last resort
    private fun ensureDirectionality() {
        binding.root.layoutDirection =
            if (language.isArabicScript) View.LAYOUT_DIRECTION_RTL // just in case resources isn't correct
            else resources.configuration.layoutDirection
    }

    private var previousAppThemeValue: String? = null

    private val navHostFragment by lazy {
        (supportFragmentManager.findFragmentById(R.id.navHostFragment) as? NavHostFragment).debugAssertNotNull
    }

    override fun onDestinationChanged(
        controller: NavController, destination: NavDestination, arguments: Bundle?
    ) {
        selectedMenuItem.value = when (destination.id) {
            // We don't have a menu entry for compass, so
            R.id.level -> R.id.compass
            else -> destination.id
        }

        if (settingHasChanged) { // update when checked menu item is changed
            applyAppLanguage(this)
            update(applicationContext, true)
            settingHasChanged = false // reset for the next time
        }
    }

    private fun navigateTo(@IdRes id: Int) {
        navHostFragment?.navController?.navigate(id, null, navOptions {
            anim {
                enter = R.anim.nav_enter_anim
                exit = R.anim.nav_exit_anim
                popEnter = R.anim.nav_enter_anim
                popExit = R.anim.nav_exit_anim
            }
        })
    }

    override fun onSharedPreferenceChanged(prefs: SharedPreferences?, key: String?) {
        settingHasChanged = true

        prefs ?: return

        // If it is the first initiation of preference, don't call the rest multiple times
        if (key == PREF_HAS_EVER_VISITED || PREF_HAS_EVER_VISITED !in prefs) return

        when (key) {
            PREF_LAST_APP_VISIT_VERSION -> return // nothing needs to be updated
            LAST_CHOSEN_TAB_KEY -> return // don't run the expensive update and etc on tab changes
            PREF_ISLAMIC_OFFSET -> prefs.edit { putJdn(PREF_ISLAMIC_OFFSET_SET_DATE, Jdn.today()) }

            PREF_SHOW_DEVICE_CALENDAR_EVENTS -> {
                if (prefs.getBoolean(
                        PREF_SHOW_DEVICE_CALENDAR_EVENTS, true
                    ) && ActivityCompat.checkSelfPermission(
                        this, Manifest.permission.READ_CALENDAR
                    ) != PackageManager.PERMISSION_GRANTED
                ) askForCalendarPermission()
            }

            PREF_THEME -> {
                // Restart activity if theme is changed and don't if app theme
                // has just got a default value by preferences as going
                // from null => SystemDefault which makes no difference
                if (previousAppThemeValue != null || !Theme.isDefault(prefs)) restartToSettings()
            }

            PREF_THEME_GRADIENT -> setNavHostBackground()

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

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CALENDAR_READ_PERMISSION_REQUEST_CODE -> {
                enableDeviceCalendar(this, navHostFragment?.navController)
            }

            POST_NOTIFICATION_PERMISSION_REQUEST_CODE_ENABLE_CALENDAR_NOTIFICATION -> {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
                val isGranted = ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
                appPrefs.edit { putBoolean(PREF_NOTIFY_DATE, isGranted) }
                updateStoredPreference(this)
                if (isGranted) update(this, updateDate = true)
            }

            POST_NOTIFICATION_PERMISSION_REQUEST_CODE_ENABLE_ATHAN_NOTIFICATION -> {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
                val isGranted = ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
                appPrefs.edit { putBoolean(PREF_NOTIFICATION_ATHAN, isGranted) }
                updateStoredPreference(this)
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        applyAppLanguage(this)
        ensureDirectionality()
    }

    override fun onResume() {
        super.onResume()
        applyAppLanguage(this)
        update(applicationContext, false)
        val today = Jdn.today()
        if (creationDateJdn != today) {
            creationDateJdn = today
            val navController = navHostFragment?.navController
            if (navController?.currentDestination?.id == R.id.calendar) {
                navController.navigateSafe(CalendarFragmentDirections.navigateToSelf())
            }
        }
    }

    private fun restartToSettings() {
        val intent = intent
        intent?.action = "SETTINGS"
        finish()
        startActivity(intent)
    }

    private var clickedItem = 0

    // TODO: Ugly, to get rid of after full Compose migration
    fun setupToolbarWithDrawer(toolbar: Toolbar) {
        val listener = ActionBarDrawerToggle(
            this, binding.root, toolbar, R.string.open_drawer, R.string.close
        ).also { it.syncState() }

        binding.root.addDrawerListener(listener)
        toolbar.setNavigationOnClickListener { binding.root.openDrawer(GravityCompat.START) }
        toolbar.findViewTreeLifecycleOwner()?.lifecycle?.addObserver(LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_DESTROY) {
                binding.root.removeDrawerListener(listener)
                toolbar.setNavigationOnClickListener(null)
            }
        })
    }

    // TODO: Ugly, to get rid of after full Compose migration
    fun openDrawer() = binding.root.openDrawer(GravityCompat.START)

    private fun createDrawerListener() = object : DrawerLayout.SimpleDrawerListener() {
        val slidingDirection = if (resources.isRtl) -1f else +1f

        override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
            super.onDrawerSlide(drawerView, slideOffset)
            slidingAnimation(drawerView, slideOffset)
        }

        private val blurs =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && windowManager.isCrossWindowBlurEnabled) (0..9).map {
                if (it == 0) null
                else RenderEffect.createBlurEffect(it * 2f, it * 2f, Shader.TileMode.CLAMP)
            } else emptyList()

        private fun slidingAnimation(drawerView: View, slideOffset: Float) {
            binding.navHostFragment.translationX =
                slideOffset * drawerView.width.toFloat() * slidingDirection * .97f
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && blurs.isNotEmpty()) {
                val blurIndex =
                    if (slideOffset.isNaN()) 0 else ((blurs.size - 1) * slideOffset).roundToInt()
                binding.navHostFragment.setRenderEffect(blurs[blurIndex])
                binding.navigation.setRenderEffect(blurs[blurs.size - 1 - blurIndex])
            }
            binding.root.bringChildToFront(drawerView)
            binding.root.requestLayout()
        }

        override fun onDrawerOpened(drawerView: View) {
            super.onDrawerOpened(drawerView)
            onBackPressedCloseDrawerCallback.isEnabled = true

            isDrawerOpen.value = true

            drawerOpenedOnce = true
        }

        override fun onDrawerClosed(drawerView: View) {
            super.onDrawerClosed(drawerView)
            onBackPressedCloseDrawerCallback.isEnabled = false
            if (clickedItem != 0) {
                navigateTo(clickedItem)
                clickedItem = 0
            }

            isDrawerOpen.value = false
        }
    }
}

@Composable
fun AppDrawer(
    isDrawerOpenFlow: StateFlow<Boolean>,
    selectedMenuItemFlow: StateFlow<Int>,
    onIdClicked: (Int) -> Unit
) {
    @OptIn(ExperimentalFoundationApi::class)
    ModalDrawerSheet(
        windowInsets = WindowInsets(0, 0, 0, 0),
        modifier = Modifier.verticalScroll(rememberScrollState())
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
        val isDrawerOpen by isDrawerOpenFlow.collectAsState()

        val actualSeason = remember { Season.fromDate(Date(), coordinates.value).ordinal }
        val pageSize = 200
        val seasonState = rememberPagerState(
            initialPage = pageSize / 2 + actualSeason - 3, // minus 3 so it does an initial animation
            pageCount = { pageSize },
        )
        val scope = rememberCoroutineScope()
        if (isDrawerOpen) {
            scope.launch { seasonState.animateScrollToPage(100 + actualSeason) }
        }
        val imageFilter = remember(LocalConfiguration.current) {
            // Consider gray scale themes of Android 14
            // And apply a gray scale filter https://stackoverflow.com/a/75698731
            if (Theme.isDynamicColor(context.appPrefs) && context.isDynamicGrayscale)
                ColorFilter.colorMatrix(ColorMatrix().also { it.setToSaturation(0f) })
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
                    @OptIn(ExperimentalComposeUiApi::class)
                    this.invisibleToUser()
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

        val selectedMenuItem by selectedMenuItemFlow.collectAsState()
        listOf(
            Triple(R.id.calendar, R.drawable.ic_date_range, R.string.calendar),
            Triple(R.id.converter, R.drawable.ic_swap_vertical_circle, R.string.date_converter),
            Triple(R.id.compass, R.drawable.ic_explore, R.string.compass),
            Triple(R.id.astronomy, R.drawable.ic_astrology_horoscope, R.string.astronomy),
            Triple(R.id.settings, R.drawable.ic_settings, R.string.settings),
            Triple(R.id.about, R.drawable.ic_info, R.string.about),
            Triple(R.id.exit, R.drawable.ic_cancel, R.string.exit),
        ).forEach { (id, icon, title) ->
            NavigationDrawerItem(
                modifier = Modifier.padding(horizontal = 16.dp),
                label = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            ImageVector.vectorResource(icon),
                            modifier = Modifier.size(24.dp),
                            contentDescription = null
                        )
                        Spacer(Modifier.width(16.dp))
                        Text(stringResource(title))
                    }
                },
                selected = id == selectedMenuItem,
                onClick = { onIdClicked(id) },
            )
        }
    }
}
