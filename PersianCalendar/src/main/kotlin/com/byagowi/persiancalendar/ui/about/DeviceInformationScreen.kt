package com.byagowi.persiancalendar.ui.about

import android.app.Activity
import android.app.ActivityManager
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorManager
import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.GLES10
import android.opengl.GLES20
import android.os.BatteryManager
import android.os.Build
import android.view.InputDevice
import android.view.RoundedCorner
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Motorcycle
import androidx.compose.material.icons.filled.PermDeviceInformation
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarDefaults.exitUntilCollapsedScrollBehavior
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.content.getSystemService
import androidx.fragment.app.FragmentActivity
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ui.utils.MaterialCornerExtraLargeTop
import com.byagowi.persiancalendar.ui.utils.openHtmlInBrowser
import com.byagowi.persiancalendar.ui.utils.resolveColor
import com.byagowi.persiancalendar.ui.utils.shareTextFile
import com.byagowi.persiancalendar.utils.logException
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
import java.util.Locale

@Preview
@Composable
private fun DeviceInformationScreenPreview() = DeviceInformationScreen {}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun DeviceInformationScreen(popNavigation: () -> Unit) {
    val scrollBehavior = exitUntilCollapsedScrollBehavior()
    Column(modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)) {
        val context = LocalContext.current
        val items = remember {
            // TODO: Ugly cast
            createItemsList(context as? Activity ?: return@remember emptyList())
        }
        // TODO: Ideally this should be onPrimary
        val colorOnAppBar = Color(context.resolveColor(R.attr.colorOnAppBar))
        LargeTopAppBar(
            scrollBehavior = scrollBehavior,
            title = { Text(stringResource(R.string.device_information)) },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                navigationIconContentColor = colorOnAppBar,
                actionIconContentColor = colorOnAppBar,
                titleContentColor = colorOnAppBar,
            ),
            navigationIcon = {
                IconButton(onClick = popNavigation) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = stringResource(R.string.navigate_up)
                    )
                }
            },
            actions = {
                TooltipBox(
                    positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                    tooltip = {
                        PlainTooltip { Text(text = stringResource(R.string.share)) }
                    },
                    state = rememberTooltipState()
                ) {
                    IconButton(onClick = {
                        context.shareTextFile(generateHtmlReport(items), "device.html", "text/html")
                    }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = stringResource(R.string.share)
                        )
                    }
                }
                TooltipBox(
                    positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                    tooltip = {
                        PlainTooltip { Text(text = "Print") }
                    },
                    state = rememberTooltipState()
                ) {
                    IconButton(onClick = { context.openHtmlInBrowser(generateHtmlReport(items)) }) {
                        Icon(
                            imageVector = Icons.Default.Print, contentDescription = "Print"
                        )
                    }
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    TooltipBox(
                        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                        tooltip = {
                            PlainTooltip { Text(text = "Game") }
                        },
                        state = rememberTooltipState()
                    ) {
                        IconButton(onClick = {
                            runCatching {
                                context.startActivity(
                                    Intent(Intent.ACTION_MAIN).setClassName(
                                        "com.android.systemui",
                                        "com.android.systemui.egg.MLandActivity"
                                    ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                )
                            }.onFailure(logException).onFailure {
                                Toast.makeText(
                                    context, R.string.device_does_not_support, Toast.LENGTH_SHORT
                                ).show()
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.SportsEsports,
                                contentDescription = "Game"
                            )
                        }
                    }
                }
            },
        )
        Surface(shape = MaterialCornerExtraLargeTop()) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                LazyColumn {
                    item { Spacer(Modifier.height(16.dp)) }
                    item { OverviewTopBar(Modifier.padding(horizontal = 16.dp)) }
                    itemsIndexed(items) { i, item ->
                        if (i > 0) Divider(
                            Modifier.padding(horizontal = 20.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = .5f),
                        )
                        Column(Modifier.padding(vertical = 4.dp, horizontal = 24.dp)) {
                            Text(item.title, fontWeight = FontWeight.Bold)
                            Row {
                                SelectionContainer { Text(item.content.toString()) }
                                Spacer(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth()
                                )
                                Text(item.version)
                            }
                        }
                    }
                    item { Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.systemBars)) }
                }
            }
        }
    }
}

