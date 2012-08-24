package com.byagowi.persiancalendar;

import android.view.GestureDetector;
import android.view.MotionEvent;

class CalendarTableGestureListener extends
		GestureDetector.SimpleOnGestureListener {
	/**
	 * 
	 */
	private final CalendarActivity calendarActivity;

	/**
	 * @param calendarActivity
	 */
	CalendarTableGestureListener(CalendarActivity calendarActivity) {
		this.calendarActivity = calendarActivity;
	}

	private static final int SWIPE_MIN_DISTANCE = 40;
	private static final int SWIPE_THRESHOLD_VELOCITY = 200;

	@Override
	public boolean onDown(MotionEvent e) {
		return true;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		if (Math.abs(e1.getX() - e2.getX()) > Math.abs(e1.getY()
				- e2.getY())) {
			if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
					&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
				this.calendarActivity.previousMonth();
			} else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
					&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
				this.calendarActivity.nextMonth();
			}
		} else {
			if (e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE
					&& Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
				this.calendarActivity.nextYear();
			} else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE
					&& Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
				this.calendarActivity.previousYear();
			}
		}
		return super.onFling(e1, e2, velocityX, velocityY);
	}
}