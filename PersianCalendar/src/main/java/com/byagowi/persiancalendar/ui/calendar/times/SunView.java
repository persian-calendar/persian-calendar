package com.byagowi.persiancalendar.ui.calendar.times;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.praytimes.Clock;
import com.byagowi.persiancalendar.praytimes.PrayTimes;
import com.byagowi.persiancalendar.utils.TypefaceUtils;
import com.byagowi.persiancalendar.utils.Utils;

import java.util.Calendar;
import java.util.Locale;

import androidx.annotation.ColorInt;
import androidx.core.content.ContextCompat;

/**
 * @author MEHDI DIMYADI
 * MEHDIMYADI
 */

public class SunView extends View implements ValueAnimator.AnimatorUpdateListener {

    private final float FULL_DAY = new Clock(24, 0).toInt();
    private final float HALF_DAY = new Clock(12, 0).toInt();
    Paint mPaint, mSunPaint, mSunRaisePaint, mDayPaint;
    @ColorInt
    int horizonColor, timelineColor, taggingColor, nightColor, dayColor, daySecondColor, sunColor,
            sunBeforeMiddayColor, sunAfterMiddayColor, sunEveningColor, sunriseTextColor,
            middayTextColor, sunsetTextColor, colorTextNormal, colorTextSecond;
    int width, height;
    Path curvePath, nightPath;
    float current = 0;
    LinearGradient linearGradient = new LinearGradient(0, 0, 1, 0, 0, 0, Shader.TileMode.MIRROR);
    Paint moonPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    Paint moonPaintB = new Paint(Paint.ANTI_ALIAS_FLAG);
    Paint moonPaintO = new Paint(Paint.ANTI_ALIAS_FLAG);
    Paint moonPaintD = new Paint(Paint.ANTI_ALIAS_FLAG);
    RectF moonRect = new RectF();
    RectF moonOval = new RectF();
    String dayLengthString = "";
    String remainingString = "";
    String sunriseString = "";
    String middayString = "";
    String sunsetString = "";
    boolean isRTL = false;
    private double segmentByPixel;
    private ArgbEvaluator argbEvaluator = new ArgbEvaluator();
    private PrayTimes prayTimes;
    //    private Horizontal moonPosition;
    private double moonPhase = 1;
    private int fontSize;

    public SunView(Context context) {
        super(context);

        init(context, null);
    }

