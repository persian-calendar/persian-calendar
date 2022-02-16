package com.byagowi.persiancalendar.ui.preferences

import android.app.Activity
import android.os.Build
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.byagowi.persiancalendar.ui.utils.sp

fun showTypographyDemoDialog(activity: Activity) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return
    val root = LinearLayout(activity).also { it.orientation = LinearLayout.VERTICAL }
    listOf(
        "DisplayLarge" to com.google.android.material.R.style.TextAppearance_Material3_DisplayLarge,
        "DisplayMedium" to com.google.android.material.R.style.TextAppearance_Material3_DisplayMedium,
        "DisplaySmall" to com.google.android.material.R.style.TextAppearance_Material3_DisplaySmall,
        "HeadlineLarge" to com.google.android.material.R.style.TextAppearance_Material3_HeadlineLarge,
        "HeadlineMedium" to com.google.android.material.R.style.TextAppearance_Material3_HeadlineMedium,
        "HeadlineSmall" to com.google.android.material.R.style.TextAppearance_Material3_HeadlineSmall,
        "TitleLarge" to com.google.android.material.R.style.TextAppearance_Material3_TitleLarge,
        "TitleMedium" to com.google.android.material.R.style.TextAppearance_Material3_TitleMedium,
        "TitleSmall" to com.google.android.material.R.style.TextAppearance_Material3_TitleSmall,
        "BodyLarge" to com.google.android.material.R.style.TextAppearance_Material3_BodyLarge,
        "BodyMedium" to com.google.android.material.R.style.TextAppearance_Material3_BodyMedium,
        "BodySmall" to com.google.android.material.R.style.TextAppearance_Material3_BodySmall,
        "LabelLarge" to com.google.android.material.R.style.TextAppearance_Material3_LabelLarge,
        "LabelMedium" to com.google.android.material.R.style.TextAppearance_Material3_LabelMedium,
        "LabelSmall" to com.google.android.material.R.style.TextAppearance_Material3_LabelSmall
    ).map { (text, style) ->
        root.addView(TextView(activity).also {
            it.setTextAppearance(style)
            it.text = listOf(text, it.textSize / 1.sp).joinToString(" ")
        })
    }
    AlertDialog.Builder(activity).setView(root).show()
}
