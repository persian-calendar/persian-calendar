import org.jetbrains.kotlin.config.KotlinCompilerVersion

plugins {
  id("com.android.application")
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

//    buildTypes {
//        debug {
//            versionNameSuffix "-" + "git rev-parse --abbrev-ref HEAD".execute().text.trim() + "-" +
//                    "git rev-list HEAD --count".execute().text.trim() + "-" +
//                    "git rev-parse --short HEAD".execute().text.trim()
//        }
//        release {
//            minifyEnabled true
//            shrinkResources true
//            // Maybe proguard-android-optimize.txt in future
//            proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.pro'
//        }
//    }

//    lintOptions {
//        abortOnError false
//    }
//
//    compileOptions {
//        sourceCompatibility 1.8
//        targetCompatibility 1.8
//    }
//
  dataBinding {
    isEnabled = true
  }
}

dependencies {

  val androidXVersion = "1.0.0-rc02"
  val leakCanaryVersion = "1.6.1"
  val junitVersion = "4.12"

  implementation("androidx.appcompat:appcompat:$androidXVersion")
  implementation("androidx.preference:preference:$androidXVersion")
  implementation("androidx.recyclerview:recyclerview:$androidXVersion")
  implementation("androidx.cardview:cardview:$androidXVersion")
  implementation("com.google.android.material:material:$androidXVersion")
  implementation("com.google.android:flexbox:1.1.0-beta1")
  implementation("com.google.android.apps.dashclock:dashclock-api:2.0.0")

  // Please apply this https://issuetracker.google.com/issues/112877717 before enabling it again
  // implementation "android.arch.work:work-runtime:1.0.0-alpha07"

  debugImplementation("com.squareup.leakcanary:leakcanary-android:$leakCanaryVersion")
  debugImplementation("com.squareup.leakcanary:leakcanary-support-fragment:$leakCanaryVersion")

  testImplementation("junit:junit:$junitVersion")

  androidTestImplementation("androidx.test:runner:1.1.0-alpha4")
  androidTestImplementation("androidx.test:rules:1.1.0-alpha4")
  androidTestImplementation("androidx.test.espresso:espresso-contrib:3.1.0-alpha4")
  androidTestImplementation("androidx.test.espresso:espresso-core:3.1.0-alpha4")
}
