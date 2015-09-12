package com.byagowi.persiancalendar;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.azizhuss.arabicreshaper.ArabicShaping;
import com.byagowi.common.IterableNodeList;
import com.byagowi.common.Range;
import com.github.praytimes.CalculationMethod;
import com.github.praytimes.Clock;
import com.github.praytimes.Coordinate;
import com.github.praytimes.Locations;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import calendar.AbstractDate;
import calendar.CivilDate;
import calendar.DateConverter;
import calendar.IslamicDate;
import calendar.LocaleData;
import calendar.PersianDate;

/**
 * Common utilities that needed for this calendar
 *
 * @author ebraminio
 */
public class Utils {
    private static final String TAG = "Utils";
    private static Utils myInstance;
    public static final char PERSIAN_COMMA = '،';
    public static final char[] arabicIndicDigits = {'٠', '١', '٢', '٣', '٤', '٥',
            '٦', '٧', '٨', '٩'};
    public static final String[] firstCharOfDaysOfWeekName = {"ش", "ی", "د", "س",
            "چ", "پ", "ج"};
    private static final char[] arabicDigits = {'0', '1', '2', '3', '4', '5', '6',
            '7', '8', '9'};
    private static final char[] persianDigits = {'۰', '۱', '۲', '۳', '۴', '۵', '۶',
            '۷', '۸', '۹'};
    private String AM_IN_PERSIAN = "ق.ظ";
    private String PM_IN_PERSIAN = "ب.ظ";

    private Typeface typeface;
    private int[] daysIcons = {0, R.drawable.day1, R.drawable.day2,
            R.drawable.day3, R.drawable.day4, R.drawable.day5, R.drawable.day6,
            R.drawable.day7, R.drawable.day8, R.drawable.day9,
            R.drawable.day10, R.drawable.day11, R.drawable.day12,
            R.drawable.day13, R.drawable.day14, R.drawable.day15,
            R.drawable.day16, R.drawable.day17, R.drawable.day18,
            R.drawable.day19, R.drawable.day20, R.drawable.day21,
            R.drawable.day22, R.drawable.day23, R.drawable.day24,
            R.drawable.day25, R.drawable.day26, R.drawable.day27,
            R.drawable.day28, R.drawable.day29, R.drawable.day30,
            R.drawable.day31};
    private List<Holiday> holidays;

    private Utils() {
    }

    public static Utils getInstance() {
        if (myInstance == null) {
            myInstance = new Utils();
        }
        return myInstance;
    }

    public static String textShaper(String text) {
        return (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) ? ArabicShaping.shape(text) : text;
    }

