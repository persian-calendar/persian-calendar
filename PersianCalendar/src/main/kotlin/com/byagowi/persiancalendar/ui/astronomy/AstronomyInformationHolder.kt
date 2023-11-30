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
    private val binding = AstronomyInformationHolderBinding.inflate(context.layoutInflater)
    fun setTitle(title: String) {
        binding.title.text = title
    }

    fun setColor(@ColorInt color: Int) {
        binding.title.chipBackgroundColor =
            ColorStateList.valueOf(if (context.isDynamicGrayscale) 0xcc808080.toInt() else color)
    }

    fun setValue(value: String) {
        binding.value.text = value
    }

    init {
        addView(binding.root)
        setupLayoutTransition()
    }
}
