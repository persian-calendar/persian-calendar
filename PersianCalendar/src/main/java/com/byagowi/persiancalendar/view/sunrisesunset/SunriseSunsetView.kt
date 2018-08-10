package com.byagowi.persiancalendar.view.sunrisesunset

import android.animation.ObjectAnimator
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PathEffect
import android.graphics.RectF
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator

import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.util.UIUtils
import com.github.praytimes.Clock

import java.util.Calendar
import java.util.Locale

import androidx.annotation.ColorInt
import androidx.annotation.Nullable

class SunriseSunsetView : View {

  private var mRatio: Float = 0.toFloat()

  private lateinit var mTrackPaint: Paint
  @ColorInt
  private var mTrackColor = DEFAULT_TRACK_COLOR
  private var mTrackWidth = DEFAULT_TRACK_WIDTH_PX
  private var mTrackPathEffect: PathEffect = DashPathEffect(floatArrayOf(15f, 15f), 1f)
  private var mTrackRadius: Float = 0.toFloat()

  private lateinit var mShadowPaint: Paint
  @ColorInt
  private var mShadowColor = DEFAULT_SHADOW_COLOR

  private lateinit var mSunPaint: Paint
  @ColorInt
  private var mSunColor = DEFAULT_SUN_COLOR
  private var mSunRadius = DEFAULT_SUN_RADIUS_PX.toFloat()
  private var mSunPaintStyle: Paint.Style = Paint.Style.FILL

  private lateinit var mLabelPaint: TextPaint
  private var mLabelTextSize = DEFAULT_LABEL_TEXT_SIZE
  @ColorInt
  private var mLabelTextColor = DEFAULT_LABEL_TEXT_COLOR
  private var mLabelVerticalOffset = DEFAULT_LABEL_VERTICAL_OFFSET_PX
  private var mLabelHorizontalOffset = DEFAULT_LABEL_HORIZONTAL_OFFSET_PX

  private var mSunriseTime: Clock? = null
  private var mMiddayTime: Clock? = null
  private var mSunsetTime: Clock? = null

  private val mBoardRectF = RectF()

  constructor(context: Context) : super(context) {
    init()
  }

  @JvmOverloads constructor(context: Context, @Nullable attrs: AttributeSet, defStyleAttr: Int = 0) : super(context, attrs, defStyleAttr) {
    val a = context.obtainStyledAttributes(attrs, R.styleable.SunriseSunsetView, defStyleAttr, 0)
    if (a != null) {
      mTrackColor = a.getColor(R.styleable.SunriseSunsetView_ssv_track_color, DEFAULT_TRACK_COLOR)
      mTrackWidth = a.getDimensionPixelSize(R.styleable.SunriseSunsetView_ssv_track_width, DEFAULT_TRACK_WIDTH_PX)

      mShadowColor = a.getColor(R.styleable.SunriseSunsetView_ssv_shadow_color, DEFAULT_SHADOW_COLOR)

      mSunColor = a.getColor(R.styleable.SunriseSunsetView_ssv_sun_color, DEFAULT_SUN_COLOR)
      mSunRadius = a.getDimensionPixelSize(R.styleable.SunriseSunsetView_ssv_sun_radius, DEFAULT_SUN_RADIUS_PX).toFloat()

      mLabelTextColor = a.getColor(R.styleable.SunriseSunsetView_ssv_label_text_color, DEFAULT_LABEL_TEXT_COLOR)
      mLabelTextSize = a.getDimensionPixelSize(R.styleable.SunriseSunsetView_ssv_label_text_size, DEFAULT_LABEL_TEXT_SIZE)
      mLabelVerticalOffset = a.getDimensionPixelOffset(R.styleable.SunriseSunsetView_ssv_label_vertical_offset, DEFAULT_LABEL_VERTICAL_OFFSET_PX)
      mLabelHorizontalOffset = a.getDimensionPixelOffset(R.styleable.SunriseSunsetView_ssv_label_horizontal_offset, DEFAULT_LABEL_HORIZONTAL_OFFSET_PX)
      a.recycle()
    }
    init()
  }

