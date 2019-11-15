package com.byagowi.persiancalendar.ui

import android.media.*
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.getSystemService
import com.byagowi.persiancalendar.DEFAULT_ATHAN_VOLUME
import com.byagowi.persiancalendar.KEY_EXTRA_PRAYER_KEY
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.ActivityAthanBinding
import com.byagowi.persiancalendar.utils.*
import java.io.IOException
import java.util.concurrent.TimeUnit

private val TAG = AthanActivity::class.java.name

class AthanActivity : AppCompatActivity() {

    private val ascendingVolumeStep = 6
    private var currentVolumeSteps = 0
    private var audioManager: AudioManager? = null
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

    private var ascendVolume = object : Runnable {
        override fun run() {
            currentVolumeSteps++
            audioManager?.setStreamVolume(AudioManager.STREAM_ALARM, currentVolumeSteps, 0)
            handler.postDelayed(this, TimeUnit.SECONDS.toMillis(ascendingVolumeStep.toLong()))
            if (currentVolumeSteps == 10) handler.removeCallbacks(this)
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
        val ascendingVolume = isAscendingAthanVolumeEnabled(this)
        val settingsVol = getAthanVolume(this)
        audioManager = getSystemService()
        audioManager?.let { am ->
            am.setStreamVolume(
                AudioManager.STREAM_ALARM,
                if (settingsVol == DEFAULT_ATHAN_VOLUME) settingsVol
                else am.getStreamVolume(AudioManager.STREAM_ALARM), 0
            )
        }

        val customAthanUri = getCustomAthanUri(this)
        if (customAthanUri != null) {
            try {
                ringtone = RingtoneManager.getRingtone(this, customAthanUri).apply {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        audioAttributes = AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
                            .build()
                    } else {
                        @Suppress("DEPRECATION")
                        streamType = AudioManager.STREAM_ALARM
                    }
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
                        setDataSource(this@AthanActivity, getDefaultAthanUri(this@AthanActivity))
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            setAudioAttributes(
                                AudioAttributes.Builder()
                                    .setUsage(AudioAttributes.USAGE_ALARM)
                                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                    .build()
                            )
                        } else {
                            @Suppress("DEPRECATION")
                            setAudioStreamType(AudioManager.STREAM_ALARM)
                        }
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

        applyAppLanguage(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }

        val prayerKey = intent.getStringExtra(KEY_EXTRA_PRAYER_KEY)

        ActivityAthanBinding.inflate(layoutInflater).apply {
            setContentView(root)
            athanName.setText(getPrayTimeText(prayerKey))

            root.setOnClickListener { stop() }
            root.setBackgroundResource(getPrayTimeImage(prayerKey))

            place.text = String.format(
                "%s %s",
                getString(R.string.in_city_time),
                getCityName(this@AthanActivity, true)
            )
        }

        handler.postDelayed(stopTask, TimeUnit.SECONDS.toMillis(10))

        if (ascendingVolume) handler.post(ascendVolume)

        try {
            getSystemService<TelephonyManager>()?.listen(
                phoneStateListener,
                PhoneStateListener.LISTEN_CALL_STATE
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (!hasFocus) {
            stop()
        }
    }

    override fun onBackPressed() = stop()

    private fun stop() {
        if (alreadyStopped) return
        alreadyStopped = true

        try {
            getSystemService<TelephonyManager>()?.listen(
                phoneStateListener,
                PhoneStateListener.LISTEN_NONE
            )
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
        handler.removeCallbacks(ascendVolume)
        finish()
    }
}
