/*
 * March 2012
 *
 * In place of a legal notice, here is a blessing:
 *
 *    May you do good and not evil.
 *    May you find forgiveness for yourself and forgive others.
 *    May you share freely, never taking more than you give.
 *
 */
package com.byagowi.persiancalendar;

import com.byagowi.common.Range;

import calendar.CivilDate;
import calendar.DateConverter;
import calendar.DayOutOfRangeException;
import calendar.PersianDate;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.ViewFlipper;

import static com.byagowi.persiancalendar.CalendarUtils.*;

/**
 * Program activity for android.
 * 
 * @author ebraminio
 * 
 */
public class CalendarActivity extends Activity {

	int currentPersianYear = 0;
	int currentPersianMonth = 0;
	int currentCalendarIndex = 0;
	PersianDate nowDate;
	ViewFlipper calendarPlaceholder;
	Animation slideInLeftAnimation;
	Animation slideOutLeftAnimation;
	Animation slideInRightAnimation;
	Animation slideOutRightAnimation;
	Animation slideInDownAnimation;
	Animation slideOutDownAnimation;
	Animation slideInUpAnimation;
	Animation slideOutUpAnimation;
	Animation fadeInAnimation;
	Animation fadeOutAnimation;

	GestureDetector gestureDetector;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.calendar);

		// loading XMLs
		Holidays.loadHolidays(getResources().openRawResource(R.raw.holidays));
		slideInLeftAnimation = AnimationUtils.loadAnimation(this,
				R.anim.slide_in_left);
		slideOutLeftAnimation = AnimationUtils.loadAnimation(this,
				R.anim.slide_out_left);
		slideInRightAnimation = AnimationUtils.loadAnimation(this,
				R.anim.slide_in_right);
		slideOutRightAnimation = AnimationUtils.loadAnimation(this,
				R.anim.slide_out_right);
		slideInDownAnimation = AnimationUtils.loadAnimation(this,
				R.anim.slide_in_down);
		slideOutDownAnimation = AnimationUtils.loadAnimation(this,
				R.anim.slide_out_down);
		slideInUpAnimation = AnimationUtils.loadAnimation(this,
				R.anim.slide_in_up);
		slideOutUpAnimation = AnimationUtils.loadAnimation(this,
				R.anim.slide_out_up);
		fadeOutAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_out);
		fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
		// end

		// filling current date on members
		nowDate = DateConverter.civilToPersian(new CivilDate());
		fillCalendarInfo();
		setCurrentYearMonth();
		// end

		// setting up app gesture
		gestureDetector = new GestureDetector(this,
				new CalendarTableGestureListener(this));
		calendarPlaceholder = (ViewFlipper) findViewById(R.id.calendar_placeholder);
		calendarPlaceholder.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return gestureDetector.onTouchEvent(event);
			}
		});
		// end

		// fill view
		View calendar = getLayoutInflater().inflate(R.layout.calendar_table,
				null);
		showCalendarOfMonthYear(currentPersianYear, currentPersianMonth,
				calendar);
		calendarPlaceholder.addView(calendar, currentCalendarIndex);
		// end
	}

	private void fillCalendarInfo() {
		char[] digits = preferenceDigits(this);

		TextView ci = (TextView) findViewById(R.id.calendar_info);
		prepareTextView(ci);
		ci.setText(infoForSpecificDay(new CivilDate(), digits));
		ci.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				bringThisMonth();
			}
		});
		ci.setOnLongClickListener(new View.OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				Intent converterIntent = new Intent(getApplicationContext(),
						CalendarConverterActivity.class);
				startActivityForResult(converterIntent, 100);
				return false;
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_dateconverter:
			Intent converterIntent = new Intent(getApplicationContext(),
					CalendarConverterActivity.class);
			startActivityForResult(converterIntent, 0);
			break;
		case R.id.menu_settings:
			Intent preferenceIntent = new Intent(getApplicationContext(),
					CalendarPreferenceActivity.class);
			startActivityForResult(preferenceIntent, 0);
			break;
		case R.id.menu_about:
			Intent aboutIntent = new Intent(getApplicationContext(),
					CalendarAboutActivity.class);
			startActivityForResult(aboutIntent, 0);
			break;
		case R.id.menu_exit:
			finish();
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

		CalendarWidget1x1.updateTime(this);
		CalendarWidget4x1.updateTime(this);
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void showCalendarOfMonthYear(int year, int month, View calendar) {
		char[] digits = preferenceDigits(this);

		PersianDate persianDate = new PersianDate(year, month, 1);
		persianDate.setMonth(month);
		persianDate.setDari(isDariVersion(this));

		int weekOfMonth = 1;
		int dayOfWeek = DateConverter.persianToCivil(persianDate)
				.getDayOfWeek() % 7;

		TextView currentMonthTextView = getTextViewInView(
				"currentMonthTextView", calendar);
		prepareTextView(currentMonthTextView);
		currentMonthTextView.setText(getMonthYearTitle(persianDate, digits));

		String[] firstCharOfDaysOfWeekName = { "ش", "ی", "د", "س", "چ", "پ",
				"ج" };
		for (int i : new Range(0, 7)) {
			TextView textView = getTextViewInView("calendarCell" + 0 + (i + 1),
					calendar);
			prepareTextView(currentMonthTextView);
			textView.setText(firstCharOfDaysOfWeekName[i]);
		}

		try {
			for (int i : new Range(1, 31)) {
				persianDate.setDayOfMonth(i);

				TextView textView = getTextViewInView("calendarCell"
						+ weekOfMonth + (dayOfWeek + 1), calendar);
				prepareTextView(currentMonthTextView);
				textView.setText(formatNumber(i, digits));

				dayOfWeek++;
				if (dayOfWeek == 7) {
					weekOfMonth++;
					dayOfWeek = 0;
				}

				final String holidayTitle = Holidays
						.getHolidayTitle(persianDate);
				if (holidayTitle != null) {
					textView.setBackgroundResource(R.drawable.holiday_background);
					textView.setTextColor(getResources().getColor(
							R.color.holidays_text_color));
					textView.setOnTouchListener(new View.OnTouchListener() {
						@Override
						public boolean onTouch(View v, MotionEvent event) {
							return gestureDetector.onTouchEvent(event);
						}
					});
					textView.setOnClickListener(new View.OnClickListener() {
						String title = holidayTitle;

						@Override
						public void onClick(View v) {
							quickToast(title, getApplicationContext());
						}
					});
				}

				if (persianDate.equals(nowDate)) {
					textView.setBackgroundResource(R.drawable.today_background);
				}
			}
		} catch (DayOutOfRangeException e) {
			// okay, it was expected
		}
	}

	private TextView getTextViewInView(String name, View view) {
		try {
			return (TextView) view.findViewById(R.id.class.getField(name)
					.getInt(null)); // null for static field of classes
		} catch (Exception e) {
			Log.e(getPackageName(),
					"Error on retriving cell: " + e.getMessage());
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

	public void nextYear() {
		currentPersianYear++;
		currentCalendarIndex++;
		setViewOnCalnedarPlaceholder();
		calendarPlaceholder.setInAnimation(slideInDownAnimation);
		calendarPlaceholder.setOutAnimation(slideOutUpAnimation);
		calendarPlaceholder.showNext();
	}

	public void previousYear() {
		currentPersianYear--;
		currentCalendarIndex++;
		setViewOnCalnedarPlaceholder();
		calendarPlaceholder.setInAnimation(slideInUpAnimation);
		calendarPlaceholder.setOutAnimation(slideOutDownAnimation);
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
