package com.byagowi.persiancalendar.shared

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.byagowi.persiancalendar.ui.converter.QrView
import io.github.persiancalendar.calendar.CivilDate
import io.github.persiancalendar.calendar.PersianDate

@Composable
fun HelloWorld(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column {
            Text(message + "\n 2000-01-01 -> " + PersianDate(CivilDate(2000, 1, 1)))
            QrView("https://example.com") {}
        }

    }
}
