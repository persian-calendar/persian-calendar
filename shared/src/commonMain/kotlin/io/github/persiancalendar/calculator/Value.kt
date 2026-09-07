package io.github.persiancalendar.calculator

import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.truncate
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

sealed interface Value {
    object Null : Value

    data class Symbol(val name: String) : Value {
        override fun toString(): String = name

        companion object {
            operator fun getValue(thisRef: Any?, property: KProperty<*>) = Symbol(property.name)
        }
    }

    data class Number(val value: Double, val unit: String? = null) : Value {
        private fun formatNumber(value: Double): String {
            return when (value) {
                Double.POSITIVE_INFINITY -> "Infinity"
                Double.NEGATIVE_INFINITY -> "-Infinity"
                Double.NaN -> "NaN"
                truncate(value) -> value.toInt().toString()
                else -> portableNumberText(value)
            }
        }

        infix fun withUnit(unit: Symbol): Number {
            if (this.unit != null) error("Trying to add unit to a number already with unit")
            return Number(value, unit.name)
        }

        override fun toString(): String = listOfNotNull(formatNumber(value), unit).joinToString(" ")

        fun detailedFormat(): String {
            if (unit != "s") return toString()
            return timeUnits.toList().fold("" to value) { (result, reminder), unit ->
                result + floor(reminder / unit.second).toInt() + unit.first + " " to
                        reminder % unit.second
            }.first.trim() + "\n" + timeUnits.map { "${formatNumber(value / it.value)} ${it.key}" }
                .joinToString("\n")
        }
    }

    data class Function(
        private val body: (arguments: List<Value>) -> Value, val inputCount: Int? = null
    ) : Value {
        operator fun invoke(arguments: List<Value>): Value {
            if (inputCount != null && arguments.size != inputCount)
                error("Invalid number of inputs")
            return body(arguments)
        }
    }

    data class Tuple(val values: List<Value>) : Value {
        override fun toString(): String =
            "(${values.joinToString(", ", transform = Value::toString)})"
    }

    data class Expression(val function: Symbol, val arguments: List<Value>) : Value {
        override fun toString(): String {
            return when (function.name) {
                "+", "-", "/", "*", "%", "**", "^" -> when (arguments.size) {
                    0 -> "0"
                    1 -> arguments[0].toString()
                    else -> "(${arguments.joinToString(" ${function.name} ")})"
                }
                else -> "${function.name}(${arguments.joinToString(", ")})"
            }
        }
    }

    operator fun plus(other: Value): Value {
        if (this !is Number || other !is Number) return Symbol("+")(this, other)
        if (unit == other.unit) return Number(value + other.value, unit)
        val thisSecondFactor = timeUnits[unit]
        val otherSecondFactor = timeUnits[other.unit]
        if (thisSecondFactor == null || otherSecondFactor == null)
            error("This addition of units isn't supported")
        return Number(value * thisSecondFactor + other.value * otherSecondFactor, "s")
    }

    operator fun minus(other: Value): Value {
        if (this !is Number || other !is Number) return Symbol("-")(this, other)
        return this + Number(-1.0) * other
    }

    operator fun times(other: Value): Value {
        if (this !is Number || other !is Number) return Symbol("*")(this, other)
        // TODO: Maybe just allowing multiply of two length units? What else should be accepted?
        if (unit != null && other.unit != null) error("Two numbers with unit are multiplied")
        return Number(value * other.value, unit ?: other.unit)
    }

    operator fun div(other: Value): Value {
        if (this !is Number || other !is Number) return Symbol("/")(this, other)
        val resultUnit = when {
            unit == other.unit -> null // 1m / 2m -> 1 (null)
            unit == null && other.unit != null -> "1/${other.unit}"
            unit != null && other.unit == null -> unit
            else -> "$unit/${other.unit}"
        }
        return Number(value / other.value, resultUnit)
    }

    operator fun rem(other: Value): Value {
        if (this !is Number || other !is Number) return Symbol("%")(this, other)
        return Number(value % other.value)
    }

    fun pow(other: Value): Value {
        if (this !is Number || other !is Number) return Symbol("^")(this, other)
        return Number(value.pow(other.value))
    }

    operator fun invoke(vararg arguments: Value): Expression {
        this as Symbol
        return Expression(this, arguments.toList())
    }

    companion object {
        private val timeUnits = mapOf("d" to 86400, "h" to 3600, "m" to 60, "s" to 1)
    }
}
