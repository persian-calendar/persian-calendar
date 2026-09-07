package io.github.persiancalendar.calculator

import kotlin.math.*

private fun degOrRadFunction(
    name: String,
    action: (Double) -> Double
): Pair<String, Value.Function> {
    return name to Value.Function({
        when (val value = it[0]) {
            is Value.Number -> Value.Number(
                action(
                    when (value.unit) {
                        "deg" -> (value.value * PI / 180)
                        null -> value.value
                        else -> error("Only degree is acceptable as a unit")
                    }
                )
            )
            else -> Value.Expression(Value.Symbol(name), it)
        }
    }, 1)
}

private fun unaryFunction(
    name: String,
    action: (Double) -> Double
): Pair<String, Value.Function> {
    return name to Value.Function({
        when (val value = it[0]) {
            is Value.Number -> Value.Number(
                action(
                    when (value.unit) {
                        null -> value.value
                        else -> error("This unary functions don't accept number with unit.")
                    }
                )
            )
            else -> Value.Expression(Value.Symbol(name), it)
        }
    }, 1)
}

private fun binaryFunction(action: (Double, Double) -> Double): Value.Function {
    return Value.Function({
        val (x, y) = it
        if ((x as Value.Number).unit != null || (y as Value.Number).unit != null)
            error("this binary function only accepts numbers without unit")
        Value.Number(action(x.value, y.value))
    }, 2)
}

private val constants = mapOf(
    "PI" to Value.Number(PI), "E" to Value.Number(E),
    degOrRadFunction("sin", ::sin), degOrRadFunction("cos", ::cos),
    degOrRadFunction("tan", ::tan), degOrRadFunction("cot") { 1 / tan(it) },
    unaryFunction("asin", ::asin), unaryFunction("acos", ::acos),
    unaryFunction("atan", ::atan), "atan2" to binaryFunction(::atan2),
    unaryFunction("sinh", ::sinh), unaryFunction("cosh", ::cosh),
    unaryFunction("tanh", ::tanh), unaryFunction("asinh", ::asinh),
    unaryFunction("acosh", ::acosh), unaryFunction("asinh", ::atanh),
    "hypot" to binaryFunction(::hypot), unaryFunction("sqrt", ::sqrt),
    unaryFunction("exp", ::exp), "log" to binaryFunction(::log), unaryFunction("ln", ::ln),
    unaryFunction("ceil", ::ceil), unaryFunction("floor", ::floor),
    unaryFunction("truncate", ::truncate), unaryFunction("round", ::round),
    unaryFunction("abs", ::abs), unaryFunction("sign", ::sign),
    "min" to binaryFunction(::min), "max" to binaryFunction(::max),
    "+" to binaryFunction { x, y -> x + y }, "-" to binaryFunction { x, y -> x - y },
    "*" to binaryFunction { x, y -> x * y }, "/" to binaryFunction { x, y -> x / y },
    "%" to binaryFunction { x, y -> x % y },
    "^" to binaryFunction { x, y -> x.pow(y) }, "**" to binaryFunction { x, y -> x.pow(y) },
    "diff" to Value.Function({ diff(it[0], it[1] as Value.Symbol) }),
)

fun eval(input: String): String {
    require(input.length <= 100_000) { "Expression is too long" }
    val result = ExpressionParser(input, constants).program()
    return if (result.size == 1 && result[0] is Value.Number)
        (result[0] as Value.Number).detailedFormat()
    else result.joinToString("\n")
}
