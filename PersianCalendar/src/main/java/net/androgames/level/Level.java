package net.androgames.level;

import android.app.Activity;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.WindowManager;

import com.byagowi.persiancalendar.R;

import net.androgames.level.orientation.Orientation;
import net.androgames.level.orientation.OrientationListener;
import net.androgames.level.orientation.OrientationProvider;
import net.androgames.level.view.LevelView;

/*
 *  This file is part of Level (an Android Bubble Level).
 *  <https://github.com/avianey/Level>
 *
 *  Copyright (C) 2014 Antoine Vianey
 *
 *  Level is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Level is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Level. If not, see <http://www.gnu.org/licenses/>
 */
public class Level extends Activity implements OrientationListener {

    private OrientationProvider provider;

    private LevelView view;

    /**
     * Gestion du son
     */
    private SoundPool soundPool;
    private boolean soundEnabled;
    private int bipSoundID;
    private int bipRate;
    private long lastBip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        view = findViewById(R.id.level);
        // sound
        soundPool = new SoundPool(1, AudioManager.STREAM_RING, 0);
        bipSoundID = soundPool.load(this, R.raw.bip, 1);
        bipRate = getResources().getInteger(R.integer.bip_rate);
        provider = new OrientationProvider(this);
        if (provider.isSupported()) {
            provider.startListening(this);
        }
    }

    public OrientationProvider getProvider() {
        return provider;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (provider.isSupported() && !provider.isListening()) {
            provider.startListening(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (provider.isListening()) {
            provider.stopListening();
        }
    }

    @Override
    public void onDestroy() {
        if (soundPool != null) {
            soundPool.release();
        }
        super.onDestroy();
    }

    @Override
    public void onOrientationChanged(Orientation orientation, float pitch, float roll, float balance) {
//        if (soundEnabled
//                && orientation.isLevel(pitch, roll, balance, provider.getSensibility())
//                && System.currentTimeMillis() - lastBip > bipRate) {
//            AudioManager mgr = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
//            float streamVolumeCurrent = mgr.getStreamVolume(AudioManager.STREAM_RING);
//            float streamVolumeMax = mgr.getStreamMaxVolume(AudioManager.STREAM_RING);
//            float volume = streamVolumeCurrent / streamVolumeMax;
//            lastBip = System.currentTimeMillis();
//            soundPool.play(bipSoundID, volume, volume, 1, 0, 1);
//        }
        view.onOrientationChanged(orientation, pitch, roll, balance);
    }

    @Override
    public void onCalibrationReset(boolean success) {
//        Toast.makeText(this, success ?
//                        R.string.calibrate_restored : R.string.calibrate_failed,
//                Level.TOAST_DURATION).show();
    }

    @Override
    public void onCalibrationSaved(boolean success) {
//        Toast.makeText(this, success ?
//                        R.string.calibrate_saved : R.string.calibrate_failed,
//                Level.TOAST_DURATION).show();
    }
}
