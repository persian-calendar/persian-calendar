plugins {
    `kotlin-dsl`
    kotlin("plugin.serialization") version "1.6.10"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.squareup:kotlinpoet:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
}

gradlePlugin {
    plugins {
        create("dependencies") {
            id = "io.github.persiancalendar.appbuildplugin"
            implementationClass = "io.github.persiancalendar.gradle.AppBuildPlugin"
        }
    }
}
