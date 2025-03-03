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
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceService
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import java.util.Date

class MainComplicationService : ComplicationDataSourceService() {
    override fun getPreviewData(type: ComplicationType): ComplicationData? =
        if (type != ComplicationType.SHORT_TEXT) null
        else createComplicationData("شنبه", "۱ مهر").build()

    override fun onComplicationRequest(
        request: ComplicationRequest,
        listener: ComplicationRequestListener,
    ) {
        val locale = ULocale("fa_IR@calendar=persian")
        val calendar = Calendar.getInstance(locale)
        val date = Date()
        val title =
            DateFormat.getPatternInstance(calendar, DateFormat.ABBR_WEEKDAY, locale).format(date)
        val body =
            DateFormat.getPatternInstance(calendar, DateFormat.ABBR_MONTH_DAY, locale).format(date)
        val dataBuilder = createComplicationData(title, body).setTapAction(
            PendingIntent.getActivity(
                this,
                0,
                Intent(this, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
        listener.onComplicationData(dataBuilder.build())
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
