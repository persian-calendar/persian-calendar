package com.byagowi.persiancalendar.ui.about

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.AppBarBinding
import com.byagowi.persiancalendar.ui.utils.MaterialCornerExtraLargeTop
import com.byagowi.persiancalendar.ui.utils.MaterialIconDimension
import com.byagowi.persiancalendar.ui.utils.layoutInflater
import com.byagowi.persiancalendar.ui.utils.setupUpNavigation
import com.google.accompanist.themeadapter.material3.Mdc3Theme

class LicensesScreen : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, viewGroup: ViewGroup?, bundle: Bundle?
    ): View {
        val root = ComposeView(inflater.context)
        root.setContent {
            Mdc3Theme {
                Column {
                    Spacer(Modifier.windowInsetsTopHeight(WindowInsets.safeDrawing))
                    AndroidView(modifier = Modifier.fillMaxWidth(), factory = {
                        val bar = AppBarBinding.inflate(it.layoutInflater)
                        bar.toolbar.title = it.getString(R.string.about_license_title)
                        bar.toolbar.setupUpNavigation()
                        bar.root
                    })
                    Surface(shape = MaterialCornerExtraLargeTop()) {
                        Box(modifier = Modifier.padding(16.dp, 16.dp, 16.dp)) {
                            // TODO: Remove this rtl use, horizontalArrangement = Arrangement.End didn't do the trick,
                            //  the latter ltr is ok
                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                                Row {
                                    Sidebar()
                                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                        Licenses()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return root
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
private fun Sidebar() {
    var selectedItem by remember { mutableIntStateOf(0) }
    NavigationRail(windowInsets = WindowInsets(0, 0, 0, 0)) {
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
                        imageVector = ImageVector.vectorResource(R.drawable.ic_motorcycle),
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
            if (i > 0) Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = .5f))
            val angle =
                animateFloatAsState(if (expandedItem == i) 0f else -90f, label = "angle").value
            Column(modifier = Modifier
                .clickable { expandedItem = if (i == expandedItem) -1 else i }
                .padding(4.dp)
                .fillMaxWidth()
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )) {
                FlowRow(verticalArrangement = Arrangement.Center) {
                    Image(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = stringResource(R.string.more),
                        modifier = Modifier.rotate(angle),
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
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
                if (expandedItem == i) SelectionContainer { Text(text) }
            }
        }
        item { Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.systemBars)) }
    }
}
