package io.github.persiancalendar.praytimes

internal data class MinuteOrAngleDouble(val value: Double, val isMinutes: Boolean)

internal val Number.deg get() = MinuteOrAngleDouble(this.toDouble(), false)
internal val Int.min get() = MinuteOrAngleDouble(this.toDouble(), true)
