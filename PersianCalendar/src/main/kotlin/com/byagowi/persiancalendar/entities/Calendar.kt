package com.byagowi.persiancalendar.entities

import com.byagowi.persiancalendar.R

val Calendar.shortTitleId
    get() = when (this) {
        Calendar.SHAMSI -> R.string.persian_calendar_short
        Calendar.ISLAMIC -> R.string.hijri_calendar_short
        Calendar.GREGORIAN -> R.string.gregorian_calendar_short
        Calendar.NEPALI -> R.string.nepali_calendar_short
    }
