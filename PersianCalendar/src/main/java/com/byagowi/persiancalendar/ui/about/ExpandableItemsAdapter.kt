package com.byagowi.persiancalendar.ui.about

import android.animation.LayoutTransition
import android.content.res.ColorStateList
import android.text.method.LinkMovementMethod
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.byagowi.persiancalendar.databinding.ExpandableItemBinding
import com.byagowi.persiancalendar.utils.dp
import com.byagowi.persiancalendar.utils.isTalkBackEnabled
import com.byagowi.persiancalendar.utils.layoutInflater
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel

class ExpandableItemsAdapter(private val sections: List<Item>, isRtl: Boolean) :
    RecyclerView.Adapter<ExpandableItemsAdapter.ViewHolder>() {

    data class Item(val title: String, val tag: String?, val content: CharSequence)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ExpandableItemBinding.inflate(parent.context.layoutInflater, parent, false)
    )

    override fun onBindViewHolder(holder: ExpandableItemsAdapter.ViewHolder, position: Int) =
        holder.bind(position)

    override fun getItemCount() = sections.size

    val notExpandedArrowDegree = if (isRtl) 90f else -90f

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
                    it.tintList = ColorStateList.valueOf(0x10000000)
                }

            if (!isTalkBackEnabled) binding.root.setOnClickListener(this)
            else binding.content.isVisible = true // make it expanded if user got talkback
        }

        fun bind(position: Int) {
            binding.title.text = sections[position].title
            binding.tag.text = sections[position].tag
            binding.tag.isVisible = !sections[position].tag.isNullOrEmpty()
            binding.content.text = sections[position].content
            binding.sectionIcon.rotation = notExpandedArrowDegree
        }

        private val arrowRotationAnimationDuration =
            binding.root.resources.getInteger(android.R.integer.config_shortAnimTime).toLong()

        override fun onClick(v: View?) {
            binding.content.isVisible = !binding.content.isVisible
            binding.sectionIcon.animate()
                .rotation(if (binding.content.isVisible) 0f else notExpandedArrowDegree)
                .setDuration(arrowRotationAnimationDuration)
                .start()
        }
    }
}
