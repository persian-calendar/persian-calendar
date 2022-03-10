package com.byagowi.persiancalendar

import com.byagowi.persiancalendar.ui.converter.Token
import com.byagowi.persiancalendar.ui.converter.eval
import com.byagowi.persiancalendar.ui.converter.timeCalculator
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class CalculatorTests {

    @Test
    fun `Basic time calculator`() {
        // Based on https://www.calculator.net/time-calculator.html?tcexpression=1d+2h+3m+4s+%2B+4h+5s+-+2030s+%2B+28h&ctype=3&x=68&y=18#expression
        assertEquals(
            """2d 9h 29m 19s
2.3953587962962963 d
57.48861111111111 h
3449.3166666666666 m
206959.0 s""", timeCalculator("1d 2h 3m 4s + 4h 5s - 2030s + 28h")
        )
        print(timeCalculator("1d 2h 3m 4s + 4h 5s - 2030s + 28h"))
    }

    // Below tests are copied from https://github.com/agasy18/kotlin-calculator/blob/master/src/test/kotlin/CalculatorKtTest.kt
    // License: Apache 2.0
    @Test
    fun `Test eval number`() {
        assertEquals(5.0, eval("5"), 1.0e-7)
    }

    @Test
    fun `Test eval div`() {
        assertEquals(6.0, eval("12 / 2"), 1.0e-7)
    }

    @Test
    fun `Test eval mul`() {
        assertEquals(24.0, eval("12 * 2"), 1.0e-7)
    }

    @Test
    fun `Test eval sub`() {
        assertEquals(4.0, eval("5 - 1"), 1.0e-7)
    }

    @Test
    fun `Test eval add`() {
        assertEquals(6.0, eval("5 + 1"), 1.0e-7)
    }

    @Test
    fun `Test div priority`() {
        assertEquals(5.0, eval("5/5*5"), 1.0e-7)
    }

    @Test
    fun `Test sub priority`() {
        assertEquals(-2.0, eval("2 - 2 - 2"), 1.0e-7)
    }

    @Test
    fun `Test mul priority`() {
        assertEquals(6.0, eval("2 * 2 + 2"), 1.0e-7)
        assertEquals(6.0, eval("2 + 2 * 2"), 1.0e-7)
    }

    @Test
    fun `Test braces`() {
        assertEquals(3.0, eval("(3)"), 1.0e-7)
        assertEquals(4.0, eval(" ( 2+2 )"), 1.0e-7)
    }

    @Test
    fun `Test braces priority`() = listOf(
        "2 * (2 + 2)" to 8.0,
        "(2 * 2) / 2" to 2.0,
        "(2 + 2) * 2" to 8.0,
        "2 *    ( 2 - 2)" to 0.0,
        "7 / 5 * ( 2 + 2 * 2 )" to 8.4,
        "7 / 5 * (((10 + 5) / 2.0 * 2 + (25-10/2*2.0)) / ((5 -7) - 4- 4/2 + 2) * 2)" to -14.0
    ).map { (exp, res) -> assertEquals(res, eval(exp), 1.0e-7) }.let {}

    @Test
    fun `Test spacing`() {
        assertEquals(6.0, eval("2 *2 + 2  "), 1.0e-7)
        assertEquals(6.0, eval("  2  + 2*      2"), 1.0e-7)
    }

    @Test
    fun `Test signs`() {
        assertEquals(-2.0, eval("2 *-2 + 2  "), 1.0e-7)
        assertEquals(-2.0, eval("  2  + 2*      -2"), 1.0e-7)
        assertEquals(-4.0, eval("-5+1"), 1.0e-7)
        assertEquals(-4.0, eval("-5++1"), 1.0e-7)
    }

    @Test
    fun `Test big expression`() {
        assertEquals(2.5, eval("2 *-2 + 2  * 2 + 2 -2 / -4"), 1.0e-7)
    }


    @Test
    fun `Test wrong expression`() {
        assertThrows(IllegalStateException::class.java) {
            assertEquals(2.5, eval("2 *-2 +aa  * 2 + 2 -2 / -4"), 1.0e-7)
        }
    }

    @Test
    fun `Test invalid expression`() {
        assertThrows(IllegalStateException::class.java) {
            eval("5+ 5 5 6 +  7")
        }
    }

    @Test
    fun `Test invalid braces`() {
        assertThrows(IllegalStateException::class.java) {
            eval("7 / 5 * ((2 + 2) / (((5 -7) + 2) * 2)")
        }
    }

    @Test
    fun `Test tokenizer`() {
        val result = Token.tokenize(" sf + 234 / sf(123 + (213 / 123), 23) + sin(2) ")
        val expected = "sf+234/sf(123+(213/123),23)+sin(2)"
        assertEquals(expected, result.joinToString("") { it.second })
    }
}
