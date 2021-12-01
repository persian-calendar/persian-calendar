package com.byagowi.persiancalendar.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import com.byagowi.persiancalendar.ASR_KEY
import com.byagowi.persiancalendar.DHUHR_KEY
import com.byagowi.persiancalendar.FAJR_KEY
import com.byagowi.persiancalendar.ISHA_KEY
import com.byagowi.persiancalendar.KEY_EXTRA_PRAYER
import com.byagowi.persiancalendar.MAGHRIB_KEY
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.global.coordinates
import com.byagowi.persiancalendar.global.spacedComma
import com.byagowi.persiancalendar.utils.FIFTEEN_MINUTES_IN_MILLIS
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.calculatePrayTimes
import com.byagowi.persiancalendar.utils.cityName
import com.byagowi.persiancalendar.utils.getAthanUri
import com.byagowi.persiancalendar.utils.getFromStringId
import com.byagowi.persiancalendar.utils.getPrayTimeName
import com.byagowi.persiancalendar.utils.logException
import com.byagowi.persiancalendar.utils.setDirection
import kotlin.random.Random

class AthanNotification : Service() {

    override fun onBind(intent: Intent): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent ?: return super.onStartCommand(intent, flags, startId)

        val notificationId = Random.nextInt(2000, 4000)
        val notificationChannelId = notificationId.toString()

        val notificationManager = getSystemService<NotificationManager>()

        val soundUri = getAthanUri(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                notificationChannelId, getString(R.string.app_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).also {
                it.description = getString(R.string.app_name)
                it.enableLights(true)
                it.lightColor = Color.GREEN
                it.vibrationPattern = longArrayOf(0, 1000, 500, 1000)
                it.enableVibration(true)
                it.setBypassDnd(true)
                it.setSound(
                    soundUri, AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .build()
                )
            }
            notificationManager?.createNotificationChannel(notificationChannel)
        }

        val athanKey = intent.getStringExtra(KEY_EXTRA_PRAYER)
        val cityName = this.appPrefs.cityName
        val prayTimeName = getString(getPrayTimeName(athanKey))
        val title =
            if (cityName == null) prayTimeName
            else "$prayTimeName$spacedComma${getString(R.string.in_city_time, cityName)}"

        val prayTimes = coordinates?.calculatePrayTimes()
        val subtitle = when (athanKey) {
            FAJR_KEY -> listOf(R.string.sunrise)
            DHUHR_KEY -> listOf(R.string.asr, R.string.sunset)
            ASR_KEY -> listOf(R.string.sunset)
            MAGHRIB_KEY -> listOf(R.string.isha, R.string.midnight)
            ISHA_KEY -> listOf(R.string.midnight)
            else -> listOf(R.string.midnight)
        }.joinToString(" - ") {
            "${getString(it)}: ${prayTimes?.getFromStringId(it)?.toFormattedString() ?: ""}"
        }

        runCatching {
            // ensure custom reminder sounds play well
            grantUriPermission(
                "com.android.systemui", soundUri, Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }.onFailure(logException)

        val notificationBuilder = NotificationCompat.Builder(this, notificationChannelId)
        notificationBuilder.setAutoCancel(true)
            .setWhen(System.currentTimeMillis())
            .setSmallIcon(R.drawable.sun)
            .setContentTitle(title)
            .setContentText(subtitle)
            .setSound(soundUri)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val cv = RemoteViews(applicationContext?.packageName, R.layout.custom_notification)
            cv.setDirection(R.id.custom_notification_root, this)
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

        notificationManager?.notify(notificationId, notificationBuilder.build())

        Handler(Looper.getMainLooper()).postDelayed({
            notificationManager?.cancel(notificationId)
            stopSelf()
        }, FIFTEEN_MINUTES_IN_MILLIS)

        return super.onStartCommand(intent, flags, startId)
    }
}
