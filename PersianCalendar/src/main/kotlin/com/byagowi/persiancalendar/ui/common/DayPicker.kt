package com.byagowi.persiancalendar.ui.common

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationResult
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.calculateTargetValue
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.entities.CalendarType
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.ui.utils.performHapticFeedbackVirtualKey
import com.byagowi.persiancalendar.utils.calendarType
import com.byagowi.persiancalendar.utils.formatNumber
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

// TODO: Make it editable on click
// TODO: Make it's a11y work
@Composable
fun DayPicker(
    calendarType: CalendarType,
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
            { item: Int -> (monthStart + item).dayOfWeekName + " / " + formatNumber(item) }
        }
        val monthsLength = remember(calendarType, date.year, date.month) {
            calendarType.getMonthLength(date.year, date.month)
        }
        val yearMonth = remember(calendarType, date.year) {
            calendarType.getYearMonths(date.year)
        }
        val monthsFormat = remember(calendarType, date.year) {
            val months = date.calendarType.monthsNames
            { item: Int -> months[item - 1] + " / " + formatNumber(item) }
        }
        val todayYear = remember(calendarType) { Jdn.today().toCalendar(calendarType).year }
        val startYear = remember(calendarType) { todayYear - 200 }
        val yearsFormat = remember(calendarType) {
            { item: Int -> formatNumber(item) }
        }
        var monthChangeToken by remember { mutableIntStateOf(0) }
        var previousMonth by remember { mutableIntStateOf(0) }
        if (previousMonth != date.month) ++monthChangeToken
        previousMonth = date.month
        Row(modifier = Modifier.fillMaxWidth()) {
            val view = LocalView.current
            NumberPicker(
                modifier = Modifier.weight(1f),
                label = daysFormat,
                range = 1..monthsLength,
                textStyle = MaterialTheme.typography.bodyMedium,
                value = date.dayOfMonth,
            ) {
                setJdn(Jdn(calendarType, date.year, date.month, it))
                view.performHapticFeedbackVirtualKey()
            }
            Spacer(modifier = Modifier.width(8.dp))
            NumberPicker(
                modifier = Modifier.weight(1f),
                label = monthsFormat,
                range = 1..yearMonth,
                textStyle = MaterialTheme.typography.bodyMedium,
                value = date.month,
            ) {
                setJdn(Jdn(calendarType, date.year, it, date.dayOfMonth))
                view.performHapticFeedbackVirtualKey()
            }
            Spacer(modifier = Modifier.width(8.dp))
            NumberPicker(
                modifier = Modifier.weight(1f),
                label = yearsFormat,
                range = startYear..startYear + 400,
                textStyle = MaterialTheme.typography.bodyMedium,
                value = date.year,
            ) {
                setJdn(Jdn(calendarType, it, date.month, date.dayOfMonth))
                view.performHapticFeedbackVirtualKey()
            }
        }
    }
}

// The following is brought from https://github.com/ChargeMap/Compose-NumberPicker and customized
// MIT licensed
private fun getItemIndexForOffset(
    range: IntRange,
    value: Int,
    offset: Float,
    halfNumbersColumnHeightPx: Float
): Int {
    val indexOf = range.indexOf(value) - (offset / halfNumbersColumnHeightPx).toInt()
    return maxOf(0, minOf(indexOf, range.count() - 1))
}

