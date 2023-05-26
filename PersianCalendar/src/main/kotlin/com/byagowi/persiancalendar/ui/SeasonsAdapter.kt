package com.byagowi.persiancalendar.ui

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.byagowi.persiancalendar.databinding.SeasonItemBinding
import com.byagowi.persiancalendar.entities.Season
import com.byagowi.persiancalendar.entities.Theme
import com.byagowi.persiancalendar.global.coordinates
import com.byagowi.persiancalendar.ui.utils.isDynamicGrayscale
import com.byagowi.persiancalendar.ui.utils.layoutInflater
import com.byagowi.persiancalendar.utils.appPrefs
import java.util.Date

class SeasonsAdapter : RecyclerView.Adapter<SeasonsAdapter.SeasonImageViewHolder>() {
    class SeasonImageViewHolder(val binding: SeasonItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SeasonImageViewHolder {
        val binding = SeasonItemBinding.inflate(parent.context.layoutInflater, parent, false)
        if (Theme.isDynamicColor(parent.context.appPrefs) && parent.context. isDynamicGrayscale)
            binding.image.colorFilter = grayScaleColorFilter
        return SeasonImageViewHolder(binding)
    }

    override fun getItemCount(): Int = toActualIndex(0) * 2

    private val seasons = enumValues<Season>()
    override fun onBindViewHolder(holder: SeasonImageViewHolder, position: Int) {
        holder.binding.image.setImageResource(seasons[position % 4].imageId)
    }

    companion object {
        private val grayScaleColorFilter by lazy(LazyThreadSafetyMode.NONE) {
            // https://stackoverflow.com/q/10904690
            ColorMatrixColorFilter(ColorMatrix().also { it.setSaturation(0f) })
        }

        private fun toActualIndex(index: Int): Int = 4 * 100 + index

        fun getCurrentIndex(): Int =
            toActualIndex(Season.fromDate(Date(), coordinates.value).ordinal)
    }
}
