package com.byagowi.persiancalendar.ui.preferences.locationathan.location

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.ListItemCityNameBinding
import com.byagowi.persiancalendar.entities.CityItem
import com.byagowi.persiancalendar.generated.citiesStore
import com.byagowi.persiancalendar.ui.utils.layoutInflater
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.language
import com.byagowi.persiancalendar.utils.saveCity

/**
 * persian_calendar
 * Author: hamidsafdari22@gmail.com
 * Date: 1/17/16
 */
fun showLocationPreferenceDialog(activity: Activity) {
    val recyclerView = RecyclerView(activity)
    val dialog = AlertDialog.Builder(activity)
        .setTitle(R.string.location)
        .setView(recyclerView)
        .setPositiveButton("", null)
        .setNegativeButton("", null)
        .create()
    recyclerView.setHasFixedSize(true)
    recyclerView.layoutManager = LinearLayoutManager(activity)
    recyclerView.adapter = CitiesListAdapter(onItemClicked = { city ->
        dialog.dismiss()
        activity.appPrefs.saveCity(city)
    })
    dialog.show()
}

private class CitiesListAdapter(val onItemClicked: (key: CityItem) -> Unit) :
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

        override fun onClick(view: View) = onItemClicked(cities[bindingAdapterPosition])
    }
}
