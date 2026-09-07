package com.byagowi.persiancalendar.shared

import com.byagowi.persiancalendar.entities.Numeral
import com.byagowi.persiancalendar.shared.generated.Res
import io.github.persiancalendar.calendar.AbstractDate
import io.github.persiancalendar.calendar.CivilDate
import io.github.persiancalendar.calendar.IslamicDate
import io.github.persiancalendar.calendar.NepaliDate
import io.github.persiancalendar.calendar.PersianDate
import io.github.persiancalendar.praytimes.Coordinates
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

enum class CalendarKind(val titleKey: String, val eventName: String, val monthKeys: List<String>) {
    PERSIAN("persian_calendar", "Persian", "farvardin ordibehesht khordad tir mordad shahrivar mehr aban azar dey bahman esfand".split(' ')),
    ISLAMIC("hijri_calendar", "Hijri", "muharram safar rabi_alawwal rabi_althani jumada_al_awwal jumada_al_thani rajab shabaan ramadan shawwal dhu_al_qidah dhu_al_hijjah".split(' ')),
    GREGORIAN("gregorian_calendar", "Gregorian", "january february march april may june july august september october november december".split(' ')),
    NEPALI("nepali_calendar", "Nepali", "baisakh jestha ashad shrawan bhadra ashwin kartik mangsir poush magh falgun chaitra".split(' '));

    fun date(year: Int, month: Int, day: Int): AbstractDate = when (this) {
        PERSIAN -> PersianDate(year, month, day)
        ISLAMIC -> IslamicDate(year, month, day)
        GREGORIAN -> CivilDate(year, month, day)
        NEPALI -> NepaliDate(year, month, day)
    }
    fun date(jdn: Long): AbstractDate = when (this) {
        PERSIAN -> PersianDate(jdn)
        ISLAMIC -> IslamicDate(jdn)
        GREGORIAN -> CivilDate(jdn)
        NEPALI -> NepaliDate(jdn)
    }
    fun monthStart(jdn: Long, distance: Int = 0): Long {
        val date = date(jdn)
        val index = date.year * 12 + date.month - 1 + distance
        return date(index.floorDiv(12), index.mod(12) + 1, 1).toJdn()
    }
    fun monthLength(year: Int, month: Int) = (monthStart(date(year, month, 1).toJdn(), 1) - date(year, month, 1).toJdn()).toInt()
    fun validDate(year: Int, month: Int, day: Int): AbstractDate {
        require(year in 1..9999 && month in 1..12) { "Invalid year or month" }
        require(day in 1..monthLength(year, month)) { "Invalid day" }
        return date(year, month, day)
    }
}

val weekdayKeys = "saturday sunday monday tuesday wednesday thursday friday".split(' ')
fun weekday(jdn: Long) = (jdn + 2).mod(7)

class AppStrings(private val all: Map<String, Map<String, String>>, val language: String) {
    val isRtl = language in listOf("fa", "ar", "ckb", "ur", "ps", "glk", "azb", "ota")
    val languages get() = all.keys.sorted()
    operator fun get(key: String): String = all[language]?.get(key) ?: webStrings[language]?.get(key) ?: all["en"]?.get(key) ?: webStrings.getValue("en")[key] ?: key.replace('_', ' ')
    fun date(jdn: Long, calendar: CalendarKind, numeral: Numeral): String {
        val date = calendar.date(jdn)
        return "${numeral.format(date.dayOfMonth)} ${get(calendar.monthKeys[date.month - 1])} ${numeral.format(date.year)}"
    }
}

data class City(val id: String, val names: Map<String, String>, val coordinates: Coordinates) {
    fun name(language: String) = names[language] ?: names["en"].orEmpty()
}

data class BundledEvent(val title: String, val holiday: Boolean, val source: String, val calendar: CalendarKind, val fields: Map<String, String>) {
    fun dayInYear(year: Int): Long? {
        fun number(key: String, default: Int = 0) = fields[key]?.toIntOrNull() ?: default
        val month = number("month", 1)
        if (month !in 1..12) return null
        val first = calendar.date(year, month, 1).toJdn()
        val length = calendar.monthLength(year, month)
        val day = when (fields["rule"]) {
            "simple" -> number("day")
            "single event" -> if (number("year") == year) number("day") else return null
            "nth day from" -> number("day") + number("nth") - 1
            "end of month" -> length
            "last weekday of month" -> length - (weekday(first + length - 1) - number("weekday").mod(7)).mod(7) + number("offset")
            "nth weekday of month" -> 1 + (number("weekday").mod(7) - weekday(first)).mod(7) + (number("nth") - 1) * 7
            else -> return null
        }
        return first + day - 1
    }
}

class CalendarData(
    val translations: Map<String, Map<String, String>>,
    val events: List<BundledEvent>,
    val cities: List<City>,
    val worldMap: String,
    val timeZonesMap: String,
    val tectonicPlates: String,
    val help: String,
    val licenses: String,
) {
    private val eventCache = mutableMapOf<Triple<CalendarKind, Int, Pair<Boolean, Int>>, Map<Long, List<BundledEvent>>>()
    fun eventsOn(jdn: Long, sources: Set<String>): List<BundledEvent> = CalendarKind.entries.flatMap { calendar ->
        val year = calendar.date(jdn).year
        if (eventCache.size > 40) eventCache.clear()
        eventCache.getOrPut(Triple(calendar, year, IslamicDate.useUmmAlQura to IslamicDate.islamicOffset)) {
            // Offset-based rules can cross calendar-year boundaries.
            (year - 1..year + 1).flatMap { eventYear ->
                events.filter { it.calendar == calendar }.mapNotNull { event -> event.dayInYear(eventYear)?.let { it to event } }
            }.groupBy({ it.first }, { it.second })
        }[jdn].orEmpty().filter { it.source in sources }
    }

    companion object {
        suspend fun load(): CalendarData {
            suspend fun read(name: String) = Res.readBytes("files/$name").decodeToString()
            fun JsonObject.strings() = mapValues { it.value.jsonPrimitive.content }
            val translations = Json.parseToJsonElement(read("strings.json")).jsonObject.mapValues { it.value.jsonObject.strings() }
            val events = Json.parseToJsonElement(read("events.json")).jsonObject.getValue("data").jsonArray.map { raw ->
                val obj = raw.jsonObject
                val fields = obj.filterValues { it is JsonPrimitive }.mapValues { it.value.jsonPrimitive.content }
                BundledEvent(fields.getValue("title"), fields["holiday"] == "true", fields.getValue("type"), CalendarKind.entries.first { it.eventName == fields["calendar"] }, fields)
            }
            val cities = Json.parseToJsonElement(read("cities.json")).jsonObject.values.flatMap { country ->
                country.jsonObject.getValue("cities").jsonObject.map { (id, raw) ->
                    val fields = raw.jsonObject.strings()
                    City(id, fields.filterKeys { it in listOf("en", "fa", "ckb", "ar") }, Coordinates(fields.getValue("latitude").toDouble(), fields.getValue("longitude").toDouble(), fields.getValue("elevation").toDouble()))
                }
            }.filter { it.id != "CUSTOM" }
            val licenseTexts = mutableListOf<String>()
            for (name in listOf("license.txt", "calendar.txt", "praytimes.txt", "qr.txt", "calculator.txt", "astronomy.txt", "geomagnetic.txt")) {
                licenseTexts += "$name\n${read(name)}"
            }
            return CalendarData(translations, events, cities, read("worldmap.txt"), read("timezones.txt"), read("tectonicplates.txt"), read("help.txt"), licenseTexts.joinToString("\n\n"))
        }
    }
}
