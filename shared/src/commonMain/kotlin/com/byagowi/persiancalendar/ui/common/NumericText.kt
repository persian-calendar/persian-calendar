package com.byagowi.persiancalendar.ui.common

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.SpringSpec
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.takeOrElse
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.time.Duration.Companion.milliseconds
import androidx.compose.animation.core.spring as springSpec

// Based on:
// * https://github.com/AmatoGiulio/react-native-numeric-text (MIT licensed)
// * https://github.com/rit3zh/expo-content-transition (MIT licensed)
//
// SwiftUI's numericText, expressed with Compose animation.
//
// Four channels run at once, each a second-order step response p(t) on a clock of
// its own. Every channel is linear in p, so animating p from 0 to 1 with that
// channel's spring and feeding p through its mapping reproduces the curve:
//
//   channel  mapping                       damping   response
//   offset   dy = travel * (1 - p)           0.55      353 ms
//   scale    s  = enterScale + (1 - s) * p   1.00      278 ms
//   alpha    a  = p                          1.00      276 ms
//   blur     r  = peak * (1 - p)             0.91      398 ms
//
// Those figures come from a transition recorded frame by frame and decomposed per
// glyph (react-native-numeric-text, docs/ARCHITECTURE.md §2.2), which is also
// where the amplitudes come from: a one-sided entry of .59375 glyph heights, a
// birth/death scale of .3984, and a peak blur of .125. Position needs a spring of
// its own - it overshoots (12.6% here, 12% as measured) where size and opacity
// never do.
//
// For a countdown that is:
//
//   NumericText(value = secondsRemaining, style = clockStyle, digits = 2)
//
// where `digits` fixes the column count so the width cannot jump; leave it out and
// the number gets as many columns as it needs. For a clock, pass an explicit
// direction: 59 -> 00 reads as a smaller number, but time moved forward.
//
// What of the references is here: direction (including their `auto`), the shared
// cascade budget, duration/bounce as SwiftUI's `.spring(duration:bounce:)`, the
// shape knobs (travel, enter scale, blur, blur ceiling, clip), instant changes via
// `animated`, accessibility, and the enter/exit lifecycle a digit-count change gets
// instead of a roll. Not here: retargeting an in-flight transition instead of
// restarting it (§2.6), gliding the surviving digits horizontally when the number's
// width changes, and number formatting with separators and affixes.

/**
 * Which way a digit travels during a transition.
 *
 * The two reference implementations disagree about this, so it is a parameter
 * rather than an assumption: react-native-numeric-text describes a counting-down
 * roll as the departing glyph leaving upward and the arriving one entering from
 * below, while expo-content-transition ties `countsDown: true` to the opposite
 * travel (arriving from above, leaving below). [Down] is the latter, and is the
 * default here.
 */
enum class RollDirection {
    /** Take the direction from the value that changed, the way `.numericText(value:)` does. */
    Auto,

    /** The digits move down: a new glyph enters from above, the old one leaves below. */
    Down,

    /** The digits move up: a new glyph enters from below, the old one leaves above. */
    Up;

    companion object {
        /** The direction SwiftUI's `countsDown:` argument selects, as the reference reads it. */
        fun forCountsDown(countsDown: Boolean): RollDirection = if (countsDown) Down else Up

        /**
         * The direction a change from [from] to [to] travels: up when the value grew,
         * down when it shrank, which is what both `direction="auto"` and
         * `.numericText(value:)` do.
         */
        fun forChange(from: Int?, to: Int): RollDirection =
            if (from != null && to > from) Up else Down
    }
}

