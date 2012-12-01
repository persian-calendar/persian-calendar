package com.byagowi.persiancalendar;

import static com.byagowi.persiancalendar.CalendarUtils.formatNumber;
import static com.byagowi.persiancalendar.CalendarUtils.getLocation;
import static com.byagowi.persiancalendar.CalendarUtils.preferenceDigits;
import static com.byagowi.persiancalendar.CalendarUtils.prepareTextView;
import static com.byagowi.persiancalendar.CalendarUtils.quickToast;
import static com.byagowi.persiancalendar.CalendarUtils.setLocation;
import static com.byagowi.persiancalendar.CalendarUtils.textShaper;

import java.util.Date;
import java.util.Map;

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

import com.github.praytimes.CalculationMethod;
import com.github.praytimes.Clock;
import com.github.praytimes.Coordinate;
import com.github.praytimes.PrayTime;
import com.github.praytimes.PrayTimesCalculator;

public class PrayTimeActivityHelper {
	CalendarActivity calendarActivity;

	public PrayTimeActivityHelper(CalendarActivity calendarActivity) {
		this.calendarActivity = calendarActivity;
	}

	public void prayTimeInitialize() {
		Location location = getLocation(calendarActivity);
		final LocationManager lm = (LocationManager) calendarActivity
				.getSystemService(Context.LOCATION_SERVICE);

		if (location == null) {
			location = lm
					.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
		}

		if (location != null) {
			fillPrayTime(location);
		} else {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
				Button b = (Button) calendarActivity
						.findViewById(R.id.praytimes_button);
				b.setVisibility(View.VISIBLE);
				b.setText(textShaper("محاسبهٔ مکان برای اوقات شرعی"));
				b.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						addPrayTimeListener(lm);
						v.setEnabled(false);
					}
				});
			}
		}
	}

	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	private void addPrayTimeListener(LocationManager lm) {
		Criteria c = new Criteria();
		c.setAccuracy(Criteria.ACCURACY_COARSE);
		String provider = lm.getBestProvider(c, true);
		if (provider != null) {
			lm.requestSingleUpdate(provider, new LocationListener() {
				@Override
				public void onLocationChanged(Location location) {
					fillPrayTime(location);
				}

				@Override
				public void onStatusChanged(String provider, int status,
						Bundle extras) {
				}

				@Override
				public void onProviderEnabled(String provider) {
				}

				@Override
				public void onProviderDisabled(String provider) {
				}
			}, null);
		} else {
			quickToast("Please set your geographical position on preference.",
					calendarActivity);
		}
	}

	private void fillPrayTime(Location location) {
		setLocation(location, calendarActivity);
		location.getLongitude();
		PrayTimesCalculator ptc = new PrayTimesCalculator(
				CalculationMethod.Jafari);
		StringBuilder sb = new StringBuilder();
		Map<PrayTime, Clock> prayTimes = ptc
				.calculate(new Date(), new Coordinate(location.getLatitude(),
						location.getLongitude()));

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

		char[] digits = preferenceDigits(calendarActivity);

		TextView tv = (TextView) calendarActivity
				.findViewById(R.id.today_praytimes);
		
		prepareTextView(tv);
		tv.setText(textShaper(formatNumber(sb.toString(), digits)));

		calendarActivity.findViewById(R.id.praytimes_button).setVisibility(
				View.GONE);
	}
}
