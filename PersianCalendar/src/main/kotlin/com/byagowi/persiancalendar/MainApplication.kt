package com.byagowi.persiancalendar

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Resources
import com.byagowi.persiancalendar.global.initGlobal
import com.byagowi.persiancalendar.service.BroadcastReceivers
import com.byagowi.persiancalendar.utils.update
import com.whatsapp.stringpacks.StringPackResources
import com.whatsapp.stringpacks.StringPacks

class MainApplication : Application() {
    override fun attachBaseContext(base: Context?) {
        registerStringPackIds()
        if (base != null) StringPacks.getInstance().setUp(base)
        super.attachBaseContext(base)
    }

    private var stringPackResources: Resources? = null
    override fun getResources(): Resources {
        if (stringPackResources == null) {
            stringPackResources = StringPackResources.wrap(super.getResources())
        }
        return stringPackResources!!
    }

    override fun onCreate() {
        super.onCreate()
        initGlobal(this) // mostly used for things should be provided in locale level
        update(this, true)

        val intentFilter = IntentFilter().apply {
            addAction(Intent.ACTION_DATE_CHANGED)
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
            addAction(Intent.ACTION_TIME_CHANGED)
            addAction(Intent.ACTION_SCREEN_ON)
            // addAction(Intent.ACTION_TIME_TICK)
        }
        registerReceiver(receiver, intentFilter)
    }

    private val receiver = BroadcastReceivers()
}
