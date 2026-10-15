package com.byagowi.persiancalendar.shared

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.byagowi.persiancalendar.ui.common.NumericText
import com.byagowi.persiancalendar.ui.common.RollingDigit
import com.byagowi.persiancalendar.ui.converter.QrView
import io.github.persiancalendar.calendar.CivilDate
import io.github.persiancalendar.calendar.PersianDate
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

@Composable
fun HelloWorld(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column {
            var number by remember { mutableStateOf(1000) }
            LaunchedEffect(Unit) {
                while (true) {
                    --number
                    delay(1.seconds)
                }
            }
            val textStyle = MaterialTheme.typography.displayLarge.copy(color = LocalContentColor.current)
            NumericText(number.coerceAtLeast(0), style = textStyle)
            Text(message + "\n 2000-01-01 -> " + PersianDate(CivilDate(2000, 1, 1)))
            QrView("https://example.com") {}
        }
    }
}
