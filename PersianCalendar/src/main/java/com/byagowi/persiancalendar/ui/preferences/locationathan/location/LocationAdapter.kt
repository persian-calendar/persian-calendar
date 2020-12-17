package com.byagowi.persiancalendar.ui.preferences.locationathan.location

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.byagowi.persiancalendar.*
import com.byagowi.persiancalendar.databinding.ListItemCityNameBinding
import com.byagowi.persiancalendar.entities.CityItem
import com.byagowi.persiancalendar.utils.language
import com.byagowi.persiancalendar.utils.layoutInflater

class LocationAdapter(
        private val locationPreferenceDialog: LocationPreferenceDialog,
        private val cities: List<CityItem>
) : RecyclerView.Adapter<LocationAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
            ListItemCityNameBinding.inflate(parent.context.layoutInflater, parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
            holder.bind(cities[position])

    override fun getItemCount(): Int = cities.size

    inner class ViewHolder(private val binding: ListItemCityNameBinding) :
            RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        fun bind(cityEntity: CityItem) = binding.let {
            it.root.setOnClickListener(this)
            when (language) {
                LANG_EN_IR, LANG_EN_US, LANG_JA -> {
                    it.city.text = cityEntity.en
                    it.country.text = cityEntity.countryEn
                }
                LANG_CKB -> {
                    it.city.text = cityEntity.ckb
                    it.country.text = cityEntity.countryCkb
                }
                LANG_AR -> {
                    it.city.text = cityEntity.ar
                    it.country.text = cityEntity.countryAr
                }
                else -> {
                    it.city.text = cityEntity.fa
                    it.country.text = cityEntity.countryFa
                }
            }
        }

        override fun onClick(view: View) =
                locationPreferenceDialog.selectItem(cities[adapterPosition].key)
    }
}
