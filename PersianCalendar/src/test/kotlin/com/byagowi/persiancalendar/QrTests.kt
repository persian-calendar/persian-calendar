package com.byagowi.persiancalendar

import io.github.persiancalendar.qr.qr
import org.junit.jupiter.api.Test
import kotlin.math.ceil
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
        val result = qr(text)
        assertEquals(
            expected,
            (0..<ceil(result.size / 2.0).toInt() + 1).joinToString("\n") { row ->
                "█" + result.indices.joinToString("") {
                    val first = !(result.getOrNull(row * 2 - 1)?.get(it) ?: false)
                    val second = !(result.getOrNull(row * 2)?.get(it) ?: false)
                    if (first) (if (second) "█" else "▀") else (if (second) "▄" else " ")
                } + "█"
            }
        )
    }
}
