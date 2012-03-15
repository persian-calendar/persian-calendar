package com.byagowi.persiancalendar;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.Window;
import android.widget.TextView;

// borrowed from http://www.codeproject.com/Articles/113831/An-Advanced-Splash-Screen-for-Android-App :)
public class PersianCalendarSplashScreen extends Activity {

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

		try {
			String versionTitle = "نسخهٔ "
					+ PersianUtils.getPersianNumber(this.getPackageManager().getPackageInfo(
							this.getPackageName(), 0).versionName);
			((TextView) findViewById(R.id.version)).setText(versionTitle);
		} catch (Exception e) {
			// okay, okay, was not important, go go :D
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
					intent.setClass(PersianCalendarSplashScreen.this,
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