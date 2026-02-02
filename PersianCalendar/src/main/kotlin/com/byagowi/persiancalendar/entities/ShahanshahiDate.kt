package com.byagowi.persiancalendar.entities

import com.byagowi.persiancalendar.global.isModernShahanshahi
import io.github.persiancalendar.calendar.AbstractDate
import io.github.persiancalendar.calendar.DateTriplet
import io.github.persiancalendar.calendar.PersianDate

/**
 * Shahanshahi (Yazdegerdi) Calendar Date
 *
 * A fixed 365-day calendar with 12 months of 30 days each, plus 5 Gatha days
 * (epagomenal days) at the end of the year. No leap year is implemented.
 *
 * Epoch: June 16, 632 CE (Julian) - Accession of Yazdegerd III
 * JDN Epoch: 1952063
 *
 * Month 13 represents the Gatha days (days 1-5 of month 13 = days 361-365 of year)
 *
 * MODERN ERA (Imperial/Pahlavi):
 * If 'isModernShahanshahi' is true, this acts as the Pahlavi Imperial calendar (1976-1978),
 * which is identical to the Solar Hijri (Persian) calendar but with a year offset of +1180.
 * Epoch: Coronation of Cyrus the Great (559 BCE).
 */
class ShahanshahiDate : AbstractDate {

    constructor(year: Int, month: Int, dayOfMonth: Int) : super(year, month, dayOfMonth)

    constructor(jdn: Long) : super(jdn)

    override fun toJdn(): Long {
        if (isModernShahanshahi) {
            // Modern Shahanshahi (Imperial) is just Persian + 1180 (e.g. 1355 + 1180 = 2535)
            // We delegate to PersianDate for the calculation
            val pParams = PersianDate(year - 1180, month, dayOfMonth)
            return pParams.toJdn()
        }

        // Standard Yazdegerdi (Old) implementation
        // Days before this year + days in current year
        val daysBeforeYear = (year - 1) * 365L
        val daysInYear = when {
            month <= 12 -> (month - 1) * 30 + dayOfMonth
            else -> 360 + dayOfMonth // Gatha days (month 13)
        }
        return EPOCH + daysBeforeYear + daysInYear - 1
    }

    override fun fromJdn(jdn: Long): DateTriplet {
        if (isModernShahanshahi) {
            val pParams = PersianDate(jdn)
            return createDateTriplet(pParams.year + 1180, pParams.month, pParams.dayOfMonth)
        }

        val daysSinceEpoch = jdn - EPOCH
        val year = (daysSinceEpoch / 365).toInt() + 1
        val dayOfYear = (daysSinceEpoch % 365).toInt() + 1

        val month: Int
        val day: Int
        when {
            dayOfYear <= 360 -> {
                month = (dayOfYear - 1) / 30 + 1
                day = (dayOfYear - 1) % 30 + 1
            }
            else -> {
                // Gatha days (361-365)
                month = 13
                day = dayOfYear - 360
            }
        }

        return createDateTriplet(year, month, day)
    }

    private fun createDateTriplet(year: Int, month: Int, day: Int): DateTriplet {
        try {
            // DateTriplet is a value class (JvmInline).
            // 1. Find the static factory method that packs the 3 ints into the underlying value.
            //    It is typically named 'constructor-impl'.
            val clazz = DateTriplet::class.java
            val packer = clazz.methods.firstOrNull { 
                it.name.contains("constructor-impl") && 
                it.parameterCount == 3 &&
                it.parameterTypes.all { t -> t == Integer.TYPE }
            } ?: throw NoSuchMethodException("Static method constructor-impl(int, int, int) not found")

            val packedValue = packer.invoke(null, year, month, day)

            // 2. Find the constructor for the boxed class, which takes the underlying value (int).
            //    We iterate to find a constructor taking a single int.
            val boxCtor = clazz.declaredConstructors.firstOrNull { 
                it.parameterCount == 1 && it.parameterTypes[0] == Integer.TYPE
            } ?: throw NoSuchMethodException("Box constructor(int) not found")
            
            boxCtor.isAccessible = true
            return boxCtor.newInstance(packedValue) as DateTriplet
        } catch (e: Exception) {
            // Detailed logging for debugging
            val methods = DateTriplet::class.java.methods.joinToString { it.name }
            val ctors = DateTriplet::class.java.declaredConstructors.joinToString { c ->
                c.parameterTypes.joinToString { it.name }
            }
            throw IllegalStateException("Cannot create DateTriplet via value class logic. Methods: $methods, Ctors: $ctors", e)
        }
    }

    fun monthsDistanceTo(other: AbstractDate): Int {
        require(other is ShahanshahiDate) { "Expected ShahanshahiDate" }
        if (isModernShahanshahi) {
             // Convert 'other' to Persian logic
             val thisP = PersianDate(year - 1180, month, dayOfMonth)
             val otherP = PersianDate(other.year - 1180, other.month, other.dayOfMonth)
             return thisP.monthsDistanceTo(otherP)
        }
        return (year - other.year) * 13 + (month - other.month)
    }

    fun monthStartOfMonthsDistance(monthsDistance: Int): AbstractDate {
        if (isModernShahanshahi) {
             val p = PersianDate(year - 1180, month, dayOfMonth).monthStartOfMonthsDistance(monthsDistance)
             return ShahanshahiDate(p.year + 1180, p.month, p.dayOfMonth)
        }

        var newMonth = month + monthsDistance
        var newYear = year
        while (newMonth > 13) {
            newMonth -= 13
            newYear++
        }
        while (newMonth < 1) {
            newMonth += 13
            newYear--
        }
        return ShahanshahiDate(newYear, newMonth, 1)
    }

    companion object {
        /**
         * JDN Epoch: June 16, 632 CE (Julian Calendar)
         * This is the date of Yazdegerd III's accession to the throne.
         */
        const val EPOCH = 1952063L
    }
}
