package com.byagowi.persiancalendar;

import calendar.CivilDate;
import calendar.DateConverter;
import calendar.DayOutOfRangeException;
import calendar.PersianDate;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.widget.TextView;

/**
 * Program activity for android.
 * 
 * @author ebraminio
 * 
 */
public class PersianCalendarActivity extends Activity {

	int currentPersianYear = 0;
	int currentPersianMonth = 0;
	TextView currentMonthTextView = null;
	PersianDate nowDate = null;
	

    private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
    
    SimpleOnGestureListener simpleOnGestureListener = new SimpleOnGestureListener() {
    	@Override
    	public boolean onDown(MotionEvent e) {
    	    return true;        
    	}
    	
		@Override
	    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
	        try {
	            if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
	                return false;
	            // right to left swipe
	            if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
	            	nextMonth(null);
	            }  else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
					previousMonth(null);
	            }
	        } catch (Exception e) {
	            // nothing
	        }
	        return super.onFling(e1, e2, velocityX, velocityY);
	    }
	};
    
	// ViewFlipper viewFlipper = null;
	View.OnTouchListener gestureListener;
	GestureDetector gestureDetector;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.calendar);

		currentMonthTextView = (TextView) findViewById(R.id.currentMonthTextView);
		/*
		 * ViewFlipper viewFlipper = (ViewFlipper) findViewById(R.id.flipper);
		 * viewFlipper.setAnimation(AnimationUtils.loadAnimation(this,
		 * android.R.anim.fade_in));
		 */

		StringBuilder sb = new StringBuilder();
		CivilDate civil = new CivilDate();
		nowDate = DateConverter.civilToPersian(civil);

		sb.append("امروز:\n");
		sb.append(PersianUtils.getDayOfWeekName(civil.getDayOfWeek()));
		sb.append(PersianUtils.PERSIAN_COMMA);
		sb.append(" ");
		sb.append(nowDate.toString());
		sb.append(" هجری خورشیدی\n\n");
		sb.append("برابر با:\n");
		sb.append(civil.toString());
		sb.append(" میلادی\n");
		sb.append(DateConverter.civilToIslamic(civil).toString());
		sb.append(" هجری قمری\n");

		TextView ci = (TextView) findViewById(R.id.calendarInfo);
		ci.setText(sb.toString());

		setMonth();

		showCalendarOfMonthYear(currentPersianYear, currentPersianMonth);
		
	    gestureDetector = new GestureDetector(simpleOnGestureListener);
		gestureListener = new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return gestureDetector.onTouchEvent(event);
			}
		};
		findViewById(R.id.calendarTable).setOnTouchListener(gestureListener);
	}	

	private void showCalendarOfMonthYear(int year, int month) {

		calendarCleanUp();

		PersianDate persian = new PersianDate(year, month, 1);
		persian.setMonth(month);

		int weekOfMonth = 1;
		int dayOfWeek = DateConverter.persianToCivil(persian).getDayOfWeek() % 7;
		for (int i = 1; i <= 31; i++) {
			try {
				persian.setDayOfMonth(i);

				TextView textView = getTextViewFromNameId(String.format(
						"calendarCell%d%d", weekOfMonth, dayOfWeek + 1));

				textView.setText(PersianUtils.getPersianNumber(i));

				dayOfWeek++;
				if (dayOfWeek == 7) {
					weekOfMonth++;
					dayOfWeek = 0;
				}

				if (persian.equals(nowDate)) {
					textView.setBackgroundResource(R.drawable.widget_background);
				} else {
					textView.setBackgroundResource(android.R.color.transparent);
				}
			} catch (DayOutOfRangeException e) {
				Log.i("com.byagowi.persiancalendar", "Limit on " + i + " "
						+ dayOfWeek);
			} catch (Exception e) {
				Log.e("com.byagowi.persiancalendar", "Error: " + e.getMessage());
			}
		}

		currentMonthTextView.setText(persian.getMonthName() + " "
				+ PersianUtils.getPersianNumber(persian.getYear()));
	}

	private void calendarCleanUp() {
		for (int i = 1; i < 7; i++) {
			for (int j = 1; j <= 7; j++) {
				getTextViewFromNameId(String.format("calendarCell%d%d", i, j))
						.setText("");
			}
		}
	}

	private TextView getTextViewFromNameId(String name) {
		TextView textView = null;
		try {
			textView = (TextView) findViewById(R.id.class.getField(name)
					.getInt(null)); // null for static field of classes
		} catch (Exception e) {
			Log.e("com.byagowi.persiancalendar", "Error on retriving cell: "
					+ e.getMessage());
		}
		return textView;
	}

	public void nextMonth(View v) {
		nextMonth();
		showCalendarOfMonthYear(currentPersianYear, currentPersianMonth);
	}

	public void previousMonth(View v) {
		previousMonth();
		showCalendarOfMonthYear(currentPersianYear, currentPersianMonth);
	}

	private void nextMonth() {
		
		currentPersianMonth++;
		if (currentPersianMonth == 13) {
			currentPersianMonth = 1;
			currentPersianYear++;
		}
	}

	private void previousMonth() {
		currentPersianMonth--;
		if (currentPersianMonth == 0) {
			currentPersianMonth = 12;
			currentPersianYear--;
		}
	}

	private void setMonth() {
		PersianDate persian = DateConverter.civilToPersian(new CivilDate());
		currentPersianMonth = persian.getMonth();
		currentPersianYear = persian.getYear();
	}
}
