import com.android.build.api.variant.HostTestBuilder
import java.net.URI

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.roborazzi)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.secrets)
}

val signRelease = providers.gradleProperty("signRelease").map {
    requireNotNull(it.toBooleanStrictOrNull()) { "signRelease must be true or false." }
}.getOrElse(false)

val backendBaseUrl = providers.gradleProperty("askitBackendBaseUrl")
    .orElse(providers.environmentVariable("ASKIT_BACKEND_BASE_URL"))
    .getOrElse("")
if (backendBaseUrl.isNotEmpty()) {
    val uri = runCatching { URI(backendBaseUrl) }.getOrNull()
    require(
        uri != null && uri.scheme == "https" && !uri.host.isNullOrBlank() &&
            uri.userInfo == null && uri.query == null && uri.fragment == null,
    ) {
        "ASKIT_BACKEND_BASE_URL must be an HTTPS URL without credentials, query, or fragment."
    }
}

android {
    namespace = "com.askit.app"
    compileSdk = 37

    androidResources {
        generateLocaleConfig = true
        localeFilters += listOf("en", "ta")
    }

    defaultConfig {
        applicationId = "com.askit.app"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
        buildConfigField("boolean", "CLIENT_PREVIEW", "false")
        buildConfigField("String", "BACKEND_BASE_URL", "\"$backendBaseUrl\"")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // The secrets plugin sets the real key per app variant; this inert default only exists
        // so the unit-test manifest (merged for Robolectric) has a value to substitute.
        manifestPlaceholders["PLACES_API_KEY"] = "DEFAULT_API_KEY"
    }

    if (signRelease) {
        val requiredNames = listOf(
            "ASKIT_UPLOAD_STORE_FILE",
            "ASKIT_UPLOAD_STORE_PASSWORD",
            "ASKIT_UPLOAD_KEY_ALIAS",
            "ASKIT_UPLOAD_KEY_PASSWORD",
        )
        val credentials = requiredNames.associateWith { providers.environmentVariable(it).orNull }
        val missing = credentials.filterValues { it.isNullOrBlank() }.keys
        require(missing.isEmpty()) { "Release signing requires: ${missing.joinToString()}" }
        val uploadStore = rootProject.file(credentials.getValue("ASKIT_UPLOAD_STORE_FILE")!!)
        require(uploadStore.isFile) { "ASKIT_UPLOAD_STORE_FILE must point to an existing keystore." }
        signingConfigs.create("upload") {
            storeFile = uploadStore
            storePassword = credentials.getValue("ASKIT_UPLOAD_STORE_PASSWORD")
            keyAlias = credentials.getValue("ASKIT_UPLOAD_KEY_ALIAS")
            keyPassword = credentials.getValue("ASKIT_UPLOAD_KEY_PASSWORD")
        }
    }

    buildTypes {
        release {
            if (signRelease) signingConfig = signingConfigs.getByName("upload")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
        create("preview") {
            initWith(getByName("release"))
            applicationIdSuffix = ".preview"
            versionNameSuffix = "-client-preview"
            matchingFallbacks += "release"
            signingConfig = signingConfigs.getByName("debug")
            buildConfigField("boolean", "CLIENT_PREVIEW", "true")
            buildConfigField("String", "BACKEND_BASE_URL", "\"\"")
        }
    }

    sourceSets {
        // Demo fixtures (sample feed, profiles, dashboards, seeded chats/jobs/tasks) compile only into
        // debug and preview. Release gets src/release/java, which supplies empty implementations of
        // the same com.askit.app.variant.AppVariant seam. See docs/build-variants.md.
        getByName("debug").kotlin.directories += "src/demo/java"
        getByName("preview").kotlin.directories += "src/demo/java"
        // Strings only the fixtures use, so release lint does not see them as unused.
        getByName("debug").res.directories += "src/demo/res"
        getByName("preview").res.directories += "src/demo/res"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        // The secrets plugin injects values into both BuildConfig and the manifest.
        buildConfig = true
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

androidComponents {
    beforeVariants(selector().withBuildType("release")) { variant ->
        // AGP only creates host (unit) tests for debug by default. Enable them for release so
        // testReleaseUnitTest runs src/testRelease/ReleaseVariantTest, which fails if any demo
        // fixture is reachable from the release variant.
        variant.hostTests[HostTestBuilder.UNIT_TEST_TYPE]?.enable = true
    }
}

secrets {
    propertiesFileName = "secrets.properties"
    defaultPropertiesFileName = "local.defaults.properties"
}

dependencies {
    implementation(project(":designsystem"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.exifinterface)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.google.places)
    implementation(libs.play.services.location)

    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
    implementation(libs.okhttp)
    implementation(libs.libphonenumber)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui.compose)

    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.video)
    implementation(libs.androidx.camera.compose)

    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.robolectric)
    testImplementation(libs.roborazzi)
    testImplementation(libs.roborazzi.compose)
    testImplementation(libs.roborazzi.junit.rule)
    testImplementation(platform(libs.androidx.compose.bom))
    testImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.core)
    debugImplementation(libs.androidx.compose.ui.tooling)
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
