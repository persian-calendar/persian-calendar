package com.byagowi.persiancalendar.ui.preferences.locationathan.location

import com.byagowi.persiancalendar.utils.Utils

import androidx.appcompat.app.AlertDialog
import androidx.preference.PreferenceDialogFragmentCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * persian_calendar
 * Author: hamidsafdari22@gmail.com
 * Date: 1/17/16
 */
class LocationPreferenceDialog : PreferenceDialogFragmentCompat() {

    override fun onPrepareDialogBuilder(builder: AlertDialog.Builder?) {
        super.onPrepareDialogBuilder(builder)

        val recyclerView = RecyclerView(builder!!.context)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = LocationAdapter(this,
                Utils.getAllCities(context, true))
        builder.setView(recyclerView)

        builder.setPositiveButton("", null)
        builder.setNegativeButton("", null)
    }

    override fun onDialogClosed(positiveResult: Boolean) {}

    fun selectItem(city: String) {
        (preference as LocationPreference).setSelected(city)
        dismiss()
    }
}
