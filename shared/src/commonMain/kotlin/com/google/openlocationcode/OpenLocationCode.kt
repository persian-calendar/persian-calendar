// Ported from https://github.com/google/open-location-code/blob/f72e2a7/java/src/main/java/com/google/openlocationcode/OpenLocationCode.java
// Tests are in https://github.com/persian-calendar/open-location-code/tree/main/kotlin/src/commonTest/kotlin/com/google/openlocationcode

// Copyright 2014 Google Inc. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.openlocationcode

import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

/**
 * Convert locations to and from convenient short codes.
 *
 * Plus Codes are short, ~10 character codes that can be used instead of street addresses. The
 * codes can be generated and decoded offline, and use a reduced character set that minimises the
 * chance of codes including words.
 *
 * This provides both object and static methods.
 *
 * Create an object with:
 * ```
 * val code = OpenLocationCode("7JVW52GR+2V")
 * val code = OpenLocationCode("52GR+2V")
 * val code = OpenLocationCode(27.175063, 78.042188)
 * val code = OpenLocationCode(27.175063, 78.042188, 11)
 * ```
 *
 * Once you have a code object, you can apply the other methods to it, such as to shorten:
 * `code.shorten(27.176, 78.05)`
 *
 * Recover the nearest match (if the code was a short code): `code.recover(27.176, 78.05)`
 *
 * Or decode a code into its coordinates, returning a [CodeArea] object: `code.decode()`
 */
class OpenLocationCode(originalCode: String) {

    /** The current code for objects. */
    val code: String = originalCode.uppercase()

    init {
        if (!isValidCode(code)) throw IllegalArgumentException(
            "The provided code '$originalCode' is not a valid Open Location Code.",
        )
    }

    /**
     * Creates Open Location Code.
     *
     * @param latitude The latitude in decimal degrees.
     * @param longitude The longitude in decimal degrees.
     * @param codeLength The desired number of digits in the code.
     * @throws IllegalArgumentException if the code length is not valid.
     */
    constructor(latitude: Double, longitude: Double, codeLength: Int) : this(
        encodeFromDegrees(
            latitude,
            longitude,
            codeLength,
        ),
    )

    /**
     * Creates Open Location Code with the default precision length.
     *
     * @param latitude The latitude in decimal degrees.
     * @param longitude The longitude in decimal degrees.
     */
    constructor(latitude: Double, longitude: Double) : this(
        latitude,
        longitude,
        CODE_PRECISION_NORMAL,
    )

    /**
     * Decodes this [OpenLocationCode] object into a [CodeArea] object encapsulating
     * latitude/longitude bounding box.
     *
     * @return A CodeArea object.
     */
    fun decode(): CodeArea {
        if (!isFullCode(code)) {
            throw IllegalStateException(
                "Method decode() could only be called on valid full codes, code was $code.",
            )
        }
        // Strip padding and separator characters out of the code.
        val clean = code.replace(SEPARATOR.toString(), "").replace(PADDING_CHARACTER.toString(), "")

        // Initialise the values. We work them out as integers and convert them to doubles at the end.
        var latVal = -LATITUDE_MAX * LAT_INTEGER_MULTIPLIER
        var lngVal = -LONGITUDE_MAX * LNG_INTEGER_MULTIPLIER
        // Define the place value for the digits. We'll divide this down as we work through the code.
        var latPlaceVal = LAT_MSP_VALUE
        var lngPlaceVal = LNG_MSP_VALUE
        var i = 0
        while (i < min(clean.length, PAIR_CODE_LENGTH)) {
            latPlaceVal /= ENCODING_BASE
            lngPlaceVal /= ENCODING_BASE
            latVal += CODE_ALPHABET.indexOf(clean[i]) * latPlaceVal
            lngVal += CODE_ALPHABET.indexOf(clean[i + 1]) * lngPlaceVal
            i += 2
        }
        i = PAIR_CODE_LENGTH
        while (i < min(clean.length, MAX_DIGIT_COUNT)) {
            latPlaceVal /= GRID_ROWS
            lngPlaceVal /= GRID_COLUMNS
            val digit = CODE_ALPHABET.indexOf(clean[i])
            val row = digit / GRID_COLUMNS
            val col = digit % GRID_COLUMNS
            latVal += row * latPlaceVal
            lngVal += col * lngPlaceVal
            i++
        }
        val latitudeLo = latVal.toDouble() / LAT_INTEGER_MULTIPLIER
        val longitudeLo = lngVal.toDouble() / LNG_INTEGER_MULTIPLIER
        val latitudeHi = (latVal + latPlaceVal).toDouble() / LAT_INTEGER_MULTIPLIER
        val longitudeHi = (lngVal + lngPlaceVal).toDouble() / LNG_INTEGER_MULTIPLIER
        return CodeArea(
            southLatitude = latitudeLo,
            westLongitude = longitudeLo,
            northLatitude = latitudeHi,
            eastLongitude = longitudeHi,
            length = min(clean.length, MAX_DIGIT_COUNT),
        )
    }

