package io.github.persiancalendar.calendar

/**
 * Abstract class representing a date.
 *
 * @author Amir
 */
abstract class AbstractDate {
    // Concrete things
    val year: Int
    val month: Int
    val dayOfMonth: Int

    operator fun component1(): Int = year
    operator fun component2(): Int = month
    operator fun component3(): Int = dayOfMonth

    /* What JDN (Julian Day Number) means?
     *
     * From https://en.wikipedia.org/wiki/Julian_day:
     * Julian day is the continuous count of days since the beginning of the
     * Julian Period and is used primarily by astronomers, and in software for
     * easily calculating elapsed days between two events (e.g. food production
     * date and sell by date).
     */
    constructor(year: Int, month: Int, dayOfMonth: Int) {
        this.year = year
        this.month = month
        this.dayOfMonth = dayOfMonth
    }

    constructor(jdn: Long) {
        val result = this.fromJdn(jdn)
        this.year = result.year
        this.month = result.month
        this.dayOfMonth = result.dayOfMonth
    }

    constructor(date: AbstractDate) : this(date.toJdn())

    // Things needed to be implemented by subclasses
    abstract fun toJdn(): Long

    protected abstract fun fromJdn(jdn: Long): DateTriplet

    override fun equals(other: Any?): Boolean {
        if (other == null || this::class != other::class || other !is AbstractDate) return false
        return year == other.year && month == other.month && dayOfMonth == other.dayOfMonth
    }

    override fun hashCode(): Int {
        var result = year
        result = 31 * result + month
        result = 31 * result + dayOfMonth
        return result
    }
}
