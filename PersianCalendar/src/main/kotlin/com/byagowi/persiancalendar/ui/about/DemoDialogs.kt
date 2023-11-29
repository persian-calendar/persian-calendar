package com.byagowi.persiancalendar.ui.about

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
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
import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.graphics.Shader
import android.graphics.SweepGradient
import android.graphics.drawable.RippleDrawable
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.opengl.GLSurfaceView
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.text.Spanned
import android.text.style.TextAppearanceSpan
import android.util.AttributeSet
import android.util.TypedValue
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Scroller
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatSpinner
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.getSystemService
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.core.graphics.applyCanvas
import androidx.core.graphics.component1
import androidx.core.graphics.component2
import androidx.core.graphics.createBitmap
import androidx.core.graphics.get
import androidx.core.graphics.withMatrix
import androidx.core.graphics.withTranslation
import androidx.core.os.postDelayed
import androidx.core.text.HtmlCompat
import androidx.core.text.buildSpannedString
import androidx.core.text.inSpans
import androidx.core.text.parseAsHtml
import androidx.core.view.isVisible
import androidx.core.view.setMargins
import androidx.core.view.setPadding
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
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.ShaderSandboxBinding
import com.byagowi.persiancalendar.generated.sandboxFragmentShader
import com.byagowi.persiancalendar.ui.SeasonsAdapter
import com.byagowi.persiancalendar.ui.common.BaseSlider
import com.byagowi.persiancalendar.ui.common.ZoomableView
import com.byagowi.persiancalendar.ui.map.GLRenderer
import com.byagowi.persiancalendar.ui.utils.createFlingDetector
import com.byagowi.persiancalendar.ui.utils.dp
import com.byagowi.persiancalendar.ui.utils.performHapticFeedbackVirtualKey
import com.byagowi.persiancalendar.ui.utils.resolveResourceIdFromTheme
import com.byagowi.persiancalendar.ui.utils.sp
import com.byagowi.persiancalendar.utils.TWO_SECONDS_IN_MILLIS
import com.byagowi.persiancalendar.utils.createStatusIcon
import com.byagowi.persiancalendar.utils.getDayIconResource
import com.byagowi.persiancalendar.utils.logException
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.carousel.CarouselLayoutManager
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.android.material.shape.TriangleEdgeTreatment
import com.google.android.material.slider.Slider
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.intellij.lang.annotations.Language
import java.util.Locale
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sign
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

//
// These are somehow a sandbox to test things not used in the app yet and can be removed anytime.
//

fun createEasterEggClickHandler(callback: (FragmentActivity) -> Unit): (FragmentActivity?) -> Unit {
    var clickCount = 0
    return { activity: FragmentActivity? ->
        if (activity != null) runCatching {
            when (++clickCount % 10) {
                0 -> callback(activity)
                9 -> Toast.makeText(activity, "One more to go!", Toast.LENGTH_SHORT).show()
            }
        }.onFailure(logException)
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
fun createIconRandomEffects(): () -> RenderEffect? {
    var clickCount = 0
    val colorShader by lazy(LazyThreadSafetyMode.NONE) { RuntimeShader(COLOR_SHIFT_EFFECT) }
    return {
        if (clickCount++ % 2 == 0) {
            colorShader.setFloatUniform("colorShift", Random.nextFloat())
            RenderEffect.createRuntimeShaderEffect(colorShader, "content")
        } else {
            val r = Random.nextFloat() * 30
            RenderEffect.createBlurEffect(r, r, Shader.TileMode.CLAMP)
        }
    }
}

@Language("AGSL")
private const val COLOR_SHIFT_EFFECT = """
uniform shader content;

uniform float colorShift;

// https://gist.github.com/983/e170a24ae8eba2cd174f
half3 rgb2hsv(half3 c) {
    half4 K = half4(0.0, -1.0 / 3.0, 2.0 / 3.0, -1.0);
    half4 p = mix(half4(c.bg, K.wz), half4(c.gb, K.xy), step(c.b, c.g));
    half4 q = mix(half4(p.xyw, c.r), half4(c.r, p.yzx), step(p.x, c.r));

    float d = q.x - min(q.w, q.y);
    float e = 1.0e-10;
    return half3(abs(q.z + (q.w - q.y) / (6.0 * d + e)), d / (q.x + e), q.x);
}

half3 hsv2rgb(half3 c) {
    half4 K = half4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    half3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

half4 main(float2 fragCoord) {
    half4 color = content.eval(fragCoord);
    half3 hsv = rgb2hsv(color.rgb);
    hsv.x = mod(hsv.x + colorShift, 1);
    return half4(hsv2rgb(hsv), color.a);
}
"""

fun showHiddenUiDialog(activity: FragmentActivity) {
    val root = LinearLayout(activity)
    root.orientation = LinearLayout.VERTICAL
//    root.addView(
//        TabLayout(activity).also { tabLayout ->
//            listOf(
//                R.drawable.ic_developer to -1,
//                R.drawable.ic_translator to 0,
//                R.drawable.ic_motorcycle to 1,
//                R.drawable.ic_help to 33,
//                R.drawable.ic_bug to 9999
//            ).map { (iconId: Int, badgeNumber: Int) ->
//                tabLayout.addTab(tabLayout.newTab().also { tab ->
//                    tab.setIcon(iconId)
//                    tab.orCreateBadge.also { badge ->
//                        badge.isVisible = badgeNumber >= 0
//                        if (badgeNumber > 0) badge.number = badgeNumber
//                    }
//                })
//            }
//            tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
//                override fun onTabUnselected(tab: TabLayout.Tab?) = Unit
//                override fun onTabReselected(tab: TabLayout.Tab?) = Unit
//                override fun onTabSelected(tab: TabLayout.Tab?) {
//                    tab?.orCreateBadge?.isVisible = false
//                }
//            })
//            tabLayout.setSelectedTabIndicator(R.drawable.cat_tabs_pill_indicator)
//            tabLayout.setSelectedTabIndicatorGravity(TabLayout.INDICATOR_GRAVITY_STRETCH)
//        })
    root.addView(LinearProgressIndicator(activity).also { indicator ->
        indicator.isIndeterminate = true
        indicator.setIndicatorColor(Color.RED, Color.YELLOW, Color.GREEN, Color.BLUE)
        indicator.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
    })

//    val morphedPathView = object : View(activity) {
//        private val pathMorph = MorphedPath(
//            "m 100 0 l -100 100 l 100 100 l 100 -100 z",
//            "m 50 50 l 0 100 l 100 0 l 0 -100 z"
//        )
//        private val paint = Paint(Paint.ANTI_ALIAS_FLAG).also { it.color = Color.BLACK }
//
//        init {
//            val scale = (100 * resources.dp).toInt()
//            layoutParams = LinearLayout.LayoutParams(scale, scale).also {
//                it.gravity = Gravity.CENTER_HORIZONTAL
//            }
//        }
//
//        override fun onDraw(canvas: Canvas) = canvas.drawPath(pathMorph.path, paint)
//
//        fun setFraction(value: Float) {
//            pathMorph.interpolateTo(value)
//            invalidate()
//        }
//    }
//    root.addView(morphedPathView)
//    root.addView(Slider(activity).also {
//        it.addOnChangeListener { _, value, _ -> morphedPathView.setFraction(value) }
//    })

    root.addView(ProgressBar(activity).also { progressBar ->
        progressBar.isIndeterminate = true
        ValueAnimator.ofArgb(
            Color.RED, Color.YELLOW, Color.GREEN, Color.BLUE
        ).also { valueAnimator ->
            valueAnimator.duration = 3000
            valueAnimator.interpolator = LinearInterpolator()
            valueAnimator.repeatMode = ValueAnimator.REVERSE
            valueAnimator.repeatCount = 1
            valueAnimator.addUpdateListener {
                progressBar.indeterminateDrawable?.colorFilter =
                    BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                        it.animatedValue as? Int ?: 0, BlendModeCompat.SRC_ATOP
                    )
            }
        }.start()
        progressBar.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 600)
    })

    BottomSheetDialog(activity).also { it.setContentView(root) }.show()
}

