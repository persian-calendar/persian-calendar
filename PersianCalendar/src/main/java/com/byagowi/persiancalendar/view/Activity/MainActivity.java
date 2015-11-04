package com.byagowi.persiancalendar.view.Activity;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.byagowi.persiancalendar.Adapter.DrawerAdapter;
import com.byagowi.persiancalendar.ApplicationService;
import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.Utils;
import com.byagowi.persiancalendar.view.Fragment.AboutFragment;
import com.byagowi.persiancalendar.view.Fragment.ApplicationPreferenceFragment;
import com.byagowi.persiancalendar.view.Fragment.CalendarMainFragment;
import com.byagowi.persiancalendar.view.Fragment.CompassFragment;
import com.byagowi.persiancalendar.view.Fragment.ConverterFragment;

/**
 * Program activity for android
 *
 * @author ebraminio
 */
public class MainActivity extends AppCompatActivity {
    public static final int CALENDAR = 0;
    public static final int CONVERTER = 1;
    public static final int COMPASS = 2;
    public static final int PREFERENCE = 3;
    public static final int ABOUT = 4;
    public static final int EXIT = 5;

    public int menuPosition = 0;
    public Utils utils = Utils.getInstance();

    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        startService(new Intent(this, ApplicationService.class));

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            toolbar.setPadding(0, 48, 0, 0);  //48 = height status bar
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String key = prefs.getString("Theme", "");
        int theme = R.style.LightTheme;

        if (key.equals("LightTheme")) {
            theme = R.style.LightTheme;
        } else if (key.equals("DarkTheme")) {
            theme = R.style.DarkTheme;
        }
        setTheme(theme);

        RecyclerView navigation = (RecyclerView) findViewById(R.id.navigation_view);
        navigation.setHasFixedSize(true);
        DrawerAdapter adapter = new DrawerAdapter(this);
        navigation.setAdapter(adapter);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        navigation.setLayoutManager(layoutManager);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        final View appMainView = findViewById(R.id.app_main_layout);
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.openDrawer, R.string.closeDrawer) {
            int slidingDirection = +1;

            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    if (isRTL())
                        slidingDirection = -1;
                }
            }

            @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
            private boolean isRTL() {
                return getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    slidingAnimation(drawerView, slideOffset);
                }
            }

            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            private void slidingAnimation(View drawerView, float slideOffset) {
                appMainView.setTranslationX(slideOffset * drawerView.getWidth() * slidingDirection);
                drawerLayout.bringChildToFront(drawerView);
                drawerLayout.requestLayout();
            }
        };

        drawerLayout.setDrawerListener(drawerToggle);
        drawerToggle.syncState();

        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.fragment_holder, new CalendarMainFragment(), "CalendarMainFragment");
        transaction.commit();
    }

    public void onClickItem(View v, int position) {
        selectItem(position);
    }

    @Override
    public void onBackPressed() {
        if (menuPosition != CALENDAR) {
            selectItem(CALENDAR);
        } else {
            finish();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Checking for the "menu" key
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawers();
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    public void selectItem(int position) {
        switch (position) {

            case CALENDAR:
                if (menuPosition != CALENDAR) {
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_holder, new CalendarMainFragment(), "CalendarMainFragment")
                            .addToBackStack(null)
                            .commit();

                    menuPosition = CALENDAR;
                }

                break;

            case CONVERTER:
                if (menuPosition != CONVERTER) {
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_holder, new ConverterFragment())
                            .addToBackStack(null)
                            .commit();

                    menuPosition = CONVERTER;
                }

                break;

            case COMPASS:
                if (menuPosition != COMPASS) {
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_holder, new CompassFragment())
                            .addToBackStack(null)
                            .commit();

                    menuPosition = COMPASS;
                }

                break;

            case PREFERENCE:
                if (menuPosition != PREFERENCE) {
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_holder, new ApplicationPreferenceFragment())
                            .addToBackStack(null)
                            .commit();

                    menuPosition = PREFERENCE;
                }

                break;

            case ABOUT:
                if (menuPosition != ABOUT) {
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_holder, new AboutFragment())
                            .addToBackStack(null)
                            .commit();

                    menuPosition = ABOUT;
                }

                break;

            case EXIT:
                finish();
                break;
        }

        drawerLayout.closeDrawers();
    }
}
