package com.byagowi.persiancalendar

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.icu.text.DateFormat
import android.icu.util.Calendar
import android.icu.util.ULocale
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.RangedValueComplicationData
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.data.TimeRange
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.byagowi.persiancalendar.ui.MainActivity
import kotlinx.coroutines.flow.firstOrNull
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Date

class MainComplicationService : SuspendingComplicationDataSourceService() {
    override fun getPreviewData(type: ComplicationType) =
        createComplicationData("شنبه", "۱ مهر").build()

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData {
        val locale = ULocale("fa_IR@calendar=persian")
        val calendar = Calendar.getInstance(locale)
        val date = Date()
        val preferences = dataStore.data.firstOrNull()
        val hideWeekDay = preferences?.get(complicationHideWeekDay) == true
        val title = when {
            hideWeekDay -> DateFormat.DAY
            preferences?.get(complicationWeekdayInitial) == true -> "EEEEE"
            else -> DateFormat.WEEKDAY
        }.let { DateFormat.getPatternInstance(calendar, it, locale).format(date) }
        val body = when {
            hideWeekDay -> DateFormat.MONTH
            preferences?.get(complicationMonthNumber) == true -> DateFormat.NUM_MONTH_DAY
            else -> DateFormat.ABBR_MONTH_DAY
        }.let { DateFormat.getPatternInstance(calendar, it, locale).format(date) }
        val dataBuilder = createComplicationData(title, body)
        dataBuilder.setTapAction(getTapAction())
        dataBuilder.setValidTimeRange(getValidTimeRange())
        return dataBuilder.build()
    }

    private fun createComplicationData(
        title: String,
        body: String,
    ): ShortTextComplicationData.Builder {
        return ShortTextComplicationData.Builder(
            text = PlainComplicationText.Builder(body).build(),
            contentDescription = PlainComplicationText.Builder("$title\n$body").build()
        ).setTitle(PlainComplicationText.Builder(title).build())
    }
}

class MonthComplicationService : SuspendingComplicationDataSourceService() {
    override fun getPreviewData(type: ComplicationType) =
        createRangedValueComplicationData("۱ مهر", 20, 30).build()

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData {
        val locale = ULocale("fa_IR@calendar=persian")
        val calendar = Calendar.getInstance(locale)
        val formatter = DateFormat.getPatternInstance(calendar, DateFormat.ABBR_MONTH_DAY, locale)
        val title = formatter.format(Date())
        val date = Jdn.today().toPersianDate()
        val endOfMonth = Jdn(date.monthStartOfMonthsDistance(1)) - 1
        val dataBuilder = createRangedValueComplicationData(
            title = title,
            dayOfMonth = date.dayOfMonth,
            monthDays = endOfMonth.toPersianDate().dayOfMonth,
        )
        dataBuilder.setTapAction(getTapAction())
        dataBuilder.setValidTimeRange(getValidTimeRange())
        return dataBuilder.build()
    }

    private fun createRangedValueComplicationData(
        title: String,
        dayOfMonth: Int,
        monthDays: Int,
    ): RangedValueComplicationData.Builder {
        val title = PlainComplicationText.Builder(title).build()
        return RangedValueComplicationData.Builder(
            min = 1f,
            value = dayOfMonth.toFloat(),
            max = monthDays.toFloat(),
            contentDescription = title,
        ).setTitle(title)
    }
}

@JvmSynthetic
private fun Context.getTapAction(): PendingIntent? {
    return PendingIntent.getActivity(
        this,
        0,
        Intent(this, MainActivity::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
}

@JvmSynthetic
private fun getValidTimeRange(): TimeRange {
    val zoneId = ZoneId.systemDefault()
    val now = ZonedDateTime.now(zoneId)
    val date = now.toLocalDate()
    val endOfDay = date.atTime(23, 59, 59, 999_999_999)
    val zonedEndOfDay = endOfDay.atZone(zoneId).toInstant()
    return TimeRange.between(Instant.now(), zonedEndOfDay)
}
