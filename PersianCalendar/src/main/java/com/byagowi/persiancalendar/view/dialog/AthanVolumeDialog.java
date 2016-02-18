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
        instantiateMediaPlayer();

        ((AthanVolumePreference)getPreference())
                .seekBarVolumeSlider = (SeekBar) view.findViewById(R.id.sbVolumeSlider);

        ((AthanVolumePreference)getPreference())
                .seekBarVolumeSlider
                .setProgress(AthanVolumePreference
                                .audioManager
                                .getStreamVolume(AudioManager.STREAM_ALARM));

        ((AthanVolumePreference)getPreference())
                .seekBarVolumeSlider
                .setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                AthanVolumePreference
                        .audioManager
                        .setStreamVolume(AudioManager.STREAM_ALARM, progress, 0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                try {
                    if (!AthanVolumePreference.mediaPlayer.isPlaying()) {
                        AthanVolumePreference.mediaPlayer.prepare();
                        AthanVolumePreference.mediaPlayer.start();
                    }
                } catch (IOException ignored) {
                }
            }
        });

        return view;
    }

    public void instantiateMediaPlayer() {
        try {
            AthanVolumePreference.mediaPlayer = null;
            AthanVolumePreference.mediaPlayer = new MediaPlayer();
            AthanVolumePreference.mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);

            AthanVolumePreference.mediaPlayer.setDataSource(
                    getContext(),
                    Utils.getInstance(getContext()).getAthanUri());

            AthanVolumePreference.mediaPlayer.setOnCompletionListener(
                    new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            instantiateMediaPlayer();
                        }
                    });
        } catch (IOException ignored) {
        }
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        AthanVolumePreference.mediaPlayer.release();

        if (!positiveResult) {
            AthanVolumePreference.audioManager.setStreamVolume(
                    AudioManager.STREAM_ALARM,
                    ((AthanVolumePreference) getPreference()).initialVolume, 0);
        }
    }

    @Override
    public void setInitialSavedState(SavedState state) {
        super.setInitialSavedState(state);
    }
}
