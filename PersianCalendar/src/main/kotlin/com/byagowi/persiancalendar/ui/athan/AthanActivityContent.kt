package com.byagowi.persiancalendar.ui.athan

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.PrayTime
import com.byagowi.persiancalendar.global.cityName
import kotlin.time.Duration.Companion.seconds

@Composable
fun AthanActivityContent(prayTime: PrayTime, onClick: () -> Unit) {
    val dpAsPx = with(LocalDensity.current) { 1.dp.toPx() }
    val darkBaseColor = isSystemInDarkTheme()
    val patternDrawable = remember(darkBaseColor) {
        // We like to reuse our drawable for now but can reconsider in future
        PatternDrawable(prayTime, darkBaseColor = darkBaseColor, dp = dpAsPx)
    }
    Box(
        modifier = Modifier
            .clickable { onClick() }
            .fillMaxSize()
            .onSizeChanged { patternDrawable.setSize(it.width, it.height) },
    ) {
        DrawBackground(patternDrawable, durationMillis = 180_000)
        Column(modifier = Modifier.padding(horizontal = 30.dp, vertical = 80.dp)) {
            val textStyle = LocalTextStyle.current.copy(
                color = Color.White, fontWeight = FontWeight.Bold,
                shadow = Shadow(color = Color.Black, blurRadius = 2f, offset = Offset(1f, 1f)),
            )
            Text(stringResource(prayTime.stringRes), fontSize = 36.sp, style = textStyle)
            val cityName = cityName
            if (cityName != null) {
                var visible by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) { visible = true }
                val density = LocalDensity.current
                // Just an exaggerated demo for https://developer.android.com/jetpack/compose/animation#animatedvisibility
                AnimatedVisibility(
                    visible = visible,
                    enter = slideInVertically(
                        animationSpec = keyframes {
                            durationMillis = 2.seconds.inWholeMilliseconds.toInt()
                        },
                    ) { with(density) { -20.dp.roundToPx() } } + fadeIn(
                        initialAlpha = 0f,
                        animationSpec = keyframes {
                            durationMillis = 2.seconds.inWholeMilliseconds.toInt()
                        },
                    ),
                ) {
                    Text(
                        stringResource(R.string.in_city_time, cityName),
                        fontSize = 18.sp,
                        style = textStyle,
                        modifier = Modifier
                            .padding(top = 10.dp)
                            .fillMaxWidth()
                            .height(200.dp),
                    )
                }
            }
        }
    }
}
