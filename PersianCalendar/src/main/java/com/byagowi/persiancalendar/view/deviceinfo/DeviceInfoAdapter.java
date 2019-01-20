package com.byagowi.persiancalendar.view.deviceinfo;

import android.app.Activity;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.byagowi.persiancalendar.databinding.DeviceInfoRowBinding;
import com.byagowi.persiancalendar.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * @author MEHDI DIMYADI
 * MEHDIMYADI
 */
public class DeviceInfoAdapter extends RecyclerView.Adapter<DeviceInfoAdapter.ViewHolder> {

    private List<DeviceInfoItem> deviceInfoItemsList = new ArrayList<>();

    public DeviceInfoAdapter(Activity activity) {
        deviceInfoItemsList.add(new DeviceInfoItem(
                "Screen Resolution",
                getScreenResolution(activity.getWindowManager()),
                null
        ));

        deviceInfoItemsList.add(new DeviceInfoItem(
                "Android Version",
                Build.VERSION.CODENAME + " " + Build.VERSION.RELEASE,
                Integer.toString(Build.VERSION.SDK_INT)
        ));

        deviceInfoItemsList.add(new DeviceInfoItem(
                "Manufacturer",
                Build.MANUFACTURER,
                null
        ));

        deviceInfoItemsList.add(new DeviceInfoItem(
                "Brand",
                Build.BRAND,
                null
        ));

        deviceInfoItemsList.add(new DeviceInfoItem(
                "Model",
                Build.MODEL,
                null
        ));

        deviceInfoItemsList.add(new DeviceInfoItem(
                "Product",
                Build.PRODUCT,
                null
        ));

        deviceInfoItemsList.add(new DeviceInfoItem(
                "Instruction CPU 1",
                Build.CPU_ABI,
                null
        ));

        deviceInfoItemsList.add(new DeviceInfoItem(
                "Instruction CPU 2",
                Build.CPU_ABI2,
                null
        ));

        deviceInfoItemsList.add(new DeviceInfoItem(
                "Instruction Architecture",
                Build.DEVICE,
                null
        ));

        deviceInfoItemsList.add(new DeviceInfoItem(
                "Android Id",
                Build.ID,
                null
        ));

        deviceInfoItemsList.add(new DeviceInfoItem(
                "Board",
                Build.BOARD,
                null
        ));

        deviceInfoItemsList.add(new DeviceInfoItem(
                "Radio Firmware Version",
                Build.getRadioVersion(),
                null
        ));

        deviceInfoItemsList.add(new DeviceInfoItem(
                "Build User",
                Build.USER,
                null
        ));

        deviceInfoItemsList.add(new DeviceInfoItem(
                "Host",
                Build.HOST,
                null
        ));

        deviceInfoItemsList.add(new DeviceInfoItem(
                "Display",
                Build.DISPLAY,
                null
        ));

        deviceInfoItemsList.add(new DeviceInfoItem(
                "Device Fingerprints",
                Build.FINGERPRINT,
                null
        ));

// If one wants to add kernel related cpu information
//        try {
//            for (File fileEntry : new File("/sys/devices/system/cpu/cpu0/cpufreq/").listFiles()) {
//                if (fileEntry.isDirectory()) continue;
//                try {
//                    deviceInfoItemsList.add(new DeviceInfoItem(
//                            fileEntry.getAbsolutePath(),
//                            Utils.readStream(new FileInputStream(fileEntry)),
//                            null
//                    ));
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    private String getScreenResolution(WindowManager wm) {
        return String.format(Locale.ENGLISH, "%d*%d pixels",
                wm.getDefaultDisplay().getWidth(), wm.getDefaultDisplay().getHeight());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        DeviceInfoRowBinding binding = DeviceInfoRowBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);

        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return deviceInfoItemsList.size();
    }

    static public class DeviceInfoItem {
        final String title, content, version;

        DeviceInfoItem(String title, String content, String version) {
            this.title = title;
            this.content = content;
            this.version = version;
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final DeviceInfoRowBinding mBinding;
        int mPosition = 0;

        ViewHolder(DeviceInfoRowBinding binding) {
            super(binding.getRoot());
            binding.getRoot().setOnClickListener(this);
            mBinding = binding;
        }

        void bind(int position) {
            mPosition = position;
            DeviceInfoItem info = deviceInfoItemsList.get(position);
            mBinding.title.setText(info.title);
            mBinding.content.setText(info.content);
            mBinding.version.setText(info.version);
        }

        @Override
        public void onClick(View v) {
            DeviceInfoItem info = deviceInfoItemsList.get(mPosition);
            Utils.copyToClipboard(v.getContext(), info.title, info.content);
        }
    }
}
