plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
    id("io.github.persiancalendar.appbuildplugin")
}

val generatedAppSrcDir = layout.buildDirectory.dir("generated/source/appsrc/main").get().asFile

kotlin {
    jvmToolchain(21)

    android {
        namespace = "com.byagowi.persiancalendar.shared"
        compileSdk = 37
        minSdk = 23
    }

    jvm("desktop")
    js {
        browser()
    }
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain {
            kotlin.srcDir(generatedAppSrcDir)

            dependencies {
                api(project(":astronomy"))
                api(project(":calculator"))
                api(project(":calendar"))
                api(project(":equinox"))
                api(project(":open-location-code"))
                api(project(":praytimes"))
                api(project(":qr"))
                api(libs.compose.multiplatform.runtime)
                api(libs.compose.multiplatform.foundation)
                implementation(libs.compose.multiplatform.material3)
            }
        }
    }
}

tasks.configureEach {
    if (name.startsWith("compile")) {
        dependsOn("codegenerators")
    }
}
