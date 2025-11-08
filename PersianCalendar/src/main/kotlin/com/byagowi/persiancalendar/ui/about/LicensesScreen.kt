package com.byagowi.persiancalendar.ui.about

import android.app.Activity
import android.os.Build
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Motorcycle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ui.common.ExpandArrow
import com.byagowi.persiancalendar.ui.common.NavigationNavigateUpIcon
import com.byagowi.persiancalendar.ui.common.ScreenSurface
import com.byagowi.persiancalendar.ui.common.ScrollShadow
import com.byagowi.persiancalendar.ui.theme.appTopAppBarColors
import com.byagowi.persiancalendar.ui.utils.appContentSizeAnimationSpec

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.LicensesScreen(
    animatedContentScope: AnimatedContentScope,
    navigateUp: () -> Unit,
) {
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text(stringResource(R.string.about_license_title)) },
                colors = appTopAppBarColors(),
                navigationIcon = { NavigationNavigateUpIcon(navigateUp) },
            )
        }
    ) { paddingValues ->
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Box(Modifier.padding(top = paddingValues.calculateTopPadding())) {
                ScreenSurface(animatedContentScope) {
                    Box(
                        modifier = Modifier.windowInsetsPadding(
                            WindowInsets.displayCutout.only(WindowInsetsSides.Horizontal)
                        ),
                    ) { Licenses(paddingValues.calculateBottomPadding()) }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .windowInsetsPadding(
                                WindowInsets.displayCutout.only(WindowInsetsSides.Horizontal)
                            )
                            .semantics(mergeDescendants = true) { this.hideFromAccessibility() }
                            .clearAndSetSemantics {},
                        contentAlignment = Alignment.CenterEnd,
                    ) { Sidebar(modifier = Modifier.padding(end = 8.dp, top = 12.dp)) }
                }
            }
        }
    }
}

// Sidesheet content not used anymore as Material library of Jetpack Compose doesn't have sidesheet
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
        windowInsets = WindowInsets(),
        containerColor = Color.Transparent,
    ) {
        listOf<Triple<String, @Composable () -> Unit, (Activity) -> Unit>>(
            Triple(
                "GPLv3",
                { Icon(imageVector = Icons.Default.Info, contentDescription = "License") },
                ::showShaderSandboxDialog
            ),
            Triple(
                KotlinVersion.CURRENT.toString(), {
                    Text("Kotlin", style = MaterialTheme.typography.bodySmall)
                }, ::showSpringDemoDialog
            ),
            Triple(
                "API ${Build.VERSION.SDK_INT}", {
                    Icon(
                        imageVector = Icons.Default.Motorcycle,
                        contentDescription = "API",
                    )
                }, ::showFlingDemoDialog
            ),
        ).forEachIndexed { i, (title, icon, action) ->
            val clickHandler = remember { createEasterEggClickHandler(action) }
            val activity = LocalActivity.current
            NavigationRailItem(
                selected = selectedItem == i,
                onClick = {
                    selectedItem = i
                    clickHandler(activity)
                },
                icon = icon,
                label = { Text(title) },
            )
        }
    }
}

@Composable
private fun BoxScope.Licenses(bottomPadding: Dp) {
    val sections = remember { getCreditsSections() }
    var expandedItem by rememberSaveable { mutableIntStateOf(-1) }
    val listState = rememberLazyListState()
    val expandArrowSizeModifier = Modifier.size(with(LocalDensity.current) { 24.sp.toDp() })
    LazyColumn(state = listState) {
        itemsIndexed(sections) { i, (title, license, text) ->
            if (i > 0) HorizontalDivider(
                modifier = Modifier.padding(start = 16.dp, end = 88.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = .5f),
            )
            val verticalPadding by animateDpAsState(
                if (expandedItem == i) 6.dp else 4.dp,
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
            )
            Column(
                modifier = Modifier
                    .toggleable(i == expandedItem) { expandedItem = if (it) i else -1 }
                    .padding(
                        start = 16.dp,
                        end = 88.dp,
                        top = verticalPadding + if (i == 0) 12.dp else 0.dp,
                        bottom = verticalPadding,
                    )
                    .fillMaxWidth()
                    .animateContentSize(appContentSizeAnimationSpec),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    ExpandArrow(
                        isExpanded = expandedItem == i,
                        tint = MaterialTheme.colorScheme.primary,
                        contentDescription = null,
                        isLineStart = true,
                        modifier = expandArrowSizeModifier,
                    )
                    Text(
                        title,
                        modifier = Modifier.align(alignment = Alignment.CenterVertically),
                        maxLines = 1,
                        autoSize = TextAutoSize.StepBased(
                            minFontSize = 11.sp,
                            maxFontSize = LocalTextStyle.current.fontSize,
                        ),
                    )
                    if (license != null) Text(
                        // Maybe linkify them just like https://stackoverflow.com/q/66130513
                        //  Linkify.addLinks(it, Linkify.WEB_URLS or Linkify.EMAIL_ADDRESSES)
                        license,
                        maxLines = 1,
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.secondaryContainer,
                                MaterialTheme.shapes.extraSmall,
                            )
                            .align(alignment = Alignment.CenterVertically)
                            .padding(horizontal = 4.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }
                this.AnimatedVisibility(visible = expandedItem == i) {
                    SelectionContainer { Text(text) }
                }
            }
        }
        item { Spacer(Modifier.height(bottomPadding)) }
    }
    ScrollShadow(listState)
}
