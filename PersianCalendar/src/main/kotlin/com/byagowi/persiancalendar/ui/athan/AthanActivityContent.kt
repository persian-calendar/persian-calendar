package com.byagowi.persiancalendar.ui.athan

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.byagowi.persiancalendar.FAJR_KEY
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Theme
import com.byagowi.persiancalendar.utils.TWO_SECONDS_IN_MILLIS
import com.byagowi.persiancalendar.utils.getPrayTimeName
import com.google.accompanist.drawablepainter.rememberDrawablePainter

@Composable
fun AthanActivityContent(prayerKey: String, cityName: String?, onClick: () -> Unit) {
    Box(modifier = Modifier.clickable { onClick() }) {
        val dpAsPx = with(LocalDensity.current) { 1.dp.toPx() }
        val context = LocalContext.current
        val patternDrawable = remember {
            PatternDrawable(prayerKey, darkBaseColor = Theme.isNightMode(context), dp = dpAsPx)
        }
        val infiniteTransition = rememberInfiniteTransition(label = "rotation")
        val rotation by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 180_000, easing = LinearEasing)
            ),
            label = ""
        )
        val direction = remember { listOf(1, -1).random() }
        Box(
            modifier = Modifier
                .fillMaxSize()
                // TODO: Previously rotation was handled by pattern drawable itself but now it's
                //  handled in compose graphics layer, as a result different origin for rotation
                //  was done inside the drawable now isn't supported.
                .graphicsLayer(rotationZ = rotation * direction)
                .paint(rememberDrawablePainter(patternDrawable))
        )
        Column(modifier = Modifier.padding(horizontal = 30.dp, vertical = 80.dp)) {
            val textStyle = LocalTextStyle.current.copy(
                color = Color.White, fontWeight = FontWeight.Bold,
                shadow = Shadow(color = Color.Black, blurRadius = 2f, offset = Offset(1f, 1f))
            )
            Text(stringResource(getPrayTimeName(prayerKey)), fontSize = 36.sp, style = textStyle)
            if (cityName != null) {
                var visible by remember { mutableStateOf(false) }
                LaunchedEffect(key1 = null) { visible = true }
                val density = LocalDensity.current
                // Just an exaggerated demo for https://developer.android.com/jetpack/compose/animation#animatedvisibility
                AnimatedVisibility(
                    visible = visible, enter = slideInVertically(
                        animationSpec = keyframes { durationMillis = TWO_SECONDS_IN_MILLIS.toInt() }
                    ) { with(density) { -20.dp.roundToPx() } } + fadeIn(
                        initialAlpha = 0f,
                        animationSpec = keyframes { durationMillis = TWO_SECONDS_IN_MILLIS.toInt() }
                    )
                ) {
                    Text(
                        stringResource(R.string.in_city_time, cityName),
                        fontSize = 18.sp,
                        style = textStyle,
                        modifier = Modifier
                            .padding(top = 10.dp)
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun AthanActivityContentPreview() = AthanActivityContent(FAJR_KEY, "CITY NAME") {}
