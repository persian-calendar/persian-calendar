package com.byagowi.persiancalendar.ui.about

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.os.Build
import android.widget.Toast
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

fun launchEmailIntent(context: Context, message: String) {
    val email = "persian-calendar-admin@googlegroups.com"
    val subject = context.getString(R.string.app_name)
    val body = """$message




===Device Information===
Manufacturer: ${Build.MANUFACTURER}
Model: ${Build.MODEL}
Android Version: ${Build.VERSION.RELEASE}
App Version Code: ${context.packageName} ${BuildConfig.VERSION_CODE}"""

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
