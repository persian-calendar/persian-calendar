package net.androgames.level.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import com.byagowi.persiancalendar.R;

import net.androgames.level.orientation.Orientation;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import static com.byagowi.persiancalendar.utils.UtilsKt.a11yAnnounceAndClick;

/*
 *  This file is part of Level (an Android Bubble Level).
 *  <https://github.com/avianey/Level>
 *
 *  Copyright (C) 2014 Antoine Vianey
 *
 *  Level is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Level is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Level. If not, see <http://www.gnu.org/licenses/>
 */
public class LevelView extends View {
    private static final double LEVEL_ASPECT_RATIO = 0.150;
    private static final double BUBBLE_WIDTH = 0.150;
    private static final double BUBBLE_ASPECT_RATIO = 1.000;
    private static final double BUBBLE_CROPPING = 0.500;
    private static final double MARKER_GAP = BUBBLE_WIDTH + 0.020;
    /**
     * Angle max
     */
    private static final double MAX_SINUS = Math.sin(Math.PI / 4);
    /**
     * Fonts and colors
     */
    private static final String FONT_LCD = "fonts/lcd.ttf";
    private boolean isAlreadyLeveled = true; // deliberately
    /**
     * Dimensions
     */
    private int canvasWidth;
    private int canvasHeight;
    private int minLevelX;
    private int maxLevelX;
    private int levelWidth;
    private int levelHeight;
    private int levelMinusBubbleWidth;
    private int levelMinusBubbleHeight;
    private int middleX;
    private int middleY;
    private int halfBubbleWidth;
    private int halfBubbleHeight;
    private int halfMarkerGap;
    private int minLevelY;
    private int maxLevelY;
    private int minBubble;
    private int maxBubble;
    private int markerThickness;
    private int levelBorderWidth;
    private int levelBorderHeight;
    private int lcdWidth;
    private int lcdHeight;
    private int displayPadding;
    private int displayGap;
    private int sensorGap;
    private int levelMaxDimension;
    /**
     * Rect
     */
    private final Rect displayRect = new Rect();
    /**
     * Angles
     */
    private float angle1;
    private float angle2;
    /**
     * Orientation
     */
    private Orientation orientation;
    private long lastTime;
    private long lastTimeShowAngle;
    private double angleToShow1;
    private double angleToShow2;
    private double angleX;
    private double angleY;
    private double speedX;
    private double speedY;
    private double x, y;
    /**
     * Drawables
     */
    private Drawable level1D;
    private Drawable bubble1D;
    private Drawable marker1D;
    private Drawable level2D;
    private Drawable bubble2D;
    private Drawable marker2D;
    private Drawable display;
    /**
     * Ajustement de la vitesse
     */
    private double viscosityValue = 1;
    /**
     * Format des angles
     */
    private final DecimalFormat displayFormat = new DecimalFormat("00.0");
    private static final String displayBackgroundText = "88.8";
    private final Paint lcdForegroundPaint = new Paint();
    private final Paint lcdBackgroundPaint = new Paint();
    private final Paint infoPaint = new Paint();
    private boolean firstTime = true;

    public LevelView(Context context) {
        super(context);
        init(context);
    }

    public LevelView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        Resources resources = context.getResources();
        // drawable
        level1D = ResourcesCompat.getDrawable(resources, R.drawable.level_1d, null);
        level2D = ResourcesCompat.getDrawable(resources, R.drawable.level_2d, null);
        bubble1D = ResourcesCompat.getDrawable(resources, R.drawable.bubble_1d, null);
        bubble2D = ResourcesCompat.getDrawable(resources, R.drawable.bubble_2d, null);
        marker1D = ResourcesCompat.getDrawable(resources, R.drawable.marker_1d, null);
        marker2D = ResourcesCompat.getDrawable(resources, R.drawable.marker_2d, null);
        display = ResourcesCompat.getDrawable(resources, R.drawable.display, null);

