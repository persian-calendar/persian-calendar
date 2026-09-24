package com.byagowi.persiancalendar.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.media.AudioManager
import android.os.Build
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.content.edit
import androidx.core.content.getSystemService
import com.byagowi.persiancalendar.DEFAULT_ATHAN_CHANNEL_ID
import com.byagowi.persiancalendar.PREF_ATHAN_CHANNEL_ID
import com.byagowi.persiancalendar.PREF_ATHAN_GAP
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.PrayTime
import com.byagowi.persiancalendar.entities.PrayTime.Companion.get
import com.byagowi.persiancalendar.global.athanVibration
import com.byagowi.persiancalendar.global.calculationMethod
import com.byagowi.persiancalendar.global.cityName
import com.byagowi.persiancalendar.global.coordinates
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.numeral
import com.byagowi.persiancalendar.global.spacedComma
import com.byagowi.persiancalendar.utils.applyAppLanguage
import com.byagowi.persiancalendar.utils.calculatePrayTimes
import com.byagowi.persiancalendar.utils.debugLog
import com.byagowi.persiancalendar.utils.getAthanUri
import com.byagowi.persiancalendar.utils.logException
import com.byagowi.persiancalendar.utils.preferences
import com.byagowi.persiancalendar.utils.setDirection
import kotlin.math.abs
import kotlin.math.roundToInt

fun startAthanNotification(context: Context, prayTime: PrayTime) {
    debugLog("Alarms: startAthanNotification for $prayTime")
    applyAppLanguage(context)

    val notificationId = currentChannelId(context)
    val notificationChannelId = "$notificationId"

    val notificationManager = context.getSystemService<NotificationManager>() ?: return

    val soundUri = getAthanUri(context)
    runCatching {
        // ensure custom reminder sounds play well
        context.grantUriPermission(
            "com.android.systemui", soundUri, Intent.FLAG_GRANT_READ_URI_PERMISSION,
        )
    }.onFailure(logException)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val notificationChannel = NotificationChannel(
            notificationChannelId, context.getString(R.string.athan),
            NotificationManager.IMPORTANCE_HIGH,
        ).also {
            it.description = context.getString(R.string.athan)
            it.enableLights(true)
            it.lightColor = Color.GREEN
            if (athanVibration) it.vibrationPattern = LongArray(2) { 500 }
            it.enableVibration(athanVibration)
            it.setBypassDnd(prayTime.isBypassDnd)
            it.setSound(
                soundUri,
                AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setLegacyStreamType(AudioManager.STREAM_NOTIFICATION)
                    .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED).build(),
            )
        }
        notificationManager.createNotificationChannel(notificationChannel)
    }

    val prayTimeName = context.getString(prayTime.stringRes).let {
        if (!language.isPersianOrDari) return@let it
        val athanGap = (context.preferences.getString(PREF_ATHAN_GAP, null)?.toDoubleOrNull()
            ?: .0).roundToInt()
        if (athanGap <= 0) return@let "اذان $it"
        context.resources.getQuantityString(
            R.plurals.minutes,
            abs(athanGap),
            numeral.format(abs(athanGap)),
        ) + " پیش از اذان " + it
    }
    val title = if (cityName == null) prayTimeName
    else "$prayTimeName$spacedComma${context.getString(R.string.in_city_time, cityName)}"

    val prayTimes = coordinates?.calculatePrayTimes()
    val isJafari = calculationMethod.isJafari
    val subtitle = prayTime.upcomingTimes(isJafari).joinToString(" - ") {
        "${context.getString(it.stringRes)}: ${prayTimes?.get(it)?.toFormattedString().orEmpty()}"
    }

    val notificationBuilder = NotificationCompat.Builder(context, notificationChannelId)
    notificationBuilder.setAutoCancel(true).setWhen(System.currentTimeMillis())
        .setSmallIcon(prayTime.drawable).setContentTitle(title).setContentText(subtitle)
        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
//        .setContentIntent(
//            PendingIntent.getActivity(
//                context, 0,
//                Intent(context, AthanActivity::class.java)
//                    .setAction(CANCEL_ATHAN_NOTIFICATION)
//                    .putExtra(KEY_EXTRA_PRAYER, prayTime)
//                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
//                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
//            ),
//        )

    notificationBuilder.priority = NotificationCompat.PRIORITY_MAX
    notificationBuilder.setSound(soundUri, AudioManager.STREAM_NOTIFICATION)
    notificationBuilder.setCategory(NotificationCompat.CATEGORY_ALARM)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        val cv = RemoteViews(context.packageName, R.layout.custom_notification)
        cv.setDirection(R.id.custom_notification_root, context.resources)
        cv.setTextViewText(R.id.title, title)
        if (subtitle.isEmpty()) {
            cv.setViewVisibility(R.id.body, View.GONE)
        } else {
            cv.setTextViewText(R.id.body, subtitle)
        }

        notificationBuilder.setCustomContentView(cv)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
    }

    notificationManager.notify(notificationId, notificationBuilder.build())

//    Handler(Looper.getMainLooper()).postDelayed(6.minutes.inWholeMilliseconds) {
//        notificationManager?.cancel(notificationId)
//    }
}

fun invalidateAthanChannel(context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
    val current = context.preferences.getInt(PREF_ATHAN_CHANNEL_ID, DEFAULT_ATHAN_CHANNEL_ID)
    context.getSystemService<NotificationManager>()?.deleteNotificationChannel("$current")
    context.preferences.edit { putInt(PREF_ATHAN_CHANNEL_ID, current + 1) }
}

private fun currentChannelId(context: Context): Int {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return DEFAULT_ATHAN_CHANNEL_ID
    val preferences = context.preferences
    return if (PREF_ATHAN_CHANNEL_ID !in preferences) {
        context.getSystemService<NotificationManager>()?.let { nm ->
            // Just clean up historical ids along the way
            (3000..3003).forEach { nm.deleteNotificationChannel("$it") }
        }
        context.preferences.edit { putInt(PREF_ATHAN_CHANNEL_ID, DEFAULT_ATHAN_CHANNEL_ID) }
        DEFAULT_ATHAN_CHANNEL_ID
    } else context.preferences.getInt(PREF_ATHAN_CHANNEL_ID, DEFAULT_ATHAN_CHANNEL_ID)
}
