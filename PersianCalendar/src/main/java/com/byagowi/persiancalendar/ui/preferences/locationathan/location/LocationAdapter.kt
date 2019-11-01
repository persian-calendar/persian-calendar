package com.byagowi.persiancalendar.ui.preferences.locationathan.location

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.byagowi.persiancalendar.*
import com.byagowi.persiancalendar.databinding.ListItemCityNameBinding
import com.byagowi.persiancalendar.entities.CityItem
import com.byagowi.persiancalendar.utils.getAppLanguage

class LocationAdapter(
    private val mLocationPreferenceDialog: LocationPreferenceDialog,
    private val mCities: List<CityItem>
) : RecyclerView.Adapter<LocationAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationAdapter.ViewHolder {
        val binding = ListItemCityNameBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(mCities[position])

    override fun getItemCount(): Int = mCities.size

    inner class ViewHolder(private val binding: ListItemCityNameBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        fun bind(cityEntity: CityItem) {
            binding.run {
                root.setOnClickListener(this@ViewHolder)
                when (getAppLanguage()) {
                    LANG_EN_IR, LANG_EN_US, LANG_JA -> {
                        city.text = cityEntity.en
                        country.text = cityEntity.countryEn
                    }
                    LANG_CKB -> {
                        city.text = cityEntity.ckb
                        country.text = cityEntity.countryCkb
                    }
                    LANG_AR -> {
                        city.text = cityEntity.ar
                        country.text = cityEntity.countryAr
                    }
                    else -> {
                        city.text = cityEntity.fa
                        country.text = cityEntity.countryFa
                    }
                }
            }
        }

        override fun onClick(view: View) =
            mLocationPreferenceDialog.selectItem(mCities[adapterPosition].key)
    }
}
