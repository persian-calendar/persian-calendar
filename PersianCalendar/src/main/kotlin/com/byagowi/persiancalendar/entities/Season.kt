package com.byagowi.persiancalendar.entities

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.Variants.debugAssertNotNull
import com.byagowi.persiancalendar.utils.isSouthernHemisphere
import com.byagowi.persiancalendar.utils.toJavaCalendar
import io.github.persiancalendar.Equinox
import io.github.persiancalendar.calendar.CivilDate
import io.github.persiancalendar.calendar.PersianDate
import io.github.persiancalendar.praytimes.Coordinates
import java.util.*

enum class Season(
    @StringRes val nameStringId: Int, @DrawableRes val imageId: Int,
    private val equinoxCalculator: (Int) -> Date
) {
    SPRING(R.string.spring, R.drawable.spring, Equinox::northwardEquinox),
    SUMMER(R.string.summer, R.drawable.summer, Equinox::northernSolstice),
    FALL(R.string.fall, R.drawable.fall, Equinox::southwardEquinox),
    WINTER(R.string.winter, R.drawable.winter, Equinox::southernSolstice);

    fun getEquinox(date: CivilDate) = equinoxCalculator(date.year).toJavaCalendar()

    companion object {
        fun fromPersianCalendar(persianDate: PersianDate, coordinates: Coordinates?): Season {
            var season = (persianDate.month - 1) / 3
            // Southern hemisphere
            if (coordinates?.isSouthernHemisphere == true) season = (season + 2) % 4
            return values().getOrNull(season).debugAssertNotNull ?: SPRING
        }
    }
}
