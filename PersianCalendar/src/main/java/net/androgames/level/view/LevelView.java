package net.androgames.level.view;

import android.content.Context;
import android.os.Handler;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import net.androgames.level.orientation.Orientation;
import net.androgames.level.painter.LevelPainter;

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
public class LevelView extends SurfaceView implements SurfaceHolder.Callback {

    private LevelPainter painter;

    public LevelView(Context context) {
        super(context);
        getHolder().addCallback(this);
        setFocusable(true);
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        if (painter != null) {
            painter.pause(!hasWindowFocus);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (painter != null) {
            painter.setSurfaceSize(width, height);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Context context = getContext();
        if (painter == null) {
            painter = new LevelPainter(holder, context, new Handler(),
                    true,
                    false,
                    false);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (painter != null) {
            painter.pause(true);
            painter.clean();
            painter = null;
        }
        // free resources
        System.gc();
    }

    public void onOrientationChanged(Orientation orientation, float pitch, float roll, float balance) {
        if (painter != null) {
            painter.onOrientationChanged(orientation, pitch, roll, balance);
        }
    }
}
