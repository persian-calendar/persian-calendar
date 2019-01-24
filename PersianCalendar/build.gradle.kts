import org.jetbrains.kotlin.config.KotlinCompilerVersion

plugins {
    id("com.android.application")
    kotlin("android")
}

// https://stackoverflow.com/a/52441962
fun String.runCommand(workingDir: File = File("."),
                      timeoutAmount: Long = 60,
                      timeoutUnit: TimeUnit = TimeUnit.SECONDS): String? {
    return try {
        ProcessBuilder(*this.split("\\s".toRegex()).toTypedArray())
                .directory(workingDir)
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .start().apply {
                    waitFor(timeoutAmount, timeoutUnit)
                }.inputStream.bufferedReader().readText()
    } catch (e: java.io.IOException) {
        e.printStackTrace()
        null
    }
}

android {
    compileSdkVersion(28)
    dataBinding.isEnabled = true

    defaultConfig {
        applicationId = "com.byagowi.persiancalendar"
        minSdkVersion(15)
        targetSdkVersion(28)
        versionCode = 600
        versionName = "6.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
    }

    buildTypes {
        getByName("debug") {
            versionNameSuffix = "-" + arrayOf(
                    "git rev-parse --abbrev-ref HEAD",
                    "git rev-list HEAD --count",
                    "git rev-parse --short HEAD"
            ).map { it.runCommand()?.trim() }.joinToString("-")
        }
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            // Maybe proguard-android-optimize.txt in future
            // setProguardFiles(listOf(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro"))
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.0.2")
    implementation("androidx.preference:preference:1.0.0")
    implementation("androidx.recyclerview:recyclerview:1.0.0")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("com.google.android.material:material:1.0.0")
    implementation("android.arch.navigation:navigation-fragment:1.0.0-alpha09")
    implementation("android.arch.navigation:navigation-ui:1.0.0-alpha09")
    implementation("com.google.android:flexbox:1.1.0")
    implementation("com.google.android.apps.dashclock:dashclock-api:2.0.0")

    // Please apply this https://issuetracker.google.com/issues/112877717 before enabling it again
    // implementation("android.arch.work:work-runtime:1.0.0-alpha07")

    val daggerVersion = "2.20"
    implementation("com.google.dagger:dagger-android:$daggerVersion")
    implementation("com.google.dagger:dagger-android-support:$daggerVersion")
    annotationProcessor("com.google.dagger:dagger-compiler:$daggerVersion")
    annotationProcessor("com.google.dagger:dagger-android-processor:$daggerVersion")

    val leakCanaryVersion = "1.6.3"
    debugImplementation("com.squareup.leakcanary:leakcanary-android:$leakCanaryVersion")
    debugImplementation("com.squareup.leakcanary:leakcanary-support-fragment:$leakCanaryVersion")

    testImplementation("junit:junit:4.12")
    testImplementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${KotlinCompilerVersion.VERSION}")

    androidTestImplementation("androidx.test:runner:1.1.1")
    androidTestImplementation("androidx.test:rules:1.1.1")
    androidTestImplementation("androidx.test.espresso:espresso-contrib:3.1.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.1.1")
}
