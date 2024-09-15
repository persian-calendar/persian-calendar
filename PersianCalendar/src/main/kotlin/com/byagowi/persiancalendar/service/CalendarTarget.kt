package com.byagowi.persiancalendar.service

import android.content.ComponentName
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.ui.MainActivity
import com.byagowi.persiancalendar.utils.createStatusIcon
import com.kieronquinn.app.smartspacer.sdk.model.SmartspaceTarget
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.TapAction
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Text
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider
import com.kieronquinn.app.smartspacer.sdk.utils.TargetTemplate

class CalendarTarget : SmartspacerTargetProvider() {

    override fun getSmartspaceTargets(smartspacerId: String): List<SmartspaceTarget> {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return emptyList()
        return listOf(
            TargetTemplate.Basic(
                id = "com.byagowi.persiancalendar_$smartspacerId",
                componentName = ComponentName(provideContext(), CalendarTarget::class.java),
                title = Text("Hello World!"),
                subtitle = Text("Example"),
                icon = com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Icon(
                    Icon.createWithResource(provideContext(), R.drawable.day18)
                ),
                onClick = TapAction(
                    intent = Intent(provideContext(), MainActivity::class.java)
                )
            ).create()
        )
    }

    override fun getConfig(smartspacerId: String?): Config {
        val date = Jdn.today().inCalendar(mainCalendar)
        val icon = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Icon.createWithBitmap(createStatusIcon(date.dayOfMonth))
        } else {
            TODO("")
        }
        return Config(
            label = provideContext().getString(R.string.app_name),
            description = provideContext().getString(R.string.today),
            icon = icon,
            refreshPeriodMinutes = 60
        )
    }

    override fun onDismiss(smartspacerId: String, targetId: String): Boolean = false

    override fun onProviderRemoved(smartspacerId: String) = Unit
}
