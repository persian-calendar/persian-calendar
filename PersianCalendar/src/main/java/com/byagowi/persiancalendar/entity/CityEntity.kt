package com.byagowi.persiancalendar.entity

import com.github.praytimes.Coordinate

/**
 * Created by ebraminio on 2/18/16.
 */
class CityEntity(var key: String?, var en: String?, var fa: String?, var ckb: String?, var countryCode: String?,
                 var countryEn: String?, var countryFa: String?, var countryCkb: String?,
                 coordinate: Coordinate) {
    var coordinate: Coordinate? = null
        private set

    init {
        this.coordinate = coordinate
    }
}