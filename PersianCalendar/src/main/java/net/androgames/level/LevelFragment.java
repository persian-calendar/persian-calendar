package net.androgames.level;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;

import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.databinding.FragmentLevelBinding;
import com.byagowi.persiancalendar.di.dependencies.MainActivityDependency;
import com.byagowi.persiancalendar.ui.MainActivity;

import net.androgames.level.orientation.OrientationProvider;

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
        MainActivity mainActivity = mainActivityDependency.getMainActivity();
        mainActivity.setTitleAndSubtitle(getString(R.string.level), "");

        FragmentLevelBinding binding = FragmentLevelBinding.inflate(inflater, container, false);
        provider = new OrientationProvider(mainActivity, binding.levelView);

        binding.bottomAppbar.replaceMenu(R.menu.level_menu_buttons);
        binding.bottomAppbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.compass)
                mainActivity.navigateTo(R.id.compass);
            return true;
        });
        binding.fab.setOnClickListener(v -> {
            boolean stop = !provider.isListening();
            binding.fab.setImageResource(stop ? R.drawable.ic_stop : R.drawable.ic_play);
            binding.fab.setContentDescription(mainActivity.getString(stop ? R.string.stop : R.string.resume));
            if (stop) provider.startListening();
            else provider.stopListening();
        });

        return binding.getRoot();
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
}
