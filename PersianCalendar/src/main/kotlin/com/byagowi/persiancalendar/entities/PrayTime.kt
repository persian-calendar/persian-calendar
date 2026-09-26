package com.byagowi.persiancalendar.entities

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.ui.graphics.Color
import com.byagowi.persiancalendar.shared.R
import com.byagowi.persiancalendar.shared.generated.resources.Res
import com.byagowi.persiancalendar.shared.generated.resources.asr
import com.byagowi.persiancalendar.shared.generated.resources.dhuhr
import com.byagowi.persiancalendar.shared.generated.resources.fajr
import com.byagowi.persiancalendar.shared.generated.resources.imsak
import com.byagowi.persiancalendar.shared.generated.resources.isha
import com.byagowi.persiancalendar.shared.generated.resources.maghrib
import com.byagowi.persiancalendar.shared.generated.resources.midnight
import com.byagowi.persiancalendar.shared.generated.resources.sunrise
import com.byagowi.persiancalendar.shared.generated.resources.sunset
import io.github.persiancalendar.praytimes.MidnightMethod
import io.github.persiancalendar.praytimes.PrayTimes
import org.jetbrains.compose.resources.StringResource

enum class PrayTime(val stringRes: StringResource, val tint: Color = Color.Gray) {
    // Don't ever change name of these, they are stored in preferences
    IMSAK(Res.string.imsak),
    FAJR(Res.string.fajr, tint = Color(0xFF009788)),
    SUNRISE(Res.string.sunrise),
    DHUHR(Res.string.dhuhr, tint = Color(0xFFF1A42A)),
    ASR(Res.string.asr, tint = Color(0xFFF57C01)),
    SUNSET(Res.string.sunset),
    MAGHRIB(Res.string.maghrib, tint = Color(0xFF5E35B1)),
    ISHA(Res.string.isha, tint = Color(0xFF283593)),
    MIDNIGHT(Res.string.midnight);

    // Is the time can even bypass silent device or do not disturb device settings
    val isBypassDnd get() = this == FAJR

    val isAthan get() = this in athans

    // Used in days view
    val imageVector
        get() = when (this) {
            DHUHR, ASR -> Icons.Default.Brightness7
            else -> Icons.Default.Brightness4
        }

    // Used in Athan notification
    val drawable
        get() = when (this) {
            DHUHR, ASR -> com.byagowi.persiancalendar.R.drawable.brightness7
            else -> com.byagowi.persiancalendar.R.drawable.brightness4
        }

    // Used in times tab for items that are always shown
    fun isAlwaysShown(isJafari: Boolean): Boolean {
        return when (this) {
            FAJR, DHUHR, MAGHRIB -> true
            else -> if (isJafari) false else when (this) {
                ASR, ISHA -> true
                else -> false
            }
        }
    }

    // Used in Athan notification
    fun upcomingTimes(isJafari: Boolean): List<PrayTime> {
        return when (this) {
            FAJR -> listOf(SUNRISE)
            DHUHR -> if (isJafari) listOf(SUNSET) else listOf(ASR, MAGHRIB)
            ASR -> listOf(MAGHRIB)
            MAGHRIB -> if (isJafari) listOf(MIDNIGHT) else listOf(ISHA, MIDNIGHT)
            ISHA -> listOf(MIDNIGHT)
            else -> listOf(MIDNIGHT)
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

        // Used in widget to show upcoming important time
        val jafariImportantTimes = listOf(FAJR, SUNRISE, DHUHR, SUNSET, MAGHRIB, MIDNIGHT)
        val nonJafariImportantTimes = listOf(FAJR, SUNRISE, DHUHR, ASR, MAGHRIB, ISHA, MIDNIGHT)

        // 4x2 related times to show
        val timesNotBetweenDhuhrAndIshaForJafari = listOf(FAJR, SUNRISE, DHUHR, MAGHRIB, MIDNIGHT)
        val timesBetweenDhuhrAndIshaForJafari = listOf(FAJR, DHUHR, SUNSET, MAGHRIB, MIDNIGHT)

        operator fun PrayTimes.get(prayTime: PrayTime): Clock {
            val value = when (prayTime) {
                IMSAK -> imsak
                FAJR -> fajr
                SUNRISE -> sunrise
                DHUHR -> dhuhr
                ASR -> asr
                SUNSET -> sunset
                MAGHRIB -> maghrib
                ISHA -> isha
                MIDNIGHT -> midnight
            }
            return Clock(value)
        }
    }
}

val PrayTime.stringResId
    get() = when (this) {
        PrayTime.IMSAK -> R.string.imsak
        PrayTime.FAJR -> R.string.fajr
        PrayTime.SUNRISE -> R.string.sunrise
        PrayTime.DHUHR -> R.string.dhuhr
        PrayTime.ASR -> R.string.asr
        PrayTime.SUNSET -> R.string.sunset
        PrayTime.MAGHRIB -> R.string.maghrib
        PrayTime.ISHA -> R.string.isha
        PrayTime.MIDNIGHT -> R.string.midnight
    }
