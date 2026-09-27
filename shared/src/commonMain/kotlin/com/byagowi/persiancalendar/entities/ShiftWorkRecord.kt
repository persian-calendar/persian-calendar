package com.byagowi.persiancalendar.entities

import com.byagowi.persiancalendar.parcelize.CommonParcelable
import com.byagowi.persiancalendar.parcelize.CommonParcelize

@CommonParcelize
data class ShiftWorkRecord(val type: String, val length: Int) : CommonParcelable
