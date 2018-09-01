package net.androgames.level.painter;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.SurfaceHolder;

import com.byagowi.persiancalendar.R;

import net.androgames.level.orientation.Orientation;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

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
public class LevelPainter implements Runnable {

    /**
     * Etats du thread
     */
    private boolean initialized;
    private boolean wait;

    /**
     * Possesseur de la surface
     */
    private final SurfaceHolder surfaceHolder;

    /**
     * Dimensions
     */
    private int height;
    private int width;
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
    private int bubbleWidth;
    private int bubbleHeight;
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
    private int infoHeight;
    private int lcdWidth;
    private int lcdHeight;
    private int lockWidth;
    private int lockHeight;
    private int displayPadding;
    private int displayGap;
    private int infoY;
    private int sensorY;
    private int sensorGap;
    private int levelMaxDimension;

    /**
     * Rect
     */
    private Rect displayRect;
    private Rect lockRect;

    /**
     * Angles
     */
    private float angle1;
    private float angle2;
    private double n, teta, l;

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
     * Orientation
     */
    private Orientation orientation;

    /**
     * Bubble physics
     */
    private long currentTime;
    private long lastTime;
    private double timeDiff;
    private double posX;
    private double posY;
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
     * Info
     */
//	private String infoText;
    private String lockText;

    /**
     * Ajustement de la vitesse
     */
    private double viscosityValue = 1;

    /**
     * Format des angles
     */
    private DecimalFormat displayFormat;
    private String displayBackgroundText;

    /**
     * Fonts and colors
     */
    private static final String FONT_LCD = "fonts/lcd.ttf";
    private Paint lcdForegroundPaint;
    private Paint lcdBackgroundPaint;
    private Paint lockForegroundPaint;
    private Paint lockBackgroundPaint;
    private Paint infoPaint;
    private int backgroundColor;

    /**
     * Config angles
     */
    private boolean showAngle;
//    private DisplayType angleType;

    /**
     * Locked
     */
    private static final String LOCKED = "LOCKED";
    private static final String LOCKED_BACKGROUND = "888888";
    private boolean lockEnabled;
    private boolean locked;

    /**
     * Animation
     */
    private boolean ecoMode;
    private final Handler handler;
    private long frameRate;

    public LevelPainter(SurfaceHolder surfaceHolder, Context context,
                        Handler handler,
                        boolean showAngle, boolean lockEnabled,
                        boolean ecoMode) {

        // get handles to some important objects
        this.surfaceHolder = surfaceHolder;

        // economy mode
        this.ecoMode = ecoMode;
        this.handler = handler;
        this.frameRate = 1000 / context.getResources().getInteger(R.integer.frame_rate);

        // drawable
        this.level1D = context.getResources().getDrawable(R.drawable.level_1d);
        this.level2D = context.getResources().getDrawable(R.drawable.level_2d);
        this.bubble1D = context.getResources().getDrawable(R.drawable.bubble_1d);
        this.bubble2D = context.getResources().getDrawable(R.drawable.bubble_2d);
        this.marker1D = context.getResources().getDrawable(R.drawable.marker_1d);
        this.marker2D = context.getResources().getDrawable(R.drawable.marker_2d);
        this.display = context.getResources().getDrawable(R.drawable.display);

        // config
        this.showAngle = showAngle;
        this.displayFormat = new DecimalFormat("00.0");
        this.displayFormat.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.ENGLISH));
        this.displayBackgroundText = "88.8";

        // colors
        this.backgroundColor = context.getResources().getColor(R.color.silver);

        // strings
