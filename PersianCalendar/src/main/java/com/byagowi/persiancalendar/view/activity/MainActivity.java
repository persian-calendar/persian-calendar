package com.byagowi.persiancalendar.view.activity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.byagowi.persiancalendar.Constants;
import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.databinding.ActivityMainBinding;
import com.byagowi.persiancalendar.service.ApplicationService;
import com.byagowi.persiancalendar.util.CalendarUtils;
import com.byagowi.persiancalendar.util.TypefaceUtils;
import com.byagowi.persiancalendar.util.UIUtils;
import com.byagowi.persiancalendar.util.UpdateUtils;
import com.byagowi.persiancalendar.util.Utils;
import com.byagowi.persiancalendar.view.fragment.AboutFragment;
import com.byagowi.persiancalendar.view.fragment.CalendarFragment;
import com.byagowi.persiancalendar.view.fragment.CompassFragment;
import com.byagowi.persiancalendar.view.fragment.ConverterFragment;
import com.byagowi.persiancalendar.view.preferences.SettingsFragment;
import com.github.praytimes.Coordinate;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import static com.byagowi.persiancalendar.Constants.DEFAULT_APP_LANGUAGE;
import static com.byagowi.persiancalendar.Constants.LANG_AR;
import static com.byagowi.persiancalendar.Constants.LANG_EN_IR;
import static com.byagowi.persiancalendar.Constants.LANG_EN_US;
import static com.byagowi.persiancalendar.Constants.LANG_FA;
import static com.byagowi.persiancalendar.Constants.LANG_FA_AF;
import static com.byagowi.persiancalendar.Constants.LANG_PS;
import static com.byagowi.persiancalendar.Constants.LANG_UR;
import static com.byagowi.persiancalendar.Constants.LIGHT_THEME;
import static com.byagowi.persiancalendar.Constants.PREF_APP_LANGUAGE;
import static com.byagowi.persiancalendar.Constants.PREF_HOLIDAY_TYPES;
import static com.byagowi.persiancalendar.Constants.PREF_MAIN_CALENDAR_KEY;
import static com.byagowi.persiancalendar.Constants.PREF_NOTIFY_DATE;
import static com.byagowi.persiancalendar.Constants.PREF_OTHER_CALENDARS_KEY;
import static com.byagowi.persiancalendar.Constants.PREF_PERSIAN_DIGITS;
import static com.byagowi.persiancalendar.Constants.PREF_SHOW_DEVICE_CALENDAR_EVENTS;
import static com.byagowi.persiancalendar.Constants.PREF_THEME;

/**
 * Program activity for android
 *
 * @author ebraminio
 */
