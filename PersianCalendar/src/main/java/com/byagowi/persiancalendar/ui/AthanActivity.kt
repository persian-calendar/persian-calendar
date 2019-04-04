package com.byagowi.persiancalendar.ui

import android.media.AudioManager
import android.media.MediaPlayer
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Bundle
import android.os.Handler
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.getSystemService
import androidx.databinding.DataBindingUtil
import com.byagowi.persiancalendar.Constants
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.ActivityAthanBinding
import com.byagowi.persiancalendar.utils.Utils
import java.io.IOException
import java.util.concurrent.TimeUnit

class AthanActivity : AppCompatActivity() {
    private val handler = Handler()
    private var ringtone: Ringtone? = null
    private var mediaPlayer: MediaPlayer? = null
    private var alreadyStopped = false
    private var stopTask = object : Runnable {
        override fun run() {
            if (ringtone == null && mediaPlayer == null) {
                this@AthanActivity.finish()
                return
            }
            try {
                if (ringtone?.isPlaying == false) {
                    this@AthanActivity.finish()
                    return
                }
                if (mediaPlayer?.isPlaying == false) {
                    this@AthanActivity.finish()
                    return
                }
            } catch (e: Exception) {
                e.printStackTrace()
                this@AthanActivity.finish()
                return
            }

            handler.postDelayed(this, TimeUnit.SECONDS.toMillis(5))
        }
    }

    private var phoneStateListener: PhoneStateListener? = object : PhoneStateListener() {
        override fun onCallStateChanged(state: Int, incomingNumber: String) {
            if (state == TelephonyManager.CALL_STATE_RINGING || state == TelephonyManager.CALL_STATE_OFFHOOK) {
                stop()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getSystemService<AudioManager?>()?.setStreamVolume(AudioManager.STREAM_ALARM, Utils.getAthanVolume(this), 0)
        val customAthanUri = Utils.getCustomAthanUri(this)
        if (customAthanUri != null) {
            try {
                ringtone = RingtoneManager.getRingtone(this, customAthanUri).apply {
                    streamType = AudioManager.STREAM_ALARM
                    volumeControlStream = AudioManager.STREAM_ALARM
                    play()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            try {
                mediaPlayer = MediaPlayer().apply {
                    try {
                        setDataSource(this@AthanActivity, Utils.getDefaultAthanUri(this@AthanActivity))
                        setAudioStreamType(AudioManager.STREAM_ALARM)
                        volumeControlStream = AudioManager.STREAM_ALARM
                        prepare()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    start()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        Utils.applyAppLanguage(this)

        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val prayerKey = intent.getStringExtra(Constants.KEY_EXTRA_PRAYER_KEY)

        DataBindingUtil.setContentView<ActivityAthanBinding>(this, R.layout.activity_athan).apply {
            athanName.setText(Utils.getPrayTimeText(prayerKey))

            root.setOnClickListener { stop() }
            root.setBackgroundResource(Utils.getPrayTimeImage(prayerKey))

            place.text = String.format("%s %s",
                    getString(R.string.in_city_time),
                    Utils.getCityName(this@AthanActivity, true))
        }

        handler.postDelayed(stopTask, TimeUnit.SECONDS.toMillis(10))

        try {
            getSystemService<TelephonyManager?>()?.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE)
        } catch (e: Exception) {
            Log.e(TAG, "TelephonyManager handling fail", e)
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (!hasFocus) {
            stop()
        }
    }

    override fun onBackPressed() {
        stop()
    }

    private fun stop() {
        if (alreadyStopped) return
        alreadyStopped = true

        try {
            getSystemService<TelephonyManager?>()?.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE)
            phoneStateListener = null
        } catch (e: RuntimeException) {
            Log.e(TAG, "TelephonyManager handling fail", e)
        }

        ringtone?.stop()

        try {
            mediaPlayer?.apply {
                if (isPlaying) {
                    stop()
                    release()
                }
            }
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }

        handler.removeCallbacks(stopTask)
        finish()
    }

    companion object {
        private val TAG = AthanActivity::class.java.name
    }
}
