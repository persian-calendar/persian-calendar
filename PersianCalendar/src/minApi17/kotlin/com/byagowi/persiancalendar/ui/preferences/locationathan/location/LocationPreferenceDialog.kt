package com.byagowi.persiancalendar.ui.preferences.locationathan.location

import android.app.Activity
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.LocationPreferenceDialogBinding
import com.byagowi.persiancalendar.generated.citiesStore
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.saveCity
import com.byagowi.persiancalendar.utils.sortCityNames

/**
 * persian_calendar
 * Author: hamidsafdari22@gmail.com
 * Date: 1/17/16
 */
fun showLocationPreferenceDialog(activity: Activity) {
    val binding = LocationPreferenceDialogBinding.inflate(activity.layoutInflater)
    val dialog = AlertDialog.Builder(activity)
        .setTitle(R.string.location)
        .setView(binding.root)
        .setPositiveButton("", null)
        .setNegativeButton("", null)
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
