package com.byagowi.persiancalendar.ui.calendar.dialogs

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.byagowi.persiancalendar.EN_DASH
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.MonthOverviewItemBinding
import com.byagowi.persiancalendar.entities.CalendarEvent
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.eventsRepository
import com.byagowi.persiancalendar.global.isShowWeekOfYearEnabled
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.global.secondaryCalendar
import com.byagowi.persiancalendar.global.secondaryCalendarDigits
import com.byagowi.persiancalendar.global.spacedColon
import com.byagowi.persiancalendar.ui.utils.copyToClipboard
import com.byagowi.persiancalendar.ui.utils.getCompatDrawable
import com.byagowi.persiancalendar.ui.utils.layoutInflater
import com.byagowi.persiancalendar.ui.utils.openHtmlInBrowser
import com.byagowi.persiancalendar.ui.utils.resolveColor
import com.byagowi.persiancalendar.utils.applyAppLanguage
import com.byagowi.persiancalendar.utils.applyWeekStartOffsetToWeekDay
import com.byagowi.persiancalendar.utils.calendarType
import com.byagowi.persiancalendar.utils.dayTitleSummary
import com.byagowi.persiancalendar.utils.formatNumber
import com.byagowi.persiancalendar.utils.getEventsTitle
import com.byagowi.persiancalendar.utils.getShiftWorkTitle
import com.byagowi.persiancalendar.utils.getWeekDayName
import com.byagowi.persiancalendar.utils.isRtl
import com.byagowi.persiancalendar.utils.logException
import com.byagowi.persiancalendar.utils.monthFormatForSecondaryCalendar
import com.byagowi.persiancalendar.utils.monthName
import com.byagowi.persiancalendar.utils.readMonthDeviceEvents
import com.byagowi.persiancalendar.utils.revertWeekStartOffsetFromWeekDay
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import io.github.persiancalendar.calendar.AbstractDate
import kotlinx.html.DIV
import kotlinx.html.body
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.head
import kotlinx.html.html
import kotlinx.html.meta
import kotlinx.html.script
import kotlinx.html.small
import kotlinx.html.span
import kotlinx.html.stream.createHTML
import kotlinx.html.style
import kotlinx.html.sub
import kotlinx.html.sup
import kotlinx.html.table
import kotlinx.html.td
import kotlinx.html.th
import kotlinx.html.tr
import kotlinx.html.unsafe

fun showMonthOverviewDialog(activity: FragmentActivity, date: AbstractDate) {
    applyAppLanguage(activity)
    val events = createEventsList(activity, date)

    BottomSheetDialog(activity, R.style.TransparentBottomSheetDialog).also { dialog ->
        fun showPrintReport(isLongClick: Boolean) {
            runCatching {
                activity.openHtmlInBrowser(
                    createEventsReport(activity, date, wholeYear = isLongClick)
                )
            }.onFailure(logException)
            dialog.dismiss()
            createEventsReport(activity, date, wholeYear = isLongClick)
        }

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
                                    it.setOnClickListener { showPrintReport(isLongClick = false) }
                                    it.setOnLongClickListener {
                                        showPrintReport(isLongClick = true)
                                        true
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
                        }) {}
                    },
                    MonthOverviewItemAdapter(
                        activity,
                        formatEventsList(events, false, activity.resolveColor(R.attr.colorHoliday)),
                    )
                )
            }
        )
    }.show()
}

private fun createEventsList(
    context: Context, date: AbstractDate
): Map<Jdn, List<CalendarEvent<*>>> {
    val baseJdn = Jdn(date)
    val deviceEvents = context.readMonthDeviceEvents(baseJdn)
    return (0 until mainCalendar.getMonthLength(date.year, date.month))
        .map { baseJdn + it }
        .associateWith { eventsRepository?.getEvents(it, deviceEvents) ?: emptyList() }
}

private fun formatEventsList(
    events: Map<Jdn, List<CalendarEvent<*>>>, isPrint: Boolean, @ColorInt holidayColor: Int
): List<Pair<Jdn, CharSequence>> {
    val result = events.toList().sortedBy { (jdn, _) -> jdn.value }.mapNotNull { (jdn, events) ->
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
            if (holidays.isNotEmpty()) color(holidayColor) { append(holidays) }
            if (nonHolidays.isNotEmpty()) {
                if (holidays.isNotEmpty()) appendLine()
                append(nonHolidays)
            }
        }
    }
    return if (isPrint) result.map { (jdn, title) ->
        jdn to title.toString().replace("\n", " $EN_DASH ")
    } else result
}

