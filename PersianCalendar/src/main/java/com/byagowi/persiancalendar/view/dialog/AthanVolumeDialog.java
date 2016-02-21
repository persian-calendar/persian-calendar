package com.byagowi.persiancalendar.view.dialog;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;

import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.Utils;

import java.io.IOException;

public class AthanVolumeDialog extends PreferenceDialogFragmentCompat {
    public static AthanVolumeDialog newInstance(Preference preference) {
        AthanVolumeDialog fragment = new AthanVolumeDialog();
        Bundle bundle = new Bundle(1);
        bundle.putString("key", preference.getKey());
        fragment.setArguments(bundle);
        return fragment;
    }

    float volume;
    MediaPlayer mediaPlayer;

    @Override
    protected View onCreateDialogView(Context context) {
        View view = super.onCreateDialogView(context);

        final AthanVolumePreference athanPref = (AthanVolumePreference)getPreference();
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
            mediaPlayer.setDataSource(
                    getContext(),
                    Utils.getInstance(getContext()).getAthanUri());
            mediaPlayer.setVolume(athanPref.getVolume(), athanPref.getVolume());
        } catch (IOException e) {
            Log.e("AthanPref", "", e);
        }

        SeekBar seekBar = (SeekBar) view.findViewById(R.id.sbVolumeSlider);

        seekBar.setProgress((int)athanPref.getVolume());
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                volume = progress;
                mediaPlayer.setVolume(progress, progress);
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
                } catch (IOException ignored) {
                }
            }
        });

        return view;
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        final AthanVolumePreference athanPref = (AthanVolumePreference)getPreference();
        mediaPlayer.release();
        if (positiveResult) {
            athanPref.setVolume(volume);
        }
    }

    @Override
    public void setInitialSavedState(SavedState state) {
        super.setInitialSavedState(state);
    }
}
