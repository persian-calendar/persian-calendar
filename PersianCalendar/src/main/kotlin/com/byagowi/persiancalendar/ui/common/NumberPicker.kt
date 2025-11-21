package com.byagowi.persiancalendar.ui.common

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.global.numeral
import com.byagowi.persiancalendar.ui.theme.animateColor
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

// The following is brought from
// https://github.com/ChargeMap/Compose-NumberPicker and customized to support number edits
// MIT licensed
@Composable
fun NumberPicker(
    modifier: Modifier = Modifier,
    label: (Int) -> String = numeral.collectAsState().value::format,
    range: IntRange,
    onClickLabel: String? = null,
    disableEdit: Boolean = false,
    onPreviousLabel: String? = null,
    onNextLabel: String? = null,
    pendingConfirms: MutableCollection<() -> Unit>,
    value: Int,
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
                                halfNumbersColumnHeightPx,
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
                    .offset { IntOffset(x = 0, y = coercedAnimatedOffset.roundToInt()) },
            ) {
                fun previousItemExists() = indexOfElement > 0
                fun nextItemExists() = indexOfElement < range.last - range.first
                if (previousItemExists()) Label(
                    text = label(range.first + indexOfElement - 1),
                    modifier = Modifier
                        .height(numbersColumnHeight / 3)
                        .offset(y = -halfNumbersColumnHeight)
                        .alpha(
                            maxOf(minimumAlpha, coercedAnimatedOffset / halfNumbersColumnHeightPx)
                        )
                        .clickable(
                            onClickLabel = onPreviousLabel,
                            indication = null,
                            interactionSource = null,
                        ) {
                            if (pendingConfirms.isEmpty()) onValueChange(value - 1)
                            else pendingConfirms.forEach { it() }
                        }
                        .semantics(mergeDescendants = true) { this.hideFromAccessibility() }
                        .clearAndSetSemantics {},
                )
                var showTextEdit by remember { mutableStateOf(false) }
                Crossfade(showTextEdit, label = "edit toggle") { isInNumberEdit ->
                    if (isInNumberEdit) NumberEdit(
                        dismissNumberEdit = { showTextEdit = false },
                        modifier = Modifier.height(numbersColumnHeight / 3),
                        pendingConfirms = pendingConfirms,
                        isValid = { it in range },
                        initialValue = value,
                        onValueChange = onValueChange,
                    ) else Label(
                        text = label(range.first + indexOfElement),
                        modifier = Modifier
                            .semantics {
                                this.role = Role.ValuePicker
                                this.customActions = listOfNotNull(
                                    onPreviousLabel?.takeIf { previousItemExists() }?.let {
                                        CustomAccessibilityAction(it) { onValueChange(value - 1); true }
                                    },
                                    onNextLabel?.takeIf { nextItemExists() }?.let {
                                        CustomAccessibilityAction(it) { onValueChange(value + 1); true }
                                    },
                                )
                            }
                            .height(numbersColumnHeight / 3)
                            .alpha(
                                maxOf(
                                    minimumAlpha,
                                    1 - abs(coercedAnimatedOffset) / halfNumbersColumnHeightPx
                                )
                            )
                            .then(
                                if (disableEdit) Modifier else Modifier.clickable(
                                    indication = null,
                                    interactionSource = null,
                                    onClickLabel = onClickLabel,
                                ) { showTextEdit = true },
                            ),
                    )
                }
                if (nextItemExists()) Label(
                    text = label(range.first + indexOfElement + 1),
                    modifier = Modifier
                        .height(numbersColumnHeight / 3)
                        .offset(y = halfNumbersColumnHeight)
                        .alpha(
                            maxOf(minimumAlpha, -coercedAnimatedOffset / halfNumbersColumnHeightPx)
                        )
                        .clickable(
                            onClickLabel = onNextLabel,
                            indication = null,
                            interactionSource = null,
                        ) {
                            if (pendingConfirms.isEmpty()) onValueChange(value + 1)
                            else pendingConfirms.forEach { it() }
                        }
                        .semantics(mergeDescendants = true) { this.hideFromAccessibility() }
                        .clearAndSetSemantics {},
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
        },
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

@Composable
fun NumberEdit(
    dismissNumberEdit: () -> Unit,
    pendingConfirms: MutableCollection<() -> Unit>,
    modifier: Modifier = Modifier,
    isValid: (Int) -> Boolean,
    initialValue: Int,
    onValueChange: (Int) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    val numeral by numeral.collectAsState()
    var value by remember(numeral) {
        val valueText = numeral.format(initialValue)
        mutableStateOf(TextFieldValue(valueText, selection = TextRange(0, valueText.length)))
    }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    var isCapturedOnce by remember { mutableStateOf(false) }

    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    if (isFocused && !isCapturedOnce) isCapturedOnce = true
    if (!isFocused && isCapturedOnce) dismissNumberEdit()

    val clearFocus = remember(focusManager, dismissNumberEdit) {
        { focusManager.clearFocus(); dismissNumberEdit() }
    }

    DisposableEffect(clearFocus) {
        pendingConfirms.add(clearFocus)
        onDispose { pendingConfirms.remove(clearFocus) }
    }
    BackHandler { clearFocus() }

    fun resolveValue() = value.text.toIntOrNull()?.takeIf(isValid)

    Box(modifier, contentAlignment = Alignment.Center) {
        val textBackground by animateColor(
            if (resolveValue() == null) MaterialTheme.colorScheme.error.copy(alpha = .1f)
            else Color.Transparent
        )
        BasicTextField(
            value = value,
            interactionSource = interactionSource,
            maxLines = 1,
            onValueChange = { value = it },
            keyboardActions = KeyboardActions(onDone = { pendingConfirms.clear(); clearFocus() }),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done,
            ),
            textStyle = LocalTextStyle.current.copy(
                textAlign = TextAlign.Center,
                color = LocalContentColor.current,
                background = textBackground,
            ),
            modifier = Modifier
                .focusRequester(focusRequester)
                .onFocusChanged {
                    if (!it.isFocused) {
                        pendingConfirms.remove(clearFocus)
                        resolveValue()?.let(onValueChange)
                    }
                },
        )
    }
}

private fun getItemIndexForOffset(
    range: IntRange,
    value: Int,
    offset: Float,
    halfNumbersColumnHeightPx: Float,
): Int {
    val indexOf = value - range.first - (offset / halfNumbersColumnHeightPx).toInt()
    return indexOf.coerceIn(0, range.last - range.first)
}

@Composable
private fun Label(text: String, modifier: Modifier) {
    Box(contentAlignment = Alignment.Center, modifier = modifier.fillMaxWidth()) {
        Text(
            text = text,
            style = LocalTextStyle.current,
            maxLines = 1,
            softWrap = false,
            autoSize = TextAutoSize.StepBased(
                minFontSize = MaterialTheme.typography.labelSmall.fontSize,
                maxFontSize = LocalTextStyle.current.fontSize,
            )
        )
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
            block = block,
        )
    } else {
        animateDecay(
            initialVelocity = initialVelocity,
            animationSpec = animationSpec,
            block = block,
        )
    }
}
