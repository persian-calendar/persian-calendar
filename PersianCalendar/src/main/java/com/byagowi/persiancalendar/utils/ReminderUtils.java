//package com.byagowi.persiancalendar.utils;
//
//import android.app.AlarmManager;
//import android.app.PendingIntent;
//import android.content.Context;
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.preference.PreferenceManager;
//
//import com.byagowi.persiancalendar.Constants;
//import com.byagowi.persiancalendar.R;
//import com.byagowi.persiancalendar.entities.Reminder;
//import com.byagowi.persiancalendar.service.ReminderAlert;
//
//import java.util.Arrays;
//import java.util.List;
//import java.util.Locale;
//import java.util.concurrent.TimeUnit;
//
//import androidx.annotation.StringRes;
//
///**
// * @author MEHDI DIMYADI
// * MEHDIMYADI
// */
//public class ReminderUtils {
//
//    static void turnOn(Context context, Reminder reminder) {
//        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//        if (alarmManager == null) return;
//
//        long startTime = reminder.startTime;
//        long period = reminder.unit.toMillis(1);
//
//        startTime = System.currentTimeMillis() + (System.currentTimeMillis() - startTime) % period;
//        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, startTime, period, prepareIntent(context, reminder.id));
//    }
//
//    public static void turnOff(Context context, int reminderId) {
//        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//        if (alarmManager == null) return;
//
//        alarmManager.cancel(prepareIntent(context, reminderId));
//    }
//
//    private static PendingIntent prepareIntent(Context context, int reminderId) {
//        Intent intent = new Intent(context, ReminderAlert.class);
//        intent.setAction(String.valueOf(reminderId));
//        intent.putExtra(Constants.REMINDER_ID, Constants.REMINDERS_BASE_ID + reminderId);
//        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
//    }
//
//    public static int unitToOrdination(TimeUnit unit) {
//        switch (unit) {
//            case HOURS:
//                return 0;
//            default:
//            case DAYS:
//                return 1;
//        }
//    }
//
//    public static @StringRes
//    int unitToStringId(TimeUnit unit) {
//        switch (unit) {
//            case HOURS:
//                return R.string.reminder_hour;
//            default:
//            case DAYS:
//                return R.string.reminder_day;
//        }
//    }
//
//    public static TimeUnit ordinationToUnit(int ordination) {
//        switch (ordination) {
//            case 0:
//                return TimeUnit.HOURS;
//            default:
//            case 1:
//                return TimeUnit.DAYS;
//        }
//    }
//
//    static TimeUnit timeUnitFromString(String string) {
//        switch (string) {
//            case "h":
//                return TimeUnit.HOURS;
//            default:
//            case "d":
//                return TimeUnit.DAYS;
//        }
//    }
//
//    static String timeUnitToString(TimeUnit unit) {
//        switch (unit) {
//            case HOURS:
//                return "h";
//            default:
//            case DAYS:
//                return "d";
//        }
//    }
//
//    public static List<String> timeUnitsStringArray(Context context) {
//        return Arrays.asList(context.getString(R.string.reminder_hour),
//                context.getString(R.string.reminder_day));
//    }
//
//    public static int getReminderCount(Context context, int reminderId) {
//        return PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext())
//                .getInt(String.format(Locale.US, Constants.REMINDERS_COUNT_KEY, reminderId), 0);
//    }
//
//    public static void increaseReminderCount(Context context, int reminderId) {
//        int count = getReminderCount(context, reminderId) + 1;
//
//        SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext()).edit();
//        edit.putInt(String.format(Locale.US, Constants.REMINDERS_COUNT_KEY, reminderId), count);
//        edit.apply();
//    }




//    public static List<Reminder> getReminderDetails() {
//        return sReminderDetails;
//    }

//    private static List<Reminder> updateSavedReminders(Context context) {
//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
//        String storedJson = prefs.getString(REMINDERS_STORE_KEY, "[]");
//        if (TextUtils.isEmpty(storedJson))
//            storedJson = "[]";
//
//        List<Reminder> reminders = new ArrayList<>();
//        try {
//            JSONArray jsonArray = new JSONArray(storedJson);
//            int length = jsonArray.length();
//            for (int i = 0; i < length; ++i) {
//                JSONObject jsonObject = jsonArray.getJSONObject(i);
//                reminders.add(new Reminder(
//                        jsonObject.getInt("id"),
//                        jsonObject.getString("name"),
//                        jsonObject.getString("info"),
//                        ReminderUtils.timeUnitFromString(jsonObject.getString("unit")),
//                        jsonObject.getInt("quantity"),
//                        jsonObject.getLong("startTime")
//                ));
//            }
//        } catch (JSONException e) {
//            e.printStackTrace();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return reminders;
//    }
//
//    public static void storeReminders(Context context, List<Reminder> reminders) {
//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
//
//        try {
//            JSONArray json = new JSONArray();
//            for (Reminder reminder : reminders) {
//                JSONObject object = new JSONObject();
//                object.put("id", reminder.id);
//                object.put("name", reminder.name);
//                object.put("info", reminder.info);
//                object.put("unit", ReminderUtils.timeUnitToString(reminder.unit));
//                object.put("quantity", reminder.quantity);
//                object.put("startTime", reminder.startTime);
//                json.put(object);
//            }
//
//            String serializedJson = json.toString();
//
//            // Just don't store huge objects
//            if (serializedJson.length() > 8000)
//                return;
//
//            SharedPreferences.Editor edit = prefs.edit();
//            edit.putString(REMINDERS_STORE_KEY, serializedJson);
//            edit.apply();
//
//        } catch (JSONException e) {
//            e.printStackTrace();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Nullable
//    public static Reminder getReminderById(int id) {
//        for (Reminder reminder : sReminderDetails) {
//            if (reminder.id == id) return reminder;
//        }
//        return null;
//    }

//}