plugins {
    alias(libs.plugins.com.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("io.github.persiancalendar.appbuildplugin")
}

android {
    sourceSets {
        operator fun File.div(child: String): File = File(this, child)
        val generatedAppSrcDir =
            layout.buildDirectory.get().asFile / "generated" / "source" / "appsrc" / "main"
        getByName("main").kotlin.srcDir(generatedAppSrcDir)
    }

    namespace = "com.byagowi.persiancalendar"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.byagowi.persiancalendar"
        minSdk = 26
        targetSdk = 36
        versionCode = 51
        versionName = "0.5.1"
        androidResources.localeFilters += listOf("en", "fa")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
        }
    }

    compileOptions {
        val javaVersion = JavaVersion.VERSION_21
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }

    lint {
        warningsAsErrors = true
        checkAllWarnings = true
        checkReleaseBuilds = true
        abortOnError = true
        checkDependencies = true
        checkTestSources = true
        baseline = file("lint-baseline.xml") // To update: ./gradlew updateLintBaseline
        disable += listOf(
            "SquareAndRoundTilePreviews",
            "IconLocation",
            "ConvertToWebp",
            "SyntheticAccessor",
        )
    }

    kotlin {
        compilerOptions {
            allWarningsAsErrors.set(true)
        }
    }

    buildFeatures { compose = true }
}

dependencies {
    implementation(libs.persiancalendar.calendar)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.activity)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.lifecycle.process)
    implementation(libs.wear.compose.foundation)
    implementation(libs.wear.compose.material3)
    implementation(libs.wear.compose.navigation)
    implementation(libs.wear.datastore.preferences)
    implementation(libs.wear.protolayout.material3)
    implementation(libs.wear.tiles)
    implementation(libs.wear.tooling.preview)
    implementation(libs.wear.watchface.complications.data.source.ktx)
    implementation(libs.work.manager.ktx)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test.junit4)
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)
}

tasks.named("preBuild").configure { dependsOn(getTasksByName("eventsgenerators", false)) }
