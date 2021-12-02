package com.byagowi.persiancalendar.ui.calendar.dialogs

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Base64
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.core.view.drawToBitmap
import androidx.core.view.isVisible
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.MonthOverviewItemBinding
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.entities.Theme
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.global.spacedColon
import com.byagowi.persiancalendar.ui.calendar.calendarpager.MonthView
import com.byagowi.persiancalendar.ui.utils.copyToClipboard
import com.byagowi.persiancalendar.ui.utils.getCompatDrawable
import com.byagowi.persiancalendar.ui.utils.layoutInflater
import com.byagowi.persiancalendar.ui.utils.resolveColor
import com.byagowi.persiancalendar.ui.utils.showHtml
import com.byagowi.persiancalendar.utils.dayTitleSummary
import com.byagowi.persiancalendar.utils.formatNumber
import com.byagowi.persiancalendar.utils.getEvents
import com.byagowi.persiancalendar.utils.getEventsTitle
import com.byagowi.persiancalendar.utils.isRtl
import com.byagowi.persiancalendar.utils.logException
import com.byagowi.persiancalendar.utils.monthName
import com.byagowi.persiancalendar.utils.prepareViewForRendering
import com.byagowi.persiancalendar.utils.readMonthDeviceEvents
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import io.github.persiancalendar.calendar.AbstractDate
import kotlinx.html.b
import kotlinx.html.body
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.head
import kotlinx.html.html
import kotlinx.html.img
import kotlinx.html.meta
import kotlinx.html.script
import kotlinx.html.stream.createHTML
import kotlinx.html.style
import kotlinx.html.unsafe
import java.io.ByteArrayOutputStream

fun showMonthOverviewDialog(activity: Activity, date: AbstractDate) {
    val events = createEventsList(activity, date)

    BottomSheetDialog(activity, R.style.TransparentBottomSheetDialog).also { dialog ->
        dialog.setContentView(
            RecyclerView(activity).also { recyclerView ->
                recyclerView.layoutManager = LinearLayoutManager(activity)
                recyclerView.adapter = ConcatAdapter(
                    object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
                        override fun getItemCount() = 1
                        override fun onBindViewHolder(
                            holder: RecyclerView.ViewHolder, position: Int
                        ) = Unit

                        override fun onCreateViewHolder(
                            parent: ViewGroup, viewType: Int
                        ) = object : RecyclerView.ViewHolder(FrameLayout(activity).also { root ->
                            root.addView(
                                FloatingActionButton(activity).also {
                                    it.contentDescription = "Print"
                                    it.setImageDrawable(activity.getCompatDrawable(R.drawable.ic_print))
                                    it.setOnClickListener {
                                        runCatching {
                                            activity.showHtml(createEventsReport(activity, date))
                                        }.onFailure(logException)
                                        dialog.dismiss()
                                    }
                                    it.layoutParams = FrameLayout.LayoutParams(
                                        FrameLayout.LayoutParams.MATCH_PARENT,
                                        FrameLayout.LayoutParams.WRAP_CONTENT
                                    ).also { p -> p.gravity = Gravity.CENTER_HORIZONTAL }
                                }
                            )
                            root.layoutParams = FrameLayout.LayoutParams(
                                FrameLayout.LayoutParams.MATCH_PARENT,
                                FrameLayout.LayoutParams.WRAP_CONTENT
                            )
                            root.isVisible = events.isNotEmpty()
                        }) {}
                    },
                    MonthOverviewItemAdapter(activity, events)
                )
            }
        )
    }.show()
}

private fun createEventsList(
    context: Context, date: AbstractDate, isPrint: Boolean = false
): List<Pair<Jdn, CharSequence>> {
    val baseJdn = Jdn(date)
    val deviceEvents = context.readMonthDeviceEvents(baseJdn)
    val colorTextHoliday = context.resolveColor(R.attr.colorTextHoliday)
    val events = (0 until mainCalendar.getMonthLength(date.year, date.month)).mapNotNull {
        val jdn = baseJdn + it
        val events = getEvents(jdn, deviceEvents)
        val holidays = getEventsTitle(
            events, holiday = true, compact = isPrint, showDeviceCalendarEvents = false,
            insertRLM = false, addIsHoliday = isPrint
        )
        val nonHolidays = getEventsTitle(
            events, holiday = false, compact = isPrint, showDeviceCalendarEvents = true,
            insertRLM = false, addIsHoliday = isPrint
        )
        if (holidays.isEmpty() && nonHolidays.isEmpty()) null
        else jdn to buildSpannedString {
            if (holidays.isNotEmpty()) color(colorTextHoliday) { append(holidays) }
            if (nonHolidays.isNotEmpty()) {
                if (holidays.isNotEmpty()) appendLine()
                append(nonHolidays)
            }
        }
    }
    return if (isPrint) events.map { (jdn, title) ->
        jdn to title.toString().replace("\n", " – ")
    } else events
}

private fun createEventsReport(context: Context, date: AbstractDate) = createHTML().html {
    attributes["lang"] = language.language
    attributes["dir"] = if (context.resources.isRtl) "rtl" else "ltr"
    head {
        meta(charset = "utf8")
        style {
            unsafe {
                +"""
                    body { font-family: system-ui }
                    h1, .center { text-align: center; font-size: 110% }
                """.trimIndent()
            }
        }
    }
    body {
        h1 { +language.my.format(date.monthName, formatNumber(date.year)) }
        div("center") {
            img {
                val w = 700
                val h = 300
                width = w.toString()
                height = h.toString()
                val view =
                    MonthView(ContextThemeWrapper(context, Theme.printSuitableStyle))
                view.initializeForRendering(Color.BLACK, h * 3, date, true)
                prepareViewForRendering(view, w * 3, h * 3)
                val buffer = ByteArrayOutputStream()
                view.drawToBitmap().compress(Bitmap.CompressFormat.PNG, 100, buffer)
                val base64 = Base64.encodeToString(buffer.toByteArray(), Base64.DEFAULT)
                src = "data:image/png;base64,${base64}"
            }
        }
        createEventsList(context, date, true).forEach { (jdn, title) ->
            div("two-columns") {
                b { +(formatNumber(jdn.toCalendar(mainCalendar).dayOfMonth) + spacedColon) }
                +title.toString()
            }
        }
        script { unsafe { +"print()" } }
    }
}

private class MonthOverviewItemAdapter(
    private val context: Context,
    private val rows: List<Pair<Jdn, CharSequence>>
) : RecyclerView.Adapter<MonthOverviewItemAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        MonthOverviewItemBinding.inflate(parent.context.layoutInflater, parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(position)

    override fun getItemCount(): Int = if (rows.isEmpty()) 1 else rows.size

    inner class ViewHolder(private val binding: MonthOverviewItemBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        init {
            binding.root.setOnClickListener(this)
        }

        private var title = ""

        fun bind(position: Int) = if (rows.isEmpty()) {
            binding.title.text = context.getString(R.string.warn_if_events_not_set)
            binding.body.isVisible = false
        } else rows[position].let { (jdn, body) ->
            title = dayTitleSummary(jdn, jdn.toCalendar(mainCalendar))
            binding.title.text = title
            binding.body.text = body
            binding.body.isVisible = body.isNotEmpty()
        }

        override fun onClick(v: View?) {
            if (rows.isEmpty()) return
            val (_, body) = rows[bindingAdapterPosition]
            if (body.isNotEmpty()) v?.context.copyToClipboard(title + "\n" + body)
        }
    }
}
