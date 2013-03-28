package com.byagowi.persiancalendar;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

public class CompassView extends View {

	private Paint paint;
	private double north = 0;
	private double qibla = 0;
	private boolean everQiblaSet = false;

	public CompassView(Context context) {
		super(context);
		init();
	}

	private void init() {
		paint = new Paint();
		paint.setAntiAlias(true);
		paint.setStrokeWidth(2);
		paint.setTextSize(25);
		paint.setStyle(Paint.Style.STROKE);
		paint.setColor(Color.BLACK);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		int xPoint = getMeasuredWidth() / 2;
		int yPoint = getMeasuredHeight() / 2;

		float radius = (float) (Math.max(xPoint, yPoint) * 0.6);
		canvas.drawCircle(xPoint, yPoint, radius, paint);

		canvas.drawRect(0, 0, getMeasuredWidth(), getMeasuredHeight(), paint);
		
		canvas.drawLine(xPoint, yPoint,
				(float) (xPoint + radius * Math.sin(-north / 180 * Math.PI)),
				(float) (yPoint - radius * Math.cos(-north / 180 * Math.PI)),
				paint);

		if (everQiblaSet == true) {
			canvas.drawLine(
					xPoint,
					yPoint,
					(float) (xPoint + radius
							* Math.sin((-north + qibla) / 180 * Math.PI)),
					(float) (yPoint - radius
							* Math.cos((-north + qibla) / 180 * Math.PI)),
					paint);
		}

		// canvas.drawText(String.valueOf(north), xPoint, yPoint, paint);
	}

	public void setQibla(double position) {
		everQiblaSet = true;
		qibla = position;
		invalidate();
	}

	public void setPosition(double position) {
		north = position;
		invalidate();
	}

}