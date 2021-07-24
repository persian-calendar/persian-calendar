package com.byagowi.persiancalendar.ui.about

import android.animation.LayoutTransition
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.FragmentLicensesBinding
import com.byagowi.persiancalendar.databinding.LicenseItemBinding
import com.byagowi.persiancalendar.utils.dp
import com.byagowi.persiancalendar.utils.layoutInflater
import com.byagowi.persiancalendar.utils.setupUpNavigation
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel

class LicensesFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ) = FragmentLicensesBinding.inflate(inflater, container, false).also { binding ->
        binding.appBar.toolbar.let {
            it.setTitle(R.string.about_license_title)
            it.setupUpNavigation()
        }

        val text = resources.openRawResource(R.raw.credits).use { String(it.readBytes()) }
        binding.recyclerView.adapter = LicensesAdapter(Regex("\n-{4}\n").split(text).map {
            val lines = it.lines()
            val parts = lines.first().split(" - ")
            val content = SpannableString(lines.drop(1).joinToString("\n").trim())
            Linkify.addLinks(content, Linkify.WEB_URLS or Linkify.EMAIL_ADDRESSES)
            LicensesAdapter.Item(parts[0], parts.getOrNull(1), content)
        })
        val layoutManager = LinearLayoutManager(context)
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.addItemDecoration(
            DividerItemDecoration(context, layoutManager.orientation)
        )
    }.root
}

private class LicensesAdapter(private val sections: List<Item>) :
    ListAdapter<LicensesAdapter.Item, LicensesAdapter.ViewHolder>(
        object : DiffUtil.ItemCallback<Item>() {
            override fun areItemsTheSame(old: Item, new: Item) = old.title == new.title
            override fun areContentsTheSame(old: Item, new: Item) = old == new
        }
    ) {

    data class Item(val title: String, val license: String?, val content: CharSequence)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        LicenseItemBinding.inflate(parent.context.layoutInflater, parent, false)
    )

    override fun onBindViewHolder(holder: LicensesAdapter.ViewHolder, position: Int) =
        holder.bind(position)

    override fun getItemCount() = sections.size

    inner class ViewHolder(private val binding: LicenseItemBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        init {
            binding.root.setOnClickListener(this)
            binding.content.movementMethod = LinkMovementMethod.getInstance()
            binding.content.setOnClickListener(this)
            binding.root.layoutTransition = LayoutTransition().also {
                it.enableTransitionType(LayoutTransition.CHANGE_APPEARING)
                it.setAnimateParentHierarchy(false)
            }
            binding.license.background =
                MaterialShapeDrawable(ShapeAppearanceModel().withCornerSize(6.dp)).also {
                    it.tintList = ColorStateList.valueOf(0x10000000)
                }
        }

        fun bind(position: Int) {
            binding.title.text = sections[position].title
            binding.license.text = sections[position].license
            binding.license.isVisible = !sections[position].license.isNullOrEmpty()
            binding.content.text = sections[position].content
            binding.sectionIcon.rotation = -90f
        }

        private val arrowRotationAnimationDuration =
            binding.root.resources.getInteger(android.R.integer.config_shortAnimTime).toLong()

        override fun onClick(v: View?) {
            binding.content.isVisible = !binding.content.isVisible
            binding.sectionIcon.animate()
                .rotation(if (binding.content.isVisible) 0f else -90f)
                .setDuration(arrowRotationAnimationDuration)
                .start()
        }
    }
}
