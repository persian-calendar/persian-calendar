package com.byagowi.persiancalendar.ui.about

import android.animation.LayoutTransition
import android.content.res.ColorStateList
import android.text.method.LinkMovementMethod
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.ExpandableItemBinding
import com.byagowi.persiancalendar.ui.shared.ArrowView
import com.byagowi.persiancalendar.utils.dp
import com.byagowi.persiancalendar.utils.layoutInflater
import com.byagowi.persiancalendar.utils.resolveColor
import com.byagowi.persiancalendar.utils.setupExpandableAccessibilityDescription
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel

class ExpandableItemsAdapter(private val sections: List<Item>) :
    RecyclerView.Adapter<ExpandableItemsAdapter.ViewHolder>() {

    data class Item(val title: String, val tag: String?, val content: CharSequence)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ExpandableItemBinding.inflate(parent.context.layoutInflater, parent, false)
    )

    override fun onBindViewHolder(holder: ExpandableItemsAdapter.ViewHolder, position: Int) =
        holder.bind(position)

    override fun getItemCount() = sections.size

    private val changeBounds = ChangeBounds()

    inner class ViewHolder(private val binding: ExpandableItemBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        init {
            binding.content.movementMethod = LinkMovementMethod.getInstance()
            binding.content.setOnClickListener(this)
            binding.root.layoutTransition = LayoutTransition().also {
                it.enableTransitionType(LayoutTransition.CHANGE_APPEARING)
                it.setAnimateParentHierarchy(false)
            }
            binding.tag.background =
                MaterialShapeDrawable(ShapeAppearanceModel().withCornerSize(6.dp)).also {
                    val tagColor = binding.root.context.resolveColor(R.attr.colorDivider)
                    it.tintList = ColorStateList.valueOf(tagColor)
                }
            binding.tag.setTextColor(binding.root.context.resolveColor(R.attr.colorTextDrawer))
            binding.root.setOnClickListener(this)
            binding.root.setupExpandableAccessibilityDescription()
        }

        fun bind(position: Int) {
            binding.title.text = sections[position].title
            binding.tag.text = sections[position].tag
            binding.tag.isVisible = !sections[position].tag.isNullOrEmpty()
            binding.content.text = sections[position].content
            binding.expansionArrow.changeTo(ArrowView.Direction.END)
            binding.content.isVisible = false
        }

        override fun onClick(v: View?) {
            val state = !binding.content.isVisible
            binding.content.isVisible = state
            binding.expansionArrow
                .animateTo(if (state) ArrowView.Direction.DOWN else ArrowView.Direction.END)
            if (state) TransitionManager.beginDelayedTransition(binding.root, changeBounds)
        }
    }
}
