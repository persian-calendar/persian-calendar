import org.jetbrains.kotlin.config.KotlinCompilerVersion

plugins {
  id("com.android.application")
  kotlin("android")
}

// https://stackoverflow.com/a/52441962
fun String.runCommand(workingDir: File = File("."),
                      timeoutAmount: Long = 60,
                      timeoutUnit: TimeUnit = TimeUnit.MINUTES): String? {
  return try {
    ProcessBuilder(*this.split("\\s".toRegex()).toTypedArray())
        .directory(workingDir)
        .redirectOutput(ProcessBuilder.Redirect.PIPE)
        .redirectError(ProcessBuilder.Redirect.PIPE)
        .start().apply {
          waitFor(timeoutAmount, timeoutUnit)
        }.inputStream.bufferedReader().readText()
  } catch (e: java.io.IOException) {
    e.printStackTrace()
    null
  }
}

android {
  compileSdkVersion(28)

  defaultConfig {
    applicationId = "com.byagowi.persiancalendar"
    minSdkVersion(15)
    targetSdkVersion(28)
    versionCode = 594
    versionName = "5.9.4"
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    vectorDrawables.useSupportLibrary = true
  }

  buildTypes {
    getByName("debug") {
      versionNameSuffix = "-" + arrayOf(
          "git rev-parse --abbrev-ref HEAD",
          "git rev-list HEAD --count",
          "git rev-parse --short HEAD"
      ).map { it.runCommand()?.trim() }.joinToString("-")
    }
    getByName("release") {
      isMinifyEnabled = true
      isShrinkResources = true
      // Maybe proguard-android-optimize.txt in future
      setProguardFiles(listOf(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro"))
    }
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }

  dataBinding.isEnabled = true
}

dependencies {
  val androidXVersion = "1.0.0"
  val leakCanaryVersion = "1.6.1"
  val junitVersion = "4.12"
  val daggerVersion = "2.16"

  implementation("androidx.appcompat:appcompat:$androidXVersion")
  implementation("androidx.preference:preference:$androidXVersion")
  implementation("androidx.recyclerview:recyclerview:$androidXVersion")
  implementation("androidx.cardview:cardview:$androidXVersion")
  implementation("com.google.android.material:material:$androidXVersion")
  implementation("com.google.android:flexbox:1.1.0")
  implementation("com.google.android.apps.dashclock:dashclock-api:2.0.0") {
    because("to provide a DashClock extension")
  }

  // Please apply this https://issuetracker.google.com/issues/112877717 before enabling it again
  // implementation("android.arch.work:work-runtime:1.0.0-alpha07")

  implementation("com.google.dagger:dagger-android:$daggerVersion")
  implementation("com.google.dagger:dagger-android-support:$daggerVersion")
  annotationProcessor("com.google.dagger:dagger-compiler:$daggerVersion")
  annotationProcessor("com.google.dagger:dagger-android-processor:$daggerVersion")

  debugImplementation("com.squareup.leakcanary:leakcanary-android:$leakCanaryVersion")
  debugImplementation("com.squareup.leakcanary:leakcanary-support-fragment:$leakCanaryVersion")

  testImplementation("junit:junit:$junitVersion")
  testImplementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${KotlinCompilerVersion.VERSION}")

  androidTestImplementation("androidx.test:runner:1.1.0-beta01")
  androidTestImplementation("androidx.test:rules:1.1.0-beta01")
  androidTestImplementation("androidx.test.espresso:espresso-contrib:3.1.0-beta01")
  androidTestImplementation("androidx.test.espresso:espresso-core:3.1.0-beta01")
}
