pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
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

listOf("calculator", "calendar", "equinox", "praytimes", "qr").forEach { name ->
    include(":$name")
    project(":$name").projectDir = file("libs/$name")
}

include(":open-location-code")
project(":open-location-code").projectDir = file("libs/open-location-code/kotlin")

include(":astronomy")
project(":astronomy").projectDir = file("libs/astronomy/source/kotlin")
