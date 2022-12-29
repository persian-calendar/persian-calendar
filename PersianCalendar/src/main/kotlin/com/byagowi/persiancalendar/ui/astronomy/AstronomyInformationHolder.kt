package com.byagowi.persiancalendar.ui.astronomy

import android.content.Context
import android.content.res.ColorStateList
import android.os.Build
import android.util.AttributeSet
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.appcompat.widget.LinearLayoutCompat
import com.byagowi.persiancalendar.databinding.AstronomyInformationHolderBinding
import com.byagowi.persiancalendar.ui.utils.layoutInflater
import com.byagowi.persiancalendar.ui.utils.setupLayoutTransition

class AstronomyInformationHolder(context: Context, attrs: AttributeSet? = null) :
    LinearLayoutCompat(context, attrs) {
    var setTitle = fun(_: String) {}
        private set
    var setColor = fun(@ColorInt _: Int) {}
        private set
    var setValue = fun(_: String) {}
        private set

    init {
        // Chip view inflation crashes in Android 4 as lack of RippleDrawable apparently and material's
        // internal bug so let's just hide it there
        addView(if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            val content = TextView(context)
            var title = ""
            setTitle = { title = it }
            setValue = { content.text = "$title: $it" }
            content
        } else {
            val binding = AstronomyInformationHolderBinding.inflate(context.layoutInflater)
            setColor = { binding.title.chipBackgroundColor = ColorStateList.valueOf(it) }
            setTitle = binding.title::setText
            setValue = binding.value::setText
            binding.root
        })
        setupLayoutTransition()
    }
}
