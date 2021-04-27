package com.byagowi.persiancalendar.ui.preferences.locationathan.location

import androidx.appcompat.app.AlertDialog
import androidx.preference.PreferenceDialogFragmentCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.byagowi.persiancalendar.*
import com.byagowi.persiancalendar.generated.citiesStore
import com.byagowi.persiancalendar.utils.language

/**
 * persian_calendar
 * Author: hamidsafdari22@gmail.com
 * Date: 1/17/16
 */
class LocationPreferenceDialog : PreferenceDialogFragmentCompat() {

    private val irCodeOrder = listOf("zz", "ir", "af", "iq")
    private val afCodeOrder = listOf("zz", "af", "ir", "iq")
    private val arCodeOrder = listOf("zz", "iq", "ir", "af")

    private fun getCountryCodeOrder(countryCode: String): Int =
        when (language) {
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

    private val sortedCitiesList = citiesStore.values.sortedWith(kotlin.Comparator { l, r ->
        if (l.key == "") return@Comparator -1

        if (r.key == DEFAULT_CITY) return@Comparator 1

        val compare = getCountryCodeOrder(l.countryCode) - getCountryCodeOrder(r.countryCode)
        if (compare != 0) return@Comparator compare

        when (language) {
            LANG_EN_US, LANG_JA, LANG_EN_IR -> l.en.compareTo(r.en)
            LANG_AR -> l.ar.compareTo(r.ar)
            LANG_CKB -> prepareForArabicSort(l.ckb).compareTo(prepareForArabicSort(r.ckb))
            else -> prepareForArabicSort(l.fa).compareTo(prepareForArabicSort(r.fa))
        }
    })

    override fun onPrepareDialogBuilder(builder: AlertDialog.Builder?) {
        super.onPrepareDialogBuilder(builder)
        if (builder == null) return
        builder
            .setView(RecyclerView(builder.context).also {
                it.setHasFixedSize(true)
                it.layoutManager = LinearLayoutManager(it.context)
                it.adapter = LocationAdapter(this, sortedCitiesList)
            })
            .setPositiveButton("", null)
            .setNegativeButton("", null)
    }

    override fun onDialogClosed(positiveResult: Boolean) {}

    fun selectItem(city: String) {
        (preference as LocationPreference).setSelected(city)
        dismiss()
    }
}
