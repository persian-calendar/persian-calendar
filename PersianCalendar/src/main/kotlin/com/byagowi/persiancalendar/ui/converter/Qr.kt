package com.byagowi.persiancalendar.ui.converter

import kotlin.math.absoluteValue
import kotlin.math.max

// Ported from https://github.com/ebraminio/headless_qr
// which is a port from https://github.com/Rich-Harris/headless-qr
// which is adapted from https://github.com/kazuhikoarase/qrcode-generator
// License reproduced below

//---------------------------------------------------------------------
//
// QR Code Generator for JavaScript
//
// Copyright (c) 2009 Kazuhiko Arase
//
// URL: http://www.d-project.com/
//
// Licensed under the MIT license:
//  http://www.opensource.org/licenses/mit-license.php
//
// The word 'QR Code' is registered trademark of
// DENSO WAVE INCORPORATED
//  http://www.denso-wave.com/qrcode/faqpatent-e.html
//
//---------------------------------------------------------------------

enum class ErrorCorrectionLevel(internal val value: Int) { L(1), M(0), Q(3), H(2) }

fun qr(
    input: String,
    // [1-40], set it to null for auto-size https://www.qrcode.com/en/about/version.html
    version_: Int? = null,
    // Level L can be dirty/damaged for up to 7%, level M 15%, level Q 25%, level H 30%.
    errorCorrectionLevel: ErrorCorrectionLevel = ErrorCorrectionLevel.M,
): List<List<Boolean>> {
    // This is a UTF-8 only implementation anyway
    val data = input.encodeToByteArray()

    val version = version_ ?: (1..<40).first {
        val rsBlocks = QrRsBlock.getRsBlocks(it, errorCorrectionLevel)
        val buffer = QrBitBuffer()

        buffer.put(4, 4)
        buffer.put(data.size, QrUtil.getSizeInBits(it))
        buffer.putBytes(data)

        val totalDataCount = rsBlocks.sumOf(Rs::dataCount)

        buffer.sizeInBits <= totalDataCount * 8
    }

    val size = version * 4 + 17
    val modules = List(size) { MutableList<Boolean?>(size) { null } }

    val cache = createData(version, errorCorrectionLevel, data)

    fun make(test: Boolean, maskPattern: MaskPattern) {
        (0..<size).forEach { row -> (0..<size).forEach { col -> modules[row][col] = null } }

        setupPositionProbePattern(modules, 0, 0)
        setupPositionProbePattern(modules, size - 7, 0)
        setupPositionProbePattern(modules, 0, size - 7)
        setupPositionAdjustPattern(modules, version)
        setupTimingPattern(modules)
        setupTypeInfo(modules, test, maskPattern, errorCorrectionLevel)

        if (version >= 7) setupVersionNumber(modules, version, test)

        mapData(modules, cache, maskPattern)
    }

    val bestPattern = MaskPattern.entries.minBy { pattern ->
        make(true, pattern)
        getLostPoint(modules.map { row -> row.map { it!! } })
    }

    make(false, bestPattern)
    return modules.map { row -> row.map { it!! } }
}

private const val pad0 = 0xec
private const val pad1 = 0x11

private enum class MaskPattern(val maskFunction: (Int, Int) -> Boolean) {
    Pattern000({ i, j -> (i + j) % 2 == 0 }),
    Pattern001({ i, _ -> i % 2 == 0 }),
    Pattern010({ _, j -> j % 3 == 0 }),
    Pattern011({ i, j -> (i + j) % 3 == 0 }),
    Pattern100({ i, j -> ((i / 2) + (j / 3)) % 2 == 0 }),
    Pattern101({ i, j -> ((i * j) % 2) + ((i * j) % 3) == 0 }),
    Pattern110({ i, j -> (((i * j) % 2) + ((i * j) % 3)) % 2 == 0 }),
    Pattern111({ i, j -> (((i * j) % 3) + ((i + j) % 2)) % 2 == 0 }),
}

