package com.byagowi.persiancalendar.view.dialog;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
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

    @Override
    protected View onCreateDialogView(Context context) {
        View view = super.onCreateDialogView(context);

        final AthanVolumePreference athanPref = (AthanVolumePreference)getPreference();
        instantiateMediaPlayer(athanPref);

        SeekBar seekBar = (SeekBar) view.findViewById(R.id.sbVolumeSlider);
        athanPref.seekBarVolumeSlider = seekBar;

        seekBar.setProgress(athanPref.audioManager.getStreamVolume(AudioManager.STREAM_ALARM));
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                athanPref.audioManager.setStreamVolume(AudioManager.STREAM_ALARM, progress, 0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                try {
                    if (!athanPref.mediaPlayer.isPlaying()) {
                        athanPref.mediaPlayer.prepare();
                        athanPref.mediaPlayer.start();
                    }
                } catch (IOException ignored) {
                }
            }
        });

        return view;
    }

    public void instantiateMediaPlayer(final AthanVolumePreference athanPref) {
        try {
            final MediaPlayer mediaPlayer = new MediaPlayer();
            athanPref.mediaPlayer = mediaPlayer;

            mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
            mediaPlayer.setDataSource(
                    getContext(),
                    Utils.getInstance(getContext()).getAthanUri());

            mediaPlayer.setOnCompletionListener(
                    new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            instantiateMediaPlayer(athanPref);
                        }
                    });
        } catch (IOException ignored) {
        }
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        final AthanVolumePreference athanPref = (AthanVolumePreference)getPreference();
        athanPref.mediaPlayer.release();

        if (!positiveResult) {
            athanPref.audioManager.setStreamVolume(
                    AudioManager.STREAM_ALARM,
                    athanPref.initialVolume, 0);
        }
    }

    @Override
    public void setInitialSavedState(SavedState state) {
        super.setInitialSavedState(state);
    }
}
