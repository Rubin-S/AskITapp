plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.secrets) apply false
}

// ---------------------------------------------------------------------------------------------
// Static analysis: ktlint and detekt (+ compose rules) run as CLI tools from JavaExec tasks.
//
// The ktlint-gradle and detekt Gradle plugins key off the standalone Kotlin Gradle plugins
// (org.jetbrains.kotlin.android/jvm). This project uses AGP 9's built-in Kotlin, so the CLIs are
// wired directly instead: same rules, no dependency on plugin/AGP compatibility.
//
//   ./gradlew ktlintCheck      fail on new ktlint violations (config/ktlint/baseline.xml)
//   ./gradlew ktlintFormat     auto-fix what ktlint can
//   ./gradlew detekt           fail on new detekt violations (config/detekt/baseline.xml)
//   ./gradlew detektBaseline   regenerate the detekt baseline (only when intentionally accepting debt)
// ---------------------------------------------------------------------------------------------

fun Configuration.jvmToolClasspath() {
    isCanBeConsumed = false
    attributes {
        // ktlint and its CLI dependencies (e.g. clikt) are Kotlin Multiplatform libraries: ask for
        // the plain JVM runtime variant explicitly, as there is no Kotlin/Java plugin on the root.
        attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
        attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.LIBRARY))
        attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.EXTERNAL))
        attribute(TargetJvmEnvironment.TARGET_JVM_ENVIRONMENT_ATTRIBUTE, objects.named(TargetJvmEnvironment.STANDARD_JVM))
    }
}

val ktlintCli: Configuration = configurations.create("ktlintCli") { jvmToolClasspath() }
val detektCli: Configuration = configurations.create("detektCli") { jvmToolClasspath() }

dependencies {
    ktlintCli(libs.ktlint.cli)
    detektCli(libs.detekt.cli)
    // Compose rules are loaded by detekt's ServiceLoader from the same classpath.
    detektCli(libs.compose.rules.detekt)
}

val kotlinSourceGlobs = listOf("app/src/**/*.kt", "designsystem/src/**/*.kt", "*.gradle.kts", "*/*.gradle.kts", "!**/build/**")
val kotlinSourceTrees =
    fileTree(rootDir) {
        include("app/src/**/*.kt", "designsystem/src/**/*.kt", "*.gradle.kts", "*/*.gradle.kts")
        exclude("**/build/**")
    }
val ktlintBaseline = layout.projectDirectory.file("config/ktlint/baseline.xml")

tasks.register<JavaExec>("ktlintCheck") {
    group = "verification"
    description = "Checks Kotlin sources with ktlint (new violations only; see config/ktlint/baseline.xml)."
    classpath = ktlintCli
    mainClass.set("com.pinterest.ktlint.Main")
    workingDir = rootDir
    inputs.files(kotlinSourceTrees)
    inputs.file(".editorconfig")
    inputs.files(ktlintBaseline).optional()
    val report = layout.buildDirectory.file("reports/ktlint/ktlint.txt")
    outputs.file(report)
    args(
        "--relative",
        "--baseline=${ktlintBaseline.asFile.relativeTo(rootDir).path}",
        "--reporter=plain",
        "--reporter=plain,output=${report.get().asFile.absolutePath}",
    )
    args(kotlinSourceGlobs)
}

tasks.register<JavaExec>("ktlintFormat") {
    group = "formatting"
    description = "Auto-formats Kotlin sources with ktlint."
    classpath = ktlintCli
    mainClass.set("com.pinterest.ktlint.Main")
    workingDir = rootDir
    isIgnoreExitValue = true
    args("--relative", "--format")
    args(kotlinSourceGlobs)
}

val detektInputs =
    listOf(
        "app/src/main/java",
        "app/src/demo/java",
        "app/src/release/java",
        "app/src/preview/java",
        "app/src/test/java",
        "app/src/testRelease/java",
        "designsystem/src/main/java",
    ).filter { file(it).isDirectory }
val detektConfig = layout.projectDirectory.file("config/detekt/detekt.yml")
val detektBaselineFile = layout.projectDirectory.file("config/detekt/baseline.xml")

fun JavaExec.configureDetekt() {
    classpath = detektCli
    mainClass.set("dev.detekt.cli.Main")
    workingDir = rootDir
    inputs.files(detektInputs.map { fileTree(it) })
    inputs.file(detektConfig)
    detektInputs.forEach { args("--input", it) }
    args(
        "--config",
        detektConfig.asFile.relativeTo(rootDir).path,
        "--build-upon-default-config",
        "--parallel",
    )
}

tasks.register<JavaExec>("detekt") {
    group = "verification"
    description = "Runs detekt with compose rules (new violations only; see config/detekt/baseline.xml)."
    configureDetekt()
    inputs.files(detektBaselineFile).optional()
    val reportDir = layout.buildDirectory.dir("reports/detekt")
    outputs.dir(reportDir)
    args(
        "--baseline",
        detektBaselineFile.asFile.relativeTo(rootDir).path,
        "--report",
        "html:${reportDir.get().file("detekt.html").asFile.absolutePath}",
        "--report",
        "checkstyle:${reportDir.get().file("detekt.xml").asFile.absolutePath}",
    )
}

tasks.register<JavaExec>("detektBaseline") {
    group = "verification"
    description = "Regenerates config/detekt/baseline.xml from the current violations."
    configureDetekt()
    args("--baseline", detektBaselineFile.asFile.relativeTo(rootDir).path, "--create-baseline")
}
