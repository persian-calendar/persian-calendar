package com.byagowi.persiancalendar.ui.common

import androidx.compose.animation.Crossfade
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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.utils.formatNumber
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

// The following is brought from
// https://github.com/ChargeMap/Compose-NumberPicker and customized to support number edits
// MIT licensed
@Composable
fun NumberPicker(
    modifier: Modifier = Modifier,
    label: (Int) -> String = { formatNumber(it) },
    range: IntRange,
    value: Int,
    onClickLabel: String? = null,
    disableEdit: Boolean = false,
    onValueChange: (Int) -> Unit,
) {
    val minimumAlpha = 0.3f
    val verticalMargin = 8.dp
    val numbersColumnHeight = 96.dp
    val halfNumbersColumnHeight = numbersColumnHeight / 2
    val halfNumbersColumnHeightPx = with(LocalDensity.current) { halfNumbersColumnHeight.toPx() }

    val coroutineScope = rememberCoroutineScope()

    val animatedOffset = remember { Animatable(0f) }
    animatedOffset.updateBounds(
        lowerBound = (value - range.last) * halfNumbersColumnHeightPx,
        upperBound = (value - range.first) * halfNumbersColumnHeightPx,
    )

    val coercedAnimatedOffset = animatedOffset.value % halfNumbersColumnHeightPx

    val indexOfElement =
        getItemIndexForOffset(range, value, animatedOffset.value, halfNumbersColumnHeightPx)

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
                                val coercedAnchors = listOf(
                                    -halfNumbersColumnHeightPx, 0f, halfNumbersColumnHeightPx
                                )
                                val coercedPoint =
                                    coercedAnchors.minByOrNull { abs(it - coercedTarget) } ?: 0f
                                val base =
                                    halfNumbersColumnHeightPx * (target / halfNumbersColumnHeightPx).toInt()
                                coercedPoint + base
                            },
                        ).endState.value

                        onValueChange(
                            range.first + getItemIndexForOffset(
                                range,
                                value,
                                endValue,
                                halfNumbersColumnHeightPx
                            )
                        )
                        animatedOffset.snapTo(0f)
                    }
                },
            )
            .padding(vertical = numbersColumnHeight / 3 + verticalMargin * 2),
        content = {
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            Box(
                modifier = Modifier
                    .padding(vertical = verticalMargin)
                    .offset { IntOffset(x = 0, y = coercedAnimatedOffset.roundToInt()) }
            ) {
                if (indexOfElement > 0) Label(
                    text = label(range.first + indexOfElement - 1),
                    modifier = Modifier
                        .height(numbersColumnHeight / 3)
                        .semantics {
                            @OptIn(ExperimentalComposeUiApi::class) this.invisibleToUser()
                        }
                        .offset(y = -halfNumbersColumnHeight)
                        .alpha(
                            maxOf(minimumAlpha, coercedAnimatedOffset / halfNumbersColumnHeightPx)
                        ),
                )
                var showTextEdit by remember { mutableStateOf(false) }
                Crossfade(showTextEdit, label = "edit toggle") { state ->
                    if (state) {
                        val focusRequester = remember { FocusRequester() }
                        var inputValue by remember {
                            val valueText = formatNumber(value)
                            mutableStateOf(
                                TextFieldValue(
                                    valueText, selection = TextRange(0, valueText.length)
                                )
                            )
                        }
                        LaunchedEffect(Unit) { focusRequester.requestFocus() }
                        var isCapturedOnce by remember { mutableStateOf(false) }

                        val interactionSource = remember { MutableInteractionSource() }
                        val isFocused by interactionSource.collectIsFocusedAsState()
                        if (isFocused && !isCapturedOnce) isCapturedOnce = true
                        if (!isFocused && isCapturedOnce) showTextEdit = false
                        Box(
                            Modifier.height(numbersColumnHeight / 3),
                            contentAlignment = Alignment.Center,
                        ) {
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
                                    },
                                ),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done,
                                ),
                                textStyle = LocalTextStyle.current.copy(
                                    textAlign = TextAlign.Center,
                                    color = LocalContentColor.current,
                                ),
                                modifier = Modifier.focusRequester(focusRequester),
                            )
                        }
                    } else Label(
                        text = label(range.first + indexOfElement),
                        modifier = Modifier
                            .height(numbersColumnHeight / 3)
                            .alpha(
                                (maxOf(
                                    minimumAlpha,
                                    1 - abs(coercedAnimatedOffset) / halfNumbersColumnHeightPx
                                ))
                            )
                            .then(
                                if (disableEdit) Modifier else Modifier.clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() },
                                    onClickLabel = onClickLabel,
                                ) { showTextEdit = true }
                            ),
                    )
                }
                if (indexOfElement < range.last - range.first) Label(
                    text = label(range.first + indexOfElement + 1),
                    modifier = Modifier
                        .height(numbersColumnHeight / 3)
                        .semantics {
                            @OptIn(ExperimentalComposeUiApi::class) this.invisibleToUser()
                        }
                        .offset(y = halfNumbersColumnHeight)
                        .alpha(
                            maxOf(minimumAlpha, -coercedAnimatedOffset / halfNumbersColumnHeightPx)
                        ),
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
        }
    ) { measurables, constraints ->
        val placeables = measurables.map { measurable -> measurable.measure(constraints) }
        layout(constraints.maxWidth, placeables.sumOf { it.height }) {
            placeables.fold(0) { yPosition, placeable ->
                placeable.placeRelative(x = 0, y = yPosition)
                yPosition + placeable.height
            }
        }
    }
}

private fun getItemIndexForOffset(
    range: IntRange,
    value: Int,
    offset: Float,
    halfNumbersColumnHeightPx: Float
): Int {
    val indexOf = value - range.first - (offset / halfNumbersColumnHeightPx).toInt()
    return indexOf.coerceIn(0, range.last - range.first)
}

@Composable
private fun Label(text: String, modifier: Modifier) {
    Box(contentAlignment = Alignment.Center, modifier = modifier.fillMaxWidth()) {
        Text(maxLines = 1, text = text)
    }
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
