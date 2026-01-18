plugins {
    alias(libs.plugins.com.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.plugin.serialization)
    id("io.github.persiancalendar.appbuildplugin")
}

android {
    sourceSets {
        operator fun File.div(child: String): File = File(this, child)
        val generatedAppSrcDir =
            layout.buildDirectory.get().asFile / "generated" / "source" / "appsrc" / "main"
        getByName("main").kotlin.srcDir(generatedAppSrcDir)
    }

    compileSdk = 36

    buildFeatures {
        buildConfig = true
        compose = true
    }

    val gitInfo = providers.of(io.github.persiancalendar.gradle.GitInfoValueSource::class) {}.get()

    namespace = "com.byagowi.persiancalendar"

    defaultConfig {
        applicationId = "com.byagowi.persiancalendar"
        minSdk = 23
        targetSdk = 36
        versionCode = 1010
        versionName = "10.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // It lowers the APK size and prevents crash in AboutScreen in API 21-23
        vectorDrawables.useSupportLibrary = true
        androidResources.localeFilters += listOf(
            "en",
            "fa",
            "ckb",
            "ar",
            "ur",
            "ps",
            "glk",
            "azb",
            "ja",
            "fr",
            "es",
            "tr",
            "kmr",
            "tg",
            "ne",
            "zh-rCN",
            "ru",
            "pt",
            "it",
            "ta",
            "de",
            "ota",
        )
        base.archivesName.set("PersianCalendar-$versionName-$gitInfo")
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
            buildConfigField("boolean", "DEVELOPMENT", "true")
        }

        getByName("debug") {
            versionNameSuffix = "-${defaultConfig.versionName}-$gitInfo"
            buildConfigField("boolean", "DEVELOPMENT", "true")
            applicationIdSuffix = ".debug"
        }

        getByName("release") {
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            isMinifyEnabled = true
            isShrinkResources = true
            buildConfigField("boolean", "DEVELOPMENT", "false")
        }
    }
    flavorDimensions += listOf("api")

    packaging {
        resources.excludes += "DebugProbesKt.bin"
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
        // Maybe we can drop the only native dependency? At least it works and doesn't lead to crash
        // jniLibs.excludes.add("lib/*/libandroidx.graphics.path.so")
    }

    bundle {
        // We have in app locale change and don't want Google Play's dependency so better to disable
        language.enableSplit = false
    }

    compileOptions {
        val javaVersion = JavaVersion.VERSION_21
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }

    lint {
        warningsAsErrors = true
        abortOnError = true
        checkAllWarnings = true
        checkReleaseBuilds = true
        checkDependencies = true
        checkTestSources = true
        checkGeneratedSources = true
        baseline = file("lint-baseline.xml") // To update: ./gradlew updateLintBaseline
        disable += listOf(
            "MissingTranslation",
            "SelectableText",
            "IconLocation",
            "DuplicateStrings",
        )
    }

    kotlin {
        compilerOptions {
            allWarningsAsErrors.set(true)
        }
    }
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
    implementation(libs.dynamicanimation)
    implementation(libs.androidx.core.ktx)
    implementation(libs.bundles.lifecycle)
    implementation(libs.browser)
    implementation(libs.work.manager.ktx)
    implementation(libs.kotlinx.coroutines.android)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    implementation(libs.kotlinx.html.jvm)
    implementation(libs.openlocationcode)
    implementation(libs.activity.ktx)

    val composeBom = platform(libs.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom)
    implementation(libs.compose.activity)
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.compose.animation)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.runtime)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.kotlinx.serialization.core)

    androidTestImplementation(libs.compose.ui.test.junit4)
    debugImplementation(libs.compose.ui.test.manifest)
    debugImplementation(libs.compose.ui.tooling)

    testImplementation(libs.junit)

    testImplementation(kotlin("test"))

    testImplementation(libs.junit.platform.runner)
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.params)
    testRuntimeOnly(libs.junit.jupiter.engine)

    androidTestImplementation(libs.test.runner)
    androidTestImplementation(libs.test.rules)
    androidTestImplementation(libs.test.core.ktx)
    androidTestImplementation(libs.androidx.test.ext.junit)

    lintChecks(project(":lintChecks"))
}

tasks.named("preBuild").configure { dependsOn(getTasksByName("codegenerators", false)) }
