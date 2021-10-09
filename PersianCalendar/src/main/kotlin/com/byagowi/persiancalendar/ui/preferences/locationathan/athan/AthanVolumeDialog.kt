package com.byagowi.persiancalendar.ui.preferences.locationathan.athan

import android.app.Activity
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.Ringtone
import android.media.RingtoneManager
import android.widget.SeekBar
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import com.byagowi.persiancalendar.PREF_ATHAN_VOLUME
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.athanVolume
import com.byagowi.persiancalendar.utils.getCustomAthanUri
import com.byagowi.persiancalendar.utils.getRawUri
import com.byagowi.persiancalendar.utils.logException

fun showAthanVolumeDialog(activity: Activity) {
    var volume = activity.athanVolume
    var ringtone: Ringtone? = null
    var mediaPlayer: MediaPlayer? = null

    val audioManager = activity.getSystemService<AudioManager>() ?: return
    val originalAlarmVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM)
    audioManager.setStreamVolume(AudioManager.STREAM_ALARM, volume, 0)

    val customAthanUri = getCustomAthanUri(activity)
    if (customAthanUri != null) {
        ringtone = RingtoneManager.getRingtone(activity, customAthanUri).also {
            it.streamType = AudioManager.STREAM_ALARM
            it.play()
        }
    } else {
        val player = MediaPlayer()
        runCatching {
            player.setDataSource(activity, activity.getRawUri(R.raw.abdulbasit).toUri())
            player.setAudioStreamType(AudioManager.STREAM_ALARM)
            player.prepare()
        }.onFailure(logException)

        runCatching {
            player.start()
            mediaPlayer = player
        }.onFailure(logException)
    }

    val seekBar = SeekBar(activity)
    seekBar.max = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
    seekBar.progress = volume
    seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
        override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
        override fun onProgressChanged(
            seekBar: SeekBar?, progress: Int, fromUser: Boolean
        ) {
            volume = progress
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, progress, 0)
        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
            if (ringtone?.isPlaying == false) {
                ringtone.play()
            }
            runCatching {
                if (mediaPlayer?.isPlaying == false) {
                    mediaPlayer?.prepare()
                    mediaPlayer?.start()
                }
            }.onFailure(logException)
        }
    })

    AlertDialog.Builder(activity)
        .setTitle(R.string.athan_volume)
        .setView(seekBar)
        .setPositiveButton(R.string.accept) { _, _ ->
            activity.appPrefs.edit { putInt(PREF_ATHAN_VOLUME, volume) }
        }
        .setNegativeButton(R.string.cancel, null)
        .setOnDismissListener {
            ringtone?.stop()
            runCatching {
                if (mediaPlayer?.isPlaying == true) {
                    mediaPlayer?.stop()
                    mediaPlayer?.release()
                }
            }.onFailure(logException)
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, originalAlarmVolume, 0)
        }
        .show()
}
