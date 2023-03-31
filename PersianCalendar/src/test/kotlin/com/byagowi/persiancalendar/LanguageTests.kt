package com.byagowi.persiancalendar

import com.byagowi.persiancalendar.entities.Language
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import kotlin.test.assertEquals

class LanguageTests {
    @ParameterizedTest
    @CsvSource(
        "بیل, بيل",
        "پگاه, بیكیاه",
        "چراگاه, جیراكیاه",
        "ژاله, زیاله",
        "اکرام, اكرام",
        "سال, سال",
    )
    fun `should replace characters correctly`(source: String, expected: String) {
        assertEquals(expected, Language.prepareForArabicSort(source))
    }
}
