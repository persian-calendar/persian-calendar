package com.byagowi.persiancalendar.ui.calendar.reports

import android.content.res.Resources
import androidx.annotation.CheckResult
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.entities.PrayTime
import com.byagowi.persiancalendar.entities.PrayTime.Companion.get
import com.byagowi.persiancalendar.global.calculationMethod
import com.byagowi.persiancalendar.global.cityName
import com.byagowi.persiancalendar.global.coordinates
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.global.numeral
import com.byagowi.persiancalendar.global.spacedComma
import com.byagowi.persiancalendar.ui.utils.isRtl
import com.byagowi.persiancalendar.utils.calculatePrayTimes
import com.byagowi.persiancalendar.utils.monthName
import com.byagowi.persiancalendar.utils.title
import io.github.persiancalendar.calendar.AbstractDate
import kotlinx.html.body
import kotlinx.html.h1
import kotlinx.html.head
import kotlinx.html.html
import kotlinx.html.meta
import kotlinx.html.script
import kotlinx.html.stream.createHTML
import kotlinx.html.style
import kotlinx.html.table
import kotlinx.html.tbody
import kotlinx.html.td
import kotlinx.html.tfoot
import kotlinx.html.th
import kotlinx.html.thead
import kotlinx.html.tr
import kotlinx.html.unsafe

@CheckResult
fun prayTimeHtmlReport(resources: Resources, date: AbstractDate): String {
    return createHTML().html {
        val coordinates = coordinates ?: return@html
        attributes["lang"] = language.language
        attributes["dir"] = if (resources.isRtl) "rtl" else "ltr"
        head {
            meta(charset = "utf8")
            style {
                unsafe {
                    +"""
                        body { font-family: system-ui }
                        th, td { padding: 0 .5em; text-align: center }
                        td { border-top: 1px solid lightgray; font-size: 95% }
                        h1 { text-align: center; font-size: 110% }
                        table { margin: 0 auto; }
                    """.trimIndent()
                }
            }
        }
        body {
            h1 {
                +listOfNotNull(
                    cityName,
                    language.my.format(date.monthName, numeral.format(date.year)),
                ).joinToString(spacedComma)
            }
            table {
                val prayTimeList = PrayTime.allTimes(calculationMethod.isJafari)
                thead {
                    tr {
                        th { +resources.getString(R.string.day) }
                        prayTimeList.forEach { th { +resources.getString(it.stringRes) } }
                    }
                }
                tbody {
                    repeat(mainCalendar.getMonthLength(date.year, date.month)) { day ->
                        tr {
                            val prayTimes = coordinates.calculatePrayTimes(
                                Jdn(
                                    mainCalendar.createDate(date.year, date.month, day),
                                ).toGregorianCalendar(),
                            )
                            th { +numeral.format(day + 1) }
                            prayTimeList.forEach {
                                td { +prayTimes[it].toBasicFormatString() }
                            }
                        }
                    }
                }
                if (calculationMethod != language.preferredCalculationMethod) tfoot {
                    tr {
                        td {
                            colSpan = "10"
                            +calculationMethod.title(resources)
                        }
                    }
                }
            }
            script { unsafe { +"print()" } }
        }
    }
}
