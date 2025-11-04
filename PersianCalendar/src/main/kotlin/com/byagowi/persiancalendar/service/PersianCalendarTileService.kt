package com.byagowi.persiancalendar.service

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.global.spacedComma
import com.byagowi.persiancalendar.ui.MainActivity
import com.byagowi.persiancalendar.utils.getDayIconResource
import com.byagowi.persiancalendar.utils.launchAppPendingIntent
import com.byagowi.persiancalendar.utils.logException
import com.byagowi.persiancalendar.utils.monthName

/**
 * Created by Alireza Afkar on 19/6/2018AD.
 */
@RequiresApi(Build.VERSION_CODES.N)
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
        val tile = qsTile ?: return
        val jdn = Jdn.today()
        val today = jdn on mainCalendar
        tile.icon = Icon.createWithResource(this, getDayIconResource(today.dayOfMonth))
        val title = jdn.weekDay.title
        tile.label = title
        tile.contentDescription = title + spacedComma + language.value.dmy.format(
            today.dayOfMonth,
            today.monthName,
            today.year,
        )
        tile.state = Tile.STATE_ACTIVE
        tile.updateTile()
    }
}
