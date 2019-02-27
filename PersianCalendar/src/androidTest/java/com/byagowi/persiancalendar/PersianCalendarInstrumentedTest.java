package com.byagowi.persiancalendar;

import android.content.ClipboardManager;
import android.content.Context;

import com.byagowi.persiancalendar.calendar.CivilDate;
import com.byagowi.persiancalendar.calendar.IslamicDate;
import com.byagowi.persiancalendar.utils.CalendarType;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import androidx.test.InstrumentationRegistry;
import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import static androidx.test.InstrumentationRegistry.getInstrumentation;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class PersianCalendarInstrumentedTest {

    @Rule
    public ActivityTestRule<MainActivity> mainActivityTestRule =
            new ActivityTestRule<>(MainActivity.class);
    private MainActivity mainActivity;

    @Before
    public void setActivity() {
        mainActivity = mainActivityTestRule.getActivity();
    }

    @Test
    public void initial_test() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.byagowi.persiancalendar", appContext.getPackageName());
    }

    @Test
    public void test_if_date_copy_works() throws ExecutionException, InterruptedException {
        FutureTask<ClipboardManager> futureResult = new FutureTask<>(() -> (ClipboardManager)
                getInstrumentation().getTargetContext().getApplicationContext()
                        .getSystemService(Context.CLIPBOARD_SERVICE));

        mainActivity.runOnUiThread(futureResult);

        ClipboardManager clipboardManager = futureResult.get();

        onView(withId(R.id.first_calendar_date)).perform(click());
        assertEquals(Utils.formatDate(Utils.getTodayOfCalendar(CalendarType.SHAMSI)),
                clipboardManager.getPrimaryClip().getItemAt(0).getText());
    }

    @Test
    public void open_all_parts_of_the_app() {
        onView(withId(R.id.drawer)).perform(DrawerActions.open());
        onView(withText(R.string.date_converter)).perform(click());
        onView(withId(R.id.drawer)).perform(DrawerActions.open());
        onView(withText(R.string.compass)).perform(click());
        onView(withId(R.id.drawer)).perform(DrawerActions.open());
        onView(withText(R.string.settings)).perform(click());
        onView(withId(R.id.drawer)).perform(DrawerActions.open());
        onView(withText(R.string.about)).perform(click());
        onView(withId(R.id.drawer)).perform(DrawerActions.open());
        onView(withText(R.string.exit)).perform(click());
    }

    @Test
    public void test_hijri() {
        // Copied from TestDateCalendar
        int tests2[][][] = {
                {{2016, 10, 3}, {1438, 1, 1}},
                {{2016, 11, 1}, {1438, 2, 1}},
                {{2016, 12, 1}, {1438, 3, 1}}
        };

        for (int[][] test : tests2) {
            long jdn = new CivilDate(test[0][0], test[0][1], test[0][2]).toJdn();
            IslamicDate islamicDate = new IslamicDate(test[1][0], test[1][1], test[1][2]);

            assertEquals(jdn, islamicDate.toJdn());
            assertTrue(islamicDate.equals(new IslamicDate(jdn)));
        }

        IslamicDate.useUmmAlQura = true;
        int tests3[][][] = {
                {{2015, 3, 14}, {1436, 5, 23}},
                {{1999, 4, 1}, {1419, 12, 15}},
                {{1989, 2, 25}, {1409, 7, 19}}
        };
        for (int[][] test : tests3) {
            long jdn = new CivilDate(test[0][0], test[0][1], test[0][2]).toJdn();
            IslamicDate islamicDate = new IslamicDate(test[1][0], test[1][1], test[1][2]);

            assertEquals(jdn, islamicDate.toJdn());
            assertTrue(islamicDate.equals(new IslamicDate(jdn)));
        }
        IslamicDate.useUmmAlQura = false;
    }
}
