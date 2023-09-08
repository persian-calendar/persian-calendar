// Copied from https://android.googlesource.com/platform/frameworks/support/+/refs/heads/androidx-main/core/core/src/main/java/androidx/core/graphics/PathParser.java
// We could use it in before but since androidx-core upgrade from 1.10.1 to 1.12.0 we just can't so let's have our copy for now
// Support of 'a' and 'A' commands are removed.
package com.byagowi.persiancalendar.ui.common

import android.graphics.Path
import com.byagowi.persiancalendar.utils.logException
import kotlin.math.min

/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

object PathParser {
    /**
     * Arc support is removed.
     *
     * @param pathData The string representing a path, the same as "d" string in svg file.
     * @return the generated Path object.
     */
    fun createPathFromPathData(pathData: String): Path {
        val path = Path()
        var start = 0
        var end = 1
        val list = ArrayList<PathDataNode>()
        while (end < pathData.length) {
            var c: Char
            while (end < pathData.length) {
                c = pathData[end]
                // Note that 'e' or 'E' are not valid path commands, but could be
                // used for floating point numbers' scientific notation.
                // Therefore, when searching for next command, we should ignore 'e'
                // and 'E'.
                if (((c.code - 'A'.code) * (c.code - 'Z'.code) <= 0 || (c.code - 'a'.code) * (c.code - 'z'.code) <= 0) && c != 'e' && c != 'E') {
                    break
                }
                end++
            }
            val s = pathData.substring(start, end).trim()
            if (s.isNotEmpty()) list.add(PathDataNode(s[0], getFloats(s)))
            start = end
            end++
        }
        if (end - start == 1 && start < pathData.length) {
            list.add(PathDataNode(pathData[start], FloatArray(0)))
        }
        runCatching {
            val current = FloatArray(6)
            var previousCommand = 'm'
            for (node in list) {
                addCommand(path, current, previousCommand, node.mType, node.mParams)
                previousCommand = node.mType
            }
        }.onFailure(logException)
        return path
    }

    /**
     * Parse the floats in the string.
     * This is an optimized version of parseFloat(s.split(",|\\s"));
     *
     * @param s the string containing a command and list of floats
     * @return array of floats
     */
    private fun getFloats(s: String): FloatArray {
        return if (s[0] == 'z' || s[0] == 'Z') {
            FloatArray(0)
        } else try {
            val results = FloatArray(s.length)
            var count = 0
            var startPosition = 1
            var endPosition = 0
            val result = ExtractFloatResult()
            val totalLength = s.length

            // The startPosition should always be the first character of the
            // current number, and endPosition is the character after the current
            // number.
            while (startPosition < totalLength) {
                extract(s, startPosition, result)
                endPosition = result.mEndPosition
                if (startPosition < endPosition) {
                    results[count++] =
                        s.substring(startPosition, endPosition).toFloat()
                }
                startPosition = if (result.mEndWithNegOrDot) {
                    // Keep the '-' or '.' sign with next number.
                    endPosition
                } else {
                    endPosition + 1
                }
            }
            val originalLength = results.size
            val resultLength = count
            val copyLength = min(resultLength, originalLength - 0)
            FloatArray(resultLength).also {
                System.arraycopy(results, 0, it, 0, copyLength)
            }
        } catch (e: NumberFormatException) {
            throw RuntimeException("error in parsing \"$s\"", e)
        }
    }

    /**
     * Calculate the position of the next comma or space or negative sign
     *
     * @param s      the string to search
     * @param start  the position to start searching
     * @param result the result of the extraction, including the position of the
     * the starting position of next number, whether it is ending with a '-'.
     */
    private fun extract(s: String, start: Int, result: ExtractFloatResult) {
        // Now looking for ' ', ',', '.' or '-' from the start.
        var currentIndex = start
        var foundSeparator = false
        result.mEndWithNegOrDot = false
        var secondDot = false
        var isExponential = false
        while (currentIndex < s.length) {
            val isPrevExponential = isExponential
            isExponential = false
            val currentChar = s[currentIndex]
            when (currentChar) {
                ' ', ',' -> foundSeparator = true
                '-' ->                     // The negative sign following a 'e' or 'E' is not a separator.
                    if (currentIndex != start && !isPrevExponential) {
                        foundSeparator = true
                        result.mEndWithNegOrDot = true
                    }

                '.' -> if (!secondDot) {
                    secondDot = true
                } else {
                    // This is the second dot, and it is considered as a separator.
                    foundSeparator = true
                    result.mEndWithNegOrDot = true
                }

                'e', 'E' -> isExponential = true
            }
            if (foundSeparator) {
                break
            }
            currentIndex++
        }
        // When there is nothing found, then we put the end position to the end
        // of the string.
        result.mEndPosition = currentIndex
    }

