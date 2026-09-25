package com.byagowi.persiancalendar.entities

import java.util.Locale

fun Numeral.formatLongNumber(value: Long) = format("%,d".format(Locale.ENGLISH, value))
