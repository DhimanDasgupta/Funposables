import com.android.build.api.variant.AndroidComponentsExtension
import com.dhimandasgupta.funposables.buildlogic.ComposeStabilityReportTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

/**
 * Compose compiler stability reports, generated per variant into build/compose_compiler/<variant>.
 * Enabled with -PcomposeReports=true, or automatically when a composeStability* task is requested.
 */
class ComposeStabilityConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      val composeReportsEnabled =
        providers.gradleProperty("composeReports").orNull == "true" ||
          gradle.startParameter.taskNames.any {
            it.substringAfterLast(':').startsWith("composeStability")
          }

      if (composeReportsEnabled) {
        tasks.withType<KotlinCompile>().configureEach {
          val variant =
            Regex("compile(\\w+)Kotlin")
              .matchEntire(name)
              ?.groupValues
              ?.get(1)
              ?.takeUnless { it.endsWith("UnitTest") || it.endsWith("AndroidTest") }
              ?.replaceFirstChar { it.lowercase() } ?: return@configureEach
          val reportsDir = layout.buildDirectory.dir("compose_compiler/$variant")
          val composePlugin = "plugin:androidx.compose.compiler.plugins.kotlin"
          compilerOptions.freeCompilerArgs.addAll(
            "-P",
            "$composePlugin:reportsDestination=${reportsDir.get().asFile.absolutePath}",
            "-P",
            "$composePlugin:metricsDestination=${reportsDir.get().asFile.absolutePath}",
          )
          // Declared as an output so up-to-date checks and the build cache restore the reports too.
          outputs.dir(reportsDir)
          // Incremental compilation only reports the recompiled files, leaving partial reports. Set
          // at execution time because the Kotlin plugin overrides a configuration-time value.
          doFirst { (this as KotlinCompile).incremental = false }
        }
      }

      val composeStability =
        tasks.register("composeStability") {
          group = "compose"
          description = "Generates Compose stability summaries for all variants."
        }

      extensions.getByType(AndroidComponentsExtension::class.java).onVariants { variant ->
        val variantTask = variant.name.replaceFirstChar { it.uppercase() }
        val task =
          tasks.register<ComposeStabilityReportTask>("composeStability$variantTask") {
            group = "compose"
            description =
              "Generates the Compose compiler stability summary for the ${variant.name} variant."
            variantName.set(variant.name)
            reportsDir.set(layout.buildDirectory.dir("compose_compiler/${variant.name}"))
            summaryFile.set(
              layout.buildDirectory.file("reports/compose-stability/${variant.name}.txt")
            )
            dependsOn("compile${variantTask}Kotlin")
            // Cheap text parse; always run so the summary is printed on every invocation.
            outputs.upToDateWhen { false }
          }
        composeStability.configure { dependsOn(task) }
      }
    }
  }
}
