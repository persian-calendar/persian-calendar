package com.byagowi.persiancalendar.ui.preferences.locationathan.athan

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.Ringtone
import android.media.RingtoneManager
import android.view.View
import android.widget.SeekBar
import androidx.core.content.getSystemService
import androidx.preference.PreferenceDialogFragmentCompat
import com.byagowi.persiancalendar.utils.getCustomAthanUri
import com.byagowi.persiancalendar.utils.getDefaultAthanUri
import com.byagowi.persiancalendar.utils.logException

class AthanVolumeDialog : PreferenceDialogFragmentCompat() {

    private var volume: Int = 0
    private var ringtone: Ringtone? = null
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreateDialogView(context: Context?): View {
        val athanPref = preference as AthanVolumePreference
        val audioManager = context?.getSystemService<AudioManager>()
        audioManager?.setStreamVolume(AudioManager.STREAM_ALARM, athanPref.volume, 0)

        val customAthanUri = context?.let { getCustomAthanUri(it) }
        if (customAthanUri != null) {
            ringtone = RingtoneManager.getRingtone(context, customAthanUri).apply {
                streamType = AudioManager.STREAM_ALARM
                play()
            }
        } else if (context != null) {
            val player = MediaPlayer()
            runCatching {
                player.setDataSource(context, getDefaultAthanUri(context))
                player.setAudioStreamType(AudioManager.STREAM_ALARM)
                player.prepare()
            }.onFailure(logException)

            runCatching {
                player.start()
                mediaPlayer = player
            }.onFailure(logException)
        }

        return SeekBar(context).apply {
            max = audioManager?.getStreamMaxVolume(AudioManager.STREAM_ALARM) ?: 7
            volume = athanPref.volume
            progress = volume
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    volume = progress
                    audioManager?.setStreamVolume(AudioManager.STREAM_ALARM, progress, 0)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    if (ringtone?.isPlaying == false) {
                        ringtone?.play()
                    }
                    runCatching {
                        if (mediaPlayer?.isPlaying == false) {
                            mediaPlayer?.prepare()
                            mediaPlayer?.start()
                        }
                    }.onFailure(logException)
                }
            })
        }
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        val athanPref = preference as AthanVolumePreference

        ringtone?.stop()

        runCatching {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.stop()
                mediaPlayer?.release()
            }
        }.onFailure(logException)

        if (positiveResult) athanPref.volume = volume
    }
}
