package com.byagowi.persiancalendar.ui

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.activity.OnBackPressedCallback
import androidx.annotation.IdRes
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.navOptions
import com.byagowi.persiancalendar.*
import com.byagowi.persiancalendar.databinding.ActivityMainBinding
import com.byagowi.persiancalendar.databinding.NavigationHeaderBinding
import com.byagowi.persiancalendar.service.ApplicationService
import com.byagowi.persiancalendar.ui.calendar.CalendarFragmentDirections
import com.byagowi.persiancalendar.utils.*
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar

/**
 * Program activity for android
 */
class MainActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener,
    DrawerSetupContext,
    NavigationView.OnNavigationItemSelectedListener, NavController.OnDestinationChangedListener {

    private var creationDateJdn: Long = 0
    private var settingHasChanged = false
    private lateinit var binding: ActivityMainBinding

    private val onBackPressedCloseDrawerCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() = binding.drawer.closeDrawer(GravityCompat.START)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(getThemeFromName(getThemeFromPreference(this, appPrefs)))

        applyAppLanguage(this)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(this, onBackPressedCloseDrawerCallback)
        ReleaseDebugDifference.startLynxListenerIfIsDebug(this)
        initUtils(this)

        // Don't apply font override to English and Japanese locale
        when (language) {
            LANG_EN_US, LANG_JA -> Unit
            else -> overrideFont("SANS_SERIF", getAppFont(applicationContext))
        }

        startEitherServiceOrWorker(this)

        setDeviceCalendarEvents(applicationContext)
        update(applicationContext, false)

        binding = ActivityMainBinding.inflate(layoutInflater).also {
            setContentView(it.root)
        }

        obtainNavHost() // sake of initializing NavHost

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            window?.also { window ->
                // https://learnpainless.com/android/material/make-fully-android-transparent-status-bar
                window.attributes = window.attributes.also {
                    it.flags and WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS.inv()
                }
                window.statusBarColor = Color.TRANSPARENT
            }

        binding.drawer.addDrawerListener(createDrawerListener())

        obtainNavHost().navController.addOnDestinationChangedListener(this)
        when (intent?.action) {
            "COMPASS" -> R.id.compass
            "LEVEL" -> R.id.level
            "CONVERTER" -> R.id.converter
            "SETTINGS" -> R.id.settings
            "DEVICE" -> R.id.deviceInformation
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
        ) askForCalendarPermission(this)

        binding.navigation.setNavigationItemSelectedListener(this)

        NavigationHeaderBinding.bind(binding.navigation.getHeaderView(0))
            .seasonImage.setImageResource(run {
                var season = (getTodayOfCalendar(CalendarType.SHAMSI).month - 1) / 3

                // Southern hemisphere
                if ((getCoordinate(this)?.latitude ?: 1.0) < .0) season = (season + 2) % 4

                when (season) {
                    0 -> R.drawable.spring
                    1 -> R.drawable.summer
                    2 -> R.drawable.fall
                    else -> R.drawable.winter
                }
            })

        if (appPrefs.getString(PREF_APP_LANGUAGE, null) == null &&
            !appPrefs.getBoolean(CHANGE_LANGUAGE_IS_PROMOTED_ONCE, false)
        ) {
            showChangeLangSnackbar()
            appPrefs.edit { putBoolean(CHANGE_LANGUAGE_IS_PROMOTED_ONCE, true) }
        }

        creationDateJdn = getTodayJdn()

        if (mainCalendar == CalendarType.SHAMSI && isIranHolidaysEnabled &&
            getTodayOfCalendar(CalendarType.SHAMSI).year > supportedYearOfIranCalendar
        ) showAppIsOutDatedSnackbar()

        applyAppLanguage(this)

        previousAppThemeValue = appPrefs.getString(PREF_THEME, null)
    }

    private var previousAppThemeValue: String? = null

    private fun obtainNavHost(): NavHostFragment {
        val navHostFragmentTag = "NavHostFrag"

        return supportFragmentManager.findFragmentByTag(navHostFragmentTag) as? NavHostFragment
            ?: NavHostFragment.create(R.navigation.navigation_graph).also { newNavHostFragment ->
                supportFragmentManager.beginTransaction()
                    .add(binding.navHostContainer.id, newNavHostFragment, navHostFragmentTag)
                    .setPrimaryNavigationFragment(newNavHostFragment)
                    .commitNow()
            }
    }

    override fun onDestinationChanged(
        controller: NavController, destination: NavDestination, arguments: Bundle?
    ) {
        binding.navigation.menu.findItem(
            when (destination.id) {
                // We don't have a menu entry for compass, so
                R.id.level -> R.id.compass
                else -> destination.id
            }
        )?.also {
            it.isCheckable = true
            it.isChecked = true
        }

        if (settingHasChanged) { // update when checked menu item is changed
            initUtils(this)
            update(applicationContext, true)
            settingHasChanged = false // reset for the next time
        }
    }

    private fun navigateTo(@IdRes id: Int) {
        obtainNavHost().navController.navigate(
            id, null, navOptions {
                anim {
                    enter = R.anim.nav_enter_anim
                    exit = R.anim.nav_exit_anim
                    popEnter = R.anim.nav_enter_anim
                    popExit = R.anim.nav_exit_anim
                }
            }
        )
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        settingHasChanged = true
        when (key) {
            PREF_APP_LANGUAGE -> {
                var persianDigits = false
                var changeToAfghanistanHolidays = false
                var changeToIslamicCalendar = false
                var changeToGregorianCalendar = false
                var changeToPersianCalendar = false
                var changeToIranEvents = false
                when (sharedPreferences?.getString(PREF_APP_LANGUAGE, null)
                    ?: DEFAULT_APP_LANGUAGE) {
                    LANG_EN_US -> {
                        changeToGregorianCalendar = true
                    }
                    LANG_JA -> {
                        changeToGregorianCalendar = true
                        persianDigits = true
                    }
                    LANG_AZB, LANG_GLK, LANG_FA -> {
                        persianDigits = true
                        changeToPersianCalendar = true
                        changeToIranEvents = true
                    }
                    LANG_EN_IR -> {
                        persianDigits = false
                        changeToPersianCalendar = true
                        changeToIranEvents = true
                    }
                    LANG_UR -> {
                        persianDigits = false
                        changeToGregorianCalendar = true
                    }
                    LANG_AR -> {
                        persianDigits = true
                        changeToIslamicCalendar = true
                    }
                    LANG_FA_AF -> {
                        persianDigits = true
                        changeToPersianCalendar = true
                        changeToAfghanistanHolidays = true
                    }
                    LANG_PS -> {
                        persianDigits = true
                        changeToPersianCalendar = true
                        changeToAfghanistanHolidays = true
                    }
                    else -> persianDigits = true
                }

                sharedPreferences?.edit {
                    putBoolean(PREF_PERSIAN_DIGITS, persianDigits)
                    // Enable Afghanistan holidays when Dari or Pashto is set
                    if (changeToAfghanistanHolidays) {
                        val currentHolidays =
                            sharedPreferences.getStringSet(PREF_HOLIDAY_TYPES, null)
                                ?: emptySet()

                        if (currentHolidays.isEmpty() || currentHolidays.size == 1 &&
                            "iran_holidays" in currentHolidays
                        ) putStringSet(PREF_HOLIDAY_TYPES, setOf("afghanistan_holidays"))
                    }
                    if (changeToIranEvents) {
                        val currentHolidays =
                            sharedPreferences.getStringSet(PREF_HOLIDAY_TYPES, null)
                                ?: emptySet()

                        if (currentHolidays.isEmpty() || currentHolidays.size == 1
                            && "afghanistan_holidays" in currentHolidays
                        ) putStringSet(PREF_HOLIDAY_TYPES, setOf("iran_holidays"))
                    }
                    when {
                        changeToGregorianCalendar -> {
                            putString(PREF_MAIN_CALENDAR_KEY, "GREGORIAN")
                            putString(PREF_OTHER_CALENDARS_KEY, "ISLAMIC,SHAMSI")
                            putString(PREF_WEEK_START, "1")
                            putStringSet(PREF_WEEK_ENDS, setOf("1"))
                        }
                        changeToIslamicCalendar -> {
                            putString(PREF_MAIN_CALENDAR_KEY, "ISLAMIC")
                            putString(PREF_OTHER_CALENDARS_KEY, "GREGORIAN,SHAMSI")
                            putString(PREF_WEEK_START, DEFAULT_WEEK_START)
                            putStringSet(PREF_WEEK_ENDS, DEFAULT_WEEK_ENDS)
                        }
                        changeToPersianCalendar -> {
                            putString(PREF_MAIN_CALENDAR_KEY, "SHAMSI")
                            putString(PREF_OTHER_CALENDARS_KEY, "GREGORIAN,ISLAMIC")
                            putString(PREF_WEEK_START, DEFAULT_WEEK_START)
                            putStringSet(PREF_WEEK_ENDS, DEFAULT_WEEK_ENDS)
                        }
                    }
                }
            }
        }

        if (key == PREF_SHOW_DEVICE_CALENDAR_EVENTS &&
            sharedPreferences?.getBoolean(PREF_SHOW_DEVICE_CALENDAR_EVENTS, true) == true
            && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.READ_CALENDAR
            ) != PackageManager.PERMISSION_GRANTED
        ) askForCalendarPermission(this)

        if (key == PREF_APP_LANGUAGE) restartToSettings()

        // Restart activity if theme is changed and don't if app theme
        // has just got a default value by preferences as going
        // from null => SystemDefault which makes no difference
        if (key == PREF_THEME && !(previousAppThemeValue == null &&
                    sharedPreferences?.getString(PREF_THEME, null) == SYSTEM_DEFAULT_THEME)
        ) restartToSettings()

        if (key == PREF_NOTIFY_DATE &&
            sharedPreferences?.getBoolean(PREF_NOTIFY_DATE, true) == false
        ) {
            stopService(Intent(this, ApplicationService::class.java))
            startEitherServiceOrWorker(applicationContext)
        }

        updateStoredPreference(this)
        update(applicationContext, true)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CALENDAR_READ_PERMISSION_REQUEST_CODE -> when (PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.READ_CALENDAR
                ) -> {
                    toggleShowDeviceCalendarOnPreference(this, true)
                    val navController = obtainNavHost().navController
                    if (navController.currentDestination?.id == R.id.calendar)
                        navController.navigate(CalendarFragmentDirections.navigateToSelf())
                }
                else -> toggleShowDeviceCalendarOnPreference(this, false)
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        initUtils(this)
        binding.drawer.layoutDirection = when {
            isRTL(this) -> View.LAYOUT_DIRECTION_RTL
            else -> View.LAYOUT_DIRECTION_LTR
        }
    }

    override fun onResume() {
        super.onResume()
        applyAppLanguage(this)
        update(applicationContext, false)
        val navController = obtainNavHost().navController
        if (creationDateJdn != getTodayJdn()) {
            creationDateJdn = getTodayJdn()
            if (navController.currentDestination?.id == R.id.calendar) {
                navController.navigate(CalendarFragmentDirections.navigateToSelf())
            }
        }
    }

    // Checking for the ancient "menu" key
    override fun onKeyDown(keyCode: Int, event: KeyEvent?) = when (keyCode) {
        KeyEvent.KEYCODE_MENU -> {
            when {
                binding.drawer.isDrawerOpen(GravityCompat.START) ->
                    binding.drawer.closeDrawer(GravityCompat.START)
                else -> binding.drawer.openDrawer(GravityCompat.START)
            }
            true
        }
        else -> super.onKeyDown(keyCode, event)
    }

    private fun restartToSettings() {
        val intent = intent
        intent?.action = "SETTINGS"
        finish()
        startActivity(intent)
    }

    private var clickedItem = 0

    override fun onNavigationItemSelected(selectedMenuItem: MenuItem): Boolean {
        when (val itemId = selectedMenuItem.itemId) {
            R.id.exit -> finish()
            else -> {
                binding.drawer.closeDrawer(GravityCompat.START)
                if (obtainNavHost().navController.currentDestination?.id != itemId) {
                    clickedItem = itemId
                }
            }
        }
        return true
    }

    private fun showChangeLangSnackbar() = Snackbar.make(
        binding.root, "✖  Change app language?", 7000
    ).apply {
        view.layoutDirection = View.LAYOUT_DIRECTION_LTR
        view.setOnClickListener { dismiss() }
        setAction("Settings") { appPrefs.edit { putString(PREF_APP_LANGUAGE, LANG_EN_US) } }
        setActionTextColor(ContextCompat.getColor(context, R.color.dark_accent))
    }.show()

    private fun showAppIsOutDatedSnackbar() = Snackbar.make(
        binding.root, getString(R.string.outdated_app), 10000
    ).apply {
        setAction(getString(R.string.update)) { bringMarketPage(this@MainActivity) }
        setActionTextColor(ContextCompat.getColor(context, R.color.dark_accent))
    }.show()

    override fun setupToolbarWithDrawer(viewLifecycleOwner: LifecycleOwner, toolbar: Toolbar) {
        val listener = ActionBarDrawerToggle(
            this, binding.drawer, toolbar,
            androidx.navigation.ui.R.string.nav_app_bar_open_drawer_description, R.string.close
        ).also { it.syncState() }

        binding.drawer.addDrawerListener(listener)
        toolbar.setNavigationOnClickListener { binding.drawer.openDrawer(GravityCompat.START) }
        viewLifecycleOwner.lifecycle.addObserver(LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_DESTROY) {
                binding.drawer.removeDrawerListener(listener)
                toolbar.setNavigationOnClickListener(null)
            }
        })
    }

    private fun createDrawerListener() = object : DrawerLayout.SimpleDrawerListener() {
        val slidingDirection = if (isRTL(this@MainActivity)) -1 else +1

        override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
            super.onDrawerSlide(drawerView, slideOffset)
            slidingAnimation(drawerView, slideOffset / 1.5f)
        }

        private fun slidingAnimation(drawerView: View, slideOffset: Float) = binding.apply {
            navHostContainer.translationX =
                slideOffset * drawerView.width.toFloat() * slidingDirection.toFloat()
            drawer.bringChildToFront(drawerView)
            drawer.requestLayout()
        }

        override fun onDrawerOpened(drawerView: View) {
            super.onDrawerOpened(drawerView)
            onBackPressedCloseDrawerCallback.isEnabled = true
        }

        override fun onDrawerClosed(drawerView: View) {
            super.onDrawerClosed(drawerView)
            onBackPressedCloseDrawerCallback.isEnabled = false
            if (clickedItem != 0) {
                navigateTo(clickedItem)
                clickedItem = 0
            }
        }
    }
}