private fun setupPositionProbePattern(
    modules: List<MutableList<Boolean?>>,
    row: Int,
    col: Int,
) {
    (-1..7)
        .asSequence()
        .filter { row + it > -1 && modules.size > row + it }
        .forEach { r ->
            (-1..7)
                .asSequence()
                .filter { col + it > -1 && modules.size > col + it }
                .forEach { c ->
                    modules[row + r][col + c] = (r in 0..6 && (c == 0 || c == 6)) ||
                            (c in 0..6 && (r == 0 || r == 6)) ||
                            (r in 2..4 && 2 <= c && c <= 4)
                }
        }
}

private fun setupPositionAdjustPattern(modules: List<MutableList<Boolean?>>, version: Int) {
    val pos = QrUtil.getPatternPosition(version)

    for (row in pos) {
        pos
            .asSequence()
            .filter { col -> modules[row][col] == null }
            .forEach { col ->
                for (r in -2..2) {
                    for (c in -2..2) {
                        modules[row + r][col + c] =
                            r == -2 || r == 2 || c == -2 || c == 2 || (r == 0 && c == 0)
                    }
                }
            }
    }
}

private fun setupTimingPattern(modules: List<MutableList<Boolean?>>) {
    (8..<modules.size - 8)
        .asSequence()
        .filter { r -> modules[r][6] == null }
        .forEach { r -> modules[r][6] = r % 2 == 0 }

    (8..<modules.size - 8)
        .asSequence()
        .filter { c -> modules[6][c] == null }
        .forEach { c -> modules[6][c] = c % 2 == 0 }
}

private fun setupTypeInfo(
    modules: List<MutableList<Boolean?>>,
    test: Boolean,
    maskPattern: MaskPattern,
    errorCorrectionLevel: ErrorCorrectionLevel,
) {
    val data = errorCorrectionLevel.value.shl(3).or(maskPattern.ordinal)
    val bits = QrUtil.getBchTypeInfo(data)

    // vertical
    (0..<15).forEach {
        val mod = !test && bits.shr(it).and(1) == 1

        modules[when {
            it < 6 -> it
            it < 8 -> it + 1
            else -> modules.size - 15 + it
        }][8] = mod
    }

    // horizontal
    (0..<15).forEach {
        val mod = !test && bits.shr(it).and(1) == 1

        modules[8][when {
            it < 8 -> modules.size - it - 1
            it < 9 -> 15 - it - 1 + 1
            else -> 15 - it - 1
        }] = mod
    }

    // fixed module
    modules[modules.size - 8][8] = !test
}

private fun setupVersionNumber(modules: List<MutableList<Boolean?>>, version: Int, test: Boolean) {
    val bits = QrUtil.getBchTypeNumber(version)

    (0..<18).forEach {
        val mod = !test && bits.shr(it).and(1) == 1
        modules[it / 3][(it % 3) + modules.size - 8 - 3] = mod
    }

    (0..<18).forEach {
        val mod = !test && bits.shr(it).and(1) == 1
        modules[(it % 3) + modules.size - 8 - 3][it / 3] = mod
    }
}

private fun mapData(
    modules: List<MutableList<Boolean?>>,
    data: List<Int>,
    maskPattern: MaskPattern,
) {
    var inc = -1
    var row = modules.size - 1
    var bitIndex = 7
    var byteIndex = 0
    val maskFunc = maskPattern.maskFunction

    var col = modules.size - 1
    while (col > 0) {
        if (col == 6) col -= 1

        while (true) {
            (0..<2).forEach { c ->
                if (modules[row][col - c] == null) {
                    var dark = false

                    if (byteIndex < data.size) {
                        dark = data[byteIndex].ushr(bitIndex).and(1) == 1
                    }

                    val mask = maskFunc(row, col - c)

                    if (mask) dark = !dark

                    modules[row][col - c] = dark
                    bitIndex -= 1

                    if (bitIndex == -1) {
                        byteIndex += 1
                        bitIndex = 7
                    }
                }
            }

            row += inc

            if (row < 0 || modules.size <= row) {
                row -= inc
                inc = -inc
                break
            }
        }
        col -= 2
    }
}