@Composable
private fun OverviewTopBar(modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
        val keyItems = remember {
            listOf(
                Triple(
                    Icons.Default.Android, Build.VERSION.RELEASE, ::showHiddenUiDialog
                ),
                Triple(
                    Icons.Default.Settings, "API " + Build.VERSION.SDK_INT, ::showSensorTestDialog
                ),
                Triple(
                    Icons.Default.Motorcycle, Build.SUPPORTED_ABIS[0], ::showInputDeviceTestDialog
                ),
                Triple(
                    Icons.Default.PermDeviceInformation, Build.MODEL, ::showColorPickerDialog
                ),
            )
        }
        var selectedIndex by rememberSaveable { mutableIntStateOf(0) }
        keyItems.forEachIndexed { i, (icon, title, action) ->
            val context = LocalContext.current
            val clickHandler = remember { createEasterEggClickHandler(action) }
            NavigationRailItem(
                selected = i == selectedIndex,
                onClick = {
                    selectedIndex = i
                    // TODO: Ugly cast
                    clickHandler(context as? FragmentActivity)
                },
                label = { Text(title) },
                icon = {
                    Icon(
                        modifier = Modifier.padding(start = 8.dp, end = 4.dp),
                        imageVector = icon,
                        contentDescription = stringResource(R.string.help)
                    )
                },
            )
        }
    }
}

// https://stackoverflow.com/a/52557989
//fun <T> T.circularRevealFromMiddle() where T : View?, T : CircularRevealWidget {
//    post {
//        val viewWidth = width
//        val viewHeight = height
//        val diagonal = hypot(viewWidth.toDouble(), viewHeight.toDouble()).toInt()
//        AnimatorSet().also {
//            it.playTogether(
//                CircularRevealCompat.createCircularReveal(
//                    this, viewWidth / 2f, viewHeight / 2f, 10f, diagonal / 2f
//                ),
//                ObjectAnimator.ofArgb(
//                    this,
//                    CircularRevealWidget.CircularRevealScrimColorProperty.CIRCULAR_REVEAL_SCRIM_COLOR,
//                    Color.GRAY, Color.TRANSPARENT
//                )
//            )
//            it.duration = resources.getInteger(android.R.integer.config_longAnimTime).toLong()
//        }.start()
//    }
//}

// @Language("AGSL")
// private const val demoRuntimeShader = """
// // This is a SkSl/AGSL flavor shader only usable in Android 13, see also:
// // * https://shaders.skia.org/?id=de2a4d7d893a7251eb33129ddf9d76ea517901cec960db116a1bbd7832757c1f
// // * https://developer.android.com/about/versions/13/features#graphics
// // * https://cs.android.com/android/platform/superproject/+/master:external/skia/src/sksl/SkSLMain.cpp;l=275
//
// uniform float iTime;
// uniform vec2 iResolution;
//
// // Source: @notargs https://twitter.com/notargs/status/1250468645030858753
// half4 main(vec2 fragCoord) {
//     vec3 d = .5 - fragCoord.xy1 / iResolution.y, p = vec3(0), o;
//     for (int i = 0; i < 32; ++i) {
//         o = p;
//         o.z -= iTime * 9.;
//         float a = o.z * .1;
//         o.xy *= mat2(cos(a), sin(a), -sin(a), cos(a));
//         p += (.1 - length(cos(o.xy) + sin(o.yz))) * d;
//     }
//     return ((sin(p) + vec3(2, 5, 12)) / length(p)).xyz1;
// }
// """

