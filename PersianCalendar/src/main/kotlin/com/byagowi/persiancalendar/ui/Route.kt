package com.byagowi.persiancalendar.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapVerticalCircle
import androidx.compose.ui.graphics.vector.ImageVector
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ui.icons.AstrologyIcon

enum class Route(
    val route: String,
    val drawerIcon: ImageVector? = null,
    val titleId: Int,
) {
    Calendar("calendar", Icons.Default.DateRange, R.string.calendar),
    Month("monthView", null, R.string.month_view),
    Schedule("schedule", null, R.string.schedule),
    Days("days", null, R.string.day_view),
    Converter("converter", Icons.Default.SwapVerticalCircle, R.string.date_converter),
    Compass("compass", Icons.Default.Explore, R.string.compass),
    Level("level", null, R.string.level),
    Astronomy("astronomy", AstrologyIcon, R.string.astronomy),
    Map("map", null, R.string.map),
    Settings("settings", Icons.Default.Settings, R.string.settings),
    About("about", Icons.Default.Info, R.string.about),
    Licenses("license", null, R.string.licenses),
    DeviceInformation("device", null, R.string.device_information),
    Exit("", Icons.Default.Cancel, R.string.exit);

    companion object {
        fun fromShortcutIds(value: String?): Route {
            // See xml/shortcuts.xml and ShortcutActivity.kt
            return when (value) {
                "COMPASS" -> Calendar
                "LEVEL" -> Level
                "CONVERTER" -> Converter
                "ASTRONOMY" -> Astronomy
                "MAP" -> Map
                else -> entries[0]
            }
        }
    }
}