//        this.infoText = context.getString(R.string.calibrate_info);
//        this.lockText = context.getString(R.string.lock_info);

        // typeface
        Typeface lcd = Typeface.createFromAsset(context.getAssets(), FONT_LCD);

        // paint
        this.infoPaint = new Paint();
        this.infoPaint.setColor(context.getResources().getColor(R.color.black));
        this.infoPaint.setAntiAlias(true);
        this.infoPaint.setTextSize(context.getResources().getDimensionPixelSize(R.dimen.info_text));
        this.infoPaint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL));
        this.infoPaint.setTextAlign(Paint.Align.CENTER);

        this.lcdForegroundPaint = new Paint();
        this.lcdForegroundPaint.setColor(context.getResources().getColor(R.color.lcd_front));
        this.lcdForegroundPaint.setAntiAlias(true);
        this.lcdForegroundPaint.setTextSize(context.getResources().getDimensionPixelSize(R.dimen.lcd_text));
        this.lcdForegroundPaint.setTypeface(lcd);
        this.lcdForegroundPaint.setTextAlign(Paint.Align.CENTER);

        this.lcdBackgroundPaint = new Paint();
        this.lcdBackgroundPaint.setColor(context.getResources().getColor(R.color.lcd_back));
        this.lcdBackgroundPaint.setAntiAlias(true);
        this.lcdBackgroundPaint.setTextSize(context.getResources().getDimensionPixelSize(R.dimen.lcd_text));
        this.lcdBackgroundPaint.setTypeface(lcd);
        this.lcdBackgroundPaint.setTextAlign(Paint.Align.CENTER);

        this.lockForegroundPaint = new Paint();
        this.lockForegroundPaint.setColor(context.getResources().getColor(R.color.lock_front));
        this.lockForegroundPaint.setAntiAlias(true);
        this.lockForegroundPaint.setTextSize(context.getResources().getDimensionPixelSize(R.dimen.lock_text));
        this.lockForegroundPaint.setTypeface(lcd);
        this.lockForegroundPaint.setTextAlign(Paint.Align.CENTER);

        this.lockBackgroundPaint = new Paint();
        this.lockBackgroundPaint.setColor(context.getResources().getColor(R.color.lock_back));
        this.lockBackgroundPaint.setAntiAlias(true);
        this.lockBackgroundPaint.setTextSize(context.getResources().getDimensionPixelSize(R.dimen.lock_text));
        this.lockBackgroundPaint.setTypeface(lcd);
        this.lockBackgroundPaint.setTextAlign(Paint.Align.CENTER);

        // dimens
        Rect rect = new Rect();
