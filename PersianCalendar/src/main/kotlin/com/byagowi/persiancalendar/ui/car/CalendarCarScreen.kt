package com.byagowi.persiancalendar.ui.car

import android.content.Intent
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.ItemList
import androidx.car.app.model.ListTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.EventsStore
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.enabledCalendars
import com.byagowi.persiancalendar.global.eventsRepository
import com.byagowi.persiancalendar.global.holidayString
import com.byagowi.persiancalendar.global.isShowDeviceCalendarEvents
import com.byagowi.persiancalendar.global.spacedComma
import com.byagowi.persiancalendar.utils.formatDate
import com.byagowi.persiancalendar.utils.formatNumber
import com.byagowi.persiancalendar.utils.jdnActionKey
import com.byagowi.persiancalendar.utils.launchAppPendingIntent
import com.byagowi.persiancalendar.utils.logException
import com.byagowi.persiancalendar.utils.readTwoWeekDeviceEvents

class CalendarCarScreen(carContext: CarContext) : Screen(carContext) {

    override fun onGetTemplate(): Template {
        val today = Jdn.today()
        val pendingIntent = carContext.launchAppPendingIntent("CALENDAR", true)
        val builder = ItemList.Builder()
        val deviceEvents = if (isShowDeviceCalendarEvents.value) {
            carContext.applicationContext.readTwoWeekDeviceEvents(today)
        } else EventsStore.empty()
        (today..today + 14).flatMap { jdn ->
            val days = jdn - today
            val subtitle = jdn.weekDayName + spacedComma + run {
                if (days == 0) carContext.applicationContext.getString(R.string.today)
                else carContext.applicationContext.resources.getQuantityString(
                    R.plurals.days, days, formatNumber(days),
                )
            }
            val events = eventsRepository?.getEvents(jdn, deviceEvents) ?: emptyList()
            events.map {
                (if (it.isHoliday) holidayString + spacedComma else "") + it.title
            }.ifEmpty {
                if (days == 0) listOf(carContext.applicationContext.getString(R.string.no_event))
                else emptyList()
            }.map { Triple(it, subtitle, jdn) }
        }.take(10).forEach { (title, subtitle, jdn) ->
            builder.addItem(
                Row.Builder()
                    .setTitle(title)
                    .addText(subtitle)
                    .setOnClickListener {
                        runCatching {
                            val intent = Intent().putExtra(jdnActionKey, jdn.value)
                            pendingIntent?.send(carContext, 0, intent)
                        }.onFailure(logException)
                    }
                    .build()
            )
        }
        return ListTemplate.Builder()
            .setTitle(enabledCalendars.joinToString(spacedComma) { formatDate(today on it) })
            .setSingleList(builder.build()).build()
    }
}


