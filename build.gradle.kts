buildscript {
    repositories {
        google()
        gradlePluginPortal()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:4.1.3")
        classpath(kotlin("gradle-plugin", version = "1.4.30"))
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven("https://jitpack.io")
    }
}

task("clean") {
    delete(rootProject.buildDir)
}
