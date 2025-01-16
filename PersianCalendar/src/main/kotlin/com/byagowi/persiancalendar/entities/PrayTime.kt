package com.byagowi.persiancalendar.entities

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.ui.graphics.Color
import com.byagowi.persiancalendar.R
import io.github.persiancalendar.praytimes.MidnightMethod
import io.github.persiancalendar.praytimes.PrayTimes

enum class PrayTime(@StringRes val stringRes: Int, val tint: Color = Color.Gray) {
    // Don't ever change name of these, they are stored in preferences
    IMSAK(R.string.imsak),
    FAJR(R.string.fajr, tint = Color(0xFF009788)),
    SUNRISE(R.string.sunrise),
    DHUHR(R.string.dhuhr, tint = Color(0xFFF1A42A)),
    ASR(R.string.asr, tint = Color(0xFFF57C01)),
    SUNSET(R.string.sunset),
    MAGHRIB(R.string.maghrib, tint = Color(0xFF5E35B1)),
    ISHA(R.string.isha, tint = Color(0xFF283593)),
    MIDNIGHT(R.string.midnight);

    val isByPassDnd get() = this == FAJR

    val imageVector
        get() = when (this) {
            DHUHR, ASR -> Icons.Default.Brightness7
            else -> Icons.Default.Brightness4
        }

    val drawable
        get() = when (this) {
            DHUHR, ASR -> R.drawable.brightness7
            else -> R.drawable.brightness4
        }

    fun getFraction(prayTimes: PrayTimes): Double {
        return when (this) {
            IMSAK -> prayTimes.imsak
            FAJR -> prayTimes.fajr
            SUNRISE -> prayTimes.sunrise
            DHUHR -> prayTimes.dhuhr
            ASR -> prayTimes.asr
            SUNSET -> prayTimes.sunset
            MAGHRIB -> prayTimes.maghrib
            ISHA -> prayTimes.isha
            MIDNIGHT -> prayTimes.midnight
        }
    }

    fun getClock(prayTimes: PrayTimes) = Clock.fromHoursFraction(getFraction(prayTimes))

    fun isAlwaysShown(isJafari: Boolean): Boolean {
        return when (this) {
            FAJR, DHUHR, MAGHRIB -> true
            else -> if (isJafari) false else when (this) {
                ASR, ISHA -> true
                else -> false
            }
        }
    }

    companion object {
        // As SUNSET and MAGHRIB are the same in non Jafari methods
        fun allTimes(isJafari: Boolean) = entries.filter { isJafari || it != SUNSET }

        fun fromName(name: String?) = entries.firstOrNull { it.name == name }

        fun pairFromMidnightMethod(method: MidnightMethod): List<PrayTime> {
            return when (method) {
                MidnightMethod.MidSunsetToSunrise -> listOf(SUNSET, SUNRISE)

                MidnightMethod.MidSunsetToFajr -> listOf(SUNSET, FAJR)

                MidnightMethod.MidMaghribToSunrise -> listOf(MAGHRIB, SUNRISE)

                MidnightMethod.MidMaghribToFajr -> listOf(MAGHRIB, FAJR)
            }
        }

        val athans = listOf(FAJR, DHUHR, ASR, MAGHRIB, ISHA)
    }
}
