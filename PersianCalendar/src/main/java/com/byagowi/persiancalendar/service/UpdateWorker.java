package com.byagowi.persiancalendar.service;

import android.content.Context;

import com.byagowi.persiancalendar.utils.UpdateUtils;
import com.byagowi.persiancalendar.utils.Utils;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class UpdateWorker extends Worker {
    public UpdateWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Worker.Result doWork() {
        Utils.setChangeDateWorker();
        Utils.updateStoredPreference(getApplicationContext());
        UpdateUtils.update(getApplicationContext(), true);
        return Result.success();
    }
}
