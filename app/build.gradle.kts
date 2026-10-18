plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.roborazzi)
}

// Release signing comes only from a real upload key supplied via environment variables
// (KEYSTORE_PATH, STORE_PASSWORD, KEY_ALIAS, KEY_PASSWORD). Without one, release artifacts are
// left unsigned on purpose instead of silently falling back to a debug key.
val uploadKeystore: File? = System.getenv("KEYSTORE_PATH")?.let { file(it) }?.takeIf { it.exists() }

// Single source of truth for the version; also names the output files
// (e.g. SouthernIndianaLore-1.0.0-debug.apk).
val appVersionName = "1.0.0"
base { archivesName.set("SouthernIndianaLore-$appVersionName") }

android {
  namespace = "com.example"
  compileSdk { version = release(36) { minorApiLevel = 1 } }

  defaultConfig {
    applicationId = "com.aistudio.southernindianalore.qvmtzy"
    minSdk = 24
    targetSdk = 36
    versionCode = 1
    versionName = appVersionName

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
  }

  signingConfigs {
    if (uploadKeystore != null) {
      create("release") {
        storeFile = uploadKeystore
        storePassword = System.getenv("STORE_PASSWORD")
        keyAlias = System.getenv("KEY_ALIAS") ?: "upload"
        keyPassword = System.getenv("KEY_PASSWORD")
      }
    }
  }

  buildTypes {
    release {
      isCrunchPngs = false
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      signingConfig = signingConfigs.findByName("release")
    }
    debug {
      // Installs side by side with any release/older copy of the app (separate app ID, separate
      // data), so a test build never collides with or wipes an existing install. The launcher
      // label is overridden in src/debug/res/values/strings.xml.
      // Signing uses the standard debug keystore AGP generates in ~/.android.
      applicationIdSuffix = ".debug"
      versionNameSuffix = "-debug"
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  buildFeatures {
    compose = true
    buildConfig = true
  }
  testOptions { unitTests { isIncludeAndroidResources = true } }
  lint {
    abortOnError = false
    checkReleaseBuilds = false
  }
}

// The app is offline-first: no API keys, no Firebase, no analytics. Keep it that way.
dependencies {
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)
  implementation(libs.coil.compose)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.play.services.location)
  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  testImplementation(libs.roborazzi)
  testImplementation(libs.roborazzi.compose)
  testImplementation(libs.roborazzi.junit.rule)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)
  "ksp"(libs.androidx.room.compiler)
}
