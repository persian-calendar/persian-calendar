package com.byagowi.persiancalendar;

import android.app.Application;
import android.content.Context;

//import com.github.pedrovgs.lynx.LynxShakeDetector;

public class ReleaseDebugDifference {
    public static void mainApplication(Application app) {
//        if (LeakCanary.isInAnalyzerProcess(app)) {
//            // This process is dedicated to LeakCanary for heap analysis.
//            // You should not init your app in this process.
//            return;
//        }
////        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder() //
////                .detectAll()
////                .penaltyLog()
////                .penaltyDeath()
////                .build());
//        LeakCanary.install(app);
    }

    public static void startLynxListenerIfIsDebug(Context context) {
//        new LynxShakeDetector(context).init();
    }
}
