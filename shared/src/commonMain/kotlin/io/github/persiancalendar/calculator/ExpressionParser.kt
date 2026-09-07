package io.github.persiancalendar.calculator

/** Portable implementation of the pinned upstream Grammar.g4 and GrammarVisitor semantics. */
internal class ExpressionParser(input: String, private val defaults: Map<String, Value>) {
    private val tokens = mutableListOf<String>()
    private val registry = defaults.toMutableMap()
    private var position = 0
    private var depth = 0
    private val current get() = tokens.getOrElse(position) { "" }

    init {
        val token = Regex("(?:0|[1-9][0-9]*)(?:\\.[0-9]+)?(?:[Ee][+-]?(?:0|[1-9][0-9]*))?|[a-zA-Z_]+|\\*\\*|[+*/%^(),=;\\n-]")
        var index = 0
        while (index < input.length) {
            when {
                input[index] in " \t\r" -> index++
                input[index] == '#' || input.startsWith("//", index) -> {
                    index = input.indexOf('\n', index).takeIf { it >= 0 } ?: input.length
                }
                else -> {
                    val match = token.find(input, index)
                    require(match != null && match.range.first == index) { "Unexpected character at ${index + 1}" }
                    tokens += match.value
                    index = match.range.last + 1
                }
            }
        }
        registry["clear"] = Value.Function({
            registry.keys.filter { it != "clear" }.toList().forEach(registry::remove)
            registry.putAll(defaults)
            Value.Null
        }, 0)
    }

    fun program(): List<Value> = buildList {
        while (current.isNotEmpty()) {
            if (current == ";" || current == "\n") { position++; continue }
            if (current.matches(Regex("[a-zA-Z_]+")) && tokens.getOrNull(position + 1) == "=") {
                val name = current
                position += 2
                registry[name] = expression()
            } else {
                val value = expression().let {
                    if (it is Value.Function && it.inputCount == 0) it(emptyList()) else it
                }
                if (value !is Value.Null) add(value)
            }
            require(current.isEmpty() || current == ";" || current == "\n") { "Unexpected token: $current" }
        }
    }

    private fun expression(): Value {
        require(++depth < 128) { "Expression nesting is too deep" }
        try {
            var result = product()
            while (current == "+" || current == "-") {
                val op = current
                position++
                val right = product()
                result = if (op == "+") result + right else result - right
            }
            return result
        } finally { depth-- }
    }

    private fun product(): Value {
        var result = power()
        while (current in listOf("*", "/", "%")) {
            val op = current
            position++
            val right = power()
            result = when (op) { "*" -> result * right; "/" -> result / right; else -> result % right }
        }
        return result
    }

    private fun power(): Value {
        val values = mutableListOf(signed())
        while (current == "^" || current == "**") { position++; values += signed() }
        return values.reduceRight { left, right -> left.pow(right) }
    }

    private fun signed(): Value {
        var sign = 1
        while (current == "+" || current == "-") { if (current == "-") sign = -sign; position++ }
        val values = mutableListOf(atom())
        while (current == "(" || current.firstOrNull()?.let { it.isLetter() || it.isDigit() || it == '_' } == true)
            values += atom()
        val pairs = values.takeIf { it.size % 2 == 0 }?.chunked(2)
        val result = if (pairs?.all { (x, y) -> x is Value.Number && y is Value.Symbol } == true)
            pairs.map { (x, y) -> (x as Value.Number) withUnit (y as Value.Symbol) }.reduce { x, y -> (x + y) as Value.Number }
        else values.reduceRight { x, y ->
            when (x) {
                is Value.Function -> x(if (y is Value.Tuple) y.values else listOf(y))
                is Value.Number -> if (y is Value.Symbol) x withUnit y else error("Not supported number call")
                else -> error("Not supported call")
            }
        }
        return if (sign < 0) Value.Number(-1.0) * result else result
    }

    private fun atom(): Value {
        val value = current
        require(value.isNotEmpty()) { "Expected expression" }
        position++
        if (value == "(") {
            val values = mutableListOf<Value>()
            if (current != ")") {
                values += expression()
                while (current == ",") { position++; values += expression() }
            }
            require(current == ")") { "Expected closing parenthesis" }
            position++
            return if (values.size == 1) values[0] else Value.Tuple(values)
        }
        value.toDoubleOrNull()?.let { return Value.Number(it) }
        require(value.matches(Regex("[a-zA-Z_]+"))) { "Unexpected token: $value" }
        return registry[value] ?: Value.Symbol(value)
    }
}