/** The amplitudes and edges of a transition, all relative to the glyph height. */
@Immutable
data class NumericTextShape(
    /** How far a glyph travels, as a fraction of its line height. */
    val travelRatio: Float = NumericTextMotion.TRAVEL_RATIO,

    /** The size a glyph is born at and dies at. */
    val enterScale: Float = NumericTextMotion.ENTER_SCALE,

    /** Peak blur radius, as a fraction of the line height. */
    val blurRatio: Float = NumericTextMotion.BLUR_RATIO,

    /** Scales every blur radius; `0f` turns blur off entirely. */
    val blurIntensity: Float = 1f,

    /**
     * A ceiling on the blur radius. Blur is the expensive part of a transition, so
     * this is the knob that keeps large text from turning to soup and many columns
     * at once from costing too much.
     */
    val maxBlurRadius: Dp? = null,

    /**
     * Cut each glyph at its own box, so a rolling glyph cannot overlap whatever sits
     * above or below it. The references clip by default and apply the clip after the
     * blur, so the softness survives to the edge; off here, because a card or a
     * fixed-height slot usually clips already.
     */
    val clip: Boolean = false,
) {
    companion object {
        val Default = NumericTextShape()
    }
}

/**
 * The springs of one transition, one per channel.
 *
 * [blurOut] defaults to [blurIn], so a departing glyph blurs out on the same clock
 * the arriving one resolves on; [spring] speeds the exit up instead, the way the
 * reference does, because a glyph that is leaving can afford to go soft sooner.
 */
@Immutable
class NumericTextSprings(
    val move: SpringSpec<Float>,
    val size: SpringSpec<Float>,
    val fade: SpringSpec<Float>,
    val blurIn: SpringSpec<Float>,
    val blurOut: SpringSpec<Float> = blurIn,
) {
    companion object {
        /** The duration [spring] treats as unmodified, and its default. */
        const val REFERENCE_DURATION_MILLIS = 420

        /** The default bounce for [spring]. */
        const val DEFAULT_BOUNCE = 0.46f

        /** How much faster a departing glyph blurs out than an arriving one resolves. */
        const val EXIT_BLUR_SPEEDUP = 1.35f

        /** The measured transition: the direct fit, as shipped. */
        val Measured = NumericTextSprings(
            move = springSpec(dampingRatio = 0.55f, stiffness = stiffnessFor(0.353f)),
            size = springSpec(dampingRatio = 1.00f, stiffness = stiffnessFor(0.278f)),
            fade = springSpec(dampingRatio = 1.00f, stiffness = stiffnessFor(0.276f)),
            blurIn = springSpec(dampingRatio = 0.91f, stiffness = stiffnessFor(0.398f)),
        )

        /**
         * SwiftUI's `.spring(duration:bounce:)`, spread across the channels the way
         * expo-content-transition derives them: position and blur track the duration
         * one-for-one, size and opacity a little tighter, and the bounce moves only
         * the travel channel.
         */
        fun spring(
            durationMillis: Int = REFERENCE_DURATION_MILLIS,
            bounce: Float = DEFAULT_BOUNCE,
        ): NumericTextSprings {
            val scale = (durationMillis.toFloat() / REFERENCE_DURATION_MILLIS).coerceIn(0.1f, 10f)
            val damping = (1f - bounce).coerceIn(0.05f, 1f)
            return NumericTextSprings(
                move = springSpec(dampingRatio = damping, stiffness = stiffnessFor(0.4f * scale)),
                size = springSpec(dampingRatio = 1f, stiffness = stiffnessFor(0.34f * scale)),
                fade = springSpec(dampingRatio = 1f, stiffness = stiffnessFor(0.28f * scale)),
                blurIn = springSpec(dampingRatio = 0.91f, stiffness = stiffnessFor(0.4f * scale)),
                blurOut = springSpec(
                    dampingRatio = 0.91f,
                    stiffness = stiffnessFor(0.4f / EXIT_BLUR_SPEEDUP * scale),
                ),
            )
        }

        /** SwiftUI's `.smooth`, which is `spring(duration: 0.5, bounce: 0)`. */
        val Smooth: NumericTextSprings = spring(500, 0f)

        /** SwiftUI's `.snappy`, which is `spring(duration: 0.5, bounce: 0.15)`. */
        val Snappy: NumericTextSprings = spring(500, 0.15f)

        /** SwiftUI's `.bouncy`, which is `spring(duration: 0.5, bounce: 0.3)`. */
        val Bouncy: NumericTextSprings = spring(500, 0.3f)
    }
}

