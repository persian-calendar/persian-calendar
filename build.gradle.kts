buildscript {
    repositories {
        google()
        gradlePluginPortal()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.0.1")
        classpath(kotlin("gradle-plugin", version = "1.5.21"))
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.3.5")
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
