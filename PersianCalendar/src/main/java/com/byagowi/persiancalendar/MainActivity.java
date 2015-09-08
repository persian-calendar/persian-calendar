package com.byagowi.persiancalendar;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.byagowi.persiancalendar.util.DateFormatUtils;
import com.byagowi.persiancalendar.view.MonthFragment;

import calendar.CivilDate;
import calendar.DateConverter;
import calendar.PersianDate;

/**
 * Program activity for android
 *
 * @author ebraminio
 */
public class MainActivity extends FragmentActivity {
    private static final String TAG = "MainActivity";
    // I know, it is ugly, but user will not notify this and this will not have
    // a memory problem
    private static final int MONTHS_LIMIT = 1200;
    private DateFormatUtils dateFormatUtils;
    public Utils utils = Utils.getInstance();
    private ViewPager viewPager;
    private TextView calendarInfo;
    private Button resetButton;
    private PrayTimeActivityHelper prayTimeActivityHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        utils.setTheme(this);
        utils.loadLanguageFromSettings(this);
        super.onCreate(savedInstanceState);

        startService(new Intent(this, ApplicationService.class));

        dateFormatUtils = DateFormatUtils.getInstance(this);

        boolean removeTitle = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            if (!hasPermanentMenuKey()) {
                removeTitle = false;
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB
                && Build.VERSION.SDK_INT <= Build.VERSION_CODES.HONEYCOMB_MR2) {
            // if a tablet version is installed
            removeTitle = false;
        }
        if (android.os.Build.BRAND.equals("chromium")) {
            removeTitle = false;
        }
        if (removeTitle) {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
        }
        setContentView(R.layout.calendar);

        calendarInfo = (TextView) findViewById(R.id.calendar_info);

        // Pray Time
        prayTimeActivityHelper = new PrayTimeActivityHelper(this);
        prayTimeActivityHelper.fillPrayTime();

        // Load Holidays
        utils.loadHolidays(getResources().openRawResource(R.raw.holidays));

        // Reset button
        resetButton = (Button) findViewById(R.id.reset_button);
        resetButton.setText(getString(R.string.today));
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                bringTodayYearMonth();
                setFocusedDay(Utils.getToday());
            }
        });
        resetButton.setTypeface(Typeface.createFromAsset(this.getAssets(),
                "fonts/NotoNaskhArabic-Regular.ttf"));

        // Initializing the viewPager
        viewPager = (ViewPager) findViewById(R.id.calendar_pager);
        viewPager.setAdapter(createCalendarAdaptor());
        viewPager.setCurrentItem(MONTHS_LIMIT / 2);
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int arg0) {
                updateResetButtonState();
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }
        });

        // Initializing the view
        fillCalendarInfo(Utils.getToday());
    }

    @Override
    protected void onResume() {
        super.onResume();
        fillCalendarInfo(Utils.getToday());
    }

    private void updateResetButtonState() {
        if (viewPager.getCurrentItem() == MONTHS_LIMIT / 2) {
            resetButton.setVisibility(View.GONE);
            fillCalendarInfo(Utils.getToday());
        } else {
            resetButton.setVisibility(View.VISIBLE);
            clearInfo();
        }
    }

    private void clearInfo() {
        calendarInfo.setText("");
        prayTimeActivityHelper.clearInfo();
    }

    private void bringTodayYearMonth() {
        if (viewPager.getCurrentItem() != MONTHS_LIMIT / 2) {
            viewPager.setCurrentItem(MONTHS_LIMIT / 2);
            fillCalendarInfo(Utils.getToday());
        }
    }

    public void setFocusedDay(PersianDate persianDate) {
        fillCalendarInfo(persianDate);
    }

    private PagerAdapter createCalendarAdaptor() {
        return new FragmentPagerAdapter(
                getSupportFragmentManager()) {
            @Override
            public int getCount() {
                return MONTHS_LIMIT;
            }

            @Override
            public Fragment getItem(int position) {
                MonthFragment fragment = new MonthFragment();
                Bundle args = new Bundle();
                args.putInt("offset", position - MONTHS_LIMIT / 2);
                fragment.setArguments(args);
                return fragment;
            }
        };
    }

    private boolean isToday(CivilDate civilDate) {
        CivilDate today = new CivilDate();
        return today.getYear() == civilDate.getYear()
                && today.getMonth() == civilDate.getMonth()
                && today.getDayOfMonth() == civilDate.getDayOfMonth();
    }

    private void fillCalendarInfo(PersianDate persianDate) {
        CivilDate civilDate = DateConverter.persianToCivil(persianDate);
        char[] digits = utils.preferredDigits(this);
        utils.prepareTextView(calendarInfo);
        StringBuilder sb = new StringBuilder();

        if (isToday(civilDate)) {
            sb.append(getString(R.string.today)).append(":\n");
            resetButton.setVisibility(View.GONE);
        } else {
            resetButton.setVisibility(View.VISIBLE);
        }

        sb.append(dateFormatUtils.getWeekDayName(persianDate)).append(Utils.PERSIAN_COMMA)
                .append(" ")
                .append(utils.dateToString(persianDate, digits))
                .append("\n\n")
                .append(getString(R.string.equals_with))
                .append(":\n")
                .append(utils.dateToString(civilDate, digits))
                .append("\n")
                .append(utils.dateToString(DateConverter.civilToIslamic(civilDate), digits))
                .append("\n");
        calendarInfo.setText(Utils.textShaper(sb.toString()));

        prayTimeActivityHelper.setDate(civilDate.getYear(),
                civilDate.getMonth() - 1, civilDate.getDayOfMonth());
        prayTimeActivityHelper.fillPrayTime();
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private boolean hasPermanentMenuKey() {
        return ViewConfiguration.get(this).hasPermanentMenuKey();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_dateconverter) {
            startActivity(new Intent(this, ConverterActivity.class));
        } else if (itemId == R.id.menu_compass) {
            startActivity(new Intent(this, CompassActivity.class));
        } else if (itemId == R.id.menu_settings) {
            startActivityForResult(
                    new Intent(this, ApplicationPreference.class), 0);
        } else if (itemId == R.id.menu_about) {
            startActivity(new Intent(this, AboutActivity.class));
        } else if (itemId == R.id.menu_exit) {
            finish();
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Restart activity
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }
}
