package com.byagowi.persiancalendar.ui.about

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ComposeShader
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Point
import android.graphics.PorterDuff
import android.graphics.RadialGradient
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.SweepGradient
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.opengl.GLSurfaceView
import android.os.Build
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Scroller
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.core.graphics.PathParser
import androidx.core.graphics.createBitmap
import androidx.core.graphics.get
import androidx.core.graphics.set
import androidx.core.graphics.withMatrix
import androidx.core.graphics.withTranslation
import androidx.core.widget.doAfterTextChanged
import androidx.customview.widget.ViewDragHelper
import androidx.dynamicanimation.animation.FlingAnimation
import androidx.dynamicanimation.animation.FloatValueHolder
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.coroutineScope
import com.byagowi.persiancalendar.BuildConfig
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.ShaderSandboxBinding
import com.byagowi.persiancalendar.generated.sandboxFragmentShader
import com.byagowi.persiancalendar.ui.common.BaseSlider
import com.byagowi.persiancalendar.ui.common.ZoomableView
import com.byagowi.persiancalendar.ui.map.GLRenderer
import com.byagowi.persiancalendar.ui.utils.dp
import com.byagowi.persiancalendar.ui.utils.resolveColor
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.slider.Slider
import com.google.android.material.tabs.TabLayout
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import java.util.*
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sign
import kotlin.math.sin
import kotlin.random.Random

//
// These are somehow a sandbox to test things not used in the app yet and can be removed anytime.
//

fun showHiddenDialog(activity: FragmentActivity) {
    val root = LinearLayout(activity)
    root.orientation = LinearLayout.VERTICAL
    root.addView(
        TabLayout(activity, null, R.style.TabLayoutColored).also { tabLayout ->
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
    root.addView(LinearProgressIndicator(activity).also { indicator ->
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
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG).also { it.color = Color.BLACK }

        init {
            val scale = 100.dp.toInt()
            layoutParams = LinearLayout.LayoutParams(scale, scale).also {
                it.gravity = Gravity.CENTER_HORIZONTAL
            }
        }

        override fun onDraw(canvas: Canvas) = canvas.drawPath(pathMorph.path, paint)

        fun setFraction(value: Float) {
            pathMorph.interpolateTo(value)
            invalidate()
        }
    }
    root.addView(morphedPathView)
    root.addView(Slider(activity).also {
        it.addOnChangeListener { _, value, _ -> morphedPathView.setFraction(value) }
    })

    root.addView(ProgressBar(activity).also { progressBar ->
        progressBar.isIndeterminate = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) ValueAnimator.ofArgb(
            Color.RED, Color.YELLOW, Color.GREEN, Color.BLUE
        ).also { valueAnimator ->
            valueAnimator.duration = 3000
            valueAnimator.interpolator = LinearInterpolator()
            valueAnimator.repeatMode = ValueAnimator.REVERSE
            valueAnimator.repeatCount = 1
            valueAnimator.addUpdateListener {
                progressBar.indeterminateDrawable?.colorFilter =
                    BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                        it.animatedValue as Int, BlendModeCompat.SRC_ATOP
                    )
            }
        }.start()
        progressBar.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 600)
    })

    BottomSheetDialog(activity).also { it.setContentView(root) }.show()
}

class MorphedPath(fromPath: String, toPath: String) {
    val path = Path()

    private val nodesFrom = PathParser.createNodesFromPathData(fromPath)
    private val currentNodes = PathParser.deepCopyNodes(nodesFrom)
    private val nodesTo = PathParser.createNodesFromPathData(toPath)

    init {
        if (BuildConfig.DEVELOPMENT) check(PathParser.canMorph(nodesFrom, nodesTo))
        interpolateTo(0f)
    }

    fun interpolateTo(fraction: Float) {
        PathParser.interpolatePathDataNodes(currentNodes, nodesFrom, nodesTo, fraction)
        path.rewind()
        PathParser.PathDataNode.nodesToPath(currentNodes, path)
    }
}

