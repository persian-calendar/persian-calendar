package com.byagowi.persiancalendar.view.activity

import android.Manifest
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.annotation.NonNull
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import calendar.CivilDate
import com.byagowi.persiancalendar.Constants
import com.byagowi.persiancalendar.Constants.DEFAULT_APP_LANGUAGE
import com.byagowi.persiancalendar.Constants.LANG_EN_US
import com.byagowi.persiancalendar.Constants.LANG_UR
import com.byagowi.persiancalendar.Constants.PREF_APP_LANGUAGE
import com.byagowi.persiancalendar.Constants.PREF_PERSIAN_DIGITS
import com.byagowi.persiancalendar.Constants.PREF_SHOW_DEVICE_CALENDAR_EVENTS
import com.byagowi.persiancalendar.Constants.PREF_THEME
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.adapter.DrawerAdapter
import com.byagowi.persiancalendar.databinding.ActivityMainBinding
import com.byagowi.persiancalendar.util.*
import com.byagowi.persiancalendar.view.fragment.*

/**
 * Program activity for android
 *
 * @author ebraminio
 */
class MainActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {
  private val TAG = MainActivity::class.java.name
  private lateinit var binding: ActivityMainBinding
  private var adapter: DrawerAdapter? = null
  private val fragments = arrayOf<Class<*>?>(null, CalendarFragment::class.java, ConverterFragment::class.java, CompassFragment::class.java, ApplicationPreferenceFragment::class.java, AboutFragment::class.java)
  private var menuPosition = 0 // it should be zero otherwise #selectItem won't be called

  // https://stackoverflow.com/a/3410200
  val statusBarHeight: Int
    get() {
      var result = 0
      val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
      if (resourceId > 0) {
        result = resources.getDimensionPixelSize(resourceId)
      }
      return result
    }

  internal var settingHasChanged = false

  private fun oneTimeClockDisablingForAndroid5LE() {
    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
      val key = "oneTimeClockDisablingForAndroid5LE"
      val prefs = PreferenceManager.getDefaultSharedPreferences(this)
      if (!prefs.getBoolean(key, false)) {
        val edit = prefs.edit()
        edit.putBoolean(Constants.PREF_WIDGET_CLOCK, false)
        edit.putBoolean(key, true)
        edit.apply()
      }
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    UIUtils.setTheme(this)
    Utils.changeAppLanguage(this)
    requestWindowFeature(Window.FEATURE_NO_TITLE)
    super.onCreate(savedInstanceState)
    Utils.initUtils(this)
    TypeFaceUtil.overrideFont(applicationContext, "SERIF", "fonts/NotoNaskhArabic-Regular.ttf") // font from assets: "assets/fonts/Roboto-Regular.ttf

    Utils.startEitherServiceOrWorker(this)

    // Doesn't matter apparently
    // oneTimeClockDisablingForAndroid5LE();
    UpdateUtils.update(applicationContext, false)

    setContentView(R.layout.activity_main)
    binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
    setSupportActionBar(binding.toolbar)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      val window = window
      window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
          WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

      binding.toolbar.setPadding(0, statusBarHeight, 0, 0)

    }

    binding.navigationView.setHasFixedSize(true)
    adapter = DrawerAdapter(this)
    binding.navigationView.adapter = adapter

    val layoutManager = LinearLayoutManager(this)
    binding.navigationView.layoutManager = layoutManager

