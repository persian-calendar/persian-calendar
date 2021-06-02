package com.byagowi.persiancalendar.service

import android.annotation.TargetApi
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.byagowi.persiancalendar.ui.MainActivity
import com.byagowi.persiancalendar.utils.*

/**
 * Created by Alireza Afkar on 19/6/2018AD.
 */
@TargetApi(Build.VERSION_CODES.N)
class PersianCalendarTileService : TileService() {

    override fun onClick() = runCatching {
        startActivityAndCollapse(
            Intent(this, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }.getOrElse(logException)

    override fun onStartListening() = runCatching {
        qsTile?.also { tile ->
            val jdn = Jdn.today
            val today = jdn.toCalendar(mainCalendar)
            tile.icon = Icon.createWithResource(this, getDayIconResource(today.dayOfMonth))
            tile.label = jdn.dayOfWeekName
            tile.contentDescription = today.monthName
            // explicitly set Tile state to Active, fixes tile not being lit on some Samsung devices
            tile.state = Tile.STATE_ACTIVE
        }?.updateTile() ?: Unit
    }.getOrElse(logException)
}