    public SunView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context, attrs);
    }

    public SunView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SunView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SunView);
            TypedValue typedValue = new TypedValue();
            Resources.Theme theme = context.getTheme();

            try {
                theme.resolveAttribute(R.attr.SunViewHorizonColor, typedValue, true);
                int HorizonColor = ContextCompat.getColor(context, typedValue.resourceId);
                horizonColor = typedArray.getColor(R.styleable.SunView_SunViewHorizonColor, HorizonColor);
                theme.resolveAttribute(R.attr.SunViewTimelineColor, typedValue, true);
                int TimelineColor = ContextCompat.getColor(context, typedValue.resourceId);
                timelineColor = typedArray.getColor(R.styleable.SunView_SunViewHorizonColor, TimelineColor);
                theme.resolveAttribute(R.attr.SunViewTaglineColor, typedValue, true);
                int taglineColor = ContextCompat.getColor(context, typedValue.resourceId);
                taggingColor = typedArray.getColor(R.styleable.SunView_SunViewHorizonColor, taglineColor);
                nightColor = typedArray.getColor(R.styleable.SunView_SunViewNightColor, ContextCompat.getColor(context, R.color.sViewNightColor));
                dayColor = typedArray.getColor(R.styleable.SunView_SunViewDayColor, ContextCompat.getColor(context, R.color.sViewDayColor));
                daySecondColor = typedArray.getColor(R.styleable.SunView_SunViewDaySecondColor, ContextCompat.getColor(context, R.color.sViewDaySecondColor));
                sunColor = typedArray.getColor(R.styleable.SunView_SunViewSunColor, ContextCompat.getColor(context, R.color.sViewSunColor));
                sunBeforeMiddayColor = typedArray.getColor(R.styleable.SunView_SunViewBeforeMiddayColor, ContextCompat.getColor(context, R.color.sViewSunBeforeMiddayColor));
                sunAfterMiddayColor = typedArray.getColor(R.styleable.SunView_SunViewAfterMiddayColor, ContextCompat.getColor(context, R.color.sViewSunAfterMiddayColor));
                sunEveningColor = typedArray.getColor(R.styleable.SunView_SunViewEveningColor, ContextCompat.getColor(context, R.color.sViewSunEveningColor));
                theme.resolveAttribute(R.attr.SunViewSunriseTextColor, typedValue, true);
                int SunriseTextColor = ContextCompat.getColor(context, typedValue.resourceId);
                sunriseTextColor = typedArray.getColor(R.styleable.SunView_SunViewSunriseTextColor, SunriseTextColor);
                theme.resolveAttribute(R.attr.SunViewMiddayTextColor, typedValue, true);
                int MiddayTextColor = ContextCompat.getColor(context, typedValue.resourceId);
                middayTextColor = typedArray.getColor(R.styleable.SunView_SunViewMiddayTextColor, MiddayTextColor);
                theme.resolveAttribute(R.attr.SunViewSunsetTextColor, typedValue, true);
                int SunsetTextColor = ContextCompat.getColor(context, typedValue.resourceId);
                sunsetTextColor = typedArray.getColor(R.styleable.SunView_SunViewSunsetTextColor, SunsetTextColor);

                theme.resolveAttribute(R.attr.colorTextNormal, typedValue, true);
                colorTextNormal = ContextCompat.getColor(context, typedValue.resourceId);
                theme.resolveAttribute(R.attr.colorTextSecond, typedValue, true);
                colorTextSecond = ContextCompat.getColor(context, typedValue.resourceId);

                fontSize = dpToPx(14);
            } finally {
                typedArray.recycle();
            }
        }

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setTypeface(TypefaceUtils.getAppFont(context));

        mSunPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mSunPaint.setColor(sunColor);
        mSunPaint.setStyle(Paint.Style.FILL);

        mSunRaisePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mSunRaisePaint.setColor(sunColor);
        mSunRaisePaint.setStyle(Paint.Style.STROKE);
        mSunRaisePaint.setStrokeWidth(7);
        PathEffect sunRaysEffects = new DashPathEffect(new float[]{3, 7}, 0);
        mSunRaisePaint.setPathEffect(sunRaysEffects);

        mDayPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mDayPaint.setStyle(Paint.Style.FILL_AND_STROKE);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);

        width = w;
        height = h - 18;

        curvePath = new Path();
        curvePath.moveTo(0, height);

        if (width != 0) {
            segmentByPixel = (2 * Math.PI) / width;
        }

        for (int x = 0; x <= width; x++) {
            curvePath.lineTo(x, getY(x, segmentByPixel, (int) (height * 0.9f)));
        }

        nightPath = new Path(curvePath);
        nightPath.setLastPoint(width, height);
        nightPath.lineTo(width, 0);
        nightPath.lineTo(0, 0);
        nightPath.close();
    }

    // https://stackoverflow.com/a/34763668
    private int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.save();

        // draw fill of night
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(nightColor);
        canvas.clipRect(0, height * 0.75f, width * current, height);
        canvas.drawPath(nightPath, mPaint);

        canvas.restore();

        canvas.save();

        // draw fill of day
        canvas.clipRect(0, 0, width, height);
        canvas.clipRect(0, 0, width * current, height * 0.75f);
        mDayPaint.setShader(linearGradient);
        canvas.drawPath(curvePath, mDayPaint);

        canvas.restore();

        canvas.save();

        // draw time curve
        canvas.clipRect(0, 0, width, height);
        mPaint.setStrokeWidth(3);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(timelineColor);
        canvas.drawPath(curvePath, mPaint);

        canvas.restore();

        // draw horizon line
        mPaint.setColor(horizonColor);
        canvas.drawLine(0, height * 0.75f, width, height * 0.75f, mPaint);

        // draw sunset and sunrise tag line indicator
        mPaint.setColor(taggingColor);
        mPaint.setStrokeWidth(2);
        canvas.drawLine(width * 0.17f, height * 0.3f, width * 0.17f, height * 0.7f, mPaint);
        canvas.drawLine(width * 0.83f, height * 0.3f, width * 0.83f, height * 0.7f, mPaint);
        canvas.drawLine(getWidth() / 2f, height * 0.7f, getWidth() / 2f, height * 0.8f, mPaint);

        // draw text
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setTextSize(fontSize);
        mPaint.setStrokeWidth(0);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(sunriseTextColor);
        canvas.drawText(sunriseString, width * 0.17f, height * .2f, mPaint);
        mPaint.setColor(middayTextColor);
        canvas.drawText(middayString, width / 2f, height * .94f, mPaint);
        mPaint.setColor(sunsetTextColor);
        canvas.drawText(sunsetString, width * 0.83f, height * .2f, mPaint);

        // draw remaining time
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setStrokeWidth(0);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(colorTextSecond);
        canvas.drawText(dayLengthString, width * (isRTL ? 0.70f : 0.30f), height * .94f, mPaint);
        if (!TextUtils.isEmpty(remainingString)) {
            canvas.drawText(remainingString, width * (isRTL ? 0.30f : 0.70f), height * .94f, mPaint);
        }

        // draw sun
        if (current >= 0.17f && current <= 0.83f) {

            @ColorInt
            int color = (int) argbEvaluator.evaluate(current,
                    sunBeforeMiddayColor, sunAfterMiddayColor);

            mSunPaint.setColor(color);
            //mSunRaisePaint.setColor(color);
            //mPaint.setShadowLayer(1.0f, 1.0f, 2.0f, 0x33000000);
            canvas.drawCircle(width * current, getY((int) (width * current), segmentByPixel, (int) (height * 0.9f)), (height * 0.09f), mSunPaint);
            //mPaint.clearShadowLayer();
            //canvas.drawCircle(width * current, getY((int) (width * current), segmentByPixel, (int) (height * 0.9f)), (height * 0.09f) - 5, mSunRaisePaint);
        } else {
            drawMoon(canvas);
        }
    }

    public void drawMoon(Canvas canvas) {
        // This is brought from QiblaCompassView with some modifications
        float r = (height * 0.08f);
        float radius = 1;
        float px = width * current;
        float py = getY((int) (width * current), segmentByPixel, (int) (height * 0.9f));
        moonPaint.reset();
        moonPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        moonPaint.setColor(Color.WHITE);
        moonPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        moonPaintB.reset();// moon Paint Black
        moonPaintB.setFlags(Paint.ANTI_ALIAS_FLAG);
        moonPaintB.setColor(Color.BLACK);
        moonPaintB.setStyle(Paint.Style.FILL_AND_STROKE);
        moonPaintO.reset();// moon Paint for Oval
        moonPaintO.setFlags(Paint.ANTI_ALIAS_FLAG);
        moonPaintO.setColor(Color.WHITE);
        moonPaintO.setStyle(Paint.Style.FILL_AND_STROKE);
        moonPaintD.reset();// moon Paint for Diameter
        // draw
        moonPaintD.setColor(Color.GRAY);
        moonPaintD.setStyle(Paint.Style.STROKE);
        moonPaintD.setFlags(Paint.ANTI_ALIAS_FLAG);
        canvas.rotate(180, px, py);
        int eOffset = 0;
        // elevation Offset 0 for 0 degree; r for 90 degree
        moonRect.set(px - r, py + eOffset - radius - r, px + r, py + eOffset - radius + r);
        canvas.drawArc(moonRect, 90, 180, false, moonPaint);
        canvas.drawArc(moonRect, 270, 180, false, moonPaintB);
        int arcWidth = (int) ((moonPhase - 0.5) * (4 * r));
        moonPaintO.setColor(arcWidth < 0 ? Color.BLACK : Color.WHITE);
        moonOval.set(px - Math.abs(arcWidth) / 2f, py + eOffset - radius - r,
                px + Math.abs(arcWidth) / 2f, py + eOffset - radius + r);
        canvas.drawArc(moonOval, 0, 360, false, moonPaintO);
        canvas.drawArc(moonRect, 0, 360, false, moonPaintD);
        canvas.drawLine(px, py - radius, px, py + radius, moonPaintD);
        moonPaintD.setPathEffect(null);
    }

    private float getY(int x, double segment, int height) {
        double cos = (Math.cos(-Math.PI + (x * segment)) + 1) / 2;
        return height - (height * (float) cos) + (height * 0.1f);
    }

    public void setSunriseSunsetMoonPhase(PrayTimes prayTimes, double moonPhase) {
        this.prayTimes = prayTimes;
        this.moonPhase = moonPhase;
        postInvalidate();
    }

    public void startAnimate(boolean immediate) {
        Context context = getContext();
        if (prayTimes == null || context == null)
            return;

        isRTL = Utils.isRTL(context);
        sunriseString = context.getString(R.string.sunriseSunView);
        middayString = context.getString(R.string.middaySunView);
        sunsetString = context.getString(R.string.sunsetSunView);

        float sunset = prayTimes.getSunsetClock().toInt();
        float sunrise = prayTimes.getSunriseClock().toInt();
        float midnight = prayTimes.getMidnightClock().toInt();

        if (midnight > HALF_DAY) midnight = midnight - FULL_DAY;
        float now = new Clock(Calendar.getInstance(Locale.getDefault())).toInt();

        float c = 0;
        if (now <= sunrise) {
            if (sunrise != 0) {
                c = ((now - midnight) / sunrise) * 0.17f;
            }
        } else if (now <= sunset) {
            if (sunset - sunrise != 0) {
                c = (((now - sunrise) / (sunset - sunrise)) * 0.66f) + 0.17f;
            }
        } else {
            if (FULL_DAY + midnight - sunset != 0) {
                c = (((now - sunset) / (FULL_DAY + midnight - sunset)) * 0.17f) + 0.17f + 0.66f;
            }
        }

        Clock dayLength = Clock.fromInt((int) (sunset - sunrise));
        Clock remaining = Clock.fromInt(now > sunset || now < sunrise ? 0 : (int) (sunset - now));
        dayLengthString = String.format(context.getString(R.string.length_of_day),
                Utils.formatNumber(dayLength.getHour()),
                Utils.formatNumber(dayLength.getMinute()));
        if (remaining.toInt() == 0) {
            remainingString = "";
        } else {
            remainingString = String.format(context.getString(R.string.remaining_daylight),
                    Utils.formatNumber(remaining.getHour()),
                    Utils.formatNumber(remaining.getMinute()));
        }

        argbEvaluator = new ArgbEvaluator();

        linearGradient = new LinearGradient(getWidth() * 0.17f, 0, getWidth() * 0.5f, 0,
                dayColor, daySecondColor, Shader.TileMode.MIRROR);

        if (immediate) {
            current = c;
            postInvalidate();
        } else {
            ValueAnimator animator = ValueAnimator.ofFloat(0, c);
            animator.setDuration(1500L);
            animator.setInterpolator(new DecelerateInterpolator());
            animator.addUpdateListener(this);
            animator.start();
        }
    }

    public void clear() {
        current = 0;
        postInvalidate();
    }

    @Override
    public void onAnimationUpdate(ValueAnimator valueAnimator) {
        current = (float) valueAnimator.getAnimatedValue();
        postInvalidate();
    }
}
