import org.codehaus.groovy.runtime.ProcessGroovyMethods

operator fun File.div(child: String) = File(this, child)
fun String.execute() = ProcessGroovyMethods.execute(this)
val Process.text: String? get() = ProcessGroovyMethods.getText(this)

plugins {
    id("com.android.application")
    kotlin("android")
    id("androidx.navigation.safeargs.kotlin")
    id("io.github.persiancalendar.appbuildplugin") apply true
}

// https://developer.android.com/jetpack/androidx/releases/compose-kotlin
val composeCompilerVersion = "1.4.4"
val composeVersion = "1.3.3"

val isMinApi21Build = gradle.startParameter.taskNames.any { "minApi21" in it || "MinApi21" in it }

val generatedAppSrcDir = buildDir / "generated" / "source" / "appsrc" / "main"
android {
    sourceSets {
        getByName("main").kotlin.srcDir(generatedAppSrcDir)
    }

    compileSdk = 33
    buildToolsVersion = "33.0.2"

    buildFeatures {
        viewBinding = true
    }

    val gitInfo = providers.of(io.github.persiancalendar.gradle.GitInfoValueSource::class) {}.get()

    namespace = "com.byagowi.persiancalendar"

    defaultConfig {
        applicationId = "com.byagowi.persiancalendar"
        minSdk = 17
        targetSdk = 33
        versionCode = 790
        versionName = "7.9.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        if (!isMinApi21Build) vectorDrawables.useSupportLibrary = true
        resourceConfigurations += listOf(
            "en", "fa", "ckb", "ar", "ur", "ps", "glk", "azb", "ja", "fr", "es", "tr", "kmr", "tg",
            "ne", "zh-rCN"
        )
        setProperty("archivesBaseName", "PersianCalendar-$versionName-$gitInfo")
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
            versionNameSuffix = "-${defaultConfig.versionName}-$gitInfo-nightly"
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

        getByName("debug") {
            versionNameSuffix = "-${defaultConfig.versionName}-$gitInfo"
            buildConfigField("boolean", "DEVELOPMENT", "true")
            applicationIdSuffix = ".debug"
            multiDexEnabled = true
        }

        getByName("release") {
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
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
        kotlinCompilerExtensionVersion = composeCompilerVersion
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

dependencies {
    implementation("com.github.persian-calendar:calendar:1.2.2")
    implementation("com.github.persian-calendar:praytimes:3.0.0")
    implementation("com.github.persian-calendar:calculator:0827f0fbcad2ffa8559f05dcc82002f1dac1464b")

    // https://github.com/cosinekitty/astronomy/releases/tag/v2.1.0
    implementation("com.github.cosinekitty:astronomy:v2.1.17")

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.preference:preference-ktx:1.2.0")
    implementation("androidx.recyclerview:recyclerview:1.3.0")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.viewpager2:viewpager2:1.0.0")
    implementation("androidx.dynamicanimation:dynamicanimation:1.0.0")
    implementation("com.google.android.material:material:1.9.0-beta01")

    val navVersion = "2.5.1"
    implementation("androidx.navigation:navigation-fragment-ktx:$navVersion")
    implementation("androidx.navigation:navigation-ui-ktx:$navVersion")
    androidTestImplementation("androidx.navigation:navigation-testing:$navVersion")

    implementation("androidx.core:core-ktx:1.9.0")
    val fragmentVersion = "1.5.2"
    implementation("androidx.fragment:fragment-ktx:$fragmentVersion")
    debugImplementation("androidx.fragment:fragment-testing:$fragmentVersion")
    implementation("androidx.activity:activity-ktx:1.6.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.0")

    implementation("androidx.browser:browser:1.5.0")

    implementation("androidx.work:work-runtime-ktx:2.8.0")

    val coroutinesVersion = "1.6.4"
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.8.0")

    implementation("com.google.openlocationcode:openlocationcode:1.0.4")
    implementation("com.google.zxing:core:3.5.1")

    // Only needed for debug builds for now, won't be needed for minApi21 builds either
    debugImplementation("androidx.multidex:multidex:2.0.1")

    minApi21Implementation("androidx.activity:activity-compose:1.6.1")
    val accompanistVersion = "0.28.0"
    minApi21Implementation("com.google.accompanist:accompanist-flowlayout:$accompanistVersion")
    minApi21Implementation("com.google.accompanist:accompanist-drawablepainter:$accompanistVersion")
    minApi21Implementation("com.google.accompanist:accompanist-themeadapter-material3:$accompanistVersion")
    minApi21Implementation("androidx.compose.ui:ui:$composeVersion")
    minApi21Implementation("androidx.compose.material3:material3:1.1.0-alpha08")
    minApi21Implementation("androidx.compose.ui:ui-tooling-preview:$composeVersion")
    if (isMinApi21Build) {
        implementation("androidx.compose.runtime:runtime:$composeVersion")
        androidTestImplementation("androidx.compose.ui:ui-test-junit4:$composeVersion")
        debugImplementation("androidx.compose.ui:ui-tooling:$composeVersion")
    }

    // debugImplementation("com.squareup.leakcanary:leakcanary-android:2.8.1")

    testImplementation("junit:junit:4.13.2")

    testImplementation(kotlin("test"))

    testImplementation("org.junit.platform:junit-platform-runner:1.9.2")
    val junit5Version = "5.9.2"
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junit5Version")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junit5Version")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junit5Version")

    testImplementation("org.mockito:mockito-core:5.2.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:4.1.0")

    testImplementation("com.google.truth:truth:1.1.3")

    val androidTestVersion = "1.4.0"
    androidTestImplementation("androidx.test:runner:$androidTestVersion")
    androidTestImplementation("androidx.test:rules:$androidTestVersion")
    androidTestImplementation("androidx.test:core-ktx:$androidTestVersion")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
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
        listOf(
            "cp $source $minApi21Target",
            "git add $minApi21Target",
            "git mv $source $minApi17Target",
            "git status",
        ).forEach { println(it.execute().text) }
    }
}

tasks.register("mergeWeblate") {
    doLast {
        val weblateRepository = "https://hosted.weblate.org/git/persian-calendar/persian-calendar/"
        listOf(
            "git remote add weblate $weblateRepository",
            "git remote update weblate",
            "git merge weblate/main",
        ).forEach { println(it.execute().text) }
    }
}
