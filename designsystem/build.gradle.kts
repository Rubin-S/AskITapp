plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.roborazzi)
}

android {
    namespace = "com.askit.designsystem"
    compileSdk = 37

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }

    testOptions {
        // Robolectric/Roborazzi tests need merged resources and the test manifest.
        unitTests.isIncludeAndroidResources = true
    }

    lint {
        warningsAsErrors = true
        abortOnError = true
        checkDependencies = true
        disable += setOf("GradleDependency", "AndroidGradlePluginVersion", "OldTargetApi")
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.coil.compose)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.roborazzi)
    testImplementation(libs.roborazzi.compose)
    testImplementation(platform(libs.androidx.compose.bom))
    testImplementation(libs.androidx.compose.ui.test.junit4)
}

// Screenshot tests (Roborazzi on Robolectric Native Graphics) live under a `screenshots` package.
// Robolectric ships no linux-aarch64 native graphics runtime, so on such hosts they are skipped
// (CI runs on ubuntu x86_64). Pass -Paskit.forceScreenshotTests to run them anyway.
val robolectricNativeGraphicsAvailable =
    !(System.getProperty("os.name") == "Linux" && System.getProperty("os.arch") in setOf("aarch64", "arm64")) ||
        providers.gradleProperty("askit.forceScreenshotTests").isPresent
tasks.withType<Test>().configureEach {
    maxHeapSize = "1536m"
    if (!robolectricNativeGraphicsAvailable) {
        exclude("**/screenshots/**")
        // :designsystem has only screenshot tests, so nothing is left to run on such hosts.
        failOnNoDiscoveredTests.set(false)
    }
}