@Composable
fun NumberPicker(
    modifier: Modifier = Modifier,
    label: (Int) -> String = { it.toString() },
    range: IntRange,
    textStyle: TextStyle = LocalTextStyle.current,
    value: Int,
    onValueChange: (Int) -> Unit,
) {
    val minimumAlpha = 0.3f
    val verticalMargin = 8.dp
    val numbersColumnHeight = 80.dp
    val halfNumbersColumnHeight = numbersColumnHeight / 2
    val halfNumbersColumnHeightPx = with(LocalDensity.current) { halfNumbersColumnHeight.toPx() }

    val coroutineScope = rememberCoroutineScope()

    val animatedOffset = remember { Animatable(0f) }
        .apply {
            val index = range.indexOf(value)
            val offsetRange = remember(value, range) {
                -((range.count() - 1) - index) * halfNumbersColumnHeightPx to
                        index * halfNumbersColumnHeightPx
            }
            updateBounds(offsetRange.first, offsetRange.second)
        }

    val coercedAnimatedOffset = animatedOffset.value % halfNumbersColumnHeightPx

    val indexOfElement =
        getItemIndexForOffset(range, value, animatedOffset.value, halfNumbersColumnHeightPx)

    var dividersWidth by remember { mutableStateOf(0.dp) }

    val focusManager = LocalFocusManager.current
    Layout(
        modifier = modifier
            .draggable(
                orientation = Orientation.Vertical,
                state = rememberDraggableState { deltaY ->
                    coroutineScope.launch {
                        animatedOffset.snapTo(animatedOffset.value + deltaY)
                    }
                },
                onDragStarted = { focusManager.clearFocus() },
                onDragStopped = { velocity ->
                    coroutineScope.launch {
                        val endValue = animatedOffset.fling(
                            initialVelocity = velocity,
                            animationSpec = exponentialDecay(frictionMultiplier = 20f),
                            adjustTarget = { target ->
                                val coercedTarget = target % halfNumbersColumnHeightPx
                                val coercedAnchors =
                                    listOf(
                                        -halfNumbersColumnHeightPx,
                                        0f,
                                        halfNumbersColumnHeightPx
                                    )
                                val coercedPoint =
                                    coercedAnchors.minByOrNull { abs(it - coercedTarget) }!!
                                val base =
                                    halfNumbersColumnHeightPx * (target / halfNumbersColumnHeightPx).toInt()
                                coercedPoint + base
                            }
                        ).endState.value

                        val result = range.elementAt(
                            getItemIndexForOffset(range, value, endValue, halfNumbersColumnHeightPx)
                        )
                        onValueChange(result)
                        animatedOffset.snapTo(0f)
                    }
                }
            )
            .padding(vertical = numbersColumnHeight / 3 + verticalMargin * 2),
        content = {
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            Box(
                modifier = Modifier
                    .padding(vertical = verticalMargin)
                    .offset { IntOffset(x = 0, y = coercedAnimatedOffset.roundToInt()) }
            ) {
                val baseLabelModifier = Modifier
                    .align(Alignment.Center)
                    .height(20.dp)
                ProvideTextStyle(textStyle) {
                    if (indexOfElement > 0)
                        Label(
                            text = label(range.elementAt(indexOfElement - 1)),
                            modifier = baseLabelModifier
                                .semantics {
                                    @OptIn(ExperimentalComposeUiApi::class) this.invisibleToUser()
                                }
                                .offset(y = -halfNumbersColumnHeight)
                                .alpha(
                                    maxOf(
                                        minimumAlpha,
                                        coercedAnimatedOffset / halfNumbersColumnHeightPx
                                    )
                                )
                        )
                    var showTextEdit by remember { mutableStateOf(false) }
                    if (showTextEdit) {
                        val focusRequester = remember { FocusRequester() }
                        var inputValue by remember {
                            val valueText = formatNumber(value)
                            mutableStateOf(
                                TextFieldValue(
                                    valueText,
                                    selection = TextRange(0, valueText.length)
                                )
                            )
                        }
                        LaunchedEffect(Unit) { focusRequester.requestFocus() }
                        var isCapturedOnce by remember { mutableStateOf(false) }

                        val interactionSource = remember { MutableInteractionSource() }
                        val isFocused by interactionSource.collectIsFocusedAsState()
                        if (isFocused && !isCapturedOnce) isCapturedOnce = true
                        if (!isFocused && isCapturedOnce) showTextEdit = false
                        BasicTextField(
                            value = inputValue,
                            interactionSource = interactionSource,
                            maxLines = 1,
                            onValueChange = { inputValue = it },
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                    showTextEdit = false
                                    inputValue.text.toIntOrNull()?.let {
                                        if (it in range) onValueChange(it)
                                    }
                                }
                            ),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done,
                            ),
                            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                            modifier = Modifier
                                .align(Alignment.Center)
                                .focusRequester(focusRequester),
                        )
                    } else Label(
                        text = label(range.elementAt(indexOfElement)),
                        modifier = baseLabelModifier
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() },
                            ) { showTextEdit = true }
                            .alpha(
                                (maxOf(
                                    minimumAlpha,
                                    1 - abs(coercedAnimatedOffset) / halfNumbersColumnHeightPx
                                ))
                            )
                    )
                    if (indexOfElement < range.count() - 1)
                        Label(
                            text = label(range.elementAt(indexOfElement + 1)),
                            modifier = baseLabelModifier
                                .semantics {
                                    @OptIn(ExperimentalComposeUiApi::class) this.invisibleToUser()
                                }
                                .offset(y = halfNumbersColumnHeight)
                                .alpha(
                                    maxOf(
                                        minimumAlpha,
                                        -coercedAnimatedOffset / halfNumbersColumnHeightPx
                                    )
                                )
                        )
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
        }
    ) { measurables, constraints ->
        // Don't constrain child views further, measure them with given constraints
        // List of measured children
        val placeables = measurables.map { measurable ->
            // Measure each children
            measurable.measure(constraints)
        }

        dividersWidth = placeables
            .drop(1)
            .first()
            .width
            .toDp()

        // Set the size of the layout as big as it can
        layout(dividersWidth.toPx().toInt(), placeables.sumOf { it.height }) {
            // Track the y co-ord we have placed children up to
            var yPosition = 0

            // Place children in the parent layout
            placeables.forEach { placeable ->

                // Position item on the screen
                placeable.placeRelative(x = 0, y = yPosition)

                // Record the y co-ord placed up to
                yPosition += placeable.height
            }
        }
    }
}

@Composable
private fun Label(text: String, modifier: Modifier) {
    Text(modifier = modifier, maxLines = 1, text = text, textAlign = TextAlign.Center)
}

private suspend fun Animatable<Float, AnimationVector1D>.fling(
    initialVelocity: Float,
    animationSpec: DecayAnimationSpec<Float>,
    adjustTarget: ((Float) -> Float)?,
    block: (Animatable<Float, AnimationVector1D>.() -> Unit)? = null,
): AnimationResult<Float, AnimationVector1D> {
    val targetValue = animationSpec.calculateTargetValue(value, initialVelocity)
    val adjustedTarget = adjustTarget?.invoke(targetValue)
    return if (adjustedTarget != null) {
        animateTo(
            targetValue = adjustedTarget,
            initialVelocity = initialVelocity,
            block = block
        )
    } else {
        animateDecay(
            initialVelocity = initialVelocity,
            animationSpec = animationSpec,
            block = block,
        )
    }
}