//class CheckerBoard(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
//    private val checkerBoard = createCheckerRoundedBoard(40f, 8f, Color.parseColor("#100A0A0A"))
//    // private val startTime = System.nanoTime()
//    // private val shader by lazy(LazyThreadSafetyMode.NONE) {
//    //     runCatching {
//    //         if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return@runCatching null
//    //         RuntimeShader(demoRuntimeShader).also {
//    //             val width = context.resources?.displayMetrics?.widthPixels?.toFloat() ?: 800f
//    //             val height = context.resources?.displayMetrics?.heightPixels?.toFloat() ?: 800f
//    //             it.setFloatUniform("iResolution", width, height)
//    //         }
//    //     }.onFailure(logException).getOrNull().debugAssertNotNull
//    // }
//    // private val shaderPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
//    //     if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//    //         it.shader = shader
//    //     }
//    // }
//
//    override fun onDraw(canvas: Canvas) {
//        // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//        //     shader?.setFloatUniform("iTime", (System.nanoTime() - startTime) / 1e9f)
//        //     canvas.drawPaint(shaderPaint)
//        //     invalidate()
//        // } else
//        canvas.drawPaint(checkerBoard)
//    }
//}

// https://stackoverflow.com/a/58471997
//@Suppress("SameParameterValue")
//private fun createCheckerRoundedBoard(
//    tileSize: Float, r: Float, @ColorInt color: Int
//) = Paint(Paint.ANTI_ALIAS_FLAG).also { paint ->
//    val tileSize2x = tileSize.toInt() * 2
//    val fill = Paint(Paint.ANTI_ALIAS_FLAG).also {
//        it.style = Paint.Style.FILL
//        it.color = color
//    }
//    val bitmap = createBitmap(tileSize2x, tileSize2x).applyCanvas {
//        drawRoundRect(0f, 0f, tileSize, tileSize, r, r, fill)
//        drawRoundRect(tileSize, tileSize, tileSize * 2f, tileSize * 2f, r, r, fill)
//    }
//    paint.shader = BitmapShader(bitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
//}

// https://stackoverflow.com/a/68822715
// instead android.text.format.Formatter.formatShortFileSize() to control its locale
private fun humanReadableByteCountBin(bytes: Long): String = when {
    bytes >= 1 shl 30 -> "%.1f GB".format(Locale.ENGLISH, bytes.toDouble() / (1 shl 30))
    bytes >= 1 shl 20 -> "%.1f MB".format(Locale.ENGLISH, bytes.toDouble() / (1 shl 20))
    bytes >= 1 shl 10 -> "%.0f kB".format(Locale.ENGLISH, bytes.toDouble() / (1 shl 10))
    else -> "$bytes bytes"
}

private data class Item(val title: String, val content: CharSequence?, val version: String = "")

