plugins {
    alias(libs.plugins.com.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.plugin.parcelize)
    alias(libs.plugins.kotlin.plugin.serialization)
    id("io.github.persiancalendar.appbuildplugin")
}

android {
    namespace = "com.byagowi.persiancalendar"
    compileSdk { version = release(37) { minorApiLevel = 1 } }

    defaultConfig {
        applicationId = "com.byagowi.persiancalendar"
        minSdk { version = release(26) }
        targetSdk { version = release(36) }
        versionCode = 53
        versionName = "0.5.3"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        checkGeneratedSources = true
        baseline = file("lint-baseline.xml") // To update: ./gradlew updateLintBaseline
        disable += listOf(
            // Tile previews can't be in webp in wear os as far as I tested
            "ConvertToWebp",
            // Just waste of space to have both versions while square wear os isn't that popular
            "SquareAndRoundTilePreviews",
            // Just waste of space to provide mipmaps
            "IconLocation",
            // Makes the CI fail in unrelated changes
            "GradleDependency",
            "AndroidGradlePluginVersion",
        )
    }

    buildFeatures { compose = true }
}

kotlin {
    compilerOptions {
        allWarningsAsErrors.set(true)
    }
}

dependencies {
    implementation(project(":shared"))

    // Google/JetBrains
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.activity)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.lifecycle.process)
    implementation(libs.wear.compose.foundation)
    implementation(libs.wear.compose.material3)
    implementation(libs.wear.compose.navigation3)
    implementation(libs.wear.datastore.preferences)
    implementation(libs.wear.protolayout.material3)
    implementation(libs.wear.tiles)
    implementation(libs.wear.tooling.preview)
    implementation(libs.wear.watchface.complications.data.source.ktx) {
        // https://issuetracker.google.com/issues/525074818
        exclude("androidx.preference", "preference")
    }
    implementation(libs.work.manager.ktx)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.kotlinx.serialization.core)
    lintChecks(libs.slack.compose.lint.checks)
    testImplementation(kotlin("test-junit"))
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.test.runner)
    androidTestImplementation(libs.test.rules)
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)
    lintChecks(project(":lintChecks"))
}
