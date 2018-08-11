package com.byagowi.persiancalendar.adapter

import android.content.res.TypedArray
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.util.CalendarUtils
import com.byagowi.persiancalendar.util.Utils
import com.byagowi.persiancalendar.view.activity.MainActivity

class DrawerAdapter(private val mainActivity: MainActivity) : RecyclerView.Adapter<DrawerAdapter.ViewHolder>() {
  private var selectedItem: Int = 0
  private val drawerTitles: Array<String> = mainActivity.resources.getStringArray(R.array.drawerTitles)
  private val drawerSubtitles: Array<String> = mainActivity.resources.getStringArray(R.array.drawerSubtitles)
  private val drawerIcon: TypedArray = mainActivity.resources.obtainTypedArray(R.array.drawerIcons)

  @ColorInt
  private val selectedBackgroundColor: Int

  @DrawableRes
  private val selectableBackgroundResource: Int

  private val season: String
    get() {
      var isSouthernHemisphere = false
      val coordinate = Utils.getCoordinate(mainActivity)
      if (coordinate != null && coordinate.latitude < 0) {
        isSouthernHemisphere = true
      }

      var month = CalendarUtils.persianToday.month
      if (isSouthernHemisphere) month = (month + 6 - 1) % 12 + 1

      return if (month < 4)
        "SPRING"
      else if (month < 7)
        "SUMMER"
      else if (month < 10)
        "FALL"
      else
        "WINTER"
    }

  init {
    val theme = mainActivity.theme
    val selectableBackground = TypedValue()
    theme.resolveAttribute(R.attr.selectableItemBackground, selectableBackground, true)

    val selectedBackground = TypedValue()
    theme.resolveAttribute(R.attr.colorDrawerSelect, selectedBackground, true)

    selectedBackgroundColor = ContextCompat.getColor(mainActivity, selectedBackground.resourceId)
    selectableBackgroundResource = selectableBackground.resourceId
  }

  inner class ViewHolder(itemView: View, viewType: Int) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
    var itemTitle: TextView
    var itemSubtitle: TextView
    var imageView: AppCompatImageView

    init {
      if (viewType == TYPE_ITEM) {
        itemView.setOnClickListener(this)
      }

      imageView = itemView.findViewById(R.id.image)
      itemTitle = itemView.findViewById(R.id.itemTitle)
      itemSubtitle = itemView.findViewById(R.id.itemSubtitle)
    }

    override fun onClick(view: View) = mainActivity.selectItem(adapterPosition)
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = when (viewType) {
    TYPE_HEADER -> ViewHolder(LayoutInflater.from(parent.context)
        .inflate(R.layout.header_drawer, parent, false), viewType)
    else -> ViewHolder(LayoutInflater.from(parent.context)
        .inflate(R.layout.item_drawer, parent, false), viewType)
  }

  override fun onBindViewHolder(holder: DrawerAdapter.ViewHolder, position: Int) {
    if (!isPositionHeader(position)) {
      holder.itemTitle.text = drawerTitles[position - 1]

      if (drawerSubtitles[position - 1].length != 0) {
        holder.itemSubtitle.visibility = View.VISIBLE
        holder.itemSubtitle.text = drawerSubtitles[position - 1]
      } else {
        holder.itemSubtitle.visibility = View.GONE
      }

      holder.imageView.setImageResource(drawerIcon.getResourceId(position - 1, 0))

      if (selectedItem == position) {
        holder.itemView.setBackgroundColor(selectedBackgroundColor)
      } else {
        holder.itemView.setBackgroundResource(selectableBackgroundResource)
      }

    } else {

      when (season) {
        "SPRING" -> holder.imageView.setImageResource(R.drawable.spring)

        "SUMMER" -> holder.imageView.setImageResource(R.drawable.summer)

        "FALL" -> holder.imageView.setImageResource(R.drawable.fall)

        "WINTER" -> holder.imageView.setImageResource(R.drawable.winter)
      }
    }
  }

  fun setSelectedItem(item: Int) {
    selectedItem = item
    notifyDataSetChanged()
  }

  override fun getItemCount(): Int = drawerTitles.size + 1

  override fun getItemViewType(position: Int): Int = if (isPositionHeader(position)) {
    TYPE_HEADER
  } else {
    TYPE_ITEM
  }

  private fun isPositionHeader(position: Int): Boolean = position == 0

  companion object {
    private const val TYPE_HEADER = 0
    private const val TYPE_ITEM = 1
  }
}