  override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    val paddingRight = paddingRight
    val paddingLeft = paddingLeft
    val paddingTop = paddingTop
    val paddingBottom = paddingBottom

    val widthSpecMode = View.MeasureSpec.getMode(widthMeasureSpec)
    var widthSpecSize = View.MeasureSpec.getSize(widthMeasureSpec)

    if (widthSpecMode == View.MeasureSpec.AT_MOST) {
      widthSpecSize = paddingLeft + paddingRight + MINIMAL_TRACK_RADIUS_PX * 2 + mSunRadius.toInt() * 2
    }

    mTrackRadius = 1.0f * (widthSpecSize.toFloat() - paddingLeft.toFloat() - paddingRight.toFloat() - 2 * mSunRadius) / 2
    var expectedHeight = (mTrackRadius + mSunRadius + paddingBottom.toFloat() + paddingTop.toFloat()).toInt()
    if (false) {
      expectedHeight /= 2
    }
    mBoardRectF.set(paddingLeft + mSunRadius, paddingTop + mSunRadius,
        widthSpecSize.toFloat() - paddingRight.toFloat() - mSunRadius, (expectedHeight - paddingBottom).toFloat())
    setMeasuredDimension(widthSpecSize, expectedHeight)
  }

  private fun init() {
    mTrackPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    mTrackPaint.style = Paint.Style.STROKE
    prepareTrackPaint()

    mShadowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    mShadowPaint.style = Paint.Style.FILL_AND_STROKE
    prepareShadowPaint()

    mSunPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    mSunPaint.strokeWidth = DEFAULT_SUN_STROKE_WIDTH_PX.toFloat()
    prepareSunPaint()

    mLabelPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
    prepareLabelPaint()
  }

  private fun prepareTrackPaint() {
    mTrackPaint.color = mTrackColor
    mTrackPaint.strokeWidth = mTrackWidth.toFloat()
    mTrackPaint.pathEffect = mTrackPathEffect
  }

  private fun prepareShadowPaint() {
    mShadowPaint.color = mShadowColor
  }

  private fun prepareSunPaint() {
    mSunPaint.color = mSunColor
    mSunPaint.strokeWidth = DEFAULT_SUN_STROKE_WIDTH_PX.toFloat()
    mSunPaint.style = mSunPaintStyle
  }

  private fun prepareLabelPaint() {
    mLabelPaint.color = mLabelTextColor
    mLabelPaint.textSize = mLabelTextSize.toFloat()
  }


  override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)

    drawSunTrack(canvas)
    drawShadow(canvas)
    drawSun(canvas)
    drawSunriseSunsetLabel(canvas)
  }

  private fun drawSunTrack(canvas: Canvas) {
    prepareTrackPaint()
    canvas.save()
    val rectF = RectF(mBoardRectF.left, mBoardRectF.top, mBoardRectF.right, mBoardRectF.bottom + mBoardRectF.height())
    canvas.drawArc(rectF, 180f, 180f, false, mTrackPaint)
    canvas.restore()
  }

  private fun drawShadow(canvas: Canvas) {
    prepareShadowPaint()

    canvas.save()
    val path = Path()
    val endY = mBoardRectF.bottom
    val rectF = RectF(mBoardRectF.left, mBoardRectF.top, mBoardRectF.right, mBoardRectF.bottom + mBoardRectF.height())
    val curPointX = mBoardRectF.left + mTrackRadius - mTrackRadius * Math.cos(Math.PI * mRatio).toFloat()

    path.moveTo(0f, endY)
    path.arcTo(rectF, 180f, 180 * mRatio)
    path.lineTo(curPointX, endY)
    path.close()
    canvas.drawPath(path, mShadowPaint)
    canvas.restore()
  }

  private fun drawSun(canvas: Canvas) {
    prepareSunPaint()
    canvas.save()

    val curPointX = mBoardRectF.left + mTrackRadius - mTrackRadius * Math.cos(Math.PI * mRatio).toFloat()
    val curPointY = mBoardRectF.bottom - mTrackRadius * Math.sin(Math.PI * mRatio).toFloat()
    canvas.drawCircle(curPointX, curPointY, mSunRadius, mSunPaint)

    canvas.restore()
  }

  private fun drawSunriseSunsetLabel(canvas: Canvas) {
    val sunriseTime = mSunriseTime
    val sunsetTime = mSunsetTime
    val middayTime = mMiddayTime
    if (sunriseTime != null && sunsetTime != null && middayTime != null) {
      prepareLabelPaint()

      canvas.save()

      val leftLabel: String
      val rightLabel: String
      leftLabel = UIUtils.getFormattedClock(sunriseTime)
      rightLabel = UIUtils.getFormattedClock(sunsetTime)

      mLabelPaint.textAlign = Paint.Align.LEFT
      val metricsInt = mLabelPaint.fontMetricsInt
      var baseLineX = mBoardRectF.left + mSunRadius + mLabelHorizontalOffset.toFloat()
      val baseLineY = mBoardRectF.bottom - metricsInt.bottom.toFloat() - mLabelVerticalOffset.toFloat()
      canvas.drawText(leftLabel, baseLineX, baseLineY, mLabelPaint)

      mLabelPaint.textAlign = Paint.Align.CENTER
      canvas.drawText(UIUtils.getFormattedClock(middayTime),
          mBoardRectF.centerX() - mLabelHorizontalOffset,
          mBoardRectF.top + metricsInt.bottom * mLabelVerticalOffset, mLabelPaint)

      mLabelPaint.textAlign = Paint.Align.RIGHT
      baseLineX = mBoardRectF.right - mSunRadius - mLabelHorizontalOffset.toFloat()
      canvas.drawText(rightLabel, baseLineX, baseLineY, mLabelPaint)
      canvas.restore()
    }
  }

  fun setSunriseTime(sunriseTime: Clock) {
    mSunriseTime = sunriseTime
  }

  fun setMiddayTime(middayTime: Clock) {
    mMiddayTime = middayTime
  }

  fun setSunsetTime(sunsetTime: Clock) {
    mSunsetTime = sunsetTime
  }

  fun startAnimate() {
    val sunriseTime = mSunriseTime
    val sunsetTime = mSunsetTime
    if (sunriseTime == null || sunsetTime == null) {
      throw RuntimeException("You need to set both sunrise and sunset time before start animation")
    }
    val sunrise = sunriseTime.transformToMinutes()
    val sunset = sunsetTime.transformToMinutes()
    val calendar = Calendar.getInstance(Locale.getDefault())
    val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
    val currentMinute = calendar.get(Calendar.MINUTE)
    val currentTime = currentHour * Clock.MINUTES_PER_HOUR + currentMinute
    var ratio = 1.0f * (currentTime - sunrise) / (sunset - sunrise)
    ratio = if (ratio <= 0) 0f else if (ratio > 1.0f) 1f else ratio
    val fromRatio = 0f
    val animator = ObjectAnimator.ofFloat(this, "ratio", fromRatio, ratio)
    animator.duration = 1500L
    animator.interpolator = LinearInterpolator()
    animator.start()
  }

  companion object {

    @ColorInt
    private val DEFAULT_TRACK_COLOR = Color.WHITE
    private val DEFAULT_TRACK_WIDTH_PX = 4

    @ColorInt
    private val DEFAULT_SUN_COLOR = Color.YELLOW
    private val DEFAULT_SUN_RADIUS_PX = 20
    private val DEFAULT_SUN_STROKE_WIDTH_PX = 4

    @ColorInt
    private val DEFAULT_SHADOW_COLOR = Color.parseColor("#32FFFFFF")

    @ColorInt
    private val DEFAULT_LABEL_TEXT_COLOR = Color.WHITE
    private val DEFAULT_LABEL_TEXT_SIZE = 40
    private val DEFAULT_LABEL_VERTICAL_OFFSET_PX = 5
    private val DEFAULT_LABEL_HORIZONTAL_OFFSET_PX = 20

    private val MINIMAL_TRACK_RADIUS_PX = 300
  }

}
