buildscript {
    repositories {
        google()
        gradlePluginPortal()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.0.4")
        classpath(kotlin("gradle-plugin", version = "1.6.10"))
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.4.0")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}

task("clean") {
    delete(rootProject.buildDir)
}