private fun getLostPoint(matrix: List<List<Boolean>>): Double {
    val size = matrix.size
    var lostPoint = .0

    // LEVEL1
    (0..<size).forEach { row ->
        (0..<size).forEach { col ->
            val dark = matrix[row][col]
            val sameCount = (-1..1)
                .asSequence()
                .filter { row + it in 0..<size }
                .sumOf { r ->
                    (-1..1).count {
                        col + it in 0..<size &&
                                !(r == 0 && it == 0) && dark == matrix[row + r][col + it]
                    }
                }

            if (sameCount > 5) lostPoint += 3 + sameCount - 5
        }
    }

    // LEVEL2
    (0..<size - 1).forEach { row ->
        (0..<size - 1).forEach { col ->
            val count = (0..1).sumOf { r -> (0..1).count { c -> matrix[row + r][col + c] } }
            if (count == 0 || count == 4) lostPoint += 3
        }
    }

    // LEVEL3
    lostPoint += (0..<size).sumOf { row ->
        (0..<size - 6).count { col ->
            (0..6).all { matrix[row][col + it].xor(it == 1 || it == 5) }
        }
    } * 40.0

    lostPoint += (0..<size).sumOf { col ->
        (0..<size - 6).count { row ->
            (0..6).all { matrix[row + it][col].xor(it == 1 || it == 5) }
        }
    } * 40.0

    // LEVEL4
    val darkCount = (0..<size).sumOf { col -> (0..<size).count { row -> matrix[row][col] } }

    val ratio = ((100 * darkCount) / size / size - 50).absoluteValue / 5
    lostPoint += ratio * 10

    return lostPoint
}

private data class Rs(val dataCount: Int, val totalCount: Int)

private fun createBytes(buffer: QrBitBuffer, rsBlocks: List<Rs>): List<Int> {
    var offset = 0

    var maxDcCount = 0
    var maxEcCount = 0

    val dcData = MutableList(rsBlocks.size) { emptyList<Int>() }
    val ecData = MutableList(rsBlocks.size) { emptyList<Int>() }

    rsBlocks.indices.forEach { r ->
        val dcCount = rsBlocks[r].dataCount
        val ecCount = rsBlocks[r].totalCount - dcCount

        maxDcCount = max(maxDcCount, dcCount)
        maxEcCount = max(maxEcCount, ecCount)

        dcData[r] = List(dcCount) { 0xff.and(buffer[it + offset]) }
        offset += dcCount

        val rsPoly = QrUtil.getErrorCorrectPolynomial(ecCount)
        val rawPoly = QrPolynomial(dcData[r], rsPoly.size - 1)

        val modPoly = rawPoly % rsPoly
        val ecDataSize = rsPoly.size - 1
        ecData[r] = List(ecDataSize) {
            val modIndex = it + modPoly.size - ecDataSize
            if (modIndex >= 0) modPoly[modIndex] else 0
        }
    }

    val totalCodeCount = rsBlocks.sumOf(Rs::totalCount)

    val data = MutableList(totalCodeCount) { 0 }
    var index = 0

    (0..<maxDcCount).forEach { i ->
        dcData.forEach { r ->
            if (i < r.size) {
                data[index] = r[i]
                index += 1
            }
        }
    }

    (0..<maxEcCount).forEach { i ->
        ecData.forEach { r ->
            if (i < r.size) {
                data[index] = r[i]
                index += 1
            }
        }
    }

    return data
}

private fun createData(
    version: Int,
    errorCorrectionLevel: ErrorCorrectionLevel,
    data: ByteArray,
): List<Int> {
    val rsBlocks = QrRsBlock.getRsBlocks(version, errorCorrectionLevel)

    val buffer = QrBitBuffer()

    buffer.put(4, 4)
    buffer.put(data.size, QrUtil.getSizeInBits(version))
    buffer.putBytes(data)

    // calc num max data.
    val totalDataCount = rsBlocks.sumOf(Rs::dataCount)

    if (buffer.sizeInBits > totalDataCount * 8) {
        error("code length overflow. (${buffer.sizeInBits}>${totalDataCount * 8})")
    }

    // end code
    if (buffer.sizeInBits + 4 <= totalDataCount * 8) buffer.put(0, 4)

    // padding
    while (buffer.sizeInBits % 8 != 0) buffer.putBit(false)

    // padding
    while (true) {
        if (buffer.sizeInBits >= totalDataCount * 8) break
        buffer.put(pad0, 8)

        if (buffer.sizeInBits >= totalDataCount * 8) break
        buffer.put(pad1, 8)
    }

    return createBytes(buffer, rsBlocks)
}

