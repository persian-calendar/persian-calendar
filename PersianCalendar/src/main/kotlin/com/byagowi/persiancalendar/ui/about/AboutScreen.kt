package com.byagowi.persiancalendar.ui.about

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.graphics.ExperimentalAnimationGraphicsApi
import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RenderEffect
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.findNavController
import com.byagowi.persiancalendar.BuildConfig
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.AppBarBinding
import com.byagowi.persiancalendar.generated.faq
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.ui.utils.bringMarketPage
import com.byagowi.persiancalendar.ui.utils.getCompatDrawable
import com.byagowi.persiancalendar.ui.utils.hideToolbarBottomShadow
import com.byagowi.persiancalendar.ui.utils.layoutInflater
import com.byagowi.persiancalendar.ui.utils.navigateSafe
import com.byagowi.persiancalendar.ui.utils.onClick
import com.byagowi.persiancalendar.ui.utils.resolveColor
import com.byagowi.persiancalendar.ui.utils.setupMenuNavigation
import com.byagowi.persiancalendar.ui.utils.topRoundedCornerShape
import com.byagowi.persiancalendar.utils.formatNumber
import com.byagowi.persiancalendar.utils.logException
import com.byagowi.persiancalendar.utils.supportedYearOfIranCalendar
import com.google.accompanist.themeadapter.material3.Mdc3Theme

class AboutScreen : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val root = ComposeView(inflater.context)
        root.setContent {
            Mdc3Theme {
                Column {
                    Spacer(Modifier.windowInsetsTopHeight(WindowInsets.safeDrawing))
                    AboutScreenToolbar()
                    AboutScreenRoot()
                }
            }
        }
        return root.rootView
    }
}

@Composable
private fun AboutScreenRoot() {
    val context = LocalContext.current
    val version = remember {
        buildAnnotatedString {
            withStyle(style = SpanStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold)) {
                appendLine(context.getString(R.string.app_name))
            }
            withStyle(style = SpanStyle(fontSize = 12.sp)) {
                val version =
                    // Don't formatNumber it if is multi-parted
                    if ("-" in BuildConfig.VERSION_NAME) BuildConfig.VERSION_NAME
                    else formatNumber(BuildConfig.VERSION_NAME)
                append(context.getString(R.string.version, version))
                if (language.isUserAbleToReadPersian) {
                    appendLine()
                    append(
                        context.getString(
                            R.string.about_help_subtitle,
                            formatNumber(supportedYearOfIranCalendar - 1),
                            formatNumber(supportedYearOfIranCalendar)
                        )
                    )
                }
            }
        }
    }
    val clickHandlerDialog = remember { createEasterEggClickHandler(::showPeriodicTableDialog) }

    Box(modifier = Modifier.clip(topRoundedCornerShape)) {
        var logoAnimationAtEnd by remember { mutableStateOf(false) }
        var logoEffect by remember { mutableStateOf<RenderEffect?>(null) }
        LaunchedEffect(key1 = null) { logoAnimationAtEnd = !logoAnimationAtEnd }

        val headerSize = 250.dp

        @OptIn(ExperimentalAnimationGraphicsApi::class)
        Row(
            Modifier
                .height(headerSize)
                .fillMaxWidth(),
        ) {
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(start = 24.dp, end = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    version,
                    color = Color(context.resolveColor(R.attr.colorOnAppBar))
                )
            }
            val image =
                AnimatedImageVector.animatedVectorResource(R.drawable.splash_icon_animation)
            Box(
                Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Image(
                        modifier = Modifier
                            .graphicsLayer { renderEffect = logoEffect }
                            .fillMaxWidth()
                            .fillMaxHeight(),
                        painter = rememberAnimatedVectorPainter(image, logoAnimationAtEnd),
                        contentDescription = stringResource(R.string.app_name),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }

        val effectsGenerator = remember {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) createIconRandomEffects()
            else null
        }

        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(headerSize)
                    .clickable(onClickLabel = version.toString()) {
                        logoAnimationAtEnd = !logoAnimationAtEnd
                        clickHandlerDialog(context as? FragmentActivity) // TODO: Ugly cast
                        logoEffect = effectsGenerator
                            ?.invoke()
                            ?.asComposeRenderEffect()
                    })
            Surface(shape = topRoundedCornerShape) {
                Box(modifier = Modifier.padding(16.dp, 16.dp, 16.dp)) { AboutScreenContent() }
            }
        }
    }
}

@Composable
private fun AboutScreenToolbar() {
    AndroidView(modifier = Modifier.fillMaxWidth(), factory = { context ->
        val appBar = AppBarBinding.inflate(context.layoutInflater)
        appBar.toolbar.setTitle(R.string.about)
        appBar.toolbar.setupMenuNavigation()
        appBar.toolbar.menu.add(R.string.share).also {
            it.icon =
                appBar.toolbar.context.getCompatDrawable(R.drawable.ic_baseline_share)
            it.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            it.onClick { shareApplication(context) }
        }
        appBar.toolbar.menu.add(R.string.device_information).also {
            it.icon =
                appBar.toolbar.context.getCompatDrawable(R.drawable.ic_device_information)
            it.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            it.onClick {
                // TODO: Ugly cast
                (context as? FragmentActivity)?.findNavController(R.id.navHostFragment)
                    ?.navigateSafe(AboutScreenDirections.actionAboutToDeviceInformation())
            }
        }
        appBar.root.hideToolbarBottomShadow()
        appBar.root
    })
}

