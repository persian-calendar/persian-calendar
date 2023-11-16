package com.byagowi.persiancalendar.ui.astronomy

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import androidx.annotation.ColorInt
import androidx.appcompat.widget.LinearLayoutCompat
import com.byagowi.persiancalendar.databinding.AstronomyInformationHolderBinding
import com.byagowi.persiancalendar.ui.utils.isDynamicGrayscale
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
        val binding = AstronomyInformationHolderBinding.inflate(context.layoutInflater)
        setColor = {
            @ColorInt val color = if (context.isDynamicGrayscale) 0xcc808080.toInt() else it
            binding.title.chipBackgroundColor = ColorStateList.valueOf(color)
        }
        setTitle = binding.title::setText
        setValue = binding.value::setText
        addView(binding.root)
        setupLayoutTransition()
    }
}
