package com.byagowi.persiancalendar.ui.common

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.entities.CalendarType
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.ui.utils.performHapticFeedbackVirtualKey
import com.byagowi.persiancalendar.utils.calendarType
import com.byagowi.persiancalendar.utils.formatNumber
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class DayPickerView(context: Context, attrs: AttributeSet? = null) : FrameLayout(context, attrs) {
    private val calendarFlow = MutableStateFlow(mainCalendar)
    var calendarType: CalendarType
        get() = calendarFlow.value
        set(value) {
            calendarFlow.value = value
        }
    private val valueFlow = MutableStateFlow(Jdn.today())
    var value: Jdn
        get() = valueFlow.value
        set(value) {
            valueFlow.value = value
        }
    var onValueChangeListener = fun(_: Jdn) {}

    init {
        val root = ComposeView(context)
        root.setContent {
            var calendar by remember { mutableStateOf(calendarType) }
            var jdn by remember { mutableStateOf(value) }
            val scope = rememberCoroutineScope()
            var changeToken by remember { mutableStateOf(0) }
            remember {
                scope.launch {
                    calendarFlow.collect {
                        if (calendar != it) {
                            calendar = it
                            ++changeToken
                        }
                    }
                }
            }
            remember {
                scope.launch {
                    valueFlow.collect {
                        if (jdn != it) {
                            jdn = it
                            ++changeToken
                        }
                    }
                }
            }
            onValueChangeListener(jdn)
            valueFlow.value = jdn
            calendarFlow.value = calendar
            DayPicker(calendar, changeToken, jdn) {
                performHapticFeedbackVirtualKey()
                jdn = it
            }
        }
        addView(root)
    }
}

// TODO: Make it editable on click
// TODO: Make it's a11y work
@Composable
fun DayPicker(
    calendarType: CalendarType,
    changeToken: Int,
    jdn: Jdn,
    setJdn: (Jdn) -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        val date = remember(jdn.value, calendarType) { jdn.toCalendar(calendarType) }
        val daysFormat = remember(calendarType, date.year, date.month) {
            val monthStart = Jdn(calendarType, date.year, date.month, 1);
            { index: Int -> (monthStart + index).dayOfWeekName + " / " + formatNumber(index + 1) }
        }
        val monthsLength = remember(calendarType, date.year, date.month) {
            calendarType.getMonthLength(date.year, date.month)
        }
        val yearMonth = remember(calendarType, date.year) {
            calendarType.getYearMonths(date.year)
        }
        val monthsFormat = remember(calendarType, date.year) {
            val months = date.calendarType.monthsNames
            { index: Int -> months[index] + " / " + formatNumber(index + 1) }
        }
        val todayYear = remember(calendarType) { Jdn.today().toCalendar(calendarType).year }
        val startYear = remember(calendarType) { todayYear - 200 }
        val yearsFormat = remember(calendarType) {
            { index: Int -> formatNumber(index + 1 + startYear) }
        }
        var monthChangeToken by remember { mutableStateOf(0) }
        var previousMonth by remember { mutableStateOf(0) }
        if (previousMonth != date.month) ++monthChangeToken
        previousMonth = date.month
        Row(modifier = Modifier.fillMaxWidth()) {
            Picker(
                modifier = Modifier.weight(1f),
                formatter = daysFormat,
                size = monthsLength,
                textStyle = MaterialTheme.typography.bodyLarge,
                itemHeight = 48.dp,
                index = date.dayOfMonth - 1,
                key1 = changeToken,
                key2 = monthChangeToken,
            ) { setJdn(Jdn(calendarType, date.year, date.month, it + 1)) }
            Spacer(modifier = Modifier.width(8.dp))
            Picker(
                modifier = Modifier.weight(1f),
                formatter = monthsFormat,
                size = yearMonth,
                textStyle = MaterialTheme.typography.bodyLarge,
                itemHeight = 48.dp,
                index = date.month - 1,
                key1 = changeToken,
            ) { setJdn(Jdn(calendarType, date.year, it + 1, date.dayOfMonth)) }
            Spacer(modifier = Modifier.width(8.dp))
            Picker(
                modifier = Modifier.weight(1f),
                formatter = yearsFormat,
                size = 400, // only 400 years
                textStyle = MaterialTheme.typography.bodyLarge,
                itemHeight = 48.dp,
                index = date.year - startYear - 1,
                key1 = changeToken,
            ) { setJdn(Jdn(calendarType, it + startYear, date.month, date.dayOfMonth)) }
        }
    }
}


// Based on https://stackoverflow.com/a/76271633 but modified
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Picker(
    modifier: Modifier = Modifier,
    formatter: (Int) -> String,
    size: Int,
    extra: Int = 1,
    textModifier: Modifier = Modifier,
    textStyle: TextStyle = LocalTextStyle.current,
    dividerColor: Color = LocalContentColor.current,
    itemHeight: Dp,
    index: Int = 0,
    key1: Int, // ugly, for now
    key2: Int = 0, // ugly, for now
    setIndex: (Int) -> Unit,
) {
    val listScrollCount = Integer.MAX_VALUE
    val listScrollMiddle = listScrollCount / 2
    val listStartIndex = listScrollMiddle - listScrollMiddle % size - extra + index

    val listState = remember(key1, key2) {
        LazyListState(listStartIndex, 0)
    }
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    val fadingEdgeGradient = remember {
        Brush.verticalGradient(
            0f to Color.Transparent, 0.5f to Color.Black, 1f to Color.Transparent
        )
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }.map { (it + extra) % size }
            .distinctUntilChanged().collect {
                if (index != it) setIndex(it)
            }
    }

    LazyColumn(state = listState,
        flingBehavior = flingBehavior,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .height(itemHeight * (extra * 2 + 1))
            .drawWithContent {
                drawContent()
                val visibleItemsCount = extra * 2 + 1
                drawLine(
                    dividerColor,
                    Offset(0f, this.size.height / visibleItemsCount * extra),
                    Offset(this.size.width, this.size.height / visibleItemsCount * extra),
                    DividerDefaults.Thickness.toPx(),
                )
                drawLine(
                    dividerColor,
                    Offset(0f, this.size.height / visibleItemsCount * (extra + 1)),
                    Offset(this.size.width, this.size.height / visibleItemsCount * (extra + 1)),
                    DividerDefaults.Thickness.toPx(),
                )
            }
            .fadingEdge(fadingEdgeGradient)) {
        items(listScrollCount) { index ->
            Box(modifier = Modifier.height(itemHeight), contentAlignment = Alignment.Center) {
                Text(
                    text = formatter(index % size),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = textStyle,
                    modifier = textModifier,
                )
            }
        }
    }
}

private fun Modifier.fadingEdge(brush: Brush) = this
    .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
    .drawWithContent {
        drawContent()
        drawRect(brush = brush, blendMode = BlendMode.DstIn)
    }
