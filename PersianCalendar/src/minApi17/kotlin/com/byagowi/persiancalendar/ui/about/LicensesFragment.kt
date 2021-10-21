package com.byagowi.persiancalendar.ui.about

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ReplacementSpan
import android.text.util.Linkify
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.buildSpannedString
import androidx.core.text.inSpans
import androidx.core.text.scale
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.FragmentLicensesBinding
import com.byagowi.persiancalendar.ui.utils.resolveColor
import com.byagowi.persiancalendar.ui.utils.setupUpNavigation
import com.byagowi.persiancalendar.ui.utils.sp
import kotlin.math.roundToInt

class LicensesFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val binding = FragmentLicensesBinding.inflate(inflater, container, false)
        binding.appBar.toolbar.let {
            it.setTitle(R.string.about_license_title)
            it.setupUpNavigation()
        }

        binding.railView.menu.also {
            fun createTextIcon(text: String): Drawable {
                val paint = Paint(Paint.ANTI_ALIAS_FLAG)
                paint.textSize = 40f
                val bounds = Rect()
                paint.color = Color.WHITE
                paint.getTextBounds(text, 0, text.length, bounds)
                val height = bounds.height()
                val bitmap = Bitmap.createBitmap(bounds.width(), height, Bitmap.Config.ARGB_8888)
                Canvas(bitmap).drawText(text, 0f, height.toFloat(), paint)
                return BitmapDrawable(layoutInflater.context.resources, bitmap)
            }

            it.add("GPLv3").setIcon(R.drawable.ic_info)
            it.add("" + KotlinVersion.CURRENT).icon = createTextIcon("Kotlin")
            it.add("API " + Build.VERSION.SDK_INT).setIcon(R.drawable.ic_motorcycle)
        }

        // Based on https://stackoverflow.com/a/34623367
        class BadgeSpan : ReplacementSpan() {
            private val sidePadding = 3.sp

            override fun getSize(
                paint: Paint, text: CharSequence?, start: Int, end: Int, fm: Paint.FontMetricsInt?
            ) = (paint.measureText(text, start, end) + sidePadding * 2).roundToInt()

            override fun draw(
                canvas: Canvas, text: CharSequence?, start: Int, end: Int, x: Float, top: Int,
                y: Int, bottom: Int, paint: Paint
            ) {
                val verticalReduce = 5.sp
                val rect = RectF(
                    x, top + verticalReduce,
                    x + getSize(paint, text, start, end, null), bottom - verticalReduce
                )
                paint.color = inflater.context.resolveColor(R.attr.colorDivider)
                canvas.drawRoundRect(rect, 5.sp, 5.sp, paint)
                paint.color = inflater.context.resolveColor(R.attr.colorTextDrawer)
                canvas.drawText(text ?: "", start, end, x + sidePadding, y.toFloat(), paint)
            }
        }

        val sections = resources.getCreditsSections().map { (title, license, text) ->
            buildSpannedString {
                append(title)
                if (license != null) {
                    append("  ")
                    scale(.7f) { inSpans(BadgeSpan()) { append(license) } }
                }
            } to SpannableString(text).also {
                Linkify.addLinks(it, Linkify.WEB_URLS or Linkify.EMAIL_ADDRESSES)
            }
        }

        binding.recyclerView.adapter = ExpandableItemsAdapter(sections)
        val layoutManager = LinearLayoutManager(context)
        val itemDecoration = DividerItemDecoration(context, layoutManager.orientation)
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.addItemDecoration(itemDecoration)
        return binding.root
    }
}
