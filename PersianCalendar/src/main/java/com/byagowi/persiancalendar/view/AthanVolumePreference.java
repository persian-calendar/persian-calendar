package com.byagowi.persiancalendar.view;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;

import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.Utils;

import java.io.IOException;

public class AthanVolumePreference extends DialogPreference {
    private static final String TAG = "SliderPreference";
    private static AudioManager audioManager;
    private static MediaPlayer mediaPlayer;

    private Context context;
    private SeekBar seekBarVolumeSlider;
    private int initialVolume;
    private Utils utils = Utils.getInstance();
    private Uri athanSoundUri;

    public AthanVolumePreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.context = context;
        setDialogLayoutResource(R.layout.preference_volume);
        setDialogIcon(null);

        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        initialVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
        athanSoundUri = utils.getAthanUri(context);
    }

    public AthanVolumePreference(Context context) {
        this(context, null);
    }

    @Override
    protected View onCreateDialogView() {
        View view = super.onCreateDialogView();
        instantiateMediaPlayer();

        seekBarVolumeSlider = (SeekBar) view.findViewById(R.id.sbVolumeSlider);
        seekBarVolumeSlider.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_ALARM));
        seekBarVolumeSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.d(TAG, "volume: " + progress);
                audioManager.setStreamVolume(AudioManager.STREAM_ALARM, progress, 0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                try {
                    if (!mediaPlayer.isPlaying()) {
                        mediaPlayer.prepare();
                        mediaPlayer.start();
                    }
                } catch (IOException e) {
                    Log.e(TAG, "", e);
                }
            }
        });

        return view;
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        mediaPlayer.release();

        if (!positiveResult) {
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, initialVolume, 0);
        }
    }

    public void instantiateMediaPlayer() {
        try {
            mediaPlayer = null;
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
            mediaPlayer.setDataSource(getContext(), athanSoundUri);
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    instantiateMediaPlayer();
                }
            });
        } catch (IOException e) {
            Log.e(TAG, "", e);
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        if (isPersistent()) {
            return superState;
        }

        final SavedState myState = new SavedState(superState);
        myState.sliderValue = seekBarVolumeSlider.getProgress();
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state == null || !state.getClass().equals(SavedState.class)) {
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());

        seekBarVolumeSlider.setProgress(myState.sliderValue);
    }

    private static class SavedState extends BaseSavedState {
        int sliderValue;

        public SavedState(Parcel source) {
            super(source);
            sliderValue = source.readInt();
        }


        public SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);

            dest.writeInt(sliderValue);
        }

        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {

            @Override
            public SavedState createFromParcel(Parcel source) {
                return new SavedState(source);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
}