private object QrUtil {
    private val patternPositionTable = listOf(
        listOf(),
        listOf(6, 18),
        listOf(6, 22),
        listOf(6, 26),
        listOf(6, 30),
        listOf(6, 34),
        listOf(6, 22, 38),
        listOf(6, 24, 42),
        listOf(6, 26, 46),
        listOf(6, 28, 50),
        listOf(6, 30, 54),
        listOf(6, 32, 58),
        listOf(6, 34, 62),
        listOf(6, 26, 46, 66),
        listOf(6, 26, 48, 70),
        listOf(6, 26, 50, 74),
        listOf(6, 30, 54, 78),
        listOf(6, 30, 56, 82),
        listOf(6, 30, 58, 86),
        listOf(6, 34, 62, 90),
        listOf(6, 28, 50, 72, 94),
        listOf(6, 26, 50, 74, 98),
        listOf(6, 30, 54, 78, 102),
        listOf(6, 28, 54, 80, 106),
        listOf(6, 32, 58, 84, 110),
        listOf(6, 30, 58, 86, 114),
        listOf(6, 34, 62, 90, 118),
        listOf(6, 26, 50, 74, 98, 122),
        listOf(6, 30, 54, 78, 102, 126),
        listOf(6, 26, 52, 78, 104, 130),
        listOf(6, 30, 56, 82, 108, 134),
        listOf(6, 34, 60, 86, 112, 138),
        listOf(6, 30, 58, 86, 114, 142),
        listOf(6, 34, 62, 90, 118, 146),
        listOf(6, 30, 54, 78, 102, 126, 150),
        listOf(6, 24, 50, 76, 102, 128, 154),
        listOf(6, 28, 54, 80, 106, 132, 158),
        listOf(6, 32, 58, 84, 110, 136, 162),
        listOf(6, 26, 54, 82, 110, 138, 166),
        listOf(6, 30, 58, 86, 114, 142, 17),
    )

    private const val g15 = 1.shl(10)
        .or(1.shl(8))
        .or(1.shl(5))
        .or(1.shl(4))
        .or(1.shl(2))
        .or(1.shl(1))
        .or(1.shl(0))
    private const val g18 = 1.shl(12)
        .or(1.shl(11))
        .or(1.shl(10))
        .or(1.shl(9))
        .or(1.shl(8))
        .or(1.shl(5))
        .or(1.shl(2))
        .or(1.shl(0))
    private const val g15Mask = 1.shl(14)
        .or(1.shl(12))
        .or(1.shl(10))
        .or(1.shl(4))
        .or(1.shl(1))

    private fun getBchDigit(data: Int): Int {
        var d = data
        var digit = 0
        while (d != 0) {
            digit += 1
            d = d.ushr(1)
        }
        return digit
    }

    fun getBchTypeInfo(data: Int): Int {
        var d = data.shl(10)
        while (getBchDigit(d) - getBchDigit(g15) >= 0) {
            d = d.xor(g15.shl(getBchDigit(d) - getBchDigit(g15)))
        }
        return data.shl(10).or(d).xor(g15Mask)
    }

    fun getBchTypeNumber(data: Int): Int {
        var d = data.shl(12)
        while (getBchDigit(d) - getBchDigit(g18) >= 0) {
            d = d.xor(g18.shl(getBchDigit(d) - getBchDigit(g18)))
        }
        return data.shl(12).or(d)
    }

    fun getPatternPosition(version: Int): List<Int> = patternPositionTable[version - 1]

    fun getErrorCorrectPolynomial(errorCorrectLength: Int): QrPolynomial {
        var a = QrPolynomial(listOf(1))
        (0..<errorCorrectLength).forEach { a *= QrPolynomial(listOf(1, QrMath.gExp(it))) }
        return a
    }

    fun getSizeInBits(type: Int): Int {
        return when {
            type in 1..9 -> 8 // 1 - 9
            type < 27 -> 16 // 10 - 26
            type < 41 -> 16 // 27 - 40
            else -> error("type:$type")
        }
    }
}

