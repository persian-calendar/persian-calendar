package com.byagowi.persiancalendar;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
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
import com.byagowi.persiancalendar.Interface.ClickListener;

/**
 * Program activity for android
 *
 * @author ebraminio
 */
public class MainActivity extends AppCompatActivity implements ClickListener {
    public Utils utils = Utils.getInstance();

    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        startService(new Intent(this, ApplicationService.class));

        setContentView(R.layout.calendar);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

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
    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        int itemId = item.getItemId();
//        if (itemId == R.id.menu_dateconverter) {
//            startActivity(new Intent(this, ConverterActivity.class));
//        } else if (itemId == R.id.menu_compass) {
//            startActivity(new Intent(this, CompassActivity.class));
//        } else if (itemId == R.id.menu_settings) {
//            startActivityForResult(
//                    new Intent(this, ApplicationPreference.class), 0);
//        } else if (itemId == R.id.menu_about) {
//            startActivity(new Intent(this, AboutActivity.class));
//        } else if (itemId == R.id.menu_exit) {
//            finish();
//        }
//        return false;
//    }

//    @Override  // TODO: 10/15/15  this cod for reload app after setting
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

    public void selectItem(int position) {
        switch (position) {

            case 0:

                break;
        }
        drawerLayout.closeDrawers();
    }
}
