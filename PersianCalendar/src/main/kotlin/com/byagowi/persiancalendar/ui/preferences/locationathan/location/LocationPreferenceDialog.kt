package com.byagowi.persiancalendar.ui.preferences.locationathan.location

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.byagowi.persiancalendar.PREF_ALTITUDE
import com.byagowi.persiancalendar.PREF_GEOCODED_CITYNAME
import com.byagowi.persiancalendar.PREF_LATITUDE
import com.byagowi.persiancalendar.PREF_LONGITUDE
import com.byagowi.persiancalendar.PREF_SELECTED_LOCATION
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.ListItemCityNameBinding
import com.byagowi.persiancalendar.entities.CityItem
import com.byagowi.persiancalendar.generated.citiesStore
import com.byagowi.persiancalendar.ui.utils.layoutInflater
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.language

/**
 * persian_calendar
 * Author: hamidsafdari22@gmail.com
 * Date: 1/17/16
 */
fun showLocationPreferenceDialog(context: Context) {
    val recyclerView = RecyclerView(context)
    val dialog = AlertDialog.Builder(context)
        .setTitle(R.string.location)
        .setView(recyclerView)
        .setPositiveButton("", null)
        .setNegativeButton("", null)
        .create()
    recyclerView.setHasFixedSize(true)
    recyclerView.layoutManager = LinearLayoutManager(context)
    recyclerView.adapter = CitiesListAdapter(onItemClicked = { result ->
        dialog.dismiss()
        context.appPrefs.edit {
            remove(PREF_GEOCODED_CITYNAME)
            remove(PREF_LATITUDE)
            remove(PREF_LONGITUDE)
            remove(PREF_ALTITUDE)
            putString(PREF_SELECTED_LOCATION, result)
        }
    })
    dialog.show()
}

private class CitiesListAdapter(val onItemClicked: (key: String) -> Unit) :
    RecyclerView.Adapter<CitiesListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ListItemCityNameBinding.inflate(parent.context.layoutInflater, parent, false)
    )

    private val cities = citiesStore.values.sortedWith(language.createCitiesComparator())

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(cities[position])

    override fun getItemCount(): Int = cities.size

    inner class ViewHolder(private val binding: ListItemCityNameBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        fun bind(cityEntity: CityItem) {
            binding.root.setOnClickListener(this)
            binding.city.text = language.getCityName(cityEntity)
            binding.country.text = language.getCountryName(cityEntity)
        }

        override fun onClick(view: View) = onItemClicked(cities[bindingAdapterPosition].key)
    }
}
