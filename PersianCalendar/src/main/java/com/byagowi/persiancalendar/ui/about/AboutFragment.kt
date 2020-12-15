package com.byagowi.persiancalendar.ui.about

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.util.Linkify
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import androidx.core.view.setMargins
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import com.byagowi.persiancalendar.*
import com.byagowi.persiancalendar.databinding.DialogEmailBinding
import com.byagowi.persiancalendar.databinding.FragmentAboutBinding
import com.byagowi.persiancalendar.ui.MainActivity
import com.byagowi.persiancalendar.utils.*
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar

class AboutFragment : Fragment() {

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val mainActivity = activity as MainActivity
        mainActivity.setTitleAndSubtitle(getString(R.string.about), "")
        setHasOptionsMenu(true)

        val binding = FragmentAboutBinding.inflate(inflater, container, false)

        // version
        val version = programVersion(mainActivity).split("-")
                .mapIndexed { i, x -> if (i == 0) formatNumber(x) else x }
        binding.version.text =
                getString(R.string.version).format(version.joinToString("\n"))

        // licenses
        binding.licenses.setOnClickListener {
            AlertDialog.Builder(
                    mainActivity,
                    com.google.android.material.R.style.Widget_MaterialComponents_MaterialCalendar_Fullscreen
            )
                    .setTitle(resources.getString(R.string.about_license_title))
                    .setView(ScrollView(mainActivity).apply {
                        addView(TextView(mainActivity).apply {
                            text = readRawResource(mainActivity, R.raw.credits)
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
        binding.reportBug.setOnClickListener {
            try {
                startActivity(
                        Intent(
                                Intent.ACTION_VIEW,
                                "https://github.com/persian-calendar/DroidPersianCalendar/issues/new".toUri()
                        )
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        binding.email.setOnClickListener {
            val emailBinding = DialogEmailBinding.inflate(inflater, container, false)
            AlertDialog.Builder(mainActivity)
                    .setView(emailBinding.root)
                    .setTitle(R.string.about_email_sum)
                    .setPositiveButton(R.string.continue_button) { _, _ ->
                        val emailIntent = Intent(
                                Intent.ACTION_SENDTO,
                                Uri.fromParts("mailto", "persian-calendar-admin@googlegroups.com", null)
                        )
                        emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
                        try {
                            emailIntent.putExtra(
                                    Intent.EXTRA_TEXT, """${emailBinding.inputText.text?.toString()}





===Device Information===
Manufacturer: ${Build.MANUFACTURER}
Model: ${Build.MODEL}
Android Version: ${Build.VERSION.RELEASE}
App Version Code: ${version[0]}"""
                            )
                            startActivity(
                                    Intent.createChooser(
                                            emailIntent,
                                            getString(R.string.about_sendMail)
                                    )
                            )
                        } catch (e: ActivityNotFoundException) {
                            e.printStackTrace()
                            Snackbar.make(binding.root, R.string.about_noClient, Snackbar.LENGTH_SHORT)
                                    .show()
                        }
                    }
                    .setNegativeButton(R.string.cancel, null).show()
        }

        val developerIcon = AppCompatResources.getDrawable(mainActivity, R.drawable.ic_developer)
        val translatorIcon = AppCompatResources.getDrawable(mainActivity, R.drawable.ic_translator)
        val designerIcon = AppCompatResources.getDrawable(mainActivity, R.drawable.ic_designer)
        val chipsIconsColor = TypedValue().apply {
            mainActivity.theme.resolveAttribute(R.attr.colorDrawerIcon, this, true)
        }.resourceId

        val chipsLayoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply { setMargins(8) }

        val chipClick = View.OnClickListener {
            (it.tag as? String?)?.run {
                try {
                    CustomTabsIntent.Builder().build().launchUrl(
                            mainActivity, "https://github.com/$this".toUri()
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        getString(R.string.about_developers_list)
                .trim().split("\n").shuffled().map {
                    Chip(mainActivity).apply {
                        layoutParams = chipsLayoutParams
                        setOnClickListener(chipClick)
                        val parts = it.split(": ")
                        tag = parts[0]
                        text = parts[1]
                        chipIcon = developerIcon
                        setChipIconTintResource(chipsIconsColor)
                    }
                }.forEach(binding.developers::addView)

        getString(R.string.about_designers_list)
                .trim().split("\n").shuffled().map {
                    Chip(mainActivity).apply {
                        layoutParams = chipsLayoutParams
                        // setOnClickListener(chipClick)
                        val parts = it.split(": ")
                        if (parts.size == 2) tag = parts[0]
                        text = parts.last()
                        chipIcon = designerIcon
                        setChipIconTintResource(chipsIconsColor)
                    }
                }.forEach(binding.developers::addView)

        getString(R.string.about_translators_list)
                .trim().split("\n").shuffled().map {
                    Chip(mainActivity).apply {
                        layoutParams = chipsLayoutParams
                        setOnClickListener(chipClick)
                        val parts = it.split(": ")
                        tag = parts[0]
                        text = parts[1]
                        chipIcon = translatorIcon
                        setChipIconTintResource(chipsIconsColor)
                    }
                }.forEach(binding.developers::addView)

        getString(R.string.about_contributors_list)
                .trim().split("\n").shuffled().map {
                    Chip(mainActivity).apply {
                        layoutParams = chipsLayoutParams
                        setOnClickListener(chipClick)
                        val parts = it.split(": ")
                        tag = parts[0]
                        text = parts[1]
                        chipIcon = developerIcon
                        setChipIconTintResource(chipsIconsColor)
                    }
                }.forEach(binding.developers::addView)

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
        inflater.inflate(R.menu.about_menu_buttons, menu)
    }

    private fun shareApplication() {
        val activity = activity ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            try {
                startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
                    putExtra(
                            Intent.EXTRA_TEXT,
                            "${getString(R.string.app_name)}\nhttps://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}"
                    )
                }, getString(R.string.share)))
            } catch (e: Exception) {
                e.printStackTrace()
                bringMarketPage(activity)
            }
        } else {
            bringMarketPage(activity)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.deviceInformation -> (activity as MainActivity).navigateTo(R.id.deviceInformation)
            R.id.share -> shareApplication()
        }
        return true
    }

    private fun programVersion(context: Context): String = try {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName
    } catch (e: PackageManager.NameNotFoundException) {
        Log.e(AboutFragment::class.java.name, "Name not found on PersianUtils.programVersion", e)
        ""
    }
}
