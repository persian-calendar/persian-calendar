package com.byagowi.persiancalendar.view.dialog;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.Preference;
import android.support.v7.preference.DialogPreference;
import android.util.AttributeSet;
import android.widget.SeekBar;

import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.Utils;

public class AthanVolumePreference extends DialogPreference {
    public static AudioManager audioManager;
    public static MediaPlayer mediaPlayer;

    public Context context;
    public SeekBar seekBarVolumeSlider;
    public int initialVolume;
    public Utils utils = Utils.getInstance();

    public AthanVolumePreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.context = context;
        setDialogLayoutResource(R.layout.preference_volume);
        setDialogIcon(null);

        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        initialVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
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


    private static class SavedState extends Preference.BaseSavedState {
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

        public static final Parcelable.Creator<SavedState> CREATOR
                = new Parcelable.Creator<SavedState>() {

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
