package com.byagowi.persiancalendar.view.activity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.byagowi.persiancalendar.Constants;
import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.adapter.DrawerAdapter;
import com.byagowi.persiancalendar.databinding.ActivityMainBinding;
import com.byagowi.persiancalendar.service.ApplicationService;
import com.byagowi.persiancalendar.util.CalendarUtils;
import com.byagowi.persiancalendar.util.TypeFaceUtil;
import com.byagowi.persiancalendar.util.UIUtils;
import com.byagowi.persiancalendar.util.UpdateUtils;
import com.byagowi.persiancalendar.util.Utils;
import com.byagowi.persiancalendar.view.fragment.CalendarFragment;
import com.github.praytimes.Coordinate;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import calendar.CivilDate;

import static com.byagowi.persiancalendar.Constants.DEFAULT_APP_LANGUAGE;
import static com.byagowi.persiancalendar.Constants.LANG_EN_US;
import static com.byagowi.persiancalendar.Constants.LANG_UR;
import static com.byagowi.persiancalendar.Constants.PREF_APP_LANGUAGE;
import static com.byagowi.persiancalendar.Constants.PREF_NOTIFY_DATE;
import static com.byagowi.persiancalendar.Constants.PREF_PERSIAN_DIGITS;
import static com.byagowi.persiancalendar.Constants.PREF_SHOW_DEVICE_CALENDAR_EVENTS;
import static com.byagowi.persiancalendar.Constants.PREF_THEME;

/**
 * Program activity for android
 *
 * @author ebraminio
 */
public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final int CALENDAR = 0,
            CONVERTER = 1,
            COMPASS = 2,
            PREFERENCE = 3,
            ABOUT = 4,
            EXIT = 5,
            DEFAULT = CALENDAR; // Default selected fragment
    private ActivityMainBinding binding;
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

    private static CivilDate creationDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        UIUtils.setTheme(this);
        Utils.applyAppLanguage(this);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        Utils.initUtils(this);
        TypeFaceUtil.overrideFont(getApplicationContext(), "SERIF", "fonts/NotoNaskhArabic-Regular.ttf"); // font from assets: "assets/fonts/Roboto-Regular.ttf

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

        binding.navigationView.setHasFixedSize(true);
        binding.navigationView.setAdapter(new DrawerAdapter(this));
        binding.navigationView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        binding.navigationView.setLayoutManager(new LinearLayoutManager(this));

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

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);

        if (Utils.isShowDeviceCalendarEvents()) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
                UIUtils.askForCalendarPermission(this);
            }
        }

        switch (getSeason()) {
            case "SPRING":
                binding.seasonImage.setImageResource(R.drawable.spring);
                break;

            case "SUMMER":
                binding.seasonImage.setImageResource(R.drawable.summer);
                break;

            case "FALL":
                binding.seasonImage.setImageResource(R.drawable.fall);
                break;

            case "WINTER":
                binding.seasonImage.setImageResource(R.drawable.winter);
                break;
        }

        creationDate = CalendarUtils.getGregorianToday();
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
            boolean persianDigits;
            switch (sharedPreferences.getString(PREF_APP_LANGUAGE, DEFAULT_APP_LANGUAGE)) {
                case LANG_EN_US:
                    persianDigits = false;
                    break;
                case LANG_UR:
                    persianDigits = false;
                    break;
                default:
                    persianDigits = true;
            }

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(PREF_PERSIAN_DIGITS, persianDigits);
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
        if (!creationDate.equals(CalendarUtils.getGregorianToday())) {
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
        if (item == EXIT) {
            finish();
            return;
        }

        if (menuPosition != item) {
            if (settingHasChanged && menuPosition == PREFERENCE) { // update on returning from preferences
                Utils.initUtils(this);
                UpdateUtils.update(getApplicationContext(), true);
            }
            switch (item) {
                case DEFAULT:
                    Navigation.findNavController(this, R.id.nav_host_fragment).navigate(R.id.calendarFragment);
                    break;
                case CONVERTER:
                    Navigation.findNavController(this, R.id.nav_host_fragment).navigate(R.id.converterFragment);
                    break;
                case COMPASS:
                    Navigation.findNavController(this, R.id.nav_host_fragment).navigate(R.id.compassFragment);
                    break;
                case PREFERENCE:
                    Navigation.findNavController(this, R.id.nav_host_fragment).navigate(R.id.preferenceFragment);
                    break;
                case ABOUT:
                    Navigation.findNavController(this, R.id.nav_host_fragment).navigate(R.id.aboutFragment);
                    break;
            }
            menuPosition = item;
        }

        RecyclerView.Adapter adapter = binding.navigationView.getAdapter();
        if (adapter != null && adapter instanceof DrawerAdapter) {
            DrawerAdapter drawerAdapter = (DrawerAdapter) adapter;
            drawerAdapter.setSelectedItem(menuPosition);
        }

        binding.drawer.closeDrawers();
    }

    @Override
    public boolean onSupportNavigateUp() {
        return Navigation.findNavController(this, R.id.nav_host_fragment).navigateUp();
    }

}
