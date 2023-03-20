package com.byagowi.persiancalendar.ui

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.byagowi.persiancalendar.databinding.SeasonCarouselItemBinding
import com.byagowi.persiancalendar.entities.Season
import com.byagowi.persiancalendar.ui.utils.layoutInflater

class SeasonsAdapter : RecyclerView.Adapter<SeasonsAdapter.SeasonImageViewHolder>() {
    class SeasonImageViewHolder(val binding: SeasonCarouselItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SeasonImageViewHolder {
        return SeasonImageViewHolder(
            SeasonCarouselItemBinding.inflate(parent.context.layoutInflater, parent, false)
        )
    }

    override fun getItemCount(): Int = toActualIndex(0) * 2

    private val seasons = enumValues<Season>()
    override fun onBindViewHolder(holder: SeasonImageViewHolder, position: Int) {
        holder.binding.seasonImage.setImageResource(seasons[position % 4].imageId)
    }

    companion object {
        // Let's skip the first four seasons to make sure an initial scroll happen
        fun toActualIndex(index: Int): Int = 4 * 100 + index
    }
}
