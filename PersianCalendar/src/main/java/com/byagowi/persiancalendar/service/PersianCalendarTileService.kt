package com.byagowi.persiancalendar.service

import android.annotation.TargetApi
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.byagowi.persiancalendar.ui.MainActivity
import com.byagowi.persiancalendar.utils.Jdn
import com.byagowi.persiancalendar.utils.createStatusIcon
import com.byagowi.persiancalendar.utils.dayOfWeekName
import com.byagowi.persiancalendar.utils.getDayIconResource
import com.byagowi.persiancalendar.utils.logException
import com.byagowi.persiancalendar.utils.mainCalendar
import com.byagowi.persiancalendar.utils.monthName

/**
 * Created by Alireza Afkar on 19/6/2018AD.
 */
@TargetApi(Build.VERSION_CODES.N)
class PersianCalendarTileService : TileService() {

    override fun onClick() = runCatching {
        startActivityAndCollapse(
            Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }.onFailure(logException).let {}

    override fun onStartListening() = runCatching {
        qsTile?.also { tile ->
            val jdn = Jdn.today
            val today = jdn.toCalendar(mainCalendar)
            if ((false)) { // Maybe increases quick tile start time? It already gives ANRs so we should be careful
                tile.icon = Icon.createWithBitmap(createStatusIcon(this, today.dayOfMonth))
            } else {
                tile.icon = Icon.createWithResource(this, getDayIconResource(today.dayOfMonth))
            }
            tile.label = jdn.dayOfWeekName
            tile.contentDescription = today.monthName
            // explicitly set Tile state to Active, fixes tile not being lit on some Samsung devices
            tile.state = Tile.STATE_ACTIVE
        }?.updateTile()
    }.onFailure(logException).let {}
}