/** The measured transition, as numbers. */
object NumericTextMotion {
    /** One-sided entry amplitude, in glyph heights. */
    const val TRAVEL_RATIO = 0.59375f

    /** The size a glyph is born at and dies at. */
    const val ENTER_SCALE = 0.3984f

    /** Peak blur radius, in glyph heights. */
    const val BLUR_RATIO = 0.125f

    /** One shared budget for the left-to-right cascade of a number, in milliseconds. */
    const val CASCADE_MILLIS = 150

    /** The springs of the measured transition. */
    val springs: NumericTextSprings = NumericTextSprings.Measured

    /** The shape of the measured transition. */
    val shape: NumericTextShape = NumericTextShape.Default
}

/**
 * Compose springs take a stiffness and have no mass, so a SwiftUI response - the
 * period, in seconds - converts as w = sqrt(stiffness) = 2 * pi / response.
 */
private fun stiffnessFor(responseSeconds: Float): Float {
    val omega = (2.0 * PI / responseSeconds).toFloat()
    return omega * omega
}

/**
 * The delay for the [index]th of the [changed] columns that change together.
 *
 * The cascade runs left to right, most significant first, and divides one budget
 * between the columns that actually change - a lone changed column has no delay.
 */
fun cascadeDelay(
    index: Int,
    changed: Int,
    cascadeMillis: Int = NumericTextMotion.CASCADE_MILLIS,
): Int = if (changed < 2) 0 else cascadeMillis * index / (changed - 1)

/**
 * One digit column that rolls to [glyph], [delayMillis] into the cascade.
 *
 * The amplitudes are fractions of the glyph height, so [style] has to resolve a
 * line height or a font size in sp for them to scale with the text. A single column
 * has no number to compare, so [RollDirection.Auto] reads as [RollDirection.Down]
 * here; [NumericText] is where Auto means something.
 */
