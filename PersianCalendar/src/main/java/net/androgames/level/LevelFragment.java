package net.androgames.level;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;

import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.di.dependencies.MainActivityDependency;
import com.byagowi.persiancalendar.view.activity.MainActivity;

import net.androgames.level.orientation.OrientationProvider;
import net.androgames.level.view.LevelView;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import dagger.android.support.DaggerFragment;

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
public class LevelFragment extends DaggerFragment {
    @Inject
    MainActivityDependency mainActivityDependency;

    private OrientationProvider provider;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        MainActivity mainActivity = mainActivityDependency.getMainActivity();
        mainActivity.setTitleAndSubtitle(getString(R.string.level), "");

        LevelView view = new LevelView(mainActivity);
        view.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        provider = new OrientationProvider(mainActivityDependency.getMainActivity(), view);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        provider.startListening();

        FragmentActivity activity = mainActivityDependency.getMainActivity();
        // https://stackoverflow.com/a/20017878
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        switch (rotation) {
            case Surface.ROTATION_180:
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
                break;
            case Surface.ROTATION_270:
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                break;
            case Surface.ROTATION_0:
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                break;
            case Surface.ROTATION_90:
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                break;
        }
    }

    @Override
    public void onPause() {
        if (provider.isListening()) {
            provider.stopListening();
        }
        mainActivityDependency.getMainActivity()
                .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        super.onPause();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.level_menu_buttons, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.compass)
            mainActivityDependency.getMainActivity().navigateTo(R.id.compass);
        else if (item.getItemId() == R.id.stop) {
            boolean stop = !provider.isListening();
            item.setIcon(stop ? R.drawable.ic_stop : R.drawable.ic_play);
            item.setTitle(stop ? R.string.stop : R.string.resume);
            if (stop) provider.startListening();
            else provider.stopListening();
        }
        return true;
    }
}
