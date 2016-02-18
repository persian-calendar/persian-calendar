package com.byagowi.persiancalendar;

import com.byagowi.persiancalendar.util.UpdateUtils;
import com.google.android.apps.dashclock.api.DashClockExtension;

public class DashClockUpdate extends DashClockExtension {
    private UpdateUtils updateUtils;

    @Override
    protected void onUpdateData(int reason) {
        setUpdateWhenScreenOn(true);
        updateUtils = UpdateUtils.getInstance(getApplicationContext());
        updateUtils.update();
        publishUpdate(updateUtils.getExtensionData());
    }

}