@Composable
fun RollingDigit(
    glyph: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    delayMillis: Int = 0,
    countsDown: Boolean = true,
    direction: RollDirection = RollDirection.forCountsDown(countsDown),
    springs: NumericTextSprings = NumericTextMotion.springs,
    shape: NumericTextShape = NumericTextMotion.shape,
    animated: Boolean = true,
) {
    var arriving by remember { mutableStateOf(glyph) }
    var departing by remember { mutableStateOf(glyph) }

    // One progress value per channel, all at rest at 1.
    val moveProgress = remember { Animatable(1f) }
    val sizeProgress = remember { Animatable(1f) }
    val fadeProgress = remember { Animatable(1f) }
    val blurInProgress = remember { Animatable(1f) }
    val blurOutProgress = remember { Animatable(1f) }

    val glyphHeight = with(LocalDensity.current) {
        style.lineHeight.takeOrElse { style.fontSize }.toDp()
    }
    val entry = glyphHeight * shape.travelRatio
    // The direction the arriving glyph travels from: below it, for an upward roll.
    val travel = if (direction == RollDirection.Up) entry else -entry
    val blurRadius = glyphHeight * shape.blurRatio * shape.blurIntensity
    val peakBlur = shape.maxBlurRadius?.let { ceiling -> minOf(blurRadius, ceiling) } ?: blurRadius

    LaunchedEffect(glyph, animated) {
        if (arriving == glyph) return@LaunchedEffect // first composition: nothing to animate
        departing = arriving
        arriving = glyph

        if (!animated) {
            // The value still changes; it just appears in place. Left at rest, the
            // departing glyph is already invisible and the arriving one already clean.
            return@LaunchedEffect
        }

        // Restart from the start of the transition. At a cadence faster than one
        // transition the measured behaviour lets the older glyph run its own curves
        // to completion rather than resetting them (§2.6); that needs one job per
        // glyph and only pays off for sub-second updates.
        moveProgress.snapTo(0f)
        sizeProgress.snapTo(0f)
        fadeProgress.snapTo(0f)
        blurInProgress.snapTo(0f)
        blurOutProgress.snapTo(0f)

        // Until the cascade reaches this column it still shows the old glyph at rest.
        delay(delayMillis.milliseconds)

        launch { moveProgress.animateTo(1f, springs.move) }
        launch { sizeProgress.animateTo(1f, springs.size) }
        launch { fadeProgress.animateTo(1f, springs.fade) }
        launch { blurInProgress.animateTo(1f, springs.blurIn) }
        launch { blurOutProgress.animateTo(1f, springs.blurOut) }
    }

    Box(
        // Two glyph layers overlap here and a transition may be on top of both, so the
        // column speaks for itself rather than letting a reader walk the layers.
        modifier.clearAndSetSemantics { text = AnnotatedString(glyph) },
        contentAlignment = Alignment.Center,
    ) {
        Glyph(
            text = departing,
            style = style,
            // The departing glyph runs the same curves in reverse.
            offset = -travel * moveProgress.value,
            size = 1f - (1f - shape.enterScale) * sizeProgress.value,
            opacity = 1f - fadeProgress.value,
            radius = peakBlur * blurOutProgress.value,
            clip = shape.clip,
        )
        Glyph(
            text = arriving,
            style = style,
            offset = travel * (1f - moveProgress.value),
            size = shape.enterScale + (1f - shape.enterScale) * sizeProgress.value,
            opacity = fadeProgress.value,
            radius = peakBlur * (1f - blurInProgress.value),
            clip = shape.clip,
        )
    }
}

/**
 * A number, one column per digit, that rolls when [value] changes - the analogue of
 *
 *     Text("\(value)")
 *         .contentTransition(.numericText(countsDown: countsDown))
 *         .animation(animation, value: value)
 *
 * The first value is shown without animating. [digits] fixes the column count, zero
 * padding shorter values and keeping only the low-order digits of longer ones; left
 * out, the number gets exactly the columns it needs.
 *
 * Note that an inferred column count changes with the value, so a number that gains
 * or loses a digit (99 -> 100) re-lays out the row. Pass [digits] to hold the width
 * still, which is what a countdown wants.
 */
@Composable
fun NumericText(
    value: Int,
    style: TextStyle,
    modifier: Modifier = Modifier,
    digits: Int? = null,
    countsDown: Boolean = true,
    direction: RollDirection = RollDirection.forCountsDown(countsDown),
    springs: NumericTextSprings = NumericTextMotion.springs,
    shape: NumericTextShape = NumericTextMotion.shape,
    cascadeMillis: Int = NumericTextMotion.CASCADE_MILLIS,
    spacing: Dp = 2.dp,
    animated: Boolean = true,
    semanticsLabel: String? = null,
    announceChanges: Boolean = false,
) {
    val glyphs = glyphsOf(value, digits ?: columnsFor(value))
    // The digits being replaced are read during composition and stored afterwards, so
    // each transition is planned against what it is actually replacing.
    var replaced by remember { mutableStateOf(glyphs) }
    var replacedValue by remember { mutableStateOf(value) }
    val plan = remember(glyphs, cascadeMillis, direction, shape) {
        planTransition(glyphs, replaced, replacedValue, value, direction, shape, cascadeMillis)
    }
    LaunchedEffect(glyphs) {
        replaced = glyphs
        replacedValue = value
    }

    // One node for the whole number: a reader should hear "12", not six layers of "1"
    // and "2", and a timer should not be read out on every tick unless asked.
    val readable = modifier.clearAndSetSemantics {
        text = AnnotatedString(glyphs.joinToString(""))
        if (semanticsLabel != null) contentDescription = semanticsLabel
        if (announceChanges) liveRegion = LiveRegionMode.Polite
    }

    // Digits keep their own order inside a right-to-left page.
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Row(
            modifier = readable,
            horizontalArrangement = Arrangement.spacedBy(spacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            glyphs.forEachIndexed { index, glyph ->
                RollingDigit(
                    glyph = glyph,
                    style = style,
                    delayMillis = plan.delays[index],
                    direction = plan.direction,
                    springs = springs,
                    shape = plan.shape,
                    animated = animated,
                )
            }
        }
    }
}

