package com.byagowi.persiancalendar.ui.about

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.hardware.Sensor
import android.hardware.SensorManager
import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.GLES10
import android.opengl.GLES20
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.annotation.ColorInt
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.getSystemService
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.core.net.toUri
import androidx.core.text.buildSpannedString
import androidx.core.text.inSpans
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.*
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.Variants.debugAssertNotNull
import com.byagowi.persiancalendar.databinding.DeviceInformationItemBinding
import com.byagowi.persiancalendar.databinding.FragmentDeviceInformationBinding
import com.byagowi.persiancalendar.ui.utils.MorphedPath
import com.byagowi.persiancalendar.ui.utils.copyToClipboard
import com.byagowi.persiancalendar.ui.utils.dp
import com.byagowi.persiancalendar.ui.utils.getCompatDrawable
import com.byagowi.persiancalendar.ui.utils.layoutInflater
import com.byagowi.persiancalendar.ui.utils.onClick
import com.byagowi.persiancalendar.ui.utils.resolveColor
import com.byagowi.persiancalendar.ui.utils.setupUpNavigation
import com.byagowi.persiancalendar.ui.utils.shareText
import com.byagowi.persiancalendar.ui.utils.showHtml
import com.byagowi.persiancalendar.utils.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.circularreveal.CircularRevealCompat
import com.google.android.material.circularreveal.CircularRevealWidget
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.slider.Slider
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import kotlinx.html.body
import kotlinx.html.h1
import kotlinx.html.head
import kotlinx.html.html
import kotlinx.html.meta
import kotlinx.html.script
import kotlinx.html.stream.createHTML
import kotlinx.html.style
import kotlinx.html.table
import kotlinx.html.tbody
import kotlinx.html.th
import kotlinx.html.thead
import kotlinx.html.tr
import kotlinx.html.unsafe
import java.util.*
import kotlin.math.sqrt

/**
 * @author MEHDI DIMYADI
 * MEHDIMYADI
 */
class DeviceInformationFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val binding = FragmentDeviceInformationBinding.inflate(inflater, container, false)
        binding.toolbar.let {
            it.setTitle(R.string.device_info)
            it.setupUpNavigation()
        }

        binding.root.circularRevealFromMiddle()

        binding.recyclerView.let {
            it.setHasFixedSize(true)
            it.layoutManager = LinearLayoutManager(inflater.context)
            it.addItemDecoration(
                DividerItemDecoration(inflater.context, LinearLayoutManager.VERTICAL)
            )
            val adapter = DeviceInformationAdapter(activity ?: return@let)
            it.adapter = adapter
            binding.toolbar.menu.add(R.string.share).also { menu ->
                menu.icon = binding.toolbar.context.getCompatDrawable(R.drawable.ic_baseline_share)
                menu.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            }.onClick { activity?.shareText(adapter.asHtml(), "device.html", "text/html") }
            binding.toolbar.menu.add("Print").also { menu ->
                menu.setIcon(R.drawable.ic_print)
                menu.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            }.onClick { context?.showHtml(adapter.asHtml()) }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            binding.toolbar.menu.add("Game").also {
                it.icon = binding.toolbar.context.getCompatDrawable(R.drawable.ic_esports)
                it.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            }.onClick {
                runCatching {
                    startActivity(
                        Intent(Intent.ACTION_MAIN).setClassName(
                            "com.android.systemui", "com.android.systemui.egg.MLandActivity"
                        ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                }.onFailure(logException).onFailure {
                    Snackbar.make(
                        binding.root,
                        R.string.device_calendar_does_not_support,
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
        }

        binding.bottomNavigation.menu.also {
            var clickCount = 0
            val click = { if (++clickCount % 10 == 0) openTestingHiddenDialog() }
            listOf(
                R.drawable.ic_developer to Build.VERSION.RELEASE,
                R.drawable.ic_settings to "API " + Build.VERSION.SDK_INT,
                R.drawable.ic_motorcycle to
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) Build.SUPPORTED_ABIS[0]
                        else Build.CPU_ABI,
                R.drawable.ic_device_information_white to Build.MODEL
            ).forEach { (icon, title) -> it.add(title).setIcon(icon).onClick(click) }
        }
        return binding.root
    }

    private fun openTestingHiddenDialog() {
        val activity = activity ?: return
        BottomSheetDialog(activity).also { bottomSheetDialog ->
            bottomSheetDialog.setContentView(LinearLayout(activity).also { linearLayout ->
                linearLayout.orientation = LinearLayout.VERTICAL
                linearLayout.addView(TabLayout(
                    activity, null, R.style.TabLayoutColored
                ).also { tabLayout ->
                    val tintColor = activity.resolveColor(R.attr.normalTabTextColor)
                    listOf(
                        R.drawable.ic_developer to -1,
                        R.drawable.ic_translator to 0,
                        R.drawable.ic_motorcycle to 1,
                        R.drawable.ic_help to 33,
                        R.drawable.ic_bug to 9999
                    ).map { (iconId: Int, badgeNumber: Int) ->
                        tabLayout.addTab(tabLayout.newTab().also { tab ->
                            tab.setIcon(iconId)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                tab.icon?.setTint(tintColor)
                            }
                            tab.orCreateBadge.also { badge ->
                                badge.isVisible = badgeNumber >= 0
                                if (badgeNumber > 0) badge.number = badgeNumber
                            }
                        })
                    }
                    tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                        override fun onTabUnselected(tab: TabLayout.Tab?) = Unit
                        override fun onTabReselected(tab: TabLayout.Tab?) = Unit
                        override fun onTabSelected(tab: TabLayout.Tab?) {
                            tab?.orCreateBadge?.isVisible = false
                        }
                    })
                    tabLayout.setSelectedTabIndicator(R.drawable.cat_tabs_pill_indicator)
                    tabLayout.setSelectedTabIndicatorGravity(TabLayout.INDICATOR_GRAVITY_STRETCH)
                })
                linearLayout.addView(LinearProgressIndicator(activity).also { indicator ->
                    indicator.isIndeterminate = true
                    indicator.setIndicatorColor(Color.RED, Color.YELLOW, Color.GREEN, Color.BLUE)
                    indicator.layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                })

                val morphedPathView = object : View(activity) {
                    private val pathMorph = MorphedPath(
                        "m 100 0 l -100 100 l 100 100 l 100 -100 z",
                        "m 50 50 l 0 100 l 100 0 l 0 -100 z"
                    )
                    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).also {
                        it.color = Color.BLACK
                    }

                    init {
                        val scale = 100.dp.toInt()
                        layoutParams = LinearLayout.LayoutParams(scale, scale).also {
                            it.gravity = Gravity.CENTER_HORIZONTAL
                        }
                    }

                    override fun onDraw(canvas: Canvas?) {
                        canvas?.drawPath(pathMorph.path, paint)
                    }

                    fun setFraction(value: Float) {
                        pathMorph.interpolateTo(value)
                        postInvalidate()
                    }
                }
                linearLayout.addView(morphedPathView)
                linearLayout.addView(Slider(activity).also {
                    it.addOnChangeListener { _, value, _ -> morphedPathView.setFraction(value) }
                })

                linearLayout.addView(ProgressBar(activity).also { progressBar ->
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
                                progressBar.indeterminateDrawable?.colorFilter =
                                    BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                                        it.animatedValue as Int, BlendModeCompat.SRC_ATOP
                                    )
                            }
                        }.start()
                    }
                    progressBar.layoutParams =
                        ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 600)
                    // setOnLongClickListener {
                    //     val player = MediaPlayer.create(activity, R.raw.moonlight)
                    //     runCatching {
                    //         if (!player.isPlaying) player.start()
                    //     }.onFailure(logException)
                    //     AlertDialog.Builder(activity)
                    //         .setView(AppCompatImageButton(context).also {
                    //             it.setImageResource(R.drawable.ic_stop)
                    //             it.setOnClickListener { dismiss() }
                    //         })
                    //         .setOnDismissListener {
                    //             runCatching { player.stop() }.onFailure(logException)
                    //         }
                    //         .show()
                    //     true
                    // }
                })
            })
        }.show()
    }
}

// https://stackoverflow.com/a/52557989
fun <T> T.circularRevealFromMiddle() where T : View?, T : CircularRevealWidget {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return
    post {
        val viewWidth = width
        val viewHeight = height
        val diagonal = sqrt((viewWidth * viewWidth + viewHeight * viewHeight).toDouble()).toInt()
        AnimatorSet().also {
            it.playTogether(
                CircularRevealCompat.createCircularReveal(
                    this, viewWidth / 2f, viewHeight / 2f, 10f, diagonal / 2f
                ),
                ObjectAnimator.ofArgb(
                    this,
                    CircularRevealWidget.CircularRevealScrimColorProperty.CIRCULAR_REVEAL_SCRIM_COLOR,
                    Color.GRAY, Color.TRANSPARENT
                )
            )
            it.duration = 500
        }.start()
    }
}

class CheckerBoard(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private val checkerBoard = createCheckerRoundedBoard(40f, 8f, Color.parseColor("#100A0A0A"))
    override fun onDraw(canvas: Canvas) = canvas.drawPaint(checkerBoard)
}

