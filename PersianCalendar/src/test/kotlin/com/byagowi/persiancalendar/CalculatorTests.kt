package com.byagowi.persiancalendar

import io.github.persiancalendar.calculator.eval
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class CalculatorTests {
    @Test
    fun `test time calculator`() {
        // Based on https://www.calculator.net/time-calculator.html?tcexpression=1d+2h+3m+4s+%2B+4h+5s+-+2030s+%2B+28h&ctype=3&x=68&y=18#expression
        assertEquals(
            """
                0d 0h 0m 2s
                2.3148148148148147E-5 d
                5.555555555555556E-4 h
                0.03333333333333333 m
                2 s
            """.trimIndent(),
            eval("2s")
        )
        assertEquals(
            """
                2d 9h 29m 19s
                2.3953587962962963 d
                57.48861111111111 h
                3449.3166666666666 m
                206959 s
            """.trimIndent(),
            eval("1d + 2h + 3m + 4s + 4h + 5s - 2030s + 28h")
        )
        assertEquals(
            """
                2d 9h 29m 19s
                2.3953587962962963 d
                57.48861111111111 h
                3449.3166666666666 m
                206959 s
            """.trimIndent(),
            eval("1d 2h 3m 4s + 4h 5s - 2030s + 28h")
        )
    }

    @ParameterizedTest
    @CsvSource(
        value = [
            "5+ 5 5 6 +  7",
            "7 / 5 * ((2 + 2) / (((5 -7) + 2) * 2)",
        ]
    )
    fun `test errors`(input: String) {
        assertThrows(Exception::class.java) { eval(input) }
    }
}