private fun createItemsList(activity: Activity) = listOf(
    Item("CPU Instructions Sets", Build.SUPPORTED_ABIS.joinToString(", ")),
    Item(
        "Android Version", Build.VERSION.CODENAME + " " + Build.VERSION.RELEASE,
        Build.VERSION.SDK_INT.toString()
    ),
    Item("Model", Build.MODEL),
    Item("Product", Build.PRODUCT),
    Item(
        "Screen Resolution", activity.windowManager?.let {
            "%d*%d pixels".format(
                Locale.ENGLISH,
                activity.resources?.displayMetrics?.widthPixels ?: 0,
                activity.resources?.displayMetrics?.heightPixels ?: 0
            )
        }, "%.1fHz".format(
            Locale.ENGLISH, when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> activity.display?.refreshRate
                else ->
                    @Suppress("DEPRECATION")
                    activity.windowManager?.defaultDisplay?.refreshRate
            } ?: ""
        )
    ),
    Item("DPI", activity.resources?.displayMetrics?.densityDpi?.toString()),
    Item("Available Processors", Runtime.getRuntime()?.availableProcessors()?.toString()),
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
            activity.getSystemService<BatteryManager>()?.let {
                listOf("Charging: ${it.isCharging}") + listOf(
                    "Capacity" to BatteryManager.BATTERY_PROPERTY_CAPACITY,
                    "Charge Counter" to BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER,
                    "Current Avg" to BatteryManager.BATTERY_PROPERTY_CURRENT_AVERAGE,
                    "Current Now" to BatteryManager.BATTERY_PROPERTY_CURRENT_NOW,
                    "Energy Counter" to BatteryManager.BATTERY_PROPERTY_ENERGY_COUNTER
                ).map { (title: String, id: Int) -> "$title: ${it.getLongProperty(id)}" }
            }?.joinToString("\n") else ""
    ),
    Item("App Standby Bucket", appStandbyStatus(activity)),
    Item("Display Metrics", activity.resources?.displayMetrics?.toString() ?: ""),
    Item(
        "Display Cutout", if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) run {
            val cutout = activity.window?.decorView?.rootWindowInsets?.displayCutout
                ?: return@run "None"
            listOf(
                "Safe Inset Top" to cutout.safeInsetTop,
                "Safe Inset Right" to cutout.safeInsetRight,
                "Safe Inset Bottom" to cutout.safeInsetBottom,
                "Safe Inset Left" to cutout.safeInsetLeft,
                "Rects" to cutout.boundingRects.joinToString(",")
            ).joinToString("\n") { (key, value) -> "$key: $value" }
        } else "None"
    ),
    Item(
        "Display Rounded Corners", if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) run {
            val insets = activity.window?.decorView?.rootWindowInsets ?: return@run "None"
            listOf(
                "Top Left Corner" to RoundedCorner.POSITION_TOP_LEFT,
                "Top Right Corner" to RoundedCorner.POSITION_TOP_RIGHT,
                "Bottom Right Corner" to RoundedCorner.POSITION_BOTTOM_RIGHT,
                "Bottom Left Corner" to RoundedCorner.POSITION_BOTTOM_LEFT,
            ).joinToString("\n") { (title, id) ->
                val corner = insets.getRoundedCorner(id) ?: return@joinToString "$title: null"
                "$title: radius=${corner.radius} center=${corner.center}"
            }
        } else "None"
    ),
    Item(
        "Install Source of ${activity.packageName}", runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                activity.packageManager?.getInstallSourceInfo(activity.packageName)?.run {
                    """
                    |Initiating Package Name: $initiatingPackageName
                    |Installing Package Name: $installingPackageName
                    |Originating Package Name: $originatingPackageName
                    |Initiating Package Signing Info: $initiatingPackageSigningInfo
                    |Installer Package Name: ${
                        @Suppress("DEPRECATION")
                        activity.packageManager?.getInstallerPackageName(activity.packageName) ?: ""
                    }
                    """.trimMargin("|").trim()
                }
            } else
                @Suppress("DEPRECATION")
                activity.packageManager?.getInstallerPackageName(activity.packageName) ?: ""
        }.onFailure(logException).getOrNull()
    ),
    Item(
        "Sensors", activity.getSystemService<SensorManager>()
            ?.getSensorList(Sensor.TYPE_ALL)?.joinToString("\n")
    ),
    Item("Input Device", InputDevice.getDeviceIds().map(InputDevice::getDevice).joinToString()),
    Item(
        "System Features",
        activity.packageManager?.systemAvailableFeatures?.joinToString("\n")
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
    val configs = arrayOfNulls<EGLConfig?>(1)
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
            "OpenGL Extensions", buildAnnotatedString {
                val extensions = GLES20.glGetString(GLES20.GL_EXTENSIONS).trim().split(" ")
                val regex = Regex("GL_([a-zA-Z]+)_(.+)")
                extensions.forEachIndexed { i, it ->
                    if (i != 0) appendLine()

                    if (!regex.matches(it)) append(it)
                    else append(it) // TODO: Make this clickable
                    // runCatching {
                    //     val pattern =
                    //         "https://www.khronos.org/registry/OpenGL/extensions/$1/$1_$2.txt"
                    //     CustomTabsIntent.Builder().build().launchUrl(
                    //         activity, it.replace(regex, pattern).toUri()
                    //     )
                    // }.onFailure(logException)
                }
            }
        )
    )
}.onFailure(logException).getOrDefault(emptyList()))

private fun generateHtmlReport(items: List<Item>) = createHTML().html {
    head {
        meta(charset = "utf8")
        style { unsafe { +"td { padding: .5em; border-top: 1px solid lightgray }" } }
    }
    body {
        h1 { +"Device Information" }
        table {
            thead { tr { th { +"Item" }; th { +"Value" } } }
            tbody {
                items.forEach {
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
