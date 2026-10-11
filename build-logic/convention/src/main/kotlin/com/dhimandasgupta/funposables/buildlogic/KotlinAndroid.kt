package com.dhimandasgupta.funposables.buildlogic

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

/** Shared Android and Kotlin configuration for this project's Android modules. */
internal fun Project.configureKotlinAndroid(applicationExtension: ApplicationExtension) {
  applicationExtension.apply {
    compileSdk = 37

    defaultConfig {
      minSdk = 28
      targetSdk = 37
      testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
      sourceCompatibility = JavaVersion.VERSION_21
      targetCompatibility = JavaVersion.VERSION_21
    }

    testOptions { unitTests.isReturnDefaultValues = true }
  }

  extensions.configure<KotlinAndroidProjectExtension> {
    compilerOptions { jvmTarget.set(JvmTarget.JVM_21) }
  }
}
