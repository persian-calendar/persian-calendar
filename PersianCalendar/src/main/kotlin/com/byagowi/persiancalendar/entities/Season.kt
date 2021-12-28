package com.byagowi.persiancalendar.entities

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.utils.isSouthernHemisphere
import com.byagowi.persiancalendar.utils.toJavaCalendar
import io.github.persiancalendar.Equinox
import io.github.persiancalendar.calendar.CivilDate
import io.github.persiancalendar.calendar.PersianDate
import io.github.persiancalendar.praytimes.Coordinates

enum class Season(@StringRes val nameRes: Int, @DrawableRes val imageRes: Int) {
    SPRING(R.string.spring, R.drawable.spring),
    SUMMER(R.string.summer, R.drawable.summer),
    FALL(R.string.fall, R.drawable.fall),
    WINTER(R.string.winter, R.drawable.winter);

    fun getEquinox(date: CivilDate) = when (this) {
        SPRING -> Equinox.northwardEquinox(date.year)
        SUMMER -> Equinox.northernSolstice(date.year)
        FALL -> Equinox.southwardEquinox(date.year)
        WINTER -> Equinox.southernSolstice(date.year)
    }.toJavaCalendar()

    companion object {
        fun fromPersianCalendar(persianDate: PersianDate, coordinates: Coordinates?): Season {
            var season = (persianDate.month - 1) / 3
            // Southern hemisphere
            if (coordinates?.isSouthernHemisphere == true) season = (season + 2) % 4
            return values()[season]
        }
    }
}
