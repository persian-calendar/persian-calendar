package com.byagowi.persiancalendar.ui.preferences.locationathan.location

import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.byagowi.persiancalendar.*
import com.byagowi.persiancalendar.databinding.ListItemCityNameBinding
import com.byagowi.persiancalendar.entities.CityItem
import com.byagowi.persiancalendar.generated.citiesStore
import com.byagowi.persiancalendar.ui.utils.layoutInflater
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.language
import com.byagowi.persiancalendar.utils.localizedCityName
import com.byagowi.persiancalendar.utils.localizedCountryName
import com.byagowi.persiancalendar.utils.updateStoredPreference

/**
 * persian_calendar
 * Author: hamidsafdari22@gmail.com
 * Date: 1/17/16
 */
fun Fragment.showLocationPreferenceDialog() {
    val recyclerView = RecyclerView(layoutInflater.context)
    val dialog = AlertDialog.Builder(layoutInflater.context)
        .setTitle(R.string.location)
        .setView(recyclerView)
        .setPositiveButton("", null)
        .setNegativeButton("", null)
        .create()
    recyclerView.setHasFixedSize(true)
    recyclerView.layoutManager = LinearLayoutManager(context)
    recyclerView.adapter = CitiesListAdapter(onItemClicked = { result ->
        dialog.dismiss()
        layoutInflater.context.appPrefs.edit {
            remove(PREF_GEOCODED_CITYNAME)
            remove(PREF_LATITUDE)
            remove(PREF_LONGITUDE)
            remove(PREF_ALTITUDE)
            putString(PREF_SELECTED_LOCATION, result)
        }
        updateStoredPreference(layoutInflater.context)
    })
    dialog.show()
}

private class CitiesListAdapter(val onItemClicked: (key: String) -> Unit) :
    RecyclerView.Adapter<CitiesListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ListItemCityNameBinding.inflate(parent.context.layoutInflater, parent, false)
    )

    private val cities = getCitiesList(language)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(cities[position])

    override fun getItemCount(): Int = cities.size

    inner class ViewHolder(private val binding: ListItemCityNameBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        fun bind(cityEntity: CityItem) {
            binding.root.setOnClickListener(this)
            binding.city.text = cityEntity.localizedCityName
            binding.country.text = cityEntity.localizedCountryName
        }

        override fun onClick(view: View) = onItemClicked(cities[bindingAdapterPosition].key)
    }

    companion object {
        private val irCodeOrder = listOf("zz", "ir", "af", "iq")
        private val afCodeOrder = listOf("zz", "af", "ir", "iq")
        private val arCodeOrder = listOf("zz", "iq", "ir", "af")

        private fun getCountryCodeOrder(countryCode: String): Int = when (language) {
            LANG_FA_AF, LANG_PS -> afCodeOrder.indexOf(countryCode)
            LANG_AR -> arCodeOrder.indexOf(countryCode)
            LANG_FA, LANG_GLK, LANG_AZB -> irCodeOrder.indexOf(countryCode)
            else -> irCodeOrder.indexOf(countryCode)
        }

        private fun prepareForArabicSort(text: String) = text
            .replace("ی", "ي")
            .replace("ک", "ك")
            .replace("گ", "كی")
            .replace("ژ", "زی")
            .replace("چ", "جی")
            .replace("پ", "بی")
            .replace("ڕ", "ری")
            .replace("ڵ", "لی")
            .replace("ڤ", "فی")
            .replace("ۆ", "وی")
            .replace("ێ", "یی")
            .replace("ھ", "نی")
            .replace("ە", "هی")

        private fun getCitiesList(language: String): List<CityItem> {
            return citiesStore.values.sortedWith(kotlin.Comparator { l, r ->
                if (l.key == "") return@Comparator -1

                if (r.key == DEFAULT_CITY) return@Comparator 1

                val compare =
                    getCountryCodeOrder(l.countryCode) - getCountryCodeOrder(r.countryCode)
                if (compare != 0) return@Comparator compare

                return@Comparator when (language) {
                    LANG_EN_US, LANG_JA, LANG_FR, LANG_ES, LANG_EN_IR -> l.en.compareTo(r.en)
                    LANG_AR -> l.ar.compareTo(r.ar)
                    LANG_CKB -> prepareForArabicSort(l.ckb).compareTo(prepareForArabicSort(r.ckb))
                    else -> prepareForArabicSort(l.fa).compareTo(prepareForArabicSort(r.fa))
                }
            })
        }
    }
}
