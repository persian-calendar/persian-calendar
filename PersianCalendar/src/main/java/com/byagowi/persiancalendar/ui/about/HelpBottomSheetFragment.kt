package com.byagowi.persiancalendar.ui.about

import android.content.res.Configuration
import android.graphics.Color
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import android.text.util.Linkify
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.byagowi.persiancalendar.databinding.FragmentHelpBottomSheetBinding

class HelpBottomSheetFragment : BottomSheetDialogFragment() {
    private var mainBinding: FragmentHelpBottomSheetBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = FragmentHelpBottomSheetBinding.inflate(inflater, container, false).also { binding ->
        mainBinding = binding
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Linkify.addLinks(mainBinding?.helpSummary!!, Linkify.WEB_URLS or Linkify.EMAIL_ADDRESSES)
        (view.parent as View).setBackgroundColor(Color.TRANSPARENT)
        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            val parent = view.parent as View
            val layoutParams = parent.layoutParams as CoordinatorLayout.LayoutParams
            layoutParams.setMargins(16, 0, 16, 0)
            parent.layoutParams = layoutParams
        }
    }

    companion object {
        fun newInstance(): HelpBottomSheetFragment {
            return HelpBottomSheetFragment()
        }
    }
}
