package io.github.cosinekitty.astronomy

// The browser application executes Kotlin on one JavaScript agent.
internal actual fun <T> withPlutoLock(lock: Any, action: () -> T): T = action()
