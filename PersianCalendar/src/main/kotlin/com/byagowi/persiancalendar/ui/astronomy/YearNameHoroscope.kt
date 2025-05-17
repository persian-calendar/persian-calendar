package com.byagowi.persiancalendar.ui.astronomy

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.ui.common.AppDialog
import io.github.persiancalendar.calendar.PersianDate

// See the following for more information:
// * https://github.com/user-attachments/assets/5f42c377-3f39-4000-b79c-08cbbf76fc07
// * https://github.com/user-attachments/assets/21eadf3f-c780-470d-91b0-a0e504689198
// See for example: https://w.wiki/E9uz
// See also: https://agnastrology.ir/بهینه-سازی-فروش/
@Composable
fun YearNameHoroscope(jdn: Jdn = Jdn.today(), onDismissRequest: () -> Unit) {
    val language by language.collectAsState()
    val items = run {
        val baseDate = jdn.toPersianDate()
        val resources = LocalContext.current.resources
        (0..<12).map {
            val date = PersianDate(baseDate.year + it, 1, 1)
            ChineseZodiac.fromPersianCalendar(date).format(
                resources = resources,
                withEmoji = true,
                persianDate = date,
                withOldEraName = language.isUserAbleToReadPersian,
                separator = "\n",
            )
        }
    }
    val originalDirection = LocalLayoutDirection.current
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        AppDialog(onDismissRequest = onDismissRequest) {
            val outline = MaterialTheme.colorScheme.outline
            BoxWithConstraints(
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
            ) {
                listOf(
                    /* 1*/1f / 2 - 1f / 6 to 1f / 2 - 1f / 3,
                    /* 2*/1f / 2 - 1f / 6 - 1f / 6 to 1f / 2 - 1f / 3 - 1f / 6,
                    /* 3*/1f / 2 - 1f / 3 - 1f / 6 to 1f / 2 - 1f / 3,
                    /* 4*/1f / 2 - 1f / 3 to 1f / 2 - 1f / 6,
                    /* 5*/1f / 2 - 1f / 3 - 1f / 6 to 1f / 2,
                    /* 6*/1f / 2 - 1f / 3 to 1f / 2 + 1f / 6,
                    /* 7*/1f / 2 - 1f / 6 to 1f / 2,
                    /* 8*/1f / 2 to 1f / 2 + 1f / 6,
                    /* 9*/1f / 2 - 1f / 6 + 1f / 3 to 1f / 2,
                    /*10*/1f / 2 to 1f / 2 - 1f / 3 + 1f / 6,
                    /*11*/1f / 2 - 1f / 6 + 1f / 3 to 1f / 2 - 1f / 3,
                    /*12*/1f / 2 to 1f / 2 - 1f / 3 - 1f / 6,
                ).forEachIndexed { i, (x, y) ->
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .absoluteOffset(this.maxWidth * x, this.maxHeight * y)
                            .size(this.maxWidth / 3),
                    ) {
                        CompositionLocalProvider(LocalLayoutDirection provides originalDirection) {
                            Text(items[i], textAlign = TextAlign.Center)
                        }
                    }
                }
                Canvas(Modifier.fillMaxSize()) {
                    val oneDp = 1.dp.toPx()
                    val sizePx = size.width
                    val c0 = Offset(0f, sizePx / 2)
                    val c1 = Offset(sizePx / 6, sizePx / 2)
                    val c2 = Offset(sizePx / 2, sizePx / 6)
                    val c3 = Offset(sizePx / 6 + 3 * oneDp, sizePx / 2)
                    val c4 = Offset(sizePx / 2, sizePx / 6 + 3 * oneDp)
                    val c5 = Offset(sizePx / 2, sizePx / 2)
                    (0..3).forEach {
                        rotate(it * 90f) {
                            drawLine(outline, Offset.Zero, c5, oneDp)
                            drawLine(outline, c0, c1, oneDp)
                            drawLine(outline, c1, c2, oneDp)
                            drawLine(outline, c3, c4, oneDp)
                        }
                    }
                }
            }
        }
    }
}
