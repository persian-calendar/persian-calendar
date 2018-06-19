package com.byagowi.persiancalendar.service;

import android.content.Intent;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.byagowi.persiancalendar.util.Utils;
import com.byagowi.persiancalendar.view.activity.MainActivity;

import calendar.PersianDate;

/**
 * Created by Alireza Afkar on 19/6/2018AD.
 */
@RequiresApi(api = Build.VERSION_CODES.N)
public class TileService extends android.service.quicksettings.TileService {
    @Override
    public void onClick() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivityAndCollapse(intent);
    }

    @Override
    public void onStartListening() {
        Utils utils = Utils.getInstance(this);
        PersianDate today = utils.getToday();

        getQsTile().setIcon(createIcon(utils, today));
        getQsTile().setLabel(utils.getWeekDayName(today));
        getQsTile().setContentDescription(utils.getMonthName(today));
        getQsTile().updateTile();
    }

    private Icon createIcon(Utils utils, PersianDate today) {
        int iconRes = utils.getDayIconResource(today.getDayOfMonth());
        return Icon.createWithResource(this, iconRes);
    }
}
