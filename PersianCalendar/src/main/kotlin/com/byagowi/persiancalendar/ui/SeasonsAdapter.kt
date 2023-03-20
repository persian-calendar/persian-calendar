package com.byagowi.persiancalendar.ui

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.byagowi.persiancalendar.databinding.SeasonCarouselItemBinding
import com.byagowi.persiancalendar.entities.Season
import com.byagowi.persiancalendar.ui.utils.layoutInflater

class SeasonsAdapter : RecyclerView.Adapter<SeasonsAdapter.SeasonImageViewHolder>() {
    class SeasonImageViewHolder(val view: SeasonCarouselItemBinding) :
        RecyclerView.ViewHolder(view.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SeasonImageViewHolder {
        return SeasonImageViewHolder(
            SeasonCarouselItemBinding.inflate(parent.context.layoutInflater)
        )
    }

    private val seasons = Season.values()
    override fun getItemCount(): Int = seasons.size * 3

    override fun onBindViewHolder(holder: SeasonImageViewHolder, position: Int) {
        holder.view.seasonImage.setImageResource(seasons[position % 4].imageId)
    }

    companion object {
        // Let's skip the first four seasons to make sure an initial scroll happen
        fun toActualIndex(index: Int): Int = 4 + index
    }
}
