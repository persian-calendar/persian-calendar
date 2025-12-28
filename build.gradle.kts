plugins {
    // All the plugins used in subprojects and plugins should be listed here with "apply false"

    // PersianCalendar plugins
    alias(libs.plugins.com.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    id("io.github.persiancalendar.appbuildplugin") apply false

    // gradlePlugins plugins
    `kotlin-dsl` apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.plugin.serialization) apply false
    alias(libs.plugins.spotless)
}

spotless {
    val editorConfigOverride = mapOf(
        "ktlint_standard_trailing-comma-on-call-site" to "disabled",
        "ktlint_standard_trailing-comma-on-declaration-site" to "disabled",
        "ktlint_standard_wrapping" to "disabled",
        "ktlint_standard_argument-list-wrapping" to "disabled",
        "ktlint_standard_parameter-list-wrapping" to "disabled",
        "ktlint_standard_comment-wrapping" to "disabled",
        "ktlint_standard_function-signature" to "disabled",
        "ktlint_standard_statement-wrapping" to "disabled",
        "ktlint_standard_value-parameter-comment" to "disabled",
        "ktlint_standard_max-line-length" to "disabled",
        "ktlint_standard_indent" to "disabled",
        "ktlint_standard_annotation" to "disabled",
        "ktlint_standard_function-naming" to "disabled",
        "ktlint_standard_property-naming" to "disabled",
        "ktlint_standard_function-expression-body" to "disabled",
        "ktlint_standard_multiline-if-else" to "disabled",
        "ktlint_standard_class-signature" to "disabled",
    )

    kotlin {
        target("**/*.kt")
        targetExclude("**/build/**")
        ktlint().editorConfigOverride(editorConfigOverride)
    }

    kotlinGradle {
        target("**/*.gradle.kts")
        targetExclude("**/build/**")
        ktlint().editorConfigOverride(editorConfigOverride)
    }

    format("misc") {
        target("**/*.md", "**/.gitignore")
        trimTrailingWhitespace()
        endWithNewline()
    }
}
