buildscript {
    repositories {
        google()
        gradlePluginPortal()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:4.1.2")
        classpath(kotlin("gradle-plugin", version = "1.4.30"))
    }
}

allprojects {
    repositories {
        google()
        maven("https://jitpack.io")
        mavenCentral()

        // can be removed when https://github.com/google/flexbox-layout/issues/566 is resolved
        jcenter()
    }
}

task("clean") {
    delete(rootProject.buildDir)
}
