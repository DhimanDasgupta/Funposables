import com.android.build.api.dsl.ApplicationExtension
import com.dhimandasgupta.funposables.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidApplicationComposeConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      apply(plugin = "funposables.android.application")
      apply(plugin = "org.jetbrains.kotlin.plugin.compose")

      extensions.configure<ApplicationExtension> { buildFeatures { compose = true } }

      dependencies {
        val bom = platform(libs.findLibrary("androidx-compose-bom").get())
        "implementation"(bom)
        "androidTestImplementation"(bom)
        "implementation"(libs.findLibrary("androidx-ui-tooling-preview").get())
        "debugImplementation"(libs.findLibrary("androidx-ui-tooling").get())
      }
    }
  }
}
