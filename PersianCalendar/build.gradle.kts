import org.codehaus.groovy.runtime.ProcessGroovyMethods

operator fun File.div(child: String) = File(this, child)
fun String.execute() = ProcessGroovyMethods.execute(this)
val Process.text: String? get() = ProcessGroovyMethods.getText(this)

plugins {
    alias(libs.plugins.com.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.androidx.navigation.safeargs.kotlin)
    // alias(libs.plugins.ksp)
    id("io.github.persiancalendar.appbuildplugin")
}

val generatedAppSrcDir =
    layout.buildDirectory.get().asFile / "generated" / "source" / "appsrc" / "main"
android {
    sourceSets {
        getByName("main").kotlin.srcDir(generatedAppSrcDir)
    }

    compileSdk = 34

    buildFeatures {
        buildConfig = true
        compose = true
    }

    val gitInfo = providers.of(io.github.persiancalendar.gradle.GitInfoValueSource::class) {}.get()

    namespace = "com.byagowi.persiancalendar"

    defaultConfig {
        applicationId = "com.byagowi.persiancalendar"
        minSdk = 21
        targetSdk = 34
        versionCode = 840
        versionName = "8.4.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // It lowers the APK size and prevents crash in AboutScreen in API 21-23
        vectorDrawables.useSupportLibrary = true
        resourceConfigurations += listOf(
            "en", "fa", "ckb", "ar", "ur", "ps", "glk", "azb", "ja", "fr", "es", "tr", "kmr", "tg",
            "ne", "zh-rCN", "ru"
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

    testOptions.unitTests.all { it.useJUnitPlatform() }

    buildTypes {
        create("nightly") {
            signingConfig = signingConfigs.getByName("nightly")
            versionNameSuffix = "-${defaultConfig.versionName}-$gitInfo-nightly"
            applicationIdSuffix = ".nightly"
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
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

    packaging {
        resources.excludes += "DebugProbesKt.bin"
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }

    bundle {
        // We have in app locale change and don't want Google Play's dependency so better to disable
        language.enableSplit = false
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }

    val javaVersion = JavaVersion.VERSION_17

    compileOptions {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
        // isCoreLibraryDesugaringEnabled = true
        //   Actually could be useful as makes use of java.time.Duration possible instead
        //   java.util.concurrent.TimeUnit but needs multidex as it says:
        //     In order to use core library desugaring, please enable multidex.
    }

    kotlinOptions {
        jvmTarget = javaVersion.majorVersion
    }

    lint { disable += listOf("MissingTranslation") }
}

dependencies {
    // Project owned libraries
    implementation(libs.persiancalendar.calendar)
    implementation(libs.persiancalendar.praytimes)
    implementation(libs.persiancalendar.calculator)
    implementation(libs.persiancalendar.qr)

    // https://github.com/cosinekitty/astronomy/releases/tag/v2.1.0
    implementation(libs.astronomy)

    // Google/JetBrains owned libraries (roughly platform libraries)
    implementation(libs.drawerlayout)
    implementation(libs.dynamicanimation)

    implementation(libs.navigation.fragment.ktx)
    androidTestImplementation(libs.navigation.testing)

    implementation(libs.androidx.core.ktx)
    implementation(libs.fragment.ktx)
    debugImplementation(libs.fragment.testing)
    implementation(libs.lifecycle.runtime.ktx)

    implementation(libs.browser)

    implementation(libs.work.manager.ktx)

    implementation(libs.kotlinx.coroutines.android)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    implementation(libs.kotlinx.html.jvm)

    implementation(libs.openlocationcode)

    // Not used directly on the app but is used by work manager anyway
    implementation(libs.bundles.room)
    annotationProcessor(libs.room.compiler)
    // ksp(libs.room.compiler)

    implementation(libs.activity.ktx)
    implementation(libs.compose.activity)

    implementation(libs.compose.accompanist.flowlayout)
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.navigation)
    implementation(libs.compose.animation)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.runtime)
    implementation(libs.compose.material.icons.extended)
    androidTestImplementation(libs.compose.ui.test.junit4)
    debugImplementation(libs.compose.ui.test.manifest)
    debugImplementation(libs.compose.ui.tooling)

//    implementation(libs.datastore.preferences)

    // implementation(libs.androidx.glance.appwidget)
    // implementation(libs.androidx.glance.material3)

    // debugImplementation(libs.leakcanary)

    testImplementation(libs.junit)

    testImplementation(kotlin("test"))

    testImplementation(libs.junit.platform.runner)
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.params)
    testRuntimeOnly(libs.junit.jupiter.engine)

    testImplementation(libs.bundles.mockito)

    testImplementation(libs.truth)

    androidTestImplementation(libs.test.runner)
    androidTestImplementation(libs.test.rules)
    androidTestImplementation(libs.test.core.ktx)
    androidTestImplementation(libs.androidx.test.ext.junit)

    androidTestImplementation(libs.bundles.espresso)
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

// Can be called like: ./gradlew mergeWeblate
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
