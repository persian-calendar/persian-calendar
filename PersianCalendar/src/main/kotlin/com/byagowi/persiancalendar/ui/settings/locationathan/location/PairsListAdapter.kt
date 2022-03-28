package com.byagowi.persiancalendar.ui.settings.locationathan.location

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.byagowi.persiancalendar.databinding.ListPairsItemBinding
import com.byagowi.persiancalendar.ui.utils.layoutInflater

class PairsListAdapter(
    private val onItemClicked: (index: Int) -> Unit, private val items: List<Pair<String, String>>
) : RecyclerView.Adapter<PairsListAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ListPairsItemBinding.inflate(parent.context.layoutInflater, parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(items[position])

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(private val binding: ListPairsItemBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        fun bind(item: Pair<String, String>) {
            binding.root.setOnClickListener(this)
            binding.first.text = item.first
            binding.second.text = item.second
        }

        override fun onClick(view: View) = onItemClicked(bindingAdapterPosition)
    }
}
