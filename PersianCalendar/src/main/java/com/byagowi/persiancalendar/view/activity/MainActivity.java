package com.byagowi.persiancalendar.view.activity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import com.byagowi.persiancalendar.di.dependencies.AppDependency;
import com.byagowi.persiancalendar.di.dependencies.MainActivityDependency;
import com.byagowi.persiancalendar.praytimes.Coordinate;
import com.byagowi.persiancalendar.service.ApplicationService;
import com.byagowi.persiancalendar.util.CalendarType;
import com.byagowi.persiancalendar.util.CalendarUtils;
import com.byagowi.persiancalendar.util.TypefaceUtils;
import com.byagowi.persiancalendar.util.UIUtils;
import com.byagowi.persiancalendar.util.UpdateUtils;
import com.byagowi.persiancalendar.util.Utils;
import com.byagowi.persiancalendar.view.fragment.CalendarFragment;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import dagger.android.support.DaggerAppCompatActivity;

import static com.byagowi.persiancalendar.Constants.DEFAULT_APP_LANGUAGE;
import static com.byagowi.persiancalendar.Constants.DEFAULT_WEEK_ENDS;
import static com.byagowi.persiancalendar.Constants.DEFAULT_WEEK_START;
import static com.byagowi.persiancalendar.Constants.LANG_AR;
import static com.byagowi.persiancalendar.Constants.LANG_EN_IR;
import static com.byagowi.persiancalendar.Constants.LANG_EN_US;
import static com.byagowi.persiancalendar.Constants.LANG_FA;
import static com.byagowi.persiancalendar.Constants.LANG_FA_AF;
import static com.byagowi.persiancalendar.Constants.LANG_PS;
import static com.byagowi.persiancalendar.Constants.LANG_UR;
import static com.byagowi.persiancalendar.Constants.PREF_APP_LANGUAGE;
import static com.byagowi.persiancalendar.Constants.PREF_HOLIDAY_TYPES;
import static com.byagowi.persiancalendar.Constants.PREF_MAIN_CALENDAR_KEY;
import static com.byagowi.persiancalendar.Constants.PREF_NOTIFY_DATE;
import static com.byagowi.persiancalendar.Constants.PREF_OTHER_CALENDARS_KEY;
import static com.byagowi.persiancalendar.Constants.PREF_PERSIAN_DIGITS;
import static com.byagowi.persiancalendar.Constants.PREF_SHOW_DEVICE_CALENDAR_EVENTS;
import static com.byagowi.persiancalendar.Constants.PREF_THEME;
import static com.byagowi.persiancalendar.Constants.PREF_WEEK_ENDS;
import static com.byagowi.persiancalendar.Constants.PREF_WEEK_START;


/**
 * Program activity for android
 *
 * @author ebraminio
 */
