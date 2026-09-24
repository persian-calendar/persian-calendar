package com.byagowi.persiancalendar

import com.byagowi.persiancalendar.entities.Language
import kotlin.test.Test
import kotlin.test.assertEquals

class LanguageTests {
    @Test
    fun `should replace characters correctly`() {
        listOf(
            "بیل" to "بيل",
            "پگاه" to "بیكیاه",
            "چراگاه" to "جیراكیاه",
            "ژاله" to "زیاله",
            "اکرام" to "اكرام",
            "سال" to "سال",
        ).forEach { (source, expected) ->
            assertEquals(expected, Language.prepareForArabicSort(source))
        }
    }
}
