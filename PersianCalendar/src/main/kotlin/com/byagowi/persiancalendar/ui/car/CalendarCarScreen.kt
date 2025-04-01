package com.byagowi.persiancalendar.ui.car

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.MessageTemplate
import androidx.car.app.model.Template

class CalendarCarScreen(carContext: CarContext) : Screen(carContext) {

    override fun onGetTemplate(): Template {
        return MessageTemplate.Builder("تقویم فارسی")
            .setTitle("سلام دنیا")
            .build()
    }
}


