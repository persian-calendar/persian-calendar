//package com.byagowi.persiancalendar.ui.reminder;
//
//import android.content.Context;
//import android.content.Intent;
//import android.media.AudioManager;
//import android.media.MediaPlayer;
//import android.os.Bundle;
//import android.speech.tts.TextToSpeech;
//import android.view.Window;
//import android.view.WindowManager.LayoutParams;
//
//import com.byagowi.persiancalendar.Constants;
//import com.byagowi.persiancalendar.R;
//import com.byagowi.persiancalendar.databinding.ActivityReminderAlertBinding;
//import com.byagowi.persiancalendar.entities.Reminder;
//import com.byagowi.persiancalendar.utils.Utils;
//
//import java.util.concurrent.Executors;
//import java.util.concurrent.ScheduledExecutorService;
//import java.util.concurrent.TimeUnit;
//
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.databinding.DataBindingUtil;
//
//
///**
// * @author MEHDI DIMYADI
// * MEHDIMYADI
// */
//public class ReminderActivity extends AppCompatActivity {
//
//    private ScheduledExecutorService scheduler;
//    private Reminder event;
//    private TextToSpeech tts;
//    private boolean isTTSEnabled;
//    private int previousVolume;
//    private AudioManager am;
//    private MediaPlayer alarm;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        ActivityReminderAlertBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_reminder_alert);
//
//        am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
//        previousVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
//        am.setStreamVolume(AudioManager.STREAM_MUSIC,
//                am.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
//        Window window = this.getWindow();
//        window.addFlags(LayoutParams.FLAG_DISMISS_KEYGUARD);
//        //window.addFlags(LayoutParams.FLAG_SHOW_WHEN_LOCKED);
//        //window.addFlags(LayoutParams.FLAG_TURN_SCREEN_ON);
//
//
////        try {
////            Ringtone ringtone = RingtoneManager.getRingtone(this,
////                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
////            ringtone.setStreamType(AudioManager.STREAM_ALARM);
////            ringtone.play();
////        } catch (Exception e) {
////            e.printStackTrace();
////        }
////
////        tts = new TextToSpeech(this, status -> {
////            isTTSEnabled = false;
////            if (status == TextToSpeech.SUCCESS) {
////                int result = tts.setLanguage(Locale.getDefault());
////                if (result != TextToSpeech.LANG_MISSING_DATA
////                        && result != TextToSpeech.LANG_NOT_SUPPORTED) {
////                    isTTSEnabled = true;
////                }
////            }
////        });
//
//        Intent intent = getIntent();
//        if (intent != null && (event = Utils.getReminderById(intent.getIntExtra(
//                Constants.REMINDER_ID, -1))) != null) {
//            binding.name.setText(event.name);
//            binding.info.setText(event.info);
//            binding.turnOff.setOnClickListener(v -> {
//                scheduler.shutdown();
//                am.setStreamVolume(AudioManager.STREAM_MUSIC,
//                        previousVolume, 0);
//                ReminderActivity.this.finish();
//            });
//            scheduler = Executors.newSingleThreadScheduledExecutor();
//            scheduler.scheduleAtFixedRate(() -> {
////                if (isTTSEnabled && !TextUtils.isEmpty(event.info)) {
////                    tts.speak(event.info, TextToSpeech.QUEUE_FLUSH, null);
////                } else {
////                    if (alarm != null && !alarm.isPlaying())
////                        alarm.start();
////                }
//            }, 0, Constants.SIGNAL_PAUSE, TimeUnit.SECONDS);
//        } else {
//            finish();
//        }
//    }
//
//    @Override
//    public void onStart() {
//        super.onStart();
//    }
//
//    @Override
//    public void onStop() {
//        super.onStop();
//    }
//
//    @Override
//    public void onDestroy() {
//        if (scheduler != null && !scheduler.isShutdown()) {
//            scheduler.shutdown();
//        }
////        am.setStreamVolume(AudioManager.STREAM_MUSIC, previousVolume, 0);
////        if (tts != null) {
////            tts.stop();
////            tts.shutdown();
////        }
//        super.onDestroy();
//    }
//}