    val drawerToggle = object : ActionBarDrawerToggle(this, binding.drawer, binding.toolbar, R.string.openDrawer, R.string.closeDrawer) {
      var slidingDirection = +1

      init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
          if (UIUtils.isRTL(applicationContext))
            slidingDirection = -1
        }
      }

      override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
        super.onDrawerSlide(drawerView, slideOffset)
        slidingAnimation(drawerView, slideOffset)
      }


      private fun slidingAnimation(drawerView: View, slideOffset: Float) {
        binding.appMainLayout.translationX = slideOffset * drawerView.width.toFloat() * slidingDirection.toFloat()
        binding.drawer.bringChildToFront(drawerView)
        binding.drawer.requestLayout()
      }
    }

    binding.drawer.addDrawerListener(drawerToggle)
    drawerToggle.syncState()
    val action = if (intent != null) intent.action else null
    if ("COMPASS_SHORTCUT" == action) {
      selectItem(COMPASS)
    } else if ("PREFERENCE_SHORTCUT" == action) {
      selectItem(PREFERENCE)
    } else if ("CONVERTER_SHORTCUT" == action) {
      selectItem(CONVERTER)
    } else if ("ABOUT_SHORTCUT" == action) {
      selectItem(ABOUT)
    } else {
      selectItem(DEFAULT)
    }

    val prefs = PreferenceManager.getDefaultSharedPreferences(this)
    prefs.registerOnSharedPreferenceChangeListener(this)

    if (Utils.isShowDeviceCalendarEvents) {
      if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
        UIUtils.askForCalendarPermission(this)
      }
    }

    creationDate = CalendarUtils.gregorianToday
    Utils.changeAppLanguage(this)
  }

  override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
    settingHasChanged = true
    if (key == PREF_APP_LANGUAGE) {
      val persianDigits: Boolean
      when (sharedPreferences.getString(PREF_APP_LANGUAGE, DEFAULT_APP_LANGUAGE)) {
        LANG_EN_US -> persianDigits = false
        LANG_UR -> persianDigits = false
        else -> persianDigits = true
      }

      val editor = sharedPreferences.edit()
      editor.putBoolean(PREF_PERSIAN_DIGITS, persianDigits)
      editor.apply()
    }

    if (key == PREF_SHOW_DEVICE_CALENDAR_EVENTS) {
      if (sharedPreferences.getBoolean(PREF_SHOW_DEVICE_CALENDAR_EVENTS, true)) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
          UIUtils.askForCalendarPermission(this)
        }
      }
    }

    if (key == PREF_APP_LANGUAGE || key == PREF_THEME) {
      restartActivity(PREFERENCE)
    }

    UpdateUtils.update(applicationContext, true)
  }

  override fun onRequestPermissionsResult(requestCode: Int, @NonNull permissions: Array<String>, @NonNull grantResults: IntArray) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    if (requestCode == Constants.CALENDAR_READ_PERMISSION_REQUEST_CODE) {
      if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED) {
        Utils.initUtils(this)
        if (menuPosition == CALENDAR) {
          restartActivity(menuPosition)
        }
      } else {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val edit = prefs.edit()
        edit.putBoolean(PREF_SHOW_DEVICE_CALENDAR_EVENTS, false)
        edit.apply()
      }
    }
  }

  override fun onConfigurationChanged(newConfig: Configuration) {
    super.onConfigurationChanged(newConfig)
    Utils.initUtils(this)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
      binding.drawer.layoutDirection = if (UIUtils.isRTL(this)) View.LAYOUT_DIRECTION_RTL else View.LAYOUT_DIRECTION_LTR
    }
  }

  override fun onResume() {
    super.onResume()
    Utils.changeAppLanguage(this)
    UpdateUtils.update(applicationContext, false)
    if (creationDate?.equals(CalendarUtils.gregorianToday) == false) {
      restartActivity(menuPosition)
    }
  }

  override fun onBackPressed() {
    if (binding.drawer.isDrawerOpen(GravityCompat.START)) {
      binding.drawer.closeDrawers()
    } else if (menuPosition != DEFAULT) {
      selectItem(DEFAULT)
    } else {
      val calendarFragment = supportFragmentManager
          .findFragmentByTag(CalendarFragment::class.java.name) as CalendarFragment?

      if (calendarFragment != null) {
        if (calendarFragment.closeSearch()) {
          return
        }
      }

      finish()
    }
  }

  override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
    // Checking for the "menu" key
    if (keyCode == KeyEvent.KEYCODE_MENU) {
      if (binding.drawer.isDrawerOpen(GravityCompat.START)) {
        binding.drawer.closeDrawers()
      } else {
        binding.drawer.openDrawer(GravityCompat.START)
      }
      return true
    } else {
      return super.onKeyDown(keyCode, event)
    }
  }

  private fun restartActivity(item: Int) {
    val intent = intent
    if (item == CONVERTER)
      intent.action = "CONVERTER_SHORTCUT"
    else if (item == COMPASS)
      intent.action = "COMPASS_SHORTCUT"
    else if (item == PREFERENCE)
      intent.action = "PREFERENCE_SHORTCUT"
    else if (item == ABOUT)
      intent.action = "ABOUT_SHORTCUT"

    finish()
    startActivity(intent)
  }

  fun selectItem(item: Int) {
    if (item == EXIT) {
      finish()
      return
    }

    if (menuPosition != item) {
      if (settingHasChanged && menuPosition == PREFERENCE) { // update on returning from preferences
        Utils.initUtils(this)
        UpdateUtils.update(applicationContext, true)
      }

      try {
        supportFragmentManager
            .beginTransaction()
            .replace(
                R.id.fragment_holder,
                fragments[item]?.newInstance() as Fragment,
                fragments[item]?.name
            ).commit()
        menuPosition = item
      } catch (e: Exception) {
        Log.e(TAG, item.toString() + " is selected as an index", e)
      }

    }

    adapter?.setSelectedItem(menuPosition)

    binding.drawer.closeDrawers()
  }

  companion object {

    private val CALENDAR = 1
    private val CONVERTER = 2
    private val COMPASS = 3
    val PREFERENCE = 4
    private val ABOUT = 5
    private val EXIT = 6
    // Default selected fragment
    private val DEFAULT = CALENDAR

    private var creationDate: CivilDate? = null
  }
}
