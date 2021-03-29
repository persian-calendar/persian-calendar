package com.byagowi.persiancalendar.ui.about

import android.animation.ValueAnimator
import android.app.Activity
import android.app.ActivityManager
import android.graphics.Color
import android.graphics.PorterDuff
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
import android.view.animation.LinearInterpolator
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.*
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.DeviceInformationRowBinding
import com.byagowi.persiancalendar.databinding.FragmentDeviceInfoBinding
import com.byagowi.persiancalendar.ui.MainActivity
import com.byagowi.persiancalendar.utils.circularRevealFromMiddle
import com.byagowi.persiancalendar.utils.copyToClipboard
import com.byagowi.persiancalendar.utils.layoutInflater
import com.byagowi.persiancalendar.utils.logException
import com.google.android.material.bottomnavigation.LabelVisibilityMode
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.progressindicator.LinearProgressIndicator
import java.util.*

/**
 * @author MEHDI DIMYADI
 * MEHDIMYADI
 */
class DeviceInformationFragment : Fragment() {

    private var clickCount = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ) = FragmentDeviceInfoBinding.inflate(inflater, container, false)
        .also { fragmentDeviceInfoBinding ->
            val mainActivity = activity as MainActivity

            mainActivity.setTitleAndSubtitle(getString(R.string.device_info), "")

            circularRevealFromMiddle(fragmentDeviceInfoBinding.circularReveal)

            fragmentDeviceInfoBinding.recyclerView.also {
                it.setHasFixedSize(true)
                it.layoutManager = LinearLayoutManager(mainActivity)
                it.addItemDecoration(
                    DividerItemDecoration(
                        mainActivity,
                        LinearLayoutManager.VERTICAL
                    )
                )
                it.adapter = DeviceInformationAdapter(mainActivity, fragmentDeviceInfoBinding.root)
            }

            fragmentDeviceInfoBinding.bottomNavigation.also { bottomNavigationView ->
                bottomNavigationView.menu.also {
                    it.add(Build.VERSION.RELEASE)
                    it.getItem(0).setIcon(R.drawable.ic_developer).isEnabled = true

                    it.add("API " + Build.VERSION.SDK_INT)
                    it.getItem(1).setIcon(R.drawable.ic_settings).isEnabled = false

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        it.add(Build.SUPPORTED_ABIS[0])
                    } else {
                        it.add(Build.CPU_ABI)
                    }
                    it.getItem(2).setIcon(R.drawable.ic_motorcycle).isEnabled = false

                    it.add(Build.MODEL)
                    it.getItem(3).setIcon(R.drawable.ic_device_information_white).isEnabled = false
                }
                bottomNavigationView.labelVisibilityMode =
                    LabelVisibilityMode.LABEL_VISIBILITY_LABELED
                bottomNavigationView.setOnNavigationItemSelectedListener {
                    // Easter egg
                    when {
                        ++clickCount % 10 == 0 -> {
                            BottomSheetDialog(mainActivity).also { bottomSheetDialog ->
                                bottomSheetDialog.setContentView(LinearLayout(mainActivity).also { linearLayout ->
                                    linearLayout.orientation = LinearLayout.VERTICAL
                                    // Add one with CircularProgressIndicator also
                                    linearLayout.addView(LinearProgressIndicator(mainActivity).also { linearProgressIndicator ->
                                        linearProgressIndicator.isIndeterminate = true
                                        linearProgressIndicator.setIndicatorColor(
                                            Color.RED,
                                            Color.YELLOW,
                                            Color.GREEN,
                                            Color.BLUE
                                        )
                                        linearProgressIndicator.layoutParams =
                                            ViewGroup.LayoutParams(
                                                ViewGroup.LayoutParams.MATCH_PARENT,
                                                ViewGroup.LayoutParams.WRAP_CONTENT
                                            )
                                    })
                                    linearLayout.addView(ProgressBar(mainActivity).also { progressBar ->
                                        progressBar.isIndeterminate = true
                                        when {
                                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> ValueAnimator.ofArgb(
                                                Color.RED, Color.YELLOW, Color.GREEN, Color.BLUE
                                            ).also { valueAnimator ->
                                                valueAnimator.duration = 3000
                                                valueAnimator.interpolator = LinearInterpolator()
                                                valueAnimator.repeatMode = ValueAnimator.REVERSE
                                                valueAnimator.repeatCount = ValueAnimator.INFINITE
                                                valueAnimator.addUpdateListener {
                                                    progressBar.indeterminateDrawable?.setColorFilter(
                                                        it.animatedValue as Int,
                                                        PorterDuff.Mode.SRC_ATOP
                                                    )
                                                }
                                            }.start()
                                        }
                                        progressBar.layoutParams =
                                            ViewGroup.LayoutParams(
                                                ViewGroup.LayoutParams.MATCH_PARENT,
                                                700
                                            )
                                        // setOnLongClickListener {
                                        //     val player = MediaPlayer.create(mainActivity, R.raw.moonlight)
                                        //     runCatching {
                                        //         if (!player.isPlaying) player.start()
                                        //     }.onFailure(logException)
                                        //     AlertDialog.Builder(mainActivity).create().apply {
                                        //         setView(AppCompatImageButton(context).apply {
                                        //             setImageResource(R.drawable.ic_stop)
                                        //             setOnClickListener { dismiss() }
                                        //         })
                                        //         setOnDismissListener {
                                        //             runCatching { player.stop() }.onFailure(logException)
                                        //         }
                                        //         show()
                                        //     }
                                        //     true
                                        // }
                                    })
                                })
                            }.show()
                        }
                    }
                    true
                }
            }
        }.root
}

