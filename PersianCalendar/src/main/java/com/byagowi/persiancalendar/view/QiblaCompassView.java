// copyedited from https://code.google.com/p/android-salat-times/source/browse/src/com/cepmuvakkit/times/view/QiblaCompassView.java
// licensed under GPLv3
package com.byagowi.persiancalendar.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import com.byagowi.persiancalendar.R;
import com.cepmuvakkit.times.posAlgo.AstroLib;
import com.cepmuvakkit.times.posAlgo.EarthHeading;
import com.cepmuvakkit.times.posAlgo.Horizontal;
import com.cepmuvakkit.times.posAlgo.SunMoonPosition;

import java.util.GregorianCalendar;

public class QiblaCompassView extends View {
    private Paint dashedPaint;
    private int px, py; // Center of Compass (px,py)
    private int Radius; // Radius of Compass dial
    private int r; // Radius of Sun and Moon
    private String northString, eastString, southString, westString;
    private DashPathEffect dashPath;
    private float bearing;
    private Horizontal sunPosition, moonPosition;
    private EarthHeading qiblaInfo;
    private SunMoonPosition sunMoonPosition;
    private double longitude = 0.0;
    private double latitude = 0.0;

    private Paint textPaint;

    public QiblaCompassView(Context context) {
        super(context);
        initCompassView();
    }

