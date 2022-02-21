package com.byagowi.persiancalendar.ui.calendar.searchevent

import android.content.Context

interface ISearchEventsRepository {
    suspend fun createStore(context: Context): SearchEventsStore
}