//class MorphedPath(fromPath: String, toPath: String) {
//    val path = Path()
//
//    private val nodesFrom = PathParser.createNodesFromPathData(fromPath)
//    private val currentNodes = PathParser.deepCopyNodes(nodesFrom)
//    private val nodesTo = PathParser.createNodesFromPathData(toPath)
//
//    init {
//        if (BuildConfig.DEVELOPMENT) check(PathParser.canMorph(nodesFrom, nodesTo))
//        interpolateTo(0f)
//    }
//
//    fun interpolateTo(fraction: Float) {
//        PathParser.interpolatePathDataNodes(currentNodes, nodesFrom, nodesTo, fraction)
//        path.rewind()
//        PathParser.PathDataNode.nodesToPath(currentNodes, path)
//    }
//}

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
    val dialog = androidx.appcompat.app.AlertDialog.Builder(activity)
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
    androidx.appcompat.app.AlertDialog.Builder(activity)
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

        if (lastX == -1f) lastX = bitmap.width / 2f
        if (lastY == -1f) lastY = bitmap.height / 2f

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
        val colors = (0..<360 step 30)
            .map { Color.HSVToColor(floatArrayOf(it.toFloat(), saturation, 1f)) }
            .let { it + it[0] } // Adds the first element at the end
            .toIntArray()
        val sweepGradient = SweepGradient(radius, radius, colors, null)
        paint.shader = ComposeShader(sweepGradient, radialGradient, PorterDuff.Mode.SRC_OVER)
        bitmap.applyCanvas { drawCircle(radius, radius, radius * PADDING_FACTOR, paint) }
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
            setBackgroundResource(
                activity.resolveResourceIdFromTheme(android.R.attr.selectableItemBackground)
            )
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

        private var shader: RuntimeShader? = null
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG).also {
            it.color = Color.GRAY
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                it.color = context.getColor(android.R.color.system_accent1_500)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                shader = RuntimeShader(shaderSource).also { shader -> it.shader = shader }
            }
        }
        private val linesPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
            it.color = Color.GRAY
            it.style = Paint.Style.STROKE
        }
        private val path = Path()
        override fun onDraw(canvas: Canvas) {
            path.lineTo(x.value, y.value)
            canvas.drawPath(path, linesPaint)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                shader?.also {
                    it.setFloatUniform("center", x.value, y.value)
                    it.setFloatUniform("bounds", width.toFloat(), height.toFloat())
                    it.setFloatUniform("radius", r)
                    it.setColorUniform("color", paint.color)
                    it.setIntUniform("mode", counter % 3)
                }
                canvas.drawPaint(paint)
            } else {
                canvas.drawCircle(x.value, y.value, r, paint)
            }
            var isWallHit = false
            if (x.value < r) {
                x.value = r
                horizontalFling.cancel()
                horizontalFling.setStartVelocity(-storedVelocityX).start()
                isWallHit = true
            }
            if (x.value > width - r) {
                x.value = width - r
                horizontalFling.cancel()
                horizontalFling.setStartVelocity(-storedVelocityX).start()
                isWallHit = true
            }
            if (y.value < r) {
                y.value = r
                verticalFling.cancel()
                verticalFling.setStartVelocity(-storedVelocityY).start()
                isWallHit = true
            }
            if (y.value > height - r) {
                y.value = height - r
                verticalFling.cancel()
                verticalFling.setStartVelocity(-storedVelocityY).start()
                isWallHit = true
            }
            if (isWallHit) {
                performHapticFeedbackVirtualKey()
                val index = ++counter % diatonicScale.size
                lifecycle.launch { playSoundTick(diatonicScale[index].toDouble()) }

                val rippleDrawable = background
                if (rippleDrawable is RippleDrawable) {
                    isPressed = false
                    rippleDrawable.setColor(ColorStateList.valueOf(getRandomTransparentColor()))
                    rippleDrawable.setHotspot(x.value, y.value)
                    isPressed = true
                }
            }
        }

        private var counter = 0

        private val diatonicScale = listOf(0, 2, 4, 5, 7, 9, 11, 12, 11, 9, 7, 5, 4, 2)

        private val lifecycle = activity.lifecycleScope

        private val flingDetector = createFlingDetector(context) { velocityX, velocityY ->
            horizontalFling.setStartVelocity(velocityX).start()
            verticalFling.setStartVelocity(velocityY).start()
            true
        }

        override fun dispatchTouchEvent(event: MotionEvent): Boolean {
            flingDetector.onTouchEvent(event)
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    horizontalFling.cancel()
                    verticalFling.cancel()
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
            }
            return true
        }
    }

    androidx.appcompat.app.AlertDialog.Builder(activity)
        .setView(view)
        .show()
}

