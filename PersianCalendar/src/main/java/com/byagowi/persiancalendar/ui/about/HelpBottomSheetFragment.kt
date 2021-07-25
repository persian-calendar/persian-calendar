package com.byagowi.persiancalendar.ui.about

import android.content.res.Configuration
import android.graphics.Color
import android.text.util.Linkify
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import com.byagowi.persiancalendar.ReleaseDebugDifference.debugAssertNotNull
import com.byagowi.persiancalendar.databinding.FragmentHelpBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialog

fun Fragment.showHelpBottomSheet() {
    val binding = FragmentHelpBottomSheetBinding.inflate(layoutInflater)
    Linkify.addLinks(binding.helpSummary, Linkify.WEB_URLS or Linkify.EMAIL_ADDRESSES)
    BottomSheetDialog(layoutInflater.context).also { it.setContentView(binding.root) }.show()
    val root = (binding.root.parent as? View).debugAssertNotNull ?: return
    root.setBackgroundColor(Color.TRANSPARENT)
    if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
        (root.layoutParams as? CoordinatorLayout.LayoutParams).debugAssertNotNull
            ?.setMargins(16, 0, 16, 0)
    }
}
