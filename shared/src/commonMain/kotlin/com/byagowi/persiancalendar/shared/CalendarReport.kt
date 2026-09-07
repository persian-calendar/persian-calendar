package com.byagowi.persiancalendar.shared

internal fun htmlEscape(text: String): String = text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;")

internal fun calendarReport(state: CalendarAppState): String = buildString {
    val s = state.strings
    val title = state.date(state.month).substringAfter(' ')
    append("<!doctype html><html lang=\"${htmlEscape(state.language)}\" dir=\"${if (s.isRtl) "rtl" else "ltr"}\"><meta charset=\"utf-8\"><title>${htmlEscape(title)}</title>")
    append("<style>body{font:16px system-ui,sans-serif;padding:24px;color:#191c1e}table{width:100%;border-collapse:collapse}th,td{border:1px solid #aaa;padding:10px;vertical-align:top}td{height:70px}.holiday{color:#b3261e}small{display:block;margin-top:6px;font-size:12px}@media print{button{display:none}body{padding:0}}</style>")
    append("<h1>${htmlEscape(title)}</h1><button onclick=\"window.print()\">${htmlEscape(s["print"])}</button><table><thead><tr>")
    repeat(7) { append("<th>${htmlEscape(s[weekdayKeys[(state.weekStart + it) % 7]])}</th>") }
    append("</tr></thead><tbody>")
    val start = state.month - (weekday(state.month) - state.weekStart).mod(7)
    val end = state.calendar.monthStart(state.month, 1)
    val rows = ((end - start + 6) / 7).toInt()
    repeat(rows) { row ->
        append("<tr>")
        repeat(7) { column ->
            val day = start + row * 7 + column
            if (day !in state.month until end) append("<td></td>") else {
                val events = state.events(day)
                append("<td${if (weekday(day) == 6 || events.any { it.holiday }) " class=\"holiday\"" else ""}>${state.numeral.format(state.calendar.date(day).dayOfMonth)}")
                events.forEach { append("<small>${htmlEscape(it.title)}</small>") }
                append("</td>")
            }
        }
        append("</tr>")
    }
    append("</tbody></table></html>")
}
