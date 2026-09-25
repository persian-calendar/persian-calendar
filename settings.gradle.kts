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
        // Node.js distribution used by Kotlin/JS (webpack). Must be declared
        // here because PREFER_SETTINGS ignores project-level repositories.
        ivy {
            name = "Node.js Distributions"
            url = uri("https://nodejs.org/dist")
            patternLayout { artifact("v[revision]/[artifact](-v[revision]-[classifier]).[ext]") }
            metadataSources { artifact() }
            content { includeModule("org.nodejs", "node") }
        }
        // Yarn distribution used by Kotlin/JS (webpack). Same reason as above.
        ivy {
            name = "Yarn Distributions"
            url = uri("https://github.com/yarnpkg/yarn/releases/download")
            patternLayout { artifact("v[revision]/[artifact](-v[revision]).[ext]") }
            metadataSources { artifact() }
            content { includeModule("com.yarnpkg", "yarn") }
        }
    }
}
rootProject.name = "persian-calendar"
include(":PersianCalendar")
include(":shared")
include(":desktop")
include(":web")
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
