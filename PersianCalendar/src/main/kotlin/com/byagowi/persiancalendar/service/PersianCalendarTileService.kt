package com.byagowi.persiancalendar.service

import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import androidx.core.content.edit
import com.byagowi.persiancalendar.PREF_TILE_STATE
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.utils.getDayIconResource
import com.byagowi.persiancalendar.utils.monthName
import com.byagowi.persiancalendar.utils.preferences

/**
 * Created by Alireza Afkar on 19/6/2018AD.
 */
@RequiresApi(Build.VERSION_CODES.N)
class PersianCalendarTileService : TileService() {

    private var preferredState = Tile.STATE_ACTIVE

    override fun onClick() {
        preferredState =
            if (preferredState == Tile.STATE_ACTIVE) Tile.STATE_INACTIVE else Tile.STATE_ACTIVE
        preferences.edit { putInt(PREF_TILE_STATE, preferredState) }
        updateTile()
    }

    override fun onStartListening() {
        preferredState = preferences.getInt(PREF_TILE_STATE, Tile.STATE_ACTIVE)
        updateTile()
    }

    private fun updateTile() {
        val tile = qsTile ?: return
        val jdn = Jdn.today()
        val today = jdn on mainCalendar
        tile.icon = Icon.createWithResource(this, getDayIconResource(today.dayOfMonth))
        tile.label = jdn.weekDayName
        tile.contentDescription = today.monthName
        // explicitly set Tile state to Active, fixes tile not being lit on some Samsung devices
        tile.state = preferredState
        tile.updateTile()
    }
}
