package com.byagowi.persiancalendar.entities

import androidx.compose.runtime.saveable.listSaver
import com.byagowi.persiancalendar.utils.debugAssertNotNull

data class ShiftWorkRecord(val type: String, val length: Int) {
    companion object {
        private const val TYPE_KEY = "type"
        private const val LENGTH_KEY = "length"
        val saver = listSaver<MutableList<ShiftWorkRecord>, Map<String, *>>(
            save = { list ->
                list.map { mapOf(TYPE_KEY to it.type, LENGTH_KEY to it.length) }
            },
            restore = { list ->
                list.map {
                    ShiftWorkRecord(
                        type = (it[TYPE_KEY] as? String).debugAssertNotNull ?: "",
                        length = (it[LENGTH_KEY] as? Int).debugAssertNotNull ?: 0,
                    )
                }.toMutableList()
            },
        )
    }
}
