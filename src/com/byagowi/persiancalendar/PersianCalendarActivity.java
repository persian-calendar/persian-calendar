package com.byagowi.persiancalendar;

import calendar.CivilDate;
import calendar.DateConverter;
import calendar.DayOutOfRangeException;
import calendar.PersianDate;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

/**
 * Program activity for android.
 * 
 * @author ebraminio
 * 
 */
public class PersianCalendarActivity extends Activity {

	int currentPersianYear = 0;
	int currentPersianMonth = 0;
	int currentCalendarIndex = 0;
	PersianDate nowDate;
	ViewFlipper calendarPlaceholder;
	Animation slideInLeftAnimation;
	Animation slideOutLeftAnimation;
	Animation slideInRightAnimation;
	Animation slideOutRightAnimation;
	Animation fadeInAnimation;
	Animation fadeOutAnimation;

	GestureDetector gestureDetector;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.calendar);

		PersianDateHolidays.loadHolidays(getResources().openRawResource(
				R.raw.holidays));

		fillCalendarInfo();

		setCurrentYearMonth();

		gestureDetector = new GestureDetector(simpleOnGestureListener);
		calendarPlaceholder = (ViewFlipper) findViewById(R.id.calendar_placeholder);
		calendarPlaceholder.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return gestureDetector.onTouchEvent(event);
			}
		});

		slideInLeftAnimation = AnimationUtils.loadAnimation(this,
				R.anim.slide_in_left);
		slideOutLeftAnimation = AnimationUtils.loadAnimation(this,
				R.anim.slide_out_left);
		slideInRightAnimation = AnimationUtils.loadAnimation(this,
				R.anim.slide_in_right);
		slideOutRightAnimation = AnimationUtils.loadAnimation(this,
				R.anim.slide_out_right);
		fadeOutAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_out);
		fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in);

		View calendar = getLayoutInflater().inflate(R.layout.calendar_table,
				null);
		showCalendarOfMonthYear(currentPersianYear, currentPersianMonth,
				calendar);
		calendarPlaceholder.addView(calendar, currentCalendarIndex);

	}

	SimpleOnGestureListener simpleOnGestureListener = new SimpleOnGestureListener() {

		private static final int SWIPE_MIN_DISTANCE = 20;
		private static final int SWIPE_MAX_OFF_PATH = 250;
		private static final int SWIPE_THRESHOLD_VELOCITY = 200;
		
		@Override
		public boolean onDown(MotionEvent e) {
			return true;
		}		

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			try {
				if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
					return false;
				// right to left swipe
				if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
						&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
					previousMonth();
				} else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
						&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
					nextMonth();
				}
			} catch (Exception e) {
				// nothing
			}
			return super.onFling(e1, e2, velocityX, velocityY);
		}
	};

	private void fillCalendarInfo() {

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		boolean persianDigit = prefs.getBoolean("PersianDigits", true);

		StringBuilder sb = new StringBuilder();
		CivilDate civil = new CivilDate();
		nowDate = DateConverter.civilToPersian(civil);

		sb.append("امروز:\n");
		sb.append(PersianUtils.getDayOfWeekName(civil.getDayOfWeek()));
		sb.append(PersianUtils.PERSIAN_COMMA);
		sb.append(" ");
		sb.append(nowDate.toString(persianDigit));
		sb.append(" هجری خورشیدی\n\n");
		sb.append("برابر با:\n");
		sb.append(civil.toString(persianDigit));
		sb.append(" میلادی\n");
		sb.append(DateConverter.civilToIslamic(civil).toString(persianDigit));
		sb.append(" هجری قمری\n");

		TextView ci = (TextView) findViewById(R.id.calendarInfo);
		ci.setText(sb.toString());
		ci.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				bringThisMonth();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_exit:
			finish();
			break;
		case R.id.menu_settings:
			Intent preferenceIntent = new Intent(getApplicationContext(),
					PersianCalendarPreferenceActivity.class);
			startActivityForResult(preferenceIntent, 0);
			break;
		}
		return false;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		currentCalendarIndex++; // hack bringThisMonth for always updating after
								// preference changes
		bringThisMonth();

		fillCalendarInfo();

		PersianCalendarWidget1x1.updateTime(this);
		PersianCalendarWidget4x1.updateTime(this);
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void showCalendarOfMonthYear(int year, int month, View calendar) {

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		boolean persianDigit = prefs.getBoolean("PersianDigits", true);

		PersianDate persianDateIterator = new PersianDate(year, month, 1);
		persianDateIterator.setMonth(month);

		int weekOfMonth = 1;
		int dayOfWeek = DateConverter.persianToCivil(persianDateIterator)
				.getDayOfWeek() % 7;
		for (int i = 1; i <= 31; i++) {
			try {
				persianDateIterator.setDayOfMonth(i);

				TextView textView = getTextViewInView(String.format(
						"calendarCell%d%d", weekOfMonth, dayOfWeek + 1),
						calendar);

				textView.setText(PersianUtils.formatNumber(i, persianDigit));

				dayOfWeek++;
				if (dayOfWeek == 7) {
					weekOfMonth++;
					dayOfWeek = 0;
				}

				final String holidayTitle = PersianDateHolidays
						.getHolidayTitle(persianDateIterator);
				if (holidayTitle != null) {
					textView.setBackgroundResource(R.drawable.holiday_background);
					textView.setTextColor(getResources().getColor(R.color.holidays_text_color));
					textView.setOnClickListener(new View.OnClickListener() {
						String title = holidayTitle;

						@Override
						public void onClick(View v) {
							Toast.makeText(getApplicationContext(), title,
									Toast.LENGTH_SHORT).show();
						}
					});
				}

				if (persianDateIterator.equals(nowDate)) {
					textView.setBackgroundResource(R.drawable.today_background);
				}
			} catch (DayOutOfRangeException e) {
				Log.i("com.byagowi.persiancalendar", "Limit on " + i + " "
						+ dayOfWeek);
			} catch (Exception e) {
				Log.e("com.byagowi.persiancalendar", "Error: " + e.getMessage());
			}
		}
		getTextViewInView("currentMonthTextView", calendar).setText(
				persianDateIterator.getMonthName()
						+ " "
						+ PersianUtils.formatNumber(
								persianDateIterator.getYear(), persianDigit));

	}

	private TextView getTextViewInView(String name, View view) {
		try {
			return (TextView) view.findViewById(R.id.class.getField(name)
					.getInt(null)); // null for static field of classes
		} catch (Exception e) {
			Log.e("com.byagowi.persiancalendar", "Error on retriving cell: "
					+ e.getMessage());
			return null;
		}
	}

	public void nextMonth() {
		currentPersianMonth++;
		if (currentPersianMonth == 13) {
			currentPersianMonth = 1;
			currentPersianYear++;
		}
		currentCalendarIndex++;
		setViewOnCalnedarPlaceholder();
		calendarPlaceholder.setInAnimation(slideInLeftAnimation);
		calendarPlaceholder.setOutAnimation(slideOutRightAnimation);
		calendarPlaceholder.showNext();
	}

	public void previousMonth() {
		currentPersianMonth--;
		if (currentPersianMonth == 0) {
			currentPersianMonth = 12;
			currentPersianYear--;
		}
		currentCalendarIndex++;
		setViewOnCalnedarPlaceholder();
		calendarPlaceholder.setInAnimation(slideInRightAnimation);
		calendarPlaceholder.setOutAnimation(slideOutLeftAnimation);
		calendarPlaceholder.showNext();
	}

	public void bringThisMonth() {
		if (currentCalendarIndex != 0) {
			calendarPlaceholder.removeAllViews();
			setCurrentYearMonth();
			currentCalendarIndex = 0;
			setViewOnCalnedarPlaceholder();
			calendarPlaceholder.setInAnimation(fadeInAnimation);
			calendarPlaceholder.setOutAnimation(fadeOutAnimation);
			calendarPlaceholder.setDisplayedChild(currentCalendarIndex);
		}
	}

	public void setViewOnCalnedarPlaceholder() {
		View calendar = getLayoutInflater().inflate(R.layout.calendar_table,
				null);
		showCalendarOfMonthYear(currentPersianYear, currentPersianMonth,
				calendar);
		calendarPlaceholder.addView(calendar, currentCalendarIndex);
	}

	private void setCurrentYearMonth() {
		PersianDate persian = DateConverter.civilToPersian(new CivilDate());
		currentPersianMonth = persian.getMonth();
		currentPersianYear = persian.getYear();
	}
}
