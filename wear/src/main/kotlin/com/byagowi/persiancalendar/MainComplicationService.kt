package com.byagowi.persiancalendar

import android.app.PendingIntent
import android.content.Intent
import android.icu.text.DateFormat
import android.icu.util.Calendar
import android.icu.util.ULocale
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.PlainComplicationText
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
    override fun getPreviewData(type: ComplicationType): ComplicationData? =
        if (type != ComplicationType.SHORT_TEXT) null
        else createComplicationData("شنبه", "۱ مهر").build()

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData {
        val locale = ULocale("fa_IR@calendar=persian")
        val calendar = Calendar.getInstance(locale)
        val date = Date()
        val preferences = dataStore.data.firstOrNull()
        val title = run {
            val format = if (preferences?.get(complicationWeekdayInitial) == true) "EEEEE"
            else DateFormat.WEEKDAY
            DateFormat.getPatternInstance(calendar, format, locale).format(date)
        }
        val body = run {
            val format =
                if (preferences?.get(complicationMonthNumber) == true) DateFormat.NUM_MONTH_DAY
                else DateFormat.ABBR_MONTH_DAY
            DateFormat.getPatternInstance(calendar, format, locale).format(date)
        }
        val dataBuilder = createComplicationData(title, body).setTapAction(
            PendingIntent.getActivity(
                this,
                0,
                Intent(this, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
        dataBuilder.setValidTimeRange(run {
            val now = ZonedDateTime.now(ZoneId.systemDefault())
            val endOfDay = now.toLocalDate().atTime(23, 59, 59, 999_999_999)
                .atZone(ZoneId.systemDefault())
                .toInstant()
            TimeRange.between(Instant.now(), endOfDay)
        })
        return dataBuilder.build()
    }

    private fun createComplicationData(
        title: String, body: String
    ): ShortTextComplicationData.Builder {
        return ShortTextComplicationData.Builder(
            text = PlainComplicationText.Builder(body).build(),
            contentDescription = PlainComplicationText.Builder(body).build()
        ).setTitle(PlainComplicationText.Builder(title).build())
    }
}
