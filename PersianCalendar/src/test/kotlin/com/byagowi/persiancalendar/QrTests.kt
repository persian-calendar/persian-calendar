package com.byagowi.persiancalendar

import com.byagowi.persiancalendar.ui.converter.qr
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
            (0 until ceil(result.size / 2.0).toInt() + 1).joinToString("\n") { row ->
                "█" + result.indices.joinToString("") {
                    val first = !(result.getOrNull(row * 2 - 1)?.get(it) ?: false)
                    val second = !(result.getOrNull(row * 2)?.get(it) ?: false)
                    if (first) (if (second) "█" else "▀") else (if (second) "▄" else " ")
                } + "█"
            }
        )

        // val bitMatrix = QRCodeWriter().encode(
        //     text, BarcodeFormat.QR_CODE, result.size, result.size, mapOf(
        //         EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M,
        //         EncodeHintType.MARGIN to 0
        //     )
        // )
        // result.indices.forEach { i ->
        //     val row = mutableListOf<Boolean>()
        //     result.indices.forEach { j ->
        //         row.add(bitMatrix[i, j])
        //     }
        //     println(row.joinToString("") { if (it) "*" else "." })
        // }
    }
}
