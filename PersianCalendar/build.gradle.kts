import groovy.json.JsonSlurper
import org.codehaus.groovy.runtime.ProcessGroovyMethods
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("androidx.navigation.safeargs.kotlin")
}

operator fun File.div(child: String) = File(this, child)

val generatedAppSrcDir = buildDir / "generated" / "source" / "appsrc" / "main"
android {
    sourceSets {
        getByName("main").java.srcDir(generatedAppSrcDir)
    }

    compileSdk = 30
    buildToolsVersion = "30.0.3"

    buildFeatures {
        viewBinding = true
    }

    val gitVersion = listOf(
        "git rev-parse --abbrev-ref HEAD", // branch, e.g. main
        "git rev-list HEAD --count", // number of commits in history, e.g. 3724
        "git rev-parse --short HEAD", // git hash, e.g. 2426d51f
        "git status -s" // i == 3, whether repo's dir is clean, -dirty is appended if smt is uncommitted
    ).mapIndexedNotNull { i, cmd ->
        // https://stackoverflow.com/a/52441962 equivalent to Groovy's "".execute().text
        ProcessGroovyMethods.getText(ProcessGroovyMethods.execute(cmd))?.trim()
            ?.takeIf { it.isNotEmpty() }.let { if (i == 3 && it != null) "dirty" else it }
    }.joinToString("-")

    defaultConfig {
        applicationId = "com.byagowi.persiancalendar"
        minSdk = 17
        targetSdk = 30
        versionCode = 651
        versionName = "6.5.1"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
        resourceConfigurations += listOf("en", "fa", "ckb", "ar", "ur", "ps", "glk", "azb", "ja")
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

    packagingOptions {
        exclude("DebugProbesKt.bin")
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
    implementation("com.github.persian-calendar:praytimes:1.0.2")

    implementation("androidx.appcompat:appcompat:1.3.0")
    implementation("androidx.preference:preference-ktx:1.1.1")
    implementation("androidx.recyclerview:recyclerview:1.2.1")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.viewpager2:viewpager2:1.0.0")
    implementation("com.google.android.material:material:1.3.0")

    implementation("com.google.openlocationcode:openlocationcode:1.0.4")

    implementation("androidx.navigation:navigation-fragment-ktx:2.3.5")
    implementation("androidx.navigation:navigation-ui-ktx:2.3.5")

    implementation("androidx.core:core-ktx:1.5.0")
    implementation("androidx.fragment:fragment-ktx:1.3.4")
    implementation("androidx.activity:activity-ktx:1.2.3")

    implementation("androidx.browser:browser:1.3.0")

    implementation("androidx.work:work-runtime-ktx:2.5.0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.5.0")

    // debugImplementation("com.squareup.leakcanary:leakcanary-android:2.0-alpha-2")
    // debugImplementation("com.github.pedrovgs:lynx:1.1.0")

    testImplementation("junit:junit:4.13.2")
    // Scratch.kt only dependencies
    // testImplementation("com.squareup.okhttp3:okhttp:3.10.0")
    // testImplementation("org.json:json:20210307")
    // testImplementation("com.ibm.icu:icu4j:68.2")

    androidTestImplementation("androidx.test:runner:1.3.0")
    androidTestImplementation("androidx.test:rules:1.3.0")
    androidTestImplementation("androidx.test:core-ktx:1.3.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.2")
    androidTestImplementation("androidx.test.espresso:espresso-contrib:3.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.3.0")
}

// App's own generated sources
val generateAppSrcTask by tasks.registering {
    val eventsJson = projectDir / "data" / "events.json"
    val citiesJson = projectDir / "data" / "cities.json"
    inputs.files(eventsJson, citiesJson)
    val generateDir = generatedAppSrcDir / "com" / "byagowi" / "persiancalendar" / "generated"
    val eventsOutput = generateDir / "Events.kt"
    val citiesOutput = generateDir / "Cities.kt"
    outputs.files(eventsOutput, citiesOutput)
    doLast {
        generateDir.mkdirs()

        // Events
        val events = JsonSlurper().parse(eventsJson) as Map<*, *>
        val (persianEvents, islamicEvents, gregorianEvents) = listOf(
            "Persian Calendar", "Hijri Calendar", "Gregorian Calendar"
        ).map { key ->
            (events[key] as List<*>).joinToString(",\n    ") {
                val record = it as Map<*, *>
                "CalendarRecord(title = \"${record["title"]}\"," +
                        " type = EventType.${record["type"].toString().replace(" ", "")}," +
                        " isHoliday = ${record["holiday"]}," +
                        " month = ${record["month"]}, day = ${record["day"]})"
            }
        }
        val irregularRecurringEvents = (events["Irregular Recurring"] as List<*>)
            .mapNotNull { (it as Map<*, *>).takeIf { event -> event["rule"] == "last day of week" } }
            .joinToString(",\n    ") { event ->
                "mapOf(${event.map { (k, v) -> """"$k" to "$v"""" }.joinToString(", ")})"
            }
        eventsOutput.writeText(
            """package ${android.defaultConfig.applicationId}.generated

enum class EventType { Afghanistan, Iran, AncientIran, International }

class CalendarRecord(val title: String, val type: EventType, val isHoliday: Boolean, val month: Int, val day: Int)

val persianEvents = listOf(
    $persianEvents
)

val islamicEvents = listOf(
    $islamicEvents
)

val gregorianEvents = listOf(
    $gregorianEvents
)

val irregularRecurringEvents = listOf(
    $irregularRecurringEvents
)
"""
        )

        // Cities
        val cities = (JsonSlurper().parse(citiesJson) as Map<*, *>).flatMap { countryEntry ->
            val countryCode = countryEntry.key as String
            val country = countryEntry.value as Map<*, *>
            (country["cities"] as Map<*, *>).map { cityEntry ->
                val key = cityEntry.key as String
                val city = cityEntry.value as Map<*, *>
                val latitude = (city["latitude"] as Number).toDouble()
                val longitude = (city["longitude"] as Number).toDouble()
                // Elevation really degrades quality of calculations
                val elevation =
                    if (countryCode == "ir") .0 else (city["elevation"] as Number).toDouble()
                """"$key" to CityItem(
        key = "$key",
        en = "${city["en"]}", fa = "${city["fa"]}",
        ckb = "${city["ckb"]}", ar = "${city["ar"]}",
        countryCode = "$countryCode",
        countryEn = "${country["en"]}", countryFa = "${country["fa"]}",
        countryCkb = "${country["ckb"]}", countryAr = "${country["ar"]}",
        coordinate = Coordinate($latitude, $longitude, $elevation)
    )"""
            }
        }.joinToString(",\n    ")
        citiesOutput.writeText(
            """package ${android.defaultConfig.applicationId}.generated

import com.byagowi.persiancalendar.entities.CityItem
import io.github.persiancalendar.praytimes.Coordinate

val citiesStore = mapOf(
    $cities
)
"""
        )
    }
}
tasks.named("preBuild").configure { dependsOn(generateAppSrcTask) }