class DeviceInformationAdapter(activity: Activity, private val rootView: View) :
    ListAdapter<DeviceInformationAdapter.Item, DeviceInformationAdapter.ViewHolder>(
        object : DiffUtil.ItemCallback<Item>() {
            override fun areItemsTheSame(old: Item, new: Item) = old.title == new.title
            override fun areContentsTheSame(old: Item, new: Item) = old == new
        }
    ) {

    data class Item(val title: String, val content: CharSequence?, val version: String)

    // https://stackoverflow.com/a/59234917
    // instead android.text.format.Formatter.formatShortFileSize() to control its locale
    private fun humanReadableByteCountBin(bytes: Long): String {
        return when {
            bytes == Long.MIN_VALUE || bytes < 0 -> "N/A"
            bytes < 1024L -> "$bytes B"
            bytes <= 0xfffccccccccccccL shr 40 -> "%.1f KiB".format(
                Locale.ENGLISH,
                bytes.toDouble() / (0x1 shl 10)
            )
            bytes <= 0xfffccccccccccccL shr 30 -> "%.1f MiB".format(
                Locale.ENGLISH,
                bytes.toDouble() / (0x1 shl 20)
            )
            bytes <= 0xfffccccccccccccL shr 20 -> "%.1f GiB".format(
                Locale.ENGLISH,
                bytes.toDouble() / (0x1 shl 30)
            )
            bytes <= 0xfffccccccccccccL shr 10 -> "%.1f TiB".format(
                Locale.ENGLISH,
                bytes.toDouble() / (0x1 shl 40)
            )
            bytes <= 0xfffccccccccccccL -> "%.1f PiB".format(
                Locale.ENGLISH,
                (bytes shr 10).toDouble() / (0x1 shl 40)
            )
            else -> "%.1f EiB".format(Locale.ENGLISH, (bytes shr 20).toDouble() / (0x1 shl 40))
        }
    }

    val deviceInformationItems = listOf(
        Item("Screen Resolution", activity.windowManager.run {
            "%d*%d pixels".format(Locale.ENGLISH, defaultDisplay.width, defaultDisplay.height)
        }, "%.1fHz".format(Locale.ENGLISH, activity.windowManager.defaultDisplay.refreshRate)),
        Item("DPI", activity.resources.displayMetrics.densityDpi.toString(), ""),
        Item(
            "Android Version", Build.VERSION.CODENAME + " " + Build.VERSION.RELEASE,
            Build.VERSION.SDK_INT.toString()
        ),
        Item(
            "CPU Instructions Sets",
            (when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> Build.SUPPORTED_ABIS
                else -> arrayOf(Build.CPU_ABI, Build.CPU_ABI2)
            }).joinToString(", "),
            ""
        ),
        Item("Available Processors", Runtime.getRuntime().availableProcessors().toString(), ""),
        Item("Instruction Architecture", Build.DEVICE, ""),
        Item("Manufacturer", Build.MANUFACTURER, ""),
        Item("Brand", Build.BRAND, ""),
        Item("Model", Build.MODEL, ""),
        Item("Product", Build.PRODUCT, ""),
        Item("Android Id", Build.ID, ""),
        Item("Board", Build.BOARD, ""),
        Item("Radio Firmware Version", Build.getRadioVersion(), ""),
        Item("Build User", Build.USER, ""),
        Item("Host", Build.HOST, ""),
        Item("Boot Loader", Build.BOOTLOADER, ""),
        Item("Device", Build.DEVICE, ""),
        Item("Tags", Build.TAGS, ""),
        Item("Hardware", Build.HARDWARE, ""),
        Item("Type", Build.TYPE, ""),
        Item("Display", Build.DISPLAY, ""),
        Item("Device Fingerprints", Build.FINGERPRINT, ""),
        Item(
            "RAM",
            humanReadableByteCountBin(ActivityManager.MemoryInfo().also {
                activity.getSystemService<ActivityManager>()?.getMemoryInfo(it)
            }.totalMem),
            ""
        ),
        Item(
            "Battery",
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                (activity.getSystemService<BatteryManager>())?.run {
                    listOf("Charging: $isCharging") + listOf(
                        "Capacity" to BatteryManager.BATTERY_PROPERTY_CAPACITY,
                        "Charge Counter" to BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER,
                        "Current Avg" to BatteryManager.BATTERY_PROPERTY_CURRENT_AVERAGE,
                        "Current Now" to BatteryManager.BATTERY_PROPERTY_CURRENT_NOW,
                        "Energy Counter" to BatteryManager.BATTERY_PROPERTY_ENERGY_COUNTER
                    ).map { "${it.first}: ${getLongProperty(it.second)}" }
                }?.joinToString("\n")
            else "",
            ""
        ),
        Item("Display Metrics", activity.resources.displayMetrics.toString(), ""),
        Item(
            "Sensors",
            (activity.getSystemService<SensorManager>())
                ?.getSensorList(Sensor.TYPE_ALL)?.joinToString("\n"), ""
        ),
        Item(
            "System Features",
            activity.packageManager.systemAvailableFeatures.joinToString("\n"), ""
        )
    ) + (runCatching {
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
        listOf(
            Item(
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
            ),
            Item(
                "OpenGL Extensions",
                SpannableStringBuilder().also { spannableStringBuilder ->
                    val extensions = GLES20.glGetString(GLES20.GL_EXTENSIONS).trim().split(" ")
                    val regex = "GL_([a-zA-Z]+)_(.+)".toRegex()
                    extensions.forEachIndexed { i, it ->
                        if (i != 0) spannableStringBuilder.append("\n")

                        if (!regex.matches(it)) spannableStringBuilder.append(it)
                        else spannableStringBuilder.append(SpannableString(it).also { spannableString ->
                            spannableString.setSpan(object : ClickableSpan() {
                                override fun onClick(textView: View) = runCatching {
                                    CustomTabsIntent.Builder().build().launchUrl(
                                        activity,
                                        it.replace(
                                            regex,
                                            "https://www.khronos.org/registry/OpenGL/extensions/$1/$1_$2.txt"
                                        ).toUri()
                                    )
                                }.getOrElse(logException)
                            }, 0, it.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                        })
                    }
                },
                ""
            )
        )
    }.onFailure(logException).getOrDefault(emptyList()))

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        DeviceInformationRowBinding.inflate(parent.context.layoutInflater, parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(position)

    override fun getItemCount() = deviceInformationItems.size

    inner class ViewHolder(private val binding: DeviceInformationRowBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        init {
            binding.root.setOnClickListener(this)
        }

        fun bind(position: Int) {
            deviceInformationItems[position].also {
                binding.title.text = it.title
                binding.content.text = it.content ?: "Unknown"
                binding.version.text = it.version
            }
            binding.content.movementMethod = LinkMovementMethod.getInstance()
        }

        override fun onClick(v: View?) = copyToClipboard(
            rootView,
            deviceInformationItems[adapterPosition].title,
            deviceInformationItems[adapterPosition].content
        )
    }
}
