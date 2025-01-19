package com.byagowi.persiancalendar.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
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
import com.byagowi.persiancalendar.BuildConfig
import com.byagowi.persiancalendar.KEY_EXTRA_PRAYER
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.PrayTime
import com.byagowi.persiancalendar.entities.PrayTime.Companion.get
import com.byagowi.persiancalendar.global.athanVibration
import com.byagowi.persiancalendar.global.calculationMethod
import com.byagowi.persiancalendar.global.cityName
import com.byagowi.persiancalendar.global.coordinates
import com.byagowi.persiancalendar.global.notificationAthan
import com.byagowi.persiancalendar.global.spacedComma
import com.byagowi.persiancalendar.ui.athan.AthanActivity
import com.byagowi.persiancalendar.ui.athan.AthanActivity.Companion.CANCEL_ATHAN_NOTIFICATION
import com.byagowi.persiancalendar.ui.athan.PreventPhoneCallIntervention
import com.byagowi.persiancalendar.utils.SIX_MINUTES_IN_MILLIS
import com.byagowi.persiancalendar.utils.applyAppLanguage
import com.byagowi.persiancalendar.utils.calculatePrayTimes
import com.byagowi.persiancalendar.utils.getAthanUri
import com.byagowi.persiancalendar.utils.logException
import com.byagowi.persiancalendar.utils.setDirection
import com.byagowi.persiancalendar.utils.startAthanActivity
import com.byagowi.persiancalendar.variants.debugAssertNotNull
import kotlin.random.Random

class AthanNotification : Service() {

    override fun onBind(intent: Intent): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent ?: return START_STICKY
        applyAppLanguage(this)

        val athanVibration = athanVibration.value
        val notificationAthan = notificationAthan.value
        val notificationId = if (BuildConfig.DEVELOPMENT) Random.nextInt(2000, 4000) else {
            if (notificationAthan) (if (athanVibration) 3000 else 3002)
            else (if (athanVibration) 3001 else 3003)
        }
        val notificationChannelId = notificationId.toString()

        val notificationManager = getSystemService<NotificationManager>()

        val prayTime = PrayTime.fromName(
            intent.getStringExtra(KEY_EXTRA_PRAYER)
        ).debugAssertNotNull ?: PrayTime.FAJR
        if (!notificationAthan) startAthanActivity(this, prayTime)

        val soundUri = if (notificationAthan) getAthanUri(this) else null
        if (soundUri != null) runCatching {
            // ensure custom reminder sounds play well
            grantUriPermission(
                "com.android.systemui", soundUri, Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }.onFailure(logException)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                notificationChannelId, getString(R.string.athan),
                if (notificationAthan) NotificationManager.IMPORTANCE_HIGH
                else NotificationManager.IMPORTANCE_DEFAULT
            ).also {
                it.description = getString(R.string.athan)
                it.enableLights(true)
                it.lightColor = Color.GREEN
                if (athanVibration) it.vibrationPattern = LongArray(2) { 500 }
                it.enableVibration(athanVibration)
                it.setBypassDnd(prayTime.isBypassDnd)
                if (soundUri == null) it.setSound(null, null) else it.setSound(
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

        val cityName = cityName.value
        val prayTimeName = getString(prayTime.stringRes)
        val title =
            if (cityName == null) prayTimeName
            else "$prayTimeName$spacedComma${getString(R.string.in_city_time, cityName)}"

        val prayTimes = coordinates.value?.calculatePrayTimes()
        val isJafari = calculationMethod.value.isJafari
        val subtitle = prayTime.upcomingTimes(isJafari).joinToString(" - ") {
            "${getString(it.stringRes)}: ${prayTimes?.get(it)?.toFormattedString() ?: ""}"
        }

        val notificationBuilder = NotificationCompat.Builder(this, notificationChannelId)
        notificationBuilder.setAutoCancel(true)
            .setWhen(System.currentTimeMillis())
            .setSmallIcon(prayTime.drawable)
            .setContentTitle(title)
            .setContentText(subtitle)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(
                PendingIntent.getActivity(
                    this, 0,
                    Intent(this, AthanActivity::class.java)
                        .setAction(CANCEL_ATHAN_NOTIFICATION)
                        .putExtra(KEY_EXTRA_PRAYER, prayTime)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                    PendingIntent.FLAG_UPDATE_CURRENT or
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
                )
            )

        if (notificationAthan) {
            notificationBuilder.priority = NotificationCompat.PRIORITY_MAX
            notificationBuilder.setSound(soundUri, AudioManager.STREAM_NOTIFICATION)
            notificationBuilder.setCategory(NotificationCompat.CATEGORY_ALARM)
        } else {
            notificationBuilder.setSound(null)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val cv = RemoteViews(applicationContext?.packageName, R.layout.custom_notification)
            cv.setDirection(R.id.custom_notification_root, this.resources)
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

        val preventPhoneCallIntervention =
            if (notificationAthan) PreventPhoneCallIntervention(cleanUp) else null
        cleanUp = {
            preventPhoneCallIntervention?.stopListener?.invoke()
            notificationManager?.cancel(notificationId)
        }

        preventPhoneCallIntervention?.startListener(this)
        Handler(Looper.getMainLooper()).postDelayed(SIX_MINUTES_IN_MILLIS) { cleanUp() }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        cleanUp()
        super.onDestroy()
    }

    private var cleanUp = {}
}
