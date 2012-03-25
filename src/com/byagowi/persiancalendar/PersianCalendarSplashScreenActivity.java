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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Window;
import android.widget.TextView;

// borrowed from http://www.codeproject.com/Articles/113831/An-Advanced-Splash-Screen-for-Android-App :)
public class PersianCalendarSplashScreenActivity extends Activity {

	/**
	 * The thread to process splash screen events
	 */
	private Thread mSplashThread;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		// Splash screen view
		setContentView(R.layout.splash);
		
		char[] digits = PersianCalendarUtils.getDigitsFromPreference(this);

		TextView versionTextView = ((TextView) findViewById(R.id.version));

		try {
			String versionTitle = "نسخهٔ "
					+ PersianCalendarUtils.formatNumber(getPackageManager().getPackageInfo(
									getPackageName(), 0).versionName, digits);

			versionTextView.setText(PersianCalendarUtils
					.textShaper(versionTitle));
		} catch (Exception e) {
			Log.e(getPackageName(), e.getMessage());
		}

		// The thread to wait for splash screen events
		mSplashThread = new Thread() {
			@Override
			public void run() {
				try {
					synchronized (this) {
						// Wait given period of time or exit on touch
						wait(1000);
					}
				} catch (InterruptedException ex) {
				}

				finish();

				try {
					// Run next activity
					Intent intent = new Intent();
					intent.setClass(PersianCalendarSplashScreenActivity.this,
							PersianCalendarActivity.class);
					startActivity(intent);
					stop();
				} catch (Exception e) {

				}
			}
		};

		mSplashThread.start();
	}

	/**
	 * Processes splash screen touch events
	 */
	@Override
	public boolean onTouchEvent(MotionEvent evt) {
		if (evt.getAction() == MotionEvent.ACTION_DOWN) {
			synchronized (mSplashThread) {
				mSplashThread.notifyAll();
			}
		}
		return true;
	}
}