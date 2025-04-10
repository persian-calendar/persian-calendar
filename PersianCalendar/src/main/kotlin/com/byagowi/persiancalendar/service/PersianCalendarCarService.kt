//package com.byagowi.persiancalendar.service
//
//import android.content.Intent
//import androidx.car.app.CarAppService
//import androidx.car.app.CarContext
//import androidx.car.app.Screen
//import androidx.car.app.Session
//import androidx.car.app.model.Action
//import androidx.car.app.model.ActionStrip
//import androidx.car.app.model.CarColor
//import androidx.car.app.model.CarIcon
//import androidx.car.app.model.GridItem
//import androidx.car.app.model.GridTemplate
//import androidx.car.app.model.ItemList
//import androidx.car.app.model.ListTemplate
//import androidx.car.app.model.Row
//import androidx.car.app.model.Template
//import androidx.car.app.validation.HostValidator
//import androidx.core.graphics.drawable.IconCompat
//import com.byagowi.persiancalendar.R
//import com.byagowi.persiancalendar.entities.EventsStore
//import com.byagowi.persiancalendar.entities.Jdn
//import com.byagowi.persiancalendar.global.enabledCalendars
//import com.byagowi.persiancalendar.global.eventsRepository
//import com.byagowi.persiancalendar.global.holidayString
//import com.byagowi.persiancalendar.global.isShowDeviceCalendarEvents
//import com.byagowi.persiancalendar.global.language
//import com.byagowi.persiancalendar.global.mainCalendar
//import com.byagowi.persiancalendar.global.spacedComma
//import com.byagowi.persiancalendar.ui.utils.isRtl
//import com.byagowi.persiancalendar.utils.formatDate
//import com.byagowi.persiancalendar.utils.formatNumber
//import com.byagowi.persiancalendar.utils.getDayIconResource
//import com.byagowi.persiancalendar.utils.jdnActionKey
//import com.byagowi.persiancalendar.utils.launchAppPendingIntent
//import com.byagowi.persiancalendar.utils.logException
//import com.byagowi.persiancalendar.utils.monthName
//import com.byagowi.persiancalendar.utils.readTwoWeekDeviceEvents
//
//class PersianCalendarCarService : CarAppService() {
//    override fun createHostValidator(): HostValidator = HostValidator.ALLOW_ALL_HOSTS_VALIDATOR
//
//    override fun onCreateSession(): Session {
//        return object : Session() {
//            override fun onCreateScreen(intent: Intent) = MainCarScreen(carContext)
//        }
//    }
//}
//
//private class MainCarScreen(carContext: CarContext) : Screen(carContext) {
//    override fun onGetTemplate(): Template {
//        val today = Jdn.today()
//        val pendingIntent = carContext.launchAppPendingIntent("CALENDAR", true)
//        val builder = ItemList.Builder()
//        val deviceEvents = if (isShowDeviceCalendarEvents.value) {
//            carContext.applicationContext.readTwoWeekDeviceEvents(today)
//        } else EventsStore.empty()
//        (today..today + 14).flatMap { jdn ->
//            val days = jdn - today
//            val subtitle = jdn.weekDayName + spacedComma + run {
//                if (days == 0) carContext.applicationContext.getString(R.string.today)
//                else carContext.applicationContext.resources.getQuantityString(
//                    R.plurals.days, days, formatNumber(days),
//                )
//            }
//            val events = eventsRepository?.getEvents(jdn, deviceEvents) ?: emptyList()
//            events.map {
//                (if (it.isHoliday) holidayString + spacedComma else "") + it.title
//            }.ifEmpty {
//                if (days == 0) listOf(carContext.applicationContext.getString(R.string.no_event))
//                else emptyList()
//            }.map { Triple(it, subtitle, jdn) }
//        }.take(10).forEach { (title, subtitle, jdn) ->
//            builder.addItem(
//                Row.Builder()
//                    .setTitle(title)
//                    .addText(subtitle)
//                    .setOnClickListener {
//                        runCatching {
//                            val intent = Intent().putExtra(jdnActionKey, jdn.value)
//                            pendingIntent?.send(carContext, 0, intent)
//                        }.onFailure(logException)
//                    }
//                    .build()
//            )
//        }
//        return ListTemplate.Builder()
//            .setActionStrip(
//                ActionStrip.Builder()
//                    .addAction(
//                        Action.Builder()
//                            .setTitle(formatDate(today on enabledCalendars[0]))
//                            .setOnClickListener {
//                                screenManager.push(CalendarCarScreen(carContext))
//                            }
//                            .build()
//                    )
//                    .build()
//            )
//            .setTitle(
//                enabledCalendars.drop(1)
//                    .joinToString(spacedComma) { formatDate(today on it) }
//                    .ifEmpty { carContext.applicationContext.getString(R.string.app_name) }
//            )
//            .setSingleList(builder.build()).build()
//    }
//}
//
//private class CalendarCarScreen(carContext: CarContext) : Screen(carContext) {
//    override fun onGetTemplate(): Template {
//        val today = Jdn.today()
//        val itemList = ItemList.Builder()
//        val pendingIntent = carContext.launchAppPendingIntent("CALENDAR", true)
//        repeat(12) { index ->
//            val jdn = today + if (carContext.applicationContext.resources.isRtl) {
//                (index / 3) * 3 + 2 - (index % 3)
//            } else index
//            val date = jdn on mainCalendar
//            val icon = CarIcon.Builder(
//                IconCompat.createWithResource(
//                    carContext,
//                    getDayIconResource(date.dayOfMonth)
//                )
//            )
//            if (jdn.isWeekEnd || eventsRepository?.getEvents(jdn, EventsStore.empty())
//                    ?.any { it.isHoliday } == true
//            ) icon.setTint(CarColor.SECONDARY)
//            itemList.addItem(
//                GridItem.Builder()
//                    .setImage(icon.build())
//                    .setText(jdn.weekDayName)
//                    .setTitle(date.monthName)
//                    .setOnClickListener {
//                        runCatching {
//                            val intent = Intent().putExtra(jdnActionKey, jdn.value)
//                            pendingIntent?.send(carContext, 0, intent)
//                        }.onFailure(logException)
//                    }
//                    .build()
//            )
//        }
//        val date = today on mainCalendar
//        return GridTemplate.Builder()
//            .setLoading(false)
//            .setTitle(
//                language.value.my.format(
//                    date.monthName,
//                    formatNumber(date.year),
//                )
//            )
//            .setHeaderAction(Action.BACK)
//            .setSingleList(itemList.build())
//            .build()
//    }
//}
