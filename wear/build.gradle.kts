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
    compileSdk = 35

    defaultConfig {
        applicationId = "com.byagowi.persiancalendar"
        minSdk = 26
        targetSdk = 34
        versionCode = 6
        versionName = "0.0.6"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
        }
    }
    val javaVersion = JavaVersion.VERSION_21
    compileOptions {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }
    kotlinOptions { jvmTarget = javaVersion.majorVersion }

    buildFeatures { compose = true }
}

dependencies {
    implementation(libs.persiancalendar.calendar)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.activity)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.wear.tiles.proto)
    implementation(libs.wear.protolayout.proto)
    implementation(libs.wear.compose.material)
    implementation(libs.wear.compose.foundation)
    implementation(libs.wear.compose.material3)
    implementation(libs.wear.tooling.preview)
    implementation(libs.wear.glance.tiles)
    implementation(libs.wear.watchface.complications.data.source.ktx)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test.junit4)
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)
}

tasks.named("preBuild").configure { dependsOn(getTasksByName("eventsgenerators", false)) }
