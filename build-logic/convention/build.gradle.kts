import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins { `kotlin-dsl` }

group = "com.dhimandasgupta.funposables.buildlogic"

java {
  sourceCompatibility = JavaVersion.VERSION_21
  targetCompatibility = JavaVersion.VERSION_21
}

kotlin { compilerOptions { jvmTarget.set(JvmTarget.JVM_21) } }

dependencies {
  compileOnly(libs.android.gradlePlugin)
  compileOnly(libs.kotlin.gradlePlugin)
}

tasks {
  validatePlugins {
    enableStricterValidation = true
    failOnWarning = true
  }
}

gradlePlugin {
  plugins {
    register("androidApplication") {
      id = libs.plugins.funposables.android.application.asProvider().get().pluginId
      implementationClass = "AndroidApplicationConventionPlugin"
    }
    register("androidApplicationCompose") {
      id = libs.plugins.funposables.android.application.compose.get().pluginId
      implementationClass = "AndroidApplicationComposeConventionPlugin"
    }
    register("composeStability") {
      id = libs.plugins.funposables.compose.stability.get().pluginId
      implementationClass = "ComposeStabilityConventionPlugin"
    }
  }
}
