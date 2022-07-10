plugins {
    id("com.android.application") version "7.2.1" apply false
    id("org.jetbrains.kotlin.android") version "1.7.0" apply false
    id("androidx.navigation.safeargs.kotlin") version "2.4.2" apply false
    // id("com.google.gms.google-services") version "4.3.10" apply false
    // id("com.google.firebase.firebase-perf") version "1.4.1" apply false
    // id("com.google.firebase.crashlytics") version "2.8.1" apply false
}

task("clean") {
    delete(rootProject.buildDir)
}
