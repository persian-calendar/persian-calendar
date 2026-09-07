package io.github.persiancalendar.praytimes

/** Asr Juristic Methods */
enum class AsrMethod(/* asr shadow factor */internal val asrFactor: Double) {
    /** Shafi`i, Maliki, Ja`fari, Hanbali */
    Standard(1.0),

    /** Hanafi */
    Hanafi(2.0)
}
