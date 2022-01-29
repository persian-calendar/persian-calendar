package com.byagowi.persiancalendar.ui.about

import android.animation.LayoutTransition
import android.text.method.LinkMovementMethod
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.byagowi.persiancalendar.databinding.ExpandableItemBinding
import com.byagowi.persiancalendar.ui.common.ArrowView
import com.byagowi.persiancalendar.ui.utils.layoutInflater
import com.byagowi.persiancalendar.ui.utils.setupExpandableAccessibilityDescription

class ExpandableItemsAdapter(private val sections: List<Pair<CharSequence, CharSequence>>) :
    RecyclerView.Adapter<ExpandableItemsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ExpandableItemBinding.inflate(parent.context.layoutInflater, parent, false)
    )

    override fun onBindViewHolder(holder: ExpandableItemsAdapter.ViewHolder, position: Int) =
        holder.bind(position)

    override fun getItemCount() = sections.size

    inner class ViewHolder(private val binding: ExpandableItemBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        init {
            binding.body.movementMethod = LinkMovementMethod.getInstance()
            binding.body.setOnClickListener(this)
            binding.root.layoutTransition = LayoutTransition().also {
                it.enableTransitionType(LayoutTransition.CHANGE_APPEARING)
                it.setAnimateParentHierarchy(false)
            }
            binding.root.setOnClickListener(this)
            binding.root.setupExpandableAccessibilityDescription()
        }

        fun bind(position: Int) = sections[position].let { (title, body) ->
            binding.title.text = title
            binding.body.text = body
            binding.expansionArrow.rotateTo(ArrowView.Direction.END)
            binding.body.isVisible = false
        }

        override fun onClick(v: View?) {
            val state = !binding.body.isVisible
            binding.body.isVisible = state
            binding.expansionArrow
                .animateTo(if (state) ArrowView.Direction.DOWN else ArrowView.Direction.END)
            if (state) TransitionManager.beginDelayedTransition(binding.root, ChangeBounds())
        }
    }
}
