package com.byagowi.persiancalendar.view.fragment;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.databinding.FragmentDeviceInfoBinding;
import com.byagowi.persiancalendar.di.dependencies.MainActivityDependency;
import com.byagowi.persiancalendar.view.deviceinfo.DeviceInfoAdapter;
import com.google.android.material.bottomnavigation.LabelVisibilityMode;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import dagger.android.support.DaggerFragment;

/**
 * @author MEHDI DIMYADI
 * MEHDIMYADI
 */
public class DeviceInfoFragment extends DaggerFragment {
    @Inject
    MainActivityDependency mainActivityDependency;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        FragmentDeviceInfoBinding binding = FragmentDeviceInfoBinding.inflate(inflater,
                container, false);

        mainActivityDependency.getMainActivity().setTitleAndSubtitle(
                getString(R.string.device_info), "");

        RecyclerView recyclerView = binding.recyclerView;
        DeviceInfoAdapter mAdapter = new DeviceInfoAdapter(
                mainActivityDependency.getMainActivity(), binding.getRoot());
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager =
                new LinearLayoutManager(mainActivityDependency.getMainActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(
                mainActivityDependency.getMainActivity(), LinearLayoutManager.VERTICAL));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        {
            Menu menu = binding.bottomNavigation.getMenu();

            menu.add(Build.VERSION.RELEASE);
            menu.getItem(0).setIcon(R.drawable.ic_developer);

            menu.add("API " + Build.VERSION.SDK_INT);
            menu.getItem(1).setIcon(R.drawable.ic_settings);

            menu.add(Build.CPU_ABI);
            menu.getItem(2).setIcon(R.drawable.ic_motorcycle);

            menu.add(Build.MODEL);
            menu.getItem(3).setIcon(R.drawable.ic_device_information);

            binding.bottomNavigation.setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_LABELED);
        }

        return binding.getRoot();
    }
}
