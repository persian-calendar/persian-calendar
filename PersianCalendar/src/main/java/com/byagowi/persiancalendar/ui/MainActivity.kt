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
import androidx.activity.addCallback
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
import com.byagowi.persiancalendar.ui.calendar.NavigationInterface
import com.byagowi.persiancalendar.utils.*
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar

/**
 * Program activity for android
 */
class MainActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener,
    NavigationInterface,
    NavigationView.OnNavigationItemSelectedListener, NavController.OnDestinationChangedListener {

    private var creationDateJdn: Long = 0
    private var settingHasChanged = false
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(getThemeFromName(getThemeFromPreference(this, appPrefs)))

        applyAppLanguage(this)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(this) {
            if (binding.drawer.isDrawerOpen(GravityCompat.START)) {
                binding.drawer.closeDrawer(GravityCompat.START)
            } else {
                isEnabled = false
                onBackPressedDispatcher.onBackPressed()
            }
        }
        ReleaseDebugDifference.startLynxListenerIfIsDebug(this)
        initUtils(this)

        // Don't apply font override to English and Japanese locale
        when (language) {
            LANG_EN_US, LANG_JA -> Unit
            else -> overrideFont("SANS_SERIF", getAppFont(applicationContext))
        }

        startEitherServiceOrWorker(this)

        // Doesn't matter apparently
        // oneTimeClockDisablingForAndroid5LE();
        setDeviceCalendarEvents(applicationContext)
        update(applicationContext, false)

        binding = ActivityMainBinding.inflate(layoutInflater).also {
            setContentView(it.root)
        }

        obtainNavHost() // sake of initialize NavHost

        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> window.also {
                // https://learnpainless.com/android/material/make-fully-android-transparent-status-bar
                it.attributes = it.attributes.apply {
                    flags and WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS.inv()
                }
                it.statusBarColor = Color.TRANSPARENT
            }
        }

        binding.drawer.addDrawerListener(createDrawerListener())

        obtainNavHost().navController.addOnDestinationChangedListener(this)
        intent?.run {
            val newDestinationId = when (action) {
                "COMPASS" -> R.id.compass
                "LEVEL" -> R.id.level
                "CONVERTER" -> R.id.converter
                "SETTINGS" -> R.id.settings
                "DEVICE" -> R.id.deviceInformation
                else -> null // unsupported action. ignore
            }
            if (newDestinationId != null) {
                navigateTo(newDestinationId)

                // So it won't happen again if the activity restarted
                action = ""
            }
        }

        appPrefs.registerOnSharedPreferenceChangeListener(this)

        when {
            isShowDeviceCalendarEvents && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.READ_CALENDAR
            ) != PackageManager.PERMISSION_GRANTED -> askForCalendarPermission(this)
        }

        binding.navigation.setNavigationItemSelectedListener(this)

        NavigationHeaderBinding.bind(binding.navigation.getHeaderView(0))
            .seasonImage.setImageResource(run {
                var season = (getTodayOfCalendar(CalendarType.SHAMSI).month - 1) / 3

                // Southern hemisphere
                when {
                    (getCoordinate(this)?.latitude ?: 1.0) < .0 -> season = (season + 2) % 4
                }

                when (season) {
                    0 -> R.drawable.spring
                    1 -> R.drawable.summer
                    2 -> R.drawable.fall
                    else -> R.drawable.winter
                }
            })

        when {
            appPrefs.getString(PREF_APP_LANGUAGE, null) == null &&
                    !appPrefs.getBoolean(CHANGE_LANGUAGE_IS_PROMOTED_ONCE, false) -> {
                changeLangSnackbar().show()
                appPrefs.edit { putBoolean(CHANGE_LANGUAGE_IS_PROMOTED_ONCE, true) }
            }
        }

        creationDateJdn = getTodayJdn()

        when {
            mainCalendar == CalendarType.SHAMSI && isIranHolidaysEnabled &&
                    getTodayOfCalendar(CalendarType.SHAMSI).year > supportedYearOfIranCalendar -> outDatedSnackbar().show()
        }

        applyAppLanguage(this)
    }

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
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?
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

        when {
            settingHasChanged -> { // update when checked menu item is changed
                initUtils(this)
                update(applicationContext, true)
                settingHasChanged = false // reset for the next time
            }
        }
    }

    private fun navigateTo(@IdRes id: Int) {
        obtainNavHost().navController.navigate(
            id,
            null,
            navOptions {
                anim {
                    enter = R.anim.nav_enter_anim
                    exit = R.anim.nav_exit_anim
                    popEnter = R.anim.nav_pop_enter_anim
                    popExit = R.anim.nav_pop_exit_anim
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
                    when {
                        changeToAfghanistanHolidays -> {
                            val currentHolidays =
                                sharedPreferences.getStringSet(PREF_HOLIDAY_TYPES, null)
                                    ?: emptySet()

                            when {
                                currentHolidays.isEmpty() || currentHolidays.size == 1 &&
                                        "iran_holidays" in currentHolidays -> putStringSet(
                                    PREF_HOLIDAY_TYPES,
                                    setOf("afghanistan_holidays")
                                )
                            }
                        }
                    }
                    when {
                        changeToIranEvents -> {
                            val currentHolidays =
                                sharedPreferences.getStringSet(PREF_HOLIDAY_TYPES, null)
                                    ?: emptySet()

                            when {
                                currentHolidays.isEmpty() || currentHolidays.size == 1 && "afghanistan_holidays" in currentHolidays -> putStringSet(
                                    PREF_HOLIDAY_TYPES,
                                    setOf("iran_holidays")
                                )
                            }
                        }
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

        when {
            key == PREF_SHOW_DEVICE_CALENDAR_EVENTS &&
                    sharedPreferences?.getBoolean(PREF_SHOW_DEVICE_CALENDAR_EVENTS, true) == true
                    && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.READ_CALENDAR
            ) != PackageManager.PERMISSION_GRANTED -> askForCalendarPermission(this)
        }

        when (key) {
            PREF_APP_LANGUAGE, PREF_THEME -> restartToSettings()
        }

        when {
            key == PREF_NOTIFY_DATE &&
                    sharedPreferences?.getBoolean(PREF_NOTIFY_DATE, true) == false -> {
                stopService(Intent(this, ApplicationService::class.java))
                startEitherServiceOrWorker(applicationContext)
            }
        }

        updateStoredPreference(this)
        update(applicationContext, true)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CALENDAR_READ_PERMISSION_REQUEST_CODE -> {
                when (PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.checkSelfPermission(
                        this, Manifest.permission.READ_CALENDAR
                    ) -> {
                        toggleShowDeviceCalendarOnPreference(this, true)
                        when (R.id.calendar) {
                            obtainNavHost().navController.currentDestination?.id -> restartActivity()
                        }
                    }
                    else -> toggleShowDeviceCalendarOnPreference(this, false)
                }
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        initUtils(this)
        binding.drawer.layoutDirection =
            when {
                isRTL(this) -> View.LAYOUT_DIRECTION_RTL
                else -> View.LAYOUT_DIRECTION_LTR
            }
    }

    override fun onResume() {
        super.onResume()
        applyAppLanguage(this)
        update(applicationContext, false)
        when {
            creationDateJdn != getTodayJdn() -> restartActivity()
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean =
        // Checking for the ancient "menu" key
        when (keyCode) {
            KeyEvent.KEYCODE_MENU -> {
                when {
                    binding.drawer.isDrawerOpen(GravityCompat.START) -> {
                        binding.drawer.closeDrawer(GravityCompat.START)
                    }
                    else -> {
                        binding.drawer.openDrawer(GravityCompat.START)
                    }
                }
                true
            }
            else -> {
                super.onKeyDown(keyCode, event)
            }
        }

    override fun restartActivity() {
        val intent = intent
        finish()
        startActivity(intent)
    }

    private fun restartToSettings() {
        val intent = intent
        intent?.action = "SETTINGS"
        finish()
        startActivity(intent)
    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.exit -> {
                finish()
            }
            else -> {
                val id = menuItem.itemId
                if (obtainNavHost().navController.currentDestination?.id == id) {
                    binding.drawer.closeDrawer(GravityCompat.START)
                } else {
                    binding.drawer.closeDrawer(GravityCompat.START, false)
                    navigateTo(menuItem.itemId)
                }
            }
        }
        return true
    }

    private fun changeLangSnackbar() =
        Snackbar.make(binding.root, "✖  Change app language?", 7000).apply {
            view.layoutDirection = View.LAYOUT_DIRECTION_LTR
            view.setOnClickListener { dismiss() }
            setAction("Settings") {
                appPrefs.edit {
                    putString(PREF_APP_LANGUAGE, LANG_EN_US)
                }
            }
            setActionTextColor(ContextCompat.getColor(context, R.color.dark_accent))
        }

    private fun outDatedSnackbar() =
        Snackbar.make(binding.root, getString(R.string.outdated_app), 10000).apply {
            setAction(getString(R.string.update)) {
                bringMarketPage(this@MainActivity)
            }
            setActionTextColor(ContextCompat.getColor(context, R.color.dark_accent))
        }

    override fun setupToolbarWithDrawer(viewLifecycleOwner: LifecycleOwner, toolbar: Toolbar) {
        val listener = ActionBarDrawerToggle(
            this, binding.drawer, toolbar,
            androidx.navigation.ui.R.string.nav_app_bar_open_drawer_description,
            R.string.closeDrawer
        ).apply { syncState() }

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
        val slidingDirection = when {
            isRTL(this@MainActivity) -> -1
            else -> +1
        }

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
    }
}