private object QrMath {
    private val expTable = buildList<Int>(256) {
        (0..<8).forEach { add(1.shl(it)) }
        (8..<256).forEach {
            add(this[it - 4].xor(this[it - 5]).xor(this[it - 6]).xor(this[it - 8]))
        }
    }
    private val logTable = buildList(256) {
        repeat(256) { add(0) }
        (0..<255).forEach { this[expTable[it]] = it }
    }

    fun gLog(n: Int): Int = if (n < 1) error("gLog($n)") else logTable[n]
    fun gExp(n: Int): Int = // Deal with negative and don't rely on mod/rem differences
        expTable[if (n == 0) 0 else if (n < 0) 254 - (-n - 1) % 255 else (n - 1) % 255 + 1]
}

private class QrPolynomial(num: List<Int>, shift: Int = 0) {
    private val num: List<Int> = run {
        var offset = 0
        while (offset < num.size && num[offset] == 0) offset += 1

        val size = num.size - offset + shift
        buildList(size) {
            repeat(size) { add(0) }
            (0..<num.size - offset).forEach { this[it] = num[it + offset] }
        }
    }

    operator fun get(index: Int): Int = num[index]

    val size get() = num.size

    operator fun times(e: QrPolynomial): QrPolynomial {
        val num = MutableList(size + e.size - 1) { 0 }

        (0..<size).forEach { i ->
            (0..<e.size).forEach { j ->
                num[i + j] = num[i + j].xor(QrMath.gExp(QrMath.gLog(this[i]) + QrMath.gLog(e[j])))
            }
        }

        return QrPolynomial(num)
    }

    operator fun rem(e: QrPolynomial): QrPolynomial {
        if (size - e.size < 0) return this

        val ratio = QrMath.gLog(this[0]) - QrMath.gLog(e[0])

        val num = num.toMutableList()

        e.num.indices.forEach { num[it] = num[it].xor(QrMath.gExp(QrMath.gLog(e[it]) + ratio)) }

        // recursive call
        return QrPolynomial(num) % e
    }
}

