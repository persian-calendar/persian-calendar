package com.byagowi.persiancalendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.azizhuss.arabicreshaper.ArabicShaping;
import com.byagowi.common.IterableNodeList;
import com.byagowi.common.Range;
import com.byagowi.persiancalendar.Entity.Day;
import com.byagowi.persiancalendar.locale.LocaleUtils;
import com.github.praytimes.CalculationMethod;
import com.github.praytimes.Clock;
import com.github.praytimes.Coordinate;
import com.github.praytimes.Locations;
import com.github.praytimes.PrayTime;
import com.github.praytimes.PrayTimesCalculator;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import calendar.AbstractDate;
import calendar.CivilDate;
import calendar.DateConverter;
import calendar.DayOutOfRangeException;
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
    private LocaleUtils localeUtils;
    public static Uri athanFileUri;
    private static boolean athanRepeaterSet = false;

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
    private List<Event> holidays;
    private List<Event> events;

    private Utils() {
    }

    public static Utils getInstance() {
        if (myInstance == null) {
            myInstance = new Utils();
        }
        return myInstance;
    }

    public static String textShaper(String text) {
        return (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN) ? ArabicShaping.shape(text) : text;
    }

    public String getString(String key) {
        return localeUtils == null ? "" : textShaper(localeUtils.getString(key));
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
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean("PersianDigits", true) ? persianDigits : arabicDigits;
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

    public List<Day> getDays(Context context, int offset) {
        List<Day> days = new ArrayList<>();
        PersianDate persianDate = getToday();
        int month = persianDate.getMonth() - offset;
        month -= 1;
        int year = persianDate.getYear();

        year = year + (month / 12);
        month = month % 12;
        if (month < 0) {
            year -= 1;
            month += 12;
        }
        month += 1;
        persianDate.setMonth(month);
        persianDate.setYear(year);
        persianDate.setDayOfMonth(1);

        char[] digits = preferredDigits(context);

        int dayOfWeek = DateConverter.persianToCivil(persianDate)
                .getDayOfWeek() % 7;

        try {
            PersianDate today = getToday();
            for (int i = 1; i <= 31; i++) {
                persianDate.setDayOfMonth(i);

                Day day = new Day();
                day.setNum(Utils.formatNumber(i, digits));
                day.setDayOfWeek(dayOfWeek);

                String holidayTitle = getHolidayTitle(persianDate);
                if (holidayTitle != null || dayOfWeek == 6) {
                    day.setHoliday(true);
                }

                String eventTitle = getEventTitle(persianDate);
                if (eventTitle != null || holidayTitle != null ) {
                    day.setEvent(true);
                }

                day.setPersianDate(persianDate.clone());

                if (persianDate.equals(today)) {
                    day.setToday(true);
                }

                days.add(day);
                dayOfWeek++;
                if (dayOfWeek == 7) {
                    dayOfWeek = 0;
                }
            }
        } catch (DayOutOfRangeException e) {
            // okay, it was expected
        }

        return days;
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

    public String dateToString(AbstractDate date, char[] digits) {
        return formatNumber(date.getDayOfMonth(), digits) + ' '
                + getMonthName(date) + ' '
                + formatNumber(date.getYear(), digits);
    }

    public String dayTitleSummary(PersianDate persianDate, char[] digits) {
        return getWeekDayName(persianDate) + PERSIAN_COMMA + " "
                + dateToString(persianDate, digits);
    }

    public String getMonthYearTitle(PersianDate persianDate, char[] digits) {
        return textShaper(getMonthName(persianDate) + ' '
                + formatNumber(persianDate.getYear(), digits));
    }

    public String getMonthName(AbstractDate date) {
        String monthName = "";
        // zero based
        int month = date.getMonth() - 1;

        if (date.getClass().equals(PersianDate.class)) {
            LocaleData.PersianMonthNames monthNameCode = LocaleData.PersianMonthNames.values()[month];
            monthName = getString(String.valueOf(monthNameCode));
        } else if (date.getClass().equals(CivilDate.class)) {
            LocaleData.CivilMonthNames monthNameCode = LocaleData.CivilMonthNames.values()[month];
            monthName = getString(String.valueOf(monthNameCode));
        } else if (date.getClass().equals(IslamicDate.class)) {
            LocaleData.IslamicMonthNames monthNameCode = LocaleData.IslamicMonthNames.values()[month];
            monthName = getString(String.valueOf(monthNameCode));
        }

        return monthName;
    }

    public List<String> getMonthNameList(AbstractDate date) {
        AbstractDate dateClone = date.clone();
        List<String> monthNameList = new ArrayList<>();
        for (int month : new Range(1, 12)) {
            dateClone.setMonth(month);
            monthNameList.add(textShaper(getMonthName(dateClone)));
        }
        return monthNameList;
    }

    public String getWeekDayName(AbstractDate date) {
        CivilDate civilDate;
        if (date.getClass().equals(PersianDate.class)) {
            civilDate = DateConverter.persianToCivil((PersianDate) date);
        } else if (date.getClass().equals(IslamicDate.class)) {
            civilDate = DateConverter.islamicToCivil((IslamicDate) date);
        } else {
            civilDate = (CivilDate) date;
        }

        // zero based
        int dayOfWeek = civilDate.getDayOfWeek() - 1;
        LocaleData.WeekDayNames weekDayNameCode = LocaleData.WeekDayNames.values()[dayOfWeek];

        return getString(weekDayNameCode.toString());
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
        holidays = new ArrayList<>();
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

                holidays.add(new Event(new PersianDate(year, month, day),
                        holidayTitle));
            }

        } catch (ParserConfigurationException | SAXException | IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public void loadEvents(InputStream xmlStream) {
        events = new ArrayList<>();
        DocumentBuilder builder;
        try {
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

            Document document = builder.parse(xmlStream);

            NodeList eventsNodes = document.getElementsByTagName("event");
            for (Node node : new IterableNodeList(eventsNodes)) {
                NamedNodeMap attrs = node.getAttributes();

                int year = Integer.parseInt(attrs.getNamedItem("year")
                        .getNodeValue());
                int month = Integer.parseInt(attrs.getNamedItem("month")
                        .getNodeValue());
                int day = Integer.parseInt(attrs.getNamedItem("day")
                        .getNodeValue());

                String eventTitle = node.getFirstChild().getNodeValue();

                events.add(new Event(new PersianDate(year, month, day),
                        eventTitle));
            }

        } catch (ParserConfigurationException | SAXException | IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public String getHolidayTitle(PersianDate day) {
        for (Event holiday : holidays) {
            if (holiday.getDate().equals(day)) {
                // trim XML whitespaces and newlines
                return holiday.getTitle().replaceAll("\n", "").trim();
            }
        }
        return null;
    }

    public String getEventTitle(PersianDate day) {
        for (Event event : events) {
            if (event.getDate().equals(day)) {
                // trim XML whitespaces and newlines
                return event.getTitle().replaceAll("\n", "").trim();
            }
        }
        return null;
    }

    public void setAthanRepeater(Context context) {
        Log.d(TAG, "athan repeater set: " + athanRepeaterSet);
        // load them so the prefs are read for today's alarms
        loadAlarms(context);
        loadAthanFiles(context);

        if (!athanRepeaterSet) {

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Calendar repeatTime = Calendar.getInstance();
            repeatTime.set(Calendar.HOUR_OF_DAY, 0);
            repeatTime.set(Calendar.MINUTE, 0);
            Intent intent = new Intent(context, AthanResetReceiver.class);
            PendingIntent repeatIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            alarmManager.setInexactRepeating(AlarmManager.RTC, repeatTime.getTimeInMillis(), (24 * 60 * 60 * 1000), repeatIntent);

            athanRepeaterSet = true;
        }
    }

    public void loadAlarms(Context context) {
        Log.d(TAG, "reading and loading all alarms from prefs");

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String prefString = prefs.getString("AthanAlarm", "");
        CalculationMethod calculationMethod = getCalculationMethod(context);
        Coordinate coordinate = getCoordinate(context);
        if (calculationMethod != null && coordinate != null && !TextUtils.isEmpty(prefString)) {
            PrayTimesCalculator calculator = new PrayTimesCalculator(calculationMethod);
            Map<PrayTime, Clock> prayTimes = calculator.calculate(new Date(), coordinate);

            String[] alarmTimesNames = TextUtils.split(prefString, ",");
            for (String prayerName : alarmTimesNames) {
                Clock alarmTime = prayTimes.get(PrayTime.valueOf(prayerName));

                if (alarmTime != null) {
                    setAlarm(context, PrayTime.valueOf(prayerName), alarmTime);
                }
            }
        }
    }

    public void setAlarm(Context context, PrayTime prayTime, Clock clock) {
        Calendar triggerTime = Calendar.getInstance();
        triggerTime.set(Calendar.HOUR_OF_DAY, clock.getHour());
        triggerTime.set(Calendar.MINUTE, clock.getMinute());
        setAlarm(context, prayTime, triggerTime.getTimeInMillis());
    }

    public void setAlarm(Context context, PrayTime prayTime, long timeInMillis) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String valAthanGap = prefs.getString("AthanGap", "0");
        long athanGap = TextUtils.isEmpty(valAthanGap) ? 0 : Long.parseLong(valAthanGap);

        Calendar triggerTime = Calendar.getInstance();
        triggerTime.setTimeInMillis(timeInMillis - TimeUnit.SECONDS.toMillis(athanGap));
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // don't set an alarm in the past
        if (!triggerTime.before(Calendar.getInstance())) {
            Log.d(TAG, "setting alarm for: " + triggerTime.getTime());

            Intent intent = new Intent(context, AlarmReceiver.class);
            intent.putExtra(AlarmReceiver.KEY_EXTRA_PRAYER_KEY, prayTime.name());
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime.getTimeInMillis(), pendingIntent);
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime.getTimeInMillis(), pendingIntent);
            }
        }
    }

    public Uri getAthanUri(Context context) {
        String defaultSoundUri = "android.resource://" + context.getPackageName() + "/" + R.raw.abdulbasit;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String athanSoundUri = prefs.getString("AthanSound", defaultSoundUri);
        if (TextUtils.isEmpty(athanSoundUri))
            athanSoundUri = defaultSoundUri;
        return Uri.parse(athanSoundUri);
    }

    public void changeAppLanguage(String localeCode, Context context) {
        Locale locale = TextUtils.isEmpty(localeCode) ? Locale.getDefault() : new Locale(localeCode);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        Resources resources = context.getApplicationContext().getResources();
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }

    public void changeCalendarLanguage(String localeCode, Context context) {
        if (localeUtils == null) {
            localeUtils = LocaleUtils.getInstance(context, localeCode);
        }

        localeUtils.changeLocale(localeCode);
    }

    public void loadLanguageFromSettings(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        // set app language
        String appLocale = prefs.getString("ApplicationLanguage", "");
        changeAppLanguage(appLocale, context);

        // set calendar language
        String calendarLocale = prefs.getString("CalendarLanguage", "fa");
        changeCalendarLanguage(calendarLocale, context);

        String calendarFont = prefs.getString("CalendarFont", "NotoNaskhArabic-Regular.ttf");
        typeface = Typeface.createFromAsset(context.getAssets(), "fonts/" + calendarFont);
    }

    public void loadAthanFiles(final Context context) {
        final String fileName = "AbdulBasit.ogg";
        final String fileTitle = "Athan Abdul Basit";
        File sdcardPath = Environment.getExternalStorageDirectory();
        File alarmsDir = new File(sdcardPath.getPath() + "/Alarms");
        final String outputPath = alarmsDir + "/" + fileName;
        if (!alarmsDir.exists()) {
            alarmsDir.mkdirs();
        }

        if (athanFileUri == null || (!(new File(outputPath).exists()))) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (!(new File(outputPath).exists())) {
                            FileOutputStream fos = new FileOutputStream(outputPath);
                            InputStream is = context.getResources().openRawResource(R.raw.abdulbasit);
                            int len;
                            byte[] buffer = new byte[1024];
                            while ((len = is.read(buffer)) != -1) {
                                fos.write(buffer, 0, len);
                            }
                            fos.close();
                            is.close();

                            ContentValues values = new ContentValues(4);
                            values.put(MediaStore.Audio.Media.TITLE, fileTitle);
                            values.put(MediaStore.Audio.Media.DATE_ADDED, System.currentTimeMillis() / 1000);
                            values.put(MediaStore.Audio.Media.MIME_TYPE, "audio/ogg");
                            values.put(MediaStore.Audio.Media.DATA, outputPath);

                            Uri base = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                            athanFileUri = context.getContentResolver().insert(base, values);
                            Log.d(TAG, "new uri: " + athanFileUri);

                            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, athanFileUri));
                        } else {
                            String[] projection = new String[]{MediaStore.Audio.AudioColumns._ID, MediaStore.Audio.AudioColumns.DATA};
                            String selection = MediaStore.Audio.AudioColumns.DATA + " LIKE ? ";
                            String[] selectionArgs = new String[]{outputPath};
                            Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection, selectionArgs, null);
                            cursor.moveToFirst();
                            Log.d(TAG, "count: " + cursor.getCount());
                            if (cursor.getCount() > 0) {
                                athanFileUri = Uri.parse(MediaStore.Audio.Media.getContentUri("external") + "/" +
                                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns._ID)));
                            }
                            cursor.close();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "", e);
                    }
                }
            }).start();
        }
    }
}
