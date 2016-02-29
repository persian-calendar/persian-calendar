package com.byagowi.persiancalendar.view.preferences;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;

import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.util.Utils;

import java.io.IOException;

public class AthanVolumeDialog extends PreferenceDialogFragmentCompat {
    private final String TAG = AthanVolumeDialog.class.getName();

    int volume;
    AudioManager audioManager;
    MediaPlayer mediaPlayer;

    @Override
    protected View onCreateDialogView(Context context) {
        View view = super.onCreateDialogView(context);

        final AthanVolumePreference athanPref = (AthanVolumePreference) getPreference();
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
            mediaPlayer.setDataSource(
                    getContext(),
                    Utils.getInstance(getContext()).getAthanUri());
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, athanPref.getVolume(), 0);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }

        SeekBar seekBar = (SeekBar) view.findViewById(R.id.sbVolumeSlider);

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
                try {
                    if (!mediaPlayer.isPlaying()) {
                        mediaPlayer.prepare();
                        mediaPlayer.start();
                    }
                } catch (IOException | IllegalStateException ignored) {
                }
            }
        });

        return view;
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        final AthanVolumePreference athanPref = (AthanVolumePreference) getPreference();
        mediaPlayer.release();
        if (positiveResult) {
            athanPref.setVolume(volume);
        }
    }
}
