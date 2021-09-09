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
import com.byagowi.persiancalendar.ui.utils.resolveColor

@Composable
fun ComposeTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val colors = remember {
        val primary =
            Color(context.resolveColor(com.google.android.material.R.attr.colorPrimary))
        val primaryVariant =
            Color(context.resolveColor(com.google.android.material.R.attr.colorPrimaryVariant))
        val secondary =
            Color(context.resolveColor(com.google.android.material.R.attr.colorSecondary))
        val surface =
            Color(context.resolveColor(com.google.android.material.R.attr.colorPrimaryDark))
        when (Theme.getCurrent(context)) {
            Theme.DARK, Theme.BLACK -> darkColors(
                primary = primary, primaryVariant = primaryVariant, secondary = secondary,
                surface = surface
            )
            else -> lightColors(
                primary = primary, primaryVariant = primaryVariant, secondary = secondary,
                surface = surface
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
