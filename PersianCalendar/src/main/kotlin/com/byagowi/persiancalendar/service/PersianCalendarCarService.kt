package com.byagowi.persiancalendar.service

import android.content.Intent
import androidx.car.app.CarAppService
import androidx.car.app.Session
import androidx.car.app.validation.HostValidator
import com.byagowi.persiancalendar.ui.car.CalendarCarScreen

class PersianCalendarCarService : CarAppService() {
    override fun createHostValidator(): HostValidator = HostValidator.ALLOW_ALL_HOSTS_VALIDATOR

    override fun onCreateSession(): Session {
        return object : Session() {
            override fun onCreateScreen(intent: Intent) = CalendarCarScreen(carContext)
        }
    }
}