fun showShaderSandboxDialog(activity: FragmentActivity) {
    val frame = object : FrameLayout(activity) {
        // Just to let AlertDialog know there is an editor here so it needs to show the soft keyboard
        override fun onCheckIsTextEditor() = true
    }
    frame.post {
        val binding = ShaderSandboxBinding.inflate(activity.layoutInflater)
        binding.glView.setEGLContextClientVersion(2)
        val renderer = GLRenderer(onError = {
            activity.runOnUiThread { Toast.makeText(activity, it, Toast.LENGTH_LONG).show() }
        })
        binding.glView.setRenderer(renderer)
        binding.glView.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
        binding.inputText.doAfterTextChanged {
            renderer.fragmentShader = binding.inputText.text?.toString() ?: ""
            binding.glView.queueEvent { renderer.compileProgram(); binding.glView.requestRender() }
        }
        binding.inputText.setText(sandboxFragmentShader)
        frame.addView(binding.root)
    }
    val dialog = MaterialAlertDialogBuilder(activity)
        .setView(frame)
        .show()
    // Just close the dialog when activity is paused so we don't get ANR after app switch and etc.
    activity.lifecycle.addObserver(LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_PAUSE) dialog.cancel()
    })
}

fun showColorPickerDialog(activity: FragmentActivity) {
    val view = LinearLayout(activity).apply {
        orientation = LinearLayout.VERTICAL
        val layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val colorCircle = CircleColorPickerView(activity).also { it.layoutParams = layoutParams }
        addView(colorCircle)
        addView(Slider(activity).also {
            it.layoutParams = layoutParams
            it.addOnChangeListener { _, value, _ -> colorCircle.setBrightness(value) }
            it.valueFrom = 0f
            it.valueTo = 100f
        })
    }
    MaterialAlertDialogBuilder(activity)
        .setView(view)
        .show()
}

class CircleColorPickerView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    private var bitmap = createBitmap(1, 1)
    private var lastX = -1f
    private var lastY = -1f
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).also { it.style = Paint.Style.FILL }
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.style = Paint.Style.STROKE
        it.color = Color.WHITE
    }
    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.style = Paint.Style.FILL
    }
    private var brightness = 0f

    fun setBrightness(value: Float) {
        brightness = value
        generateReferenceCircle()
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        super.onMeasure(
            MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY)
        )
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        generateReferenceCircle()
        lastX = lastX.takeIf { it != -1f } ?: (bitmap.width / 2f)
        lastY = lastY.takeIf { it != -1f } ?: (bitmap.height / 2f)

        strokePaint.strokeWidth = bitmap.width / 100f
        shadowPaint.shader = RadialGradient(
            0f, 0f, bitmap.height / 15f,
            Color.BLACK, Color.TRANSPARENT, Shader.TileMode.CLAMP
        )
    }

    private fun generateReferenceCircle() {
        val min = min(width, height)
        val radius = min / 2f
        if (bitmap.width != min || bitmap.height != min) bitmap = createBitmap(min, min)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val radialGradient = RadialGradient(
            radius, radius, radius * PADDING_FACTOR, Color.WHITE,
            0x00FFFFFF, Shader.TileMode.CLAMP
        )
        val saturation = (100 - brightness) / 100f
        val colors = (0 until 360 step 30)
            .map { Color.HSVToColor(floatArrayOf(it.toFloat(), saturation, 1f)) }
            .let { it + listOf(it[0]) } // Adds the first element at the end
            .toIntArray()
        val sweepGradient = SweepGradient(radius, radius, colors, null)
        paint.shader = ComposeShader(sweepGradient, radialGradient, PorterDuff.Mode.SRC_OVER)
        Canvas(bitmap).drawCircle(radius, radius, radius * PADDING_FACTOR, paint)
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawBitmap(bitmap, 0f, 0f, null)
        fillPaint.color = bitmap[lastX.toInt(), lastY.toInt()]
        canvas.withTranslation(lastX, lastY) {
            canvas.drawCircle(0f, 0f, bitmap.width / 8f, shadowPaint)
            canvas.drawCircle(0f, 0f, bitmap.width / 20f, fillPaint)
            canvas.drawCircle(0f, 0f, bitmap.width / 20f, strokePaint)
        }
    }

    var onColorPicked = fun(@ColorInt _: Int) {}

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        val r = bitmap.width / 2
        val radius = hypot(event.x - r, event.y - r).coerceAtMost(r * PADDING_FACTOR - 2f)
        val angle = atan2(event.y - r, event.x - r)
        lastX = radius * cos(angle) + r
        lastY = radius * sin(angle) + r
        onColorPicked(bitmap[lastX.toInt(), lastY.toInt()])
        invalidate()
        return true
    }

    companion object {
        private const val PADDING_FACTOR = 87f / 100
    }
}

