package com.byagowi.persiancalendar;

import android.util.Log
import com.google.common.util.concurrent.ListenableFuture
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit

// Akin to https://github.com/google/guava/blob/master/android/guava/src/com/google/common/util/concurrent/ImmediateFuture.java
// Tile interface needs ListenableFuture and this is a bare minimum one
class ImmediateFuture<T>(private val value: T) : ListenableFuture<T> {
    override fun cancel(mayInterruptIfRunning: Boolean): Boolean = false
    override fun get(): T = value
    override fun get(timeout: Long, unit: TimeUnit): T = value
    override fun isCancelled(): Boolean = false
    override fun isDone(): Boolean = true
    override fun addListener(listener: Runnable, executor: Executor) {
        runCatching { executor.execute(listener) }.onFailure { Log.e("Future", "", it) }
    }
}
