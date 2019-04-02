//package com.byagowi.persiancalendar.service;
//
//
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.os.Build;
//import android.os.Bundle;
//import android.util.Log;
//
//import com.byagowi.persiancalendar.Constants;
//
//import androidx.core.content.ContextCompat;
//
///**
// * @author MEHDI DIMYADI
// * MEHDIMYADI
// */
//public class ReminderAlert extends BroadcastReceiver {
//
//    @Override
//    public void onReceive(Context context, Intent intent) {
//        if (intent == null || context == null) return;
//        Log.i("REMINDER", String.valueOf(intent.getIntExtra(Constants.REMINDER_ID, -1)));
//
////        Intent reminderIntent = new Intent(context, ReminderActivity.class);
////        Bundle extras = intent.getExtras();
////        if (extras != null)
////            reminderIntent.putExtras(extras);
////        reminderIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
////        context.startActivity(reminderIntent);
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
//            ContextCompat.startForegroundService(context,
//                    new Intent(context, ReminderNotification.class));
//
//        Intent i = new Intent(context, ReminderNotification.class);
//        Bundle extras = intent.getExtras();
//        if (extras != null)
//            i.putExtras(extras);
//        context.startService(i);
//    }
//}
