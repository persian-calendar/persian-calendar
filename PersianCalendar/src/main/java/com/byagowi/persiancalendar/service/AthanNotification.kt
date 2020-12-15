package com.byagowi.persiancalendar.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import com.byagowi.persiancalendar.KEY_EXTRA_PRAYER_KEY
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.utils.*
import java.util.concurrent.TimeUnit

private const val NOTIFICATION_ID = 1002
private const val NOTIFICATION_CHANNEL_ID = NOTIFICATION_ID.toString()

class AthanNotification : Service() {

    override fun onBind(intent: Intent): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent ?: return super.onStartCommand(intent, flags, startId)

        val notificationManager = getSystemService<NotificationManager>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                    NOTIFICATION_CHANNEL_ID, getString(R.string.app_name),
                    NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = getString(R.string.app_name)
                enableLights(true)
                lightColor = Color.GREEN
                vibrationPattern = longArrayOf(0, 1000, 500, 1000)
                enableVibration(true)
            }
            notificationManager?.createNotificationChannel(notificationChannel)
        }

        val athanKey = intent.getStringExtra(KEY_EXTRA_PRAYER_KEY)
        val cityName = getCityName(this, false)
        val title =
                if (cityName.isNotEmpty()) getString(getPrayTimeText(athanKey))
                else "${getString(getPrayTimeText(athanKey))} - ${getString(R.string.in_city_time)} $cityName"

        val subtitle = when (athanKey) {
            "FAJR" -> listOf(R.string.sunrise)
            "DHUHR" -> listOf(R.string.asr, R.string.sunset)
            "ASR" -> listOf(R.string.sunset)
            "MAGHRIB" -> listOf(R.string.isha, R.string.midnight)
            "ISHA" -> listOf(R.string.midnight)
            else -> listOf(R.string.midnight)
        }.joinToString(" - ") {
            "${getString(it)}: ${getClockFromStringId(it).toFormattedString()}"
        }

        val notificationBuilder = NotificationCompat.Builder(
                this,
                NOTIFICATION_CHANNEL_ID
        )
        notificationBuilder.setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.sun)
                .setContentTitle(title)
                .setContentText(subtitle)

        notificationBuilder.setDefaults(NotificationCompat.DEFAULT_VIBRATE)
        notificationBuilder.setDefaults(NotificationCompat.DEFAULT_SOUND)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val cv = RemoteViews(
                    applicationContext?.packageName, if (isLocaleRTL())
                R.layout.custom_notification
            else
                R.layout.custom_notification_ltr
            )
            cv.setTextViewText(R.id.title, title)
            if (subtitle.isEmpty()) {
                cv.setViewVisibility(R.id.body, View.GONE)
            } else {
                cv.setTextViewText(R.id.body, subtitle)
            }

            notificationBuilder
                    .setCustomContentView(cv)
                    .setStyle(NotificationCompat.DecoratedCustomViewStyle())
        }

        notificationManager?.notify(NOTIFICATION_ID, notificationBuilder.build())

        Handler(Looper.getMainLooper()).postDelayed({
            notificationManager?.cancel(NOTIFICATION_ID)
            stopSelf()
        }, TimeUnit.MINUTES.toMillis(5))

        return super.onStartCommand(intent, flags, startId)
    }
}
