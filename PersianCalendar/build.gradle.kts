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
-        getByName("main").kotlin.srcDir(generatedAppSrcDir)
+        getByName("main").kotlin.directories.add(generatedAppSrcDir.path)
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
             "zh-rCN",
             "zh-rTW",
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
