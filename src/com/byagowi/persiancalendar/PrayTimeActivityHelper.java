package com.byagowi.persiancalendar;

import android.annotation.TargetApi;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.github.praytimes.*;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

/**
 * Pray time helper. It is like an aspect for activity class somehow
 * 
 * @author ebraminio
 */
class PrayTimeActivityHelper {
	private CalendarUtils utils;

	private final static boolean POSITIONING_PERMISSIONS = false;
	private final CalendarActivity calendarActivity;
	private Date date = new Date();
	private Location location;
	private final TextView prayTimeTextView;
	private LocationManager locationManager;

	public PrayTimeActivityHelper(CalendarActivity calendarActivity) {
		this.calendarActivity = calendarActivity;
		this.utils = calendarActivity.utils;
		prayTimeTextView = (TextView) calendarActivity
				.findViewById(R.id.today_praytimes);
	}

	public void setDate(int year, int month, int dayOfMonth) {
		Calendar c = Calendar.getInstance();
		c.set(year, month, dayOfMonth);
		date = c.getTime();
	}

	public void prayTimeInitialize() {
		location = utils.getLocation(calendarActivity);

		if (POSITIONING_PERMISSIONS) {
			locationManager = (LocationManager) calendarActivity
					.getSystemService(Context.LOCATION_SERVICE);

			if (location == null) {
				location = locationManager
						.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
			}
		}

		if (location != null) {
			fillPrayTime();
		} else {
			if (POSITIONING_PERMISSIONS) {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
					Button b = (Button) calendarActivity
							.findViewById(R.id.praytimes_button);
					b.setVisibility(View.VISIBLE);
					b.setText(utils.textShaper("محاسبهٔ مکان برای اوقات شرعی"));
					b.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							addPrayTimeListener();
							v.setEnabled(false);
						}
					});
				}
			}
		}
	}

	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	private void addPrayTimeListener() {
		Criteria c = new Criteria();
		c.setAccuracy(Criteria.ACCURACY_COARSE);
		String provider = locationManager.getBestProvider(c, true);
		if (provider != null) {
			locationManager.requestSingleUpdate(provider,
					new LocationListener() {
						@Override
						public void onLocationChanged(Location location) {
							fillPrayTime();
						}

						@Override
						public void onStatusChanged(String provider,
								int status, Bundle extras) {
						}

						@Override
						public void onProviderEnabled(String provider) {
						}

						@Override
						public void onProviderDisabled(String provider) {
						}
					}, null);
		} else {
			utils.quickToast(
					"Please set your geographical position on preference.",
					calendarActivity);
		}
	}

	private void fillPrayTime() {
		utils.setLocation(location, calendarActivity);

		PrayTimesCalculator ptc = new PrayTimesCalculator(
				utils.getCalculationMethod(calendarActivity));
		StringBuilder sb = new StringBuilder();
		Map<PrayTime, Clock> prayTimes = ptc.calculate(date,
				new Coordinate(location.getLatitude(), location.getLongitude(),
						location.getAltitude()));
		
		sb.append("اذان صبح: ");
		sb.append(prayTimes.get(PrayTime.Imsak).toString());

		sb.append("\nطلوع آفتاب: ");
		sb.append(prayTimes.get(PrayTime.Sunrise).toString());

		sb.append("\nاذان ظهر: ");
		sb.append(prayTimes.get(PrayTime.Dhuhr).toString());

		sb.append("\nاذان مغرب: ");
		sb.append(prayTimes.get(PrayTime.Maghrib).toString());

		sb.append("\nنیمه وقت شرعی: ");
		sb.append(prayTimes.get(PrayTime.Midnight).toString());

		char[] digits = utils.preferredDigits(calendarActivity);

		utils.prepareTextView(prayTimeTextView);
		prayTimeTextView.setText(utils.textShaper(utils.formatNumber(
				sb.toString(), digits)));

		calendarActivity.findViewById(R.id.praytimes_button).setVisibility(
				View.GONE);
	}

	public void clearInfo() {
		prayTimeTextView.setText("");
	}
}