private fun shareApplication(context: Context) {
    runCatching {
        context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.app_name))
            val textToShare = """${context.getString(R.string.app_name)}
https://github.com/persian-calendar/persian-calendar"""
            putExtra(Intent.EXTRA_TEXT, textToShare)
        }, context.getString(R.string.share)))
    }.onFailure(logException).onFailure { (context as? FragmentActivity)?.bringMarketPage() }
}

@Composable
private fun AboutScreenContent() {
    Column {
        // Licenses
        val context = LocalContext.current
        Text(
            stringResource(R.string.licenses),
            modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 4.dp),
        )
        AboutScreenButton(
            icon = ImageVector.vectorResource(R.drawable.ic_licences),
            action = {
                // TODO: Ugly cast
                (context as? FragmentActivity)?.findNavController(R.id.navHostFragment)
                    ?.navigateSafe(AboutScreenDirections.actionAboutToLicenses())
            },
            title = R.string.about_license_title,
            summary = R.string.about_license_sum
        )

        // Help
        if (language.isUserAbleToReadPersian) {
            Row(modifier = Modifier.padding(top = 16.dp)) {
                Icon(
                    modifier = Modifier.padding(start = 8.dp, end = 4.dp),
                    imageVector = ImageVector.vectorResource(R.drawable.ic_help),
                    contentDescription = stringResource(R.string.help)
                )
                Column { Text(stringResource(R.string.help)) }
            }
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                HelpItems()
            }
        }

        // Bug report
        Text(
            stringResource(R.string.about_support_developers),
            modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 12.dp),
        )
        AboutScreenButton(
            icon = ImageVector.vectorResource(R.drawable.ic_bug),
            action = ::launchReportIntent,
            title = R.string.about_report_bug,
            summary = R.string.about_report_bug_sum
        )
        AboutScreenButton(
            icon = Icons.Default.Email,
            action = click@{
                // TODO: Ugly cast
                showEmailDialog(context as? FragmentActivity ?: return@click)
            },
            title = R.string.about_sendMail,
            summary = R.string.about_email_sum
        )

        // Developers
        Text(
            stringResource(R.string.about_developers),
            modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 12.dp),
        )
        DevelopersChips()

        Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.systemBars))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HelpItems() {
    val sections = remember {
        faq
            .split(Regex("^={4}$", RegexOption.MULTILINE))
            .map { it.trim().lines() }
            .map { lines ->
                val title = lines.first()
                val body = lines.drop(1).joinToString("\n").trim()
                title to body
            }
    }
    val expansionsState = remember { List(sections.size) { false }.toMutableStateList() }
    val initialDegree = 90f
    Column {
        sections.forEachIndexed { i, (title, body) ->
            val isExpanded = expansionsState[i]
            val angle = animateFloatAsState(
                if (isExpanded) 0f else initialDegree, label = "angle"
            ).value
            Column(modifier = Modifier
                .clickable { expansionsState[i] = !expansionsState[i] }
                .padding(6.dp)
                .fillMaxWidth()
                .animateContentSize()) {
                FlowRow(verticalArrangement = Arrangement.Center) {
                    Image(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = stringResource(R.string.more),
                        modifier = Modifier
                            .rotate(angle)
                            .size(24.dp, 24.dp),
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                    )
                    Text(title)
                }
                if (isExpanded) SelectionContainer { Text(body) }
            }
        }
    }
}

@Composable
private fun AboutScreenButton(
    icon: ImageVector,
    action: (context: Context) -> Unit,
    @StringRes title: Int,
    @StringRes summary: Int,
) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .clickable(onClickLabel = stringResource(title)) { action(context) }
            .padding(start = 8.dp, end = 12.dp, top = 4.dp, bottom = 4.dp)) {
        Row {
            Icon(
                modifier = Modifier.padding(end = 4.dp),
                imageVector = icon,
                contentDescription = stringResource(title)
            )
            Column {
                Text(stringResource(title))
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    stringResource(summary),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

private fun launchReportIntent(context: Context) {
    runCatching {
        val uri = "https://github.com/persian-calendar/persian-calendar/issues/new".toUri()
        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
    }.onFailure(logException)
}

@Composable
private fun DevelopersChips() {
    val context = LocalContext.current
    val developers = remember {
        listOf(
            R.string.about_developers_list to R.drawable.ic_developer,
            R.string.about_designers_list to R.drawable.ic_designer,
            R.string.about_translators_list to R.drawable.ic_translator,
            R.string.about_contributors_list to R.drawable.ic_developer
        ).flatMap { (listId: Int, iconId: Int) ->
            context.getString(listId).trim().split("\n").map {
                val (username, displayName) = it.split(": ")
                Triple(username, displayName, iconId)
            }
        }.shuffled()
    }
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        @OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
        FlowRow {
            developers.forEach { (username, displayName, icon) ->
                ElevatedFilterChip(
                    modifier = Modifier.padding(2.dp),
                    onClick = click@{
                        if (username == "ImanSoltanian") return@click // The only person without GitHub account
                        runCatching {
                            val uri = "https://github.com/$username".toUri()
                            CustomTabsIntent.Builder().build().launchUrl(context, uri)
                        }.onFailure(logException)
                    },
                    label = { Text(displayName) },
                    selected = true,
                    colors = FilterChipDefaults.elevatedFilterChipColors(),
                    leadingIcon = {
                        Icon(
                            ImageVector.vectorResource(icon),
                            contentDescription = displayName,
                            Modifier.size(AssistChipDefaults.IconSize)
                        )
                    }
                )
            }
        }
    }
}