public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener, NavigationView.OnNavigationItemSelectedListener {

    private static final int CALENDAR = 0,
            CONVERTER = 1,
            COMPASS = 2,
            PREFERENCE = 3,
            ABOUT = 4,
            EXIT = 5,
            DEFAULT = CALENDAR; // Default selected fragment
    private final String TAG = MainActivity.class.getName();
    private ActivityMainBinding binding;
    private final Class<?>[] fragments = {
            CalendarFragment.class,
            ConverterFragment.class,
            CompassFragment.class,
            SettingsFragment.class,
            AboutFragment.class
    };
    private int menuPosition = -1; // it should be zero otherwise #selectItem won't be called

    // https://stackoverflow.com/a/3410200
    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    // A never used migration
    private void oneTimeClockDisablingForAndroid5LE() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
            String key = "oneTimeClockDisablingForAndroid5LE";
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            if (!prefs.getBoolean(key, false)) {
                SharedPreferences.Editor edit = prefs.edit();
                edit.putBoolean(Constants.PREF_WIDGET_CLOCK, false);
                edit.putBoolean(key, true);
                edit.apply();
            }
        }
    }

    private static long creationDateJdn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        setTheme(UIUtils.getThemeFromName(prefs.getString(PREF_THEME, LIGHT_THEME)));

        Utils.applyAppLanguage(this);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        Utils.initUtils(this);

        TypefaceUtils.overrideFont("SERIF",
                TypefaceUtils.getAppFont(getApplicationContext()));

        Utils.startEitherServiceOrWorker(this);

        // Doesn't matter apparently
        // oneTimeClockDisablingForAndroid5LE();
        UpdateUtils.setDeviceCalendarEvents(getApplicationContext());
        UpdateUtils.update(getApplicationContext(), false);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        setSupportActionBar(binding.toolbar);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            binding.toolbar.setPadding(0, getStatusBarHeight(), 0, 0);

        }

        boolean isRTL = UIUtils.isRTL(this);

        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, binding.drawer, binding.toolbar, R.string.openDrawer, R.string.closeDrawer) {
            int slidingDirection = isRTL ? -1 : +1;

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                slidingAnimation(drawerView, slideOffset);
            }


            private void slidingAnimation(View drawerView, float slideOffset) {
                binding.appMainLayout.setTranslationX(slideOffset * drawerView.getWidth() * slidingDirection);
                binding.drawer.bringChildToFront(drawerView);
                binding.drawer.requestLayout();
            }
        };

        binding.drawer.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
        String action = getIntent() != null ? getIntent().getAction() : null;
        if ("COMPASS_SHORTCUT".equals(action)) {
            selectItem(COMPASS);
        } else if ("PREFERENCE_SHORTCUT".equals(action)) {
            selectItem(PREFERENCE);
        } else if ("CONVERTER_SHORTCUT".equals(action)) {
            selectItem(CONVERTER);
        } else if ("ABOUT_SHORTCUT".equals(action)) {
            selectItem(ABOUT);
        } else {
            selectItem(DEFAULT);
        }

        prefs.registerOnSharedPreferenceChangeListener(this);

        if (Utils.isShowDeviceCalendarEvents()) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
                UIUtils.askForCalendarPermission(this);
            }
        }

        binding.navigation.setNavigationItemSelectedListener(this);
        ImageView seasonImage = binding.navigation
                .getHeaderView(0).findViewById(R.id.season_image);
        switch (getSeason()) {
            case "SPRING":
                seasonImage.setImageResource(R.drawable.spring);
                break;

            case "SUMMER":
                seasonImage.setImageResource(R.drawable.summer);
                break;

            case "FALL":
                seasonImage.setImageResource(R.drawable.fall);
                break;

            case "WINTER":
                seasonImage.setImageResource(R.drawable.winter);
                break;
        }

        if (prefs.getString(PREF_APP_LANGUAGE, "N/A").equals("N/A")
                && !prefs.getBoolean(Constants.CHANGE_LANGUAGE_IS_PROMOTED_ONCE, false)) {
            Snackbar snackbar = Snackbar.make(binding.coordinator, "✖  Change app language?",
                    10000);
            View snackbarView = snackbar.getView();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                snackbarView.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
            }
            TextView text = snackbar.getView().findViewById(com.google.android.material.R.id.snackbar_text);
            text.setTextColor(Color.WHITE);

            snackbarView.setOnClickListener(v -> snackbar.dismiss());
            snackbar.setAction("Settings", view -> {
                menuPosition = PREFERENCE;
                SharedPreferences.Editor edit = prefs.edit();
                edit.putString(Constants.PREF_APP_LANGUAGE, Constants.LANG_EN_US);
                edit.putString(PREF_MAIN_CALENDAR_KEY, "GREGORIAN");
                edit.putString(PREF_OTHER_CALENDARS_KEY, "ISLAMIC,SHAMSI");
                edit.putStringSet(PREF_HOLIDAY_TYPES, new HashSet<>());
                edit.apply();
                restartActivity();
            });
            snackbar.setActionTextColor(getResources().getColor(R.color.dark_accent));
            snackbar.show();

            // Show this snackbar only once
            SharedPreferences.Editor edit = prefs.edit();
            edit.putBoolean(Constants.CHANGE_LANGUAGE_IS_PROMOTED_ONCE, true);
            edit.apply();
        }

        creationDateJdn = CalendarUtils.getTodayJdn();
        Utils.applyAppLanguage(this);
    }

    private String getSeason() {
        boolean isSouthernHemisphere = false;
        Coordinate coordinate = Utils.getCoordinate(this);
        if (coordinate != null && coordinate.getLatitude() < 0) {
            isSouthernHemisphere = true;
        }

        int month = CalendarUtils.getPersianToday().getMonth();
        if (isSouthernHemisphere) month = ((month + 6 - 1) % 12) + 1;

        if (month < 4) return "SPRING";
        else if (month < 7) return "SUMMER";
        else if (month < 10) return "FALL";
        else return "WINTER";
    }

    boolean settingHasChanged = false;

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        settingHasChanged = true;
        if (key.equals(PREF_APP_LANGUAGE)) {
            boolean persianDigits = false;
            boolean changeToAfghanistanHolidays = false;
            boolean changeToIslamicCalendar = false;
            boolean changeToGregorianCalendar = false;
            boolean changeToPersianCalendar = false;
            boolean changeToIranEvents = false;
            switch (sharedPreferences.getString(PREF_APP_LANGUAGE, DEFAULT_APP_LANGUAGE)) {
                case LANG_EN_US:
                    persianDigits = false;
                    changeToGregorianCalendar = true;
                    break;
                case LANG_FA:
                    persianDigits = true;
                    changeToPersianCalendar = true;
                    changeToIranEvents = true;
                    break;
                case LANG_EN_IR:
                    persianDigits = false;
                    changeToPersianCalendar = true;
                    changeToIranEvents = true;
                    break;
                case LANG_UR:
                    persianDigits = false;
                    changeToGregorianCalendar = true;
                    break;
                case LANG_AR:
                    persianDigits = true;
                    changeToIslamicCalendar = true;
                    break;
                case LANG_FA_AF:
                    persianDigits = true;
                    changeToPersianCalendar = true;
                    changeToAfghanistanHolidays = true;
                    break;
                case LANG_PS:
                    persianDigits = true;
                    changeToPersianCalendar = true;
                    changeToAfghanistanHolidays = true;
                    break;
                default:
                    persianDigits = true;
            }

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(PREF_PERSIAN_DIGITS, persianDigits);
            // Enable Afghanistan holidays when Dari or Pashto is set
            if (changeToAfghanistanHolidays) {
                Set<String> currentHolidays =
                        sharedPreferences.getStringSet(PREF_HOLIDAY_TYPES, new HashSet<>());

                if (currentHolidays.isEmpty() ||
                        (currentHolidays.size() == 1 && currentHolidays.contains("iran_holidays"))) {
                    editor.putStringSet(PREF_HOLIDAY_TYPES,
                            new HashSet<>(Collections.singletonList("afghanistan_holidays")));
                }
            }
            if (changeToIranEvents) {
                Set<String> currentHolidays =
                        sharedPreferences.getStringSet(PREF_HOLIDAY_TYPES, new HashSet<>());

                if (currentHolidays.isEmpty() ||
                        (currentHolidays.size() == 1 && currentHolidays.contains("afghanistan_holidays"))) {
                    editor.putStringSet(PREF_HOLIDAY_TYPES,
                            new HashSet<>(Collections.singletonList("iran_holidays")));
                }
            }
            if (changeToGregorianCalendar) {
                editor.putString(PREF_MAIN_CALENDAR_KEY, "GREGORIAN");
                editor.putString(PREF_OTHER_CALENDARS_KEY, "ISLAMIC,SHAMSI");
            } else if (changeToIslamicCalendar) {
                editor.putString(PREF_MAIN_CALENDAR_KEY, "ISLAMIC");
                editor.putString(PREF_OTHER_CALENDARS_KEY, "GREGORIAN,SHAMSI");
            } else if (changeToPersianCalendar) {
                editor.putString(PREF_MAIN_CALENDAR_KEY, "SHAMSI");
                editor.putString(PREF_OTHER_CALENDARS_KEY, "GREGORIAN,ISLAMIC");
            }
            editor.apply();
        }

        if (key.equals(PREF_SHOW_DEVICE_CALENDAR_EVENTS)) {
            if (sharedPreferences.getBoolean(PREF_SHOW_DEVICE_CALENDAR_EVENTS, true)) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
                    UIUtils.askForCalendarPermission(this);
                }
            }
        }

        if (key.equals(PREF_APP_LANGUAGE) || key.equals(PREF_THEME)) {
            restartActivity();
        }

        if (key.equals(PREF_NOTIFY_DATE)) {
            if (!sharedPreferences.getBoolean(PREF_NOTIFY_DATE, true)) {
                stopService(new Intent(this, ApplicationService.class));
                Utils.startEitherServiceOrWorker(getApplicationContext());
            }
        }

        Utils.updateStoredPreference(this);
        UpdateUtils.update(getApplicationContext(), true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Constants.CALENDAR_READ_PERMISSION_REQUEST_CODE) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR)
                    == PackageManager.PERMISSION_GRANTED) {
                UIUtils.toggleShowCalendarOnPreference(this, true);
                if (menuPosition == CALENDAR) {
                    restartActivity();
                }
            } else {
                UIUtils.toggleShowCalendarOnPreference(this, false);
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Utils.initUtils(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            binding.drawer.setLayoutDirection(UIUtils.isRTL(this) ? View.LAYOUT_DIRECTION_RTL : View.LAYOUT_DIRECTION_LTR);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Utils.applyAppLanguage(this);
        UpdateUtils.update(getApplicationContext(), false);
        if (creationDateJdn != CalendarUtils.getTodayJdn()) {
            restartActivity();
        }
    }

    @Override
    public void onBackPressed() {
        if (binding.drawer.isDrawerOpen(GravityCompat.START)) {
            binding.drawer.closeDrawers();
        } else if (menuPosition != DEFAULT) {
            selectItem(DEFAULT);
        } else {
            CalendarFragment calendarFragment = (CalendarFragment) getSupportFragmentManager()
                    .findFragmentByTag(CalendarFragment.class.getName());

            if (calendarFragment != null) {
                if (calendarFragment.closeSearch()) {
                    return;
                }
            }

            finish();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Checking for the "menu" key
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            if (binding.drawer.isDrawerOpen(GravityCompat.START)) {
                binding.drawer.closeDrawers();
            } else {
                binding.drawer.openDrawer(GravityCompat.START);
            }
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    public void restartActivity() {
        Intent intent = getIntent();
        if (menuPosition == CONVERTER)
            intent.setAction("CONVERTER_SHORTCUT");
        else if (menuPosition == COMPASS)
            intent.setAction("COMPASS_SHORTCUT");
        else if (menuPosition == PREFERENCE)
            intent.setAction("PREFERENCE_SHORTCUT");
        else if (menuPosition == ABOUT)
            intent.setAction("ABOUT_SHORTCUT");

        finish();
        startActivity(intent);
    }

    public void bringPreferences() {
        selectItem(PREFERENCE);
    }

    public void selectItem(int item) {
        onNavigationItemSelected(binding.navigation.getMenu().getItem(item));
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.exit) {
            finish();
            return true;
        }

        menuItem.setCheckable(true);
        menuItem.setChecked(true);

        int item = -1;
        switch (menuItem.getItemId()) {
            case R.id.calendar:
                item = CALENDAR;
                break;
            case R.id.converter:
                item = CONVERTER;
                break;
            case R.id.compass:
                item = COMPASS;
                break;
            case R.id.settings:
                item = PREFERENCE;
                break;
            case R.id.about:
                item = ABOUT;
                break;
        }

        if (menuPosition != item) {
            if (settingHasChanged && menuPosition == PREFERENCE) { // update on returning from preferences
                Utils.initUtils(this);
                UpdateUtils.update(getApplicationContext(), true);
            }

            try {
                Fragment fragment = (Fragment) fragments[item].newInstance();
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_holder, fragment, fragments[item].getName())
                        .commit();
                menuPosition = item;
            } catch (Exception e) {
                Log.e(TAG, item + " is selected as an index", e);
            }
        }

        binding.drawer.closeDrawers();
        return true;
    }
}
