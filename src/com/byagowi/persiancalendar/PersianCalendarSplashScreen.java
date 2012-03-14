package com.byagowi.persiancalendar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;

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

		// Splash screen view
		setContentView(R.layout.splash);

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