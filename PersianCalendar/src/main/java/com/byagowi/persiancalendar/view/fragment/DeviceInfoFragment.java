package com.byagowi.persiancalendar.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.di.dependencies.MainActivityDependency;
import com.byagowi.persiancalendar.view.deviceinfo.DeviceAdapter;
import com.byagowi.persiancalendar.view.deviceinfo.DeviceInfoUtils;
import com.byagowi.persiancalendar.view.deviceinfo.InfoList;
import com.byagowi.persiancalendar.view.deviceinfo.RecyclerTouchListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * @author MEHDI DIMYADI
 * MEHDIMYADI
 */
public class DeviceInfoFragment extends Fragment {

    @Inject
    MainActivityDependency mainActivityDependency;
    private List<InfoList> DeviceInfoList = new ArrayList<>();
    private DeviceAdapter mAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_device_info, container, false);

        //MainActivity mainActivity = mainActivityDependency.getMainActivity();
        //mainActivity.setTitleAndSubtitle(getString(R.string.device_info), "");

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        mAdapter = new DeviceAdapter(DeviceInfoList);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(Objects.requireNonNull(getActivity()).getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getActivity().getApplicationContext(), recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                InfoList DeviceInfo = DeviceInfoList.get(position);
                Toast.makeText(Objects.requireNonNull(getActivity()).getApplicationContext(), DeviceInfo.getTitle() + " is selected!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

        prepareDeviceInfoData();

        return view;

    }

    private void prepareDeviceInfoData() {
        InfoList DeviceInfo = new InfoList(getString(R.string.info_manufacturer), DeviceInfoUtils.getDeviceBrand(), null);
        DeviceInfoList.add(DeviceInfo);

        DeviceInfo = new InfoList(getString(R.string.info_Brand), DeviceInfoUtils.getDeviceBrand(), null);
        DeviceInfoList.add(DeviceInfo);

        DeviceInfo = new InfoList(getString(R.string.info_model), DeviceInfoUtils.getSystemModel(), DeviceInfoUtils.getSystemVersion());
        DeviceInfoList.add(DeviceInfo);

        DeviceInfo = new InfoList(getString(R.string.info_Serial_No), DeviceInfoUtils.getSerialNo(), null);
        DeviceInfoList.add(DeviceInfo);

        DeviceInfo = new InfoList(getString(R.string.info_androidId), DeviceInfoUtils.getBuildId(), null);
        DeviceInfoList.add(DeviceInfo);

        DeviceInfo = new InfoList(getString(R.string.info_board), DeviceInfoUtils.getBuildBoard(), null);
        DeviceInfoList.add(DeviceInfo);

        DeviceInfo = new InfoList(getString(R.string.info_user), DeviceInfoUtils.getBuildUser(), null);
        DeviceInfoList.add(DeviceInfo);

        DeviceInfo = new InfoList(getString(R.string.info_host), DeviceInfoUtils.getBuildHost(), null);
        DeviceInfoList.add(DeviceInfo);

        DeviceInfo = new InfoList(getString(R.string.info_screen), getScreenResolution(), null);
        DeviceInfoList.add(DeviceInfo);

        mAdapter.notifyDataSetChanged();
    }

    private String getScreenResolution() {
        WindowManager wm = Objects.requireNonNull(getActivity()).getWindowManager();
        if (null != wm) {
            int width = wm.getDefaultDisplay().getWidth();
            int height = wm.getDefaultDisplay().getHeight();
            return width + "*" + height + " Pixels";
        } else {
            return null;
        }
    }
}