    /**
     * Returns whether this [OpenLocationCode] is a full Open Location Code.
     *
     * @return True if it is a full code.
     */
    fun isFull(): Boolean = code.indexOf(SEPARATOR) == SEPARATOR_POSITION

    /**
     * Returns whether this [OpenLocationCode] is a short Open Location Code.
     *
     * @return True if it is short.
     */
    fun isShort(): Boolean = code.indexOf(SEPARATOR) in 0..<SEPARATOR_POSITION

    /**
     * Returns whether this [OpenLocationCode] is a padded Open Location Code, meaning that it
     * contains less than 8 valid digits.
     *
     * @return True if this code is padded.
     */
    private fun isPadded(): Boolean = code.indexOf(PADDING_CHARACTER) >= 0

    /**
     * Returns short [OpenLocationCode] from the full Open Location Code created by removing
     * four or six digits, depending on the provided reference point. It removes as many digits as
     * possible.
     *
     * @param referenceLatitude Degrees.
     * @param referenceLongitude Degrees.
     * @return A short code if possible.
     */
    fun shorten(referenceLatitude: Double, referenceLongitude: Double): OpenLocationCode {
        if (!isFull()) {
            throw IllegalStateException("shorten() method could only be called on a full code.")
        }
        if (isPadded()) {
            throw IllegalStateException("shorten() method can not be called on a padded code.")
        }

        val codeArea = decode()
        val range = max(
            abs(referenceLatitude - codeArea.centerLatitude),
            abs(referenceLongitude - codeArea.centerLongitude),
        )
        // We are going to check to see if we can remove three pairs, two pairs or just one pair of
        // digits from the code.
        (4 downTo 1).forEach { i ->
            // Check if we're close enough to shorten. The range must be less than 1/2
            // the precision to shorten at all, and we want to allow some safety, so
            // use 0.3 instead of 0.5 as a multiplier.
            if (range < computeLatitudePrecision(i * 2) * 0.3) {
                // We're done.
                return OpenLocationCode(code.substring(i * 2))
            }
        }
        throw IllegalArgumentException(
            "Reference location is too far from the Open Location Code center.",
        )
    }