public class MainActivity extends DaggerAppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener, NavigationView.OnNavigationItemSelectedListener {
    @Inject
    AppDependency appDependency; // same object from App
    @Inject
    MainActivityDependency mainActivityDependency;
    private long creationDateJdn;
    private ActionBar actionBar;
    private boolean settingHasChanged = false;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Don't replace below with appDependency.getSharedPreferences() ever
        // as the injection won't happen at the right time
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        setTheme(UIUtils.getThemeFromName(Utils.getThemeFromPreference(prefs)));

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
            // https://learnpainless.com/android/material/make-fully-android-transparent-status-bar
            Window win = getWindow();
            WindowManager.LayoutParams winParams = win.getAttributes();
            winParams.flags &= ~WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
            win.setAttributes(winParams);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        boolean isRTL = UIUtils.isRTL(this);

        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, binding.drawer, binding.toolbar, R.string.openDrawer, R.string.closeDrawer) {
            int slidingDirection = isRTL ? -1 : +1;

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                slidingAnimation(drawerView, slideOffset / 1.5f);
            }


            private void slidingAnimation(View drawerView, float slideOffset) {
                binding.appMainLayout.setTranslationX(slideOffset * drawerView.getWidth() * slidingDirection);
                binding.drawer.bringChildToFront(drawerView);
                binding.drawer.requestLayout();
            }
        };

        binding.drawer.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        Intent intent = getIntent();
        if (intent != null) {
            String action = intent.getAction();
            if ("COMPASS".equals(action)) navigateTo(R.id.compass);
            else if ("LEVEL".equals(action)) navigateTo(R.id.level);
            else if ("CONVERTER".equals(action)) navigateTo(R.id.converter);
            else if ("SETTINGS".equals(action)) navigateTo(R.id.settings);
            else if ("DEVICE".equals(action)) navigateTo(R.id.deviceInfo);
            else navigateTo(R.id.calendar);

            // So it won't happen again if the activity restarted
            intent.setAction("");
        }

        prefs.registerOnSharedPreferenceChangeListener(this);

        if (Utils.isShowDeviceCalendarEvents()) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
                UIUtils.askForCalendarPermission(this);
            }
        }

        binding.navigation.setNavigationItemSelectedListener(this);

        ((ImageView) binding.navigation.getHeaderView(0).findViewById(R.id.season_image))
                .setImageResource(getSeasonImage());

        String appLanguage = prefs.getString(PREF_APP_LANGUAGE, "N/A");
        if (appLanguage == null) appLanguage = "N/A";
        if (appLanguage.equals("N/A")
                && !prefs.getBoolean(Constants.CHANGE_LANGUAGE_IS_PROMOTED_ONCE, false)) {
            Snackbar snackbar = Snackbar.make(getCoordinator(), "✖  Change app language?",
                    7000);
            View snackbarView = snackbar.getView();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                snackbarView.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
            }
            TextView text = snackbar.getView().findViewById(com.google.android.material.R.id.snackbar_text);
            text.setTextColor(Color.WHITE);

            snackbarView.setOnClickListener(v -> snackbar.dismiss());
            snackbar.setAction("Settings", view -> {
                SharedPreferences.Editor edit = prefs.edit();
                edit.putString(Constants.PREF_APP_LANGUAGE, Constants.LANG_EN_US);
                edit.putString(PREF_MAIN_CALENDAR_KEY, "GREGORIAN");
                edit.putString(PREF_OTHER_CALENDARS_KEY, "ISLAMIC,SHAMSI");
                edit.putStringSet(PREF_HOLIDAY_TYPES, new HashSet<>());
                edit.apply();

                restartToSettings();
            });
            snackbar.setActionTextColor(getResources().getColor(R.color.dark_accent));
            snackbar.show();

            // Show this snackbar only once
            SharedPreferences.Editor edit = prefs.edit();
            edit.putBoolean(Constants.CHANGE_LANGUAGE_IS_PROMOTED_ONCE, true);
            edit.apply();
        }

        actionBar = getSupportActionBar();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            binding.appbarLayout.setOutlineProvider(null);
        }

        creationDateJdn = CalendarUtils.getTodayJdn();

        if (Utils.getMainCalendar() == CalendarType.SHAMSI &&
                Utils.isIranHolidaysEnabled() &&
                (CalendarUtils.getTodayOfCalendar(CalendarType.SHAMSI).getYear() > Utils.getMaxSupportedYear())) {
            Snackbar snackbar = Snackbar.make(getCoordinator(), getString(R.string.outdated_app),
                    10000);
            TextView text = snackbar.getView().findViewById(com.google.android.material.R.id.snackbar_text);
            text.setTextColor(Color.WHITE);

            snackbar.setAction(getString(R.string.update), view -> {
                final String appPackageName = getPackageName();
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                }
            });
            snackbar.setActionTextColor(getResources().getColor(R.color.dark_accent));
            snackbar.show();
        }

        Utils.applyAppLanguage(this);
    }

    public void navigateTo(@IdRes int id) {
        MenuItem menuItem = binding.navigation.getMenu().findItem(
                id == R.id.level ? R.id.compass : id); // We don't have a menu entry for compass, so
        if (menuItem != null) {
            menuItem.setCheckable(true);
            menuItem.setChecked(true);
        }

        if (settingHasChanged) { // update when checked menu item is changed
            Utils.initUtils(this);
            UpdateUtils.update(getApplicationContext(), true);
            settingHasChanged = false; // reset for the next time
        }

        Navigation.findNavController(this, R.id.nav_host_fragment)
                .navigate(id, null, null);
    }

    public CoordinatorLayout getCoordinator() {
        return binding.coordinator;
    }

    @DrawableRes
    private int getSeasonImage() {
        boolean isSouthernHemisphere = false;
        Coordinate coordinate = Utils.getCoordinate(this);
        if (coordinate != null && coordinate.getLatitude() < 0) {
            isSouthernHemisphere = true;
        }

        int month = CalendarUtils.getTodayOfCalendar(CalendarType.SHAMSI).getMonth();
        if (isSouthernHemisphere) month = ((month + 6 - 1) % 12) + 1;

        if (month < 4) return R.drawable.spring;
        else if (month < 7) return R.drawable.summer;
        else if (month < 10) return R.drawable.fall;
        else return R.drawable.winter;
    }

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
            boolean removeAllEvents = false;
            String lang = sharedPreferences.getString(PREF_APP_LANGUAGE, DEFAULT_APP_LANGUAGE);
            if (lang == null) lang = "";
            switch (lang) {
                case LANG_EN_US:
                    changeToGregorianCalendar = true;
                    removeAllEvents = true;
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

                if (currentHolidays == null || currentHolidays.isEmpty() ||
                        (currentHolidays.size() == 1 && currentHolidays.contains("iran_holidays"))) {
                    editor.putStringSet(PREF_HOLIDAY_TYPES,
                            new HashSet<>(Collections.singletonList("afghanistan_holidays")));
                }
            }
            if (changeToIranEvents) {
                Set<String> currentHolidays =
                        sharedPreferences.getStringSet(PREF_HOLIDAY_TYPES, new HashSet<>());

                if (currentHolidays == null || currentHolidays.isEmpty() ||
                        (currentHolidays.size() == 1 && currentHolidays.contains("afghanistan_holidays"))) {
                    editor.putStringSet(PREF_HOLIDAY_TYPES,
                            new HashSet<>(Collections.singletonList("iran_holidays")));
                }
            }
            if (removeAllEvents) {
                Set<String> currentHolidays =
                        sharedPreferences.getStringSet(PREF_HOLIDAY_TYPES, new HashSet<>());

                if (currentHolidays == null || currentHolidays.isEmpty() ||
                        (currentHolidays.size() == 1 && currentHolidays.contains("iran_holidays"))) {
                    editor.putStringSet(PREF_HOLIDAY_TYPES, new HashSet<>());
                }
            }
            if (changeToGregorianCalendar) {
                editor.putString(PREF_MAIN_CALENDAR_KEY, "GREGORIAN");
                editor.putString(PREF_OTHER_CALENDARS_KEY, "ISLAMIC,SHAMSI");
                editor.putString(PREF_WEEK_START, "1");
                editor.putStringSet(PREF_WEEK_ENDS, new HashSet<>(Collections.singletonList("1")));
            } else if (changeToIslamicCalendar) {
                editor.putString(PREF_MAIN_CALENDAR_KEY, "ISLAMIC");
                editor.putString(PREF_OTHER_CALENDARS_KEY, "GREGORIAN,SHAMSI");
                editor.putString(PREF_WEEK_START, DEFAULT_WEEK_START);
                editor.putStringSet(PREF_WEEK_ENDS, DEFAULT_WEEK_ENDS);
            } else if (changeToPersianCalendar) {
                editor.putString(PREF_MAIN_CALENDAR_KEY, "SHAMSI");
                editor.putString(PREF_OTHER_CALENDARS_KEY, "GREGORIAN,ISLAMIC");
                editor.putString(PREF_WEEK_START, DEFAULT_WEEK_START);
                editor.putStringSet(PREF_WEEK_ENDS, DEFAULT_WEEK_ENDS);
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
            restartToSettings();
        }

        if (key.equals(PREF_NOTIFY_DATE)) {
            if (!sharedPreferences.getBoolean(PREF_NOTIFY_DATE, true)) {
                stopService(new Intent(this, ApplicationService.class));
                Utils.startEitherServiceOrWorker(getApplicationContext());
            }
        }

        Utils.updateStoredPreference(this);
        UpdateUtils.update(getApplicationContext(), true);

        appDependency.getLocalBroadcastManager()
                .sendBroadcast(new Intent(Constants.LOCAL_INTENT_UPDATE_PREFERENCE));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Constants.CALENDAR_READ_PERMISSION_REQUEST_CODE) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR)
                    == PackageManager.PERMISSION_GRANTED) {
                UIUtils.toggleShowDeviceCalendarOnPreference(this, true);
                NavDestination currentDestination = Navigation
                        .findNavController(this, R.id.nav_host_fragment)
                        .getCurrentDestination();
                if (currentDestination != null && currentDestination.getId() == R.id.calendar) {
                    restartActivity();
                }
            } else {
                UIUtils.toggleShowDeviceCalendarOnPreference(this, false);
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
        finish();
        startActivity(intent);
    }

    public void restartToSettings() {
        Intent intent = getIntent();
        intent.setAction("SETTINGS");
        finish();
        startActivity(intent);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.exit) {
            finish();
            return true;
        }

        binding.drawer.closeDrawers();
        navigateTo(menuItem.getItemId());
        return true;
    }

    public void setTitleAndSubtitle(String title, String subtitle) {
        actionBar.setTitle(title);
        actionBar.setSubtitle(subtitle);
    }

    @Override
    public void onBackPressed() {
        if (binding.drawer.isDrawerOpen(GravityCompat.START)) {
            binding.drawer.closeDrawers();
        } else {
            CalendarFragment calendarFragment = (CalendarFragment) getSupportFragmentManager()
                    .findFragmentByTag(CalendarFragment.class.getName());

            if (calendarFragment != null) {
                if (calendarFragment.closeSearch())
                    return;
            }

            NavDestination currentDestination = Navigation
                    .findNavController(this, R.id.nav_host_fragment)
                    .getCurrentDestination();
            if (currentDestination == null || (currentDestination.getId() == R.id.calendar))
                finish();
            else
                navigateTo(R.id.calendar);
        }
    }
}
