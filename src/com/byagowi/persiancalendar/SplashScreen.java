package com.byagowi.persiancalendar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.Window;
import android.widget.TextView;

// borrowed from http://www.codeproject.com/Articles/113831/An-Advanced-Splash-Screen-for-Android-App :)
public class SplashScreen extends Activity {
	private final Utils utils = Utils.getInstance();

	/**
	 * The thread to process splash screen events
	 */
	private Thread mSplashThread = new Thread() {
		@Override
		public void run() {
			try {
				synchronized (this) {
					// Wait given period of time or exit on touch
					wait(10);
				}
			} catch (InterruptedException ex) {
				// Okay
			}
			finish();
			Intent intent = new Intent();
			intent.setClass(SplashScreen.this, MainActivity.class);
			startActivity(intent);
		}
	};

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		utils.setTheme(this);
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		// Splash screen view
		setContentView(R.layout.splash);

		char[] digits = utils.preferredDigits(this);

		TextView versionTextView = ((TextView) findViewById(R.id.version));

		String versionTitle = utils.version
				+ utils.formatNumber(utils.programVersion(this), digits);

		utils.prepareTextView(versionTextView);
		versionTextView.setText(utils.textShaper(versionTitle));

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