fun showFlingDemoDialog(activity: FragmentActivity) {
    val x = FloatValueHolder()
    val horizontalFling = FlingAnimation(x)
    val y = FloatValueHolder()
    val verticalFling = FlingAnimation(y)
    val view = object : View(activity) {
        private var r = 0f
        private var previousX = 0f
        private var previousY = 0f

        private var storedVelocityX = 0f
        private var storedVelocityY = 0f

        init {
            horizontalFling.addUpdateListener { _, _, velocity ->
                storedVelocityX = velocity
                invalidate()
            }
            verticalFling.addUpdateListener { _, _, velocity ->
                storedVelocityY = velocity
                invalidate()
            }
        }

        override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
            super.onSizeChanged(w, h, oldw, oldh)
            x.value = w / 2f
            y.value = h / 2f
            r = w / 20f
            path.rewind()
            path.moveTo(x.value, y.value)
        }

        private val paint = Paint(Paint.ANTI_ALIAS_FLAG).also { it.color = Color.GRAY }
        private val linesPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
            it.color = Color.GRAY
            it.style = Paint.Style.STROKE
        }
        private val path = Path()
        override fun onDraw(canvas: Canvas) {
            path.lineTo(x.value, y.value)
            canvas.drawPath(path, linesPaint)
            canvas.drawCircle(x.value, y.value, r, paint)
            if (x.value < r) {
                x.value = r
                horizontalFling.cancel()
                horizontalFling.setStartVelocity(-storedVelocityX).start()
            }
            if (x.value > width - r) {
                x.value = width - r
                horizontalFling.cancel()
                horizontalFling.setStartVelocity(-storedVelocityX).start()
            }
            if (y.value < r) {
                y.value = r
                verticalFling.cancel()
                verticalFling.setStartVelocity(-storedVelocityY).start()
            }
            if (y.value > height - r) {
                y.value = height - r
                verticalFling.cancel()
                verticalFling.setStartVelocity(-storedVelocityY).start()
            }
        }

        private var velocityTracker: VelocityTracker? = null
        override fun dispatchTouchEvent(event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    velocityTracker = VelocityTracker.obtain()
                    horizontalFling.cancel()
                    verticalFling.cancel()
                    previousX = event.x
                    previousY = event.y
                }
                MotionEvent.ACTION_MOVE -> {
                    velocityTracker?.addMovement(event)
                    x.value += event.x - previousX
                    y.value += event.y - previousY
                    previousX = event.x
                    previousY = event.y
                    invalidate()
                }
                MotionEvent.ACTION_UP -> {
                    velocityTracker?.computeCurrentVelocity(1000)
                    horizontalFling.setStartVelocity(velocityTracker?.xVelocity ?: 0f).start()
                    verticalFling.setStartVelocity(velocityTracker?.yVelocity ?: 0f).start()
                    velocityTracker?.recycle()
                    velocityTracker = null
                }
            }
            return true
        }
    }

    MaterialAlertDialogBuilder(activity)
        .setView(view)
        .show()
}

fun showPeriodicTableDialog(activity: FragmentActivity) {
    val zoomableView = ZoomableView(activity)
    val cellSize = 100
    zoomableView.contentWidth = 100f * 18
    zoomableView.contentHeight = 100f * 9
    zoomableView.maxScale = 64f

    val rect = RectF(0f, 0f, cellSize.toFloat(), cellSize.toFloat()).also {
        it.inset(cellSize * .02f, cellSize * .02f)
    }
    val rectPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.style = Paint.Style.FILL
        it.textAlign = Paint.Align.CENTER
    }
    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.style = Paint.Style.FILL
        it.textAlign = Paint.Align.CENTER
        it.color = Color.BLACK
    }
    zoomableView.onDraw = { canvas, matrix ->
        canvas.withMatrix(matrix) {
            (0 until 18).forEach { i ->
                (0 until 9).forEach { j ->
                    withTranslation(i * cellSize.toFloat(), j * cellSize.toFloat()) {
                        val index = elementsIndices.getOrNull(i + j * 18) ?: return@withTranslation
                        val details = elements[index - 1].split(",")
                        rectPaint.color = elementsColor.getValue(index).toInt()
                        drawRect(rect, rectPaint)
                        textPaint.textSize = cellSize * .35f
                        drawText(details[0], cellSize / 2f, cellSize * .37f, textPaint)
                        drawText(index.toString(), cellSize / 2f, cellSize * .70f, textPaint)
                        textPaint.textSize = cellSize * .15f
                        drawText(details[1], cellSize / 2f, cellSize * .87f, textPaint)
                    }
                }
            }
        }
    }

    MaterialAlertDialogBuilder(activity)
        .setView(zoomableView)
        .show()
}

