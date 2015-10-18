package com.byagowi.persiancalendar.view.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.byagowi.persiancalendar.Adapter.DrawerAdapter;
import com.byagowi.persiancalendar.ApplicationService;
import com.byagowi.persiancalendar.Interface.ClickListener;
import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.Utils;
import com.byagowi.persiancalendar.view.Fragment.AboutFragment;
import com.byagowi.persiancalendar.view.Fragment.ApplicationPreferenceFragment;
import com.byagowi.persiancalendar.view.Fragment.CalendarFragment;
import com.byagowi.persiancalendar.view.Fragment.CompassFragment;
import com.byagowi.persiancalendar.view.Fragment.ConverterFragment;

/**
 * Program activity for android
 *
 * @author ebraminio
 */
public class MainActivity extends AppCompatActivity implements ClickListener {
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
            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            toolbar.setPadding(0, 48, 0, 0);  //48 = height status bar
        }

        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(this);
        String key = prefs.getString("Theme", "");
        int theme = R.style.LightTheme;

        if (key.equals("LightTheme")) {
            theme = R.style.LightTheme;
        } else if (key.equals("DarkTheme")) {
            theme = R.style.DarkTheme;
        }
        setTheme(theme);

        toolbar.setBackgroundColor(getResources().getColor(R.color.first_row_background_color));

        RecyclerView navigation = (RecyclerView) findViewById(R.id.navigation_view);
        navigation.setHasFixedSize(true);
        DrawerAdapter adapter = new DrawerAdapter(this, this);
        navigation.setAdapter(adapter);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        navigation.setLayoutManager(layoutManager);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.openDrawer, R.string.closeDrawer) {

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };

        drawerLayout.setDrawerListener(drawerToggle);
        drawerToggle.syncState();

        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.fragment_holder, new CalendarFragment());
        transaction.commit();
    }

//    @Override  // TODO: 10/15/15 to replace app setting
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//         Restart activity
//        Intent intent = getIntent();
//        finish();
//        startActivity(intent);
//    }

    @Override
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

    public void selectItem(int position) {
        switch (position) {

            case CALENDAR:
                if (menuPosition != CALENDAR) {
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_holder, new CalendarFragment())
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