    public String programVersion(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            Log.e(context.getPackageName(),
                    "Name not found on PersianCalendarUtils.programVersion");
        }
        return "";
    }

    public void prepareTextView(TextView textView) {
        if (typeface == null) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(textView.getContext());
            String calendarFont = prefs.getString("CalendarFont", "NotoNaskhArabic-Regular.ttf");
            typeface = Typeface.createFromAsset(textView.getContext()
                    .getAssets(), "fonts/" + calendarFont);
        }
        textView.setTypeface(typeface);
        textView.setLineSpacing(0f, 0.8f);
    }

    public CalculationMethod getCalculationMethod(Context context) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);

        return CalculationMethod.valueOf(prefs.getString("PrayTimeMethod",
                "Jafari")); // Seems Iran using Jafari method
    }

    public Coordinate getCoordinate(Context context) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);

        String location = prefs.getString("Location", "CUSTOM");
        if (!location.equals("CUSTOM")) {
            return Locations.valueOf(location).getCoordinate();
        }

        try {
            Coordinate coord = new Coordinate(Double.parseDouble(prefs
                    .getString("Latitude", "0")), Double.parseDouble(prefs
                    .getString("Longitude", "0")), Double.parseDouble(prefs
                    .getString("Altitude", "0")));

            // If latitude or longitude is zero probably preference not set yet
            if (coord.getLatitude() == 0 && coord.getLongitude() == 0) {
                return null;
            }

            return coord;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public char[] preferredDigits(Context context) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        return prefs.getBoolean("PersianDigits", true) ? persianDigits
                : arabicDigits;
    }

    public void setTheme(Context context) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        String key = prefs.getString("Theme", "");

        int theme = R.style.LightTheme; // default theme

        if (key.equals("LightTheme")) {
            theme = R.style.LightTheme;
        } else if (key.equals("DarkTheme")) {
            theme = R.style.DarkTheme;
        }

        context.setTheme(theme);
    }

    public boolean clockIn24(Context context) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        return prefs.getBoolean("WidgetIn24", true);
    }

    public static PersianDate getToday() {
        CivilDate civilDate = new CivilDate();
        return DateConverter.civilToPersian(civilDate);
    }

    public Calendar makeCalendarFromDate(Date date, boolean iranTime) {
        Calendar calendar = Calendar.getInstance();
        if (iranTime) {
            calendar.setTimeZone(TimeZone.getTimeZone("Asia/Tehran"));
        }
        calendar.setTime(date);
        return calendar;
    }

    public String clockToString(Clock clock, char[] digits) {
        return clockToString(clock.getHour(), clock.getMinute(), digits);
    }

    public String clockToString(int hour, int minute, char[] digits) {
        return formatNumber(
                String.format(Locale.ENGLISH, "%d:%02d", hour, minute), digits);
    }

    public String getPersianFormattedClock(Clock clock, char[] digits, boolean in24) {
        String timeText = null;

        int hour = clock.getHour();
        if (!in24) {
            if (hour >= 12) {
                timeText = PM_IN_PERSIAN;
                hour -= 12;
            } else {
                timeText = AM_IN_PERSIAN;
            }
        }

        String result = clockToString(hour, clock.getMinute(), digits);
        if (!in24) {
            result = result + " " + timeText;
        }
        return result;
    }

    public String getPersianFormattedClock(Calendar calendar, char[] digits,
                                           boolean in24) {
        String timeText = null;

        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        if (!in24) {
            if (calendar.get(Calendar.HOUR_OF_DAY) >= 12) {
                timeText = PM_IN_PERSIAN;
                hour -= 12;
            } else {
                timeText = AM_IN_PERSIAN;
            }
        }

        String result = clockToString(hour, calendar.get(Calendar.MINUTE),
                digits);
        if (!in24) {
            result = result + " " + timeText;
        }
        return result;
    }

    public static String formatNumber(int number, char[] digits) {
        return formatNumber(Integer.toString(number), digits);
    }

    public static String formatNumber(String number, char[] digits) {
        if (digits == arabicDigits)
            return number;

        StringBuilder sb = new StringBuilder();
        for (char i : number.toCharArray()) {
            if (Character.isDigit(i)) {
                sb.append(digits[Integer.parseInt(i + "")]);
            } else {
                sb.append(i);
            }
        }
        return sb.toString();
    }

    public String dateToString(AbstractDate date, char[] digits, Context context) {
        return formatNumber(date.getDayOfMonth(), digits) + ' '
                + getMonthName(date, context) + ' '
                + formatNumber(date.getYear(), digits);
    }

    public String dayTitleSummary(PersianDate persianDate, char[] digits, Context context) {
        return getWeekDayName(persianDate, context) + PERSIAN_COMMA + " "
                + dateToString(persianDate, digits, context);
    }

    public String getMonthYearTitle(PersianDate persianDate, char[] digits, Context context) {
        return textShaper(getMonthName(persianDate, context) + ' '
                + formatNumber(persianDate.getYear(), digits));
    }

    public String getMonthName(AbstractDate date, Context context) {
        String monthName = "";

        if (date.getClass().equals(PersianDate.class)) {
            LocaleData.PersianMonthNames monthNameCode = LocaleData.PersianMonthNames.values()[date.getDayOfMonth()];
            switch (monthNameCode) {
                case FARVARDIN:
                    monthName = context.getString(R.string.FARVARDIN);
                    break;
                case ORDIBEHESHT:
                    monthName = context.getString(R.string.ORDIBEHESHT);
                    break;
                case KHORDARD:
                    monthName = context.getString(R.string.KHORDARD);
                    break;
                case TIR:
                    monthName = context.getString(R.string.TIR);
                    break;
                case MORDAD:
                    monthName = context.getString(R.string.MORDAD);
                    break;
                case SHAHRIVAR:
                    monthName = context.getString(R.string.SHAHRIVAR);
                    break;
                case MEHR:
                    monthName = context.getString(R.string.MEHR);
                    break;
                case ABAN:
                    monthName = context.getString(R.string.ABAN);
                    break;
                case AZAR:
                    monthName = context.getString(R.string.AZAR);
                    break;
                case DEY:
                    monthName = context.getString(R.string.DEY);
                    break;
                case BAHMAN:
                    monthName = context.getString(R.string.BAHMAN);
                    break;
                case ESFAND:
                    monthName = context.getString(R.string.ESFAND);
                    break;
            }
        } else if (date.getClass().equals(CivilDate.class)) {
            LocaleData.CivilMonthNames monthNameCode = LocaleData.CivilMonthNames.values()[date.getDayOfMonth()];
            switch (monthNameCode) {
                case JANUARY:
                    monthName = context.getString(R.string.JANUARY);
                    break;
                case FEBRUARY:
                    monthName = context.getString(R.string.FEBRUARY);
                    break;
                case MARCH:
                    monthName = context.getString(R.string.MARCH);
                    break;
                case APRIL:
                    monthName = context.getString(R.string.APRIL);
                    break;
                case MAY:
                    monthName = context.getString(R.string.MAY);
                    break;
                case JUNE:
                    monthName = context.getString(R.string.JUNE);
                    break;
                case JULY:
                    monthName = context.getString(R.string.JULY);
                    break;
                case AUGUST:
                    monthName = context.getString(R.string.AUGUST);
                    break;
                case SEPTEMBER:
                    monthName = context.getString(R.string.SEPTEMBER);
                    break;
                case OCTOBER:
                    monthName = context.getString(R.string.OCTOBER);
                    break;
                case NOVEMBER:
                    monthName = context.getString(R.string.NOVEMBER);
                    break;
                case DECEMBER:
                    monthName = context.getString(R.string.DECEMBER);
                    break;

            }
        } else if (date.getClass().equals(IslamicDate.class)) {
            LocaleData.IslamicMonthNames monthNameCode = LocaleData.IslamicMonthNames.values()[date.getDayOfMonth()];
            switch (monthNameCode) {
                case MUHARRAM:
                    monthName = context.getString(R.string.MUHARRAM);
                    break;
                case SAFAR:
                    monthName = context.getString(R.string.SAFAR);
                    break;
                case RABI_OL_AWAL:
                    monthName = context.getString(R.string.RABI_OL_AWAL);
                    break;
                case RABI_O_THANI:
                    monthName = context.getString(R.string.RABI_O_THANI);
                    break;
                case JAMADI_OL_AWLA:
                    monthName = context.getString(R.string.JAMADI_OL_AWLA);
                    break;
                case JAMADI_O_THANI:
                    monthName = context.getString(R.string.JAMADI_O_THANI);
                    break;
                case RAJAB:
                    monthName = context.getString(R.string.RAJAB);
                    break;
                case SHABAN:
                    monthName = context.getString(R.string.SHABAN);
                    break;
                case RAMADAN:
                    monthName = context.getString(R.string.RAMADAN);
                    break;
                case SHAWAL:
                    monthName = context.getString(R.string.SHAWAL);
                    break;
                case ZOLQADA:
                    monthName = context.getString(R.string.ZOLQADA);
                    break;
                case ZOLHAJJA:
                    monthName = context.getString(R.string.ZOLHAJJA);
                    break;
            }
        }

        return monthName;
    }

    public List<String> getMonthNameList(AbstractDate date, Context context) {
        List<String> monthNameList = new ArrayList<>();
        for (int month : new Range(1, 12)) {
            date.setMonth(month);
            monthNameList.add(textShaper(getMonthName(date, context)));
        }
        return monthNameList;
    }

    public String getWeekDayName(AbstractDate date, Context context) {
        String weekDayName = "";
        CivilDate civilDate;
        if (date.getClass().equals(PersianDate.class)) {
            civilDate = DateConverter.persianToCivil((PersianDate) date);
        } else if (date.getClass().equals(IslamicDate.class)) {
            civilDate = DateConverter.islamicToCivil((IslamicDate) date);
        } else {
            civilDate = (CivilDate) date;
        }

        LocaleData.WeekDayNames weekDayNameCode = LocaleData.WeekDayNames.values()[civilDate.getDayOfWeek()];
        switch (weekDayNameCode) {
            case SUNDAY:
                weekDayName = context.getString(R.string.SUNDAY);
                break;
            case MONDAY:
                weekDayName = context.getString(R.string.MONDAY);
                break;
            case TUESDAY:
                weekDayName = context.getString(R.string.TUESDAY);
                break;
            case WEDNESDAY:
                weekDayName = context.getString(R.string.WEDNESDAY);
                break;
            case THURSDAY:
                weekDayName = context.getString(R.string.THURSDAY);
                break;
            case FRIDAY:
                weekDayName = context.getString(R.string.FRIDAY);
                break;
            case SATURDAY:
                weekDayName = context.getString(R.string.SATURDAY);
                break;
        }

        return weekDayName;
    }

    public void quickToast(String message, Context context) {
        Toast.makeText(context, textShaper(message), Toast.LENGTH_SHORT).show();
    }

    public int getDayIconResource(int day) {
        try {
            return daysIcons[day];
        } catch (IndexOutOfBoundsException e) {
            Log.e("com.byagowi.calendar", "No such field is available");
            return 0;
        }
    }

    public void loadHolidays(InputStream xmlStream) {
        holidays = new ArrayList<Holiday>();
        DocumentBuilder builder;
        try {
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

            Document document = builder.parse(xmlStream);

            NodeList holidaysNodes = document.getElementsByTagName("holiday");
            for (Node node : new IterableNodeList(holidaysNodes)) {
                NamedNodeMap attrs = node.getAttributes();

                int year = Integer.parseInt(attrs.getNamedItem("year")
                        .getNodeValue());
                int month = Integer.parseInt(attrs.getNamedItem("month")
                        .getNodeValue());
                int day = Integer.parseInt(attrs.getNamedItem("day")
                        .getNodeValue());

                String holidayTitle = node.getFirstChild().getNodeValue();

                holidays.add(new Holiday(new PersianDate(year, month, day),
                        holidayTitle));
            }

        } catch (ParserConfigurationException | SAXException | IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public String getHolidayTitle(PersianDate day) {
        for (Holiday holiday : holidays) {
            if (holiday.getDate().equals(day)) {
                // trim XML whitespaces and newlines
                return holiday.getTitle().replaceAll("\n", "").trim();
            }
        }
        return null;
    }

    public void changeLanguage(String localeCode, Context context) {
        Resources resources = context.getApplicationContext().getResources();
        Locale locale = new Locale(localeCode);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }

    public void loadLanguageFromSettings(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String localeCode = prefs.getString("ApplicationLanguage", "en");
        changeLanguage(localeCode, context);

        String calendarFont = prefs.getString("CalendarFont", "NotoNaskhArabic-Regular.ttf");
        typeface = Typeface.createFromAsset(context.getAssets(), "fonts/" + calendarFont);
    }
}