private fun createEventsReport(
    context: Context, date: AbstractDate, wholeYear: Boolean
) = createHTML().html {
    attributes["lang"] = language.language
    attributes["dir"] = if (context.resources.isRtl) "rtl" else "ltr"
    head {
        meta(charset = "utf8")
        style {
            unsafe {
                val calendarColumnsPercent = 100 / if (isShowWeekOfYearEnabled) 8 else 7
                +"""
                    body { font-family: system-ui }
                    td { vertical-align: top }
                    table.calendar td, table.calendar th {
                        width: $calendarColumnsPercent%;
                        text-align: center;
                        height: 2em;
                    }
                    .holiday { color: red; font-weight: bold }
                    .hasEvents { border-bottom: 1px dotted; }
                    table.events { padding: 1em 0; font-size: 95% }
                    table.events td { width: 50%; padding: 0 1em }
                    table { width: 100% }
                    h1 { text-align: center }
                    .page { break-after: page }
                    sup { font-size: x-small; position: absolute }
                """.trimIndent()
            }
        }
    }
    body {
        (if (wholeYear) {
            val calendar = date.calendarType
            (1..calendar.getYearMonths(date.year)).map { calendar.createDate(date.year, it, 1) }
        } else listOf(date)).forEach { div("page") { generateMonthPage(context, it) } }
        script { unsafe { +"print()" } }
    }
}

private fun DIV.generateMonthPage(context: Context, date: AbstractDate) {
    val events = createEventsList(context, date)
    fun generateDayClasses(jdn: Jdn, weekEndsAsHoliday: Boolean): String {
        val dayEvents = events[jdn] ?: emptyList()
        return listOf(
            "holiday" to ((jdn.isWeekEnd() && weekEndsAsHoliday) || dayEvents.any { it.isHoliday }),
            "hasEvents" to dayEvents.isNotEmpty()
        ).filter { it.second }.joinToString(" ") { it.first }
    }
    h1 {
        +language.my.format(date.monthName, formatNumber(date.year))
        val title = monthFormatForSecondaryCalendar(date, secondaryCalendar ?: return@h1)
        small { +" ($title)" }
    }
    table("calendar") {
        tr {
            if (isShowWeekOfYearEnabled) th {}
            (0..6).forEach { th { +getWeekDayName(revertWeekStartOffsetFromWeekDay(it)) } }
        }
        val monthLength = date.calendarType.getMonthLength(date.year, date.month)
        val monthStartJdn = Jdn(date)
        val startingDayOfWeek = monthStartJdn.dayOfWeek
        val fixedStartingDayOfWeek = applyWeekStartOffsetToWeekDay(startingDayOfWeek)
        val startOfYearJdn = Jdn(date.calendarType, date.year, 1, 1)
        (0 until (6 * 7)).map {
            val index = it - fixedStartingDayOfWeek
            if (index !in (0 until monthLength)) return@map null
            (index + 1) to (monthStartJdn + index)
        }.chunked(7).map { row ->
            val firstJdnInWeek = row.firstNotNullOfOrNull { it?.second/*jdn*/ } ?: return@map
            tr {
                if (isShowWeekOfYearEnabled) {
                    val weekOfYear = firstJdnInWeek.getWeekOfYear(startOfYearJdn)
                    th { sub { small { +formatNumber(weekOfYear) } } }
                }
                row.map { pair ->
                    td {
                        val (dayOfMonth, jdn) = pair ?: return@td
                        span(generateDayClasses(jdn, true)) {
                            +formatNumber(dayOfMonth)
                        }
                        listOfNotNull(
                            secondaryCalendar?.let {
                                val secondaryDateDay = jdn.toCalendar(it).dayOfMonth
                                val digits = secondaryCalendarDigits
                                formatNumber(secondaryDateDay, digits)
                            },
                            getShiftWorkTitle(jdn, false).takeIf { it.isNotEmpty() }
                        ).joinToString(" ").takeIf { it.isNotEmpty() }?.let { sup { +" $it" } }
                    }
                }
            }
        }
    }
    table("events") {
        tr {
            val titles = formatEventsList(events, true, Color.RED)
            if (titles.isEmpty()) return@tr
            val sizes = titles.map { it.second.toString().length }
                .runningFold(0) { acc, it -> acc + it }
            val halfOfTotal = sizes.last() / 2
            val center = sizes.indexOfFirst { it > halfOfTotal }
            listOf(titles.take(center), titles.drop(center)).forEach {
                td {
                    it.forEach { (jdn, title) ->
                        div {
                            span(generateDayClasses(jdn, false)) {
                                +formatNumber(jdn.toCalendar(mainCalendar).dayOfMonth)
                            }
                            +spacedColon
                            +title.toString()
                        }
                    }
                }
            }
        }
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
