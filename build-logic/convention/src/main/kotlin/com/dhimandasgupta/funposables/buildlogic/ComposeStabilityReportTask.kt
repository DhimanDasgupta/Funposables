package com.dhimandasgupta.funposables.buildlogic

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

/** Summarises the Compose compiler's stability reports for one variant. */
@DisableCachingByDefault(because = "Cheap text parse that prints its summary on every run")
abstract class ComposeStabilityReportTask : DefaultTask() {
  @get:Input abstract val variantName: Property<String>

  @get:InputDirectory
  @get:PathSensitive(PathSensitivity.RELATIVE)
  abstract val reportsDir: DirectoryProperty

  @get:OutputFile abstract val summaryFile: RegularFileProperty

  @TaskAction
  fun analyse() {
    val dir = reportsDir.get().asFile
    fun report(suffix: String) =
      dir.listFiles().orEmpty().firstOrNull { it.name.endsWith(suffix) }
        ?: throw GradleException(
          "Missing *$suffix in $dir. Re-run with --rerun-tasks if compilation was up-to-date."
        )

    // classes.txt: every class that is not stable (unstable, or `runtime` = decided at runtime).
    val classHeader = Regex("^(unstable|runtime) class (\\S+) \\{")
    val unstableClasses =
      report("-classes.txt")
        .readLines()
        .mapNotNull { classHeader.find(it)?.destructured }
        .map { (stability, name) -> "$stability  $name" }

    // composables.txt: restartable-but-not-skippable functions, and any function taking unstable
    // params.
    val funHeader = Regex("^(.*?)fun (\\S+?)\\($")
    val nonSkippable = mutableListOf<String>()
    val unstableParams = mutableListOf<String>()
    var currentFun: String? = null
    report("-composables.txt").forEachLine { line ->
      val header = funHeader.find(line)
      when {
        header != null -> {
          val (modifiers, name) = header.destructured
          currentFun = name
          if ("restartable" in modifiers && "skippable" !in modifiers) nonSkippable += name
        }
        line.trimStart().startsWith("unstable ") ->
          unstableParams += "$currentFun  ->  ${line.trim().removePrefix("unstable ")}"
      }
    }

    val metrics =
      dir
        .walkTopDown()
        .firstOrNull { it.name.endsWith("-module.json") }
        ?.readLines()
        ?.map { it.trim().trimEnd(',') }
        ?.filter { line -> METRIC_KEYS.any { line.startsWith("\"$it\"") } }
        .orEmpty()

    val summary = buildString {
      appendLine("Compose stability report — ${variantName.get()}")
      appendLine()
      appendLine("Metrics:")
      metrics.forEach { appendLine("  $it") }
      appendLine()
      appendLine("Non-stable classes (${unstableClasses.size}):")
      unstableClasses.forEach { appendLine("  $it") }
      appendLine()
      appendLine("Restartable but not skippable composables (${nonSkippable.size}):")
      nonSkippable.forEach { appendLine("  $it") }
      appendLine()
      appendLine("Unstable composable parameters (${unstableParams.size}):")
      unstableParams.forEach { appendLine("  $it") }
      appendLine()
      appendLine("Raw reports: $dir")
    }
    summaryFile.get().asFile.writeText(summary)
    logger.lifecycle(summary)
  }

  private companion object {
    val METRIC_KEYS =
      listOf(
        "skippableComposables",
        "restartableComposables",
        "totalComposables",
        "knownUnstableArguments",
        "inferredUnstableClasses",
        "inferredUncertainClasses",
        "StrongSkipping",
      )
  }
}