    private class ExtractFloatResult {
        // We need to return the position of the next separator and whether the
        // next float starts with a '-' or a '.'.
        var mEndPosition = 0
        var mEndWithNegOrDot = false
    }

    /**
     * Each PathDataNode represents one command in the "d" attribute of the svg
     * file.
     * An array of PathDataNode can represent the whole "d" attribute.
     */
    private class PathDataNode(type: Char, params: FloatArray) {
        var mType: Char = type
        var mParams: FloatArray = params
    }

    private fun addCommand(
        path: Path, current: FloatArray,
        previousCmd: Char, cmd: Char, values: FloatArray
    ) {
        var previousCmd = previousCmd
        var incr = 2
        var currentX = current[0]
        var currentY = current[1]
        var ctrlPointX = current[2]
        var ctrlPointY = current[3]
        var currentSegmentStartX = current[4]
        var currentSegmentStartY = current[5]
        var reflectiveCtrlPointX: Float
        var reflectiveCtrlPointY: Float
        when (cmd) {
            'z', 'Z' -> {
                path.close()
                // Path is closed here, but we need to move the pen to the
                // closed position. So we cache the segment's starting position,
                // and restore it here.
                currentX = currentSegmentStartX
                currentY = currentSegmentStartY
                ctrlPointX = currentSegmentStartX
                ctrlPointY = currentSegmentStartY
                path.moveTo(currentX, currentY)
            }

            'm', 'M', 'l', 'L', 't', 'T' -> incr = 2
            'h', 'H', 'v', 'V' -> incr = 1
            'c', 'C' -> incr = 6
            's', 'S', 'q', 'Q' -> incr = 4
        }
        var k = 0
        while (k < values.size) {
            when (cmd) {
                'm' -> {
                    currentX += values[k + 0]
                    currentY += values[k + 1]
                    if (k > 0) {
                        // According to the spec, if a moveto is followed by multiple
                        // pairs of coordinates, the subsequent pairs are treated as
                        // implicit lineto commands.
                        path.rLineTo(values[k + 0], values[k + 1])
                    } else {
                        path.rMoveTo(values[k], values[k + 1])
                        currentSegmentStartX = currentX
                        currentSegmentStartY = currentY
                    }
                }

                'M' -> {
                    currentX = values[k + 0]
                    currentY = values[k + 1]
                    if (k > 0) {
                        // According to the spec, if a moveto is followed by multiple
                        // pairs of coordinates, the subsequent pairs are treated as
                        // implicit lineto commands.
                        path.lineTo(values[k + 0], values[k + 1])
                    } else {
                        path.moveTo(values[k], values[k + 1])
                        currentSegmentStartX = currentX
                        currentSegmentStartY = currentY
                    }
                }

                'l' -> {
                    path.rLineTo(values[k + 0], values[k + 1])
                    currentX += values[k + 0]
                    currentY += values[k + 1]
                }

                'L' -> {
                    path.lineTo(values[k + 0], values[k + 1])
                    currentX = values[k + 0]
                    currentY = values[k + 1]
                }

                'h' -> {
                    path.rLineTo(values[k + 0], 0f)
                    currentX += values[k + 0]
                }

                'H' -> {
                    path.lineTo(values[k + 0], currentY)
                    currentX = values[k + 0]
                }

                'v' -> {
                    path.rLineTo(0f, values[k + 0])
                    currentY += values[k + 0]
                }

                'V' -> {
                    path.lineTo(currentX, values[k + 0])
                    currentY = values[k + 0]
                }

                'c' -> {
                    path.rCubicTo(
                        values[k + 0], values[k + 1], values[k + 2], values[k + 3],
                        values[k + 4], values[k + 5]
                    )
                    ctrlPointX = currentX + values[k + 2]
                    ctrlPointY = currentY + values[k + 3]
                    currentX += values[k + 4]
                    currentY += values[k + 5]
                }

                'C' -> {
                    path.cubicTo(
                        values[k + 0], values[k + 1], values[k + 2], values[k + 3],
                        values[k + 4], values[k + 5]
                    )
                    currentX = values[k + 4]
                    currentY = values[k + 5]
                    ctrlPointX = values[k + 2]
                    ctrlPointY = values[k + 3]
                }

                's' -> {
                    reflectiveCtrlPointX = 0f
                    reflectiveCtrlPointY = 0f
                    if (previousCmd == 'c' || previousCmd == 's' || previousCmd == 'C' || previousCmd == 'S') {
                        reflectiveCtrlPointX = currentX - ctrlPointX
                        reflectiveCtrlPointY = currentY - ctrlPointY
                    }
                    path.rCubicTo(
                        reflectiveCtrlPointX, reflectiveCtrlPointY,
                        values[k + 0], values[k + 1],
                        values[k + 2], values[k + 3]
                    )
                    ctrlPointX = currentX + values[k + 0]
                    ctrlPointY = currentY + values[k + 1]
                    currentX += values[k + 2]
                    currentY += values[k + 3]
                }

                'S' -> {
                    reflectiveCtrlPointX = currentX
                    reflectiveCtrlPointY = currentY
                    if (previousCmd == 'c' || previousCmd == 's' || previousCmd == 'C' || previousCmd == 'S') {
                        reflectiveCtrlPointX = 2 * currentX - ctrlPointX
                        reflectiveCtrlPointY = 2 * currentY - ctrlPointY
                    }
                    path.cubicTo(
                        reflectiveCtrlPointX, reflectiveCtrlPointY,
                        values[k + 0], values[k + 1], values[k + 2], values[k + 3]
                    )
                    ctrlPointX = values[k + 0]
                    ctrlPointY = values[k + 1]
                    currentX = values[k + 2]
                    currentY = values[k + 3]
                }

                'q' -> {
                    path.rQuadTo(values[k + 0], values[k + 1], values[k + 2], values[k + 3])
                    ctrlPointX = currentX + values[k + 0]
                    ctrlPointY = currentY + values[k + 1]
                    currentX += values[k + 2]
                    currentY += values[k + 3]
                }

                'Q' -> {
                    path.quadTo(values[k + 0], values[k + 1], values[k + 2], values[k + 3])
                    ctrlPointX = values[k + 0]
                    ctrlPointY = values[k + 1]
                    currentX = values[k + 2]
                    currentY = values[k + 3]
                }

                't' -> {
                    reflectiveCtrlPointX = 0f
                    reflectiveCtrlPointY = 0f
                    if (previousCmd == 'q' || previousCmd == 't' || previousCmd == 'Q' || previousCmd == 'T') {
                        reflectiveCtrlPointX = currentX - ctrlPointX
                        reflectiveCtrlPointY = currentY - ctrlPointY
                    }
                    path.rQuadTo(
                        reflectiveCtrlPointX, reflectiveCtrlPointY,
                        values[k + 0], values[k + 1]
                    )
                    ctrlPointX = currentX + reflectiveCtrlPointX
                    ctrlPointY = currentY + reflectiveCtrlPointY
                    currentX += values[k + 0]
                    currentY += values[k + 1]
                }

                'T' -> {
                    reflectiveCtrlPointX = currentX
                    reflectiveCtrlPointY = currentY
                    if (previousCmd == 'q' || previousCmd == 't' || previousCmd == 'Q' || previousCmd == 'T') {
                        reflectiveCtrlPointX = 2 * currentX - ctrlPointX
                        reflectiveCtrlPointY = 2 * currentY - ctrlPointY
                    }
                    path.quadTo(
                        reflectiveCtrlPointX, reflectiveCtrlPointY,
                        values[k + 0], values[k + 1]
                    )
                    ctrlPointX = reflectiveCtrlPointX
                    ctrlPointY = reflectiveCtrlPointY
                    currentX = values[k + 0]
                    currentY = values[k + 1]
                }
            }
            previousCmd = cmd
            k += incr
        }
        current[0] = currentX
        current[1] = currentY
        current[2] = ctrlPointX
        current[3] = ctrlPointY
        current[4] = currentSegmentStartX
        current[5] = currentSegmentStartY
    }
}
