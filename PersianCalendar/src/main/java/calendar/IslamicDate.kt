package calendar

import java.util.Calendar

/**
 * @author Amir
 * @author ebraminio
 */

class IslamicDate : AbstractDate {
    private var day: Int = 0
    private var month: Int = 0
    private var year: Int = 0

    @JvmOverloads constructor(calendar: Calendar = Calendar.getInstance()) {
        val islamicDate = DateConverter.civilToIslamic(CivilDate(calendar), 0)
        dayOfMonth = islamicDate.dayOfMonth
        setYear(islamicDate.year)
        setMonth(islamicDate.month)
    }

    constructor(year: Int, month: Int, day: Int) {
        setYear(year)
        // Initialize day, so that we get no exceptions when setting month
        this.day = 1
        setMonth(month)
        dayOfMonth = day
    }

    override fun getDayOfMonth(): Int = day

    override fun setDayOfMonth(day: Int) {
        // TODO This check is not very exact! But it's not worth of it
        // to compute the number of days in this month exactly
        if (day < 1 || day > 30)
            throw DayOutOfRangeException(
                    Constants.DAY + " " + day + " " + Constants.IS_OUT_OF_RANGE)

        this.day = day
    }

    override fun getDayOfWeek(): Int {
        val civilDate = DateConverter.islamicToCivil(this)
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, civilDate.year)
        cal.set(Calendar.MONTH, civilDate.month - 1)
        cal.set(Calendar.DAY_OF_MONTH, civilDate.dayOfMonth)
        return cal.get(Calendar.DAY_OF_WEEK)
    }

    override fun getMonth(): Int = month

    override fun setMonth(month: Int) {
        if (month < 1 || month > 12)
            throw MonthOutOfRangeException(
                    Constants.MONTH + " " + month + " " + Constants.IS_OUT_OF_RANGE)

        // Set the day again, so that exceptions are thrown if the
        // day is out of range
        dayOfMonth = day

        this.month = month
    }

    override fun getWeekOfYear(): Int {
        throw RuntimeException(Constants.NOT_IMPLEMENTED_YET)
    }

    override fun getYear(): Int = year

    override fun setYear(year: Int) {
        if (year == 0)
            throw YearOutOfRangeException(Constants.YEAR_0_IS_INVALID)

        this.year = year
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

    override fun getEvent(): String {
        throw RuntimeException(Constants.NOT_IMPLEMENTED_YET)
    }

    override fun getDayOfYear(): Int {
        throw RuntimeException(Constants.NOT_IMPLEMENTED_YET)
    }

    override fun getWeekOfMonth(firstDayOfWeek: Int): Int {

        val dowOfFirstDayOfMonth = IslamicDate(getYear(), getMonth(), 1).dayOfWeek
        var dayCountInFirstWeek = 7 - dowOfFirstDayOfMonth + firstDayOfWeek
        if (dayCountInFirstWeek > 7)
            dayCountInFirstWeek = dayCountInFirstWeek % 7

        val week1 = dayCountInFirstWeek
        val week2 = week1 + 7
        val week3 = week2 + 7
        val week4 = week3 + 7
        val week5 = week4 + 7
        val week6 = week5 + 7
        val week7 = week6 + 7

        if (day <= week1)
            return 1
        else if (day <= week2)
            return 2
        else if (day <= week3)
            return 3
        else if (day <= week4)
            return 4
        else if (day <= week5)
            return 5
        else if (day <= week6)
            return 6
        else if (day <= week7)
            return 7
        return 0
    }

    override fun isLeapYear(): Boolean {
        throw RuntimeException(Constants.NOT_IMPLEMENTED_YET)
    }

    fun equals(islamicDate: IslamicDate): Boolean {
        return (dayOfMonth == islamicDate.dayOfMonth
                && getMonth() == islamicDate.getMonth()
                && (getYear() == islamicDate.getYear() || getYear() == -1))
    }

    override fun clone(): IslamicDate = IslamicDate(getYear(), getMonth(), dayOfMonth)
}
