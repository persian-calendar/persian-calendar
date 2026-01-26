package com.byagowi.persiancalendar.entities

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ShiftWorkRecord(val type: String, val length: Int) : Parcelable
