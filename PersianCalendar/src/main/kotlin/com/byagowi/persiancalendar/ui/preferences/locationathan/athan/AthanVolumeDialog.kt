package com.byagowi.persiancalendar.ui.preferences.locationathan.athan

import android.app.Activity
import android.media.AudioManager
import android.media.RingtoneManager
import androidx.core.content.edit
import androidx.core.content.getSystemService
import com.byagowi.persiancalendar.PREF_ATHAN_VOLUME
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.athanVolume
import com.byagowi.persiancalendar.utils.getAthanUri
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.slider.Slider

fun showAthanVolumeDialog(activity: Activity) {
    var volume = activity.athanVolume

    val audioManager = activity.getSystemService<AudioManager>() ?: return
    val originalAlarmVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM)
    audioManager.setStreamVolume(AudioManager.STREAM_ALARM, volume, 0)

    val ringtone = RingtoneManager.getRingtone(activity, getAthanUri(activity))
    ringtone?.streamType = AudioManager.STREAM_ALARM
    ringtone?.play()

    val slider = Slider(activity)
    slider.valueFrom = 0f
    slider.valueTo = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM).toFloat()
    slider.stepSize = 1f
    slider.value = volume.toFloat()
    slider.addOnChangeListener { _, value, _ ->
        if (ringtone?.isPlaying == false) {
            ringtone.play()
        }
        volume = value.toInt()
        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, volume, 0)
    }

    MaterialAlertDialogBuilder(activity)
        .setTitle(R.string.athan_volume)
        .setView(slider)
        .setPositiveButton(R.string.accept) { _, _ ->
            activity.appPrefs.edit { putInt(PREF_ATHAN_VOLUME, volume) }
        }
        .setNegativeButton(R.string.cancel, null)
        .setOnDismissListener {
            ringtone?.stop()
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, originalAlarmVolume, 0)
        }
        .show()
}
