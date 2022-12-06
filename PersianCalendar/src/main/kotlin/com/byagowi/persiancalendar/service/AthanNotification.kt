package com.byagowi.persiancalendar.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import androidx.core.os.postDelayed
import com.byagowi.persiancalendar.ASR_KEY
import com.byagowi.persiancalendar.BuildConfig
import com.byagowi.persiancalendar.DHUHR_KEY
import com.byagowi.persiancalendar.FAJR_KEY
import com.byagowi.persiancalendar.ISHA_KEY
import com.byagowi.persiancalendar.KEY_EXTRA_PRAYER
import com.byagowi.persiancalendar.MAGHRIB_KEY
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.global.calculationMethod
import com.byagowi.persiancalendar.global.coordinates
import com.byagowi.persiancalendar.global.spacedComma
import com.byagowi.persiancalendar.utils.SIX_MINUTES_IN_MILLIS
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.applyAppLanguage
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
        intent ?: return START_STICKY

        applyAppLanguage(this)

        val notificationId =
            if (BuildConfig.DEVELOPMENT) Random.nextInt(2000, 4000) else 3000
        val notificationChannelId = notificationId.toString()

        val notificationManager = getSystemService<NotificationManager>()

        val soundUri = getAthanUri(this)
        runCatching {
            // ensure custom reminder sounds play well
            grantUriPermission(
                "com.android.systemui", soundUri, Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }.onFailure(logException)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                notificationChannelId, getString(R.string.athan),
                NotificationManager.IMPORTANCE_HIGH
            ).also {
                it.description = getString(R.string.athan)
                it.enableLights(true)
                it.lightColor = Color.GREEN
                it.vibrationPattern = LongArray(2) { 500 }
                it.enableVibration(true)
                it.setBypassDnd(true)
                it.setSound(
                    soundUri, AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .setLegacyStreamType(AudioManager.STREAM_NOTIFICATION)
                        .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
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
            DHUHR_KEY ->
                if (calculationMethod.isJafari) listOf(R.string.sunset)
                else listOf(R.string.asr, R.string.sunset)
            ASR_KEY -> listOf(R.string.sunset)
            MAGHRIB_KEY ->
                if (calculationMethod.isJafari) listOf(R.string.midnight)
                else listOf(R.string.isha, R.string.midnight)
            ISHA_KEY -> listOf(R.string.midnight)
            else -> listOf(R.string.midnight)
        }.joinToString(" - ") {
            "${getString(it)}: ${prayTimes?.getFromStringId(it)?.toFormattedString() ?: ""}"
        }

        val notificationBuilder = NotificationCompat.Builder(this, notificationChannelId)
        notificationBuilder.setAutoCancel(true)
            .setWhen(System.currentTimeMillis())
            .setSmallIcon(R.drawable.sun)
            .setContentTitle(title)
            .setContentText(subtitle)
            .setSound(soundUri, AudioManager.STREAM_NOTIFICATION)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

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

        startForeground(notificationId, notificationBuilder.build())

        Handler(Looper.getMainLooper()).postDelayed(SIX_MINUTES_IN_MILLIS) {
            notificationManager?.cancel(notificationId)
            stopSelf()
        }

        return super.onStartCommand(intent, flags, startId)
    }
}
