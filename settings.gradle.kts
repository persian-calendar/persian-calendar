pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()

    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    // Kotlin/Wasm registers a distribution repository for its pinned Node runtime.
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
    repositories {
        mavenCentral()
        // Same Google Maven mirror as pluginManagement; some Google artifacts are
        // unavailable directly from this environment.
        maven("https://maven.aliyun.com/repository/google") {
            content { includeGroupByRegex("androidx\\..*"); includeGroupByRegex("com\\.android.*") }
        }
        google()
        maven("https://jitpack.io")
    }
}
rootProject.name = "persian-calendar"
include(":PersianCalendar")
includeBuild("gradlePlugins")
include(":wear")
include(":lintChecks")
include(":shared")
include(":webApp")
