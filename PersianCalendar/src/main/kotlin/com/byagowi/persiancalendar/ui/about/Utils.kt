package com.byagowi.persiancalendar.ui.about

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import com.byagowi.persiancalendar.BuildConfig
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.generated.credits
import com.byagowi.persiancalendar.utils.logException

fun getCreditsSections() = credits
    .split(Regex("^-{4}$", RegexOption.MULTILINE))
    .map {
        val lines = it.trim().lines()
        val parts = lines.first().split(" - ")
        Triple(parts[0], parts.getOrNull(1), lines.drop(1).joinToString("\n").trim())
    }

fun appStandbyStatus(context: Context): String? {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return null
    return runCatching {
        val code = context.getSystemService<UsageStatsManager>()?.appStandbyBucket
        (mapOf(
            5 to "EXEMPTED",
            UsageStatsManager.STANDBY_BUCKET_ACTIVE to "ACTIVE",
            UsageStatsManager.STANDBY_BUCKET_WORKING_SET to "WORKING_SET",
            UsageStatsManager.STANDBY_BUCKET_FREQUENT to "FREQUENT",
            UsageStatsManager.STANDBY_BUCKET_RARE to "RARE",
            45 to "RESTRICTED",
            50 to "NEVER"
        )[code] ?: code.toString())
    }.getOrNull()
}

fun launchEmailIntent(context: Context, message: String) {
    val bucket =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
            "\nStandby Bucket: ${appStandbyStatus(context)}"
        else ""
    val email = "persian-calendar-admin@googlegroups.com"
    val subject = context.getString(R.string.app_name)
    val body = """$message




===Device Information===
Manufacturer: ${Build.MANUFACTURER}
Model: ${Build.MODEL}
Android Version: ${Build.VERSION.RELEASE}
App Version Code: ${context.packageName} ${BuildConfig.VERSION_CODE} $bucket"""

    // https://stackoverflow.com/a/62597382
    val selectorIntent = Intent(Intent.ACTION_SENDTO).apply {
        data = "mailto:$email?subject=${Uri.encode(subject)}&body=${Uri.encode(body)}".toUri()
    }
    val emailIntent = Intent(Intent.ACTION_SEND).apply {
        putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, body)
        selector = selectorIntent
    }
    runCatching {
        context.startActivity(
            Intent.createChooser(emailIntent, context.getString(R.string.about_sendMail))
        )
    }.onFailure(logException).onFailure {
        Toast.makeText(context, R.string.about_noClient, Toast.LENGTH_SHORT).show()
    }
}
