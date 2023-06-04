package com.byagowi.persiancalendar

import com.byagowi.persiancalendar.ui.converter.qr
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class QrTests {
    @Test
    fun `basic qr test`() {
        val expected = """
*******.**..*..*.***..*******
*.....*.*..*...**.*...*.....*
*.***.*..*..*....****.*.***.*
*.***.*.**.*..**...*..*.***.*
*.***.*...*.*.*.**.*..*.***.*
*.....*...***..*..***.*.....*
*******.*.*.*.*.*.*.*.*******
........**...*.**..*.........
*.**.***.**.....*.....*..*.**
..***.......*.**..***.***...*
..*.*.*.****.***.**...*...**.
**.*....**....*..**...**.*..*
**..*.****.**.**.........**..
*.*..*.**.*.*.*.**.*..*....**
**..***..*..****..**..*.*****
**.**..****.****.*.**..*...*.
*..******.***.***..***.**..*.
...*......****..***.......**.
*.*..****.*.**......*.**.**..
........*.*...*.*....*....*..
.**..****..*.*..*.*********..
........***..*.**.*.*...*****
*******.*.*..*.*.****.*.**.*.
*.....*.*.**...****.*...*..**
*.***.*..****.*.*.*.*********
*.***.*.*..*.***..*.**..***.*
*.***.*.*.*..*.*.*..**.*..*.*
*.....*....*.*****..**.*.*.*.
*******.**.*.*...****....*.*.
""".trim()

        val text = "http://www.example.com/ążśźęćńół"
        val result = qr(text)
        assertEquals(
            expected,
            result.joinToString("\n") { row ->
                row.joinToString("") { if (it) "*" else "." }
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
