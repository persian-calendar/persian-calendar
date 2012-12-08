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
import android.view.MotionEvent;
import android.view.Window;
import android.widget.TextView;

import static com.byagowi.persiancalendar.CalendarUtils.*;

// borrowed from http://www.codeproject.com/Articles/113831/An-Advanced-Splash-Screen-for-Android-App :)
public class CalendarSplashScreenActivity extends Activity {

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

		char[] digits = preferredDigits(this);

		TextView versionTextView = ((TextView) findViewById(R.id.version));

		String versionTitle = "نسخهٔ "
				+ formatNumber(programVersion(this),
						digits);

		prepareTextView(versionTextView);
		versionTextView.setText(textShaper(versionTitle));

		// The thread to wait for splash screen events
		mSplashThread = new Thread() {
			@Override
			public void run() {
				try {
					synchronized (this) {
						// Wait given period of time or exit on touch
						wait(10);
					}
				} catch (InterruptedException ex) {
				}
				finish();
				Intent intent = new Intent();
				intent.setClass(CalendarSplashScreenActivity.this,
						CalendarActivity.class);
				startActivity(intent);
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