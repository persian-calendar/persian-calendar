package com.byagowi.persiancalendar.ui.preferences.locationathan.location

import android.app.Activity
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.generated.distrcitsStore
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.saveLocation
import io.github.persiancalendar.praytimes.Coordinates

fun showProvinceDialog(activity: Activity) {
    val recyclerView = RecyclerView(activity)
    val dialog = AlertDialog.Builder(activity)
        // i18n is no concern here as it is shown only in specific locales
        .setTitle("انتخاب استان برای مشاهدهٔ بخش‌ها")
        .setView(recyclerView)
        .setPositiveButton("", null)
        .setNegativeButton("", null)
        .create()
    recyclerView.setHasFixedSize(true)
    recyclerView.layoutManager = LinearLayoutManager(activity)
    recyclerView.adapter = PairsListAdapter(onItemClicked = { index ->
        dialog.dismiss()
        showDistrictsDialog(activity, distrcitsStore[index].second)
    }, items = distrcitsStore.map { it.first to "" })
    dialog.show()
}

@VisibleForTesting
fun showDistrictsDialog(activity: Activity, provinceDetails: List<String>) {
    val recyclerView = RecyclerView(activity)
    val dialog = AlertDialog.Builder(activity)
        .setTitle(R.string.location)
        .setView(recyclerView)
        .setPositiveButton("", null)
        .setNegativeButton("", null)
        .create()
    recyclerView.setHasFixedSize(true)
    recyclerView.layoutManager = LinearLayoutManager(activity)
    val districts = provinceDetails.flatMap { county ->
        val countyDetails = county.split(";")
        countyDetails.drop(1).map { it.split(":") to countyDetails[0] }
    }.sortedBy { (district, _) -> language.prepareForSort(district[0/*district name*/]) }
    recyclerView.adapter = PairsListAdapter(onItemClicked = { index ->
        dialog.dismiss()
        val coordinates = Coordinates(
            districts[index].first[1/*latitude*/].toDoubleOrNull() ?: 0.0,
            districts[index].first[2/*longitude*/].toDoubleOrNull() ?: 0.0,
            0.0
        )
        activity.appPrefs.saveLocation(coordinates, districts[index].first[0])
    }, items = districts.map { (districtDetails, county) -> districtDetails[0] to county })
    dialog.show()
}

