plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.plugin.parcelize)
    alias(libs.plugins.kotlin.plugin.serialization)
    id("io.github.persiancalendar.appbuildplugin")
}

val generatedAppSrcDir = layout.buildDirectory.dir("generated/source/appsrc/main").get().asFile
val generatedAppSrcDirAndroidMain = layout.buildDirectory.dir("generated/source/appsrc/androidMain").get().asFile

kotlin {
    jvmToolchain(21)

    android {
        namespace = "com.byagowi.persiancalendar.shared"
        compileSdk = 37
        minSdk = 23

        androidResources {
            enable = true
        }
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
                api(libs.compose.components.resources)
            }
        }
        androidMain {
            kotlin.srcDir(generatedAppSrcDirAndroidMain)
        }
        jsMain {
            dependencies {
                implementation(libs.kotlinx.browser)
            }
        }
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = "com.byagowi.persiancalendar.shared.generated.resources"
    // Point CMP's commonMain resources at the Android res directory so both
    // AAPT (R.string) and Compose resources (Res.string) read the same files.
    customDirectory(
        sourceSetName = "commonMain",
        directoryProvider = provider { layout.projectDirectory.dir("src/androidMain/res") },
    )
}

tasks.configureEach {
    if (name.startsWith("compile")) {
        dependsOn("codegenerators")
    }
}
