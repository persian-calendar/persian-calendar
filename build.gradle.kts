plugins {
    alias(libs.plugins.com.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.androidx.navigation.safeargs.kotlin) apply false
}

task("clean") {
    delete(rootProject.layout.buildDirectory)
}
