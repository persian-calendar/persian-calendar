package com.byagowi.persiancalendar.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Shapes
import androidx.compose.material.Typography
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.byagowi.persiancalendar.entities.Theme

@Composable
fun ComposeTheme(content: @Composable () -> Unit) {
    val colors = when (Theme.getCurrent(LocalContext.current)) {
        Theme.DARK, Theme.BLACK -> remember {
            darkColors(
                primary = Color(0xFF00BF50),
                primaryVariant = Color(0xFF009688),
                secondary = Color(0xFF03DAC5)
            )
        }
        else -> remember {
            lightColors(
                primary = Color(0xFF00695c),
                primaryVariant = Color(0xFF004D40),
                secondary = Color(0xFF00796B)
                /* Other default colors to override
                background = Color.White,
                surface = Color.White,
                onPrimary = Color.White,
                onSecondary = Color.Black,
                onBackground = Color.Black,
                onSurface = Color.Black,
                */
            )
        }
    }

    MaterialTheme(
        colors = colors,
        typography = remember {
            Typography(
                body1 = TextStyle(
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight.Normal,
                    fontSize = 16.sp
                )
                /* Other default text styles to override
                button = TextStyle(
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight.W500,
                    fontSize = 14.sp
                ),
                caption = TextStyle(
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp
                )
                */
            )
        },
        shapes = remember {
            Shapes(
                small = RoundedCornerShape(4.dp),
                medium = RoundedCornerShape(16.dp),
                large = RoundedCornerShape(16.dp)
            )
        },
        content = content
    )
}
