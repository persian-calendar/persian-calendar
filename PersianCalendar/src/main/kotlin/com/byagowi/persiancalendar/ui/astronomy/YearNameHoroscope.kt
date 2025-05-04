package com.byagowi.persiancalendar.ui.astronomy

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.ui.common.AppDialog
import io.github.persiancalendar.calendar.PersianDate

@Composable
fun YearNameHoroscope(jdn: Jdn = Jdn.today(), onDismissRequest: () -> Unit) {
    val items = run {
        val baseDate = jdn.toPersianDate()
        val resources = LocalContext.current.resources
        (0..<12).map {
            val date = PersianDate(baseDate.year + it, 1, 1)
            ChineseZodiac.fromPersianCalendar(date).format(
                resources = resources,
                withEmoji = true,
                persianDate = date,
                withOldEraName = true,
                separator = "\n",
            )
        }
    }
    AppDialog(onDismissRequest = onDismissRequest) {
        val outline = MaterialTheme.colorScheme.outline
        BoxWithConstraints(
            Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
        ) {
            val size = this.maxHeight
            listOf(
                size / 2 - size / 6 to size / 2 - size / 3,
                size / 2 to size / 2 - size / 3 - size / 6,
                size / 2 - size / 6 + size / 3 to size / 2 - size / 3,
                size / 2 to size / 2 - size / 3 + size / 6,
                size / 2 - size / 6 + size / 3 to size / 2,
                size / 2 to size / 2 + size / 6,
                size / 2 - size / 6 to size / 2,
                size / 2 - size / 3 to size / 2 + size / 6,
                size / 2 - size / 3 - size / 6 to size / 2,
                size / 2 - size / 3 to size / 2 - size / 6,
                size / 2 - size / 3 - size / 6 to size / 2 - size / 3,
                size / 2 - size / 6 - size / 6 to size / 2 - size / 3 - size / 6,
            ).forEachIndexed { i, (x, y) ->
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .offset(x, y)
                        .size(size / 3),
                ) {
                    Text(items[i], textAlign = TextAlign.Center)
                }
            }
            Canvas(Modifier.fillMaxSize()) {
                val (width, height) = this.size
                val strokeWidth = 1.dp.toPx()
                drawLine(outline, Offset.Zero, Offset(width, height), strokeWidth)
                drawLine(outline, Offset(0f, height), Offset(width, 0f), strokeWidth)
                run {
                    val c0 = Offset(0f, height / 2)
                    val c1 = Offset(width / 6, height / 2)
                    val c2 = Offset(width / 2, height / 6)
                    (0..3).forEach {
                        rotate(it * 90f) {
                            drawLine(outline, c0, c1, strokeWidth)
                            drawLine(outline, c1, c2, strokeWidth)
                        }
                    }
                }
            }
        }
    }
}
