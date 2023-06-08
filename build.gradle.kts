plugins {
    id("com.android.application") version "8.0.2" apply false
    id("org.jetbrains.kotlin.android") version "1.8.22" apply false
    id("androidx.navigation.safeargs.kotlin") version "2.5.1" apply false
}

task("clean") {
    delete(rootProject.buildDir)
}
