package com.byagowi.persiancalendar.ui.about

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.StringRes
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PermDeviceInformation
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalMinimumInteractiveComponentEnforcement
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RenderEffect
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.byagowi.persiancalendar.BuildConfig
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.generated.faq
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.ui.common.AppIconButton
import com.byagowi.persiancalendar.ui.common.ExpandArrow
import com.byagowi.persiancalendar.ui.common.NavigationOpenDrawerIcon
import com.byagowi.persiancalendar.ui.icons.MaterialIconDimension
import com.byagowi.persiancalendar.ui.theme.animatedSurfaceColor
import com.byagowi.persiancalendar.ui.theme.appTopAppBarColors
import com.byagowi.persiancalendar.ui.utils.bringMarketPage
import com.byagowi.persiancalendar.ui.utils.getActivity
import com.byagowi.persiancalendar.ui.utils.materialCornerExtraLargeTop
import com.byagowi.persiancalendar.utils.formatNumber
import com.byagowi.persiancalendar.utils.logException
import com.byagowi.persiancalendar.utils.supportedYearOfIranCalendar

context(AnimatedContentScope, SharedTransitionScope)
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AboutScreen(
    openDrawer: () -> Unit,
    navigateToDeviceInformation: () -> Unit,
    navigateToLicenses: () -> Unit,
) {
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text(stringResource(R.string.about)) },
                colors = appTopAppBarColors(),
                navigationIcon = { NavigationOpenDrawerIcon(openDrawer) },
                actions = {
                    val context = LocalContext.current
                    AppIconButton(
                        icon = Icons.Default.Share,
                        title = stringResource(R.string.share),
                    ) { shareApplication(context) }
                    AppIconButton(
                        icon = Icons.Default.PermDeviceInformation,
                        title = stringResource(R.string.device_information),
                        onClick = navigateToDeviceInformation,
                    )
                },
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(top = paddingValues.calculateTopPadding())
                .clip(materialCornerExtraLargeTop()),
        ) {
            val scrollState = rememberScrollState()
            Column(modifier = Modifier.verticalScroll(scrollState)) {
                Box(Modifier.offset { IntOffset(0, scrollState.value * 3 / 4) }) { Header() }
                Surface(
                    shape = materialCornerExtraLargeTop(),
                    color = animatedSurfaceColor(),
                ) { AboutScreenContent(navigateToLicenses, paddingValues.calculateBottomPadding()) }
            }
        }
    }
}

@Composable
private fun Header() {
    val clickHandlerDialog = remember { createEasterEggClickHandler(::showPeriodicTableDialog) }

    var logoAnimationAtEnd by remember { mutableStateOf(false) }
    var logoEffect by remember { mutableStateOf<RenderEffect?>(null) }
    LaunchedEffect(Unit) { logoAnimationAtEnd = !logoAnimationAtEnd }

    val effectsGenerator = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) createIconRandomEffects()
        else null
    }

    Row(
        Modifier
            .height(250.dp)
            .fillMaxWidth(),
    ) {
        Box(
            Modifier
                .weight(1f)
                .fillMaxSize()
                .padding(horizontal = MaterialIconDimension.dp),
            contentAlignment = Alignment.Center
        ) {
            Column {
                Text(
                    stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = LocalContentColor.current,
                )
                Text(
                    buildString {
                        val version =
                            // Don't formatNumber it if is multi-parted
                            if ("-" in BuildConfig.VERSION_NAME) BuildConfig.VERSION_NAME
                            else formatNumber(BuildConfig.VERSION_NAME)
                        append(stringResource(R.string.version, version))
                        if (language.value.isUserAbleToReadPersian) {
                            appendLine()
                            append(
                                stringResource(
                                    R.string.about_help_subtitle,
                                    formatNumber(supportedYearOfIranCalendar - 1),
                                    formatNumber(supportedYearOfIranCalendar)
                                )
                            )
                        }
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = LocalContentColor.current,
                )
            }
        }
        val context = LocalContext.current
        Box(
            Modifier
                .weight(1f)
                .semantics { @OptIn(ExperimentalComposeUiApi::class) this.invisibleToUser() }
                .clickable {
                    logoAnimationAtEnd = !logoAnimationAtEnd
                    clickHandlerDialog(context.getActivity())
                    logoEffect = effectsGenerator
                        ?.invoke()
                        ?.asComposeRenderEffect()
                },
            contentAlignment = Alignment.Center,
        ) {
            @OptIn(ExperimentalAnimationGraphicsApi::class) CompositionLocalProvider(
                LocalLayoutDirection provides LayoutDirection.Ltr
            ) {
                val image =
                    AnimatedImageVector.animatedVectorResource(R.drawable.splash_icon_animation)
                Image(
                    modifier = Modifier
                        .graphicsLayer { renderEffect = logoEffect }
                        .fillMaxSize(),
                    painter = rememberAnimatedVectorPainter(image, logoAnimationAtEnd),
                    contentDescription = stringResource(R.string.app_name),
                    contentScale = ContentScale.Fit,
                )
            }
        }
    }
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
    }.onFailure(logException).onFailure { context.bringMarketPage() }
}

