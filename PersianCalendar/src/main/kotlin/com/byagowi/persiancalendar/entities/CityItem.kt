package com.byagowi.persiancalendar.entities

import io.github.persiancalendar.praytimes.Coordinates

data class CityItem(
    val key: String, val en: String, val fa: String, val ckb: String, val ar: String,
    val countryCode: String, val countryEn: String, val countryFa: String, val countryCkb: String,
    val countryAr: String, val coordinates: Coordinates
)
