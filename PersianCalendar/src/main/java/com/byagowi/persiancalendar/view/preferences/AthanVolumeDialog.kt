package com.byagowi.persiancalendar.view.preferences

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.Ringtone
import android.media.RingtoneManager
import android.view.View
import android.widget.SeekBar
import androidx.preference.PreferenceDialogFragmentCompat
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.util.UIUtils
import com.byagowi.persiancalendar.util.Utils
import java.io.IOException

class AthanVolumeDialog : PreferenceDialogFragmentCompat() {
  private val TAG = AthanVolumeDialog::class.java.name

  private var volume: Int = 0
  private var audioManager: AudioManager? = null
  private var ringtone: Ringtone? = null
  private var mediaPlayer: MediaPlayer? = null

  override fun onCreateDialogView(context: Context): View {
    val view = super.onCreateDialogView(context)

    val athanPref = preference as AthanVolumePreference

    audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    audioManager?.setStreamVolume(AudioManager.STREAM_ALARM, athanPref.volume, 0)

    val customAthanUri = Utils.getCustomAthanUri(context)
    if (customAthanUri != null) {
      ringtone = RingtoneManager.getRingtone(context, customAthanUri)
      ringtone?.streamType = AudioManager.STREAM_ALARM
      ringtone?.play()
    } else {
      val player = MediaPlayer()
      try {
        player.setDataSource(context, UIUtils.getDefaultAthanUri(context))
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

    val seekBar = view.findViewById<SeekBar>(R.id.sbVolumeSlider)

    volume = athanPref.volume
    seekBar.progress = volume
    seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

      override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        volume = progress
        audioManager?.setStreamVolume(AudioManager.STREAM_ALARM, progress, 0)
      }

      override fun onStartTrackingTouch(seekBar: SeekBar) {}

      override fun onStopTrackingTouch(seekBar: SeekBar) {
        val rngtone = ringtone
        if (rngtone != null && !rngtone.isPlaying) {
          rngtone.play()
        }
        val player = mediaPlayer
        if (player != null) {
          try {
            if (!player.isPlaying) {
              player.prepare()
              player.start()
            }
          } catch (e: IOException) {
            e.printStackTrace()
          } catch (e: IllegalStateException) {
            e.printStackTrace()
          }

        }
      }
    })

    return view
  }

  override fun onDialogClosed(positiveResult: Boolean) {
    val athanPref = preference as AthanVolumePreference
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
    if (positiveResult) {
      athanPref.volume = volume
    }
  }
}
