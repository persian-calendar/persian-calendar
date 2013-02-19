package com.byagowi.persiancalendar;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.*;
import android.graphics.*;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;
import calendar.CivilDate;
import calendar.DateConverter;
import calendar.PersianDate;

import java.util.Date;

import static com.byagowi.persiancalendar.CalendarUtils.*;

/**
 * @author Ebrahim Byagowi <ebrahim@byagowi.com>
 */
public class CalendarService extends Service {

    @Override
    public IBinder onBind(Intent paramIntent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_DATE_CHANGED);
        intentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        intentFilter.addAction(Intent.ACTION_TIME_TICK);
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                update(context);
            }
        }, intentFilter);

        update(getApplicationContext());

        return super.onStartCommand(intent, flags, startId);
    }

    private static final int NOTIFICATION_ID = 1001;
    private NotificationManager mNotificationManager;

    public void update(Context context) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        boolean gadgetClock = prefs.getBoolean("GadgetClock", true);
        boolean gadgetIn24 = prefs.getBoolean("GadgetIn24", false);
        boolean blackWidget = prefs.getBoolean("BlackWidget", false);
        char[] digits = preferredDigits(context);

        PendingIntent launchAppPendingIntent = PendingIntent.getActivity(
                context, 0, new Intent(context, CalendarActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);

        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        RemoteViews remoteViews1 = new RemoteViews(context.getPackageName(),
                R.layout.widget1x1);
        RemoteViews remoteViews4 = new RemoteViews(context.getPackageName(),
                R.layout.widget4x1);
        CivilDate civil = new CivilDate();
        PersianDate persian = DateConverter.civilToPersian(civil);
        persian.setDari(isDariVersion(context));
        int color;
        if (blackWidget) {
            color = context.getResources().getColor(android.R.color.black);
        } else {
            color = context.getResources().getColor(android.R.color.white);
        }

        // Widget1x1
        remoteViews1.setTextColor(R.id.textPlaceholder1_1x1, color);
        remoteViews1.setTextColor(R.id.textPlaceholder2_1x1, color);
        remoteViews1.setTextViewText(R.id.textPlaceholder2_1x1,
                textShaper(persian.getMonthName()));
        remoteViews1.setTextViewText(R.id.textPlaceholder1_1x1,
                formatNumber(persian.getDayOfMonth(), digits));
        remoteViews1.setOnClickPendingIntent(R.id.widget_layout1x1,
                launchAppPendingIntent);
        manager.updateAppWidget(new ComponentName(context,
                CalendarWidget1x1.class), remoteViews1);
        // Widget 4x1
        remoteViews4.setTextColor(R.id.textPlaceholder1_4x1, color);
        remoteViews4.setTextColor(R.id.textPlaceholder2_4x1, color);

        String text1;
        String text2;
        text1 = getDayOfWeekName(civil.getDayOfWeek());
        String dayTitle = dateToString(persian, digits, true);
        text2 = dayTitle + PERSIAN_COMMA + " " + dateToString(civil, digits, true);
        if (gadgetClock) {
            text2 = text1 + " " + text2;
            text1 = getPersianFormattedClock(new Date(), digits, gadgetIn24);
        }

        text1 = textShaper(text1);
        text2 = textShaper(text2);

        remoteViews4.setTextViewText(R.id.textPlaceholder1_4x1, text1);
        remoteViews4.setTextViewText(R.id.textPlaceholder2_4x1, text2);
        remoteViews4.setOnClickPendingIntent(R.id.widget_layout4x1,
                launchAppPendingIntent);
        manager.updateAppWidget(new ComponentName(context,
                CalendarWidget4x1.class), remoteViews4);
        //

        // notification
        if (mNotificationManager == null) {
            mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }

        if (prefs.getBoolean("NotifyDate", true)) {
            mNotificationManager.cancel(NOTIFICATION_ID);

            Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types
            int w = 64;//getResources().getDimensionPixelSize(android.R.dimen.notification_large_icon_width);
            int h = 64;//getResources().getDimensionPixelSize(android.R.dimen.notification_large_icon_height);
            Bitmap bmp = Bitmap.createBitmap(h, w, conf); // this creates a MUTABLE bitmap

            Canvas canvas = new Canvas(bmp);
            drawRectangleCenter(canvas, 0, 0, w, h, CalendarUtils.formatNumber(persian.getDayOfMonth(), digits));

            String contentText = dateToString(civil, digits, true) + PERSIAN_COMMA + " " +
                    dateToString(DateConverter.civilToIslamic(civil), digits, true);

            Notification notification = new Notification.Builder(this)
                    .setSmallIcon(R.drawable.launcher_icon)
                    .setLargeIcon(bmp)
                    .setContentText(text2)
                    .setContentTitle(dayTitle)
                    .setContentIntent(launchAppPendingIntent)
                    .setOngoing(true)
                    .build();
            mNotificationManager.notify(NOTIFICATION_ID, notification);
            //
        } else {
            mNotificationManager.cancel(NOTIFICATION_ID);
        }
    }

    // modified from http://stackoverflow.com/a/8553604/1414809
    void drawRectangleCenter(Canvas c, int topLeftX, int topLeftY, int width, int height, String textToDraw) {
        // height of 'Hello World'; height * 0.7 looks good
        int fontHeight = (int) (height * 0.9); // but I modified it

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextSize(fontHeight);
        paint.setColor(Color.WHITE);
        paint.setTextAlign(Paint.Align.CENTER);
        Rect bounds;
        bounds = new Rect();
        paint.getTextBounds(textToDraw, 0, textToDraw.length(), bounds);
        c.drawText(textToDraw, topLeftX + width / 2, topLeftY + height / 2 + (bounds.bottom - bounds.top) / 2, paint);
    }
}
