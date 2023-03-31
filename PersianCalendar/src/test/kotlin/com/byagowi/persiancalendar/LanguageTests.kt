package com.byagowi.persiancalendar

import com.byagowi.persiancalendar.entities.Language
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import kotlin.test.assertEquals

class LanguageTests {
    @ParameterizedTest
    @CsvSource(
        value = [
            "بیل, بيل",
            "پگاه, بیكیاه",
            "چراگاه, جیراكیاه",
            "ژاله, زیاله",
            "اکرام, اكرام",
        ]
    )
    fun `should replace characters correctly`(from: String, to: String) {
        assertEquals(Language.prepareForArabicSort(from), to)
    }
}
