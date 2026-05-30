package com.byagowi.persiancalendar.ui.astronomy

import androidx.annotation.VisibleForTesting
import com.byagowi.persiancalendar.global.language
import io.github.cosinekitty.astronomy.Body
import io.github.cosinekitty.astronomy.Time

// Arabic lots (سهام) — Abu Ma'shar / classical Islamic tradition for year charts
// https://en.wikipedia.org/wiki/Arabic_parts
// Comprehensive body-pair reference: https://web.archive.org/web/2024/https://public.websites.umich.edu/~pfa/dreamhouse/attic/lots/lotslist.html
// https://alhekma.dk/library/al3nqaa/noqatfalakia.html
// https://raaed.ahlamontada.com/t114-topic
enum class Lot(
    private val arabicTitle: String,
    private val bodies: Pair<Body, Body>,
) {
    // Part of Fortune: day = ASC + ☽ − ☉, night = ASC + ☉ − ☽
    // Moon (body/material fortune) & Sun (vitality); the two luminaries, the foundational lot.
    // https://en.wikipedia.org/wiki/Arabic_parts#Calculating_the_Lot_of_Fortune
    Fortune(arabicTitle = "سهم السعادة", bodies = Body.Moon to Body.Sun),

    // Part of Kings: day = ASC + ☉ − ♃, night = ASC + ♃ − ☉
    // Sun (kingship) & Jupiter (royal benefic) — both diurnal, both associated with rule and authority.
    // Note: classical "Kings" lots (Michigan list) use ☽/♄ or ☽/♂, not ☉/♃. The ☉/♃ pair
    // appears classically as "Pomegranate" with OPPOSITE day/night (day = ASC + ♃ − ☉).
    // A second source (raaed.ahlamontada.com/t114-topic, "سهام الأسعار") confirms سهم الحنطة
    // as day = ASC + ♃ − ☉, which is exactly the nocturnal formula used here for Sultan.
    // The Sultan name and this day/night assignment come from the 1225 manuscript only.
    // ⚠️ DRAFT — brute-forced from the 1225 manuscript; no classical textual source confirms the Sultan name.
    Sultan(arabicTitle = "سهم السلطان", bodies = Body.Sun to Body.Jupiter),

    // Part of Wheat: day = ASC + ☿ − ♄, night = ASC + ♄ − ☿
    // Mercury (commerce, grain trade) & Saturn (agriculture, harvest time).
    // The ☿/♄ pair with the same day/night reversal appears in Abu Ma'shar as "Danger, Violence,
    // Debt" (Michigan lot list, source AL = Albumasar). Different name, same formula structure.
    // Note: raaed.ahlamontada.com/t114-topic attests سهم الحنطة with a DIFFERENT body pair
    // (♃/☉, day = ASC + ♃ − ☉); the ☿/♄ pair here comes from the 1225 manuscript alone.
    // ⚠️ DRAFT — verified against 1225 manuscript result only; no source confirms the ☿/♄ wheat formula.
    Wheat(arabicTitle = "سهم الحنطة", bodies = Body.Mercury to Body.Saturn),

    // Part of Grapes: day = ASC + ♃ − ♀, night = ASC + ♀ − ♃
    // Jupiter & Venus (the two benefics, abundance and pleasure).
    // The ♃/♀ day formula appears in Abu Ma'shar as "Love" (Michigan lot list, source AL).
    // ⚠️ DRAFT — verified against 1225 manuscript result only; no source confirms the grape name.
    Grapes(arabicTitle = "سهم العنب", bodies = Body.Jupiter to Body.Venus);

    fun title() = if (language.isArabicScript) arabicTitle else name

    // Arabic lots (سهام): formula is (ASC + x − y).mod(360), reversed for nocturnal charts
    private fun lot(ascendant: Double, x: Double, y: Double) = (ascendant + x - y).mod(360.0)

    fun calculate(ascendant: Double, isDiurnal: Boolean, time: Time) = lot(
        ascendant = ascendant,
        x = geocentricLongitudeAndDistanceOfBody(
            if (isDiurnal) bodies.first else bodies.second, time,
        ).first,
        y = geocentricLongitudeAndDistanceOfBody(
            if (isDiurnal) bodies.second else bodies.first, time,
        ).first,
    )
}
