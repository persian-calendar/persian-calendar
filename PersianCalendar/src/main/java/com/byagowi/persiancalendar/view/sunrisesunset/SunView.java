package com.byagowi.persiancalendar.view.sunrisesunset;

import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.Region;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import com.byagowi.persiancalendar.R;
import com.github.praytimes.Clock;
import com.github.praytimes.PrayTime;

import java.util.Calendar;
import java.util.Locale;
import java.util.Map;

import androidx.core.content.ContextCompat;

/**
 * @author MEHDI DIMYADI
 * MEHDIMYADI
 */

public class SunView extends View {

    Paint mPaint;
    Paint mSunPaint;
    Paint mSunRaisePaint;

    int horizonColor;
    int timelineColor;
    int taggingColor;
    int nightColor;
    int dayColor;
    int daySecondColor;
    int sunColor;

    int width;
    int height;

    Path curvePath;
    Path nightPath;
    double segmentByPixel;

    Map<PrayTime, Clock> prayTime;

    public SunView(Context context) {
        super(context);

        init(context, null);
    }

    public SunView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
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

            try {
                horizonColor = typedArray.getColor(R.styleable.SunView_SunViewHorizonColor, ContextCompat.getColor(context, R.color.sViewHorizonColor));
                timelineColor = typedArray.getColor(R.styleable.SunView_SunViewTimelineColor, ContextCompat.getColor(context, R.color.sViewTimelineColor));
                taggingColor = typedArray.getColor(R.styleable.SunView_SunViewTaglineColor, ContextCompat.getColor(context, R.color.sViewTaglineColor));
                nightColor = typedArray.getColor(R.styleable.SunView_SunViewNightColor, ContextCompat.getColor(context, R.color.sViewNightColor));
                dayColor = typedArray.getColor(R.styleable.SunView_SunViewDayColor, ContextCompat.getColor(context, R.color.sViewDayColor));
                daySecondColor = typedArray.getColor(R.styleable.SunView_SunViewDaySecondColor, ContextCompat.getColor(context, R.color.sViewDaySecondColor));
                sunColor = typedArray.getColor(R.styleable.SunView_SunViewSunColor, ContextCompat.getColor(context, R.color.sViewSunColor));
            } finally {
                typedArray.recycle();
            }
        }

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        mSunPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mSunPaint.setColor(sunColor);
        mSunPaint.setStyle(Paint.Style.FILL);

        mSunRaisePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mSunRaisePaint.setColor(sunColor);
        mSunRaisePaint.setStyle(Paint.Style.STROKE);
        mSunRaisePaint.setStrokeWidth(12);
        PathEffect sunRaysEffects = new DashPathEffect(new float[]{5, 12}, 0);
        mSunRaisePaint.setPathEffect(sunRaysEffects);

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            setLayerType(LAYER_TYPE_SOFTWARE, mPaint);*/
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);

        width = w;
        height = h;

        curvePath = new Path();
        curvePath.moveTo(0, height);

        segmentByPixel = (2 * Math.PI) / width;

        for (int x = 0; x <= width; x++) {
            curvePath.lineTo(x, getY(x, segmentByPixel, (int) (height * 0.9f)));
        }

        nightPath = new Path(curvePath);
        nightPath.setLastPoint(width, height);
        nightPath.lineTo(width, 0);
        nightPath.lineTo(0, 0);
        nightPath.close();
    }

    float current = 0;

    @androidx.annotation.Keep
    public void setRatio(float ratio) {
        current = ratio;
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        mPaint.setStyle(Paint.Style.FILL);

        // draw fill of day
        mPaint.setColor(nightColor);
        canvas.clipRect(0, height * 0.75f, width * current, height);
        canvas.drawPath(nightPath, mPaint);

        // draw fill of night
        mPaint.setColor(dayColor);
        canvas.clipRect(0, 0, width, height, Region.Op.REPLACE);
        canvas.clipRect(0, 0, width * current, height * 0.75f);
        canvas.drawPath(curvePath, mPaint);

        // draw time curve
        canvas.clipRect(0, 0, width, height, Region.Op.REPLACE);
        mPaint.setStrokeWidth(3);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(timelineColor);
        canvas.drawPath(curvePath, mPaint);

        // draw horizon line
        mPaint.setColor(horizonColor);
        canvas.drawLine(0, height * 0.75f, width, height * 0.75f, mPaint);

        // draw sunset and sunrise tag line indicator
        mPaint.setColor(taggingColor);
        mPaint.setStrokeWidth(2);
        canvas.drawLine(width * 0.17f, height * 0.2f, width * 0.17f, height * 0.7f, mPaint);
        canvas.drawLine(width * 0.83f, height * 0.2f, width * 0.83f, height * 0.7f, mPaint);

        // draw sun
        if (current >= 0.17f && current <= 0.83f) {
            //mPaint.setShadowLayer(1.0f, 1.0f, 2.0f, 0x33000000);
            canvas.drawCircle(width * current, getY((int) (width * current), segmentByPixel, (int) (height * 0.9f)), height * 0.08f - 12, mSunPaint);
            //mPaint.clearShadowLayer();
            canvas.drawCircle(width * current, getY((int) (width * current), segmentByPixel, (int) (height * 0.9f)), height * 0.08f, mSunRaisePaint);
        }

    }

    private float getY(int x, double segment, int height) {
        double cos = (Math.cos(-Math.PI + (x * segment)) + 1) / 2;
        return height - (height * (float) cos) + (height * 0.1f);
    }

    public void setSunriseSunsetCalculator(Map<PrayTime, Clock> prayTime) {
        this.prayTime = prayTime;
        postInvalidate();
    }

    private final float FULL_DAY = new Clock(24, 0).toInt();
    private final float HALF_DAY = new Clock(12, 0).toInt();

    public void startAnimate() {
        if (prayTime == null)
            return;
//
//        int sunset = prayTime.get(PrayTime.SUNSET).toInt();
//        if (midnight < HALF_DAY) midnight += FULL_DAY;
//        int sunrise = prayTime.get(PrayTime.SUNRISE).toInt();
//
//        // recalculate from cero
//        int noon = prayTime.get(PrayTime.DHUHR).toInt();
//        int end = noon + new Clock(24, 0).toInt() / 2;
//
//
//        float c = 0;

//        if (current <= sunrise) {
//            c = ((float) current / sunrise) * 0.17f;
//        } else if (current <= sunset) {
//            c = (((float) (current - sunrise) / (sunset - sunrise)) * 0.66f) + 0.17f;
//        } else if (current <= end) {
//            c = (((float) (current - sunset) / (end - sunset)) * 0.17f) + 0.17f + 0.66f;
//        }

        float midnight = prayTime.get(PrayTime.MIDNIGHT).toInt();
//        if (midnight > HALF_DAY) midnight = FULL_DAY - midnight;
        float current = new Clock(Calendar.getInstance(Locale.getDefault())).toInt();
        float ratio = (current - midnight) / FULL_DAY;

        ObjectAnimator animator = ObjectAnimator.ofFloat(this, "ratio", 0, ratio);
        animator.setDuration(1500L);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.start();
    }
}
