package com.byagowi.persiancalendar.view.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.FragmentAboutBinding
import com.byagowi.persiancalendar.util.UIUtils
import com.byagowi.persiancalendar.util.Utils

/**
 * About Calendar Activity
 *
 * @author ebraminio
 */
class AboutFragment : Fragment() {

  @SuppressLint("SetTextI18n")
  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    val localActivity = activity ?: return null
    val binding = DataBindingUtil.inflate<FragmentAboutBinding>(inflater, R.layout.fragment_about,
        container, false)

    UIUtils.setActivityTitleAndSubtitle(localActivity, getString(R.string.about), "")

    // version
    val version = programVersion()
    binding.version.text = getString(R.string.version) + " " + Utils.formatNumber(version.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0])

    // licenses
    binding.licenses.setOnClickListener {
      val wv = WebView(localActivity)
      val settings = wv.settings
      settings.defaultTextEncodingName = "utf-8"
      wv.loadUrl("file:///android_res/raw/credits.txt")
      val builder = AlertDialog.Builder(localActivity)
      builder.setTitle(resources.getString(R.string.about_license_title))
      builder.setView(wv)
      builder.setCancelable(true)
      builder.setNegativeButton(R.string.about_license_dialog_close) { _, _ -> }
      builder.show()
    }

    // help
    binding.aboutTitle.text = String.format(getString(R.string.about_help_subtitle),
        Utils.formatNumber(Utils.maxSupportedYear))
    binding.helpSum.setText(R.string.about_help_sum)

    // report bug
    binding.reportBug.setOnClickListener {
      try {
        startActivity(Intent(Intent.ACTION_VIEW,
            Uri.parse("https://github.com/ebraminio/DroidPersianCalendar/issues/new")))
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }

    binding.email.setOnClickListener {
      val emailIntent = Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", getString(R.string.about_mailto), null))
      emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
      try {
        emailIntent.putExtra(Intent.EXTRA_TEXT, "\n\n\n\n\n\n\n" +
            "===Device Information===\nManufacturer: " + Build.MANUFACTURER + "\nModel: " + Build.MODEL + "\nAndroid Version: " + Build.VERSION.RELEASE + "\nApp Version Code: " + version.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0])
        startActivity(Intent.createChooser(emailIntent, getString(R.string.about_sendMail)))
      } catch (ex: android.content.ActivityNotFoundException) {
        Toast.makeText(localActivity, getString(R.string.about_noClient), Toast.LENGTH_SHORT).show()
      }
    }

    return binding.root
  }

  private fun programVersion(): String {
    return try {
      context?.packageManager?.getPackageInfo(context?.packageName, 0)?.versionName ?: ""
    } catch (e: PackageManager.NameNotFoundException) {
      Log.e(AboutFragment::class.java.name, "Name not found on PersianCalendarUtils.programVersion")
      ""
    }
  }
}
