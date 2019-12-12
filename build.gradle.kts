// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    repositories {
        google()
        jcenter()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:3.6.0-beta05")
        classpath(kotlin("gradle-plugin", version = "1.3.50"))
    }
}

allprojects {
    repositories {
        google()
        jcenter()
    }
}

task("clean") {
    delete(rootProject.buildDir)
}
