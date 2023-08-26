// Doesn't work without this workaround: https://github.com/gradle/gradle/issues/15383
// See settings.gradle.kts of this folder also.
val Project.libs: org.gradle.accessors.dm.LibrariesForLibs get() = extensions.getByType()

plugins {
    `kotlin-dsl`
    alias(libs.plugins.org.jetbrains.kotlin.jvm)
    alias(libs.plugins.org.jetbrains.kotlin.plugin.serialization)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.kotlinpoet)
    implementation(libs.kotlinx.serialization.json)
}

gradlePlugin {
    plugins {
        create("dependencies") {
            id = "io.github.persiancalendar.appbuildplugin"
            implementationClass = "io.github.persiancalendar.gradle.AppBuildPlugin"
        }
    }
}
