package com.byagowi.persiancalendar.ui.common

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalResources
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.coordinates
import com.byagowi.persiancalendar.utils.isSouthernHemisphere
import io.github.cosinekitty.astronomy.Time
import io.github.cosinekitty.astronomy.eclipticGeoMoon
import io.github.cosinekitty.astronomy.sunPosition

@Composable
fun MoonView(jdn: Jdn, modifier: Modifier = Modifier) {
    val solarDraw = SolarDraw(LocalResources.current)
    val time = Time.fromMillisecondsSince1970(jdn.toGregorianCalendar().timeInMillis)
    val eventualValue = eclipticGeoMoon(time).lon - sunPosition(time).elon
    var value by remember { mutableDoubleStateOf(eventualValue - 180) }
    LaunchedEffect(jdn) { value = eventualValue }
    val phase by animateFloatAsState(
        targetValue = value.toFloat(),
        animationSpec = spring(Spring.DampingRatioLowBouncy, Spring.StiffnessLow),
    )
    Canvas(modifier) {
        drawIntoCanvas {
            val cx = size.width / 2f
            solarDraw.moon(
                canvas = it.nativeCanvas,
                phase = phase.toDouble(),
                cx = cx,
                cy = cx,
                r = cx,
                flipHorizontally = coordinates?.isSouthernHemisphere == true,
            )
        }
    }
}
