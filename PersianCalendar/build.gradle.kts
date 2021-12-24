import groovy.json.JsonSlurper
import org.codehaus.groovy.runtime.ProcessGroovyMethods

operator fun File.div(child: String) = File(this, child)
fun String.execute() = ProcessGroovyMethods.execute(this)
val Process.text: String? get() = ProcessGroovyMethods.getText(this)

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("androidx.navigation.safeargs.kotlin")
}

// Disabled due to F-Droid inability to parse dynamic versioning
//   val versionMajor = 1
//   val versionMinor = 1
//   val versionPatch = 1
//   val versionNumber = versionMajor * 100 + versionMinor * 10 + versionPatch
//   if (listOf(versionMinor, versionPatch).any { it !in 0..9 })
//       error("Use one digit numbers for minor and patch")
//   if (versionPatch % 2 != 0)
//      error("As current Api based flavors scheme, use even number for patch numbers")
//   val baseVersionName = "$versionMajor.$versionMinor.$versionPatch"

val composeVersion = "1.0.5"

val isMinApi21Build = gradle.startParameter.taskNames.any { "minApi21" in it || "MinApi21" in it }

val generatedAppSrcDir = buildDir / "generated" / "source" / "appsrc" / "main"
android {
    sourceSets {
        getByName("main").kotlin.srcDir(generatedAppSrcDir)
    }

    compileSdk = 31
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
        cmd.execute().text?.trim()
            ?.takeIf { it.isNotEmpty() }.let { if (i == 3 && it != null) "dirty" else it }
    }.joinToString("-")

    defaultConfig {
        applicationId = "com.byagowi.persiancalendar"
        minSdk = 17
        targetSdk = 31
        versionCode = 720
        versionName = "7.2.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        if (!isMinApi21Build) vectorDrawables.useSupportLibrary = true
        resourceConfigurations += listOf(
            "en", "fa", "ckb", "ar", "ur", "ps", "glk", "azb", "ja", "fr", "es", "tr", "kmr", "tg",
            "ne"
        )
        setProperty("archivesBaseName", "PersianCalendar-$versionName-$gitVersion")
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
            buildConfigField("boolean", "DEVELOPMENT", "true")
        }

        getByName("debug") {
            versionNameSuffix = "-${defaultConfig.versionName}-$gitVersion"
            buildConfigField("boolean", "DEVELOPMENT", "true")

            // Commented out To make it work in older Android version, feel free to undo
            // applicationIdSuffix = ".debug"
        }

        getByName("release") {
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            isMinifyEnabled = true
            isShrinkResources = true
            multiDexEnabled = false
            buildConfigField("boolean", "DEVELOPMENT", "false")
        }
    }

    flavorDimensions("api")
    productFlavors {
        create("minApi17") {
            dimension = "api"
        }
        create("minApi21") {
            applicationIdSuffix = ".minApi21"
            dimension = "api"
            minSdk = 21
            // versionCode = versionNumber + 1
        }
    }

    packagingOptions {
        resources.excludes += "DebugProbesKt.bin"
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }

    bundle {
        language {
            // We have in app locale change and don't want Google Play's dependency so better
            // to disable this.
            enableSplit = false
        }
    }

    composeOptions {
        kotlinCompilerExtensionVersion = composeVersion
    }

    if (isMinApi21Build) {
        buildFeatures {
            compose = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11

        // isCoreLibraryDesugaringEnabled = true
        //   Actually could be useful as makes use of java.time.Duration possible instead
        //   java.util.concurrent.TimeUnit but needs multidex as it says:
        //     In order to use core library desugaring, please enable multidex.
        //   And multidex doesn't play that well for older Android versions so let's
        //   skip it.
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    lint { disable("MissingTranslation") }
}

val minApi21Implementation by configurations

dependencies {
    implementation("com.github.persian-calendar:equinox:1.0.1")
    implementation("com.github.persian-calendar:calendar:1.2.0")
    implementation("com.github.persian-calendar:praytimes:2.1.2")

    implementation("androidx.appcompat:appcompat:1.4.0")
    implementation("androidx.preference:preference-ktx:1.1.1")
    implementation("androidx.recyclerview:recyclerview:1.2.1")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.viewpager2:viewpager2:1.0.0")
    implementation("androidx.dynamicanimation:dynamicanimation:1.0.0")
    implementation("com.google.android.material:material:1.6.0-alpha01")

    implementation("com.google.openlocationcode:openlocationcode:1.0.4")

    implementation("androidx.navigation:navigation-fragment-ktx:2.3.5")
    implementation("androidx.navigation:navigation-ui-ktx:2.3.5")

    implementation("androidx.core:core-ktx:1.7.0")
    implementation("androidx.fragment:fragment-ktx:1.4.0")
    implementation("androidx.activity:activity-ktx:1.4.0")

    implementation("androidx.browser:browser:1.4.0")

    implementation("androidx.work:work-runtime-ktx:2.7.1")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.5.2")
    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.7.3")

    minApi21Implementation("androidx.activity:activity-compose:1.4.0")
    minApi21Implementation("com.google.android.material:compose-theme-adapter:1.1.2")
    minApi21Implementation("com.google.accompanist:accompanist-flowlayout:0.20.3")
    minApi21Implementation("androidx.compose.ui:ui:$composeVersion")
    minApi21Implementation("androidx.compose.material:material:$composeVersion")
    minApi21Implementation("androidx.compose.material3:material3:1.0.0-alpha02")
    minApi21Implementation("androidx.compose.ui:ui-tooling-preview:$composeVersion")
    if (isMinApi21Build) {
        implementation("androidx.compose.runtime:runtime:$composeVersion")
        androidTestImplementation("androidx.compose.ui:ui-test-junit4:$composeVersion")
        debugImplementation("androidx.compose.ui:ui-tooling:$composeVersion")
    }

    // debugImplementation("com.squareup.leakcanary:leakcanary-android:2.0-alpha-2")

    testImplementation("junit:junit:4.13.2")
    // Scratch.kt only dependencies
    // testImplementation("com.squareup.okhttp3:okhttp:3.10.0")
    // testImplementation("org.json:json:20210307")
    // testImplementation("com.ibm.icu:icu4j:68.2")

    androidTestImplementation("androidx.test:runner:1.4.0")
    androidTestImplementation("androidx.test:rules:1.4.0")
    androidTestImplementation("androidx.test:core-ktx:1.4.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-contrib:3.4.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
}

// App's own generated sources
val generateAppSrcTask by tasks.registering {
    val eventsJson = projectDir / "data" / "events.json"
    val citiesJson = projectDir / "data" / "cities.json"
    val districtsJson = projectDir / "data" / "districts.json"
    inputs.files(eventsJson, citiesJson)
    val generateDir = generatedAppSrcDir / "com" / "byagowi" / "persiancalendar" / "generated"
    val eventsOutput = generateDir / "Events.kt"
    val citiesOutput = generateDir / "Cities.kt"
    val districtsOutput = generateDir / "Districts.kt"
    outputs.files(eventsOutput, citiesOutput, districtsOutput)
    doLast {
        generateDir.mkdirs()

        // Events
        val events = JsonSlurper().parse(eventsJson) as Map<*, *>
        val (persianEvents, islamicEvents, gregorianEvents, nepaliEvents) = listOf(
            "Persian Calendar", "Hijri Calendar", "Gregorian Calendar", "Nepali Calendar"
        ).map { key ->
            (events[key] as List<*>).joinToString(",\n    ") {
                val record = it as Map<*, *>
                "CalendarRecord(title = \"${record["title"]}\"," +
                        " type = EventType.${record["type"]}," +
                        " isHoliday = ${record["holiday"]}," +
                        " month = ${record["month"]}, day = ${record["day"]})"
            }
        }
        val irregularRecurringEvents = (events["Irregular Recurring"] as List<*>)
            .mapNotNull { it as Map<*, *> }
            .joinToString(",\n    ") { event ->
                "mapOf(${event.map { (k, v) -> """"$k" to "$v"""" }.joinToString(", ")})"
            }
        val eventsSource = (events["Source"] as Map<*, *>).toList()
            .joinToString(",\n    ") { (k, v) -> """$k("$v")""" }
        eventsOutput.writeText(
            """package ${android.defaultConfig.applicationId}.generated

enum class EventType(val source: String) {
    $eventsSource
}

class CalendarRecord(val title: String, val type: EventType, val isHoliday: Boolean, val month: Int, val day: Int)

val persianEvents = listOf<CalendarRecord>(
    $persianEvents
)

val islamicEvents = listOf<CalendarRecord>(
    $islamicEvents
)

val gregorianEvents = listOf<CalendarRecord>(
    $gregorianEvents
)

val nepaliEvents = listOf<CalendarRecord>(
    $nepaliEvents
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
        coordinates = Coordinates($latitude, $longitude, $elevation)
    )"""
            }
        }.joinToString(",\n    ")
        citiesOutput.writeText(
            """package ${android.defaultConfig.applicationId}.generated

import com.byagowi.persiancalendar.entities.CityItem
import io.github.persiancalendar.praytimes.Coordinates

val citiesStore = mapOf(
    $cities
)
""")

        // Districts
        val districts = (JsonSlurper().parse(districtsJson) as Map<*, *>).mapNotNull { province ->
            val provinceName = province.key as String
            if (provinceName.startsWith("#")) return@mapNotNull null
            "\"$provinceName\" to listOf(\n" + (province.value as Map<*, *>).map { county ->
                val key = county.key as String
                """       "$key;""" + (county.value as Map<*, *>).map { district ->
                    val coordinates = district.value as Map<*, *>
                    val latitude = (coordinates["lat"] as Number).toDouble()
                    val longitude = (coordinates["long"] as Number).toDouble()
                    // Remove what is in the parenthesis
                    val name = district.key.toString().split("(")[0]
                    "$name:$latitude:$longitude"
                }.joinToString(";") + "\""
            }.joinToString(",\n") + "\n    )"
        }.joinToString(",\n    ")
        districtsOutput.writeText(
            """package ${android.defaultConfig.applicationId}.generated

import com.byagowi.persiancalendar.entities.CityItem
import io.github.persiancalendar.praytimes.Coordinates

val distrcitsStore = listOf(
    $districts
)
""")
    }
}
tasks.named("preBuild").configure { dependsOn(generateAppSrcTask) }

//// Just a personal debugging tool, isn't that useful as it doesn't resolve all the dependencies
//// later to be completed by ideas of
//// https://github.com/ProtonMail/proton-mail-android/blob/release/scripts/extract_dependencies/ExtractDeps.kts
//val dependenciesURLs: Sequence<Pair<String, URL?>>
//    get() = project.configurations.getByName(
//        "implementation"
//    ).dependencies.asSequence().mapNotNull {
//        it.run { "$group:$name:$version" } to project.repositories.mapNotNull { repo ->
//            (repo as? UrlArtifactRepository)?.url
//        }.flatMap { repoUrl ->
//            "%s/%s/%s/%s/%s-%s".format(
//                repoUrl.toString().trimEnd('/'),
//                it.group?.replace('.', '/') ?: "", it.name, it.version,
//                it.name, it.version
//            ).let { x -> listOf("$x.jar", "$x.aar") }
//        }.toList().firstNotNullOfOrNull { url ->
//            runCatching {
//                val connection = URL(url).openConnection()
//                connection.getInputStream() ?: throw Exception()
//                connection.url
//            }.getOrNull()
//        }
//    }
//tasks.register("printDependenciesURLs") {
//    doLast {
//        dependenciesURLs.forEach { (dependency: String, url: URL?) -> println("$dependency => $url") }
//    }
//}

// Called like: ./gradlew moveToApiFlavors -PfileName=
tasks.register("moveToApiFlavors") {
    doLast {
        val source = gradle.startParameter.projectProperties["fileName"]
            ?: error("Moves a source file to api flavors\nPass -P fileName=FILENAME to this")
        if ("/main/" !in source) error("File name should be a source file in the main flavor")
        if (!File(source).isFile) error("Source file name doesn't exist")
        val minApi17Target = source.replace("/main/", "/minApi17/")
        File(File(minApi17Target).parent).mkdirs()
        val minApi21Target = source.replace("/main/", "/minApi21/")
        File(File(minApi21Target).parent).mkdirs()
        println("cp $source $minApi21Target".execute().text)
        println("git add $minApi21Target".execute().text)
        println("git mv $source $minApi17Target".execute().text)
        println("git status".execute().text)
    }
}

tasks.register("mergeWeblate") {
    doLast {
        val weblateRepository = "https://hosted.weblate.org/git/persian-calendar/persian-calendar/"
        println("git remote add weblate $weblateRepository".execute().text)
        println("git remote update weblate".execute().text)
        println("git merge weblate/main".execute().text)
    }
}
