package com.byagowi.persiancalendar.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.byagowi.persiancalendar.Constants
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entity.CityEntity
import com.byagowi.persiancalendar.util.Utils
import com.byagowi.persiancalendar.view.preferences.LocationPreferenceDialog
import java.util.Collections.emptyList

class LocationAdapter(private val locationPreferenceDialog: LocationPreferenceDialog) : RecyclerView.Adapter<LocationAdapter.ViewHolder>() {
  private val locale: String = Utils.appLanguage
  private val cities: List<CityEntity>
  private val layoutInflater: LayoutInflater

  init {
    val ctx = locationPreferenceDialog.context
    this.layoutInflater = LayoutInflater.from(ctx)
    this.cities = if (ctx == null) emptyList() else Utils.getAllCities(ctx, true)
  }

  inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
    val tvCountry: TextView = itemView.findViewById(R.id.tvCity)
    val tvCity: TextView = itemView.findViewById(R.id.tvCountry)

    init {
      itemView.setOnClickListener(this)
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