package com.byagowi.persiancalendar.view.preferences;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;

import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.util.Utils;

import java.io.IOException;

import androidx.preference.PreferenceDialogFragmentCompat;

public class AthanVolumeDialog extends PreferenceDialogFragmentCompat {
    private final String TAG = AthanVolumeDialog.class.getName();

    private int volume;
    private AudioManager audioManager;
    private Ringtone ringtone;

    @Override
    protected View onCreateDialogView(Context context) {
        View view = super.onCreateDialogView(context);

        final AthanVolumePreference athanPref = (AthanVolumePreference) getPreference();

        ringtone = RingtoneManager.getRingtone(context, Utils.getAthanUri(context));
        ringtone.setStreamType(AudioManager.STREAM_ALARM);

        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, athanPref.getVolume(), 0);
        }

        ringtone.play();

        SeekBar seekBar = view.findViewById(R.id.sbVolumeSlider);

        volume = athanPref.getVolume();
        seekBar.setProgress(volume);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                volume = progress;
                audioManager.setStreamVolume(AudioManager.STREAM_ALARM, progress, 0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (!ringtone.isPlaying()) {
                    ringtone.play();
                }
            }
        });

        return view;
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        final AthanVolumePreference athanPref = (AthanVolumePreference) getPreference();
        ringtone.stop();
        if (positiveResult) {
            athanPref.setVolume(volume);
        }
    }
}
