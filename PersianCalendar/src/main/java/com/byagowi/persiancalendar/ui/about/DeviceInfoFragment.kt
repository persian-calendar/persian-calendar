package com.byagowi.persiancalendar.ui.about

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager

import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.DeviceInfoRowBinding
import com.byagowi.persiancalendar.databinding.FragmentDeviceInfoBinding
import com.byagowi.persiancalendar.di.dependencies.MainActivityDependency
import com.byagowi.persiancalendar.utils.Utils
import com.google.android.material.bottomnavigation.LabelVisibilityMode

import java.util.ArrayList
import java.util.Locale

import javax.inject.Inject
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.android.support.DaggerFragment

/**
 * @author MEHDI DIMYADI
 * MEHDIMYADI
 */
class DeviceInfoFragment : DaggerFragment() {
    @Inject
    internal var mainActivityDependency: MainActivityDependency? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val binding = FragmentDeviceInfoBinding.inflate(inflater,
                container, false)

        mainActivityDependency!!.mainActivity.setTitleAndSubtitle(
                getString(R.string.device_info), "")

        val recyclerView = binding.recyclerView
        val mAdapter = DeviceInfoAdapter(
                mainActivityDependency!!.mainActivity, binding.root)
        recyclerView.setHasFixedSize(true)
        val mLayoutManager = LinearLayoutManager(mainActivityDependency!!.mainActivity)
        recyclerView.layoutManager = mLayoutManager
        recyclerView.addItemDecoration(DividerItemDecoration(
                mainActivityDependency!!.mainActivity, LinearLayoutManager.VERTICAL))
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.adapter = mAdapter

        run {
            val menu = binding.bottomNavigation.menu

            menu.add(Build.VERSION.RELEASE)
            menu.getItem(0).setIcon(R.drawable.ic_developer)

            menu.add("API " + Build.VERSION.SDK_INT)
            menu.getItem(1).setIcon(R.drawable.ic_settings)

            menu.add(Build.CPU_ABI)
            menu.getItem(2).setIcon(R.drawable.ic_motorcycle)

            menu.add(Build.MODEL)
            menu.getItem(3).setIcon(R.drawable.ic_device_information)

            binding.bottomNavigation.labelVisibilityMode = LabelVisibilityMode.LABEL_VISIBILITY_LABELED
        }

        return binding.root
    }
}

class DeviceInfoAdapter internal constructor(activity: Activity, private val mRootView: View) : RecyclerView.Adapter<DeviceInfoAdapter.ViewHolder>() {
    private val deviceInfoItemsList = ArrayList<DeviceInfoItem>()

    init {

        deviceInfoItemsList.add(DeviceInfoItem(
                "Screen Resolution",
                getScreenResolution(activity.windowManager),
                ""
        ))

        deviceInfoItemsList.add(DeviceInfoItem(
                "Android Version",
                Build.VERSION.CODENAME + " " + Build.VERSION.RELEASE,
                Integer.toString(Build.VERSION.SDK_INT)
        ))

        deviceInfoItemsList.add(DeviceInfoItem(
                "Manufacturer",
                Build.MANUFACTURER, ""
        ))

        deviceInfoItemsList.add(DeviceInfoItem(
                "Brand",
                Build.BRAND, ""
        ))

        deviceInfoItemsList.add(DeviceInfoItem(
                "Model",
                Build.MODEL, ""
        ))

        deviceInfoItemsList.add(DeviceInfoItem(
                "Product",
                Build.PRODUCT, ""
        ))

        deviceInfoItemsList.add(DeviceInfoItem(
                "Instruction CPU 1",
                Build.CPU_ABI, ""
        ))

        deviceInfoItemsList.add(DeviceInfoItem(
                "Instruction CPU 2",
                Build.CPU_ABI2, ""
        ))

        deviceInfoItemsList.add(DeviceInfoItem(
                "Instruction Architecture",
                Build.DEVICE, ""
        ))

        deviceInfoItemsList.add(DeviceInfoItem(
                "Android Id",
                Build.ID, ""
        ))

        deviceInfoItemsList.add(DeviceInfoItem(
                "Board",
                Build.BOARD, ""
        ))

        deviceInfoItemsList.add(DeviceInfoItem(
                "Radio Firmware Version",
                Build.getRadioVersion(), ""
        ))

        deviceInfoItemsList.add(DeviceInfoItem(
                "Build User",
                Build.USER, ""
        ))

        deviceInfoItemsList.add(DeviceInfoItem(
                "Host",
                Build.HOST, ""
        ))

        deviceInfoItemsList.add(DeviceInfoItem(
                "Display",
                Build.DISPLAY, ""
        ))

        deviceInfoItemsList.add(DeviceInfoItem(
                "Device Fingerprints",
                Build.FINGERPRINT, ""
        ))

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

    private fun getScreenResolution(wm: WindowManager): String {
        return String.format(Locale.ENGLISH, "%d*%d pixels",
                wm.defaultDisplay.width, wm.defaultDisplay.height)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = DeviceInfoRowBinding.inflate(
                LayoutInflater.from(parent.context), parent, false)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int {
        return deviceInfoItemsList.size
    }

    class DeviceInfoItem(val title: String, val content: String, val version: String)

    inner class ViewHolder(private val mBinding: DeviceInfoRowBinding) : RecyclerView.ViewHolder(mBinding.root), View.OnClickListener {
        var mPosition = 0

        init {
            mBinding.root.setOnClickListener(this)
        }

        fun bind(position: Int) {
            mPosition = position
            val info = deviceInfoItemsList[position]
            mBinding.title.text = info.title
            mBinding.content.text = info.content
            mBinding.version.text = info.version
        }

        override fun onClick(v: View) {
            val info = deviceInfoItemsList[mPosition]
            Utils.copyToClipboard(mRootView, info.title, info.content)
        }
    }
}
