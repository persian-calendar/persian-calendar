package com.byagowi.persiancalendar.ui.about

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp

@Composable
fun ColorSchemeDemoDialog(onDismissRequest: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {},
        dismissButton = {},
        title = {},
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                listOf(
                    "primary" to MaterialTheme.colorScheme.primary,
                    "onPrimary" to MaterialTheme.colorScheme.onPrimary,
                    "primaryContainer" to MaterialTheme.colorScheme.primaryContainer,
                    "onPrimaryContainer" to MaterialTheme.colorScheme.onPrimaryContainer,
                    "inversePrimary" to MaterialTheme.colorScheme.inversePrimary,
                    "secondary" to MaterialTheme.colorScheme.secondary,
                    "onSecondary" to MaterialTheme.colorScheme.onSecondary,
                    "secondaryContainer" to MaterialTheme.colorScheme.secondaryContainer,
                    "onSecondaryContainer" to MaterialTheme.colorScheme.onSecondaryContainer,
                    "tertiary" to MaterialTheme.colorScheme.tertiary,
                    "onTertiary" to MaterialTheme.colorScheme.onTertiary,
                    "tertiaryContainer" to MaterialTheme.colorScheme.tertiaryContainer,
                    "onTertiaryContainer" to MaterialTheme.colorScheme.onTertiaryContainer,
                    "background" to MaterialTheme.colorScheme.background,
                    "onBackground" to MaterialTheme.colorScheme.onBackground,
                    "surface" to MaterialTheme.colorScheme.surface,
                    "onSurface" to MaterialTheme.colorScheme.onSurface,
                    "surfaceVariant" to MaterialTheme.colorScheme.surfaceVariant,
                    "onSurfaceVariant" to MaterialTheme.colorScheme.onSurfaceVariant,
                    "surfaceTint" to MaterialTheme.colorScheme.surfaceTint,
                    "inverseSurface" to MaterialTheme.colorScheme.inverseSurface,
                    "inverseOnSurface" to MaterialTheme.colorScheme.inverseOnSurface,
                    "error" to MaterialTheme.colorScheme.error,
                    "onError" to MaterialTheme.colorScheme.onError,
                    "errorContainer" to MaterialTheme.colorScheme.errorContainer,
                    "onErrorContainer" to MaterialTheme.colorScheme.onErrorContainer,
                    "outline" to MaterialTheme.colorScheme.outline,
                    "outlineVariant" to MaterialTheme.colorScheme.outlineVariant,
                    "scrim" to MaterialTheme.colorScheme.scrim,
                ).map { (title, color) ->
                    Text(
                        title,
                        color = MaterialTheme.colorScheme.contentColorFor(color),
                        modifier = Modifier.background(color, MaterialTheme.shapes.extraSmall),
                    )
                }
            }
        },
    )
}

@Composable
fun TypographyDemoDialog(onDismissRequest: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {},
        dismissButton = {},
        title = {},
        text = {
            Column {
                listOf(
                    "DisplayLarge" to MaterialTheme.typography.displayLarge,
                    "DisplayMedium" to MaterialTheme.typography.displayMedium,
                    "DisplaySmall" to MaterialTheme.typography.displaySmall,
                    "HeadlineLarge" to MaterialTheme.typography.headlineLarge,
                    "HeadlineMedium" to MaterialTheme.typography.headlineMedium,
                    "HeadlineSmall" to MaterialTheme.typography.headlineSmall,
                    "TitleLarge" to MaterialTheme.typography.titleLarge,
                    "TitleMedium" to MaterialTheme.typography.titleMedium,
                    "TitleSmall" to MaterialTheme.typography.titleSmall,
                    "BodyLarge" to MaterialTheme.typography.bodyLarge,
                    "BodyMedium" to MaterialTheme.typography.bodyMedium,
                    "BodySmall" to MaterialTheme.typography.bodySmall,
                    "LabelLarge" to MaterialTheme.typography.labelLarge,
                    "LabelMedium" to MaterialTheme.typography.labelMedium,
                    "LabelSmall" to MaterialTheme.typography.labelSmall,
                ).map { (title, style) ->
                    Text(buildAnnotatedString {
                        withStyle(style.toSpanStyle()) {
                            append(title)
                        }
                        append(" w=${style.fontWeight?.weight} ")
                        when {
                            style.fontSize.isSp -> append("${style.fontSize.value}sp")
                            style.fontSize.isEm -> append("${style.fontSize.value}em")
                            else -> append("${style.fontSize.value} UNKNOWN!")
                        }
                    })
                }
            }
        },
    )
}

@Composable
fun ShapesDemoDialog(onDismissRequest: () -> Unit) {
    fun f(cornerSize: CornerSize): String {
        return cornerSize.toString()
            .replace("CornerSize", "")
            .replace("size = ", "")
    }
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {},
        dismissButton = {},
        title = {},
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                listOf(
                    "extraLarge" to MaterialTheme.shapes.extraLarge,
                    "large" to MaterialTheme.shapes.large,
                    "medium" to MaterialTheme.shapes.medium,
                    "small" to MaterialTheme.shapes.small,
                    "extraSmall" to MaterialTheme.shapes.extraSmall,
                ).map { (title, shape) ->
                    Surface(
                        color = MaterialTheme.colorScheme.onSurface,
                        shape = shape,
                        modifier = Modifier
                            .size(160.dp)
                            .padding(4.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Text(
                                "$title topStart=${f(shape.topStart)} topEnd=${f(shape.topEnd)} bottomStart=${
                                    f(
                                        shape.bottomStart
                                    )
                                } topEnd=${f(shape.bottomEnd)}",
                                modifier = Modifier.align(Alignment.Center),
                                color = MaterialTheme.colorScheme.surface
                            )
                        }
                    }
                }
            }
        },
    )
}
