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
import androidx.core.net.toUri
import androidx.core.view.setMargins
import androidx.core.view.setPadding
import com.byagowi.persiancalendar.*
import com.byagowi.persiancalendar.databinding.DialogEmailBinding
import com.byagowi.persiancalendar.databinding.FragmentAboutBinding
import com.byagowi.persiancalendar.di.MainActivityDependency
import com.byagowi.persiancalendar.utils.formatNumber
import com.byagowi.persiancalendar.utils.getMaxSupportedYear
import com.byagowi.persiancalendar.utils.language
import com.byagowi.persiancalendar.utils.readRawResource
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class AboutFragment : DaggerFragment() {

    @Inject
    lateinit var mainActivityDependency: MainActivityDependency

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)

        val binding = FragmentAboutBinding.inflate(inflater, container, false)

        val activity = mainActivityDependency.mainActivity
        activity.setTitleAndSubtitle(getString(R.string.about), "")

        // version
        val version = programVersion(activity).split("-")
            .mapIndexed { i, x -> if (i == 0) formatNumber(x) else x }
        binding.version.text =
            String.format(getString(R.string.version), version.joinToString("\n"))

        // licenses
        binding.licenses.setOnClickListener {
            AlertDialog.Builder(
                activity,
                com.google.android.material.R.style.Widget_MaterialComponents_MaterialCalendar_Fullscreen
            )
                .setTitle(resources.getString(R.string.about_license_title))
                .setView(ScrollView(activity).apply {
                    addView(TextView(activity).apply {
                        text = readRawResource(activity, R.raw.credits)
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
        binding.aboutTitle.text = String.format(
            getString(R.string.about_help_subtitle),
            formatNumber(getMaxSupportedYear() - 1),
            formatNumber(getMaxSupportedYear())
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
            AlertDialog.Builder(mainActivityDependency.mainActivity)
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
                            Intent.EXTRA_TEXT,
                            String.format(
                                emailBinding.inputText.text?.toString() + "\n\n\n\n\n\n\n===Device Information===\nManufacturer: %s\nModel: %s\nAndroid Version: %s\nApp Version Code: %s",
                                Build.MANUFACTURER, Build.MODEL, Build.VERSION.RELEASE, version[0]
                            )
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

        val developerIcon = AppCompatResources.getDrawable(activity, R.drawable.ic_developer)
        val translatorIcon = AppCompatResources.getDrawable(activity, R.drawable.ic_translator)
        val designerIcon = AppCompatResources.getDrawable(activity, R.drawable.ic_designer)
        val color = TypedValue()
        activity.theme.resolveAttribute(R.attr.colorDrawerIcon, color, true)

        val layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(8)

        val chipClick = View.OnClickListener { view ->
            try {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        ("https://github.com/" + (view as Chip).text.toString()
                            .split("@")[1].split(")")[0]).toUri()
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        getString(R.string.about_developers_list)
            .trim().split("\n").shuffled().map {
                Chip(activity).apply {
                    this.layoutParams = layoutParams
                    setOnClickListener(chipClick)
                    text = it
                    chipIcon = developerIcon
                    setChipIconTintResource(color.resourceId)
                }
            }.forEach(binding.developers::addView)

        getString(R.string.about_designers_list)
            .trim().split("\n").shuffled().map {
                Chip(activity).apply {
                    this.layoutParams = layoutParams
                    text = it
                    chipIcon = designerIcon
                    setChipIconTintResource(color.resourceId)
                }
            }.forEach(binding.developers::addView)

        getString(R.string.about_translators_list)
            .trim().split("\n").shuffled().map {
                Chip(activity).apply {
                    this.layoutParams = layoutParams
                    setOnClickListener(chipClick)
                    text = it
                    chipIcon = translatorIcon
                    setChipIconTintResource(color.resourceId)
                }
            }.forEach(binding.developers::addView)

        getString(R.string.about_contributors_list)
            .trim().split("\n").shuffled().map {
                Chip(activity).apply {
                    this.layoutParams = layoutParams
                    setOnClickListener(chipClick)
                    text = it
                    chipIcon = developerIcon
                    setChipIconTintResource(color.resourceId)
                }
            }.forEach(binding.developers::addView)

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
        inflater.inflate(R.menu.about_menu_buttons, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.deviceInfo -> mainActivityDependency.mainActivity.navigateTo(R.id.deviceInfo)
            else -> {
            }
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
