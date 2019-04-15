package com.byagowi.persiancalendar.ui.preferences.locationathan.location

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.byagowi.persiancalendar.Constants
import com.byagowi.persiancalendar.databinding.ListItemCityNameBinding
import com.byagowi.persiancalendar.entities.CityItem
import com.byagowi.persiancalendar.utils.Utils

class LocationAdapter constructor(private val mLocationPreferenceDialog: LocationPreferenceDialog,
                                  private val mCities: List<CityItem>) : RecyclerView.Adapter<LocationAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationAdapter.ViewHolder {
        val binding = ListItemCityNameBinding.inflate(
                LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(mCities[position])

    override fun getItemCount(): Int = mCities.size

    inner class ViewHolder(private val binding: ListItemCityNameBinding) : RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        fun bind(cityEntity: CityItem) {
            val city: String
            val country: String
            when (Utils.getAppLanguage()) {
                Constants.LANG_EN_IR, Constants.LANG_EN_US, Constants.LANG_JA -> {
                    city = cityEntity.en
                    country = cityEntity.countryEn
                }
                Constants.LANG_CKB -> {
                    city = cityEntity.ckb
                    country = cityEntity.countryCkb
                }
                Constants.LANG_AR -> {
                    city = cityEntity.ar
                    country = cityEntity.countryAr
                }
                else -> {
                    city = cityEntity.fa
                    country = cityEntity.countryFa
                }
            }
            val model = LocationAdapterModel(city, country, this)
            binding.model = model
            binding.executePendingBindings()
        }

        override fun onClick(view: View) =
                mLocationPreferenceDialog.selectItem(mCities[adapterPosition].key)
    }
}
