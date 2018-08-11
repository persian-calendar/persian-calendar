package com.byagowi.persiancalendar.view.dialog

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.DialogAccessBinding

class GPSDiagnosticDialog : DialogFragment() {

  // This is a workaround for the strange behavior of onCreateView (which doesn't show dialog's layout)
  @NonNull
  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val context = context ?: return Dialog(null)

    val dialogBuilder = AlertDialog.Builder(context)

    val binding = DataBindingUtil.inflate<DialogAccessBinding>(LayoutInflater.from(getContext()),
        R.layout.dialog_access, null, false)
    dialogBuilder.setView(binding.root)

    // check whether gps provider and network providers are enabled or not
    val gps = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
    val info: NetworkInfo? = connectivityManager?.activeNetworkInfo

    var gpsEnabled = false

    try {
      gpsEnabled = gps?.isProviderEnabled(LocationManager.GPS_PROVIDER) ?: false
    } catch (ignored: Exception) {
    }

    binding.dialogButtonGPS.setOnClickListener {
      val myIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
      activity?.startActivity(myIntent)
      dialog.dismiss()
      // get gps
    }

    binding.dialogButtonWiFi.setOnClickListener {
      val myIntent = Intent(Settings.ACTION_WIFI_SETTINGS)
      activity?.startActivity(myIntent)
      dialog.dismiss()
      // get wifi
    }

    binding.dialogButtonGPRS.setOnClickListener {
      val myIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
      activity?.startActivity(myIntent)
      dialog.dismiss()
      // get gprs
    }

    binding.dialogButtonExit.setOnClickListener {
      dialog.dismiss()
      // exit
    }

    if (!gpsEnabled && info == null) {
      Toast.makeText(activity, R.string.internet_location_enable, Toast.LENGTH_SHORT).show()
      binding.dialogButtonGPS.visibility = View.VISIBLE
      binding.dialogButtonWiFi.visibility = View.VISIBLE
      binding.dialogButtonGPRS.visibility = View.VISIBLE
    } else if (!gpsEnabled) {
      Toast.makeText(activity, R.string.location_enable, Toast.LENGTH_SHORT).show()
      binding.dialogButtonGPRS.visibility = View.GONE
      binding.dialogButtonWiFi.visibility = View.GONE
    } else if (info == null) {
      Toast.makeText(activity, R.string.internet_enable, Toast.LENGTH_SHORT).show()
      binding.dialogButtonGPS.visibility = View.GONE
    }

    isCancelable = true

    return dialogBuilder.create()
  }
}