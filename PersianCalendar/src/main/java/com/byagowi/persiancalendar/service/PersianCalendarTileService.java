package com.byagowi.persiancalendar.service;

import android.content.Intent;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.support.annotation.RequiresApi;

import com.byagowi.persiancalendar.util.Utils;
import com.byagowi.persiancalendar.view.activity.MainActivity;

import calendar.PersianDate;

/**
 * Created by Alireza Afkar on 19/6/2018AD.
 */
@RequiresApi(api = Build.VERSION_CODES.N)
public class PersianCalendarTileService extends TileService {
    @Override
    public void onClick() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivityAndCollapse(intent);
    }


    @Override
    public void onStartListening() {

        Tile tile = getQsTile();
        PersianDate today = Utils.getToday();

        tile.setIcon(Icon.createWithResource(this,
                Utils.getDayIconResource(today.getDayOfMonth())));
        tile.setLabel(Utils.getWeekDayName(today));
        tile.setContentDescription(Utils.getMonthName(today));
        tile.updateTile();
    }
}
