package com.byagowi.persiancalendar.utils

import kotlinx.coroutines.flow.MutableStateFlow

inline fun <T : Any> MutableStateFlow<T>.setState(reducer: T.() -> T) {
    value = reducer(value)
}
