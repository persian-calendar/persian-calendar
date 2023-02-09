plugins {
    id("com.android.application") version "7.4.1" apply false
    id("org.jetbrains.kotlin.android") version "1.8.10" apply false
    id("androidx.navigation.safeargs.kotlin") version "2.5.1" apply false
}

task("clean") {
    delete(rootProject.buildDir)
}
