package com.byagowi.persiancalendar.ui.about

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.annotation.VisibleForTesting
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PermDeviceInformation
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.byagowi.persiancalendar.BuildConfig
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.generated.faq
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.ui.MainActivity
import com.byagowi.persiancalendar.ui.utils.MaterialCornerExtraLargeTop
import com.byagowi.persiancalendar.ui.utils.MaterialIconDimension
import com.byagowi.persiancalendar.ui.utils.bringMarketPage
import com.byagowi.persiancalendar.ui.utils.resolveColor
import com.byagowi.persiancalendar.utils.formatNumber
import com.byagowi.persiancalendar.utils.logException
import com.byagowi.persiancalendar.utils.supportedYearOfIranCalendar
import com.google.accompanist.themeadapter.material3.Mdc3Theme


class AboutFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val root = ComposeView(inflater.context)
        root.setContent {
            Mdc3Theme {
                val navController = rememberNavController()
                val aboutRoute = "about"
                val licensesRoute = "licenses"
                val deviceInformationRoute = "deviceInformation"
                NavHost(navController = navController, startDestination = aboutRoute) {
                    composable(aboutRoute) {
                        AboutScreen(
                            navigateToLicenses = { navController.navigate(licensesRoute) },
                            navigateToDeviceInformation = {
                                navController.navigate(deviceInformationRoute)
                            },
                        )
                    }
                    composable(licensesRoute) {
                        LicensesScreen { navController.popBackStack() }
                    }
                    composable(deviceInformationRoute) {
                        DeviceInformationScreen { navController.popBackStack() }
                    }
                }
            }
        }
        return root.rootView
    }
}

@Preview
@Composable
private fun Preview() = AboutScreen({}, {})

@Composable
@VisibleForTesting
fun AboutScreen(
    navigateToDeviceInformation: () -> Unit,
    navigateToLicenses: () -> Unit,
) {
    Column {
        val context = LocalContext.current
        // TODO: Ideally this should be onPrimary
        val colorOnAppBar = Color(context.resolveColor(R.attr.colorOnAppBar))

        @OptIn(ExperimentalMaterial3Api::class)
        TopAppBar(
            title = { Text(stringResource(R.string.about)) },
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
                IconButton(onClick = { shareApplication(context) }) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = stringResource(R.string.share)
                    )
                }
                IconButton(onClick = navigateToDeviceInformation) {
                    Icon(
                        imageVector = Icons.Default.PermDeviceInformation,
                        contentDescription = stringResource(R.string.device_information)
                    )
                }
            },
        )

        Box(modifier = Modifier.clip(MaterialCornerExtraLargeTop())) {
            var logoAnimationAtEnd by remember { mutableStateOf(false) }
            var logoEffect by remember { mutableStateOf<RenderEffect?>(null) }
            LaunchedEffect(key1 = null) { logoAnimationAtEnd = !logoAnimationAtEnd }

            val headerSize = 250.dp

            val aboutTitle = stringResource(R.string.app_name)
            val aboutSubtitle = remember {
                buildString {
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

            @OptIn(ExperimentalAnimationGraphicsApi::class) Row(
                Modifier
                    .height(headerSize)
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
                            aboutTitle,
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = colorOnAppBar,
                        )
                        Text(
                            aboutSubtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorOnAppBar,
                        )
                    }
                }
                val image =
                    AnimatedImageVector.animatedVectorResource(R.drawable.splash_icon_animation)
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        Image(modifier = Modifier
                            .graphicsLayer { renderEffect = logoEffect }
                            .fillMaxSize(),
                            painter = rememberAnimatedVectorPainter(image, logoAnimationAtEnd),
                            contentDescription = stringResource(R.string.app_name),
                            contentScale = ContentScale.Fit)
                    }
                }
            }

            val effectsGenerator = remember {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) createIconRandomEffects()
                else null
            }

            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                val clickHandlerDialog =
                    remember { createEasterEggClickHandler(::showPeriodicTableDialog) }
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .height(headerSize)
                    .clickable(onClickLabel = aboutTitle + "\n" + aboutSubtitle) {
                        logoAnimationAtEnd = !logoAnimationAtEnd
                        clickHandlerDialog(context as? FragmentActivity) // TODO: Ugly cast
                        logoEffect = effectsGenerator
                            ?.invoke()
                            ?.asComposeRenderEffect()
                    })
                Surface(shape = MaterialCornerExtraLargeTop()) {
                    Box(modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp)) {
                        AboutScreenContent(navigateToLicenses)
                    }
                }
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
    }.onFailure(logException).onFailure { (context as? FragmentActivity)?.bringMarketPage() }
}

@Composable
private fun AboutScreenContent(navigateToLicenses: () -> Unit) {
    Column {
        // Licenses
        Text(
            stringResource(R.string.licenses, MaterialTheme.typography.bodyLarge),
            modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 4.dp),
        )
        AboutScreenButton(
            icon = Icons.Default.Folder,
            action = { navigateToLicenses() },
            title = R.string.about_license_title,
            summary = R.string.about_license_sum
        )

        // Help
        if (language.isUserAbleToReadPersian) {
            Row(modifier = Modifier.padding(top = 16.dp)) {
                Icon(
                    modifier = Modifier.padding(start = 8.dp, end = 4.dp),
                    imageVector = Icons.AutoMirrored.Default.Help,
                    contentDescription = stringResource(R.string.help)
                )
                Column {
                    Text(
                        stringResource(R.string.help),
                        style = MaterialTheme.typography.bodyLarge
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
            modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 12.dp),
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
                title = R.string.about_sendMail,
                summary = R.string.about_email_sum
            )
            if (showDialog) EmailDialog { showDialog = false }
        }

        // Developers
        Text(
            stringResource(R.string.about_developers),
            style = MaterialTheme.typography.bodyLarge,
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
    Column {
        sections.forEach { (title, body) ->
            var isExpanded by rememberSaveable { mutableStateOf(false) }
            val angle = animateFloatAsState(if (isExpanded) 0f else 90f, label = "angle").value
            Column(modifier = Modifier
                .clickable { isExpanded = !isExpanded }
                .padding(all = 4.dp)
                .fillMaxWidth()
                .animateContentSize()) {
                FlowRow(verticalArrangement = Arrangement.Center) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Image(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = stringResource(R.string.more),
                        modifier = Modifier
                            .rotate(angle)
                            .size(MaterialIconDimension.dp),
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(title, modifier = Modifier.align(alignment = Alignment.CenterVertically))
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
            .clickable { action(context) }
            .padding(start = 8.dp, end = 12.dp, top = 4.dp, bottom = 4.dp)) {
        Row {
            Icon(
                modifier = Modifier.padding(end = 4.dp),
                imageVector = icon,
                contentDescription = stringResource(title)
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
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        @OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
        FlowRow(Modifier.fillMaxWidth()) {
            developers.forEach { (username, displayName, icon) ->
                ElevatedFilterChip(
                    modifier = Modifier.padding(all = 2.dp),
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
