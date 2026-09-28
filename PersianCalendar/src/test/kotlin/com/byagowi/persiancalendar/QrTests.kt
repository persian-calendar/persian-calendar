package com.byagowi.persiancalendar

import io.github.persiancalendar.qr.Qr
import kotlin.math.ceil
import kotlin.test.Test
import kotlin.test.assertEquals

class QrTests {
    @Test
    fun `basic qr test`() {
        val expected = """
█▀▀▀▀▀▀▀█▀▀██▀██▀█▀▀▀██▀▀▀▀▀▀▀█
█ █▀▀▀█ █▄▀█▄▀██▄▄▀ ▀▀█ █▀▀▀█ █
█ █   █ █▄▄▀▄▀█ ▄▀▀█ ██ █   █ █
█ ▀▀▀▀▀ █▀█ ▄ █▀▄▀█ ▄ █ ▀▀▀▀▀ █
█▀█▀▀█▀▀▀▄ ▀██▄█▄ ██▄██▀██▀█▀▀█
███ ▄ █▀█▀▀▀▀▄▀  █▀ ▄▄█ ▄▄█▀▀▄█
█  █▄▀█▀▀  █▀▀█ ▀█▄▄███▄▄█ ▀█▄█
█ ▀▄█▀ ▀▄▄▀▄█ ▀ ▀▄▄▀ ██ █▀▀▀  █
█ ▄█  ▀▀  ▄ ▀ ▄  ▀▄█  ▀█ ▀██ ██
█▀█▀▄█▀▀▀▀█ ▄  ██▄▄▄█▀█▀▀█▀ ▄██
██▀▀██▀▀▀ █▄▀█▀▄█ █▀▀▀ ▀▀▀▀ ███
█▀▀▀▀▀▀▀█ ▄ ██ █ ▄▀ ▀ █▀█  ▄ ▄█
█ █▀▀▀█ █▄▀  ▀█▀▄ ▄ █ ▀▀▀ ▀▀  █
█ █   █ █ █▀▄█ ▄ █▀▄█  █▀▄▄ █ █
█ ▀▀▀▀▀ █▀▀█ █ ▄▄▄ ▀▀ ▄█▄█ █ ██
███████████████████████████████
""".trim()

        val text = "http://www.example.com/ążśźęćńół"
        val result = Qr(text)
        assertEquals(
            expected,
            (0..<ceil(result.size / 2.0).toInt() + 1).joinToString("\n") { row ->
                "█" + (0..<result.size).joinToString("") {
                    val first = !result[row * 2 - 1, it]
                    val second = !result[row * 2, it]
                    if (first) (if (second) "█" else "▀") else (if (second) "▄" else " ")
                } + "█"
            }
        )
    }
}
