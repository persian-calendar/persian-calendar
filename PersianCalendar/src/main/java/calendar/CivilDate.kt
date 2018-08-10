package calendar

import java.util.Calendar

/**
 * @author Amir
 * @author ebraminio
 */

class CivilDate @JvmOverloads constructor(calendar: Calendar = Calendar.getInstance()) : AbstractDate() {
    private var year: Int = 0
    private var month: Int = 0
    private var day: Int = 0

    init {
        this.year = calendar.get(Calendar.YEAR)
        this.month = calendar.get(Calendar.MONTH) + 1
        this.day = calendar.get(Calendar.DAY_OF_MONTH)
    }

    constructor(year: Int, month: Int, day: Int) : this() {
        setYear(year)
        // Initialize day, so that we get no exceptions when setting month
        this.day = 1
        setMonth(month)
        dayOfMonth = day
    }

    override fun getDayOfMonth(): Int {
        return day
    }

    override fun setDayOfMonth(day: Int) {
        if (day < 1)
            throw DayOutOfRangeException(
                    Constants.DAY + " " + day + " " + Constants.IS_OUT_OF_RANGE)

        if (month != 2 && day > daysInMonth[month])
            throw DayOutOfRangeException(
                    Constants.DAY + " " + day + " " + Constants.IS_OUT_OF_RANGE)

        if (month == 2 && isLeapYear && day > 29)
            throw DayOutOfRangeException(
                    Constants.DAY + " " + day + " " + Constants.IS_OUT_OF_RANGE)

        if (month == 2 && !isLeapYear && day > 28)
            throw DayOutOfRangeException(
                    Constants.DAY + " " + day + " " + Constants.IS_OUT_OF_RANGE)

        // TODO check for the case of leap year for February
        this.day = day
    }

    override fun getDayOfWeek(): Int {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month - 1)
        cal.set(Calendar.DAY_OF_MONTH, day)
        return cal.get(Calendar.DAY_OF_WEEK)
    }

    override fun getDayOfYear(): Int {
        throw RuntimeException(Constants.NOT_IMPLEMENTED_YET)
    }

    override fun getEvent(): String {
        throw RuntimeException(Constants.NOT_IMPLEMENTED_YET)
    }

    override fun getMonth(): Int {
        return month
    }

    override fun setMonth(month: Int) {
        if (month < 1 || month > 12)
            throw MonthOutOfRangeException(
                    Constants.MONTH + " " + month + " " + Constants.IS_OUT_OF_RANGE)

        // Set the day again, so that exceptions are thrown if the
        // day is out of range
        dayOfMonth = dayOfMonth

        this.month = month
    }

    override fun getWeekOfMonth(firstDayOfWeek: Int): Int {
        val cal = Calendar.getInstance()
        cal.firstDayOfWeek = firstDayOfWeek
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month - 1)
        cal.set(Calendar.DAY_OF_MONTH, day)
        return cal.get(Calendar.WEEK_OF_MONTH)
    }

    override fun getWeekOfYear(): Int {
        throw RuntimeException(Constants.NOT_IMPLEMENTED_YET)
    }

    override fun getYear(): Int {
        return year
    }

    override fun setYear(year: Int) {
        if (year == 0)
            throw YearOutOfRangeException(Constants.YEAR_0_IS_INVALID)

        this.year = year
    }

    override fun isLeapYear(): Boolean {
        if (year % 400 == 0)
            return true
        else if (year % 100 == 0)
            return false

        return year % 4 == 0
    }

    override fun rollDay(amount: Int, up: Boolean) {
        throw RuntimeException(Constants.NOT_IMPLEMENTED_YET)
    }

    override fun rollMonth(amount: Int, up: Boolean) {
        throw RuntimeException(Constants.NOT_IMPLEMENTED_YET)
    }

    override fun rollYear(amount: Int, up: Boolean) {
        throw RuntimeException(Constants.NOT_IMPLEMENTED_YET)
    }

    fun equals(civilDate: CivilDate): Boolean {
        return (dayOfMonth == civilDate.dayOfMonth
                && getMonth() == civilDate.getMonth()
                && (getYear() == civilDate.getYear() || getYear() == -1))
    }

    override fun clone(): CivilDate {
        return CivilDate(getYear(), getMonth(), dayOfMonth)
    }

    companion object {
        private val daysInMonth = intArrayOf(0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
    }
}