//    	this.infoPaint.getTextBounds(infoText, 0, infoText.length(), rect);
        this.infoHeight = rect.height();
        this.lcdBackgroundPaint.getTextBounds(displayBackgroundText, 0, displayBackgroundText.length(), rect);
        this.lcdHeight = rect.height();
        this.lcdWidth = rect.width();
        this.lockBackgroundPaint.getTextBounds(LOCKED, 0, LOCKED.length(), rect);
        this.lockHeight = rect.height();
        this.lockWidth = rect.width();
        this.levelBorderWidth = context.getResources().getDimensionPixelSize(R.dimen.level_border_width);
        this.levelBorderHeight = context.getResources().getDimensionPixelSize(R.dimen.level_border_height);
        this.markerThickness = context.getResources().getDimensionPixelSize(R.dimen.marker_thickness);
        this.displayGap = context.getResources().getDimensionPixelSize(R.dimen.display_gap);
        this.sensorGap = context.getResources().getDimensionPixelSize(R.dimen.sensor_gap);
        this.displayPadding = context.getResources().getDimensionPixelSize(R.dimen.display_padding);
        this.displayRect = new Rect();
        this.lockRect = new Rect();

        // init
        this.locked = false;
        this.lockEnabled = lockEnabled;
        this.orientation = Orientation.TOP;
        this.wait = true;
        this.initialized = false;
    }

    public void clean() {
        // suppression des ressources
        // afin de bypasser les problemes
        // de cache des xml drawable
        synchronized (this.surfaceHolder) {
            level1D = null;
            level2D = null;
            bubble1D = null;
            bubble2D = null;
            marker1D = null;
            marker2D = null;
            display = null;
        }
    }

    @Override
    public void run() {
        Canvas c = null;
        updatePhysics();
        try {
            c = this.surfaceHolder.lockCanvas(null);
            if (c != null) {
                synchronized (this.surfaceHolder) {
                    doDraw(c);
                }
            }
        } finally {
            // do this in a finally so that if an exception is thrown
            // during the above, we don't leave the Surface in an
            // inconsistent state
            if (c != null) {
                this.surfaceHolder.unlockCanvasAndPost(c);
            }
        }
        // lancement du traitement differe en mode eco
        handler.removeCallbacks(this);
        if (!wait && !ecoMode) {
            handler.postDelayed(this, frameRate - System.currentTimeMillis() + lastTime);
        }
    }

    /**
     * Mise en pause du thread
     */
    public void pause(boolean paused) {
        wait = !initialized || paused;
        // si on est en mode eco et
        // que la pause est supprimee
        // relance du traitement
        if (!wait) {
            handler.postDelayed(this, frameRate);
        }
    }

    /**
     * Modification / initialisation de la taille de l'ecran
     */
    public void setSurfaceSize(int width, int height) {
        canvasWidth = width;
        canvasHeight = height;

        levelMaxDimension = Math.min(
                Math.min(height, width) - 2 * displayGap,
                Math.max(height, width) - 2 * (sensorGap + 2 * infoHeight + 3 * displayGap + lcdHeight));

        setOrientation(orientation);
    }

    private void updatePhysics() {
        currentTime = System.currentTimeMillis();
        if (ecoMode) {
            switch (orientation) {
                case LANDING:
                    y = (angleY * levelMinusBubbleHeight + minLevelY + maxLevelY) / 2;
                default:
                    x = (angleX * levelMinusBubbleWidth + minLevelX + maxLevelX) / 2;
            }
        } else {
            if (lastTime > 0) {
                timeDiff = (currentTime - lastTime) / 1000.0;
                posX = orientation.getReverse() * (2 * x - minLevelX - maxLevelX) / levelMinusBubbleWidth;
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
                        posY = (2 * y - minLevelY - maxLevelY) / levelMinusBubbleHeight;
                        speedX = (angleX - posX) * viscosityValue;
                        speedY = (angleY - posY) * viscosityValue;
                        y += speedY * timeDiff;
                        break;
                }
                x += speedX * timeDiff;
                // en cas de latence elevee
                // si la bubble a trop deviee
                // elle est replacee correctement
                switch (orientation) {
                    case LANDING:
                        if (Math.sqrt((middleX - x) * (middleX - x)
                                + (middleY - y) * (middleY - y)) > levelMaxDimension / 2 - halfBubbleWidth) {
                            x = (angleX * levelMinusBubbleWidth + minLevelX + maxLevelX) / 2;
                            y = (angleY * levelMinusBubbleHeight + minLevelY + maxLevelY) / 2;
                        }
                        break;
                    default:
                        if (x < minLevelX + halfBubbleWidth || x > maxLevelX - halfBubbleWidth) {
                            x = (angleX * levelMinusBubbleWidth + minLevelX + maxLevelX) / 2;
                        }
                }
            }
        }
        lastTime = currentTime;
    }

    private void doDraw(Canvas canvas) {
        canvas.save();

        canvas.drawColor(backgroundColor);

        switch (orientation) {
            case LANDING:
//	    		canvas.drawText(infoText, middleX, infoY, infoPaint);
//                if (lockEnabled) {
//                    display.setBounds(lockRect);
//                    display.draw(canvas);
//                    canvas.drawText(
//                            LOCKED_BACKGROUND,
//                            middleX,
//                            lockRect.centerY() + lockHeight / 2,
//                            lockBackgroundPaint);
//                    canvas.drawText(lockText, middleX, lockRect.bottom + displayGap, infoPaint);
//                    if (locked) {
//                        canvas.drawText(
//                                LOCKED,
//                                middleX,
//                                lockRect.centerY() + lockHeight / 2,
//                                lockForegroundPaint);
//                    }
//                }
                if (showAngle) {
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
                            displayFormat.format(angle2),
                            middleX - (displayRect.width() + displayGap) / 2,
                            displayRect.centerY() + lcdHeight / 2,
                            lcdForegroundPaint);
                    canvas.drawText(
                            displayBackgroundText,
                            middleX + (displayRect.width() + displayGap) / 2,
                            displayRect.centerY() + lcdHeight / 2,
                            lcdBackgroundPaint);
                    canvas.drawText(
                            displayFormat.format(angle1),
                            middleX + (displayRect.width() + displayGap) / 2,
                            displayRect.centerY() + lcdHeight / 2,
                            lcdForegroundPaint);
                }
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
                break;
            default:
                canvas.rotate(orientation.getRotation(), middleX, middleY);
//	    		canvas.drawText(infoText, middleX, infoY, infoPaint);
//                if (lockEnabled) {
//                    display.setBounds(lockRect);
//                    display.draw(canvas);
//                    canvas.drawText(
//                            LOCKED_BACKGROUND,
//                            middleX,
//                            lockRect.centerY() + lockHeight / 2,
//                            lockBackgroundPaint);
//                    canvas.drawText(lockText, middleX, lockRect.bottom + displayGap, infoPaint);
//                    if (locked) {
//                        canvas.drawText(
//                                LOCKED,
//                                middleX,
//                                lockRect.centerY() + lockHeight / 2,
//                                lockForegroundPaint);
//                    }
//                }
                if (showAngle) {
                    display.setBounds(displayRect);
                    display.draw(canvas);
                    canvas.drawText(
                            displayBackgroundText,
                            middleX,
                            displayRect.centerY() + lcdHeight / 2,
                            lcdBackgroundPaint);
                    canvas.drawText(
                            displayFormat.format(angle1),
                            middleX,
                            displayRect.centerY() + lcdHeight / 2,
                            lcdForegroundPaint);
                }
                // level
                level1D.draw(canvas);
                // bubble
                canvas.clipRect(
                        minLevelX + levelBorderWidth,
                        minLevelY + levelBorderHeight,
                        maxLevelX - levelBorderWidth,
                        maxLevelY - levelBorderHeight);
                bubble1D.setBounds(
                        (int) (x - halfBubbleWidth),
                        minBubble,
                        (int) (x + halfBubbleWidth),
                        maxBubble);
                bubble1D.draw(canvas);
                // marker
                marker1D.setBounds(
                        middleX - halfMarkerGap - markerThickness,
                        minLevelY,
                        middleX - halfMarkerGap,
                        maxLevelY);
                marker1D.draw(canvas);
                marker1D.setBounds(
                        middleX + halfMarkerGap,
                        minLevelY,
                        middleX + halfMarkerGap + markerThickness,
                        maxLevelY);
                marker1D.draw(canvas);
                break;
        }

        canvas.restore();
    }

    private void setOrientation(Orientation newOrientation) {
        if (!(lockEnabled && locked) || !initialized) {
            synchronized (this.surfaceHolder) {
                orientation = newOrientation;

                switch (newOrientation) {
                    case LEFT:        // left
                    case RIGHT:    // right
                        height = canvasWidth;
                        width = canvasHeight;
                        infoY = (canvasHeight - canvasWidth) / 2 + canvasWidth - infoHeight;
                        break;
                    case TOP:        // top
                    case BOTTOM:    // bottom
                    default:        // landing
                        height = canvasHeight;
                        width = canvasWidth;
                        infoY = canvasHeight - infoHeight;
                        break;
                }

                sensorY = infoY - infoHeight - sensorGap;

                middleX = canvasWidth / 2;
                middleY = canvasHeight / 2;

                // level
                switch (newOrientation) {
                    case LANDING:    // landing
                        levelWidth = levelMaxDimension;
                        levelHeight = levelMaxDimension;
                        break;
                    case TOP:        // top
                    case BOTTOM:    // bottom
                    case LEFT:        // left
                    case RIGHT:    // right
                        levelWidth = width - 2 * displayGap;
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
                bubbleWidth = 2 * halfBubbleWidth;
                bubbleHeight = 2 * halfBubbleHeight;
                maxBubble = (int) (maxLevelY - bubbleHeight * BUBBLE_CROPPING);
                minBubble = maxBubble - bubbleHeight;

                // display
                displayRect.set(
                        middleX - lcdWidth / 2 - displayPadding,
                        sensorY - displayGap - 2 * displayPadding - lcdHeight - infoHeight / 2,
                        middleX + lcdWidth / 2 + displayPadding,
                        sensorY - displayGap - infoHeight / 2);

                // lock
                lockRect.set(
                        middleX - lockWidth / 2 - displayPadding,
                        middleY - height / 2 + displayGap,
                        middleX + lockWidth / 2 + displayPadding,
                        middleY - height / 2 + displayGap + 2 * displayPadding + lockHeight);

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
                if (!initialized) {
                    initialized = true;
                    pause(false);
                }
            }
        }
    }

    public void onOrientationChanged(Orientation newOrientation, float newPitch, float newRoll, float newBalance) {
        if (!orientation.equals(newOrientation)) {
            setOrientation(newOrientation);
        }
        if (!wait) {
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
                n = Math.sqrt(angleX * angleX + angleY * angleY);
                teta = Math.acos(Math.abs(angleX) / n);
                l = 1 / Math.max(Math.abs(Math.cos(teta)), Math.abs(Math.sin(teta)));
                angleX = angleX / l;
                angleY = angleY / l;
            }
            // lancement de l'animation si mode eco
            if (ecoMode) {
                handler.post(this);
            }
        }
    }
}
