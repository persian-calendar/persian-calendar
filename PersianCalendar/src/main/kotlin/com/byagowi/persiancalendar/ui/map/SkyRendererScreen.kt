package com.byagowi.persiancalendar.ui.map

import android.graphics.Paint
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.graphics.createBitmap
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.navGraphViewModels
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.FragmentSkyRendererBinding
import com.byagowi.persiancalendar.global.coordinates
import com.byagowi.persiancalendar.ui.utils.setupUpNavigation
import com.byagowi.persiancalendar.utils.logException
import com.byagowi.persiancalendar.utils.toObserver
import io.github.cosinekitty.astronomy.Aberration
import io.github.cosinekitty.astronomy.Body
import io.github.cosinekitty.astronomy.EquatorEpoch
import io.github.cosinekitty.astronomy.Refraction
import io.github.cosinekitty.astronomy.Time
import io.github.cosinekitty.astronomy.equator
import io.github.cosinekitty.astronomy.horizon

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
        val time = Time.fromMillisecondsSince1970(run {
            runCatching {
                val mapViewModel by navGraphViewModels<MapViewModel>(R.id.map)
                mapViewModel.state.value.time
            }.onFailure(logException).getOrNull() ?: System.currentTimeMillis()
        })
        val horizon = coordinates?.toObserver()?.let {
            val sunEquator = equator(Body.Sun, time, it, EquatorEpoch.OfDate, Aberration.None)
            horizon(time, it, sunEquator.ra, sunEquator.dec, Refraction.None)
        }

        var bitmap = createBitmap(1, 1)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).also { it.isFilterBitmap = true }
        binding.image.onDraw = { canvas, matrix -> canvas.drawBitmap(bitmap, matrix, paint) }

        fun update() {
            bitmap = panoRendo(
                sunElevationDegrees = horizon?.altitude ?: 30.0,
                sunAzimuthDegrees = horizon?.azimuth ?: 0.0,
                toneMap = ToneMap.values().getOrNull(binding.toneMap.selectedItemPosition)
                    ?: ToneMap.Reinhard,
                zoom = binding.zoom.text?.toString()?.toDoubleOrNull() ?: .0
            )
            binding.image.contentWidth = bitmap.width.toFloat()
            binding.image.contentHeight = bitmap.height.toFloat()
            binding.image.invalidate()
        }
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