    /**
     * Returns an [OpenLocationCode] object representing a full Open Location Code from this
     * (short) Open Location Code, given the reference location.
     *
     * @param referenceLatitude Degrees.
     * @param referenceLongitude Degrees.
     * @return The nearest matching full code.
     */
    fun recover(referenceLatitude: Double, referenceLongitude: Double): OpenLocationCode {
        if (isFull()) {
            // Note: each code is either full xor short, no other option.
            return this
        }
        val clippedLatitude = clipLatitude(referenceLatitude)
        val normalizedLongitude = normalizeLongitude(referenceLongitude)

        val digitsToRecover = SEPARATOR_POSITION - code.indexOf(SEPARATOR)
        // The precision (height and width) of the missing prefix in degrees.
        val prefixPrecision = ENCODING_BASE.toDouble().pow(2 - (digitsToRecover / 2))

        // Use the reference location to generate the prefix.
        val recoveredPrefix = OpenLocationCode(clippedLatitude, normalizedLongitude).code.substring(
            0,
            digitsToRecover,
        )
        // Combine the prefix with the short code and decode it.
        val recovered = OpenLocationCode(recoveredPrefix + code)
        val recoveredCodeArea = recovered.decode()
        // Work out whether the new code area is too far from the reference location. If it is, we
        // move it. It can only be out by a single precision step.
        var recoveredLatitude = recoveredCodeArea.centerLatitude
        var recoveredLongitude = recoveredCodeArea.centerLongitude

        // Move the recovered latitude by one precision up or down if it is too far from the
        // reference, unless doing so would lead to an invalid latitude.
        val latitudeDiff = recoveredLatitude - clippedLatitude
        if (latitudeDiff > prefixPrecision / 2 && recoveredLatitude - prefixPrecision > -LATITUDE_MAX) {
            recoveredLatitude -= prefixPrecision
        } else if (latitudeDiff < -prefixPrecision / 2 && recoveredLatitude + prefixPrecision < LATITUDE_MAX) {
            recoveredLatitude += prefixPrecision
        }

        // Move the recovered longitude by one precision up or down if it is too far from the
        // reference.
        val longitudeDiff = recoveredCodeArea.centerLongitude - normalizedLongitude
        if (longitudeDiff > prefixPrecision / 2) {
            recoveredLongitude -= prefixPrecision
        } else if (longitudeDiff < -prefixPrecision / 2) {
            recoveredLongitude += prefixPrecision
        }

        return OpenLocationCode(recoveredLatitude, recoveredLongitude, recovered.code.length - 1)
    }

    /**
     * Returns whether the bounding box specified by the Open Location Code contains provided point.
     *
     * @param latitude Degrees.
     * @param longitude Degrees.
     * @return True if the coordinates are contained by the code.
     */
    fun contains(latitude: Double, longitude: Double): Boolean {
        val codeArea = decode()
        return codeArea.southLatitude <= latitude && latitude < codeArea.northLatitude && codeArea.westLongitude <= longitude && longitude < codeArea.eastLongitude
    }

    override fun equals(other: Any?): Boolean =
        this === other || (other is OpenLocationCode && code == other.code)

    override fun hashCode(): Int = code.hashCode()

    override fun toString(): String = code

    /**
     * Coordinates of a decoded Open Location Code.
     *
     * The coordinates include the latitude and longitude of the lower left and upper right corners
     * and the center of the bounding box for the area the code represents.
     */
    class CodeArea(
        val southLatitude: Double,
        val westLongitude: Double,
        val northLatitude: Double,
        val eastLongitude: Double,
        val length: Int,
    ) {
        val latitudeHeight: Double
            get() = northLatitude - southLatitude

        val longitudeWidth: Double
            get() = eastLongitude - westLongitude

        val centerLatitude: Double
            get() = (southLatitude + northLatitude) / 2

        val centerLongitude: Double
            get() = (westLongitude + eastLongitude) / 2
    }

