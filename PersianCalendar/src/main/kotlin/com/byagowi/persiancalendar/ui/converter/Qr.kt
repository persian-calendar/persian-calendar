package com.byagowi.persiancalendar.ui.converter

import kotlin.math.absoluteValue
import kotlin.math.max

// Adapted from https://github.com/Rich-Harris/headless-qr
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
    version: Int? = null,
    // Level L can be dirty/damaged for up to 7%, level M 15%, level Q 25%, level H 30%.
    errorCorrectionLevel: ErrorCorrectionLevel = ErrorCorrectionLevel.M,
): List<List<Boolean>> {
    // This is a Utf8 only implementation
    val data = input.encodeToByteArray()

    var versionValue: Int
    if (version != null) {
        versionValue = version
    } else {
        versionValue = 1
        while (versionValue < 40) {
            val rsBlocks = QRRSBlock.getRsBlocks(versionValue, errorCorrectionLevel)
            val buffer = QrBitBuffer()

            buffer.put(4, 4)
            buffer.put(data.size, QRUtil.getLengthInBits(versionValue))
            buffer.putBytes(data)

            var totalDataCount = 0
            for (block in rsBlocks) {
                totalDataCount += block.dataCount
            }

            if (buffer.lengthInBits <= totalDataCount * 8) break
            versionValue += 1
        }
    }

    // Now we have version also, let's call the concrete implementation
    return qrMain(data, versionValue, errorCorrectionLevel)
}

private fun qrMain(
    data: ByteArray,
    version: Int,
    errorCorrectionLevel: ErrorCorrectionLevel,
): List<List<Boolean>> {
    val size = version * 4 + 17
    val modules = List(size) { MutableList<Boolean?>(size) { null } }

    var minLostPoint = .0
    var bestPattern = MaskPattern.Pattern000

    val cache = createData(version, errorCorrectionLevel, data)

    fun make(test: Boolean, maskPattern: MaskPattern) {
        for (row in 0 until size) {
            for (col in 0 until size) {
                modules[row][col] = null
            }
        }

        setupPositionProbePattern(modules, 0, 0)
        setupPositionProbePattern(modules, size - 7, 0)
        setupPositionProbePattern(modules, 0, size - 7)
        setupPositionAdjustPattern(modules, version)
        setupTimingPattern(modules)
        setupTypeInfo(modules, test, maskPattern, errorCorrectionLevel)

        if (version >= 7) setupVersionNumber(modules, version, test)

        mapData(modules, cache, maskPattern)
    }

    val maskPatterns = MaskPattern.values()
    for (i in 0 until 8) {
        make(true, maskPatterns[i])

        val lostPoint = getLostPoint(modules)

        if (i == 0 || minLostPoint > lostPoint) {
            minLostPoint = lostPoint
            bestPattern = maskPatterns[i]
        }
    }

    make(false, bestPattern)
    return modules.map { row -> row.map { it!! } }
}

private const val pad0 = 0xec
private const val pad1 = 0x11

private enum class MaskPattern(val maskFunction: (Int, Int) -> Boolean) {
    Pattern000({ i: Int, j: Int -> (i + j) % 2 == 0 }),
    Pattern001({ i: Int, _: Int -> i % 2 == 0 }),
    Pattern010({ _: Int, j: Int -> j % 3 == 0 }),
    Pattern011({ i: Int, j: Int -> (i + j) % 3 == 0 }),
    Pattern100({ i: Int, j: Int -> ((i / 2) + (j / 3)) % 2 == 0 }),
    Pattern101({ i: Int, j: Int -> ((i * j) % 2) + ((i * j) % 3) == 0 }),
    Pattern110({ i: Int, j: Int -> (((i * j) % 2) + ((i * j) % 3)) % 2 == 0 }),
    Pattern111({ i: Int, j: Int -> (((i * j) % 3) + ((i + j) % 2)) % 2 == 0 }),
}

private fun setupPositionProbePattern(
    modules: List<MutableList<Boolean?>>,
    row: Int,
    col: Int,
) {
    for (r in -1..7) {
        if (row + r <= -1 || modules.size <= row + r) continue

        for (c in -1..7) {
            if (col + c <= -1 || modules.size <= col + c) continue

            modules[row + r][col + c] = (r in 0..6 && (c == 0 || c == 6)) ||
                    (c in 0..6 && (r == 0 || r == 6)) ||
                    (r in 2..4 && 2 <= c && c <= 4)
        }
    }
}

