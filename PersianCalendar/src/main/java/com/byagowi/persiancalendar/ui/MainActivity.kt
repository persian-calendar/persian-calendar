package com.byagowi.persiancalendar.ui

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import com.byagowi.persiancalendar.Constants
import com.byagowi.persiancalendar.Constants.*
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ReleaseDebugDifference
import com.byagowi.persiancalendar.databinding.ActivityMainBinding
import com.byagowi.persiancalendar.di.dependencies.AppDependency
import com.byagowi.persiancalendar.di.dependencies.MainActivityDependency
import com.byagowi.persiancalendar.service.ApplicationService
import com.byagowi.persiancalendar.ui.calendar.CalendarFragment
import com.byagowi.persiancalendar.utils.CalendarType
import com.byagowi.persiancalendar.utils.TypefaceUtils
import com.byagowi.persiancalendar.utils.UpdateUtils
import com.byagowi.persiancalendar.utils.Utils
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import dagger.android.support.DaggerAppCompatActivity
import java.util.*
import javax.inject.Inject


/**
 * Program activity for android
 *
 * @author ebraminio
 */
class MainActivity : DaggerAppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener, NavigationView.OnNavigationItemSelectedListener {
    @Inject
    internal var appDependency: AppDependency? = null // same object from App
    @Inject
    internal var mainActivityDependency: MainActivityDependency? = null
    private var creationDateJdn: Long = 0
    private var actionBar: ActionBar? = null
    private var settingHasChanged = false
    private var binding: ActivityMainBinding? = null

    val coordinator: CoordinatorLayout
        get() = binding!!.coordinator

    private val seasonImage: Int
        @DrawableRes
        get() {
            var isSouthernHemisphere = false
            val coordinate = Utils.getCoordinate(this)
            if (coordinate != null && coordinate.latitude < 0) {
                isSouthernHemisphere = true
            }

            var month = Utils.getTodayOfCalendar(CalendarType.SHAMSI).month
            if (isSouthernHemisphere) month = (month + 6 - 1) % 12 + 1

            return if (month < 4)
                R.drawable.spring
            else if (month < 7)
                R.drawable.summer
            else if (month < 10)
                R.drawable.fall
            else
                R.drawable.winter
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Don't replace below with appDependency.getSharedPreferences() ever
        // as the injection won't happen at the right time
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        setTheme(Utils.getThemeFromName(Utils.getThemeFromPreference(prefs)))

        Utils.applyAppLanguage(this)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        ReleaseDebugDifference.startLynxListenerIfIsDebug(this)
        Utils.initUtils(this)

        TypefaceUtils.overrideFont("SERIF",
            TypefaceUtils.getAppFont(applicationContext))

        Utils.startEitherServiceOrWorker(this)

        // Doesn't matter apparently
        // oneTimeClockDisablingForAndroid5LE();
        UpdateUtils.setDeviceCalendarEvents(applicationContext)
        UpdateUtils.update(applicationContext, false)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setSupportActionBar(binding!!.toolbar)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // https://learnpainless.com/android/material/make-fully-android-transparent-status-bar
            val win = window
            val winParams = win.attributes
            winParams.flags = winParams.flags and WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS.inv()
            win.attributes = winParams
            window.statusBarColor = Color.TRANSPARENT
        }

        val isRTL = Utils.isRTL(this)

        val drawerToggle = object : ActionBarDrawerToggle(this, binding!!.drawer, binding!!.toolbar, R.string.openDrawer, R.string.closeDrawer) {
            var slidingDirection = if (isRTL) -1 else +1

            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                super.onDrawerSlide(drawerView, slideOffset)
                slidingAnimation(drawerView, slideOffset / 1.5f)
            }

