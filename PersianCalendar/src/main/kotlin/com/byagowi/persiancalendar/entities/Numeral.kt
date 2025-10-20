package com.byagowi.persiancalendar.entities

import java.util.Locale

// https://en.wikipedia.org/wiki/Numeral_system
// See also https://developer.mozilla.org/en-US/docs/Web/CSS/list-style-type
enum class Numeral(private val digits: List<Char>) {
    PERSIAN(listOf10Items('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')),
    ARABIC(listOf10Items('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')),
    ARABIC_INDIC(listOf10Items('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')),
    DEVANAGARI(listOf10Items('०', '१', '२', '३', '४', '५', '६', '७', '८', '९')),
    TAMIL(listOf10Items('௦', '௧', '௨', '௩', '௪', '௫', '௬', '௭', '௮', '௯'));
    //CJK(listOf10Items('０', '１', '２', '３', '４', '５', '６', '７', '８', '９'));

    fun format(number: Int) = format(number.toString())
    fun format(number: Double) = format(number.toString())
    fun format(number: String): String {
        if (this == ARABIC) return number
        if (this == TAMIL) when (number) {
            "10" -> return "௰"
            "100" -> return "௱"
            "1000" -> return "௲"
            else -> Unit
        }
        return number.map { digits.getOrNull(Character.getNumericValue(it)) ?: it }
            .joinToString("")
            .replace(".", ARABIC_DECIMAL_SEPARATOR)
    }

    fun parseDouble(number: String): Double? {
        if (this == TAMIL) when (number) {
            "௰" -> return 10.0
            "௱" -> return 100.0
            "௲" -> return 1000.0
        }
        return number
            .let { if (isEasternArabic) it.replace(ARABIC_DECIMAL_SEPARATOR, ".") else it }
            .map {
                val digit = digits.indexOf(it)
                if (digit == -1) "$it" else "$digit"
            }
            .joinToString("")
            .toDoubleOrNull()
    }

    fun formatLongNumber(value: Long): String {
        return format("%,d".format(Locale.ENGLISH, value)).let {
            if (isEasternArabic) it.replace(",", ARABIC_THOUSANDS_SEPARATOR) else it
        }
    }

    val isArabic get() = this == ARABIC
    val isTamil get() = this == TAMIL
    val isEasternArabic get() = this == PERSIAN || this == ARABIC_INDIC

    companion object {
        const val ARABIC_THOUSANDS_SEPARATOR = "٬"
        const val ARABIC_DECIMAL_SEPARATOR = "٫"
    }
}

private fun <T> listOf10Items(
    x1: T, x2: T, x3: T, x4: T, x5: T, x6: T, x7: T, x8: T, x9: T, x10: T,
) = listOf(x1, x2, x3, x4, x5, x6, x7, x8, x9, x10)
