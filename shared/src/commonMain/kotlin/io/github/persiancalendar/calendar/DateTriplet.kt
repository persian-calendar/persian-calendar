package io.github.persiancalendar.calendar

// Putting numbers bigger than [-32768-32767] causes undefined behavior
@kotlin.jvm.JvmInline
value class DateTriplet private constructor(private val packedValue: Int) {
    internal constructor(year: Int, month: Int, dayOfMonth: Int) : this(
        ((year and 0xFFFF) shl 16) or
                ((month and 0xFF) shl 8) or
                (dayOfMonth and 0xFF)
    )

    val year: Int get() = ((packedValue shr 16) and 0xFFFF).toShort().toInt()
    val month: Int get() = ((packedValue shr 8) and 0xFF).toByte().toInt()
    val dayOfMonth: Int get() = (packedValue and 0xFF).toByte().toInt()

    operator fun component1(): Int = year
    operator fun component2(): Int = month
    operator fun component3(): Int = dayOfMonth

    override fun toString(): String = "(year=$year, month=$month, dayOfMonth=$dayOfMonth)"
}