private val elementsColor = mapOf(
    1 to 0xffb0c0e6, 7 to 0xffb0c0e6, 8 to 0xffb0c0e6, 9 to 0xffb0c0e6, 16 to 0xffb0c0e6,
    17 to 0xffb0c0e6, 35 to 0xffb0c0e6, 53 to 0xffb0c0e6,
    2 to 0xffe0b0e6, 10 to 0xffe0b0e6, 18 to 0xffe0b0e6, 36 to 0xffe0b0e6, 54 to 0xffe0b0e6,
    86 to 0xffe0b0e6, 118 to 0xffe0b0e6,
    5 to 0xffb0e0e6, 6 to 0xffb0e0e6, 14 to 0xffb0e0e6, 15 to 0xffb0e0e6, 32 to 0xffb0e0e6,
    34 to 0xffb0e0e6, 52 to 0xffb0e0e6,
).withDefault { 0xffeee8aa }

private val elementsIndices = buildList {
    var i = 1
    add(i++)
    addAll(List(16) { null })
    add(i++)
    repeat(2) {
        addAll(List(2) { i++ })
        addAll(List(10) { null })
        addAll(List(6) { i++ })
    }
    repeat(2) { addAll(List(18) { i++ }) }
    repeat(2) {
        addAll(List(2) { i++ })
        i += 14
        addAll(List(16) { i++ })
    }
    repeat(2) {
        i = if (it == 0) 57 else 89
        addAll(List(2) { null })
        addAll(List(14) { i++ })
        addAll(List(2) { null })
    }
}

private val elements = """
H,Hydrogen
He,Helium
Li,Lithium
Be,Beryllium
B,Boron
C,Carbon
N,Nitrogen
O,Oxygen
F,Fluorine
Ne,Neon
Na,Sodium
Mg,Magnesium
Al,Aluminium
Si,Silicon
P,Phosphorus
S,Sulfur
Cl,Chlorine
Ar,Argon
K,Potassium
Ca,Calcium
Sc,Scandium
Ti,Titanium
V,Vanadium
Cr,Chromium
Mn,Manganese
Fe,Iron
Co,Cobalt
Ni,Nickel
Cu,Copper
Zn,Zinc
Ga,Gallium
Ge,Germanium
As,Arsenic
Se,Selenium
Br,Bromine
Kr,Krypton
Rb,Rubidium
Sr,Strontium
Y,Yttrium
Zr,Zirconium
Nb,Niobium
Mo,Molybdenum
Tc,Technetium
Ru,Ruthenium
Rh,Rhodium
Pd,Palladium
Ag,Silver
Cd,Cadmium
In,Indium
Sn,Tin
Sb,Antimony
Te,Tellurium
I,Iodine
Xe,Xenon
Cs,Caesium
Ba,Barium
La,Lanthanum
Ce,Cerium
Pr,Praseodymium
Nd,Neodymium
Pm,Promethium
Sm,Samarium
Eu,Europium
Gd,Gadolinium
Tb,Terbium
Dy,Dysprosium
Ho,Holmium
Er,Erbium
Tm,Thulium
Yb,Ytterbium
Lu,Lutetium
Hf,Hafnium
Ta,Tantalum
W,Tungsten
Re,Rhenium
Os,Osmium
Ir,Iridium
Pt,Platinum
Au,Gold
Hg,Mercury
Tl,Thallium
Pb,Lead
Bi,Bismuth
Po,Polonium
At,Astatine
Rn,Radon
Fr,Francium
Ra,Radium
Ac,Actinium
Th,Thorium
Pa,Protactinium
U,Uranium
Np,Neptunium
Pu,Plutonium
Am,Americium
Cm,Curium
Bk,Berkelium
Cf,Californium
Es,Einsteinium
Fm,Fermium
Md,Mendelevium
No,Nobelium
Lr,Lawrencium
Rf,Rutherfordium
Db,Dubnium
Sg,Seaborgium
Bh,Bohrium
Hs,Hassium
Mt,Meitnerium
Ds,Darmstadtium
Rg,Roentgenium
Cn,Copernicium
Nh,Nihonium
Fl,Flerovium
Mc,Moscovium
Lv,Livermorium
Ts,Tennessine
Og,Oganesson
""".trim().split("\n")

