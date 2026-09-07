package io.github.cosinekitty.astronomy

internal expect fun <T> withPlutoLock(lock: Any, action: () -> T): T
