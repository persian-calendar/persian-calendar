package com.byagowi.persiancalendar

import android.content.ComponentName
import android.content.Context
import androidx.wear.tiles.TileService.getUpdater
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester

fun Context.requestComplicationUpdate() {
    val component = ComponentName(this, MainComplicationService::class.java)
    ComplicationDataSourceUpdateRequester.create(this, component).requestUpdateAll()
}

fun Context.requestTileUpdate() {
    val updater = getUpdater(this)
    updater.requestUpdate(MainTileService::class.java)
    updater.requestUpdate(MonthTileService::class.java)
}

// It's possible to write an immediate future implementation like this to avoid full Guava dependency
//class ImmediateFuture<T>(private val value: T) : ListenableFuture<T> {
//    override fun addListener(listener: Runnable, executor: Executor) {
//        runCatching { executor.execute(listener) }.onFailure { Log.e("Future", "", it) }
//    }
//
//    override fun cancel(mayInterruptIfRunning: Boolean): Boolean = false
//    override fun get(): T = value
//    override fun get(timeout: Long, unit: TimeUnit): T = get()
//    override fun isCancelled(): Boolean = false
//    override fun isDone(): Boolean = true
//}
