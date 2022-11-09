plugins {
    `kotlin-dsl`
    val kotlinVersion = "1.7.21"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.squareup:kotlinpoet:1.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")
}

gradlePlugin {
    plugins {
        create("dependencies") {
            id = "io.github.persiancalendar.appbuildplugin"
            implementationClass = "io.github.persiancalendar.gradle.AppBuildPlugin"
        }
    }
}
