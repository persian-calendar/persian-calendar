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
import io.github.cosinekitty.astronomy.Aberration
import io.github.cosinekitty.astronomy.AstroTime
import io.github.cosinekitty.astronomy.Body
import io.github.cosinekitty.astronomy.EquatorEpoch
import io.github.cosinekitty.astronomy.Observer
import io.github.cosinekitty.astronomy.Refraction
import io.github.cosinekitty.astronomy.equator
import io.github.cosinekitty.astronomy.horizon
import java.util.*

class SkyRendererScreen : Fragment(R.layout.fragment_sky_renderer) {
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
        val time = AstroTime(GregorianCalendar().also {
            val mapViewModel by navGraphViewModels<MapViewModel>(R.id.map)
            it.time = Date(mapViewModel.state.value.time)
        }.time)
        val horizon = coordinates?.let { Observer(it.latitude, it.longitude, it.elevation) }?.let {
            val sunEquator = equator(Body.Sun, time, it, EquatorEpoch.OfDate, Aberration.None)
            horizon(time, it, sunEquator.ra, sunEquator.dec, Refraction.None)
        }

        fun update() = binding.image.setImageBitmap(
            panoRendo(
                sunElevationDegrees = horizon?.altitude ?: 30.0,
                sunAzimuthDegrees = horizon?.azimuth ?: 0.0,
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
