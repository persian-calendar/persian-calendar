import com.android.build.gradle.internal.api.BaseVariantOutputImpl
import org.jetbrains.kotlin.config.KotlinCompilerVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
}

// https://stackoverflow.com/a/52441962
fun String.runCommand(
    workingDir: File = File("."),
    timeoutAmount: Long = 60,
    timeoutUnit: TimeUnit = TimeUnit.SECONDS
): String? = try {
    ProcessBuilder(split("\\s".toRegex()))
        .directory(workingDir)
        .redirectOutput(ProcessBuilder.Redirect.PIPE)
        .redirectError(ProcessBuilder.Redirect.PIPE)
        .start().apply { waitFor(timeoutAmount, timeoutUnit) }
        .inputStream.bufferedReader().readText()
} catch (e: java.io.IOException) {
    e.printStackTrace()
    null
}

android {
    compileSdkVersion(29)
    buildToolsVersion("29.0.2")
    viewBinding.isEnabled = true

    defaultConfig {
        applicationId = "com.byagowi.persiancalendar"
        minSdkVersion(15)
        targetSdkVersion(29)
        versionCode = 618
        versionName = "6.1.8"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
        resConfigs("en", "fa", "ckb", "ar", "ur", "ps", "glk", "azb", "ja")
    }

    val appVerboseVersion =
        defaultConfig.versionName + "-" + listOf(
            "git rev-parse --abbrev-ref HEAD",
            "git rev-list HEAD --count",
            "git rev-parse --short HEAD"
        ).map { it.runCommand()?.trim() }.joinToString("-") +
                (if ("git status -s".runCommand()?.trim()?.isEmpty() == false) "-dirty" else "")

    buildTypes {
        getByName("debug") {
            buildOutputs.all {
                (this as BaseVariantOutputImpl).outputFileName =
                    "PersianCalendar-debug-$appVerboseVersion.apk"
            }
            versionNameSuffix = "-$appVerboseVersion"
            applicationIdSuffix = ".debug"
        }
        getByName("release") {
            buildOutputs.all {
                (this as BaseVariantOutputImpl).outputFileName =
                    "PersianCalendar-release-$appVerboseVersion.apk"
            }
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

// compile bytecode to java 8 (default is java 6)
tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

dependencies {
    implementation(project(":equinox"))
    implementation(project(":calendar"))
    implementation(project(":praytimes"))

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${KotlinCompilerVersion.VERSION}")

    implementation("androidx.appcompat:appcompat:1.1.0")
    implementation("androidx.preference:preference-ktx:1.1.0")
    implementation("androidx.recyclerview:recyclerview:1.0.0")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.viewpager2:viewpager2:1.0.0-rc01")
    implementation("com.google.android.material:material:1.1.0-beta01")
    implementation("com.google.android:flexbox:1.1.0")
    implementation("com.google.android.apps.dashclock:dashclock-api:2.0.0")

    val navVersion = "2.1.0"
    implementation("androidx.navigation:navigation-fragment-ktx:$navVersion")
    implementation("androidx.navigation:navigation-ui-ktx:$navVersion")

    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.1.0")

    implementation("androidx.core:core-ktx:1.1.0")
    implementation("androidx.fragment:fragment-ktx:1.1.0")
    implementation("androidx.activity:activity-ktx:1.0.0")

    implementation("androidx.browser:browser:1.0.0")

    implementation("androidx.work:work-runtime-ktx:2.2.0")

    val daggerVersion = "2.25.2"
    implementation("com.google.dagger:dagger-android:$daggerVersion")
    implementation("com.google.dagger:dagger-android-support:$daggerVersion")
    kapt("com.google.dagger:dagger-compiler:$daggerVersion")
    kapt("com.google.dagger:dagger-android-processor:$daggerVersion")

    // debugImplementation("com.squareup.leakcanary:leakcanary-android:2.0-alpha-2")
    // debugImplementation("com.github.pedrovgs:lynx:1.1.0")

    testImplementation("junit:junit:4.12")

    androidTestImplementation("androidx.test:runner:1.2.0")
    androidTestImplementation("androidx.test:rules:1.2.0")
    androidTestImplementation("androidx.test.espresso:espresso-contrib:3.2.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.2.0")
}