@Language("AGSL")
val shaderSource = """
uniform float2 center;
uniform float2 bounds;
uniform float radius;
uniform int mode;
layout(color) uniform vec4 color;

float smin(float a, float b, float k) { // https://www.mayerowitz.io/blog/a-journey-into-shaders
    float h = max(k - abs(a - b), 0) / k;
    return min(a, b) - h * h * k / 4;
}

float sdBox(vec2 p, vec2 b) { // https://iquilezles.org/articles/distfunctions2d/
    vec2 d = abs(p) - b;
    return length(max(d, 0)) + min(max(d.x, d.y), 0);
}

float4 main(float2 fragCoord) {
    float d1 = (distance(fragCoord, center) - radius) / min(bounds.x, bounds.y);
    float d2;
    if (mode == 0) d2 = (distance(bounds - fragCoord, center) - radius) / min(bounds.x, bounds.y);
    else if (mode == 1) d2 = -sdBox(fragCoord * 2 * .99 - bounds * .99, bounds) / min(bounds.x, bounds.y);
    else d2 = 1 - (-sdBox(fragCoord * 2 * .99 - bounds * .99, bounds) / min(bounds.x, bounds.y));
    // return vec4(vec3(d2), 1.0);
    float d = smoothstep(0., 0.01, smin(d1, d2, 1 / 3. + 0.001));
    return d < 1 ? color : vec4(0);
}
"""

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
            (0..<18).forEach { i ->
                (0..<9).forEach { j ->
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

    fun formatTitle(input: String): Spanned {
        return "<small><small>$input</small></small>"
            .replace(Regex("([a-zA-Z])(\\d+)"), "$1<sup><small>$2</small></sup>")
            .parseAsHtml(HtmlCompat.FROM_HTML_MODE_LEGACY)
    }

    val dialog = androidx.appcompat.app.AlertDialog.Builder(activity)
        .setTitle(
            formatTitle(
                "1s2 | 2s2 2p6 | 3s2 3p6 | 3d10 4s2 4p6 | 4d10 5s2 5p6 | 4f14 5d10 6s2 6p6 | 5f14 6d10 7s2 7p6"
            )
        )
        .setView(zoomableView)
        .show()

    zoomableView.onClick = { x, y ->
        val index = floor(x / cellSize).toInt() + floor(y / cellSize).toInt() * 18
        elementsIndices.getOrNull(index)?.let { atomicNumber ->
            val info = elements.getOrNull(atomicNumber - 1)?.split(",") ?: return@let
            dialog.setTitle(formatTitle("$atomicNumber ${info[0]} ${info[1]}<br>${info[2]}"))
        }
        if (index == 161) {
            androidx.appcompat.app.AlertDialog.Builder(activity)
                .setView(EditText(activity).also {
                    it.layoutDirection = View.LAYOUT_DIRECTION_LTR
                    it.textDirection = View.TEXT_DIRECTION_LTR
                    it.setText(elements.reversed()
                        .mapIndexed { index, s -> "${elements.size - index},$s" }
                        .joinToString("\n"))
                })
                .show()
        }
    }
}

private val elementsColor = buildMap {
    listOf(3, 11, 19, 37, 55, 87).forEach { put(it, 0xffff9d9d) } // Alkali metals
    listOf(4, 12, 20, 38, 56, 88).forEach { put(it, 0xffffdead) } // Alkaline earth metals
    (57..71).forEach { put(it, 0xffffbfff) } // Lanthanides
    (89..103).forEach { put(it, 0xffff99cc) } // Actinides
    listOf(1, 6, 7, 8, 15, 16, 34).forEach { put(it, 0xffa0ffa0) } // Other nonmetals
    listOf(5, 14, 32, 33, 51, 52).forEach { put(it, 0xffcccc99) } // Metalloids
    // Other nonmetals
    listOf(13, 31, 49, 50, 81, 82, 83, 84, 113, 114, 115, 116).forEach { put(it, 0xffcccccc) }
    listOf(9, 17, 35, 53, 85, 117).forEach { put(it, 0xffffff99) } // Halogens
    listOf(2, 10, 18, 36, 54, 86, 118).forEach { put(it, 0xffc0ffff) } // Noble gases
}.withDefault { 0xffffc0c0 } // Transition metals

private val elementsIndices = buildList<Int?> {
    var i = 1
    add(i++)
    addAll(arrayOfNulls(16))
    add(i++)
    repeat(2) {
        addAll(List(2) { i++ })
        addAll(arrayOfNulls(10))
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
        addAll(arrayOfNulls(2))
        addAll(List(14) { i++ })
        addAll(arrayOfNulls(2))
    }
}

// Based on https://en.wikipedia.org/wiki/Template:Infobox_element/symbol-to-electron-configuration
// Algorithmic atomic configuration won't be perfect, see also https://github.com/xanecs/aufbau-principle
private val elements = """
H,Hydrogen,1s1
He,Helium,1s2
Li,Lithium,[He] 2s1
Be,Beryllium,[He] 2s2
B,Boron,[He] 2s2 2p1
C,Carbon,[He] 2s2 2p2
N,Nitrogen,[He] 2s2 2p3
O,Oxygen,[He] 2s2 2p4
F,Fluorine,[He] 2s2 2p5
Ne,Neon,[He] 2s2 2p6
Na,Sodium,[Ne] 3s1
Mg,Magnesium,[Ne] 3s2
Al,Aluminium,[Ne] 3s2 3p1
Si,Silicon,[Ne] 3s2 3p2
P,Phosphorus,[Ne] 3s2 3p3
S,Sulfur,[Ne] 3s2 3p4
Cl,Chlorine,[Ne] 3s2 3p5
Ar,Argon,[Ne] 3s2 3p6
K,Potassium,[Ar] 4s1
Ca,Calcium,[Ar] 4s2
Sc,Scandium,[Ar] 3d1 4s2
Ti,Titanium,[Ar] 3d2 4s2
V,Vanadium,[Ar] 3d3 4s2
Cr,Chromium,[Ar] 3d5 4s1
Mn,Manganese,[Ar] 3d5 4s2
Fe,Iron,[Ar] 3d6 4s2
Co,Cobalt,[Ar] 3d7 4s2
Ni,Nickel,[Ar] 3d8 4s2 or [Ar] 3d9 4s1
Cu,Copper,[Ar] 3d10 4s1
Zn,Zinc,[Ar] 3d10 4s2
Ga,Gallium,[Ar] 3d10 4s2 4p1
Ge,Germanium,[Ar] 3d10 4s2 4p2
As,Arsenic,[Ar] 3d10 4s2 4p3
Se,Selenium,[Ar] 3d10 4s2 4p4
Br,Bromine,[Ar] 3d10 4s2 4p5
Kr,Krypton,[Ar] 3d10 4s2 4p6
Rb,Rubidium,[Kr] 5s1
Sr,Strontium,[Kr] 5s2
Y,Yttrium,[Kr] 4d1 5s2
Zr,Zirconium,[Kr] 4d2 5s2
Nb,Niobium,[Kr] 4d4 5s1
Mo,Molybdenum,[Kr] 4d5 5s1
Tc,Technetium,[Kr] 4d5 5s2
Ru,Ruthenium,[Kr] 4d7 5s1
Rh,Rhodium,[Kr] 4d8 5s1
Pd,Palladium,[Kr] 4d10
Ag,Silver,[Kr] 4d10 5s1
Cd,Cadmium,[Kr] 4d10 5s2
In,Indium,[Kr] 4d10 5s2 5p1
Sn,Tin,[Kr] 4d10 5s2 5p2
Sb,Antimony,[Kr] 4d10 5s2 5p3
Te,Tellurium,[Kr] 4d10 5s2 5p4
I,Iodine,[Kr] 4d10 5s2 5p5
Xe,Xenon,[Kr] 4d10 5s2 5p6
Cs,Caesium,[Xe] 6s1
Ba,Barium,[Xe] 6s2
La,Lanthanum,[Xe] 5d1 6s2
Ce,Cerium,[Xe] 4f1 5d1 6s2
Pr,Praseodymium,[Xe] 4f3 6s2
Nd,Neodymium,[Xe] 4f4 6s2
Pm,Promethium,[Xe] 4f5 6s2
Sm,Samarium,[Xe] 4f6 6s2
Eu,Europium,[Xe] 4f7 6s2
Gd,Gadolinium,[Xe] 4f7 5d1 6s2
Tb,Terbium,[Xe] 4f9 6s2
Dy,Dysprosium,[Xe] 4f10 6s2
Ho,Holmium,[Xe] 4f11 6s2
Er,Erbium,[Xe] 4f12 6s2
Tm,Thulium,[Xe] 4f13 6s2
Yb,Ytterbium,[Xe] 4f14 6s2
Lu,Lutetium,[Xe] 4f14 5d1 6s2
Hf,Hafnium,[Xe] 4f14 5d2 6s2
Ta,Tantalum,[Xe] 4f14 5d3 6s2
W,Tungsten,[Xe] 4f14 5d4 6s2
Re,Rhenium,[Xe] 4f14 5d5 6s2
Os,Osmium,[Xe] 4f14 5d6 6s2
Ir,Iridium,[Xe] 4f14 5d7 6s2
Pt,Platinum,[Xe] 4f14 5d9 6s1
Au,Gold,[Xe] 4f14 5d10 6s1
Hg,Mercury,[Xe] 4f14 5d10 6s2
Tl,Thallium,[Xe] 4f14 5d10 6s2 6p1 (to check)
Pb,Lead,[Xe] 4f14 5d10 6s2 6p2
Bi,Bismuth,[Xe] 4f14 5d10 6s2 6p3
Po,Polonium,[Xe] 4f14 5d10 6s2 6p4
At,Astatine,[Xe] 4f14 5d10 6s2 6p5
Rn,Radon,[Xe] 4f14 5d10 6s2 6p6
Fr,Francium,[Rn] 7s1
Ra,Radium,[Rn] 7s2
Ac,Actinium,[Rn] 6d1 7s2
Th,Thorium,[Rn] 6d2 7s2
Pa,Protactinium,[Rn] 5f2 6d1 7s2
U,Uranium,[Rn] 5f3 6d1 7s2
Np,Neptunium,[Rn] 5f4 6d1 7s2
Pu,Plutonium,[Rn] 5f6 7s2
Am,Americium,[Rn] 5f7 7s2
Cm,Curium,[Rn] 5f7 6d1 7s2
Bk,Berkelium,[Rn] 5f9 7s2
Cf,Californium,[Rn] 5f10 7s2
Es,Einsteinium,[Rn] 5f11 7s2
Fm,Fermium,[Rn] 5f12 7s2
Md,Mendelevium,[Rn] 5f13 7s2
No,Nobelium,[Rn] 5f14 7s2
Lr,Lawrencium,[Rn] 5f14 7s2 7p1 (modern calculations all favour the 7p1)
Rf,Rutherfordium,[Rn] 5f14 6d2 7s2
Db,Dubnium,[Rn] 5f14 6d3 7s2
Sg,Seaborgium,[Rn] 5f14 6d4 7s2
Bh,Bohrium,[Rn] 5f14 6d5 7s2
Hs,Hassium,[Rn] 5f14 6d6 7s2
Mt,Meitnerium,[Rn] 5f14 6d7 7s2
Ds,Darmstadtium,[Rn] 5f14 6d8 7s2
Rg,Roentgenium,[Rn] 5f14 6d9 7s2
Cn,Copernicium,[Rn] 5f14 6d10 7s2
Nh,Nihonium,[Rn] 5f14 6d10 7s2 7p1
Fl,Flerovium,[Rn] 5f14 6d10 7s2 7p2
Mc,Moscovium,[Rn] 5f14 6d10 7s2 7p3
Lv,Livermorium,[Rn] 5f14 6d10 7s2 7p4
Ts,Tennessine,[Rn] 5f14 6d10 7s2 7p5
Og,Oganesson,[Rn] 5f14 6d10 7s2 7p6
Uue,Ununennium,[Og] 8s1 (predicted)
Ubn,Unbinilium,[Og] 8s2 (predicted)
Ubu,Unbiunium,[Og] 8s2 8p1 (predicted)
Ubb,Unbibium,[Og] 7d1 8s2 8p1
Ubt,Unbitrium,
Ubq,Unbiquadium,[Og] 6f3 8s2 8p1
Ubc,Unbipentium,
Ubh,Unbihexium,[Og] 5g2 6f3 8s2 8p1
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

    androidx.appcompat.app.AlertDialog.Builder(activity)
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
            it.textSize = 20 * resources.dp
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
                val phase = 2 * PI * it / (sampleRate / frequency)
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

    val dialog = androidx.appcompat.app.AlertDialog.Builder(activity)
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

                    val angle = atan2(y.value - height / 2f, x.value - width / 2f)
                    performHapticFeedbackVirtualKey()
                    lifecycle.launch { playSoundTick(angle * 10.0) }
                }
            }
            return true
        }

        private val lifecycle = activity.lifecycleScope
    }

    val dialog = androidx.appcompat.app.AlertDialog.Builder(activity)
        .setView(view)
        .show()

    view.setBackgroundResource(
        activity.resolveResourceIdFromTheme(android.R.attr.selectableItemBackground)
    )
    val rippleDrawable = view.background
    if (rippleDrawable is RippleDrawable) {
        val handler = Handler(Looper.getMainLooper())
        fun next() {
            val delay = Random.nextLong(10, TWO_SECONDS_IN_MILLIS)
            handler.postDelayed(delay) {
                if (!dialog.isShowing) return@postDelayed
                view.isPressed = false
                rippleDrawable.setColor(ColorStateList.valueOf(getRandomTransparentColor()))
                rippleDrawable.setHotspot(
                    view.width * Random.nextFloat(),
                    view.height * Random.nextFloat()
                )
                view.isPressed = true
                next()
            }
        }
        next()
    }
}

private fun getRandomTransparentColor(): Int {
    return Color.argb(0x10, Random.nextInt(256), Random.nextInt(256), Random.nextInt(256))
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
                val (x, y) = computeFinalPosition(releasedChild, xvel, yvel)
                dragHelper.settleCapturedViewAt(x, y)
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
            (0..<360 step 40)
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
    androidx.appcompat.app.AlertDialog.Builder(activity)
        .setView(view)
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
    val random = (0..<n).runningFold(Random.nextDouble() * 2 - 1) { lastOut, _ ->
        (1 - p) * (Random.nextDouble() * 2 - 1) + p * lastOut
    }

    // Pick-position comb filter
    val pick = (beta * n + 1 / 2).roundToInt().let { if (it == 0) n else it }
    val noise = DoubleArray(random.size) { random[it] - if (it < pick) .0 else random[it - pick] }

    val samples = DoubleArray((sampleRate * duration).roundToInt())
    (0..<n).forEach { samples[it] = noise[it] }

    fun delayLine(i: Int) = samples.getOrNull(i - n) ?: .0

    // String-dampling filter.
    fun stringDamplingFilter(i: Int) = 0.996 * ((1 - s) * delayLine(i) + s * delayLine(i - 1))

    // First-order string-tuning allpass filter
    (n..<samples.size).forEach {
        samples[it] =
            c * (stringDamplingFilter(it) - samples[it - 1]) + stringDamplingFilter(it - 1)
    }

    // Dynamic-level lowpass filter. L ∈ (0, 1/3)
    val wTilde = PI * frequency / sampleRate
    val buffer = DoubleArray(samples.size)
    buffer[0] = wTilde / (1 + wTilde) * samples[0]
    (1..<samples.size).forEach {
        buffer[it] =
            wTilde / (1 + wTilde) * (samples[it] + samples[it - 1]) + (1 - wTilde) / (1 + wTilde) * buffer[it - 1]
    }
    samples.indices
        .forEach { samples[it] = ((l.pow(4 / 3.0)) * samples[it]) + (1 - l) * buffer[it] }

    val max = samples.maxOf(::abs)
    return samples.map { (it / max * Short.MAX_VALUE).roundToInt().toShort() }.toShortArray()
}

suspend fun playSoundTick(offset: Double) {
    withContext(Dispatchers.IO) {
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
        runCatching { audioTrack.play() }.onFailure(logException)
    }
}

fun showSensorTestDialog(activity: FragmentActivity) {
    val sensorManager = activity.getSystemService<SensorManager>() ?: return
    val root = LinearLayout(activity)
    val spinner = AppCompatSpinner(activity)
    root.orientation = LinearLayout.VERTICAL
    root.addView(spinner)
    var width = 1
    val emptyFloat = floatArrayOf()
    val log = MutableList(width) { emptyFloat }
    var counter = 0
    fun initiateLog() {
        counter = 0
        log.clear()
        log.addAll(List(width) { emptyFloat })
    }

    val textView = object : AppCompatTextView(activity) {
        override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
            super.onSizeChanged(w, h, oldw, oldh)
            width = w
            initiateLog()
        }

        private val paths = List(4) { Path() } // just a hack to make different colors possible
        private val paintSink = Paint(Paint.ANTI_ALIAS_FLAG).also {
            it.strokeWidth = 1 * resources.dp
            it.style = Paint.Style.STROKE
            it.color = Color.GRAY
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val h2 = height / 2
            val max = log.maxOf { it.maxOfOrNull(::abs) ?: 0f }.coerceAtLeast(1f)
            log[0].indices.forEach { n ->
                val path = paths[n.coerceAtMost(paths.size - 1)]
                path.rewind()
                log.forEachIndexed { x, it ->
                    val y = (if (it.size > n) it[n] else return@forEachIndexed) / max * h2 + h2
                    if (x == 0) path.moveTo(x.toFloat(), y) else path.lineTo(x.toFloat(), y)
                }
                paintSink.color = when (n) {
                    0 -> Color.RED
                    1 -> Color.GREEN
                    2 -> Color.BLUE
                    else -> Color.GRAY
                }
                canvas.drawPath(path, paintSink)
            }
        }
    }
    root.addView(textView)
    val sensors = sensorManager.getSensorList(Sensor.TYPE_ALL)
    spinner.adapter = ArrayAdapter(
        spinner.context, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
        listOf("Select a sensor") + sensors
    )
    textView.setPadding((8 * activity.resources.dp).toInt())
    textView.isVisible = false
    textView.textDirection = View.TEXT_DIRECTION_LTR
    var previousListener: SensorEventListener? = null
    var samplingPeriod = SensorManager.SENSOR_DELAY_NORMAL
    fun listenToSensor() {
        val position = spinner.selectedItemPosition
        textView.isVisible = position != 0
        if (previousListener != null) sensorManager.unregisterListener(previousListener)
        if (position != 0) {
            val sensor = sensors.getOrNull(position - 1) ?: return
            val sensorDescription = sensor.toString()
            textView.text = sensorDescription
            val listener = object : SensorEventListener {
                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                    Toast.makeText(activity, "Accuracy is changed", Toast.LENGTH_SHORT).show()
                }

                override fun onSensorChanged(event: SensorEvent?) {
                    val v = event?.values ?: return
                    @SuppressLint("SetTextI18n")
                    textView.text = sensorDescription + "\nn: ${v.size}\n" +
                            v.joinToString("\n") + run {
                        if (v.size == 3) "\n|v| ${sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2])}"
                        else ""
                    }
                    log[counter++ % width] = v.clone()
                }
            }
            sensorManager.registerListener(listener, sensor, samplingPeriod)
            previousListener = listener
            initiateLog()
        }
    }
    textView.setOnClickListener {
        samplingPeriod = when (samplingPeriod) {
            SensorManager.SENSOR_DELAY_NORMAL -> SensorManager.SENSOR_DELAY_UI
            SensorManager.SENSOR_DELAY_UI -> SensorManager.SENSOR_DELAY_GAME
            else -> SensorManager.SENSOR_DELAY_NORMAL
        }
        listenToSensor()
    }
    spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) =
            listenToSensor()
    }

    androidx.appcompat.app.AlertDialog.Builder(activity)
        .setView(root)
        .setPositiveButton(R.string.close, null)
        .setCancelable(false)
        .setOnDismissListener { sensorManager.unregisterListener(previousListener) }
        .show()
}


fun showInputDeviceTestDialog(activity: FragmentActivity) {
    androidx.appcompat.app.AlertDialog.Builder(activity)
        .setView(object : AppCompatEditText(activity) {
            init {
                setPadding((8 * resources.dp).toInt())
                textSize = 4 * resources.dp
                text?.append("Input Devices Monitor:")
            }

            fun log(any: Any?) {
                text?.appendLine()
                text?.appendLine(any.toString())
            }

            override fun onGenericMotionEvent(event: MotionEvent?): Boolean {
                log(event)
                return super.onGenericMotionEvent(event)
            }

            override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
                log(event)
                return super.onKeyDown(keyCode, event)
            }

            override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
                log(event)
                return super.onKeyUp(keyCode, event)
            }
        })
        .show()
}

// Debug only dialog to check validity of dynamic icons generation
fun showIconsDemoDialog(activity: FragmentActivity) {
    val recyclerViewAdapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {}
        override fun getItemCount() = 62
        override fun getItemViewType(position: Int) = position
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            object : RecyclerView.ViewHolder(ShapeableImageView(activity).apply {
                val day = viewType / 2 + 1
                when (viewType % 2) {
                    0 -> setImageResource(getDayIconResource(day))
                    1 -> setImageBitmap(createStatusIcon(day))
                }
                val dp = resources.dp
                layoutParams =
                    ViewGroup.MarginLayoutParams((36 * dp).toInt(), (36 * dp).toInt())
                        .apply { setMargins((4 * dp).toInt()) }
                shapeAppearanceModel = ShapeAppearanceModel.Builder()
                    .setAllCorners(CornerFamily.ROUNDED, 8 * dp)
                    .setAllEdges(TriangleEdgeTreatment(4 * dp, true))
                    .build()
                setBackgroundColor(Color.DKGRAY)
            }) {}
    }
    androidx.appcompat.app.AlertDialog.Builder(activity)
        .setView(RecyclerView(activity).also {
            it.adapter = recyclerViewAdapter
            it.layoutManager = GridLayoutManager(activity, 8)
            it.setBackgroundColor(Color.WHITE)
        })
        .setNegativeButton(R.string.cancel, null)
        .show()
}

@RequiresApi(Build.VERSION_CODES.S)
fun showDynamicColorsDialog(activity: FragmentActivity) {
    val dynamicColors = listOf(
        android.R.color.system_accent1_0, android.R.color.system_accent1_10,
        android.R.color.system_accent1_50, android.R.color.system_accent1_100,
        android.R.color.system_accent1_200, android.R.color.system_accent1_300,
        android.R.color.system_accent1_400, android.R.color.system_accent1_500,
        android.R.color.system_accent1_600, android.R.color.system_accent1_700,
        android.R.color.system_accent1_800, android.R.color.system_accent1_900,
        android.R.color.system_accent1_1000,
        android.R.color.system_accent2_0, android.R.color.system_accent2_10,
        android.R.color.system_accent2_50, android.R.color.system_accent2_100,
        android.R.color.system_accent2_200, android.R.color.system_accent2_300,
        android.R.color.system_accent2_400, android.R.color.system_accent2_500,
        android.R.color.system_accent2_600, android.R.color.system_accent2_700,
        android.R.color.system_accent2_800, android.R.color.system_accent2_900,
        android.R.color.system_accent2_1000,
        android.R.color.system_accent3_0, android.R.color.system_accent3_10,
        android.R.color.system_accent3_50, android.R.color.system_accent3_100,
        android.R.color.system_accent3_200, android.R.color.system_accent3_300,
        android.R.color.system_accent3_400, android.R.color.system_accent3_500,
        android.R.color.system_accent3_600, android.R.color.system_accent3_700,
        android.R.color.system_accent3_800, android.R.color.system_accent3_900,
        android.R.color.system_accent3_1000,
        android.R.color.system_neutral1_0, android.R.color.system_neutral1_10,
        android.R.color.system_neutral1_50, android.R.color.system_neutral1_100,
        android.R.color.system_neutral1_200, android.R.color.system_neutral1_300,
        android.R.color.system_neutral1_400, android.R.color.system_neutral1_500,
        android.R.color.system_neutral1_600, android.R.color.system_neutral1_700,
        android.R.color.system_neutral1_800, android.R.color.system_neutral1_900,
        android.R.color.system_neutral1_1000,
        android.R.color.system_neutral2_0, android.R.color.system_neutral2_10,
        android.R.color.system_neutral2_50, android.R.color.system_neutral2_100,
        android.R.color.system_neutral2_200, android.R.color.system_neutral2_300,
        android.R.color.system_neutral2_400, android.R.color.system_neutral2_500,
        android.R.color.system_neutral2_600, android.R.color.system_neutral2_700,
        android.R.color.system_neutral2_800, android.R.color.system_neutral2_900,
        android.R.color.system_neutral2_1000,
    )
    val rows = listOf(
        "", "0", "10", "50", "100", "200", "300", "400", "500", "600", "700", "800", "900", "1000"
    )
    val cols = listOf("", "accent1", "accent2", "accent3", "neutral1", "neutral2")
    val recyclerViewAdapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {}
        override fun getItemCount() = 84
        override fun getItemViewType(position: Int) = position
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            object : RecyclerView.ViewHolder(TextView(activity).apply {
                textAlignment = View.TEXT_ALIGNMENT_CENTER
                val dp = resources.dp
                setAutoSizeTextTypeUniformWithConfiguration(8, 16, 1, dp.toInt())
                layoutParams = ViewGroup.MarginLayoutParams((50 * dp).toInt(), (25 * dp).toInt())
                    .apply { setMargins((4 * dp).toInt()) }
                val col = viewType % 6
                val row = viewType / 6
                if (row == 0) text = cols[col]
                else if (col == 0) text = rows[row]
                else {
                    val index = (col - 1) * 13 + row - 1
                    setBackgroundColor(context.getColor(dynamicColors[index]))
                }
            }) {}
    }
    androidx.appcompat.app.AlertDialog.Builder(activity)
        .setView(RecyclerView(activity).also {
            it.adapter = recyclerViewAdapter
            it.layoutManager = GridLayoutManager(activity, 6)
        })
        .show()
}

fun showTypographyDemoDialog(activity: FragmentActivity) {
    val text = buildSpannedString {
        textAppearances.forEach { (appearanceName, appearanceId) ->
            val textAppearance = TextAppearanceSpan(activity, appearanceId)
            inSpans(textAppearance) { append(appearanceName) }
            val originalSize = if (Build.VERSION.SDK_INT >= 34) {
                TypedValue.deriveDimension(
                    TypedValue.COMPLEX_UNIT_SP,
                    textAppearance.textSize.toFloat(),
                    activity.resources.displayMetrics
                )
            } else textAppearance.textSize / activity.resources.sp(1f)
            append(" ${originalSize.roundToInt()}sp")
            appendLine()
        }
    }
    androidx.appcompat.app.AlertDialog.Builder(activity)
        .setView(TextView(activity).also { it.text = text }).show()
}

private val textAppearances = listOf(
    "DisplayLarge" to com.google.android.material.R.style.TextAppearance_Material3_DisplayLarge,
    "DisplayMedium" to com.google.android.material.R.style.TextAppearance_Material3_DisplayMedium,
    "DisplaySmall" to com.google.android.material.R.style.TextAppearance_Material3_DisplaySmall,
    "HeadlineLarge" to com.google.android.material.R.style.TextAppearance_Material3_HeadlineLarge,
    "HeadlineMedium" to com.google.android.material.R.style.TextAppearance_Material3_HeadlineMedium,
    "HeadlineSmall" to com.google.android.material.R.style.TextAppearance_Material3_HeadlineSmall,
    "TitleLarge" to com.google.android.material.R.style.TextAppearance_Material3_TitleLarge,
    "TitleMedium" to com.google.android.material.R.style.TextAppearance_Material3_TitleMedium,
    "TitleSmall" to com.google.android.material.R.style.TextAppearance_Material3_TitleSmall,
    "BodyLarge" to com.google.android.material.R.style.TextAppearance_Material3_BodyLarge,
    "BodyMedium" to com.google.android.material.R.style.TextAppearance_Material3_BodyMedium,
    "BodySmall" to com.google.android.material.R.style.TextAppearance_Material3_BodySmall,
    "LabelLarge" to com.google.android.material.R.style.TextAppearance_Material3_LabelLarge,
    "LabelMedium" to com.google.android.material.R.style.TextAppearance_Material3_LabelMedium,
    "LabelSmall" to com.google.android.material.R.style.TextAppearance_Material3_LabelSmall
)

fun showCarouselDialog(activity: FragmentActivity) {
    androidx.appcompat.app.AlertDialog.Builder(activity)
        .setView(FrameLayout(activity).also { root ->
            root.addView(RecyclerView(activity).also {
                it.layoutManager = CarouselLayoutManager()
                it.adapter = SeasonsAdapter()
                it.setHasFixedSize(true) // Just as an optimization
                it.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    (196 * activity.resources.dp).toInt()
                )
                // When items have match parent width PagerSnapHelper can be used instead of LinearSnapHelper
                PagerSnapHelper().attachToRecyclerView(it) // LinearSnapHelper().attachToRecyclerView(it)
            it.scrollToPosition(0)
            it.smoothScrollToPosition(12)
        })
    }).show()
}

// Lindenmayer system: https://en.wikipedia.org/wiki/L-system
class LSystem(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    private fun lSystem(startAxiom: String, rules: Map<Char, String>): Sequence<String> = sequence {
        var result = startAxiom
        yield(result)
        while (true) {
            result = result.map { rules[it] ?: it.toString() }.joinToString("")
            yield(result)
        }
    }

    private val s = lSystem("X", mapOf('X' to "F+[[X]-X]-F[-FX]+X", 'F' to "FF"))
        .take(7).last()

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun onDraw(canvas: Canvas) {
        canvas.scale(2f, -2f, width / 2f, height / 2f)
        canvas.translate(width / 2f, height / 2f)
        val angle = 25f
        s.forEach {
            when (it) {
                '-' -> canvas.rotate(-angle)
                '+' -> canvas.rotate(+angle)
                '[' -> canvas.save()
                ']' -> canvas.restore()

                else -> {
                    canvas.drawLine(0f, 0f, 0f, 1f, paint)
                    canvas.translate(0f, 1f)
                }
            }
        }
        // postDelayed(1000) { invalidate() }
    }
}
