package com.byagowi.persiancalendar.ui.settings

import android.graphics.Color
import android.view.ViewGroup
import androidx.core.view.setMargins
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ui.utils.dp
import com.byagowi.persiancalendar.utils.createStatusIcon
import com.byagowi.persiancalendar.utils.getDayIconResource
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.android.material.shape.TriangleEdgeTreatment

// Debug only dialog to check validity of dynamic icons generation
fun showIconsDemoDialog(activity: FragmentActivity) {
    MaterialAlertDialogBuilder(activity)
        .setView(RecyclerView(activity).also {
            it.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
                override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {}
                override fun getItemCount() = 62
                override fun getItemViewType(position: Int) = position
                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
                    object : RecyclerView.ViewHolder(ShapeableImageView(activity).apply {
                        val day = viewType / 2 + 1
                        when (viewType % 2) {
                            0 -> setImageResource(getDayIconResource(day))
                            1 -> setImageBitmap(createStatusIcon(day))
                        }
                        layoutParams = ViewGroup.MarginLayoutParams(36.dp.toInt(), 36.dp.toInt())
                            .apply { setMargins(4.dp.toInt()) }
                        shapeAppearanceModel = ShapeAppearanceModel.Builder()
                            .setAllCorners(CornerFamily.ROUNDED, 8.dp)
                            .setAllEdges(TriangleEdgeTreatment(4.dp, true))
                            .build()
                        setBackgroundColor(Color.DKGRAY)
                    }) {}
            }
            it.layoutManager = GridLayoutManager(activity, 8)
            it.setBackgroundColor(Color.WHITE)
        })
        .setNegativeButton(R.string.cancel, null)
        .show()
}
