package com.byagowi.persiancalendar.ui.astronomy

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.util.lruCache
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.entities.Season
import com.byagowi.persiancalendar.global.coordinates
import com.byagowi.persiancalendar.ui.MainActivity
import com.byagowi.persiancalendar.ui.calendar.dialogs.DayPickerDialog
import com.byagowi.persiancalendar.ui.common.SolarDraw
import com.byagowi.persiancalendar.ui.utils.MaterialCornerExtraLargeTop
import com.byagowi.persiancalendar.ui.utils.isDynamicGrayscale
import com.byagowi.persiancalendar.ui.utils.navigateSafe
import com.byagowi.persiancalendar.ui.utils.performHapticFeedbackVirtualKey
import com.byagowi.persiancalendar.ui.utils.resolveColor
import com.byagowi.persiancalendar.utils.formatDateAndTime
import com.byagowi.persiancalendar.utils.isSouthernHemisphere
import com.byagowi.persiancalendar.utils.toCivilDate
import com.byagowi.persiancalendar.utils.toGregorianCalendar
import com.google.accompanist.themeadapter.material3.Mdc3Theme
import io.github.cosinekitty.astronomy.seasons
import io.github.persiancalendar.calendar.CivilDate
import io.github.persiancalendar.calendar.PersianDate
import java.util.Date
import kotlin.math.abs

class AstronomyFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Just that our UI tests don't have access to the nav controllers, let's don't access nav there
        val ifNavAvailable = runCatching { findNavController() }.getOrNull() != null
        val viewModel =
            if (ifNavAvailable) navGraphViewModels<AstronomyViewModel>(R.id.astronomy).value else AstronomyViewModel()
        if (ifNavAvailable && viewModel.minutesOffset.value == AstronomyViewModel.DEFAULT_TIME) {
            viewModel.animateToAbsoluteDayOffset(navArgs<AstronomyFragmentArgs>().value.dayOffset)
        }

        val root = ComposeView(inflater.context)
        root.setContent {
            Mdc3Theme {
                AstronomyScreen(viewModel) {
                    // Pass time also
                    findNavController().navigateSafe(
                        AstronomyFragmentDirections.actionAstronomyToMap()
                    )
                }
            }
        }
        return root
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AstronomyScreen(viewModel: AstronomyViewModel, navigateToMap: () -> Unit) = Column {
    val context = LocalContext.current
    // TODO: Ideally this should be onPrimary
    val colorOnAppBar = Color(context.resolveColor(R.attr.colorOnAppBar))

    var showDayPickerDialog by rememberSaveable { mutableStateOf(false) }
    if (showDayPickerDialog) DayPickerDialog(initialJdn = Jdn(viewModel.astronomyState.value.date.toCivilDate()),
        positiveButtonTitle = R.string.accept,
        onSuccess = { jdn -> viewModel.animateToAbsoluteDayOffset(jdn - Jdn.today()) }) {
        showDayPickerDialog = false
    }

    var showHoroscopeDialog by rememberSaveable { mutableStateOf(false) }
    if (showHoroscopeDialog) HoroscopesDialog(viewModel.astronomyState.value.date.time) {
        showHoroscopeDialog = false
    }

    var isTropical by rememberSaveable { mutableStateOf(false) }
    var mode by rememberSaveable { mutableStateOf(AstronomyMode.entries[0]) }
    val state by viewModel.astronomyState.collectAsState()

    TopAppBar(
        title = { Text(stringResource(R.string.astronomy)) },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            navigationIconContentColor = colorOnAppBar,
            actionIconContentColor = colorOnAppBar,
            titleContentColor = colorOnAppBar,
        ),
        navigationIcon = {
            IconButton(onClick = { (context as? MainActivity)?.openDrawer() }) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = stringResource(R.string.open_drawer)
                )
            }
        },
        actions = {
            AnimatedVisibility(visible = viewModel.minutesOffset.value != 0) {
                IconButton(onClick = { viewModel.animateToAbsoluteMinutesOffset(0) }) {
                    Icon(
                        ImageVector.vectorResource(R.drawable.ic_restore_modified),
                        contentDescription = stringResource(R.string.return_to_today),
                    )
                }
            }
            AnimatedVisibility(visible = mode == AstronomyMode.Earth) {
                Row(
                    Modifier.clickable { isTropical = !isTropical },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.tropical))
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(isTropical, onCheckedChange = { isTropical = !isTropical })
                }
            }
            Box {
                var showMenu by rememberSaveable { mutableStateOf(false) }
                IconButton(onClick = { showMenu = !showMenu }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = stringResource(R.string.more_options),
                    )
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.goto_date)) },
                        onClick = {
                            showMenu = false
                            showDayPickerDialog = true
                        },
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.map)) },
                        onClick = {
                            showMenu = false
                            navigateToMap()
                        },
                    )
                }
            }
        },
    )

    val sunZodiac = if (isTropical) Zodiac.fromTropical(state.sun.elon)
    else Zodiac.fromIau(state.sun.elon)
    val moonZodiac = if (isTropical) Zodiac.fromTropical(state.moon.lon)
    else Zodiac.fromIau(state.moon.lon)

    val jdn = derivedStateOf { Jdn(state.date.toCivilDate()) }

    val headerCache = remember {
        lruCache(1024, create = { jdn: Jdn ->
            viewModel.astronomyState.value.generateHeader(context, jdn).joinToString("\n")
        })
    }

    var lastButtonClickTimestamp by remember { mutableStateOf(System.currentTimeMillis()) }
    fun buttonScrollSlider(days: Int): Boolean {
        lastButtonClickTimestamp = System.currentTimeMillis()
        // TODO: Bring back arrow keys causing slider scroll
        //  binding.slider.smoothScrollBy(250f * days * viewDirection, 0f)
        viewModel.animateToRelativeDayOffset(days)
        return true
    }

    Surface(shape = MaterialCornerExtraLargeTop()) {
        Box {
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(Modifier.height(24.dp))
                Column(Modifier.padding(horizontal = 24.dp)) {
                    SelectionContainer {
                        Text(
                            headerCache[jdn.value],
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 3
                        )
                    }
                    Seasons(jdn.value)
                    AnimatedVisibility(visible = mode == AstronomyMode.Earth) {
                        Row(Modifier.padding(top = 8.dp)) {
                            Box(Modifier.weight(1f)) {
                                Cell(
                                    Modifier.align(Alignment.Center),
                                    0xcceaaa00.toInt(),
                                    stringResource(R.string.sun),
                                    sunZodiac.format(context, true) // ☉☀️
                                )
                            }
                            Box(Modifier.weight(1f)) {
                                Cell(
                                    Modifier.align(Alignment.Center),
                                    0xcc606060.toInt(),
                                    stringResource(R.string.moon),
                                    moonZodiac.format(context, true) // ☽it.moonPhaseEmoji
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Box(Modifier.fillMaxWidth()) {
                    Column(Modifier.align(Alignment.CenterStart)) {
                        AstronomyMode.entries.forEach {
                            NavigationRailItem(
                                modifier = Modifier.size(56.dp, 56.dp),
                                selected = mode == it,
                                onClick = { mode = it },
                                icon = {
                                    if (it == AstronomyMode.Moon) MoonIcon(state) else Icon(
                                        ImageVector.vectorResource(it.icon),
                                        modifier = Modifier.size(24.dp, 24.dp),
                                        contentDescription = null,
                                        tint = Color.Unspecified,
                                    )
                                },
                            )
                        }
                    }
                    AndroidView(
                        factory = {
                            val solarView = SolarView(it)
                            var clickCount = 0
                            solarView.setOnClickListener {
                                if (++clickCount % 2 == 0) showHoroscopeDialog = true
                            }
                            solarView.rotationalMinutesChange = { offset ->
                                viewModel.addMinutesOffset(offset)
                                // TODO: Bring this back rotating in solar view causing slider scroll
                                //  binding.slider.manualScrollBy(offset / 200f, 0f)
                            }
                            solarView
                        },
                        modifier = Modifier
                            .size(290.dp, 290.dp)
                            .align(Alignment.Center),
                        update = {
                            it.isTropicalDegree = isTropical
                            it.setTime(state)
                            it.mode = mode
                        },
                    )
                    NavigationRailItem(
                        modifier = Modifier
                            .size(56.dp, 56.dp)
                            .align(Alignment.CenterEnd),
                        selected = false, onClick = navigateToMap,
                        icon = {
                            Text(if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) "m" else "🗺")
                        },
                    )
                }
            }
            @OptIn(ExperimentalFoundationApi::class) Column(
                Modifier
                    .align(Alignment.BottomCenter)
                    .safeDrawingPadding()
                    .fillMaxWidth()
            ) {
                Text(
                    state.date.formatDateAndTime(),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = { showDayPickerDialog = true },
                            onLongClick = { viewModel.animateToAbsoluteMinutesOffset(0) },
                        )
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(modifier = Modifier.width(16.dp))
                    Icon(
                        Icons.AutoMirrored.Default.KeyboardArrowLeft,
                        contentDescription = null,
                        Modifier.combinedClickable(
                            onClick = { buttonScrollSlider(-1) },
                            onClickLabel = stringResource(
                                R.string.previous_x, stringResource(R.string.day)
                            ),
                            onLongClick = { buttonScrollSlider(-365) },
                            onLongClickLabel = stringResource(
                                R.string.previous_x, stringResource(R.string.year)
                            ),
                        )
                    )
                    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
                    AndroidView(
                        factory = { context ->
                            val slider = SliderView(context)
                            var latestVibration = 0L
                            slider.smoothScrollBy(250f * if (isRtl) 1 else -1, 0f)
                            slider.onScrollListener = { dx, _ ->
                                if (dx != 0f) {
                                    val current = System.currentTimeMillis()
                                    if (current - lastButtonClickTimestamp > 2000) {
                                        if (current >= latestVibration + 25_000_000 / abs(dx)) {
                                            slider.performHapticFeedbackVirtualKey()
                                            latestVibration = current
                                        }
                                        viewModel.addMinutesOffset(
                                            (dx * if (isRtl) 1 else -1).toInt()
                                        )
                                    }
                                }
                            }
                            slider
                        },
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .height(46.dp)
                            .weight(1f, fill = false),
                    )
                    Icon(
                        Icons.AutoMirrored.Default.KeyboardArrowRight,
                        contentDescription = null,
                        Modifier.combinedClickable(
                            onClick = { buttonScrollSlider(+1) },
                            onClickLabel = stringResource(
                                R.string.next_x, stringResource(R.string.day)
                            ),
                            onLongClick = { buttonScrollSlider(+365) },
                            onLongClickLabel = stringResource(
                                R.string.next_x, stringResource(R.string.year)
                            ),
                        )
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun Seasons(jdn: Jdn) {
    val seasonsCache = remember { lruCache(1024, create = ::seasons) }
    val seasonsOrder = remember {
        if (coordinates.value?.isSouthernHemisphere == true) listOf(
            Season.WINTER,
            Season.SPRING,
            Season.SUMMER,
            Season.AUTUMN
        )
        else listOf(Season.SUMMER, Season.AUTUMN, Season.WINTER, Season.SPRING)
    }
    val equinoxes = (1..4).map { i ->
        Date(
            seasonsCache[CivilDate(
                PersianDate(jdn.toPersianDate().year, i * 3, 29)
            ).year].let {
                when (i) {
                    1 -> it.juneSolstice
                    2 -> it.septemberEquinox
                    3 -> it.decemberSolstice
                    else -> it.marchEquinox
                }
            }.toMillisecondsSince1970()
        ).toGregorianCalendar().formatDateAndTime()
    }
    repeat(2) { row ->
        Row(Modifier.padding(top = 8.dp)) {
            repeat(2) { cell ->
                Box(Modifier.weight(1f)) {
                    Cell(
                        Modifier,
                        seasonsOrder[cell + row * 2].color,
                        stringResource(seasonsOrder[cell + row * 2].nameStringId),
                        equinoxes[cell + row * 2],
                    )
                }
            }
        }
    }
}

@Stable
@Composable
private fun MoonIcon(astronomyState: AstronomyState) {
    val context = LocalContext.current
    val solarDraw = remember { SolarDraw(context) }
    Box(modifier = Modifier
        .size(24.dp, 24.dp)
        .drawBehind {
            drawIntoCanvas {
                val radius = size.minDimension / 2f
                val sun = astronomyState.sun
                val moon = astronomyState.moon
                solarDraw.moon(it.nativeCanvas, sun, moon, radius, radius, radius)
            }
        })
}

@Stable
@Composable
private fun Cell(modifier: Modifier, @ColorInt color: Int, label: String, value: String) {
    Row(
        modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val context = LocalContext.current
        val isDynamicGrayscale = remember { context.isDynamicGrayscale }
        Text(
            label,
            modifier = Modifier
                .background(
                    Color(if (isDynamicGrayscale) 0xcc808080.toInt() else color),
                    RoundedCornerShape(CornerSize(8.dp)),
                )
                .align(alignment = Alignment.CenterVertically)
                .padding(vertical = 4.dp, horizontal = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
        )
        Spacer(Modifier.width(8.dp))
        SelectionContainer {
            Text(value, maxLines = 1, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
