package com.byagowi.persiancalendar.ui.counter

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.CounterScreenBinding
import com.byagowi.persiancalendar.ui.utils.setupUpNavigation

class CounterScreen : Fragment(R.layout.counter_screen) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = CounterScreenBinding.bind(view)

        binding.appBar.toolbar.also { toolbar ->
            toolbar.setTitle("شمارنده")
            toolbar.setupUpNavigation()
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.appBar.toolbar.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = insets.top
            }
            WindowInsetsCompat.CONSUMED
        }
    }
}
