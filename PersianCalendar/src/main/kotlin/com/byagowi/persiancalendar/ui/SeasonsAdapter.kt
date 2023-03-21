package com.byagowi.persiancalendar.ui

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.byagowi.persiancalendar.databinding.SeasonItemBinding
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.entities.Season
import com.byagowi.persiancalendar.global.coordinates
import com.byagowi.persiancalendar.ui.utils.layoutInflater

class SeasonsAdapter : RecyclerView.Adapter<SeasonsAdapter.SeasonImageViewHolder>() {
    class SeasonImageViewHolder(val binding: SeasonItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SeasonImageViewHolder {
        return SeasonImageViewHolder(
            SeasonItemBinding.inflate(parent.context.layoutInflater, parent, false)
        )
    }

    override fun getItemCount(): Int = toActualIndex(0) * 2

    private val seasons = enumValues<Season>()
    override fun onBindViewHolder(holder: SeasonImageViewHolder, position: Int) {
        holder.binding.root.setImageResource(seasons[position % 4].imageId)
    }

    companion object {
        private fun toActualIndex(index: Int): Int = 4 * 100 + index

        fun getCurrentIndex(): Int {
            val jdn = Jdn.today().toPersianCalendar()
            return toActualIndex(Season.seasonIndexFromPersianCalendar(jdn, coordinates.value))
        }
    }
}
