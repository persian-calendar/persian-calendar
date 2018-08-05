package com.byagowi.persiancalendar.service;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.util.Log;

import com.byagowi.persiancalendar.util.Utils;
import com.byagowi.persiancalendar.view.activity.MainActivity;

import calendar.AbstractDate;

/**
 * Created by Alireza Afkar on 19/6/2018AD.
 */
@TargetApi(Build.VERSION_CODES.N)
public class PersianCalendarTileService extends TileService {
    @Override
    public void onClick() {
        try {
            startActivityAndCollapse(new Intent(this, MainActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        } catch (Exception e) {
            Log.e("TileService", "Tile onClick fail", e);
        }
    }


    @Override
    public void onStartListening() {
        Tile tile = getQsTile();
        AbstractDate today = Utils.getTodayOfCalendar(Utils.getMainCalendar());

        tile.setIcon(Icon.createWithResource(this,
                Utils.getDayIconResource(today.getDayOfMonth())));
        tile.setLabel(Utils.getWeekDayName(today));
        tile.setContentDescription(Utils.getMonthName(today));
        // explicitly set Tile state to Active, fixes tile not being lit on some Samsung devices
        tile.setState(Tile.STATE_ACTIVE);
        tile.updateTile();
    }
}