            private fun slidingAnimation(drawerView: View, slideOffset: Float) {
                binding!!.appMainLayout.translationX = slideOffset * drawerView.width.toFloat() * slidingDirection.toFloat()
                binding!!.drawer.bringChildToFront(drawerView)
                binding!!.drawer.requestLayout()
            }
        }

        binding!!.drawer.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        val intent = intent
        if (intent != null) {
            val action = intent.action
            if ("COMPASS" == action)
                navigateTo(R.id.compass)
            else if ("LEVEL" == action)
                navigateTo(R.id.level)
            else if ("CONVERTER" == action)
                navigateTo(R.id.converter)
            else if ("SETTINGS" == action)
                navigateTo(R.id.settings)
            else if ("DEVICE" == action)
                navigateTo(R.id.deviceInfo)
            else
                navigateTo(R.id.calendar)

            // So it won't happen again if the activity restarted
            intent.action = ""
        }

        prefs.registerOnSharedPreferenceChangeListener(this)

        if (Utils.isShowDeviceCalendarEvents()) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
                Utils.askForCalendarPermission(this)
            }
        }

        binding!!.navigation.setNavigationItemSelectedListener(this)

        (binding!!.navigation.getHeaderView(0).findViewById<View>(R.id.season_image) as ImageView)
            .setImageResource(seasonImage)

        var appLanguage = prefs.getString(PREF_APP_LANGUAGE, "N/A")
        if (appLanguage == null) appLanguage = "N/A"
        if (appLanguage == "N/A" && !prefs.getBoolean(Constants.CHANGE_LANGUAGE_IS_PROMOTED_ONCE, false)) {
            val snackbar = Snackbar.make(coordinator, "✖  Change app language?",
                7000)
            val snackbarView = snackbar.view
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                snackbarView.layoutDirection = View.LAYOUT_DIRECTION_LTR
            }
            val text = snackbar.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
            text.setTextColor(Color.WHITE)

            snackbarView.setOnClickListener { v -> snackbar.dismiss() }
            snackbar.setAction("Settings") { view ->
                val edit = prefs.edit()
                edit.putString(Constants.PREF_APP_LANGUAGE, Constants.LANG_EN_US)
                edit.putString(PREF_MAIN_CALENDAR_KEY, "GREGORIAN")
                edit.putString(PREF_OTHER_CALENDARS_KEY, "ISLAMIC,SHAMSI")
                edit.putStringSet(PREF_HOLIDAY_TYPES, HashSet())
                edit.apply()

                restartToSettings()
            }
            snackbar.setActionTextColor(resources.getColor(R.color.dark_accent))
            snackbar.show()

            // Show this snackbar only once
            val edit = prefs.edit()
            edit.putBoolean(Constants.CHANGE_LANGUAGE_IS_PROMOTED_ONCE, true)
            edit.apply()
        }

        actionBar = supportActionBar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            binding!!.appbarLayout.outlineProvider = null
        }

        creationDateJdn = Utils.getTodayJdn()

        if (Utils.getMainCalendar() == CalendarType.SHAMSI &&
            Utils.isIranHolidaysEnabled() &&
            Utils.getTodayOfCalendar(CalendarType.SHAMSI).year > Utils.getMaxSupportedYear()) {
            val snackbar = Snackbar.make(coordinator, getString(R.string.outdated_app),
                10000)
            val text = snackbar.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
            text.setTextColor(Color.WHITE)

            snackbar.setAction(getString(R.string.update)) { view ->
                val appPackageName = packageName
                try {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName")))
                } catch (anfe: android.content.ActivityNotFoundException) {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")))
                }
            }
            snackbar.setActionTextColor(resources.getColor(R.color.dark_accent))
            snackbar.show()
        }

        Utils.applyAppLanguage(this)
    }

    fun navigateTo(@IdRes id: Int) {
        val menuItem = binding!!.navigation.menu.findItem(
            if (id == R.id.level) R.id.compass else id) // We don't have a menu entry for compass, so
        if (menuItem != null) {
            menuItem.isCheckable = true
            menuItem.isChecked = true
        }

        if (settingHasChanged) { // update when checked menu item is changed
            Utils.initUtils(this)
            UpdateUtils.update(applicationContext, true)
            settingHasChanged = false // reset for the next time
        }

        Navigation.findNavController(this, R.id.nav_host_fragment)
            .navigate(id, null, null)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        settingHasChanged = true
        if (key == PREF_APP_LANGUAGE) {
            var persianDigits = false
            var changeToAfghanistanHolidays = false
            var changeToIslamicCalendar = false
            var changeToGregorianCalendar = false
            var changeToPersianCalendar = false
            var changeToIranEvents = false
            var removeAllEvents = false
            var lang = sharedPreferences.getString(PREF_APP_LANGUAGE, DEFAULT_APP_LANGUAGE)
            if (lang == null) lang = ""
            when (lang) {
                LANG_EN_US -> {
                    changeToGregorianCalendar = true
                    removeAllEvents = true
                }
                LANG_GLK, LANG_FA -> {
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

            val editor = sharedPreferences.edit()
            editor.putBoolean(PREF_PERSIAN_DIGITS, persianDigits)
            // Enable Afghanistan holidays when Dari or Pashto is set
            if (changeToAfghanistanHolidays) {
                val currentHolidays = sharedPreferences.getStringSet(PREF_HOLIDAY_TYPES, HashSet())

                if (currentHolidays == null || currentHolidays.isEmpty() ||
                    currentHolidays.size == 1 && currentHolidays.contains("iran_holidays")) {
                    editor.putStringSet(PREF_HOLIDAY_TYPES,
                        HashSet(listOf("afghanistan_holidays")))
                }
            }
            if (changeToIranEvents) {
                val currentHolidays = sharedPreferences.getStringSet(PREF_HOLIDAY_TYPES, HashSet())

                if (currentHolidays == null || currentHolidays.isEmpty() ||
                    currentHolidays.size == 1 && currentHolidays.contains("afghanistan_holidays")) {
                    editor.putStringSet(PREF_HOLIDAY_TYPES,
                        HashSet(listOf("iran_holidays")))
                }
            }
            if (removeAllEvents) {
                val currentHolidays = sharedPreferences.getStringSet(PREF_HOLIDAY_TYPES, HashSet())

                if (currentHolidays == null || currentHolidays.isEmpty() ||
                    currentHolidays.size == 1 && currentHolidays.contains("iran_holidays")) {
                    editor.putStringSet(PREF_HOLIDAY_TYPES, HashSet())
                }
            }
            if (changeToGregorianCalendar) {
                editor.putString(PREF_MAIN_CALENDAR_KEY, "GREGORIAN")
                editor.putString(PREF_OTHER_CALENDARS_KEY, "ISLAMIC,SHAMSI")
                editor.putString(PREF_WEEK_START, "1")
                editor.putStringSet(PREF_WEEK_ENDS, HashSet(listOf("1")))
            } else if (changeToIslamicCalendar) {
                editor.putString(PREF_MAIN_CALENDAR_KEY, "ISLAMIC")
                editor.putString(PREF_OTHER_CALENDARS_KEY, "GREGORIAN,SHAMSI")
                editor.putString(PREF_WEEK_START, DEFAULT_WEEK_START)
                editor.putStringSet(PREF_WEEK_ENDS, DEFAULT_WEEK_ENDS)
            } else if (changeToPersianCalendar) {
                editor.putString(PREF_MAIN_CALENDAR_KEY, "SHAMSI")
                editor.putString(PREF_OTHER_CALENDARS_KEY, "GREGORIAN,ISLAMIC")
                editor.putString(PREF_WEEK_START, DEFAULT_WEEK_START)
                editor.putStringSet(PREF_WEEK_ENDS, DEFAULT_WEEK_ENDS)
            }
            editor.apply()
        }

        if (key == PREF_SHOW_DEVICE_CALENDAR_EVENTS) {
            if (sharedPreferences.getBoolean(PREF_SHOW_DEVICE_CALENDAR_EVENTS, true)) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
                    Utils.askForCalendarPermission(this)
                }
            }
        }

        if (key == PREF_APP_LANGUAGE || key == PREF_THEME) {
            restartToSettings()
        }

        if (key == PREF_NOTIFY_DATE) {
            if (!sharedPreferences.getBoolean(PREF_NOTIFY_DATE, true)) {
                stopService(Intent(this, ApplicationService::class.java))
                Utils.startEitherServiceOrWorker(applicationContext)
            }
        }

        Utils.updateStoredPreference(this)
        UpdateUtils.update(applicationContext, true)

        ViewModelProviders.of(this).get(MainActivityModel::class.java).preferenceIsUpdated()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.CALENDAR_READ_PERMISSION_REQUEST_CODE) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED) {
                Utils.toggleShowDeviceCalendarOnPreference(this, true)
                val currentDestination = Navigation
                    .findNavController(this, R.id.nav_host_fragment)
                    .currentDestination
                if (currentDestination != null && currentDestination.id == R.id.calendar) {
                    restartActivity()
                }
            } else {
                Utils.toggleShowDeviceCalendarOnPreference(this, false)
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        Utils.initUtils(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            binding!!.drawer.layoutDirection = if (Utils.isRTL(this)) View.LAYOUT_DIRECTION_RTL else View.LAYOUT_DIRECTION_LTR
        }
    }

    override fun onResume() {
        super.onResume()
        Utils.applyAppLanguage(this)
        UpdateUtils.update(applicationContext, false)
        if (creationDateJdn != Utils.getTodayJdn()) {
            restartActivity()
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        // Checking for the "menu" key
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            if (binding!!.drawer.isDrawerOpen(GravityCompat.START)) {
                binding!!.drawer.closeDrawers()
            } else {
                binding!!.drawer.openDrawer(GravityCompat.START)
            }
            return true
        } else {
            return super.onKeyDown(keyCode, event)
        }
    }

    fun restartActivity() {
        val intent = intent
        finish()
        startActivity(intent)
    }

    fun restartToSettings() {
        val intent = intent
        intent.action = "SETTINGS"
        finish()
        startActivity(intent)
    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        if (menuItem.itemId == R.id.exit) {
            finish()
            return true
        }

        binding!!.drawer.closeDrawers()
        navigateTo(menuItem.itemId)
        return true
    }

    fun setTitleAndSubtitle(title: String, subtitle: String) {
        actionBar!!.title = title
        actionBar!!.subtitle = subtitle
    }

    override fun onBackPressed() {
        if (binding!!.drawer.isDrawerOpen(GravityCompat.START)) {
            binding!!.drawer.closeDrawers()
        } else {
            val calendarFragment = supportFragmentManager
                .findFragmentByTag(CalendarFragment::class.java.name) as CalendarFragment?

            if (calendarFragment != null) {
                if (calendarFragment.closeSearch())
                    return
            }

            val currentDestination = Navigation
                .findNavController(this, R.id.nav_host_fragment)
                .currentDestination
            if (currentDestination == null || currentDestination.id == R.id.calendar)
                finish()
            else
                navigateTo(R.id.calendar)
        }
    }
}
