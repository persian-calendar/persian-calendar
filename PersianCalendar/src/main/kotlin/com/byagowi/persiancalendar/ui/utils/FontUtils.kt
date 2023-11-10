package com.byagowi.persiancalendar.ui.utils

import android.content.Context
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat
import com.byagowi.persiancalendar.R

object FontUtils {
    private var defaultTypeface: Typeface? = null

    fun getDefaultTypeface(context: Context): Typeface {
        if (defaultTypeface == null) {
            defaultTypeface = ResourcesCompat.getFont(context, R.font.vazirmatn_light)
                ?: Typeface.DEFAULT // Fallback to default system font
        }
        return defaultTypeface!!
    }
}
