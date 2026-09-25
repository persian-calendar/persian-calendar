package com.byagowi.persiancalendar

import androidx.compose.runtime.annotation.RememberInComposition
import com.byagowi.persiancalendar.entities.Jdn
import io.github.persiancalendar.calendar.CivilDate
import java.util.GregorianCalendar

@RememberInComposition
fun Jdn.Companion.today(): Jdn {
    val calendar = GregorianCalendar.getInstance()
    val jdn = CivilDate(
        calendar[GregorianCalendar.YEAR],
        calendar[GregorianCalendar.MONTH] + 1,
        calendar[GregorianCalendar.DAY_OF_MONTH],
    )
    return Jdn(jdn)
}
