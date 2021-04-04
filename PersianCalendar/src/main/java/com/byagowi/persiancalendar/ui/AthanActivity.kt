package com.byagowi.persiancalendar.ui

import android.app.KeyguardManager
import android.media.*
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.getSystemService
import com.byagowi.persiancalendar.DEFAULT_ATHAN_VOLUME
import com.byagowi.persiancalendar.KEY_EXTRA_PRAYER_KEY
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.ActivityAthanBinding
import com.byagowi.persiancalendar.utils.*
import java.util.concurrent.TimeUnit

class AthanActivity : AppCompatActivity() {

    private val ascendingVolumeStep = 6
    private var currentVolumeSteps = 1
    private var audioManager: AudioManager? = null
    private val handler = Handler(Looper.getMainLooper())
    private var ringtone: Ringtone? = null
    private var mediaPlayer: MediaPlayer? = null
    private var alreadyStopped = false
    private var spentSeconds = 0
    private val stopTask = object : Runnable {
        override fun run() = runCatching {
            spentSeconds += 5
            if ((ringtone == null && mediaPlayer == null) ||
                ringtone?.isPlaying == false ||
                mediaPlayer?.isPlaying == false ||
                spentSeconds > 240
            ) this@AthanActivity.finish()
            else handler.postDelayed(this, TimeUnit.SECONDS.toMillis(5))
            Unit
        }.onFailure { this@AthanActivity.finish() }.getOrElse(logException)
    }

    private val ascendVolume = object : Runnable {
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

        // Workaround AlarmManager (or the way we use it) that calls it multiple times,
        // don't run if it is ran less than 10 seconds ago
        val currentMillis = System.currentTimeMillis()
        if (currentMillis - lastStart < TimeUnit.SECONDS.toMillis(10)) return finish()
        lastStart = currentMillis
        //

        audioManager = getSystemService()
        audioManager?.let { am ->
            am.setStreamVolume(
                AudioManager.STREAM_ALARM,
                athanVolume.takeUnless { it == DEFAULT_ATHAN_VOLUME } ?: am.getStreamVolume(
                    AudioManager.STREAM_ALARM
                ),
                0
            )
        }

        val customAthanUri = getCustomAthanUri(this)
        runCatching {
            if (customAthanUri != null) {
                ringtone = RingtoneManager.getRingtone(this, customAthanUri).apply {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        audioAttributes = AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .build()
                    } else {
                        @Suppress("DEPRECATION")
                        streamType = AudioManager.STREAM_ALARM
                    }
                    volumeControlStream = AudioManager.STREAM_ALARM
                    play()
                }
            } else {
                mediaPlayer = MediaPlayer().apply {
                    runCatching {
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
                    }.onFailure(logException)
                    start()
                }
            }
        }.onFailure(logException)

        applyAppLanguage(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            getSystemService<KeyguardManager>()?.requestDismissKeyguard(this, null)
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

            place.text = listOf(
                getString(R.string.in_city_time), getCityName(this@AthanActivity, true)
            ).joinToString(" ")
        }

        handler.postDelayed(stopTask, TimeUnit.SECONDS.toMillis(10))

        if (isAscendingAthanVolumeEnabled) handler.post(ascendVolume)

        runCatching {
            getSystemService<TelephonyManager>()?.listen(
                phoneStateListener,
                PhoneStateListener.LISTEN_CALL_STATE
            )
        }.onFailure(logException)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (!hasFocus) stop()
    }

    override fun onBackPressed() = stop()

    private fun stop() {
        if (alreadyStopped) return
        alreadyStopped = true

        runCatching {
            getSystemService<TelephonyManager>()?.listen(
                phoneStateListener, PhoneStateListener.LISTEN_NONE
            )
            phoneStateListener = null
        }.onFailure(logException)

        ringtone?.stop()

        runCatching {
            mediaPlayer?.apply {
                if (isPlaying) {
                    stop()
                    release()
                }
            }
        }.onFailure(logException)

        handler.removeCallbacks(stopTask)
        handler.removeCallbacks(ascendVolume)
        finish()
    }

    companion object {
        private var lastStart = 0L
    }
}
