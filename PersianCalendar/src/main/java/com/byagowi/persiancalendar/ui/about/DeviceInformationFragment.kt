package com.byagowi.persiancalendar.ui.about

import android.app.Activity
import android.hardware.Sensor
import android.hardware.SensorManager
import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.GLES10
import android.opengl.GLES20
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import androidx.recyclerview.widget.*
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.DeviceInfoRowBinding
import com.byagowi.persiancalendar.databinding.FragmentDeviceInfoBinding
import com.byagowi.persiancalendar.di.MainActivityDependency
import com.byagowi.persiancalendar.utils.circularRevealFromMiddle
import com.byagowi.persiancalendar.utils.copyToClipboard
import com.google.android.material.bottomnavigation.LabelVisibilityMode
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.android.support.DaggerFragment
import java.util.*
import javax.inject.Inject

/**
 * @author MEHDI DIMYADI
 * MEHDIMYADI
 */
class DeviceInformationFragment : DaggerFragment() {

    @Inject
    lateinit var mainActivityDependency: MainActivityDependency

    private var clickCount: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = FragmentDeviceInfoBinding.inflate(inflater, container, false).apply {
        val activity = mainActivityDependency.mainActivity

        activity.setTitleAndSubtitle(getString(R.string.device_info), "")

        circularRevealFromMiddle(circularReveal)

        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(activity)
            addItemDecoration(DividerItemDecoration(activity, LinearLayoutManager.VERTICAL))
            adapter = DeviceInfoAdapter(activity, root)
        }

        bottomNavigation.apply {
            menu.apply {
                add(Build.VERSION.RELEASE)
                getItem(0).setIcon(R.drawable.ic_developer)

                add("API " + Build.VERSION.SDK_INT)
                getItem(1).setIcon(R.drawable.ic_settings)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    add(Build.SUPPORTED_ABIS[0])
                } else {
                    add(Build.CPU_ABI)
                }
                getItem(2).setIcon(R.drawable.ic_motorcycle)

                add(Build.MODEL)
                getItem(3).setIcon(R.drawable.ic_device_information_white)
            }
            labelVisibilityMode = LabelVisibilityMode.LABEL_VISIBILITY_LABELED
            setOnNavigationItemSelectedListener {
                // Easter egg
                if (++clickCount % 10 == 0) {
                    BottomSheetDialog(activity).apply {
                        setContentView(IndeterminateProgressBar(activity).apply {
                            layoutParams =
                                ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 700)
                        })
                    }.show()
                }
                true
            }
        }
    }.root
}

