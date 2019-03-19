// copyedited from https://code.google.com/p/android-salat-times/source/browse/src/com/cepmuvakkit/times/view/QiblaCompassView.java
// licensed under GPLv3
package com.cepmuvakkit.times.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.utils.Utils;
import com.cepmuvakkit.times.posAlgo.AstroLib;
import com.cepmuvakkit.times.posAlgo.EarthHeading;
import com.cepmuvakkit.times.posAlgo.Horizontal;
import com.cepmuvakkit.times.posAlgo.SunMoonPosition;

import java.util.GregorianCalendar;

import androidx.annotation.ColorInt;
import androidx.core.content.ContextCompat;

public class QiblaCompassView extends View {
    @ColorInt
    int qiblaColor;
    // deliberately true
    boolean isCurrentlyNorth = true;
    boolean isCurrentlyEast = true;
    boolean isCurrentlyWest = true;
    boolean isCurrentlySouth = true;
    boolean isCurrentlyQibla = true;
    private Paint dashedPaint;
    private int px, py; // Center of Compass (px,py)
    private int radius; // radius of Compass dial
    private int r; // radius of Sun and Moon
    private String northString, eastString, southString, westString;
    private DashPathEffect dashPath;
    private float bearing;
    private EarthHeading qiblaInfo;
    private SunMoonPosition sunMoonPosition;
    private Horizontal sunPosition, moonPosition;
    private double longitude = 0.0;
    private double latitude = 0.0;
    private Paint textPaint;
    private Path mPath = new Path();
    private Paint trueNorthArrowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint markerPaint = new Paint(Paint.FAKE_BOLD_TEXT_FLAG);
    private Paint circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint sunPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint moonPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint moonPaintB = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint moonPaintO = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint moonPaintD = new Paint(Paint.ANTI_ALIAS_FLAG);
    private RectF moonRect = new RectF();
    private RectF moonOval = new RectF();
    private Paint qiblaPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Bitmap kaaba = BitmapFactory.decodeResource(getResources(), R.drawable.kaaba);

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

    static public boolean isNearToDegree(float angle, float compareTo) {
        float difference = Math.abs(angle - compareTo);
        return difference > 180 ? 360 - difference < 3f : difference < 3.f;
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
        qiblaColor = ContextCompat.getColor(getContext(), R.color.qibla_color);
        dashedPaint.setColor(qiblaColor);

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
        this.radius = Math.min(px - px / 12, py - py / 12);
        this.r = radius / 10; // Sun Moon radius;
        // over here
        qiblaInfo = sunMoonPosition.getDestinationHeading();
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setColor(qiblaColor);
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

    public void drawTrueNorthArrow(Canvas canvas, float drawnAngle) {
        trueNorthArrowPaint.reset();
        trueNorthArrowPaint.setColor(Color.RED);
        trueNorthArrowPaint.setStyle(Paint.Style.FILL);
        trueNorthArrowPaint.setAlpha(100);
        int r = radius / 12;
        // Construct a wedge-shaped path
        mPath.reset();
        mPath.moveTo(px, py - radius);
        mPath.lineTo(px - r, py);
        mPath.lineTo(px, py + r);
        mPath.lineTo(px + r, py);
        mPath.addCircle(px, py, r, Path.Direction.CCW);
        mPath.close();
        canvas.drawPath(mPath, trueNorthArrowPaint);
        dashedPaint.setColor(Color.RED);
        canvas.drawLine(px, py - radius, px, py + radius, dashedPaint);
        canvas.drawCircle(px, py, 5, dashedPaint);
        canvas.restore();
    }

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
        canvas.drawCircle(px, py, radius, circlePaint);
        canvas.drawCircle(px, py, radius - 20, circlePaint);
        // Rotate our perspective so that the "top" is
        // facing the current bearing.

        int textWidth = (int) textPaint.measureText("W");
        int cardinalX = px - textWidth / 2;
        int cardinalY = py - radius + textHeight;

        // Draw the marker every 15 degrees and text every 45.
        for (int i = 0; i < 24; i++) {
            // Draw a marker.
            canvas.drawLine(px, py - radius, px, py - radius + 10, markerPaint);
            canvas.save();
            canvas.translate(0, textHeight);
            // Draw the cardinal points
            if (i % 6 == 0) {
                String dirString = "";
                switch (i) {
                    case 0:
                        dirString = northString;
                        break;
                    case 6:
                        dirString = eastString;
                        break;
                    case 12:
                        dirString = southString;
                        break;
                    case 18:
                        dirString = westString;
                        break;
                }
                canvas.drawText(dirString, cardinalX, cardinalY, textPaint);
            } else if (i % 3 == 0) {
                // Draw the text every alternate 45deg
                String angle = String.valueOf(i * 15);
                float angleTextWidth = textPaint.measureText(angle);
                int angleTextX = (int) (px - angleTextWidth / 2);
                int angleTextY = py - radius + textHeight;
                canvas.drawText(angle, angleTextX, angleTextY, textPaint);
            }
            canvas.restore();

            canvas.rotate(15, px, py);
        }

    }

