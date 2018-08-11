package com.byagowi.persiancalendar.entity

import com.github.praytimes.Coordinate

data class CityEntity(val key: String, val en: String, val fa: String, val ckb: String,
                      val countryCode: String,
                      val countryEn: String, val countryFa: String, val countryCkb: String,
                      val coordinate: Coordinate)