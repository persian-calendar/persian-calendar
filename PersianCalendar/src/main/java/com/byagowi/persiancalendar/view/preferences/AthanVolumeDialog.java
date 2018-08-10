package com.byagowi.persiancalendar.view.preferences;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.view.View;
import android.widget.SeekBar;

import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.util.UIUtils;
import com.byagowi.persiancalendar.util.Utils;

import java.io.IOException;

import androidx.preference.PreferenceDialogFragmentCompat;

public class AthanVolumeDialog extends PreferenceDialogFragmentCompat {
    private final String TAG = AthanVolumeDialog.class.getName();

    private int volume;
    private AudioManager audioManager;
    private Ringtone ringtone;
    private MediaPlayer mediaPlayer;

    @Override
    protected View onCreateDialogView(Context context) {
        View view = super.onCreateDialogView(context);

        final AthanVolumePreference athanPref = (AthanVolumePreference) getPreference();

        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, athanPref.getVolume(), 0);
        }

        Uri customAthanUri = Utils.getCustomAthanUri(context);
        if (customAthanUri != null) {
            ringtone = RingtoneManager.getRingtone(context, customAthanUri);
            ringtone.setStreamType(AudioManager.STREAM_ALARM);
            ringtone.play();
        } else {
            MediaPlayer player = new MediaPlayer();
            try {
                player.setDataSource(context, UIUtils.getDefaultAthanUri(context));
                player.setAudioStreamType(AudioManager.STREAM_ALARM);
                player.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                player.start();
                mediaPlayer = player;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

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
                if (ringtone != null && !ringtone.isPlaying()) {
                    ringtone.play();
                }
                if (mediaPlayer != null) {
                    try {
                        if (!mediaPlayer.isPlaying()) {
                            mediaPlayer.prepare();
                            mediaPlayer.start();
                        }
                    } catch (IOException | IllegalStateException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        return view;
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        final AthanVolumePreference athanPref = (AthanVolumePreference) getPreference();
        if (ringtone != null) {
            ringtone.stop();
        }
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                }
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
        if (positiveResult) {
            athanPref.setVolume(volume);
        }
    }
}