/**
 * Works out how [glyphs] should replace [replaced]: when each column starts, which way
 * they travel, and whether this is a structural change.
 *
 * A change in digit count is structural rather than a roll (§2.5). The column that
 * appeared or disappeared enters or leaves *in place* - small and blurred, then
 * resolving - instead of travelling the full height of the line, and nothing staggers,
 * because the horizontal re-layout is not part of the cascade. Only an inferred column
 * count can change, so a fixed `digits` never triggers this.
 */
private fun planTransition(
    glyphs: List<String>,
    replaced: List<String>,
    replacedValue: Int,
    value: Int,
    direction: RollDirection,
    shape: NumericTextShape,
    cascadeMillis: Int,
): TransitionPlan {
    val structural = replaced.size != glyphs.size
    val changed = glyphs.indices.filter { replaced.getOrNull(it) != glyphs[it] }
    return TransitionPlan(
        delays = IntArray(glyphs.size) { index ->
            val order = changed.indexOf(index)
            when {
                structural || order < 0 -> 0
                else -> cascadeDelay(order, changed.size, cascadeMillis)
            }
        },
        direction = if (direction == RollDirection.Auto) {
            RollDirection.forChange(replacedValue, value)
        } else {
            direction
        },
        shape = if (structural) shape.copy(travelRatio = 0f) else shape,
    )
}

/**
 * What one transition decided, worked out once and then held for its whole duration.
 *
 * Reading it out of a live composition would let a recomposition in the middle of a
 * cascade change delays that are already in flight, or flip the direction of a column
 * that has not started yet.
 */
private class TransitionPlan(
    val delays: IntArray,
    val direction: RollDirection,
    val shape: NumericTextShape,
)

/** The digits of [value], zero-padded to [digits] columns and cut to its low-order ones. */
private fun glyphsOf(value: Int, digits: Int): List<String> {
    val out = MutableList(digits.coerceAtLeast(1)) { "0" }
    var rest = value.coerceAtLeast(0)
    for (index in out.indices.reversed()) {
        out[index] = (rest % 10).toString()
        rest /= 10
    }
    return out
}

/** How many columns [value] needs to be written out, at least one. */
private fun columnsFor(value: Int): Int {
    var rest = value.coerceAtLeast(0)
    var count = 1
    while (rest >= 10) {
        rest /= 10
        count++
    }
    return count
}

@Composable
private fun Glyph(
    text: String,
    style: TextStyle,
    offset: Dp,
    size: Float,
    opacity: Float,
    radius: Dp,
    clip: Boolean,
) {
    // The clip goes outermost, so it is applied to the blurred glyph rather than
    // letting the blur be cut at the unclipped bounds.
    val clipping = if (clip) Modifier.clipToBounds() else Modifier
    BasicText(
        text = text,
        style = style,
        modifier = clipping
            .graphicsLayer {
                translationY = offset.toPx()
                scaleX = size
                scaleY = size
                alpha = opacity
            }
            // Modifier.blur is a no-op below API 31; there the roll still reads,
            // just flatter, because size and opacity carry it.
            .blur(radius = radius, edgeTreatment = BlurredEdgeTreatment.Unbounded),
    )
}
