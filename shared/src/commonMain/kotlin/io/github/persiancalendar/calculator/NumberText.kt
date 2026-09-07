package io.github.persiancalendar.calculator

import kotlin.math.abs

/** Keep the JVM calculator's scientific-notation thresholds on JavaScript/Wasm. */
internal fun portableNumberText(value: Double): String {
    val raw = value.toString()
    if (!value.isFinite() || value == 0.0) return raw
    val magnitude = abs(value)
    if (magnitude >= .001 && magnitude < 10_000_000) return raw
    val sign = if (value < 0) "-" else ""
    val unsigned = raw.removePrefix("-")
    val scientific = unsigned.split('e', 'E')
    if (scientific.size == 2) {
        val mantissa = scientific[0].let { if ('.' in it) it else "$it.0" }
        return "$sign${mantissa}E${scientific[1].toInt()}"
    }
    val dot = unsigned.indexOf('.').let { if (it < 0) unsigned.length else it }
    val allDigits = unsigned.replace(".", "")
    val first = allDigits.indexOfFirst { it != '0' }
    val digits = allDigits.substring(first).trimEnd('0')
    val mantissa = digits.first() + "." + digits.drop(1).ifEmpty { "0" }
    return "$sign${mantissa}E${dot - first - 1}"
}
