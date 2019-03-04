//package com.byagowi.persiancalendar.service;
//
//import com.byagowi.persiancalendar.utils.UpdateUtils;
//import com.byagowi.persiancalendar.utils.Utils;
//
//import androidx.work.Worker;
//
//public class UpdateWorker extends Worker {
//    @Override
//    public Worker.Result doWork() {
//        Utils.setChangeDateWorker();
//        Utils.updateStoredPreference(getApplicationContext());
//        UpdateUtils.update(getApplicationContext(), true);
//        return Result.SUCCESS;
//    }
//}