private fun setupPositionAdjustPattern(modules: List<MutableList<Boolean?>>, version: Int) {
    val pos = QRUtil.getPatternPosition(version)

    for (row in pos) {
        for (col in pos) {
            if (modules[row][col] != null) continue

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
    for (r in 8 until modules.size - 8) {
        if (modules[r][6] != null) continue
        modules[r][6] = r % 2 == 0
    }

    for (c in 8 until modules.size - 8) {
        if (modules[6][c] != null) continue
        modules[6][c] = c % 2 == 0
    }
}

private fun setupTypeInfo(
    modules: List<MutableList<Boolean?>>,
    test: Boolean,
    maskPattern: MaskPattern,
    errorCorrectionLevel: ErrorCorrectionLevel,
) {
    val data = errorCorrectionLevel.value.shl(3).or(maskPattern.ordinal)
    val bits = QRUtil.getBchTypeInfo(data)

    // vertical
    for (i in 0 until 15) {
        val mod = !test && bits.shr(i).and(1) == 1

        if (i < 6) {
            modules[i][8] = mod
        } else if (i < 8) {
            modules[i + 1][8] = mod
        } else {
            modules[modules.size - 15 + i][8] = mod
        }
    }

    // horizontal
    for (i in 0 until 15) {
        val mod = !test && bits.shr(i).and(1) == 1

        if (i < 8) {
            modules[8][modules.size - i - 1] = mod
        } else if (i < 9) {
            modules[8][15 - i - 1 + 1] = mod
        } else {
            modules[8][15 - i - 1] = mod
        }
    }

    // fixed module
    modules[modules.size - 8][8] = !test
}

private fun setupVersionNumber(modules: List<MutableList<Boolean?>>, version: Int, test: Boolean) {
    val bits = QRUtil.getBchTypeNumber(version)

    for (i in 0 until 18) {
        val mod = !test && bits.shr(i).and(1) == 1
        modules[i / 3][(i % 3) + modules.size - 8 - 3] = mod
    }

    for (i in 0 until 18) {
        val mod = !test && bits.shr(i).and(1) == 1
        modules[(i % 3) + modules.size - 8 - 3][i / 3] = mod
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
            for (c in 0 until 2) {
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

private fun getLostPoint(modules: List<MutableList<Boolean?>>): Double {
    val size = modules.size
    var lostPoint = .0

    fun isDark(row: Int, col: Int) = modules[row][col]!!

    // LEVEL1
    for (row in 0 until size) {
        for (col in 0 until size) {
            val dark = isDark(row, col)
            var sameCount = 0

            for (r in -1..1) {
                if (row + r < 0 || size <= row + r) continue

                for (c in -1..1) {
                    if (col + c < 0 || size <= col + c) continue
                    if (r == 0 && c == 0) continue

                    if (dark == isDark(row + r, col + c)) sameCount += 1
                }
            }

            if (sameCount > 5) lostPoint += 3 + sameCount - 5
        }
    }

    // LEVEL2
    for (row in 0 until size - 1) {
        for (col in 0 until size - 1) {
            var count = 0
            if (isDark(row, col)) count += 1
            if (isDark(row + 1, col)) count += 1
            if (isDark(row, col + 1)) count += 1
            if (isDark(row + 1, col + 1)) count += 1
            if (count == 0 || count == 4) lostPoint += 3
        }
    }

    // LEVEL3
    for (row in 0 until size) {
        for (col in 0 until size - 6) {
            if (isDark(row, col) &&
                !isDark(row, col + 1) &&
                isDark(row, col + 2) &&
                isDark(row, col + 3) &&
                isDark(row, col + 4) &&
                !isDark(row, col + 5) &&
                isDark(row, col + 6)
            ) {
                lostPoint += 40
            }
        }
    }

    for (col in 0 until size) {
        for (row in 0 until size - 6) {
            if (isDark(row, col) &&
                !isDark(row + 1, col) &&
                isDark(row + 2, col) &&
                isDark(row + 3, col) &&
                isDark(row + 4, col) &&
                !isDark(row + 5, col) &&
                isDark(row + 6, col)
            ) {
                lostPoint += 40
            }
        }
    }

    // LEVEL4
    var darkCount = 0

    for (col in 0 until size) {
        for (row in 0 until size) {
            if (isDark(row, col)) darkCount += 1
        }
    }

    val ratio = ((100 * darkCount) / size / size - 50).absoluteValue / 5
    lostPoint += ratio * 10

    return lostPoint
}

private data class Rs(val dataCount: Int, val totalCount: Int)

private fun createBytes(buffer: QrBitBuffer, rsBlocks: List<Rs>): List<Int> {
    var offset = 0

    var maxDcCount = 0
    var maxEcCount = 0

    val dcData = MutableList(rsBlocks.size) { mutableListOf<Int>() }
    val ecData = MutableList(rsBlocks.size) { mutableListOf<Int>() }

    for (r in rsBlocks.indices) {
        val dcCount = rsBlocks[r].dataCount
        val ecCount = rsBlocks[r].totalCount - dcCount

        maxDcCount = max(maxDcCount, dcCount)
        maxEcCount = max(maxEcCount, ecCount)

        dcData[r] = MutableList(dcCount) { 0 }

        for (i in 0 until dcData[r].size) {
            dcData[r][i] = 0xff.and(buffer.getBuffer()[i + offset])
        }
        offset += dcCount

        val rsPoly = QRUtil.getErrorCorrectPolynomial(ecCount)
        val rawPoly = QrPolynomial(dcData[r], rsPoly.length - 1)

        val modPoly = rawPoly.mod(rsPoly)
        ecData[r] = MutableList(rsPoly.length - 1) { 0 }
        for (i in ecData[r].indices) {
            val modIndex = i + modPoly.length - ecData[r].size
            ecData[r][i] = if (modIndex >= 0) modPoly.getAt(modIndex) else 0
        }
    }

    var totalCodeCount = 0
    for (block in rsBlocks) {
        totalCodeCount += block.totalCount
    }

    val data = MutableList(totalCodeCount) { 0 }
    var index = 0

    for (i in 0 until maxDcCount) {
        for (r in rsBlocks.indices) {
            if (i < dcData[r].size) {
                data[index] = dcData[r][i]
                index += 1
            }
        }
    }

    for (i in 0 until maxEcCount) {
        for (r in rsBlocks.indices) {
            if (i < ecData[r].size) {
                data[index] = ecData[r][i]
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
    val rsBlocks = QRRSBlock.getRsBlocks(version, errorCorrectionLevel)

    val buffer = QrBitBuffer()

    buffer.put(4, 4)
    buffer.put(data.size, QRUtil.getLengthInBits(version))
    buffer.putBytes(data)

    // calc num max data.
    var totalDataCount = 0
    for (block in rsBlocks) {
        totalDataCount += block.dataCount
    }

    if (buffer.lengthInBits > totalDataCount * 8) {
        error(
            "code length overflow. (${buffer.lengthInBits}>${totalDataCount * 8})"
        )
    }

    // end code
    if (buffer.lengthInBits + 4 <= totalDataCount * 8) buffer.put(0, 4)

    // padding
    while (buffer.lengthInBits % 8 != 0) {
        buffer.putBit(false)
    }

    // padding
    while (true) {
        if (buffer.lengthInBits >= totalDataCount * 8) break
        buffer.put(pad0, 8)

        if (buffer.lengthInBits >= totalDataCount * 8) break
        buffer.put(pad1, 8)
    }

    return createBytes(buffer, rsBlocks)
}

private object QRUtil {
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

    private const val g15 = 1.shl(10).or(1.shl(8)).or(1.shl(5)).or(1.shl(4))
        .or(1.shl(2)).or(1.shl(1)).or(1.shl(0))
    private const val g18 = 1.shl(12).or(1.shl(11)).or(1.shl(10)).or(1.shl(9))
        .or(1.shl(8)).or(1.shl(5)).or(1.shl(2)).or(1.shl(0))
    private const val g15Mask = (1.shl(14)).or(1.shl(12)).or(1.shl(10)).or(1.shl(4)).or(1.shl(1))

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

    fun getPatternPosition(version: Int): List<Int> =
        patternPositionTable[version - 1]

    fun getErrorCorrectPolynomial(errorCorrectLength: Int): QrPolynomial {
        var a = QrPolynomial(listOf(1), 0)
        for (i in 0 until errorCorrectLength) {
            a = a.multiply(QrPolynomial(listOf(1, QRMath.gExp(i)), 0))
        }
        return a
    }

    fun getLengthInBits(type: Int): Int {
        return when {
            type in 1..9 -> 8 // 1 - 9
            type < 27 -> 16 // 10 - 26
            type < 41 -> 16 // 27 - 40
            else -> error("type:$type")
        }
    }
}

private object QRMath {
    private val expTable = MutableList(256) { 0 }
    private val logTable = MutableList(256) { 0 }

    init {
        // initialize tables
        for (i in 0 until 8) {
            expTable[i] = 1.shl(i)
        }
        for (i in 8 until 256) {
            expTable[i] =
                expTable[i - 4].xor(expTable[i - 5]).xor(expTable[i - 6]).xor(expTable[i - 8])
        }
        for (i in 0 until 255) {
            logTable[expTable[i]] = i
        }
    }

    fun gLog(n: Int): Int {
        if (n < 1) error("gLog($n)")

        return logTable[n]
    }

    fun gExp(n: Int): Int {
        var i = n

        while (i < 0) {
            i += 255
        }

        while (i >= 256) {
            i -= 255
        }

        return expTable[i]
    }
}

private class QrPolynomial(num: List<Int>, shift: Int) {
    private val _num: MutableList<Int>

    init {
        var offset = 0
        while (offset < num.size && num[offset] == 0) {
            offset += 1
        }

        _num = MutableList(num.size - offset + shift) { 0 }
        for (i in 0 until num.size - offset) {
            _num[i] = num[i + offset]
        }
    }

    fun getAt(index: Int): Int = _num[index]

    val length get() = _num.size

    fun multiply(e: QrPolynomial): QrPolynomial {
        val num = MutableList(length + e.length - 1) { 0 }

        for (i in 0 until length) {
            for (j in 0 until e.length) {
                num[i + j] =
                    num[i + j].xor(QRMath.gExp(QRMath.gLog(getAt(i)) + QRMath.gLog(e.getAt(j))))
            }
        }

        return QrPolynomial(num, 0)
    }

    fun mod(e: QrPolynomial): QrPolynomial {
        if (length - e.length < 0) return this

        val ratio = QRMath.gLog(getAt(0)) - QRMath.gLog(e.getAt(0))

        val num = MutableList(length) { 0 }
        for (i in 0 until length) {
            num[i] = getAt(i)
        }

        for (i in 0 until e.length) {
            num[i] = num[i].xor(QRMath.gExp(QRMath.gLog(e.getAt(i)) + ratio))
        }

        // recursive call
        return QrPolynomial(num, 0).mod(e)
    }
}

private object QRRSBlock {
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

        val list = mutableListOf<Rs>()

        for (i in 0 until length) {
            val count = rsBlock[i * 3 + 0]
            val totalCount = rsBlock[i * 3 + 1]
            val dataCount = rsBlock[i * 3 + 2]

            for (j in 0 until count) {
                list.add(Rs(totalCount = totalCount, dataCount = dataCount))
            }
        }

        return list
    }
}

private class QrBitBuffer {
    private val _buffer = mutableListOf<Int>()
    private var _length = 0

    fun getBuffer(): List<Int> = _buffer

    fun put(num: Int, length: Int) {
        for (i in 0 until length) {
            putBit(((num.ushr(length - i - 1)).and(1) == 1))
        }
    }

    val lengthInBits: Int get() = _length

    fun putBit(bit: Boolean) {
        val bufIndex = _length / 8
        if (_buffer.size <= bufIndex) _buffer.add(0)
        if (bit) _buffer[bufIndex] = _buffer[bufIndex].or(0x80.ushr(_length % 8))
        _length += 1
    }

    fun putBytes(bytes: ByteArray) {
        for (i in bytes.indices) {
            put(bytes[i].toInt(), 8)
        }
    }
}
