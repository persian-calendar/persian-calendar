package com.byagowi.persiancalendar.view.reminder.activity;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.TextView;

import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.view.reminder.constants.Constants;
import com.byagowi.persiancalendar.view.reminder.database.DatabaseManager;
import com.byagowi.persiancalendar.view.reminder.model.ReminderDetails;


/**
 * @author MEHDI DIMYADI
 * MEHDIMYADI
 */
public class ReminderActivity extends AppCompatActivity {

	private ScheduledExecutorService scheduler;
	private ReminderDetails event;
	private TextToSpeech tts;
	private boolean isTTSEnabled;
	private int previousVolume;
	private AudioManager am;
	private MediaPlayer alarm;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reminder_alert);

		DatabaseManager databaseManager = new DatabaseManager(this);
		am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		previousVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
		am.setStreamVolume(AudioManager.STREAM_MUSIC,
				am.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
		Window window = this.getWindow();
		window.addFlags(LayoutParams.FLAG_DISMISS_KEYGUARD);
		//window.addFlags(LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		//window.addFlags(LayoutParams.FLAG_TURN_SCREEN_ON);

		Uri notification = RingtoneManager
				.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		alarm = MediaPlayer.create(getApplicationContext(), notification);
		tts = new TextToSpeech(this, status -> {
			isTTSEnabled = false;
			if (status == TextToSpeech.SUCCESS) {
				int result = tts.setLanguage(Locale.getDefault());
				if (result == TextToSpeech.LANG_MISSING_DATA
						|| result == TextToSpeech.LANG_NOT_SUPPORTED) {
				} else {
					isTTSEnabled = true;
				}
			}
		});

		TextView tv_name = findViewById(R.id.tv_name);
		TextView tv_info = findViewById(R.id.tv_info);
		Button btn_turn_off = findViewById(R.id.btn_turn_off);

		long event_id = getIntent().getLongExtra(Constants.EVENT_ID, -1);
		event = databaseManager.getEvent(event_id);
		if (event != null) {
			tv_name.setText(event.getReminderName());
			tv_info.setText(event.getReminderInfo());
			btn_turn_off.setOnClickListener(v -> {
				scheduler.shutdown();
				am.setStreamVolume(AudioManager.STREAM_MUSIC,
						previousVolume, 0);
				ReminderActivity.this.finish();
			});
			scheduler = Executors.newSingleThreadScheduledExecutor();
			scheduler.scheduleAtFixedRate(() -> {
				if (isTTSEnabled && !event.getReminderInfo().isEmpty()) {
					speakOut();
				} else {
					if (!alarm.isPlaying())
						alarm.start();
				}
			}, 0, Constants.SIGNAL_PAUSE, TimeUnit.SECONDS);
		} else {
			finish();
		}
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onDestroy() {
		if (scheduler != null && !scheduler.isShutdown()) {
			scheduler.shutdown();
		}
		am.setStreamVolume(AudioManager.STREAM_MUSIC, previousVolume, 0);
		if (tts != null) {
			tts.stop();
			tts.shutdown();
		}
		super.onDestroy();
	}

	private void speakOut() {
		tts.speak(event.getReminderInfo(), TextToSpeech.QUEUE_FLUSH, null);
	}
}