private object QrRsBlock {
    private val rsBlockTable = listOf(
        // L
        // M
        // Q
        // H

        // 1
        listOf(1, 26, 19),
        listOf(1, 26, 16),
        listOf(1, 26, 13),
        listOf(1, 26, 9),

        // 2
        listOf(1, 44, 34),
        listOf(1, 44, 28),
        listOf(1, 44, 22),
        listOf(1, 44, 16),

        // 3
        listOf(1, 70, 55),
        listOf(1, 70, 44),
        listOf(2, 35, 17),
        listOf(2, 35, 13),

        // 4
        listOf(1, 100, 80),
        listOf(2, 50, 32),
        listOf(2, 50, 24),
        listOf(4, 25, 9),

        // 5
        listOf(1, 134, 108),
        listOf(2, 67, 43),
        listOf(2, 33, 15, 2, 34, 16),
        listOf(2, 33, 11, 2, 34, 12),

        // 6
        listOf(2, 86, 68),
        listOf(4, 43, 27),
        listOf(4, 43, 19),
        listOf(4, 43, 15),

        // 7
        listOf(2, 98, 78),
        listOf(4, 49, 31),
        listOf(2, 32, 14, 4, 33, 15),
        listOf(4, 39, 13, 1, 40, 14),

        // 8
        listOf(2, 121, 97),
        listOf(2, 60, 38, 2, 61, 39),
        listOf(4, 40, 18, 2, 41, 19),
        listOf(4, 40, 14, 2, 41, 15),

        // 9
        listOf(2, 146, 116),
        listOf(3, 58, 36, 2, 59, 37),
        listOf(4, 36, 16, 4, 37, 17),
        listOf(4, 36, 12, 4, 37, 13),

        // 10
        listOf(2, 86, 68, 2, 87, 69),
        listOf(4, 69, 43, 1, 70, 44),
        listOf(6, 43, 19, 2, 44, 20),
        listOf(6, 43, 15, 2, 44, 16),

        // 11
        listOf(4, 101, 81),
        listOf(1, 80, 50, 4, 81, 51),
        listOf(4, 50, 22, 4, 51, 23),
        listOf(3, 36, 12, 8, 37, 13),

        // 12
        listOf(2, 116, 92, 2, 117, 93),
        listOf(6, 58, 36, 2, 59, 37),
        listOf(4, 46, 20, 6, 47, 21),
        listOf(7, 42, 14, 4, 43, 15),

        // 13
        listOf(4, 133, 107),
        listOf(8, 59, 37, 1, 60, 38),
        listOf(8, 44, 20, 4, 45, 21),
        listOf(12, 33, 11, 4, 34, 12),

        // 14
        listOf(3, 145, 115, 1, 146, 116),
        listOf(4, 64, 40, 5, 65, 41),
        listOf(11, 36, 16, 5, 37, 17),
        listOf(11, 36, 12, 5, 37, 13),

        // 15
        listOf(5, 109, 87, 1, 110, 88),
        listOf(5, 65, 41, 5, 66, 42),
        listOf(5, 54, 24, 7, 55, 25),
        listOf(11, 36, 12, 7, 37, 13),

        // 16
        listOf(5, 122, 98, 1, 123, 99),
        listOf(7, 73, 45, 3, 74, 46),
        listOf(15, 43, 19, 2, 44, 20),
        listOf(3, 45, 15, 13, 46, 16),

        // 17
        listOf(1, 135, 107, 5, 136, 108),
        listOf(10, 74, 46, 1, 75, 47),
        listOf(1, 50, 22, 15, 51, 23),
        listOf(2, 42, 14, 17, 43, 15),

        // 18
        listOf(5, 150, 120, 1, 151, 121),
        listOf(9, 69, 43, 4, 70, 44),
        listOf(17, 50, 22, 1, 51, 23),
        listOf(2, 42, 14, 19, 43, 15),

        // 19
        listOf(3, 141, 113, 4, 142, 114),
        listOf(3, 70, 44, 11, 71, 45),
        listOf(17, 47, 21, 4, 48, 22),
        listOf(9, 39, 13, 16, 40, 14),

        // 20
        listOf(3, 135, 107, 5, 136, 108),
        listOf(3, 67, 41, 13, 68, 42),
        listOf(15, 54, 24, 5, 55, 25),
        listOf(15, 43, 15, 10, 44, 16),

        // 21
        listOf(4, 144, 116, 4, 145, 117),
        listOf(17, 68, 42),
        listOf(17, 50, 22, 6, 51, 23),
        listOf(19, 46, 16, 6, 47, 17),

        // 22
        listOf(2, 139, 111, 7, 140, 112),
        listOf(17, 74, 46),
        listOf(7, 54, 24, 16, 55, 25),
        listOf(34, 37, 13),

        // 23
        listOf(4, 151, 121, 5, 152, 122),
        listOf(4, 75, 47, 14, 76, 48),
        listOf(11, 54, 24, 14, 55, 25),
        listOf(16, 45, 15, 14, 46, 16),

        // 24
        listOf(6, 147, 117, 4, 148, 118),
        listOf(6, 73, 45, 14, 74, 46),
        listOf(11, 54, 24, 16, 55, 25),
        listOf(30, 46, 16, 2, 47, 17),

        // 25
        listOf(8, 132, 106, 4, 133, 107),
        listOf(8, 75, 47, 13, 76, 48),
        listOf(7, 54, 24, 22, 55, 25),
        listOf(22, 45, 15, 13, 46, 16),

        // 26
        listOf(10, 142, 114, 2, 143, 115),
        listOf(19, 74, 46, 4, 75, 47),
        listOf(28, 50, 22, 6, 51, 23),
        listOf(33, 46, 16, 4, 47, 17),

        // 27
        listOf(8, 152, 122, 4, 153, 123),
        listOf(22, 73, 45, 3, 74, 46),
        listOf(8, 53, 23, 26, 54, 24),
        listOf(12, 45, 15, 28, 46, 16),

        // 28
        listOf(3, 147, 117, 10, 148, 118),
        listOf(3, 73, 45, 23, 74, 46),
        listOf(4, 54, 24, 31, 55, 25),
        listOf(11, 45, 15, 31, 46, 16),

        // 29
        listOf(7, 146, 116, 7, 147, 117),
        listOf(21, 73, 45, 7, 74, 46),
        listOf(1, 53, 23, 37, 54, 24),
        listOf(19, 45, 15, 26, 46, 16),

        // 30
        listOf(5, 145, 115, 10, 146, 116),
        listOf(19, 75, 47, 10, 76, 48),
        listOf(15, 54, 24, 25, 55, 25),
        listOf(23, 45, 15, 25, 46, 16),

        // 31
        listOf(13, 145, 115, 3, 146, 116),
        listOf(2, 74, 46, 29, 75, 47),
        listOf(42, 54, 24, 1, 55, 25),
        listOf(23, 45, 15, 28, 46, 16),

        // 32
        listOf(17, 145, 115),
        listOf(10, 74, 46, 23, 75, 47),
        listOf(10, 54, 24, 35, 55, 25),
        listOf(19, 45, 15, 35, 46, 16),

        // 33
        listOf(17, 145, 115, 1, 146, 116),
        listOf(14, 74, 46, 21, 75, 47),
        listOf(29, 54, 24, 19, 55, 25),
        listOf(11, 45, 15, 46, 46, 16),

        // 34
        listOf(13, 145, 115, 6, 146, 116),
        listOf(14, 74, 46, 23, 75, 47),
        listOf(44, 54, 24, 7, 55, 25),
        listOf(59, 46, 16, 1, 47, 17),

        // 35
        listOf(12, 151, 121, 7, 152, 122),
        listOf(12, 75, 47, 26, 76, 48),
        listOf(39, 54, 24, 14, 55, 25),
        listOf(22, 45, 15, 41, 46, 16),

        // 36
        listOf(6, 151, 121, 14, 152, 122),
        listOf(6, 75, 47, 34, 76, 48),
        listOf(46, 54, 24, 10, 55, 25),
        listOf(2, 45, 15, 64, 46, 16),

        // 37
        listOf(17, 152, 122, 4, 153, 123),
        listOf(29, 74, 46, 14, 75, 47),
        listOf(49, 54, 24, 10, 55, 25),
        listOf(24, 45, 15, 46, 46, 16),

        // 38
        listOf(4, 152, 122, 18, 153, 123),
        listOf(13, 74, 46, 32, 75, 47),
        listOf(48, 54, 24, 14, 55, 25),
        listOf(42, 45, 15, 32, 46, 16),

        // 39
        listOf(20, 147, 117, 4, 148, 118),
        listOf(40, 75, 47, 7, 76, 48),
        listOf(43, 54, 24, 22, 55, 25),
        listOf(10, 45, 15, 67, 46, 16),

        // 40
        listOf(19, 148, 118, 6, 149, 119),
        listOf(18, 75, 47, 31, 76, 48),
        listOf(34, 54, 24, 34, 55, 25),
        listOf(20, 45, 15, 61, 46, 16)
    )

