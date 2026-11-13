package io.github.persiancalendar.lint

import com.android.tools.lint.detector.api.JavaContext

/**
 * Sources vendored under `libs/` are compiled into this project but are owned elsewhere, so this
 * project's custom checks do not apply to them.
 */
internal fun JavaContext.isVendoredSource(): Boolean =
    file.absolutePath.replace('\\', '/').contains("/libs/")
