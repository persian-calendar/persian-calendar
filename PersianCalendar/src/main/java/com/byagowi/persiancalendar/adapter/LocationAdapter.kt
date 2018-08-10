package com.byagowi.persiancalendar.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.byagowi.persiancalendar.Constants
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entity.CityEntity
import com.byagowi.persiancalendar.util.Utils
import com.byagowi.persiancalendar.view.preferences.LocationPreferenceDialog

import androidx.recyclerview.widget.RecyclerView

class LocationAdapter(private val locationPreferenceDialog: LocationPreferenceDialog) : RecyclerView.Adapter<LocationAdapter.ViewHolder>() {
  private val locale: String
  private val cities: List<CityEntity>
  private val layoutInflater: LayoutInflater

  init {
    val ctx = locationPreferenceDialog.context
    this.layoutInflater = LayoutInflater.from(ctx)
    this.cities = if (ctx == null) emptyList() else Utils.getAllCities(ctx!!, true)
    this.locale = Utils.appLanguage
  }

  inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
    val tvCountry: TextView
    val tvCity: TextView

    init {
      itemView.setOnClickListener(this)
      tvCity = itemView.findViewById(R.id.tvCity)
      tvCountry = itemView.findViewById(R.id.tvCountry)
    }

    override fun onClick(view: View) =
        locationPreferenceDialog.selectItem(cities[adapterPosition].key)
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationAdapter.ViewHolder =
      ViewHolder(layoutInflater.inflate(R.layout.list_item_city_name, parent, false))

  override fun onBindViewHolder(holder: ViewHolder, position: Int) = when (locale) {
    Constants.LANG_EN -> {
      holder.tvCity.text = cities[position].en
      holder.tvCountry.text = cities[position].countryEn
    }
    Constants.LANG_CKB -> {
      holder.tvCity.text = cities[position].ckb
      holder.tvCountry.text = cities[position].countryCkb
    }
    else -> {
      holder.tvCity.text = cities[position].fa
      holder.tvCountry.text = cities[position].countryFa
    }
  }

  override fun getItemCount(): Int = cities.size
}