// https://stackoverflow.com/a/58471997
@Suppress("SameParameterValue")
private fun createCheckerRoundedBoard(
    tileSize: Float, r: Float, @ColorInt color: Int
) = Paint(Paint.ANTI_ALIAS_FLAG).also { paint ->
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return@also
    val tileSize2x = tileSize.toInt() * 2
    val bitmap = Bitmap.createBitmap(tileSize2x, tileSize2x, Bitmap.Config.ARGB_8888)
    val fill = Paint(Paint.ANTI_ALIAS_FLAG)
    fill.style = Paint.Style.FILL
    fill.color = color
    val canvas = Canvas(bitmap)
    canvas.drawRoundRect(0f, 0f, tileSize, tileSize, r, r, fill)
    canvas.drawRoundRect(tileSize, tileSize, tileSize * 2f, tileSize * 2f, r, r, fill)
    paint.shader = BitmapShader(bitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
}


// https://stackoverflow.com/a/68822715
// instead android.text.format.Formatter.formatShortFileSize() to control its locale
private fun humanReadableByteCountBin(bytes: Long): String = when {
    bytes >= 1 shl 30 -> "%.1f GB".format(Locale.ENGLISH, bytes.toDouble() / (1 shl 30))
    bytes >= 1 shl 20 -> "%.1f MB".format(Locale.ENGLISH, bytes.toDouble() / (1 shl 20))
    bytes >= 1 shl 10 -> "%.0f kB".format(Locale.ENGLISH, bytes.toDouble() / (1 shl 10))
    else -> "$bytes bytes"
}

private class DeviceInformationAdapter(private val activity: Activity) :
    ListAdapter<DeviceInformationAdapter.Item, DeviceInformationAdapter.ViewHolder>(
        object : DiffUtil.ItemCallback<Item>() {
            override fun areItemsTheSame(old: Item, new: Item) = old.title == new.title
            override fun areContentsTheSame(old: Item, new: Item) = old == new
        }
    ) {

    data class Item(val title: String, val content: CharSequence?, val version: String = "")

    private val deviceInformationItems = listOf(
        Item(
            "CPU Instructions Sets", (when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> Build.SUPPORTED_ABIS
                else -> arrayOf(Build.CPU_ABI, Build.CPU_ABI2)
            }).joinToString(", ")
        ),
        Item(
            "Android Version", Build.VERSION.CODENAME + " " + Build.VERSION.RELEASE,
            Build.VERSION.SDK_INT.toString()
        ),
        Item("Model", Build.MODEL),
        Item("Product", Build.PRODUCT),
        Item("Screen Resolution", activity.windowManager.let {
            "%d*%d pixels".format(
                Locale.ENGLISH,
                activity.resources.displayMetrics.widthPixels,
                activity.resources.displayMetrics.heightPixels
            )
        }, "%.1fHz".format(Locale.ENGLISH, activity.windowManager.defaultDisplay.refreshRate)),
        Item("DPI", activity.resources.displayMetrics.densityDpi.toString()),
        Item("Available Processors", Runtime.getRuntime().availableProcessors().toString()),
        Item("Instruction Architecture", Build.DEVICE),
        Item("Manufacturer", Build.MANUFACTURER),
        Item("Brand", Build.BRAND),
        Item("Android Id", Build.ID),
        Item("Board", Build.BOARD),
        Item("Radio Firmware Version", Build.getRadioVersion()),
        Item("Build User", Build.USER),
        Item("Host", Build.HOST),
        Item("Boot Loader", Build.BOOTLOADER),
        Item("Device", Build.DEVICE),
        Item("Tags", Build.TAGS),
        Item("Hardware", Build.HARDWARE),
        Item("Type", Build.TYPE),
        Item("Display", Build.DISPLAY),
        Item("Device Fingerprints", Build.FINGERPRINT),
        Item(
            "RAM", humanReadableByteCountBin(ActivityManager.MemoryInfo().also {
                activity.getSystemService<ActivityManager>()?.getMemoryInfo(it)
            }.totalMem)
        ),
        Item(
            "Battery", if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                (activity.getSystemService<BatteryManager>())?.let {
                    listOf("Charging: ${it.isCharging}") + listOf(
                        "Capacity" to BatteryManager.BATTERY_PROPERTY_CAPACITY,
                        "Charge Counter" to BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER,
                        "Current Avg" to BatteryManager.BATTERY_PROPERTY_CURRENT_AVERAGE,
                        "Current Now" to BatteryManager.BATTERY_PROPERTY_CURRENT_NOW,
                        "Energy Counter" to BatteryManager.BATTERY_PROPERTY_ENERGY_COUNTER
                    ).map { (title: String, id: Int) -> "$title: ${it.getLongProperty(id)}" }
                }?.joinToString("\n") else ""
        ),
        Item("Display Metrics", activity.resources.displayMetrics.toString()),
        Item(
            "Display Cutout", if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) run {
                val cutout = activity.window.decorView.rootWindowInsets.displayCutout
                    ?: return@run "None"
                listOf(
                    "Safe Inset Top" to cutout.safeInsetTop,
                    "Safe Inset Right" to cutout.safeInsetRight,
                    "Safe Inset Bottom" to cutout.safeInsetBottom,
                    "Safe Inset Left" to cutout.safeInsetLeft,
                    "Rects" to (cutout.boundingRects.joinToString(","))
                ).joinToString("\n") { (key, value) -> "$key: $value" }
            } else "None"
        ),
        Item(
            "Install Source of ${activity.packageName}", runCatching {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    activity.packageManager.getInstallSourceInfo(activity.packageName).run {
                        """
                        |Initiating Package Name: $initiatingPackageName
                        |Installing Package Name: $installingPackageName
                        |Originating Package Name: $originatingPackageName
                        |Initiating Package Signing Info: $initiatingPackageSigningInfo
                        |Installer Package Name: ${
                            activity.packageManager.getInstallerPackageName(activity.packageName)
                        }
                        """.trimMargin("|").trim()
                    }
                } else activity.packageManager.getInstallerPackageName(activity.packageName)
            }.onFailure(logException).getOrNull()
        ),
        Item(
            "Sensors", activity.getSystemService<SensorManager>()
                ?.getSensorList(Sensor.TYPE_ALL)?.joinToString("\n")
        ),
        Item(
            "System Features",
            activity.packageManager.systemAvailableFeatures.joinToString("\n")
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
                "OpenGL", (listOf(
                    "GL_VERSION" to GLES20.GL_VERSION, "GL_RENDERER" to GLES20.GL_RENDERER,
                    "GL_VENDOR" to GLES20.GL_VENDOR
                ).map { (title: String, id: Int) -> "$title: ${GLES20.glGetString(id)}" } + listOf(
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
                ).map { (title: String, id: Int) ->
                    val intBuffer = IntArray(1)
                    GLES10.glGetIntegerv(id, intBuffer, 0)
                    "$title: ${intBuffer[0]}"
                }).joinToString("\n")
            ),
            Item(
                "OpenGL Extensions", buildSpannedString {
                    val extensions = GLES20.glGetString(GLES20.GL_EXTENSIONS).trim().split(" ")
                    val regex = Regex("GL_([a-zA-Z]+)_(.+)")
                    extensions.forEachIndexed { i, it ->
                        if (i != 0) appendLine()

                        if (!regex.matches(it)) append(it)
                        else inSpans(object : ClickableSpan() {
                            override fun onClick(textView: View) = runCatching {
                                val pattern =
                                    "https://www.khronos.org/registry/OpenGL/extensions/$1/$1_$2.txt"
                                CustomTabsIntent.Builder().build().launchUrl(
                                    activity, it.replace(regex, pattern).toUri()
                                )
                            }.onFailure(logException).let {}
                        }) { append(it) }
                    }
                }
            )
        )
    }.onFailure(logException).getOrDefault(emptyList()))

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        DeviceInformationItemBinding.inflate(parent.context.layoutInflater, parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(position)

    override fun getItemCount() = deviceInformationItems.size

    fun asHtml() = createHTML().html {
        head {
            meta(charset = "utf8")
            style { unsafe { +"td { padding: .5em; border-top: 1px solid lightgray }" } }
        }
        body {
            h1 { +"Device Information" }
            table {
                thead { tr { th { +"Item" }; th { +"Value" } } }
                tbody {
                    deviceInformationItems.forEach {
                        tr {
                            th { +(it.title + if (it.version.isEmpty()) "" else " (${it.version})") }
                            th { +it.content.toString() }
                        }
                    }
                }
            }
            script { unsafe { +"print()" } }
        }
    }

    inner class ViewHolder(private val binding: DeviceInformationItemBinding) :
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

        override fun onClick(v: View?) {
            activity.copyToClipboard(deviceInformationItems[bindingAdapterPosition].content) {
                val view = activity.findViewById<View?>(android.R.id.content).debugAssertNotNull
                    ?: return@copyToClipboard
                Snackbar.make(view, it, Snackbar.LENGTH_SHORT).show()
            }
        }
    }
}
