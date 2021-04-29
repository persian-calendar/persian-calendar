package com.byagowi.persiancalendar.ui.about

import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.util.Linkify
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.byagowi.persiancalendar.*
import com.byagowi.persiancalendar.databinding.DialogEmailBinding
import com.byagowi.persiancalendar.databinding.FragmentAboutBinding
import com.byagowi.persiancalendar.utils.*
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar

class AboutFragment : Fragment() {

    private val appVersionList
        get() = BuildConfig.VERSION_NAME.split("-")
            .mapIndexed { i, x -> if (i == 0) formatNumber(x) else x }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val binding = FragmentAboutBinding.inflate(inflater, container, false)

        binding.appBar.toolbar.let {
            it.setTitle(R.string.about)
            it.setupUpNavigation()
            it.inflateMenu(R.menu.about_menu_buttons)
            it.setOnMenuItemClickListener { clickedMenuItem ->
                when (clickedMenuItem.itemId) {
                    R.id.deviceInformation ->
                        findNavController().navigate(AboutFragmentDirections.actionAboutToDeviceinfo())
                    R.id.share -> shareApplication()
                }
                true
            }
        }

        // version
        binding.version.text = getString(R.string.version).format(appVersionList.joinToString("\n"))

        // licenses
        binding.licenses.setOnClickListener { showLicenceDialog() }

        // help
        binding.aboutTitle.text = getString(R.string.about_help_subtitle).format(
            formatNumber(supportedYearOfIranCalendar - 1),
            formatNumber(supportedYearOfIranCalendar)
        )
        binding.helpCard.visibility = when (language) {
            LANG_FA, LANG_GLK, LANG_AZB, LANG_FA_AF, LANG_EN_IR -> View.VISIBLE
            else -> View.GONE
        }

        Linkify.addLinks(binding.helpSummary, Linkify.WEB_URLS or Linkify.EMAIL_ADDRESSES)

        // report bug
        binding.reportBug.setOnClickListener { launchReportIntent() }

        binding.email.setOnClickListener { showEmailDialog() }

        setupContributorsList(binding)

        return binding.root
    }

    private fun setupContributorsList(binding: FragmentAboutBinding) {
        val context = binding.root.context

        val chipsIconTintId = TypedValue().apply {
            context.theme.resolveAttribute(R.attr.colorDrawerIcon, this, true)
        }.resourceId

        val chipClick = View.OnClickListener {
            (it.tag as? String)?.also { user ->
                if (user == "ImanSoltanian") return@also // The only person without GitHub account
                runCatching {
                    CustomTabsIntent.Builder().build().launchUrl(
                        context, "https://github.com/$user".toUri()
                    )
                }.onFailure(logException)
            }
        }

        listOf(
            R.string.about_developers_list to R.drawable.ic_developer,
            R.string.about_designers_list to R.drawable.ic_designer,
            R.string.about_translators_list to R.drawable.ic_translator,
            R.string.about_contributors_list to R.drawable.ic_developer
        ).forEach {
            val icon = AppCompatResources.getDrawable(context, it.second)
            getString(it.first).trim().split("\n").shuffled().forEach {
                binding.developers.addView(Chip(context).also { chip ->
                    chip.setOnClickListener(chipClick)
                    val parts = it.split(": ")
                    chip.tag = parts[0]
                    chip.text = parts[1]
                    chip.chipIcon = icon
                    chip.setChipIconTintResource(chipsIconTintId)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        chip.elevation = resources.getDimension(R.dimen.chip_elevation)
                    }
                })
            }
        }
    }

    private fun showLicenceDialog() {
        AlertDialog.Builder(
            context ?: return,
            com.google.android.material.R.style.Widget_MaterialComponents_MaterialCalendar_Fullscreen
        )
            .setTitle(resources.getString(R.string.about_license_title))
            .setView(ScrollView(context).apply {
                addView(TextView(context).apply {
                    text = resources.openRawResource(R.raw.credits).use { String(it.readBytes()) }
                    setPadding(20)
                    typeface = Typeface.MONOSPACE
                    Linkify.addLinks(this, Linkify.WEB_URLS or Linkify.EMAIL_ADDRESSES)
                    setTextIsSelectable(true)
                })
            })
            .setCancelable(true)
            .setNegativeButton(R.string.about_license_dialog_close, null)
            .show()
    }

    private fun launchReportIntent() {
        runCatching {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    "https://github.com/persian-calendar/DroidPersianCalendar/issues/new".toUri()
                )
            )
        }.onFailure(logException)
    }

    private fun showEmailDialog() {
        val activity = activity ?: return
        val emailBinding = DialogEmailBinding.inflate(activity.layoutInflater)
        AlertDialog.Builder(activity)
            .setView(emailBinding.root)
            .setTitle(R.string.about_email_sum)
            .setPositiveButton(R.string.continue_button) { _, _ ->
                launchEmailIntent(emailBinding.inputText.text?.toString())
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun launchEmailIntent(defaultMessage: String? = null) {
        val emailIntent = Intent(
            Intent.ACTION_SENDTO,
            Uri.fromParts("mailto", "persian-calendar-admin@googlegroups.com", null)
        ).apply {
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
            putExtra(
                Intent.EXTRA_TEXT,
                """$defaultMessage
    
    
    
    
===Device Information===
Manufacturer: ${Build.MANUFACTURER}
Model: ${Build.MODEL}
Android Version: ${Build.VERSION.RELEASE}
App Version Code: ${appVersionList[0]}"""
            )
        }
        runCatching {
            startActivity(
                Intent.createChooser(
                    emailIntent,
                    getString(R.string.about_sendMail)
                )
            )
        }
            .onFailure(logException)
            .getOrElse {
                Snackbar.make(view ?: return, R.string.about_noClient, Snackbar.LENGTH_SHORT)
                    .show()
            }
    }

    private fun shareApplication() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1)
            runCatching {
                startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
                    putExtra(
                        Intent.EXTRA_TEXT,
                        "${getString(R.string.app_name)}\nhttps://github.com/persian-calendar/DroidPersianCalendar"
                    )
                }, getString(R.string.share)))
            }
                .onFailure(logException)
                .getOrElse { bringMarketPage(activity ?: return) }
    }
}
