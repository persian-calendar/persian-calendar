package com.byagowi.persiancalendar.ui.map

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.navGraphViewModels
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.FragmentSkyRendererBinding
import com.byagowi.persiancalendar.global.coordinates
import com.byagowi.persiancalendar.ui.utils.setupUpNavigation
import com.byagowi.persiancalendar.utils.calculateSunMoonPosition
import java.util.*

class SkyRendererFragment : Fragment(R.layout.fragment_sky_renderer) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentSkyRendererBinding.bind(view)
        binding.appBar.toolbar.let {
            it.title = "PanoRendo"
            it.setupUpNavigation()
        }

        binding.toneMap.adapter = ArrayAdapter(
            view.context,
            androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
            ToneMap.values().map { it.toString() }
        )

        val coordinates = coordinates
        if (coordinates == null) {
            Toast.makeText(view.context, "Location is not set", Toast.LENGTH_SHORT).show()
        }
        val sunPosition = GregorianCalendar().also {
            val mapViewModel by navGraphViewModels<MapViewModel>(R.id.map)
            it.time = Date(mapViewModel.state.value.time)
        }.calculateSunMoonPosition(coordinates).sunPosition

        fun update() = binding.image.setImageBitmap(
            panoRendo(
                sunElevationDegrees = sunPosition?.altitude ?: 30.0,
                sunAzimuthDegrees = sunPosition?.azimuth ?: 0.0,
                toneMap = ToneMap.values().getOrNull(binding.toneMap.selectedItemPosition)
                    ?: ToneMap.Reinhard,
                zoom = binding.zoom.text?.toString()?.toDoubleOrNull() ?: .0
            )
        )
        update()
        binding.toneMap.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) = update()
        }
        binding.zoom.addTextChangedListener { update() }
    }
}