    public QiblaCompassView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initCompassView();
    }

    public QiblaCompassView(Context context, AttributeSet ats, int defaultStyle) {
        super(context, ats, defaultStyle);
        initCompassView();
    }

    private void initAstronomicParameters() {
        GregorianCalendar c = new GregorianCalendar();
        double jd = AstroLib.calculateJulianDay(c);

        double ΔT = 0;
        double altitude = 0.0;
        sunMoonPosition = new SunMoonPosition(jd, latitude, longitude,
                altitude, ΔT);
        sunPosition = sunMoonPosition.getSunPosition();
        moonPosition = sunMoonPosition.getMoonPosition();
    }

    public void initCompassView() {
        setFocusable(true);
        initAstronomicParameters();
        northString = "N";
        eastString = "E";
        southString = "S";
        westString = "W";

        dashPath = new DashPathEffect(new float[]{2, 5}, 1);
        dashedPaint = new Paint(Paint.FAKE_BOLD_TEXT_FLAG);
        dashedPaint.setPathEffect(dashPath);
        dashedPaint.setStrokeWidth(2);
        dashedPaint.setPathEffect(dashPath);
        dashedPaint.setColor(ContextCompat.getColor(getContext(), R.color.qibla_color));

        textPaint = new Paint(Paint.FAKE_BOLD_TEXT_FLAG);
        textPaint.setColor(ContextCompat.getColor(getContext(), (R.color.qibla_color)));
        textPaint.setTextSize(20);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // The compass is a circle that fills as much space as possible.
        // Set the measured dimensions by figuring out the shortest boundary,
        // height or width.
        int measuredWidth = measure(widthMeasureSpec);
        int measuredHeight = measure(heightMeasureSpec);

        // int d = Math.min(measuredWidth, measuredHeight);

        setMeasuredDimension(measuredWidth, measuredHeight);
    }

    private int measure(int measureSpec) {
        int result;

        // Decode the measurement specifications.
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.UNSPECIFIED) {
            // Return a default size of 200 if no bounds are specified.
            result = 600;
        } else {
            // As you want to fill the available space
            // always return the full available bounds.
            result = specSize;
        }
        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        this.Radius = Math.min(px, py);
        this.r = Radius / 10; // Sun Moon radius;
        // over here
        qiblaInfo = sunMoonPosition.getDestinationHeading();
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setColor(ContextCompat.getColor(getContext(), (R.color.qibla_color)));
        canvas.rotate(-bearing, px, py);// Attach and Detach capability lies
        canvas.save();
        drawDial(canvas);
        if (isLongLatAvailable()) {
            canvas.save();
            drawQibla(canvas);
        }
        canvas.save();
        drawTrueNorthArrow(canvas, bearing);
        if (isLongLatAvailable()) {
            canvas.save();
            drawMoon(canvas);
            canvas.save();
            drawSun(canvas);
        }
        canvas.save();
    }

    public boolean isLongLatAvailable() {
        return longitude != 0.0 && latitude != 0.0;
    }

    Path mPath = new Path();
    Paint trueNorthArrowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public void drawTrueNorthArrow(Canvas canvas, float drawnAngle) {
        trueNorthArrowPaint.reset();
        trueNorthArrowPaint.setColor(Color.RED);
        trueNorthArrowPaint.setStyle(Paint.Style.FILL);
        trueNorthArrowPaint.setAlpha(100);
        int r = Radius / 12;
        // Construct a wedge-shaped path
        mPath.reset();
        mPath.moveTo(px, py - px);
        mPath.lineTo(px - r, py);
        mPath.lineTo(px, py + r);
        mPath.lineTo(px + r, py);
        mPath.addCircle(px, py, r, Path.Direction.CCW);
        mPath.close();
        canvas.drawPath(mPath, trueNorthArrowPaint);
        dashedPaint.setColor(Color.RED);
        canvas.drawLine(px, py - px, px, py + Radius, dashedPaint);
        canvas.drawCircle(px, py, 5, dashedPaint);
        canvas.restore();
    }

    Paint markerPaint = new Paint(Paint.FAKE_BOLD_TEXT_FLAG);
    Paint circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public void drawDial(Canvas canvas) {
        // over here
        circlePaint.reset();
        circlePaint.setColor(ContextCompat.getColor(getContext(), R.color.qibla_color));
        circlePaint.setStrokeWidth(1);
        circlePaint.setStyle(Paint.Style.STROKE); // Sadece Cember ciziyor.

        int textHeight = (int) textPaint.measureText("yY");
        markerPaint.reset();
        markerPaint.setColor(ContextCompat.getColor(getContext(), R.color.qibla_color));
        // Draw the background
        canvas.drawCircle(px, py, Radius, circlePaint);
        canvas.drawCircle(px, py, Radius - 20, circlePaint);
        // Rotate our perspective so that the "top" is
        // facing the current bearing.

        int textWidth = (int) textPaint.measureText("W");
        int cardinalX = px - textWidth / 2;
        int cardinalY = py - Radius + textHeight;

        // Draw the marker every 15 degrees and text every 45.
        for (int i = 0; i < 24; i++) {
            // Draw a marker.
            canvas.drawLine(px, py - Radius, px, py - Radius + 10, markerPaint);
            canvas.save();
            canvas.translate(0, textHeight);
            // Draw the cardinal points
            if (i % 6 == 0) {
                String dirString = "";
                switch (i) {
                    case (0): {
                        dirString = northString;
                        break;
                    }
                    case (6):
                        dirString = eastString;
                        break;
                    case (12):
                        dirString = southString;
                        break;
                    case (18):
                        dirString = westString;
                        break;
                }
                canvas.drawText(dirString, cardinalX, cardinalY, textPaint);
            } else if (i % 3 == 0) {
                // Draw the text every alternate 45deg
                String angle = String.valueOf(i * 15);
                float angleTextWidth = textPaint.measureText(angle);
                int angleTextX = (int) (px - angleTextWidth / 2);
                int angleTextY = py - Radius + textHeight;
                canvas.drawText(angle, angleTextX, angleTextY, textPaint);
            }
            canvas.restore();

            canvas.rotate(15, px, py);
        }

    }

    Paint sunPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public void drawSun(Canvas canvas) {
        sunPaint.reset();
        sunPaint.setColor(Color.YELLOW);
        sunPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        // Horizontal sunPosition = new Horizontal(225, 45);

        if (sunPosition.getElevation() > -10) {
            canvas.rotate((float) sunPosition.getAzimuth() - 360, px, py);
            sunPaint.setPathEffect(dashPath);

            int ry = (int) (((90 - sunPosition.getElevation()) / 90) * Radius);
            canvas.drawCircle(px, py - ry, r, sunPaint);
            dashedPaint.setColor(Color.YELLOW);
            canvas.drawLine(px, py - Radius, px, py + Radius, dashedPaint);
            sunPaint.setPathEffect(null);
            canvas.restore();
        }

    }

    Paint moonPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    Paint moonPaintB = new Paint(Paint.ANTI_ALIAS_FLAG);
    Paint moonPaintO = new Paint(Paint.ANTI_ALIAS_FLAG);
    Paint moonPaintD = new Paint(Paint.ANTI_ALIAS_FLAG);
    RectF moonRect = new RectF();
    RectF moonOval = new RectF();

    public void drawMoon(Canvas canvas) {
        moonPaint.reset();
        moonPaint.setColor(Color.WHITE);
        moonPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        moonPaintB.reset();// moon Paint Black
        moonPaintB.setColor(Color.BLACK);
        moonPaintB.setStyle(Paint.Style.FILL_AND_STROKE);
        moonPaintO.reset();// moon Paint for Oval
        moonPaintO.setColor(Color.WHITE);
        moonPaintO.setStyle(Paint.Style.FILL_AND_STROKE);
        moonPaintD.reset();// moon Paint for Diameter
        // draw
        moonPaintD.setColor(Color.GRAY);
        moonPaintD.setStyle(Paint.Style.STROKE);
        double moonPhase = sunMoonPosition.getMoonPhase();
        if (moonPosition.getElevation() > -5) {
            canvas.rotate((float) moonPosition.getAzimuth() - 360, px, py);
            int eOffset = (int) ((moonPosition.getElevation() / 90) * Radius);
            // elevation Offset 0 for 0 degree; r for 90 degree
            moonRect.set(px - r, py + eOffset - Radius - r, px + r, py + eOffset - Radius + r);
            canvas.drawArc(moonRect, 90, 180, false, moonPaint);
            canvas.drawArc(moonRect, 270, 180, false, moonPaintB);
            int arcWidth = (int) ((moonPhase - 0.5) * (4 * r));
            moonPaintO.setColor(arcWidth < 0 ? Color.BLACK : Color.WHITE);
            moonOval.set(px - Math.abs(arcWidth) / 2, py + eOffset - Radius - r,
                    px + Math.abs(arcWidth) / 2, py + eOffset - Radius + r);
            canvas.drawArc(moonOval, 0, 360, false, moonPaintO);
            canvas.drawArc(moonRect, 0, 360, false, moonPaintD);
            moonPaintD.setPathEffect(dashPath);
            canvas.drawLine(px, py - Radius, px, py + Radius, moonPaintD);
            moonPaintD.setPathEffect(null);
            canvas.restore();

        }

    }

    Paint qiblaPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    Bitmap kaaba = BitmapFactory.decodeResource(getResources(), R.drawable.kaaba);

    public void drawQibla(Canvas canvas) {

        canvas.rotate((float) qiblaInfo.getHeading() - 360, px, py);
        qiblaPaint.reset();
        qiblaPaint.setColor(Color.GREEN);
        qiblaPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        qiblaPaint.setPathEffect(dashPath);
        qiblaPaint.setStrokeWidth(5.5f);

        canvas.drawLine(px, py - Radius, px, py + Radius, qiblaPaint);
        qiblaPaint.setPathEffect(null);
        canvas.drawBitmap(kaaba, px - kaaba.getWidth() / 2, py - Radius - kaaba.getHeight() / 2,
                qiblaPaint);
        canvas.restore();

    }

    public void setBearing(float _bearing) {
        bearing = _bearing;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setScreenResolution(int widthPixels, int heightPixels) {
        this.px = widthPixels / 2;
        this.py = heightPixels / 2;
    }
}