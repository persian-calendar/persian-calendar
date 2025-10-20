package com.byagowi.persiancalendar.entities

import java.util.Locale

// https://en.wikipedia.org/wiki/Numeral_system
// See also https://developer.mozilla.org/en-US/docs/Web/CSS/list-style-type
enum class Numeral(private val zero: Char) {
    PERSIAN('۰'), // ۰۱۲۳۴۵۶۷۸۹
    ARABIC('0'), // 0123456789
    ARABIC_INDIC('٠'), // ٠١٢٣٤٥٦٧٨٩
    DEVANAGARI('०'), // ०१२३४५६७८९
    TAMIL('௦'), // ௦௧௨௩௪௫௬௭௮௯
    CJK('０'); // ０１２３４５６７８９

    fun format(number: Int) = format("$number")
    fun format(number: Double) = format("$number")
    fun format(number: String, isInEdit: Boolean = false): String {
        if (isArabic) return number
        if (isTamil && !isInEdit) when (number) {
            "10" -> return "௰"
            "100" -> return "௱"
            "1000" -> return "௲"
            else -> Unit
        }
        return number.map {
            val value = it - '0'
            if (0 <= value && value <= 9) zero + value else it
        }.joinToString("").replace(".", ARABIC_DECIMAL_SEPARATOR)
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
            .map { ch -> (ch - zero).takeIf { 0 <= it && it <= 9 }?.let { "$it" } ?: ch }
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
