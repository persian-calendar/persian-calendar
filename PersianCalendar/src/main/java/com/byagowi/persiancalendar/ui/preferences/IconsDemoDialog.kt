package com.byagowi.persiancalendar.ui.preferences

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.utils.createStatusIcon
import com.byagowi.persiancalendar.utils.dp
import com.byagowi.persiancalendar.utils.getDayIconResource

// Debug only dialog to check validity of dynamic icons generation
fun Fragment.showIconsDemoDialog() {
    val context = layoutInflater.context
    AlertDialog.Builder(context)
        .setView(RecyclerView(context).also {
            it.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
                override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {}
                override fun getItemCount() = 62
                override fun getItemViewType(position: Int) = position
                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
                    object : RecyclerView.ViewHolder(FrameLayout(context).also { frameLayout ->
                        frameLayout.setPadding(4.dp)
                        frameLayout.addView(ImageView(context).also { imageView ->
                            val gradientDrawable = GradientDrawable()
                            gradientDrawable.cornerRadius = 10.dp.toFloat()
                            gradientDrawable.setColor(Color.BLACK)
                            imageView.background = gradientDrawable
                            imageView.layoutParams = ViewGroup.LayoutParams(36.dp, 36.dp)
                        })
                        frameLayout.addView(ImageView(context).also { imageView ->
                            val day = viewType / 2 + 1
                            if (viewType % 2 == 0) {
                                imageView.setImageResource(getDayIconResource(day))
                            } else {
                                imageView.setImageBitmap(createStatusIcon(context, day))
                            }
                            imageView.layoutParams = ViewGroup.LayoutParams(36.dp, 36.dp)
                        })
                    }) {}
            }
            it.layoutManager = GridLayoutManager(context, 8)
            it.setBackgroundColor(Color.WHITE)
        })
        .setNegativeButton(R.string.cancel, null)
        .show()
}
