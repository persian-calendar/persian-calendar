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
}

tasks.register("clean") {
    delete(rootProject.layout.buildDirectory)
}
