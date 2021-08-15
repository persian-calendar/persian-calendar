package com.byagowi.persiancalendar.ui.about

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
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
        val context = binding.root.context

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
                paint.color = context.resolveColor(R.attr.colorDivider)
                canvas.drawRoundRect(rect, 5.sp, 5.sp, paint)
                paint.color = context.resolveColor(R.attr.colorTextDrawer)
                canvas.drawText(text ?: "", start, end, x + sidePadding, y.toFloat(), paint)
            }
        }

        val sections = resources.openRawResource(R.raw.credits).use { String(it.readBytes()) }
            .split(Regex("^-{4}$", RegexOption.MULTILINE))
            .map { it.trim().lines() }
            .map { lines ->
                val title = buildSpannedString {
                    val parts = lines.first().split(" - ")
                    append(parts[0])
                    if (parts.size > 1) {
                        append("  ")
                        scale(.7f) { inSpans(BadgeSpan()) { append(parts.getOrNull(1)) } }
                    }
                }
                val body = SpannableString(lines.drop(1).joinToString("\n").trim()).also {
                    Linkify.addLinks(it, Linkify.WEB_URLS or Linkify.EMAIL_ADDRESSES)
                }
                title to body
            }
        binding.recyclerView.adapter = ExpandableItemsAdapter(sections)

        val layoutManager = LinearLayoutManager(context)
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.addItemDecoration(
            DividerItemDecoration(context, layoutManager.orientation)
        )
        return binding.root
    }
}
