package com.byagowi.persiancalendar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.lerp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.NavigationDrawer
import androidx.tv.material3.NavigationDrawerItem
import androidx.tv.material3.StandardCardContainer
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import androidx.tv.material3.darkColorScheme
import androidx.tv.material3.lightColorScheme
import java.util.Locale

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalTvMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme(colorScheme = colorScheme()) { App() } }
    }
}

@Composable
fun colorScheme() = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()

@Composable
fun App() {
    var selectedIndex by remember { mutableIntStateOf(0) }

    val items = listOf(
        "تقویم" to Icons.Default.Home,
        "تنظیمات" to Icons.Default.Settings,
    )

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        NavigationDrawer(
            drawerContent = {
                Column(
                    Modifier
                        .background(Color.Gray)
                        .fillMaxHeight()
                        .padding(12.dp)
                        .selectableGroup(),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items.forEachIndexed { index, item ->
                        val (text, icon) = item
                        NavigationDrawerItem(
                            selected = selectedIndex == index,
                            onClick = { selectedIndex = index },
                            leadingContent = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                )
                            },
                        ) { Text(text) }
                    }
                }
            }
        ) {
            val isFirstRun = remember { mutableStateOf(false) }
            val listState = rememberLazyListState(weeksToShowCount / 2)
            LazyColumn(state = listState) {
                items(weeksToShowCount) { week ->
                    Week(week, isFirstRun)
                }
            }
        }
    }
}

private const val weeksToShowCount = 1000

@Composable
fun Week(
    week: Int,
    isFirstRun: MutableState<Boolean>,
) {
    var focusedItem by remember { mutableIntStateOf(-1) }
    val isCurrentWeek = week == weeksToShowCount / 2
    val currentWeekDay = 2
    Column(
        Modifier
            .alpha(animateFloatAsState(if (focusedItem == -1) .5f else 1f).value)
            .padding(top = 24.dp, bottom = 24.dp),

        ) {
        Text(
            "هفتهٔ %d سال ۱۴۰۴".format(Locale("fa"), week),
            style = lerp(
                MaterialTheme.typography.displayMedium,
                MaterialTheme.typography.headlineSmall,
                animateFloatAsState(if (focusedItem != -1) 0f else 1f).value
            ),
            modifier = Modifier.padding(start = 108.dp),
            color = MaterialTheme.colorScheme.onBackground,
        )
        LazyRow(
            contentPadding = PaddingValues(
                start = 108.dp,
                end = 108.dp,
            ),
        ) {
            itemsIndexed(
                listOf(
                    "شنبه",
                    "یکشنبه",
                    "دوشنبه",
                    "سه‌شنبه",
                    "چهارشنبه",
                    "پنجشنبه",
                    "جمعه",
                )
            ) { index, weekDay ->
                val focusRequester = remember { FocusRequester() }
                if (index == currentWeekDay && isCurrentWeek) LaunchedEffect(Unit) {
                    var isFirstRun by isFirstRun
                    if (isFirstRun) {
                        focusRequester.requestFocus()
//                        isFirstRun = false
                    }
                }
                StandardCardContainer(
                    modifier = Modifier
                        .focusRequester(focusRequester)
                        .onFocusChanged {
                            if (it.isFocused) focusedItem = index
                            else if (focusedItem == index) focusedItem = -1
                        }
                        .focusProperties {
//                                right = if (index == 0) {
//                                    FocusRequester.Cancel
//                                } else {
//                                    FocusRequester.Default
//                                }
                        }
                        .padding(end = 8.dp)
                        .width(150.dp),
                    title = {},
                    subtitle = {
                        Text(
                            weekDay,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    },
                    imageCard = {
                        Surface(
                            onClick = {},
                            shape = ClickableSurfaceDefaults.shape(),
                            border = ClickableSurfaceDefaults.border(
                                border = if (isCurrentWeek && index == currentWeekDay) Border(
                                    border = BorderStroke(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    ),
                                    shape = MaterialTheme.shapes.large,
                                ) else Border.None,
                                focusedBorder = Border(
                                    border = BorderStroke(
                                        width = 2.dp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    ),
                                    shape = MaterialTheme.shapes.large,
                                )
                            ),
                            scale = ClickableSurfaceDefaults.scale(focusedScale = 1f),
                            content = {
                                Box(
                                    Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.background)
                                        .padding(horizontal = 16.dp)
                                        .height(80.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    val contentColor = MaterialTheme.colorScheme.onBackground
                                    BasicText(
                                        text = "%d فروردین".format(Locale("fa"), index + 1),
                                        color = { contentColor },
                                        autoSize = TextAutoSize.StepBased(
                                            12.sp,
                                            MaterialTheme.typography.displayMedium.fontSize,
                                        ),
                                    )
                                }
                            }
                        )
                    },
                )
            }
        }
    }
}

