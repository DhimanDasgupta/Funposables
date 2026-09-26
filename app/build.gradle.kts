import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.kotlinx.serialization)
  alias(libs.plugins.metro)
}

kotlin {
  compilerOptions {
    jvmTarget.set(JvmTarget.JVM_21)
  }
}

// Compose compiler stability reports, generated per variant into build/compose_compiler/<variant>.
// Enabled with -PcomposeReports=true, or automatically when a composeStability* task is requested.
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
    // Incremental compilation only reports the recompiled files, leaving partial reports. Set at
    // execution time because the Kotlin plugin overrides a configuration-time value.
    doFirst { (this as KotlinCompile).incremental = false }
  }
}

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

val composeStability =
  tasks.register("composeStability") {
    group = "compose"
    description = "Generates Compose stability summaries for all variants."
  }

androidComponents {
  onVariants { variant ->
    val variantTask = variant.name.replaceFirstChar { it.uppercase() }
    val task =
      tasks.register<ComposeStabilityReportTask>("composeStability$variantTask") {
        group = "compose"
        description =
          "Generates the Compose compiler stability summary for the ${variant.name} variant."
        variantName = variant.name
        reportsDir = layout.buildDirectory.dir("compose_compiler/${variant.name}")
        summaryFile = layout.buildDirectory.file("reports/compose-stability/${variant.name}.txt")
        dependsOn("compile${variantTask}Kotlin")
        // Cheap text parse; always run so the summary is printed on every invocation.
        outputs.upToDateWhen { false }
      }
    composeStability.configure { dependsOn(task) }
  }
}

android {
  namespace = "com.dhimandasgupta.funposables"
  compileSdk = 37

  defaultConfig {
    applicationId = "com.dhimandasgupta.funposables"
    minSdk = 28
    targetSdk = 37
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro",
      )
    }
  }

  val javaVersion = JavaVersion.VERSION_21
  compileOptions {
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
  }

  buildFeatures {
    buildConfig = true
    compose = true
  }

  testOptions {
    unitTests.isReturnDefaultValues = true
  }
}

dependencies {
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.splash.screen)
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.ui)
  implementation(libs.androidx.ui.graphics)
  implementation(libs.androidx.ui.tooling.preview)
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
  debugImplementation(libs.androidx.ui.tooling)
  debugImplementation(libs.androidx.ui.test.manifest)

  // Android Implementation
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.ui.test.junit4)
}