    private fun getRsBlockTable(
        version: Int,
        errorCorrectionLevel: ErrorCorrectionLevel,
    ): List<Int> = rsBlockTable[(version - 1) * 4 + errorCorrectionLevel.ordinal]

    fun getRsBlocks(
        version: Int,
        errorCorrectionLevel: ErrorCorrectionLevel,
    ): List<Rs> {
        val rsBlock = getRsBlockTable(version, errorCorrectionLevel)

        val length = rsBlock.size / 3

        return (0..<length).flatMap {
            val count = rsBlock[it * 3 + 0]
            val totalCount = rsBlock[it * 3 + 1]
            val dataCount = rsBlock[it * 3 + 2]

            (0..<count)
                .asSequence()
                .map { Rs(totalCount = totalCount, dataCount = dataCount) }
        }
    }
}

private class QrBitBuffer {
    private val buffer = mutableListOf<Int>()
    private var size = 0

    operator fun get(index: Int): Int = buffer[index]

    fun put(num: Int, length: Int): Unit =
        (0..<length).forEach { putBit(num.ushr(length - it - 1).and(1) == 1) }

    val sizeInBits: Int get() = size

    fun putBit(bit: Boolean) {
        val bufIndex = size / 8
        if (buffer.size <= bufIndex) buffer.add(0)
        if (bit) buffer[bufIndex] = buffer[bufIndex].or(0x80.ushr(size % 8))
        size += 1
    }

    fun putBytes(bytes: ByteArray): Unit = bytes.forEach { put(it.toInt(), 8) }
}
