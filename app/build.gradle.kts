plugins {
  alias(libs.plugins.funposables.android.application)
  alias(libs.plugins.funposables.android.application.compose)
  alias(libs.plugins.funposables.compose.stability)
  alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.kotlinx.serialization)
  alias(libs.plugins.metro)
}

android {
  namespace = "com.dhimandasgupta.funposables"

  defaultConfig {
    applicationId = "com.dhimandasgupta.funposables"
    versionCode = 1
    versionName = "1.0"
  }

  buildTypes {
    release {
      isMinifyEnabled = true
      isShrinkResources = true
      proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro",
      )
    }
  }

  buildFeatures { buildConfig = true }
}

dependencies {
  implementation(libs.androidx.splash.screen)
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.ui)
  implementation(libs.androidx.ui.graphics)
  implementation(libs.androidx.material3)
  implementation(libs.androidx.navigation.runtime.ktx)
  implementation(libs.androidx.material3.window.size.class1)
  implementation(libs.androidx.material.icons)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.coil.compose)
  implementation(libs.androidx.compose.runtime)
  implementation(libs.androidx.palette.ktx)

  // Nav3
  implementation(libs.androidx.navigation3.runtime)
  implementation(libs.androidx.navigation3.ui)
  implementation(libs.kotlinx.serialization.core)
  implementation(libs.androidx.material3.adaptive.navigation3)

  // Flow Redux
  implementation(libs.flow.redux.jvm)
  implementation(libs.flow.redux.extension.jvm)

  implementation(libs.timber)
  implementation(libs.kotlinx.collections.immutable)

  // Test implementation
  testImplementation(libs.junit)

  // Debug implementation
  debugImplementation(libs.androidx.ui.test.manifest)

  // Android Implementation
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.ui.test.junit4)
}
