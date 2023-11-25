package com.byagowi.persiancalendar.ui.about

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.util.Linkify
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.text.scale
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.byagowi.persiancalendar.BuildConfig
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.AboutScreenBinding
import com.byagowi.persiancalendar.generated.faq
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.ui.utils.bringMarketPage
import com.byagowi.persiancalendar.ui.utils.getAnimatedDrawable
import com.byagowi.persiancalendar.ui.utils.getCompatDrawable
import com.byagowi.persiancalendar.ui.utils.hideToolbarBottomShadow
import com.byagowi.persiancalendar.ui.utils.isRtl
import com.byagowi.persiancalendar.ui.utils.navigateSafe
import com.byagowi.persiancalendar.ui.utils.onClick
import com.byagowi.persiancalendar.ui.utils.setupMenuNavigation
import com.byagowi.persiancalendar.utils.formatNumber
import com.byagowi.persiancalendar.utils.logException
import com.byagowi.persiancalendar.utils.supportedYearOfIranCalendar
import com.google.accompanist.themeadapter.material3.Mdc3Theme

class AboutScreen : Fragment(R.layout.about_screen) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = AboutScreenBinding.bind(view)
        binding.appBar.toolbar.setTitle(R.string.about)
        binding.appBar.toolbar.setupMenuNavigation()
        binding.appBar.toolbar.menu.add(R.string.share).also {
            it.icon = binding.appBar.toolbar.context.getCompatDrawable(R.drawable.ic_baseline_share)
            it.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            it.onClick { shareApplication() }
        }
        binding.appBar.toolbar.menu.add(R.string.device_information).also {
            it.icon =
                binding.appBar.toolbar.context.getCompatDrawable(R.drawable.ic_device_information)
            it.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            it.onClick {
                findNavController().navigateSafe(AboutScreenDirections.actionAboutToDeviceInformation())
            }
        }
        binding.appBar.root.hideToolbarBottomShadow()

        // app
        val version = buildSpannedString {
            scale(1.5f) { bold { appendLine(getString(R.string.app_name)) } }
            scale(.8f) {
                val version =
                    // Don't formatNumber it if is multi-parted
                    if ("-" in BuildConfig.VERSION_NAME) BuildConfig.VERSION_NAME
                    else formatNumber(BuildConfig.VERSION_NAME)
                append(getString(R.string.version, version))
            }
            if (language.isUserAbleToReadPersian) {
                appendLine()
                scale(.8f) {
                    append(
                        getString(
                            R.string.about_help_subtitle,
                            formatNumber(supportedYearOfIranCalendar - 1),
                            formatNumber(supportedYearOfIranCalendar)
                        )
                    )
                }
            }
        }
        binding.aboutHeader.text = version
        binding.accessibleVersion.contentDescription = version
        run {
            val animation =
                context?.getAnimatedDrawable(R.drawable.splash_icon_animation) ?: return@run
            binding.icon.setImageDrawable(animation)
            animation.start()
            val clickHandlerDialog = createEasterEggClickHandler(::showPeriodicTableDialog)
            val clickHandlerIcon = createIconRandomEffects(binding.icon)
            binding.headerPlaceHolder.setOnClickListener {
                animation.stop()
                animation.start()
                clickHandlerDialog(activity)
                clickHandlerIcon()
            }
        }

        fun TextView.putLineStartIcon(@DrawableRes icon: Int) {
            if (resources.isRtl) setCompoundDrawablesWithIntrinsicBounds(0, 0, icon, 0)
            else setCompoundDrawablesWithIntrinsicBounds(icon, 0, 0, 0)
        }

        // licenses
        binding.licenses.setOnClickListener {
            findNavController().navigateSafe(AboutScreenDirections.actionAboutToLicenses())
        }
        binding.licensesTitle.putLineStartIcon(R.drawable.ic_licences)

        // help
        binding.helpCard.isVisible = language.isUserAbleToReadPersian
        binding.helpTitle.putLineStartIcon(R.drawable.ic_help)
        binding.helpSectionsRecyclerView.apply {
            val sections = faq
                .split(Regex("^={4}$", RegexOption.MULTILINE))
                .map { it.trim().lines() }
                .map { lines ->
                    val title = lines.first()
                    val body = SpannableString(lines.drop(1).joinToString("\n").trim())
                    Linkify.addLinks(body, Linkify.WEB_URLS or Linkify.EMAIL_ADDRESSES)
                    title to body
                }
            adapter = ExpandableItemsAdapter(sections)
            layoutManager = LinearLayoutManager(context)
        }

        // report bug
        binding.reportBug.setOnClickListener { launchReportIntent() }
        binding.reportBugTitle.putLineStartIcon(R.drawable.ic_bug)

        binding.email.setOnClickListener click@{ showEmailDialog(activity ?: return@click) }
        binding.emailTitle.putLineStartIcon(R.drawable.ic_email)

        binding.compose.setContent {
            Mdc3Theme {
                // Developers
                Column {
                    Text(
                        stringResource(R.string.about_developers),
                        modifier = Modifier.padding(all = 12.dp)
                    )
                    DevelopersChips()
                }
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.contentRoot.updatePadding(bottom = insets.bottom)
            binding.appBar.toolbar.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = insets.top
            }
            WindowInsetsCompat.CONSUMED
        }
    }

    private fun launchReportIntent() {
        runCatching {
            val uri = "https://github.com/persian-calendar/persian-calendar/issues/new".toUri()
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        }.onFailure(logException)
    }

    private fun shareApplication() {
        runCatching {
            startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
                val textToShare = """${getString(R.string.app_name)}
https://github.com/persian-calendar/persian-calendar"""
                putExtra(Intent.EXTRA_TEXT, textToShare)
            }, getString(R.string.share)))
        }.onFailure(logException).onFailure { (activity ?: return).bringMarketPage() }
    }
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
                            CustomTabsIntent.Builder().build()
                                .launchUrl(context, uri)
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
