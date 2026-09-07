package io.github.cosinekitty.astronomy

internal actual fun <T> withPlutoLock(lock: Any, action: () -> T): T = synchronized(lock, action)