fun showRotationalSpringDemoDialog(activity: FragmentActivity) {
    val radius = FloatValueHolder()
    val radiusSpring = SpringAnimation(radius)
    radiusSpring.spring = SpringForce(0f)
        .setDampingRatio(SpringForce.DAMPING_RATIO_HIGH_BOUNCY)
        .setStiffness(SpringForce.STIFFNESS_LOW)
    val theta = FloatValueHolder()
    val rotationalSpring = SpringAnimation(theta)
    rotationalSpring.spring = SpringForce(0f)
        .setDampingRatio(SpringForce.DAMPING_RATIO_HIGH_BOUNCY)
        .setStiffness(SpringForce.STIFFNESS_LOW)
    val view = object : View(activity) {
        private var circleRadius = 0f
        private var centerX = 0f
        private var centerY = 0f

        init {
            radiusSpring.addUpdateListener { _, _, _ -> invalidate() }
            rotationalSpring.addUpdateListener { _, _, _ -> invalidate() }
        }

        override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
            super.onSizeChanged(w, h, oldw, oldh)
            radius.value = 0f
            theta.value = 0f
            circleRadius = w / 20f
            centerX = w / 2f
            centerY = h / 2f
            path.rewind()
            path.moveTo(centerX, centerY)
        }

        private val paint = Paint(Paint.ANTI_ALIAS_FLAG).also { it.color = Color.GRAY }
        private val linesPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
            it.color = Color.GRAY
            it.style = Paint.Style.STROKE
        }
        private val path = Path()
        override fun onDraw(canvas: Canvas) {
            val x = radius.value * cos(theta.value / 180) + centerX
            val y = radius.value * sin(theta.value / 180) + centerY
            path.lineTo(x, y)
            canvas.drawPath(path, linesPaint)
            canvas.drawCircle(x, y, circleRadius, paint)
        }

        override fun dispatchTouchEvent(event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    radiusSpring.cancel()
                    rotationalSpring.cancel()
                }
                MotionEvent.ACTION_MOVE -> {
                    radius.value = hypot(event.x - centerX, event.y - centerY)
                    theta.value = atan2(event.y - centerY, event.x - centerX) * 180
                    invalidate()
                }
                MotionEvent.ACTION_UP -> {
                    radiusSpring.animateToFinalPosition(0f)
                    rotationalSpring.animateToFinalPosition(0f)
                }
            }
            return true
        }
    }

    MaterialAlertDialogBuilder(activity)
        .setView(view)
        .show()
}

const val MIDDLE_A_SEMITONE = 69.0
const val MIDDLE_A_FREQUENCY = 440.0 // Hz
fun getStandardFrequency(note: Double): Double {
    return MIDDLE_A_FREQUENCY * 2.0.pow((note - MIDDLE_A_SEMITONE) / 12)
}

//fun getNote(frequency: Double): Double {
//    val note = 12 * (ln(frequency / MIDDLE_A_FREQUENCY) / ln(2.0))
//    return note.roundToInt() + MIDDLE_A_SEMITONE
//}

val ABC_NOTATION = listOf(
    "C", "C♯", "D", "D♯", "E", "F", "F♯", "G", "G♯", "A", "A♯", "B"
)
var SOLFEGE_NOTATION = listOf(
    "Do", "Do♯", "Re", "Re♯", "Mi", "Fa", "Fa♯", "Sol", "Sol♯", "La", "La♯", "Si"
)

fun getAbcNoteLabel(note: Int) = ABC_NOTATION[note % 12] + ((note / 12) - 1)
fun getSolfegeNoteLabel(note: Int) = SOLFEGE_NOTATION[note % 12] + ((note / 12) - 1)

