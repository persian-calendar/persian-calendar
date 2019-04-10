package com.byagowi.persiancalendar.ui.preferences.locationathan.athan

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.view.View
import android.widget.SeekBar

import com.byagowi.persiancalendar.utils.Utils

import java.io.IOException

import androidx.preference.PreferenceDialogFragmentCompat

class AthanVolumeDialog : PreferenceDialogFragmentCompat() {
    private var volume: Int = 0
    private var audioManager: AudioManager? = null
    private var ringtone: Ringtone? = null
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreateDialogView(context: Context): View {
        val athanPref = preference as AthanVolumePreference

        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (audioManager != null) {
            audioManager!!.setStreamVolume(AudioManager.STREAM_ALARM, athanPref.volume, 0)
        }

        val customAthanUri = Utils.getCustomAthanUri(context)
        if (customAthanUri != null) {
            ringtone = RingtoneManager.getRingtone(context, customAthanUri)
            ringtone!!.streamType = AudioManager.STREAM_ALARM
            ringtone!!.play()
        } else {
            val player = MediaPlayer()
            try {
                player.setDataSource(context, Utils.getDefaultAthanUri(context))
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

        val seekBar = SeekBar(context)
        seekBar.max = 7

        volume = athanPref.volume
        seekBar.progress = volume
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                volume = progress
                audioManager!!.setStreamVolume(AudioManager.STREAM_ALARM, progress, 0)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                if (ringtone != null && !ringtone!!.isPlaying) {
                    ringtone!!.play()
                }
                if (mediaPlayer != null) {
                    try {
                        if (!mediaPlayer!!.isPlaying) {
                            mediaPlayer!!.prepare()
                            mediaPlayer!!.start()
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    } catch (e: IllegalStateException) {
                        e.printStackTrace()
                    }

                }
            }
        })

        return seekBar
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        val athanPref = preference as AthanVolumePreference
        if (ringtone != null) {
            ringtone!!.stop()
        }
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer!!.isPlaying) {
                    mediaPlayer!!.stop()
                    mediaPlayer!!.release()
                }
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            }

        }
        if (positiveResult) {
            athanPref.volume = volume
        }
    }
}
