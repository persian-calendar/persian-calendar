package com.byagowi.persiancalendar

import com.byagowi.persiancalendar.entities.Language
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class LanguageTests {
    @Test
    fun `should replace characters correctly`() {
        assertEquals(Language.prepareForArabicSort("بیل"), "بيل")
        assertEquals(Language.prepareForArabicSort("پگاه"), "بیكیاه")
        assertEquals(Language.prepareForArabicSort("چراگاه"), "جیراكیاه")
        assertEquals(Language.prepareForArabicSort("ژاله"), "زیاله")
        assertEquals(Language.prepareForArabicSort("اکرام"), "اكرام")
    }
}