fun showSignalGeneratorDialog(activity: FragmentActivity, viewLifecycle: Lifecycle) {
    val currentSemitone = MutableStateFlow(MIDDLE_A_SEMITONE)

    val view = object : BaseSlider(activity) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).also {
            it.color = Color.GREEN
            it.style = Paint.Style.FILL
        }

        var r = 1f
        override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
            r = min(w, h) / 2f
        }

        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            val width = MeasureSpec.getSize(widthMeasureSpec)
            super.onMeasure(
                MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY)
            )
        }

        private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
            it.color = Color.BLACK
            it.textAlign = Paint.Align.CENTER
            it.textSize = 20.dp
        }

        override fun onDraw(canvas: Canvas) {
            canvas.drawCircle(r, r, r / 1.1f, paint)
            val text = "%d Hz %s %s".format(
                Locale.ENGLISH,
                getStandardFrequency(currentSemitone.value).toInt(),
                getAbcNoteLabel(currentSemitone.value.roundToInt()),
                getSolfegeNoteLabel(currentSemitone.value.roundToInt())
            )
            canvas.drawText(text, r, r, textPaint)
        }
    }

    view.onScrollListener = { dx, _ ->
        currentSemitone.value = (currentSemitone.value - dx / 1000.0)
            .coerceIn(15.0, 135.0) // Clamps it in terms of semitones
    }

    val sampleRate = 44100
    val buffer = ShortArray(sampleRate * 10)
    var previousAudioTrack: AudioTrack? = null

    currentSemitone
        .map { semitone ->
            val frequency = getStandardFrequency(semitone)
            buffer.indices.forEach {
                val phase = 2 * Math.PI * it / (sampleRate / frequency)
                buffer[it] = (when (0) {
                    0 -> sin(phase) // Sine
                    1 -> sign(sin(phase)) // Square
                    2 -> abs(asin(sin(phase / 2))) // Sawtooth
                    else -> .0
                } * Short.MAX_VALUE).toInt().toShort()
            }

            val audioTrack = AudioTrack(
                AudioManager.STREAM_MUSIC, sampleRate,
                AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
                buffer.size, AudioTrack.MODE_STATIC
            )
            audioTrack.write(buffer, 0, buffer.size)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                audioTrack.setLoopPoints(0, audioTrack.bufferSizeInFrames, -1)
            }
            audioTrack.play()
            if (previousAudioTrack?.state == AudioTrack.STATE_INITIALIZED) {
                previousAudioTrack?.stop()
                previousAudioTrack?.release()
            }
            previousAudioTrack = audioTrack
        }
        .flowOn(Dispatchers.Unconfined)
        .launchIn(viewLifecycle.coroutineScope)

    val dialog = MaterialAlertDialogBuilder(activity)
        .setView(view)
        .setOnCancelListener { previousAudioTrack?.stop() }
        .show()

    activity.lifecycle.addObserver(LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_PAUSE) dialog.cancel()
    })
}

