package com.byagowi.persiancalendar.service

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.ui.MainActivity
import com.byagowi.persiancalendar.utils.getDayIconResource
import com.byagowi.persiancalendar.utils.launchAppPendingIntent
import com.byagowi.persiancalendar.utils.logException
import com.byagowi.persiancalendar.utils.monthName

/**
 * Created by Alireza Afkar on 19/6/2018AD.
 */
@TargetApi(Build.VERSION_CODES.N)
class PersianCalendarTileService : TileService() {

    override fun onClick() {
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                launchAppPendingIntent()?.let(::startActivityAndCollapse)
            } else @SuppressLint("StartActivityAndCollapseDeprecated") {
                @Suppress("DEPRECATION")
                startActivityAndCollapse(
                    Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }
        }.onFailure(logException)
    }

    override fun onStartListening() {
        runCatching {
            val tile = qsTile ?: return@runCatching
            val jdn = Jdn.today()
            val today = jdn.inCalendar(mainCalendar)
            tile.icon = Icon.createWithResource(this, getDayIconResource(today.dayOfMonth))
            tile.label = jdn.weekDayName
            tile.contentDescription = today.monthName
            // explicitly set Tile state to Active, fixes tile not being lit on some Samsung devices
            tile.state = Tile.STATE_ACTIVE
            tile.updateTile()
        }.onFailure(logException)
    }
}
