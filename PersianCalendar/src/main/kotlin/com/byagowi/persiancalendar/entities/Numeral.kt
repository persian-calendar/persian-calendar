package com.byagowi.persiancalendar.entities

import java.util.Locale

// https://en.wikipedia.org/wiki/Numeral_system
// See also https://developer.mozilla.org/en-US/docs/Web/CSS/list-style-type
enum class Numeral(private val zero: Char) {
    PERSIAN('۰'), // ۰۱۲۳۴۵۶۷۸۹ it's called Arabic-Indic Extended also
    ARABIC('0'), // 0123456789 the that is most common in the world
    ARABIC_INDIC('٠'), // ٠١٢٣٤٥٦٧٨٩ used in Central Kurdish for example
    DEVANAGARI('०'), // ०१२३४५६७८९ used in Nepali for example
    TAMIL('௦'), // ௦௧௨௩௪௫௬௭௮௯ but 10, 100 and 1000 of it has some complexity
    CJK('０'); // ０１２３４５６７８９ not used in the UI currently

    fun format(number: Int) = format("$number")
    fun format(number: Double) = format("$number")
    fun format(number: String, isInEdit: Boolean = false): String {
        if (isArabic) return number
        if (isTamil) {
            if (isInEdit) return number // don't format Tamil in edits, complicated
            when (number) {
                "10" -> return "௰"
                "100" -> return "௱"
                "1000" -> return "௲"
            }
        }
        return number
            .map {
                val value = it - '0' // Or Arabic.zero
                if (0 <= value && value <= 9) zero + value else it
            }
            .joinToString("")
            .let { if (isArabicIndicVariants) it.replace(".", ARABIC_DECIMAL_SEPARATOR) else it }
    }

    fun parseDouble(number: String): Double? {
        if (isArabic) return number.toDoubleOrNull()
        if (isTamil) when (number) {
            "௰" -> return 10.0
            "௱" -> return 100.0
            "௲" -> return 1000.0
        }
        return number
            .let { if (isArabicIndicVariants) it.replace(ARABIC_DECIMAL_SEPARATOR, ".") else it }
            .map {
                val value = it - zero
                if (0 <= value && value <= 9) "$value" else it
            }
            .joinToString("")
            .toDoubleOrNull()
    }

    fun formatLongNumber(value: Long): String {
        return format("%,d".format(Locale.ENGLISH, value)).let {
            if (isArabicIndicVariants) it.replace(",", ARABIC_THOUSANDS_SEPARATOR) else it
        }
    }

    val isArabic get() = this == ARABIC
    val isTamil get() = this == TAMIL
    val isArabicIndicVariants get() = this == PERSIAN || this == ARABIC_INDIC

    companion object {
        const val ARABIC_THOUSANDS_SEPARATOR = "٬"
        const val ARABIC_DECIMAL_SEPARATOR = "٫"
    }
}
