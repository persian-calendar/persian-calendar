package com.byagowi.persiancalendar.ui.preferences.locationathan.location

import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.LocationPreferenceDialogBinding
import com.byagowi.persiancalendar.generated.citiesStore
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.saveCity
import com.byagowi.persiancalendar.utils.sortCityNames
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * persian_calendar
 * Author: hamidsafdari22@gmail.com
 * Date: 1/17/16
 */
fun showLocationPreferenceDialog(activity: FragmentActivity) {
    val binding = LocationPreferenceDialogBinding.inflate(activity.layoutInflater)
    val dialog = MaterialAlertDialogBuilder(activity)
        .setTitle(R.string.location)
        .setView(binding.root)
        .create()
    binding.recyclerView.setHasFixedSize(true)
    binding.recyclerView.layoutManager = LinearLayoutManager(activity)
    val cities = citiesStore.values.sortCityNames
    binding.recyclerView.adapter = PairsListAdapter(onItemClicked = { index ->
        dialog.dismiss()
        activity.appPrefs.saveCity(cities[index])
    }, cities.map { language.getCityName(it) to language.getCountryName(it) })
    if (language.isIranExclusive) {
        binding.more.setOnClickListener {
            dialog.dismiss()
            showProvinceDialog(activity)
        }
    } else {
        binding.more.isVisible = false
        binding.recyclerView.setPadding(0, 0, 0, 0)
    }
    dialog.show()
}
