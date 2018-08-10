package com.byagowi.persiancalendar.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.widget.RemoteViews
import androidx.annotation.Nullable
import androidx.core.app.NotificationCompat
import com.byagowi.persiancalendar.BuildConfig
import com.byagowi.persiancalendar.Constants
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.util.UIUtils
import com.byagowi.persiancalendar.util.Utils
import java.util.*
import java.util.concurrent.TimeUnit

@SuppressLint("Registered")
class AthanNotification : Service() {
  internal var NOTIFICATION_CHANNEL_ID = "1002"

  override fun onBind(intent: Intent): IBinder? = null

  override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
    //            AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    //            if (audioManager != null) {
    //                audioManager.setStreamVolume(AudioManager.STREAM_ALARM, Utils.getAthanVolume(this), 0);
    //            }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val notificationChannel = NotificationChannel(NOTIFICATION_CHANNEL_ID, getString(R.string.app_name),
          NotificationManager.IMPORTANCE_DEFAULT)

      //                AudioAttributes att = new AudioAttributes.Builder()
      //                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
      //                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
      //                        .setLegacyStreamType(AudioManager.STREAM_ALARM)
      //                        .build();
      notificationChannel.description = getString(R.string.app_name)
      notificationChannel.enableLights(true)
      notificationChannel.lightColor = Color.GREEN
      notificationChannel.vibrationPattern = longArrayOf(0, 1000, 500, 1000)
      notificationChannel.enableVibration(true)
      //                notificationChannel.setSound(Utils.getAthanUri(getApplicationContext()), att);
      notificationManager?.createNotificationChannel(notificationChannel)
    }

    val title = getString(UIUtils.getPrayTimeText(
        intent.getStringExtra(Constants.KEY_EXTRA_PRAYER_KEY)))
    val subtitle = getString(R.string.in_city_time) + " " +
        Utils.getCityName(this, true)

    var notificationBuilder = NotificationCompat.Builder(this,
        NOTIFICATION_CHANNEL_ID)
    notificationBuilder.setAutoCancel(true)
        .setWhen(System.currentTimeMillis())
        .setSmallIcon(R.drawable.sun)
        //                    .setSound(Utils.getAthanUri(getApplicationContext()), AudioManager.STREAM_ALARM)
        .setContentTitle(title)
        .setContentText(subtitle)

    notificationBuilder.setDefaults(NotificationCompat.DEFAULT_VIBRATE)
    notificationBuilder.setDefaults(NotificationCompat.DEFAULT_SOUND)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N || BuildConfig.DEBUG) {
      val cv = RemoteViews(applicationContext.packageName,
          if (Utils.isLocaleRTL)
            R.layout.custom_notification
          else
            R.layout.custom_notification_ltr)
      cv.setTextViewText(R.id.title, title)
      cv.setTextViewText(R.id.body, subtitle)

      notificationBuilder = notificationBuilder
          .setCustomContentView(cv)
          .setStyle(NotificationCompat.DecoratedCustomViewStyle())
    }

    notificationManager?.notify(NOTIFICATION_ID, notificationBuilder.build())

    Timer().schedule(object : TimerTask() {
      override fun run() {
        notificationManager?.cancel(NOTIFICATION_ID)
        stopSelf()
      }
    }, TimeUnit.MINUTES.toMillis(5))

    return super.onStartCommand(intent, flags, startId)
  }

  companion object {

    val instance: AthanNotification?
      @Nullable
      get() = null

    private val NOTIFICATION_ID = 1002
  }

}