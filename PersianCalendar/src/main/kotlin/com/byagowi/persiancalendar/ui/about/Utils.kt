package com.byagowi.persiancalendar.ui.about

import android.content.res.Resources
import com.byagowi.persiancalendar.R

fun Resources.getCreditsSections() = openRawResource(R.raw.credits).use { String(it.readBytes()) }
    .split(Regex("^-{4}$", RegexOption.MULTILINE))
    .map {
        val lines = it.trim().lines()
        val parts = lines.first().split(" - ")
        Triple(parts[0], parts.getOrNull(1), lines.drop(1).joinToString("\n").trim())
    }
