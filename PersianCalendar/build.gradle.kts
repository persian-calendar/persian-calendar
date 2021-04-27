import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.json.JSONObject

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("androidx.navigation.safeargs.kotlin")
}

// https://stackoverflow.com/a/52441962
fun String.runCommand(
    workingDir: File = File("."),
    timeoutAmount: Long = 60,
    timeoutUnit: TimeUnit = TimeUnit.SECONDS
): String? = runCatching {
    ProcessBuilder("\\s".toRegex().split(this))
        .directory(workingDir)
        .redirectOutput(ProcessBuilder.Redirect.PIPE)
        .redirectError(ProcessBuilder.Redirect.PIPE)
        .start().also { it.waitFor(timeoutAmount, timeoutUnit) }
        .inputStream.bufferedReader().readText()
}.onFailure { it.printStackTrace() }.getOrNull()

val generatedAppSrcDir = File(buildDir, "generated/source/appsrc/main")
android {
    sourceSets {
        getByName("main").java.srcDir(generatedAppSrcDir)
    }

    compileSdkVersion(30)
    buildToolsVersion("30.0.3")

    buildFeatures {
        viewBinding = true
    }

    val gitVersion = listOf(
        "git rev-parse --abbrev-ref HEAD",
        "git rev-list HEAD --count",
        "git rev-parse --short HEAD"
    ).joinToString("-") { it.runCommand()?.trim() ?: "" } +
            (if (("git status -s".runCommand() ?: "").isBlank()) "" else "-dirty")

    defaultConfig {
        applicationId = "com.byagowi.persiancalendar"
        minSdkVersion(17)
        targetSdkVersion(30)
        versionCode = 651
        versionName = "6.5.1"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
        resConfigs("en", "fa", "ckb", "ar", "ur", "ps", "glk", "azb", "ja")
        setProperty("archivesBaseName", "PersianCalendar-$versionName-$gitVersion")
        multiDexEnabled = false
    }

    signingConfigs {
        create("nightly") {
            storeFile = rootProject.file("nightly.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }

    buildTypes {
        create("nightly") {
            signingConfig = signingConfigs.getByName("nightly")
            versionNameSuffix = "-${defaultConfig.versionName}-$gitVersion-nightly"
            applicationIdSuffix = ".nightly"
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            isMinifyEnabled = true
            isShrinkResources = true
        }

        getByName("debug") {
            versionNameSuffix = "-${defaultConfig.versionName}-$gitVersion"
            applicationIdSuffix = ".debug"
        }

        getByName("release") {
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            isMinifyEnabled = true
            isShrinkResources = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

dependencies {
    implementation("com.github.persian-calendar:equinox:1.0.0")
    implementation("com.github.persian-calendar:calendar:1.0.1")
    implementation("com.github.persian-calendar:praytimes:1.0.0")

    implementation("androidx.appcompat:appcompat:1.2.0")
    implementation("androidx.preference:preference-ktx:1.1.1")
    implementation("androidx.recyclerview:recyclerview:1.2.0")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.viewpager2:viewpager2:1.0.0")
    implementation("com.google.android.material:material:1.3.0")

    implementation("com.google.openlocationcode:openlocationcode:1.0.4")

    implementation("androidx.navigation:navigation-fragment-ktx:2.3.5")
    implementation("androidx.navigation:navigation-ui-ktx:2.3.5")

    implementation("androidx.core:core-ktx:1.3.2")
    implementation("androidx.fragment:fragment-ktx:1.3.3")
    implementation("androidx.activity:activity-ktx:1.2.2")

    implementation("androidx.browser:browser:1.3.0")

    implementation("androidx.work:work-runtime-ktx:2.5.0")

    // debugImplementation("com.squareup.leakcanary:leakcanary-android:2.0-alpha-2")
    // debugImplementation("com.github.pedrovgs:lynx:1.1.0")

    testImplementation("junit:junit:4.13.2")
    // Scratch.kt only dependencies
    // testImplementation("com.squareup.okhttp3:okhttp:3.10.0")
    // testImplementation("org.json:json:20210307")
    // testImplementation("com.ibm.icu:icu4j:68.2")

    androidTestImplementation("androidx.test:runner:1.3.0")
    androidTestImplementation("androidx.test:rules:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-contrib:3.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.3.0")
}

// App's own generated sources
val generateAppSrcTask by tasks.registering {
    val generateDir = File(generatedAppSrcDir, "com/byagowi/persiancalendar/generated")
    inputs.dir(File(projectDir, "data"))
    outputs.file(File(generateDir, "Events.kt"))
    outputs.file(File(generateDir, "Cities.kt"))
    doLast {
        generateDir.mkdirs()

        // Events
        fun JSONObject.getArray(key: String): Sequence<JSONObject> =
            getJSONArray(key).run { (0 until length()).asSequence().map { getJSONObject(it) } }

        val events = JSONObject(File(projectDir, "data/events.json").readText())

        fun createEventStore(key: String) = events.getArray(key).joinToString(",\n    ") {
            "CalendarRecord(\"${it.getString("title")}\"," +
                    " \"${it.optString("type", "")}\"," +
                    " ${it.optBoolean("holiday", false)}," +
                    " ${it.optInt("year", -1)}," +
                    " ${it.getInt("month")}, ${it.getInt("day")})"
        }

        val persianEvents = createEventStore("Persian Calendar")
        val islamicEvents = createEventStore("Hijri Calendar")
        val gregorianEvents = createEventStore("Gregorian Calendar")

        File(generateDir, "Events.kt").writeText(
            """package com.byagowi.persiancalendar.generated

class CalendarRecord(val title: String, val type: String, val isHoliday: Boolean, val year: Int, val month: Int, val day: Int)
val persianEvents = listOf(
    $persianEvents
)
val islamicEvents = listOf(
    $islamicEvents
)
val gregorianEvents = listOf(
    $gregorianEvents
)"""
        )

        // Cities
        fun <T> JSONObject.map(f: (String, JSONObject) -> T) =
            keys().asSequence().map { f(it, this.getJSONObject(it)) }

        val cities = JSONObject(
            File(projectDir, "data/cities.json").readText()
        ).map { countryCode, country ->
            country.getJSONObject("cities").map { key, city ->
                """"$key" to CityItem(
        key = "$key",
        en = "${city.getString("en")}", fa = "${city.getString("fa")}",
        ckb = "${city.getString("ckb")}", ar = "${city.getString("ar")}",
        countryCode = "$countryCode",
        countryEn = "${country.getString("en")}", countryFa = "${country.getString("fa")}",
        countryCkb = "${country.getString("ckb")}", countryAr = "${country.getString("ar")}",
        coordinate = Coordinate(
            ${city.getDouble("latitude")},
            ${city.getDouble("longitude")},
            ${if (countryCode == "ir") 0.0 else city.getDouble("elevation")}
        )
    )"""
            }
        }.flatten().joinToString(",\n    ")

        File(generateDir, "Cities.kt").writeText(
            """package ${android.defaultConfig.applicationId}.generated

import com.byagowi.persiancalendar.entities.CityItem
import io.github.persiancalendar.praytimes.Coordinate

val citiesStore = mapOf(
    $cities
)"""
        )
    }
}
afterEvaluate {
    android.applicationVariants.forEach { variant ->
        variant.registerJavaGeneratingTask(generateAppSrcTask.get(), generatedAppSrcDir)
    }
}
