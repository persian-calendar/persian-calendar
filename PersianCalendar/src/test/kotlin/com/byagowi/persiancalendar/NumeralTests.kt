package com.byagowi.persiancalendar

import com.byagowi.persiancalendar.entities.Numeral
import kotlin.test.Test
import kotlin.test.assertEquals

class NumeralTests {
    @Test
    fun `format works properly`() {
        assertEquals("۱۲۳۴۵۶۷۸۹۰", Numeral.PERSIAN.format(1234567890))
        assertEquals("۳٫۱۴", Numeral.PERSIAN.format(3.14))
        assertEquals("௩.௧௪", Numeral.TAMIL.format(3.14))
        assertEquals("abc۱۲۳abc", Numeral.PERSIAN.format("abc123abc"))
        assertEquals("abc۱۲۳abc", Numeral.PERSIAN.format("abc123abc", true))
        assertEquals("abc۱۲۳abc", Numeral.PERSIAN.format("abc123abc", false))
        assertEquals("abc123abc", Numeral.ARABIC.format("abc123abc"))
        assertEquals("1234567890", Numeral.ARABIC.format(1234567890))
        assertEquals("١٢٣٤٥٦٧٨٩٠", Numeral.ARABIC_INDIC.format(1234567890))
        assertEquals("१२३४५६७८९०", Numeral.DEVANAGARI.format(1234567890))
        assertEquals("௧௨௩௪௫௬௭௮௯௦", Numeral.TAMIL.format(1234567890))
        assertEquals("１２３４５６７８９０", Numeral.CJK.format(1234567890))
        assertEquals("௰", Numeral.TAMIL.format(10))
    }

    @Test
    fun `numeral parse works`() {
        assertEquals(3.14, Numeral.PERSIAN.parseDouble("۳٫۱۴"))
        assertEquals(3.14, Numeral.PERSIAN.parseDouble("3.14"))
        assertEquals(null, Numeral.PERSIAN.parseDouble("۱۲۳a"))
        assertEquals(null, Numeral.PERSIAN.parseDouble("a۱۲۳"))
        assertEquals(12312.0, Numeral.PERSIAN.parseDouble("۱۲۳۱۲"))
    }

    @Test
    fun `formatLongNumber works properly`() {
        assertEquals("۱۲٬۳۱۲٬۳۱۲", Numeral.PERSIAN.formatLongNumber(12312312L))
    }
}
