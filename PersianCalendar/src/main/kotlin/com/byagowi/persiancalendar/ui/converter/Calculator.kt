package com.byagowi.persiancalendar.ui.converter

import androidx.annotation.CheckResult
import androidx.annotation.VisibleForTesting
import kotlin.math.floor

private val units = mapOf("d" to 86400, "h" to 3600, "m" to 60, "s" to 1)

@CheckResult
fun timeCalculator(input: String): String {
    val seconds = eval(
        input.replace(Regex("[\\d\\w\\s]+")) { x ->
            "(${
                units.toList().fold(x.value) { acc, unit ->
                    acc.replace(
                        Regex("(\\d+)" + unit.first),
                        "+$1*" + unit.second
                    )
                }
            })"
        }
    )
    return units.toList().fold("" to seconds) { (result, reminder), unit ->
        result + floor(reminder / unit.second).toInt() + unit.first + " " to
                reminder % unit.second
    }.first.trim() + "\n" + units.map { "${seconds / it.value} ${it.key}" }
        .joinToString("\n")
}

// https://github.com/agasy18/kotlin-calculator
// License: Apache 2.0
@VisibleForTesting
@CheckResult
fun eval(expr: String): Double {
    var index = 0 // current index
    val skipWhile =
        { cond: (Char) -> Boolean -> while (index < expr.length && cond(expr[index])) index++ }
    val tryRead = { c: Char -> (index < expr.length && expr[index] == c).also { if (it) index++ } }
    val skipWhitespaces = { skipWhile(Char::isWhitespace) }
    val tryReadOp =
        { op: Char -> skipWhitespaces(); tryRead(op).also { if (it) skipWhitespaces() } }
    var rootOp: () -> Double = { .0 }
    val num = {
        if (tryReadOp('(')) {
            rootOp().also { tryReadOp(')').also { if (!it) error("Missing ) at $index") } }
        } else {
            val start = index; tryRead('-') or tryRead('+'); skipWhile { it.isDigit() || it == '.' }
            expr.substring(start, index).toDoubleOrNull() ?: error("Invalid number at $index")
        }
    }

    fun binary(left: () -> Double, op: Char): List<Double> =
        buildList { add(left()); while (tryReadOp(op)) addAll(binary(left, op)) }

    val div = { binary(num, '/').reduce { a, b -> a / b } }
    val mul = { binary(div, '*').reduce { a, b -> a * b } }
    val sub = { binary(mul, '-').reduce { a, b -> a - b } }
    val add = { binary(sub, '+').reduce { a, b -> a + b } }
    rootOp = add
    return rootOp().also { if (index < expr.length) error("Invalid expression at $index") }
}

enum class Expression(transform: (List<Pair<Token, String>>) -> List<Pair<Token, String>>) {
    NULL({ it }),
    EXPONENTIATION_EXPRESSION({ it }),
    PARENTHESIZED_EXPRESSION({ it }),
    MULTIPLICATIVE_EXPRESSION({ it }),
    ADDITIVE_EXPRESSION({ it })
}

@OptIn(ExperimentalStdlibApi::class)
enum class Token(val matcher: Regex) {
    NUMBER(Regex("-?(?:0|[1-9]\\d*)(?:\\.\\d+)?(?:[eE][+-]?\\d+)?")),
    SYMBOL(Regex("[a-zA-Z_]+")),
    MULTIPLICATIVE_OPERATOR(Regex("[*/%]")),
    ADDITIVE_OPERATOR(Regex("[+-]")),
    OPERATOR(Regex("[,()^]"));

    companion object {
        fun tokenize(text: String): List<Pair<Token, String>> = buildList {
            var i = 0
            fun skipSpaces() = run { while (i != text.length && text[i] == ' ') ++i }
            while (i != text.length) {
                skipSpaces()
                values().firstNotNullOfOrNull {
                    it.matcher.matchAt(text, i)?.apply { i += value.length; add(it to value) }
                } ?: error("Tokenize failure at $i")
                skipSpaces()
            }
        }
    }
}
