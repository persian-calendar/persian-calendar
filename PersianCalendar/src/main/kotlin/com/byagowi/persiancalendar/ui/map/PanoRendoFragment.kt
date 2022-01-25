package com.byagowi.persiancalendar.ui.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.byagowi.persiancalendar.databinding.FragmentPanoRendoBinding
import com.byagowi.persiancalendar.ui.utils.setupUpNavigation

class PanoRendoFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val binding = FragmentPanoRendoBinding.inflate(inflater)
        binding.appBar.toolbar.let {
            it.title = "PanoRendo"
            it.setupUpNavigation()
        }

        var zoom = 1.0
        fun update() = binding.image.setImageBitmap(panoRendo(zoom = zoom))
        update()

        binding.zoom.setOnClickListener {
            zoom = if (zoom == 1.0) 0.0 else 1.0
            update()
        }

        return binding.root
    }
}
