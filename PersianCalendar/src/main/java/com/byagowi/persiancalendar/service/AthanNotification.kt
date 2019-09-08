package com.byagowi.persiancalendar.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.text.TextUtils
import android.view.View
import android.widget.RemoteViews

import androidx.core.app.NotificationCompat

import com.byagowi.persiancalendar.BuildConfig
import com.byagowi.persiancalendar.Constants
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.utils.Utils

import java.util.concurrent.TimeUnit

class AthanNotification : Service() {

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (notificationManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notificationChannel = NotificationChannel(NOTIFICATION_CHANNEL_ID, getString(R.string.app_name),
                        NotificationManager.IMPORTANCE_DEFAULT)

                notificationChannel.description = getString(R.string.app_name)
                notificationChannel.enableLights(true)
                notificationChannel.lightColor = Color.GREEN
                notificationChannel.vibrationPattern = longArrayOf(0, 1000, 500, 1000)
                notificationChannel.enableVibration(true)
                notificationManager.createNotificationChannel(notificationChannel)
            }

            val title = if (intent == null)
                ""
            else
                getString(Utils.getPrayTimeText(intent.getStringExtra(Constants.KEY_EXTRA_PRAYER_KEY)))
            val cityName = Utils.getCityName(this, false)
            val subtitle = if (TextUtils.isEmpty(cityName))
                ""
            else
                getString(R.string.in_city_time) + " " + cityName

            var notificationBuilder = NotificationCompat.Builder(this,
                    NOTIFICATION_CHANNEL_ID)
            notificationBuilder.setAutoCancel(true)
                    .setWhen(System.currentTimeMillis())
                    .setSmallIcon(R.drawable.sun)
                    .setContentTitle(title)
                    .setContentText(subtitle)

            notificationBuilder.setDefaults(NotificationCompat.DEFAULT_VIBRATE)
            notificationBuilder.setDefaults(NotificationCompat.DEFAULT_SOUND)

            val appContext = applicationContext
            if (appContext != null && (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N || BuildConfig.DEBUG)) {
                val cv = RemoteViews(appContext.packageName, if (Utils.isLocaleRTL())
                    R.layout.custom_notification
                else
                    R.layout.custom_notification_ltr)
                cv.setTextViewText(R.id.title, title)
                if (TextUtils.isEmpty(subtitle)) {
                    cv.setViewVisibility(R.id.body, View.GONE)
                } else {
                    cv.setTextViewText(R.id.body, subtitle)
                }

                notificationBuilder = notificationBuilder
                        .setCustomContentView(cv)
                        .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            }

            notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())

            Handler().postDelayed({
                notificationManager.cancel(NOTIFICATION_ID)
                stopSelf()
            }, TimeUnit.MINUTES.toMillis(5))
        }


        return super.onStartCommand(intent, flags, startId)
    }

    companion object {

        private val NOTIFICATION_ID = 1002
        private val NOTIFICATION_CHANNEL_ID = NOTIFICATION_ID.toString()
    }
}
