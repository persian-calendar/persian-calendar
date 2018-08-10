package com.byagowi.persiancalendar.view.activity

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Bundle
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.byagowi.persiancalendar.Constants
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.ActivityAthanBinding
import com.byagowi.persiancalendar.util.UIUtils
import com.byagowi.persiancalendar.util.Utils
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit

class AthanActivity : AppCompatActivity(), View.OnClickListener {
  private var ringtone: Ringtone? = null
  private var mediaPlayer: MediaPlayer? = null

  private val phoneStateListener = object : PhoneStateListener() {
    override fun onCallStateChanged(state: Int, incomingNumber: String) {
      if (state == TelephonyManager.CALL_STATE_RINGING || state == TelephonyManager.CALL_STATE_OFFHOOK) {
        stop()
        finish()
      }
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
    audioManager?.setStreamVolume(AudioManager.STREAM_ALARM, Utils.getAthanVolume(this), 0)

    val customAthanUri = Utils.getCustomAthanUri(this)
    if (customAthanUri != null) {
      ringtone = RingtoneManager.getRingtone(this, customAthanUri)
      ringtone?.streamType = AudioManager.STREAM_ALARM
      ringtone?.play()
    } else {
      val player = MediaPlayer()
      try {
        player.setDataSource(this, UIUtils.getDefaultAthanUri(this))
        player.setAudioStreamType(AudioManager.STREAM_ALARM)
        player.prepare()
      } catch (e: IOException) {
        e.printStackTrace()
      }

      try {
        player.start()
        mediaPlayer = player
      } catch (e: Exception) {
        e.printStackTrace()
      }

    }

    Utils.changeAppLanguage(this)

    val binding = DataBindingUtil.setContentView<ActivityAthanBinding>(this, R.layout.activity_athan)

    window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

    val prayerKey = intent.getStringExtra(Constants.KEY_EXTRA_PRAYER_KEY)
    binding.athanName.setText(UIUtils.getPrayTimeText(prayerKey))

    val root = binding.root
    root.setOnClickListener(this)
    root.setBackgroundResource(UIUtils.getPrayTimeImage(prayerKey))

    binding.place.text = getString(R.string.in_city_time) + " " + Utils.getCityName(this, true)

    Timer().scheduleAtFixedRate(object : TimerTask() {
      override fun run() {
        if (ringtone == null && mediaPlayer == null) {
          cancel()
          finish()
        }
        try {
          val rngtone = ringtone
          if (rngtone != null) {
            if (!rngtone.isPlaying) {
              cancel()
              finish()
              return
            }
          }
          val player = mediaPlayer
          if (player != null) {
            if (!player.isPlaying) {
              cancel()
              finish()
              return
            }
          }
        } catch (e: Exception) {
          e.printStackTrace()
          cancel()
          finish()
        }

      }
    }, TimeUnit.SECONDS.toMillis(10), TimeUnit.SECONDS.toMillis(5))

    try {
      val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
      telephonyManager?.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE)
    } catch (e: Exception) {
      Log.e(TAG, "TelephonyManager handling fail", e)
    }

  }

  override fun onWindowFocusChanged(hasFocus: Boolean) {
    super.onWindowFocusChanged(hasFocus)
    if (!hasFocus) {
      stop()
      finish()
    }
  }

  override fun onClick(v: View) {
    stop()
    finish()
  }

  override fun onBackPressed() {
    stop()
    finish()
  }

  private fun stop() {
    ringtone?.stop()
    val player = mediaPlayer
    if (player != null) {
      try {
        if (player.isPlaying) {
          player.stop()
          player.release()
        }
      } catch (e: IllegalStateException) {
        e.printStackTrace()
      }

    }
  }

  companion object {
    private val TAG = AthanActivity::class.java.name
  }
}