    companion object {
        // Provides a normal precision code, approximately 14x14 meters.
        const val CODE_PRECISION_NORMAL = 10

        // The character set used to encode the values.
        const val CODE_ALPHABET = "23456789CFGHJMPQRVWX"

        // A separator used to break the code into two parts to aid memorability.
        const val SEPARATOR = '+'

        // The character used to pad codes.
        const val PADDING_CHARACTER = '0'

        // The number of characters to place before the separator.
        private const val SEPARATOR_POSITION = 8

        // The minimum number of digits in a Plus Code.
        const val MIN_DIGIT_COUNT = 2

        // The max number of digits to process in a Plus Code.
        const val MAX_DIGIT_COUNT = 15

        // Maximum code length using just lat/lng pair encoding.
        private const val PAIR_CODE_LENGTH = 10

        // Number of digits in the grid coding section.
        private const val GRID_CODE_LENGTH = MAX_DIGIT_COUNT - PAIR_CODE_LENGTH

        // The base to use to convert numbers to/from.
        private val ENCODING_BASE = CODE_ALPHABET.length

        // The maximum value for latitude in degrees.
        private const val LATITUDE_MAX = 90L

        // The maximum value for longitude in degrees.
        private const val LONGITUDE_MAX = 180L

        // Number of columns in the grid refinement method.
        private const val GRID_COLUMNS = 4

        // Number of rows in the grid refinement method.
        private const val GRID_ROWS = 5

        // Value to multiple latitude degrees to convert it to an integer with the maximum encoding
        // precision. I.e. ENCODING_BASE**3 * GRID_ROWS**GRID_CODE_LENGTH
        private const val LAT_INTEGER_MULTIPLIER = 8000L * 3125L

        // Value to multiple longitude degrees to convert it to an integer with the maximum encoding
        // precision. I.e. ENCODING_BASE**3 * GRID_COLUMNS**GRID_CODE_LENGTH
        private const val LNG_INTEGER_MULTIPLIER = 8000L * 1024L

        // Value of the most significant latitude digit after it has been converted to an integer.
        private val LAT_MSP_VALUE = LAT_INTEGER_MULTIPLIER * ENCODING_BASE * ENCODING_BASE

        // Value of the most significant longitude digit after it has been converted to an integer.
        private val LNG_MSP_VALUE = LNG_INTEGER_MULTIPLIER * ENCODING_BASE * ENCODING_BASE

        /**
         * Encodes latitude/longitude into 10 digit Open Location Code. This method is equivalent to
         * creating the OpenLocationCode object and getting the code from it.
         *
         * @param latitude The latitude in decimal degrees.
         * @param longitude The longitude in decimal degrees.
         * @return The code.
         */
        fun encode(latitude: Double, longitude: Double): String =
            OpenLocationCode(latitude, longitude).code

        /**
         * Encodes latitude/longitude into Open Location Code of the provided length. This method is
         * equivalent to creating the OpenLocationCode object and getting the code from it.
         *
         * @param latitude The latitude in decimal degrees.
         * @param longitude The longitude in decimal degrees.
         * @param codeLength The number of digits in the returned code.
         * @return The code.
         */
        fun encode(latitude: Double, longitude: Double, codeLength: Int): String =
            OpenLocationCode(latitude, longitude, codeLength).code

        /**
         * Decodes code representing Open Location Code into [CodeArea] object encapsulating
         * latitude/longitude bounding box.
         *
         * @param code Open Location Code to be decoded.
         * @return A CodeArea object.
         * @throws IllegalArgumentException if the provided code is not a valid Open Location Code.
         */
        fun decode(code: String): CodeArea = OpenLocationCode(code).decode()

        /**
         * Returns whether the provided Open Location Code is a full Open Location Code.
         *
         * @param code The code to check.
         * @return True if it is a full code.
         */
        fun isFull(code: String): Boolean = OpenLocationCode(code).isFull()

        /**
         * Returns whether the provided Open Location Code is a short Open Location Code.
         *
         * @param code The code to check.
         * @return True if it is short.
         */
        fun isShort(code: String): Boolean = OpenLocationCode(code).isShort()

        /**
         * Returns whether the provided Open Location Code is a padded Open Location Code, meaning
         * that it contains less than 8 valid digits.
         *
         * @param code The code to check.
         * @return True if it is padded.
         */
        fun isPadded(code: String): Boolean = OpenLocationCode(code).isPadded()

        /**
         * Returns whether the provided string is a valid Open Location code.
         *
         * @param code The code to check.
         * @return True if it is a valid full or short code.
         */
        fun isValidCode(code: String?): Boolean {
            if (code == null || code.length < 2) return false
            val upperCode = code.uppercase()

            // There must be exactly one separator.
            val separatorPosition = upperCode.indexOf(SEPARATOR)
            if (separatorPosition == -1) return false
            if (separatorPosition != upperCode.lastIndexOf(SEPARATOR)) return false
            // There must be an even number of at most 8 characters before the separator.
            if (separatorPosition % 2 != 0 || separatorPosition > SEPARATOR_POSITION) return false

            // Check first two characters: only some values from the alphabet are permitted.
            if (separatorPosition == SEPARATOR_POSITION) {
                // First latitude character can only have first 9 values.
                if (CODE_ALPHABET.indexOf(upperCode[0]) > 8) return false

                // First longitude character can only have first 18 values.
                if (CODE_ALPHABET.indexOf(upperCode[1]) > 17) return false
            }

            // Check the characters before the separator.
            var paddingStarted = false
            repeat(separatorPosition) { i ->
                if (CODE_ALPHABET.indexOf(upperCode[i]) == -1 && upperCode[i] != PADDING_CHARACTER) return false // Invalid character.
                if (paddingStarted) {
                    // Once padding starts, there must not be anything but padding.
                    if (upperCode[i] != PADDING_CHARACTER) return false
                } else if (upperCode[i] == PADDING_CHARACTER) {
                    paddingStarted = true
                    // Short codes cannot have padding
                    if (separatorPosition < SEPARATOR_POSITION) return false
                    // Padding can start on even character: 2, 4 or 6.
                    if (i != 2 && i != 4 && i != 6) return false
                }
            }

            // Check the characters after the separator.
            if (upperCode.length > separatorPosition + 1) {
                if (paddingStarted) return false
                // Only one character after separator is forbidden.
                if (upperCode.length == separatorPosition + 2) return false
                (separatorPosition + 1..<upperCode.length).forEach { i ->
                    if (CODE_ALPHABET.indexOf(upperCode[i]) == -1) return false
                }
            }

            return true
        }

        /**
         * Returns if the code is a valid full Open Location Code.
         *
         * @param code The code to check.
         * @return True if it is a valid full code.
         */
        fun isFullCode(code: String): Boolean = try {
            OpenLocationCode(code).isFull()
        } catch (e: IllegalArgumentException) {
            false
        }

        /**
         * Returns if the code is a valid short Open Location Code.
         *
         * @param code The code to check.
         * @return True if it is a valid short code.
         */
        fun isShortCode(code: String): Boolean = try {
            OpenLocationCode(code).isShort()
        } catch (e: IllegalArgumentException) {
            false
        }

        // Private static methods.

        /**
         * Convert latitude and longitude in degrees into the integer values needed for reliable
         * encoding. (To avoid floating point precision errors.)
         *
         * @param latitude The latitude in decimal degrees.
         * @param longitude The longitude in decimal degrees.
         * @return A list of [latitude, longitude] in clipped, normalised integer values.
         */
        internal fun degreesToIntegers(latitude: Double, longitude: Double): LongArray {
            var lat = floor(latitude * LAT_INTEGER_MULTIPLIER).toLong()
            var lng = floor(longitude * LNG_INTEGER_MULTIPLIER).toLong()

            // Clip and normalise values.
            lat += LATITUDE_MAX * LAT_INTEGER_MULTIPLIER
            if (lat < 0) {
                lat = 0
            } else if (lat >= 2 * LATITUDE_MAX * LAT_INTEGER_MULTIPLIER) {
                lat = 2 * LATITUDE_MAX * LAT_INTEGER_MULTIPLIER - 1
            }
            lng += LONGITUDE_MAX * LNG_INTEGER_MULTIPLIER
            if (lng < 0) {
                lng =
                    lng % (2 * LONGITUDE_MAX * LNG_INTEGER_MULTIPLIER) + 2 * LONGITUDE_MAX * LNG_INTEGER_MULTIPLIER
            } else if (lng >= 2 * LONGITUDE_MAX * LNG_INTEGER_MULTIPLIER) {
                lng = lng % (2 * LONGITUDE_MAX * LNG_INTEGER_MULTIPLIER)
            }
            return longArrayOf(lat, lng)
        }

        /**
         * Encode a location specified with integer values and return the code.
         *
         * @param lat The latitude as a positive integer.
         * @param lng The longitude as a positive integer.
         * @param codeLength The requested number of digits.
         * @return The OLC for the location.
         * @throws IllegalArgumentException if the code length is not valid.
         */
        internal fun encodeIntegers(lat: Long, lng: Long, codeLength: Int): String {
            var lat = lat
            var lng = lng
            // Limit the maximum number of digits in the code.
            var codeLength = min(codeLength, MAX_DIGIT_COUNT)
            // Check that the code length requested is valid.
            if (codeLength < PAIR_CODE_LENGTH && codeLength % 2 == 1 || codeLength < MIN_DIGIT_COUNT) {
                throw IllegalArgumentException("Illegal code length $codeLength")
            }

            // Store the code - we build it in reverse and reorder it afterward.
            val revCodeBuilder = StringBuilder()
            // Compute the grid part of the code if necessary.
            if (codeLength > PAIR_CODE_LENGTH) repeat(GRID_CODE_LENGTH) {
                val latDigit = lat % GRID_ROWS
                val lngDigit = lng % GRID_COLUMNS
                val ndx = (latDigit * GRID_COLUMNS + lngDigit).toInt()
                revCodeBuilder.append(CODE_ALPHABET[ndx])
                lat /= GRID_ROWS
                lng /= GRID_COLUMNS
            } else {
                lat = (lat / GRID_ROWS.toDouble().pow(GRID_CODE_LENGTH)).toLong()
                lng = (lng / GRID_COLUMNS.toDouble().pow(GRID_CODE_LENGTH)).toLong()
            }
            // Compute the pair section of the code.
            repeat(PAIR_CODE_LENGTH / 2) { i ->
                revCodeBuilder.append(CODE_ALPHABET[(lng % ENCODING_BASE).toInt()])
                revCodeBuilder.append(CODE_ALPHABET[(lat % ENCODING_BASE).toInt()])
                lat /= ENCODING_BASE
                lng /= ENCODING_BASE
                // If we are at the separator position, add the separator.
                if (i == 0) revCodeBuilder.append(SEPARATOR)
            }
            // Reverse the code.
            val codeBuilder = revCodeBuilder.reverse()

            // If we need to pad the code, replace some of the digits.
            if (codeLength < SEPARATOR_POSITION) {
                (codeLength..<SEPARATOR_POSITION).forEach { i ->
                    codeBuilder[i] = PADDING_CHARACTER
                }
            }
            return codeBuilder.subSequence(0, max(SEPARATOR_POSITION + 1, codeLength + 1))
                .toString()
        }

        private fun encodeFromDegrees(
            latitude: Double,
            longitude: Double,
            codeLength: Int,
        ): String {
            val integers = degreesToIntegers(latitude, longitude)
            return encodeIntegers(integers[0], integers[1], codeLength)
        }

        private fun clipLatitude(latitude: Double): Double =
            min(max(latitude, -LATITUDE_MAX.toDouble()), LATITUDE_MAX.toDouble())

        private fun normalizeLongitude(longitude: Double): Double {
            if (longitude >= -LONGITUDE_MAX && longitude < LONGITUDE_MAX) {
                // longitude is within proper range, no normalization necessary
                return longitude
            }

            // % in Java uses truncated division with the remainder having the same sign as
            // the dividend. For any input longitude < -360, the result of longitude%CIRCLE_DEG
            // will still be negative but > -360, so we need to add 360 and apply % a second time.
            val CIRCLE_DEG = 2 * LONGITUDE_MAX // 360 degrees
            return (longitude % CIRCLE_DEG + CIRCLE_DEG + LONGITUDE_MAX) % CIRCLE_DEG - LONGITUDE_MAX
        }

        /**
         * Compute the latitude precision value for a given code length. Lengths <= 10 have the same
         * precision for latitude and longitude, but lengths > 10 have different precisions due to the
         * grid method having fewer columns than rows. Copied from the JS implementation.
         */
        private fun computeLatitudePrecision(codeLength: Int): Double {
            if (codeLength <= CODE_PRECISION_NORMAL) {
                return ENCODING_BASE.toDouble().pow(codeLength / -2 + 2)
            }
            return ENCODING_BASE.toDouble().pow(-3) / GRID_ROWS.toDouble()
                .pow(codeLength - PAIR_CODE_LENGTH)
        }
    }
}