    public void drawSun(Canvas canvas) {
        sunPaint.reset();
        sunPaint.setColor(Color.YELLOW);
        sunPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        // Horizontal sunPosition = new Horizontal(225, 45);

        if (sunPosition.getElevation() > -10) {
            canvas.rotate((float) sunPosition.getAzimuth() - 360, px, py);
            sunPaint.setPathEffect(dashPath);

            int ry = (int) (((90 - sunPosition.getElevation()) / 90) * radius);
            canvas.drawCircle(px, py - ry, r, sunPaint);
            dashedPaint.setColor(Color.YELLOW);
            canvas.drawLine(px, py - radius, px, py + radius, dashedPaint);
            sunPaint.setPathEffect(null);
            canvas.restore();
        }

    }

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
            int eOffset = (int) ((moonPosition.getElevation() / 90) * radius);
            // elevation Offset 0 for 0 degree; r for 90 degree
            moonRect.set(px - r, py + eOffset - radius - r, px + r, py + eOffset - radius + r);
            canvas.drawArc(moonRect, 90, 180, false, moonPaint);
            canvas.drawArc(moonRect, 270, 180, false, moonPaintB);
            int arcWidth = (int) ((moonPhase - 0.5) * (4 * r));
            moonPaintO.setColor(arcWidth < 0 ? Color.BLACK : Color.WHITE);
            moonOval.set(px - Math.abs(arcWidth) / 2, py + eOffset - radius - r,
                    px + Math.abs(arcWidth) / 2, py + eOffset - radius + r);
            canvas.drawArc(moonOval, 0, 360, false, moonPaintO);
            canvas.drawArc(moonRect, 0, 360, false, moonPaintD);
            moonPaintD.setPathEffect(dashPath);
            canvas.drawLine(px, py - radius, px, py + radius, moonPaintD);
            moonPaintD.setPathEffect(null);
            canvas.restore();
        }
    }

    public void drawQibla(Canvas canvas) {
        canvas.rotate((float) qiblaInfo.getHeading() - 360, px, py);
        qiblaPaint.reset();
        qiblaPaint.setColor(Color.GREEN);
        qiblaPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        qiblaPaint.setPathEffect(dashPath);
        qiblaPaint.setStrokeWidth(5.5f);

        canvas.drawLine(px, py - radius, px, py + radius, qiblaPaint);
        qiblaPaint.setPathEffect(null);
        canvas.drawBitmap(kaaba, px - kaaba.getWidth() / 2, py - radius - kaaba.getHeight() / 2,
                qiblaPaint);
        canvas.restore();
    }

    public void setBearing(float bearing) {
        this.bearing = bearing;
        postInvalidate();
    }

    public void isOnDirectionAction() {
        Context context = getContext();
        if (context == null) return;

        // 0=North, 90=East, 180=South, 270=West
        if (isNearToDegree(bearing, 0)) {
            if (!isCurrentlyNorth) {
                Utils.a11yAnnounceAndClick(this, R.string.north);
                isCurrentlyNorth = true;
            }
        } else {
            isCurrentlyNorth = false;
        }

        if (isNearToDegree(bearing, 90)) {
            if (!isCurrentlyEast) {
                Utils.a11yAnnounceAndClick(this, R.string.east);
                isCurrentlyEast = true;
            }
        } else {
            isCurrentlyEast = false;
        }

        if (isNearToDegree(bearing, 180)) {
            if (!isCurrentlySouth) {
                Utils.a11yAnnounceAndClick(this, R.string.south);
                isCurrentlySouth = true;
            }
        } else {
            isCurrentlySouth = false;
        }

        if (isNearToDegree(bearing, 270)) {
            if (!isCurrentlyWest) {
                Utils.a11yAnnounceAndClick(this, R.string.west);
                isCurrentlyWest = true;
            }
        } else {
            isCurrentlyWest = false;
        }

        if (isLongLatAvailable() && qiblaInfo != null) {
            if (isNearToDegree(bearing, (float) qiblaInfo.getHeading())) {
                if (!isCurrentlyQibla) {
                    Utils.a11yAnnounceAndClick(this, R.string.qibla);
                    isCurrentlyQibla = true;
                }
            } else {
                isCurrentlyQibla = false;
            }
        }
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
