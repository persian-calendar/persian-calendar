package io.github.persiancalendar.calculator

fun diff(f: Value, symbol: Value.Symbol): Value {
    return when (f) {
        is Value.Number -> Value.Number(0.0)
        is Value.Symbol -> Value.Number(if (f.name == symbol.name) 1.0 else 0.0)
        is Value.Expression -> {
            when (f.function.name) {
                "+", "-" -> Value.Expression(
                    f.function,
                    f.arguments
                        .map { diff(it, symbol) }
                        .filter { (it as? Value.Number)?.value != .0 }
                    // TODO: Move this logic to the operator itself
                )
                "*" -> {
                    if (f.arguments.size != 2)
                        error("Only multiplication of two operands is supported")
                    Value.Expression(
                        Value.Symbol("+"),
                        listOf(
                            f.arguments[0] * diff(f.arguments[1], symbol),
                            f.arguments[1] * diff(f.arguments[0], symbol)
                        ).filter { arg ->
                            val arguments = (arg as? Value.Expression)?.arguments
                                ?: return@filter true
                            arguments.all { (it as? Value.Number)?.value != .0 }
                            // TODO: Move this logic to the operator itself
                        }
                    )
                }
                "/" -> {
                    if (f.arguments.size != 2)
                        error("Only division of two operands is supported")
                    Value.Expression(
                        Value.Symbol("-"),
                        listOf(
                            f.arguments[0] * diff(f.arguments[1], symbol),
                            f.arguments[1] * diff(f.arguments[0], symbol)
                        ).filter { arg ->
                            val arguments = (arg as? Value.Expression)?.arguments
                                ?: return@filter true
                            arguments.all { (it as? Value.Number)?.value != .0 }
                            // TODO: Move this logic to the operator itself
                        }
                    ) / (f.arguments[1].pow(Value.Number(2.0)))
                }
                "^" -> {
                    if (f.arguments.size != 2)
                        error("Only exponential of two operands is supported")
                    val exponent = f.arguments[1]
                    if (exponent !is Value.Number)
                        error("Only constant exponents are supported")
                    exponent *
                            (f.arguments[0].pow(Value.Number(exponent.value - 1))) *
                            diff(f.arguments[0], symbol)
                }
                "sqrt" -> {
                    if (f.arguments.size != 1) error("Sqrt should have one argument")
                    Value.Number(.5) *
                            diff(f.arguments[0], symbol) /
                            Value.Symbol("sqrt")(f.arguments[0])
                }
                "ln" -> {
                    if (f.arguments.size != 1) error("Sqrt should have one argument")
                    diff(f.arguments[0], symbol) / f.arguments[0]
                }
                "exp" -> {
                    if (f.arguments.size != 1) error("Sqrt should have one argument")
                    Value.Symbol("exp")(f.arguments[0]) * diff(f.arguments[0], symbol)
                }
                "sin" -> {
                    if (f.arguments.size != 1) error("Sqrt should have one argument")
                    Value.Symbol("cos")(f.arguments[0]) *
                            diff(f.arguments[0], symbol)
                }
                "cos" -> {
                    if (f.arguments.size != 1) error("Sqrt should have one argument")
                    Value.Number(-1.0) *
                            Value.Symbol("sin")(f.arguments[0]) *
                            diff(f.arguments[0], symbol)
                }
                "tan" -> {
                    if (f.arguments.size != 1) error("Sqrt should have one argument")
                    (Value.Number(1.0) +
                            (Value.Symbol("tan")(f.arguments[0]).pow(Value.Number(2.0)))
                            ) * diff(f.arguments[0], symbol)
                }
                // TODO: Implement chain rule once instead repeatinng it
                else -> error("Not supported function to differentiate $f")
            }
        }
        else -> Value.Expression(Value.Symbol("diff"), listOf(f, symbol))
    }
}