fun showSpringDemoDialog(activity: FragmentActivity) {
    val x = FloatValueHolder()
    val horizontalSpring = SpringAnimation(x)
    horizontalSpring.spring = SpringForce(0f)
        .setDampingRatio(SpringForce.DAMPING_RATIO_HIGH_BOUNCY)
        .setStiffness(SpringForce.STIFFNESS_LOW)
    val y = FloatValueHolder()
    val verticalSpring = SpringAnimation(y)
    verticalSpring.spring = SpringForce(0f)
        .setDampingRatio(SpringForce.DAMPING_RATIO_HIGH_BOUNCY)
        .setStiffness(SpringForce.STIFFNESS_LOW)
    val view = object : View(activity) {
        private var r = 0f
        private var previousX = 0f
        private var previousY = 0f

        init {
            horizontalSpring.addUpdateListener { _, _, _ -> invalidate() }
            verticalSpring.addUpdateListener { _, _, _ -> invalidate() }
        }

        override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
            super.onSizeChanged(w, h, oldw, oldh)
            x.value = w / 2f
            y.value = h / 2f
            r = w / 20f
            path.rewind()
            path.moveTo(x.value, y.value)
        }

        private val paint = Paint(Paint.ANTI_ALIAS_FLAG).also { it.color = Color.GRAY }
        private val linesPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
            it.color = Color.GRAY
            it.style = Paint.Style.STROKE
        }
        private val path = Path()
        override fun onDraw(canvas: Canvas) {
            path.lineTo(x.value, y.value)
            canvas.drawPath(path, linesPaint)
            canvas.drawCircle(x.value, y.value, r, paint)
        }

        override fun dispatchTouchEvent(event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    horizontalSpring.cancel()
                    verticalSpring.cancel()
                    previousX = event.x
                    previousY = event.y
                }
                MotionEvent.ACTION_MOVE -> {
                    x.value += event.x - previousX
                    y.value += event.y - previousY
                    previousX = event.x
                    previousY = event.y
                    invalidate()
                }
                MotionEvent.ACTION_UP -> {
                    horizontalSpring.animateToFinalPosition(width / 2f)
                    verticalSpring.animateToFinalPosition(height / 2f)

                    val angle = atan2(y.value - width / 2f, x.value - height / 2f)
                    createSoundTick(angle * 10.0).play()
                }
            }
            return true
        }
    }

    MaterialAlertDialogBuilder(activity)
        .setView(view)
        .show()
}

fun showViewDragHelperDemoDialog(activity: FragmentActivity) {
    // This id based on https://gist.github.com/pskink/b747e89c1e1a1e314ca6 but relatively changed
    val view = object : ViewGroup(activity) {
        private val bounds = List(9) { Rect() }
        override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {}
        override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
            super.onSizeChanged(w, h, oldw, oldh)
            val w3 = w / 3
            val h3 = h / 3
            bounds.forEachIndexed { i, r ->
                r.set(0, 0, w3, h3)
                r.offset(w3 * (i % 3), h3 * (i / 3))
                getChildAt(i).layout(r.left, r.top, r.right, r.bottom)
            }
        }

        private val callback = object : ViewDragHelper.Callback() {
            override fun tryCaptureView(view: View, i: Int): Boolean = true
            override fun onViewPositionChanged(
                changedView: View, left: Int, top: Int, dx: Int, dy: Int
            ) = invalidate()

            override fun getViewHorizontalDragRange(child: View): Int = width
            override fun getViewVerticalDragRange(child: View): Int = height
            override fun onViewCaptured(capturedChild: View, activePointerId: Int) =
                bringChildToFront(capturedChild)

            override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
                val pos = computeFinalPosition(releasedChild, xvel, yvel)
                dragHelper.settleCapturedViewAt(pos.x, pos.y)
                invalidate()
            }

            private fun computeFinalPosition(child: View, xvel: Float, yvel: Float): Point {
                val r = Rect()
                child.getHitRect(r)
                var cx = r.centerX()
                var cy = r.centerY()
                if (xvel != 0f || yvel != 0f) {
                    val s =
                        Scroller(context) // Creating a view just to use its computation doesn't look cool
                    val w2: Int = r.width() / 2
                    val h2: Int = r.height() / 2
                    s.fling(cx, cy, xvel.toInt(), yvel.toInt(), w2, width - w2, h2, height - h2)
                    cx = s.finalX
                    cy = s.finalY
                }
                bounds.forEach { if (it.contains(cx, cy)) return Point(it.left, it.top) }
                return Point()
            }

            override fun clampViewPositionHorizontal(child: View, left: Int, dx: Int): Int =
                left.coerceIn(0, width - child.width)

            override fun clampViewPositionVertical(child: View, top: Int, dy: Int): Int =
                top.coerceIn(0, height - child.height)
        }
        private val dragHelper: ViewDragHelper = ViewDragHelper.create(this, callback)

        override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
            val action = event.action
            if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
                dragHelper.cancel()
                return false
            }
            return dragHelper.shouldInterceptTouchEvent(event)
        }

        @SuppressLint("ClickableViewAccessibility")
        override fun onTouchEvent(event: MotionEvent): Boolean {
            dragHelper.processTouchEvent(event)
            return true
        }

        override fun computeScroll() {
            if (dragHelper.continueSettling(true)) invalidate()
        }

        init {
            (0 until 360 step 40)
                .map { Color.HSVToColor(floatArrayOf(it.toFloat(), 100f, 1f)) }
                .shuffled()
                .mapIndexed { i, color ->
                    TextView(context).also {
                        it.textSize = 32f
                        it.textAlignment = View.TEXT_ALIGNMENT_CENTER
                        it.setBackgroundColor(color)
                        var clickedCount = i
                        it.text = clickedCount.toString()
                        it.setOnClickListener { _ -> it.text = (++clickedCount).toString() }
                    }
                }.forEach(::addView)
        }
    }
    MaterialAlertDialogBuilder(activity)
        .setView(view)
        .show()
}

