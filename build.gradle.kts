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

//spotless {
//    kotlin {
//        target("**/*.kt")
//        targetExclude("**/build/**")
//        ktlint()
// standard:trailing-comma-on-call-site,standard:trailing-comma-on-declaration-site,standard:comment-wrapping,standard:filename,standard:function-naming,standard:if-else-wrapping,standard:max-line-length,standard:property-naming
//    }
//
//    kotlinGradle {
//        target("**/*.gradle.kts")
//        targetExclude("**/build/**")
//        ktlint()
//    }
//
//    format("misc") {
//        target("**/*.md", "**/.gitignore")
//        trimTrailingWhitespace()
//        endWithNewline()
//    }
//}
