package net.androgames.level;

import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.byagowi.persiancalendar.R;

import net.androgames.level.orientation.Orientation;
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
public class Level extends Activity {

    private OrientationProvider provider;
    private LevelView view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        view = new LevelView(this);
        addContentView(view, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        provider = new OrientationProvider(this, view);
        provider.startListening();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!provider.isListening()) {
            provider.startListening();
        }
    }

    @Override
    protected void onPause() {
        if (provider.isListening()) {
            provider.stopListening();
        }
        super.onPause();
    }
}
