import org.codehaus.groovy.runtime.ProcessGroovyMethods

operator fun File.div(child: String) = File(this, child)
fun String.execute() = ProcessGroovyMethods.execute(this)
val Process.text: String? get() = ProcessGroovyMethods.getText(this)

val isFirebaseBuildType = gradle.startParameter.taskNames.any { "Nightly" in it || "nightly" in it }

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("androidx.navigation.safeargs.kotlin")
    id("io.github.persiancalendar.appbuildplugin") apply true
}

if (isFirebaseBuildType) {
    plugins.apply("com.google.gms.google-services")
    plugins.apply("com.google.firebase.firebase-perf")
    plugins.apply("com.google.firebase.crashlytics")
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

// https://developer.android.com/jetpack/androidx/releases/compose-kotlin
val composeVersion = "1.1.1"
val composeSecondaryVersion = "1.1.1"

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
        // = if (enableFirebaseInNightlyBuilds) 19 else 17
        minSdk = 17
        targetSdk = 31
        versionCode = 750
        versionName = "7.5.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        if (!isMinApi21Build) vectorDrawables.useSupportLibrary = true
        resourceConfigurations += listOf(
            "en", "fa", "ckb", "ar", "ur", "ps", "glk", "azb", "ja", "fr", "es", "tr", "kmr", "tg",
            "ne", "zh-rCN"
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

    testOptions {
        unitTests.all {
            it.useJUnitPlatform()
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
            multiDexEnabled = false
            buildConfigField("boolean", "DEVELOPMENT", "true")
        }

        create("firebase") {
            initWith(getByName("nightly"))
            applicationIdSuffix = ".nightly.firebase"
            matchingFallbacks += listOf("nightly")
        }

        getByName("debug") {
            versionNameSuffix = "-${defaultConfig.versionName}-$gitVersion"
            buildConfigField("boolean", "DEVELOPMENT", "true")
            applicationIdSuffix = ".debug"
            multiDexEnabled = true
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
    flavorDimensions += listOf("api")

    productFlavors {
        create("minApi17") {
            dimension = "api"
        }
        create("minApi19") {
            applicationIdSuffix = ".minApi19"
            dimension = "api"
            minSdk = 19
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

    lint { disable += listOf("MissingTranslation") }
}

val minApi21Implementation by configurations
val firebaseImplementation by configurations

dependencies {
    implementation("com.github.persian-calendar:equinox:2.0.0")
    implementation("com.github.persian-calendar:calendar:1.2.0")
    implementation("com.github.persian-calendar:praytimes:2.1.2")
    implementation("com.github.persian-calendar:calculator:9a8b4980873f8acf83cf119cf9bf3e31e5259c1d")

    implementation("androidx.appcompat:appcompat:1.4.1")
    implementation("androidx.preference:preference-ktx:1.2.0")
    implementation("androidx.recyclerview:recyclerview:1.2.1")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.viewpager2:viewpager2:1.0.0")
    implementation("androidx.dynamicanimation:dynamicanimation:1.0.0")
    implementation("com.google.android.material:material:1.5.0")

    val navVersion = "2.4.1"
    implementation("androidx.navigation:navigation-fragment-ktx:$navVersion")
    implementation("androidx.navigation:navigation-ui-ktx:$navVersion")
    androidTestImplementation("androidx.navigation:navigation-testing:$navVersion")

    implementation("androidx.core:core-ktx:1.7.0")
    val fragmentVersion = "1.4.1"
    implementation("androidx.fragment:fragment-ktx:$fragmentVersion")
    debugImplementation("androidx.fragment:fragment-testing:$fragmentVersion")
    implementation("androidx.activity:activity-ktx:1.4.0")

    implementation("androidx.browser:browser:1.4.0")

    implementation("androidx.work:work-runtime-ktx:2.7.1")

    val coroutinesVersion = "1.6.0"
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.7.3")

    implementation("com.google.openlocationcode:openlocationcode:1.0.4")
    implementation("com.google.zxing:core:3.4.1")

    // Only needed for debug builds for now, won't be needed for minApi21 builds either
    debugImplementation("com.android.support:multidex:2.0.0")

    // For development builds only, they aren't and most likely won't ever be used in stable releases
    firebaseImplementation(platform("com.google.firebase:firebase-bom:29.1.0"))
    // BoM specifies individual Firebase libraries versions so we don't need to.
    firebaseImplementation("com.google.firebase:firebase-crashlytics-ktx")
    firebaseImplementation("com.google.firebase:firebase-analytics-ktx")
    firebaseImplementation("com.google.firebase:firebase-perf-ktx")

    minApi21Implementation("androidx.activity:activity-compose:1.4.0")
    minApi21Implementation("com.google.android.material:compose-theme-adapter:1.1.5")
    val accompanistVersion = "0.23.1"
    minApi21Implementation("com.google.accompanist:accompanist-flowlayout:$accompanistVersion")
    minApi21Implementation("com.google.accompanist:accompanist-drawablepainter:$accompanistVersion")
    minApi21Implementation("androidx.compose.ui:ui:$composeVersion")
    minApi21Implementation("androidx.compose.material:material:$composeSecondaryVersion")
    minApi21Implementation("androidx.compose.material3:material3:1.0.0-alpha07")
    minApi21Implementation("androidx.compose.ui:ui-tooling-preview:$composeSecondaryVersion")
    if (isMinApi21Build) {
        implementation("androidx.compose.runtime:runtime:$composeVersion")
        androidTestImplementation("androidx.compose.ui:ui-test-junit4:$composeSecondaryVersion")
    }
    minApi21Implementation("androidx.compose.ui:ui-tooling:$composeSecondaryVersion")

    // debugImplementation("com.squareup.leakcanary:leakcanary-android:2.8.1")

    testImplementation("junit:junit:4.13.2")

    testImplementation("org.junit.platform:junit-platform-runner:1.8.2")
    val junit5Version = "5.8.2"
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junit5Version")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junit5Version")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junit5Version")

    testImplementation("com.google.truth:truth:1.1.3")
    // Scratch.kt only dependencies
    // testImplementation("com.squareup.okhttp3:okhttp:3.10.0")
    // testImplementation("org.json:json:20210307")
    // testImplementation("com.ibm.icu:icu4j:68.2")

    val androidTestVersion = "1.4.0"
    androidTestImplementation("androidx.test:runner:$androidTestVersion")
    androidTestImplementation("androidx.test:rules:$androidTestVersion")
    androidTestImplementation("androidx.test:core-ktx:$androidTestVersion")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    val espressoVersion = "3.4.0"
    androidTestImplementation("androidx.test.espresso:espresso-contrib:$espressoVersion")
    androidTestImplementation("androidx.test.espresso:espresso-core:$espressoVersion")
}

tasks.named("preBuild").configure { dependsOn(getTasksByName("codegenerators", false)) }

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
