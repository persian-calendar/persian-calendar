package com.byagowi.persiancalendar.ui.map

/** Owns a globe texture until replacement/release, never recycling it during a GL upload. */
internal class GlobeTextureOwner<T : Any>(initial: T, private val recycle: (T) -> Unit) {
    private val lock = Any()
    private var texture: T? = initial
    private var closed = false

    fun replace(next: T) = synchronized(lock) {
        if (texture === next) return@synchronized
        if (closed) {
            recycle(next)
        } else {
            val previous = texture
            texture = next
            previous?.let(recycle)
        }
    }

    // The callback must finish using the texture before returning. Never retain it in a queue.
    fun withTexture(upload: (T) -> Unit) = synchronized(lock) {
        texture?.let(upload)
        Unit
    }

    fun close() = synchronized(lock) {
        closed = true
        val previous = texture
        texture = null
        previous?.let(recycle)
    }
}