        // config
        displayFormat.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.ENGLISH));

        // typeface
        Typeface lcd = Typeface.createFromAsset(context.getAssets(), FONT_LCD);

        // paint
        infoPaint.setColor(resources.getColor(R.color.black));
        infoPaint.setAntiAlias(true);
        infoPaint.setTextSize(resources.getDimensionPixelSize(R.dimen.info_text));
        infoPaint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL));
        infoPaint.setTextAlign(Paint.Align.CENTER);

        lcdForegroundPaint.setColor(resources.getColor(R.color.lcd_front));
        lcdForegroundPaint.setAntiAlias(true);
        lcdForegroundPaint.setTextSize(resources.getDimensionPixelSize(R.dimen.lcd_text));
        lcdForegroundPaint.setTypeface(lcd);
        lcdForegroundPaint.setTextAlign(Paint.Align.CENTER);

        lcdBackgroundPaint.setColor(resources.getColor(R.color.lcd_back));
        lcdBackgroundPaint.setAntiAlias(true);
        lcdBackgroundPaint.setTextSize(resources.getDimensionPixelSize(R.dimen.lcd_text));
        lcdBackgroundPaint.setTypeface(lcd);
        lcdBackgroundPaint.setTextAlign(Paint.Align.CENTER);

        // dimens
        lcdBackgroundPaint.getTextBounds(displayBackgroundText, 0, displayBackgroundText.length(), displayRect);
        lcdHeight = displayRect.height();
        lcdWidth = displayRect.width();
        levelBorderWidth = resources.getDimensionPixelSize(R.dimen.level_border_width);
        levelBorderHeight = resources.getDimensionPixelSize(R.dimen.level_border_height);
        markerThickness = resources.getDimensionPixelSize(R.dimen.marker_thickness);
        displayGap = resources.getDimensionPixelSize(R.dimen.display_gap);
        sensorGap = resources.getDimensionPixelSize(R.dimen.sensor_gap);
        displayPadding = resources.getDimensionPixelSize(R.dimen.display_padding);

        invalidate();
    }

    public void setOrientation(Orientation newOrientation, float newPitch, float newRoll, float newBalance) {
        if (orientation == null || !orientation.equals(newOrientation)) {
            orientation = newOrientation;

            int infoY;
            switch (newOrientation) {
                case LEFT:      // left
                case RIGHT:     // right
                    infoY = (canvasHeight - canvasWidth) / 2 + canvasWidth;
                    break;
                case TOP:       // top
                case BOTTOM:    // bottom
                default:        // landing
                    infoY = canvasHeight;
                    break;
            }

            int sensorY = infoY - sensorGap;

            middleX = canvasWidth / 2;
            middleY = canvasHeight / 2;

            // level
            switch (newOrientation) {
                case LANDING:   // landing
                    levelWidth = levelMaxDimension;
                    levelHeight = levelMaxDimension;
                    break;
                case TOP:       // top
                case BOTTOM:    // bottom
                case LEFT:      // left
                case RIGHT:     // right
                    levelWidth = canvasWidth - 2 * displayGap;
                    levelHeight = (int) (levelWidth * LEVEL_ASPECT_RATIO);
                    break;
            }

            viscosityValue = levelWidth;

            minLevelX = middleX - levelWidth / 2;
            maxLevelX = middleX + levelWidth / 2;
            minLevelY = middleY - levelHeight / 2;
            maxLevelY = middleY + levelHeight / 2;

            // bubble
            halfBubbleWidth = (int) (levelWidth * BUBBLE_WIDTH / 2);
            halfBubbleHeight = (int) (halfBubbleWidth * BUBBLE_ASPECT_RATIO);
            int bubbleWidth = 2 * halfBubbleWidth;
            int bubbleHeight = 2 * halfBubbleHeight;
            maxBubble = (int) (maxLevelY - bubbleHeight * BUBBLE_CROPPING);
            minBubble = maxBubble - bubbleHeight;

            // display
            displayRect.set(
                    middleX - lcdWidth / 2 - displayPadding,
                    sensorY - displayGap - 2 * displayPadding - lcdHeight,
                    middleX + lcdWidth / 2 + displayPadding,
                    sensorY - displayGap);

            // marker
            halfMarkerGap = (int) (levelWidth * MARKER_GAP / 2);

            // autres
            levelMinusBubbleWidth = levelWidth - bubbleWidth - 2 * levelBorderWidth;
            levelMinusBubbleHeight = levelHeight - bubbleHeight - 2 * levelBorderWidth;

            // positionnement
            level1D.setBounds(minLevelX, minLevelY, maxLevelX, maxLevelY);
            level2D.setBounds(minLevelX, minLevelY, maxLevelX, maxLevelY);
            marker2D.setBounds(
                    middleX - halfMarkerGap - markerThickness,
                    middleY - halfMarkerGap - markerThickness,
                    middleX + halfMarkerGap + markerThickness,
                    middleY + halfMarkerGap + markerThickness);

            x = ((double) (maxLevelX + minLevelX)) / 2;
            y = ((double) (maxLevelY + minLevelY)) / 2;
        }
        switch (orientation) {
            case TOP:
            case BOTTOM:
                angle1 = Math.abs(newBalance);
                angleX = Math.sin(Math.toRadians(newBalance)) / MAX_SINUS;
                break;
            case LANDING:
                angle2 = Math.abs(newRoll);
                angleX = Math.sin(Math.toRadians(newRoll)) / MAX_SINUS;
            case RIGHT:
            case LEFT:
                angle1 = Math.abs(newPitch);
                angleY = Math.sin(Math.toRadians(newPitch)) / MAX_SINUS;
                if (angle1 > 90) {
                    angle1 = 180 - angle1;
                }
                break;
        }
        // correction des angles affiches
        if (angle1 > 99.9f) {
            angle1 = 99.9f;
        }
        if (angle2 > 99.9f) {
            angle2 = 99.9f;
        }
        // correction des angles aberrants
        // pour ne pas que la bulle sorte de l'ecran
        if (angleX > 1) {
            angleX = 1;
        } else if (angleX < -1) {
            angleX = -1;
        }
        if (angleY > 1) {
            angleY = 1;
        } else if (angleY < -1) {
            angleY = -1;
        }
        // correction des angles a plat
        // la bulle ne doit pas sortir du niveau
        if (orientation.equals(Orientation.LANDING) && angleX != 0 && angleY != 0) {
            double n = Math.sqrt(angleX * angleX + angleY * angleY);
            double teta = Math.acos(Math.abs(angleX) / n);
            double l = 1 / Math.max(Math.abs(Math.cos(teta)), Math.abs(Math.sin(teta)));
            angleX = angleX / l;
            angleY = angleY / l;
        }

        if (orientation.isLevel(newPitch, newRoll, newBalance, .8f)) {
            if (!isAlreadyLeveled) {
                a11yAnnounceAndClick(this, R.string.level);
                isAlreadyLeveled = true;
            }
        } else {
            isAlreadyLeveled = false;
        }

        postInvalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        canvasWidth = w;
        canvasHeight = h;

        levelMaxDimension = Math.min(
                Math.min(h, w) - 2 * displayGap,
                Math.max(h, w) - 2 * (sensorGap + 3 * displayGap + lcdHeight));
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.save();

        if (firstTime) {
            setOrientation(Orientation.LANDING, 0, 0, 0);
            firstTime = false;
        }

        // update physics
        long currentTime = System.currentTimeMillis();
        double timeDiff = (currentTime - lastTime) / 1000.0;
        lastTime = currentTime;

        if (currentTime - lastTimeShowAngle > 500) {
            angleToShow1 = angle1;
            angleToShow2 = angle2;
            lastTimeShowAngle = currentTime;
        }

        double posX = orientation.getReverse() * (2 * x - minLevelX - maxLevelX) / levelMinusBubbleWidth;
        switch (orientation) {
            case TOP:
            case BOTTOM:
                speedX = orientation.getReverse() * (angleX - posX) * viscosityValue;
                break;
            case LEFT:
            case RIGHT:
                speedX = orientation.getReverse() * (angleY - posX) * viscosityValue;
                break;
            case LANDING:
                double posY = (2 * y - minLevelY - maxLevelY) / levelMinusBubbleHeight;
                speedX = (angleX - posX) * viscosityValue;
                speedY = (angleY - posY) * viscosityValue;
                y += speedY * timeDiff;
                break;
        }
        x += speedX * timeDiff;
        // en cas de latence elevee
        // si la bubble a trop deviee
        // elle est replacee correctement
        if (orientation == Orientation.LANDING) {
            if (Math.sqrt((middleX - x) * (middleX - x)
                    + (middleY - y) * (middleY - y)) > levelMaxDimension / 2 - halfBubbleWidth) {
                x = (angleX * levelMinusBubbleWidth + minLevelX + maxLevelX) / 2;
                y = (angleY * levelMinusBubbleHeight + minLevelY + maxLevelY) / 2;
            }
        } else {
            if (x < minLevelX + halfBubbleWidth || x > maxLevelX - halfBubbleWidth) {
                x = (angleX * levelMinusBubbleWidth + minLevelX + maxLevelX) / 2;
            }
        }

        if (orientation == Orientation.LANDING) {
            // Angle
            display.setBounds(
                    displayRect.left - (displayRect.width() + displayGap) / 2,
                    displayRect.top,
                    displayRect.right - (displayRect.width() + displayGap) / 2,
                    displayRect.bottom);
            display.draw(canvas);
            display.setBounds(
                    displayRect.left + (displayRect.width() + displayGap) / 2,
                    displayRect.top,
                    displayRect.right + (displayRect.width() + displayGap) / 2,
                    displayRect.bottom);
            display.draw(canvas);
            canvas.drawText(
                    displayBackgroundText,
                    middleX - (displayRect.width() + displayGap) / 2,
                    displayRect.centerY() + lcdHeight / 2,
                    lcdBackgroundPaint);
            canvas.drawText(
                    displayFormat.format(angleToShow2),
                    middleX - (displayRect.width() + displayGap) / 2,
                    displayRect.centerY() + lcdHeight / 2,
                    lcdForegroundPaint);
            canvas.drawText(
                    displayBackgroundText,
                    middleX + (displayRect.width() + displayGap) / 2,
                    displayRect.centerY() + lcdHeight / 2,
                    lcdBackgroundPaint);
            canvas.drawText(
                    displayFormat.format(angleToShow1),
                    middleX + (displayRect.width() + displayGap) / 2,
                    displayRect.centerY() + lcdHeight / 2,
                    lcdForegroundPaint);
            //
            bubble2D.setBounds(
                    (int) (x - halfBubbleWidth),
                    (int) (y - halfBubbleHeight),
                    (int) (x + halfBubbleWidth),
                    (int) (y + halfBubbleHeight));
            level2D.draw(canvas);
            bubble2D.draw(canvas);
            marker2D.draw(canvas);
            canvas.drawLine(minLevelX, middleY,
                    middleX - halfMarkerGap, middleY, infoPaint);
            canvas.drawLine(middleX + halfMarkerGap, middleY,
                    maxLevelX, middleY, infoPaint);
            canvas.drawLine(middleX, minLevelY,
                    middleX, middleY - halfMarkerGap, infoPaint);
            canvas.drawLine(middleX, middleY + halfMarkerGap,
                    middleX, maxLevelY, infoPaint);
        } else {
            canvas.rotate(orientation.getRotation(), middleX, middleY);
            // Angle
            display.setBounds(displayRect);
            display.draw(canvas);
            canvas.drawText(
                    displayBackgroundText, middleX,
                    displayRect.centerY() + lcdHeight / 2, lcdBackgroundPaint);
            canvas.drawText(
                    displayFormat.format(angleToShow1), middleX,
                    displayRect.centerY() + lcdHeight / 2, lcdForegroundPaint);
            // level
            level1D.draw(canvas);
            // bubble
            canvas.clipRect(
                    minLevelX + levelBorderWidth, minLevelY + levelBorderHeight,
                    maxLevelX - levelBorderWidth, maxLevelY - levelBorderHeight);
            bubble1D.setBounds(
                    (int) (x - halfBubbleWidth), minBubble,
                    (int) (x + halfBubbleWidth), maxBubble);
            bubble1D.draw(canvas);
            // marker
            marker1D.setBounds(
                    middleX - halfMarkerGap - markerThickness, minLevelY,
                    middleX - halfMarkerGap, maxLevelY);
            marker1D.draw(canvas);
            marker1D.setBounds(
                    middleX + halfMarkerGap, minLevelY,
                    middleX + halfMarkerGap + markerThickness, maxLevelY);
            marker1D.draw(canvas);
        }

        canvas.restore();
    }
}