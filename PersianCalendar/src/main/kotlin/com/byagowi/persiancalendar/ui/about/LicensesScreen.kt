package com.byagowi.persiancalendar.ui.about

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Motorcycle
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ui.utils.MaterialCornerExtraLargeTop
import com.byagowi.persiancalendar.ui.utils.MaterialIconDimension
import com.byagowi.persiancalendar.ui.utils.resolveColor

@Preview
@Composable
private fun LicensesScreenPreview() = LicensesScreen {}

@Composable
fun LicensesScreen(popNavigation: () -> Unit) {
    Column {
        val context = LocalContext.current
        // TODO: Ideally this should be onPrimary
        val colorOnAppBar = Color(context.resolveColor(R.attr.colorOnAppBar))
        @OptIn(ExperimentalMaterial3Api::class)
        TopAppBar(
            title = { Text(stringResource(R.string.licenses)) },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                navigationIconContentColor = colorOnAppBar,
                actionIconContentColor = colorOnAppBar,
                titleContentColor = colorOnAppBar,
            ),
            navigationIcon = {
                IconButton(onClick = popNavigation) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = stringResource(R.string.navigate_up)
                    )
                }
            },
        )
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Surface(shape = MaterialCornerExtraLargeTop()) {
                Licenses()
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Sidebar(modifier = Modifier.padding(end = 8.dp, top = 12.dp))
                }
            }
        }
    }
}

// TODO: Sidesheet content not used anymore as Material library of Jetpack Compose doesn't have sidesheet
//    """Events count:
//   Persian Events: ${persianEvents.size + 1}
//   Islamic Events: ${islamicEvents.size + 1}
//   Gregorian Events: ${gregorianEvents.size + 1}
//   Nepali Events: ${nepaliEvents.size + 1}
//   Irregular Recurring Events: ${irregularRecurringEvents.size + 1}
//   \nSources:
//   ${EventType.entries.joinToString("\n") { "${it.name}: ${it.source}" }}"""
//  And as the result has link, it should be linkified https://stackoverflow.com/q/66130513

@Composable
private fun Sidebar(modifier: Modifier = Modifier) {
    var selectedItem by rememberSaveable { mutableIntStateOf(0) }
    NavigationRail(
        modifier,
        windowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = Color.Transparent,
    ) {
        listOf<Triple<String, @Composable () -> Unit, (FragmentActivity) -> Unit>>(
            Triple(
                "GPLv3",
                { Icon(imageVector = Icons.Default.Info, contentDescription = "License") },
                ::showShaderSandboxDialog
            ),
            Triple(
                KotlinVersion.CURRENT.toString(), {
                    Text("Kotlin", style = MaterialTheme.typography.bodySmall)
                }, ::showSpringDemoDialog
            ), Triple(
                "API ${Build.VERSION.SDK_INT}",
                {
                    Icon(
                        modifier = Modifier.size(MaterialIconDimension.dp),
                        imageVector = Icons.Default.Motorcycle,
                        contentDescription = "API"
                    )
                },
                ::showFlingDemoDialog
            )
        ).forEachIndexed { i, (title, icon, action) ->
            val clickHandler = remember { createEasterEggClickHandler(action) }
            val context = LocalContext.current
            NavigationRailItem(
                selected = selectedItem == i,
                onClick = {
                    selectedItem = i
                    clickHandler(context as? FragmentActivity) // TODO: Ugly cast
                },
                icon = icon,
                label = { Text(title) },
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Licenses() {
    val sections = remember { getCreditsSections() }
    var expandedItem by rememberSaveable { mutableIntStateOf(-1) }
    LazyColumn {
        itemsIndexed(sections) { i, (title, license, text) ->
            if (i > 0) Divider(
                modifier = Modifier.padding(start = 16.dp, end = 88.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = .5f)
            )
            val angle by animateFloatAsState(if (expandedItem == i) 0f else -90f, label = "angle")
            Column(modifier = Modifier
                .clickable { expandedItem = if (i == expandedItem) -1 else i }
                .padding(
                    start = 16.dp,
                    end = 88.dp,
                    top = if (i == 0) 16.dp else 4.dp,
                    bottom = 4.dp,
                )
                .fillMaxWidth()
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )) {
                FlowRow(verticalArrangement = Arrangement.Center) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = stringResource(R.string.more),
                        modifier = Modifier.rotate(angle),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(title, modifier = Modifier.align(alignment = Alignment.CenterVertically))
                    Spacer(modifier = Modifier.width(4.dp))
                    if (license != null) Text(
                        // TODO: Linkify them just like https://stackoverflow.com/q/66130513
                        //  Linkify.addLinks(it, Linkify.WEB_URLS or Linkify.EMAIL_ADDRESSES)
                        license,
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.secondaryContainer,
                                RoundedCornerShape(CornerSize(4.dp)),
                            )
                            .align(alignment = Alignment.CenterVertically)
                            .padding(horizontal = 4.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }
                AnimatedVisibility(visible = expandedItem == i) {
                    SelectionContainer { Text(text) }
                }
            }
        }
        item { Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.systemBars)) }
    }
}