private fun textToQrCodeBitmap(text: String): Bitmap {
    val size = 768
    val bitmap = createBitmap(size, size)
    val bitMatrix = QRCodeWriter().encode(
        text, BarcodeFormat.QR_CODE, size, size, mapOf(
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M,
            EncodeHintType.MARGIN to 0
        )
    )
    (0 until bitMatrix.height).forEach { y ->
        (0 until bitMatrix.width).forEach { x ->
            bitmap[x, y] = if (bitMatrix[x, y]) Color.BLACK else Color.TRANSPARENT
        }
    }
    return bitmap
}

fun showQrCode(activity: FragmentActivity, text: String) {
    MaterialAlertDialogBuilder(activity)
        .setView(ImageView(activity).also { it.setImageBitmap(textToQrCodeBitmap(text)) })
        .show()
}

// Based on https://habr.com/ru/post/514844/ and https://timiskhakov.github.io/posts/programming-guitar-music
private fun guitarString(
    frequency: Double,
    duration: Double = 1.0,
    sampleRate: Int = 44100,
    p: Double = .9,
    beta: Double = .1,
    s: Double = .1,
    c: Double = .1,
    l: Double = .1
): ShortArray {
    val n = (sampleRate / frequency).roundToInt()

    // Pick-direction lowpass filter
    val random = (0 until n).runningFold(Random.nextDouble() * 2 - 1) { lastOut, _ ->
        (1 - p) * (Random.nextDouble() * 2 - 1) + p * lastOut
    }

    // Pick-position comb filter
    val pick = (beta * n + 1 / 2).roundToInt().let { if (it == 0) n else it }
    val noise = DoubleArray(random.size) { random[it] - if (it < pick) .0 else random[it - pick] }

    val samples = DoubleArray((sampleRate * duration).roundToInt())
    (0 until n).forEach { samples[it] = noise[it] }

    fun delayLine(i: Int) = samples.getOrNull(i - n) ?: .0

    // String-dampling filter.
    fun stringDamplingFilter(i: Int) = 0.996 * ((1 - s) * delayLine(i) + s * delayLine(i - 1))

    // First-order string-tuning allpass filter
    (n until samples.size).forEach {
        samples[it] =
            c * (stringDamplingFilter(it) - samples[it - 1]) + stringDamplingFilter(it - 1)
    }

    // Dynamic-level lowpass filter. L ∈ (0, 1/3)
    val wTilde = PI * frequency / sampleRate
    val buffer = DoubleArray(samples.size)
    buffer[0] = wTilde / (1 + wTilde) * samples[0]
    (1 until samples.size).forEach {
        buffer[it] =
            wTilde / (1 + wTilde) * (samples[it] + samples[it - 1]) + (1 - wTilde) / (1 + wTilde) * buffer[it - 1]
    }
    samples.indices
        .forEach { samples[it] = ((l.pow(4 / 3.0)) * samples[it]) + (1 - l) * buffer[it] }

    val max = samples.maxOf { it.absoluteValue }
    return samples.map { (it / max * Short.MAX_VALUE).roundToInt().toShort() }.toShortArray()
}

fun createSoundTick(offset: Double): AudioTrack {
    val sampleRate = 44100
    val buffer = guitarString(
        getStandardFrequency(offset + MIDDLE_A_SEMITONE),
        sampleRate = sampleRate,
        duration = 4.0
    )
    val audioTrack = AudioTrack(
        AudioManager.STREAM_MUSIC, sampleRate, AudioFormat.CHANNEL_OUT_MONO,
        AudioFormat.ENCODING_PCM_16BIT, buffer.size, AudioTrack.MODE_STATIC
    )
    audioTrack.write(buffer, 0, buffer.size)
    return audioTrack
}
