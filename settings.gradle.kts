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
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "persian-calendar"
include(":PersianCalendar")
includeBuild("gradlePlugins")
include(":wear")
include(":lintChecks")

listOf("calculator", "calendar", "equinox", "praytimes", "qr").forEach { repo ->
    includeBuild("libs/$repo") {
        dependencySubstitution {
            substitute(module("io.github.persiancalendar:$repo")).using(project(":"))
        }
    }
}

includeBuild("libs/open-location-code/kotlin") {
    name = "open-location-code"
    dependencySubstitution {
        substitute(module("com.google.openlocationcode:openlocationcode")).using(project(":"))
    }
}

includeBuild("libs/astronomy/source/kotlin") {
    name = "astronomy"
    dependencySubstitution {
        substitute(module("io.github.cosinekitty:astronomy")).using(project(":"))
    }
}
