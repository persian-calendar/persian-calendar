package com.byagowi.persiancalendar.service

import android.content.Intent
import androidx.car.app.CarAppService
import androidx.car.app.Screen
import androidx.car.app.Session
import androidx.car.app.validation.HostValidator
import com.byagowi.persiancalendar.ui.car.CalendarCarScreen

class PersianCalendarCarService : CarAppService() {
    override fun createHostValidator(): HostValidator {
        return HostValidator.ALLOW_ALL_HOSTS_VALIDATOR
    }

    override fun onCreateSession(): Session {
        return PersianCalendarSession()
    }
}

class PersianCalendarSession : Session() {
    override fun onCreateScreen(intent: Intent): Screen {
        return CalendarCarScreen(carContext)
    }
}
