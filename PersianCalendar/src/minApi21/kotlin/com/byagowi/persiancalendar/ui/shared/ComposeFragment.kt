package com.byagowi.persiancalendar.ui.shared

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.byagowi.persiancalendar.databinding.FragmentComposeBinding
import com.byagowi.persiancalendar.ui.ComposeTheme
import com.byagowi.persiancalendar.ui.utils.setupMenuNavigation
import com.byagowi.persiancalendar.ui.utils.setupUpNavigation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

abstract class ComposeFragment : Fragment() {

    @Composable
    abstract fun Content()

    open val isUpNavigation get() = false

    val title = MutableStateFlow<String?>(null)
    val subtitle = MutableStateFlow<String?>(null)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val binding = FragmentComposeBinding.inflate(inflater, container, false)
        binding.appBar.toolbar.also { toolbar ->
            title.onEach { toolbar.title = it }.launchIn(viewLifecycleOwner.lifecycleScope)
            subtitle.onEach { toolbar.subtitle = it }.launchIn(viewLifecycleOwner.lifecycleScope)
            if (isUpNavigation) toolbar.setupUpNavigation() else toolbar.setupMenuNavigation()
        }
        binding.content.setContent { ComposeTheme { Content() } }
        return binding.root
    }
}
