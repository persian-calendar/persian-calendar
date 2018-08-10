package com.byagowi.persiancalendar.view.preferences

import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.preference.PreferenceDialogFragmentCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.adapter.LocationAdapter

/**
 * persian_calendar
 * Author: hamidsafdari22@gmail.com
 * Date: 1/17/16
 */
class LocationPreferenceDialog : PreferenceDialogFragmentCompat() {

  override fun onPrepareDialogBuilder(builder: AlertDialog.Builder?) {
    super.onPrepareDialogBuilder(builder)
    val inflater = LayoutInflater.from(context)
    val view = inflater.inflate(R.layout.preference_location, null, false)

    val recyclerView = view.findViewById<RecyclerView>(R.id.RecyclerView)
    recyclerView.setHasFixedSize(true)
    recyclerView.layoutManager = LinearLayoutManager(context)
    recyclerView.adapter = LocationAdapter(this)

    if (builder != null) {
      builder.setPositiveButton("", null)
      builder.setNegativeButton("", null)
      builder.setView(view)
    }
  }

  override fun onDialogClosed(positiveResult: Boolean) {}

  fun selectItem(city: String) {
    (preference as LocationPreference).setSelected(city)
    dismiss()
  }
}
