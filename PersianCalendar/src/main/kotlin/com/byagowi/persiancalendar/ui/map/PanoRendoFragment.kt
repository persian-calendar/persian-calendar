package com.byagowi.persiancalendar.ui.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.widget.addTextChangedListener
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

        binding.toneMap.adapter = ArrayAdapter(
            inflater.context, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
            ToneMap.values().map { it.toString() }
        )

        fun update() = binding.image.setImageBitmap(
            panoRendo(
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

        return binding.root
    }
}