context(AnimatedContentScope, SharedTransitionScope)
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun AboutScreenContent(navigateToLicenses: () -> Unit, bottomPadding: Dp) {
    Column {
        // Licenses
        Text(
            stringResource(R.string.licenses, MaterialTheme.typography.bodyLarge),
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 20.dp),
        )
        AboutScreenButton(
            icon = Icons.Default.Folder,
            action = { navigateToLicenses() },
            title = R.string.about_license_title,
            summary = R.string.about_license_sum,
            modifier = Modifier.sharedBounds(
                rememberSharedContentState(key = "licenses"),
                animatedVisibilityScope = this@AnimatedContentScope,
            ),
        )

        // Help
        val language by language.collectAsState()
        if (language.isUserAbleToReadPersian) {
            Row(modifier = Modifier.padding(top = 16.dp, start = 12.dp)) {
                Icon(
                    modifier = Modifier.padding(start = 8.dp, end = 4.dp),
                    imageVector = Icons.AutoMirrored.Default.Help,
                    contentDescription = stringResource(R.string.help),
                )
                Column {
                    Text(
                        stringResource(R.string.help), style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                HelpItems()
            }
        }

        // Bug report
        Text(
            stringResource(R.string.about_support_developers),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 24.dp, end = 12.dp, top = 12.dp),
        )
        AboutScreenButton(
            icon = Icons.Default.BugReport,
            action = ::launchReportIntent,
            title = R.string.about_report_bug,
            summary = R.string.about_report_bug_sum
        )
        run {
            var showDialog by rememberSaveable { mutableStateOf(false) }
            AboutScreenButton(
                icon = Icons.Default.Email,
                action = { showDialog = true },
                title = R.string.about_send_mail,
                summary = R.string.about_email_sum
            )
            if (showDialog) EmailDialog { showDialog = false }
        }

        // Developers
        Text(
            stringResource(R.string.about_developers),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 24.dp, end = 12.dp, top = 12.dp),
        )
        DevelopersChips()

        Spacer(Modifier.height(bottomPadding))
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
    Column {
        sections.forEach { (title, body) ->
            var isExpanded by rememberSaveable { mutableStateOf(false) }
            Column(
                modifier = Modifier
                    .clickable { isExpanded = !isExpanded }
                    .padding(all = 4.dp)
                    .fillMaxWidth()
                    .animateContentSize(),
            ) {
                FlowRow(verticalArrangement = Arrangement.Center) {
                    Spacer(modifier = Modifier.width(16.dp))
                    ExpandArrow(
                        isExpanded = isExpanded,
                        tint = MaterialTheme.colorScheme.primary,
                        contentDescription = stringResource(R.string.more),
                        isLineStart = true,
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(title, modifier = Modifier.align(alignment = Alignment.CenterVertically))
                }
                AnimatedVisibility(visible = isExpanded) {
                    SelectionContainer {
                        Text(body, Modifier.padding(horizontal = 16.dp))
                    }
                }
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
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .clickable { action(context) }
            .padding(start = 20.dp, end = 16.dp, top = 4.dp, bottom = 4.dp)
            .then(modifier),
    ) {
        Row {
            Icon(
                modifier = Modifier.padding(end = 4.dp),
                imageVector = icon,
                contentDescription = stringResource(title),
            )
            Column {
                Text(stringResource(title), style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    stringResource(summary),
                    style = MaterialTheme.typography.bodySmall,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DevelopersChips() {
    val context = LocalContext.current
    val developers = remember {
        listOf(
            R.string.about_developers_list to Icons.Default.Android,
            R.string.about_designers_list to Icons.Default.Palette,
            R.string.about_translators_list to Icons.Default.Translate,
            R.string.about_contributors_list to Icons.Default.Android,
        ).flatMap { (listId: Int, icon: ImageVector) ->
            context.getString(listId).trim().split("\n").map {
                val (username, displayName) = it.split(": ")
                Triple(username, displayName, icon)
            }
        }.shuffled()
    }
    CompositionLocalProvider(
        LocalLayoutDirection provides LayoutDirection.Ltr,
        LocalMinimumInteractiveComponentEnforcement provides false,
    ) {
        @OptIn(ExperimentalLayoutApi::class)
        FlowRow(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        ) {
            developers.forEach { (username, displayName, icon) ->
                ElevatedFilterChip(
                    modifier = Modifier.padding(all = 4.dp),
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
                            icon,
                            contentDescription = displayName,
                            Modifier.size(AssistChipDefaults.IconSize)
                        )
                    }
                )
            }
        }
    }
}