class DeviceInfoAdapter(activity: Activity, private val rootView: View) :
    ListAdapter<DeviceInfoAdapter.DeviceInfoItem, DeviceInfoAdapter.ViewHolder>(
        DeviceInfoDiffCallback()
    ) {
    private val deviceInfoItemsList = ArrayList<DeviceInfoItem>()

    data class DeviceInfoItem(val title: String, val content: CharSequence?, val version: String)

    class DeviceInfoDiffCallback : DiffUtil.ItemCallback<DeviceInfoItem>() {
        override fun areItemsTheSame(oldItem: DeviceInfoItem, newItem: DeviceInfoItem): Boolean =
            oldItem == newItem

        override fun areContentsTheSame(oldItem: DeviceInfoItem, newItem: DeviceInfoItem): Boolean =
            oldItem == newItem
    }

    init {
        deviceInfoItemsList.apply {
            fun addIfNotNull(title: String, content: CharSequence?, version: String) =
                deviceInfoItemsList.add(DeviceInfoItem(title, content ?: "Unknown", version))

            fun getScreenResolution(wm: WindowManager) = String.format(
                Locale.ENGLISH, "%d*%d pixels", wm.defaultDisplay.width, wm.defaultDisplay.height
            )
            addIfNotNull("Screen Resolution", getScreenResolution(activity.windowManager), "")
            addIfNotNull(
                "Android Version", Build.VERSION.CODENAME + " " + Build.VERSION.RELEASE,
                Build.VERSION.SDK_INT.toString()
            )
            addIfNotNull("Manufacturer", Build.MANUFACTURER, "")
            addIfNotNull("Brand", Build.BRAND, "")
            addIfNotNull("Model", Build.MODEL, "")
            addIfNotNull("Product", Build.PRODUCT, "")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Build.SUPPORTED_ABIS.forEachIndexed { index, abi ->
                    addIfNotNull("Instruction CPU ${index + 1}", abi, "")
                }
            } else {
                addIfNotNull("Instruction CPU 1", Build.CPU_ABI, "")
                addIfNotNull("Instruction CPU 2", Build.CPU_ABI2, "")
            }

            addIfNotNull("Instruction Architecture", Build.DEVICE, "")
            addIfNotNull("Android Id", Build.ID, "")
            addIfNotNull("Board", Build.BOARD, "")
            addIfNotNull("Radio Firmware Version", Build.getRadioVersion(), "")
            addIfNotNull("Build User", Build.USER, "")
            addIfNotNull("Host", Build.HOST, "")
            addIfNotNull("Display", Build.DISPLAY, "")
            addIfNotNull("Device Fingerprints", Build.FINGERPRINT, "")
            addIfNotNull(
                "Sensors",
                (activity.getSystemService<SensorManager>())
                    ?.getSensorList(Sensor.TYPE_ALL)?.joinToString("\n"), ""
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                addIfNotNull(
                    "Battery",
                    (activity.getSystemService<BatteryManager>())?.run {
                        listOf("Charging: $isCharging") + listOf(
                            "Capacity" to BatteryManager.BATTERY_PROPERTY_CAPACITY,
                            "Charge Counter" to BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER,
                            "Current Avg" to BatteryManager.BATTERY_PROPERTY_CURRENT_AVERAGE,
                            "Current Now" to BatteryManager.BATTERY_PROPERTY_CURRENT_NOW,
                            "Energy Counter" to BatteryManager.BATTERY_PROPERTY_ENERGY_COUNTER
                        ).map { "${it.first}: ${getLongProperty(it.second)}" }
                    }?.joinToString("\n"), ""
                )
            }

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    // Quick Kung-fu to create gl context, https://stackoverflow.com/a/27092070
                    val display = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
                    val versions = IntArray(2)
                    EGL14.eglInitialize(display, versions, 0, versions, 1)
                    val configAttr = intArrayOf(
                        EGL14.EGL_COLOR_BUFFER_TYPE, EGL14.EGL_RGB_BUFFER,
                        EGL14.EGL_LEVEL, 0, EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                        EGL14.EGL_SURFACE_TYPE, EGL14.EGL_PBUFFER_BIT, EGL14.EGL_NONE
                    )
                    val configs = Array<EGLConfig?>(1) { null }
                    val configsCount = IntArray(1)
                    EGL14.eglChooseConfig(display, configAttr, 0, configs, 0, 1, configsCount, 0)
                    if (configsCount[0] != 0) {
                        val surf = EGL14.eglCreatePbufferSurface(
                            display, configs[0],
                            intArrayOf(EGL14.EGL_WIDTH, 64, EGL14.EGL_HEIGHT, 64, EGL14.EGL_NONE), 0
                        )
                        EGL14.eglMakeCurrent(
                            display, surf, surf, EGL14.eglCreateContext(
                                display, configs[0], EGL14.EGL_NO_CONTEXT,
                                intArrayOf(EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, EGL14.EGL_NONE), 0
                            )
                        )
                    }
                }
                addIfNotNull(
                    "OpenGL",
                    (listOf(
                        "GL_VERSION" to GLES20.GL_VERSION,
                        "GL_RENDERER" to GLES20.GL_RENDERER,
                        "GL_VENDOR" to GLES20.GL_VENDOR
                    ).map { "${it.first}: ${GLES20.glGetString(it.second)}" } + listOf(
                        "GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS" to GLES20.GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS,
                        "GL_MAX_CUBE_MAP_TEXTURE_SIZE" to GLES20.GL_MAX_CUBE_MAP_TEXTURE_SIZE,
                        "GL_MAX_FRAGMENT_UNIFORM_VECTORS" to GLES20.GL_MAX_FRAGMENT_UNIFORM_VECTORS,
                        "GL_MAX_RENDERBUFFER_SIZE" to GLES20.GL_MAX_RENDERBUFFER_SIZE,
                        "GL_MAX_TEXTURE_IMAGE_UNITS" to GLES20.GL_MAX_TEXTURE_IMAGE_UNITS,
                        "GL_MAX_TEXTURE_SIZE" to GLES20.GL_MAX_TEXTURE_SIZE,
                        "GL_MAX_VARYING_VECTORS" to GLES20.GL_MAX_VARYING_VECTORS,
                        "GL_MAX_VERTEX_ATTRIBS" to GLES20.GL_MAX_VERTEX_ATTRIBS,
                        "GL_MAX_VERTEX_TEXTURE_IMAGE_UNITS" to GLES20.GL_MAX_VERTEX_TEXTURE_IMAGE_UNITS,
                        "GL_MAX_VERTEX_UNIFORM_VECTORS" to GLES20.GL_MAX_VERTEX_UNIFORM_VECTORS,
                        "GL_MAX_VIEWPORT_DIMS" to GLES20.GL_MAX_VIEWPORT_DIMS
                    ).map {
                        val intBuffer = IntArray(1)
                        GLES10.glGetIntegerv(it.second, intBuffer, 0)
                        "${it.first}: ${intBuffer[0]}"
                    }).joinToString("\n"),
                    ""
                )

                addIfNotNull(
                    "OpenGL Extensions",
                    SpannableStringBuilder().apply {
                        val extensions = GLES20.glGetString(GLES20.GL_EXTENSIONS).trim().split(" ")
                        val regex = "GL_([a-zA-Z]+)_(.+)".toRegex()
                        extensions.forEachIndexed { i, it ->
                            if (i != 0) append("\n")

                            if (!regex.matches(it)) append(it)
                            else append(SpannableString(it).apply {
                                setSpan(object : ClickableSpan() {
                                    override fun onClick(textView: View) = try {
                                        CustomTabsIntent.Builder().build().launchUrl(
                                            activity,
                                            it.replace(
                                                regex,
                                                "https://www.khronos.org/registry/OpenGL/extensions/$1/$1_$2.txt"
                                            ).toUri()
                                        )
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }, 0, it.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                            })
                        }
                    },
                    ""
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // If one wants to add kernel related cpu information
        //   try {
        //       for (File fileEntry : new File("/sys/devices/system/cpu/cpu0/cpufreq/").listFiles()) {
        //           if (fileEntry.isDirectory()) continue;
        //           try {
        //               deviceInfoItemsList.add(new DeviceInfoItem(
        //                       fileEntry.getAbsolutePath(),
        //                       Utils.readStream(new FileInputStream(fileEntry)),
        //                       null
        //               ));
        //           } catch (Exception e) {
        //               e.printStackTrace();
        //           }
        //       }
        //   } catch (Exception e) {
        //       e.printStackTrace();
        //   }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        DeviceInfoRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(position)

    override fun getItemCount(): Int = deviceInfoItemsList.size

    inner class ViewHolder(private val binding: DeviceInfoRowBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        init {
            binding.root.setOnClickListener(this)
        }

        fun bind(position: Int) {
            with(deviceInfoItemsList[position]) {
                binding.title.text = title
                binding.content.text = content
                binding.version.text = version
            }
            binding.content.movementMethod = LinkMovementMethod.getInstance()
        }

        override fun onClick(v: View?) = copyToClipboard(
            rootView,
            deviceInfoItemsList[adapterPosition].title, deviceInfoItemsList[adapterPosition].content
        )
    }
}
