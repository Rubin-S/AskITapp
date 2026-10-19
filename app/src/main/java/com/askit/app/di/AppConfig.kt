package com.askit.app.di

import com.askit.app.BuildConfig

/** Build-time switches the object graph depends on, so tests can build a graph without BuildConfig. */
data class AppConfig(
    /** Base URL of the AskIT backend, or null when this build has none configured. */
    val backendBaseUrl: String?,
    /** Client-preview build: local personas, demo content and the "view as other party" tool. */
    val clientPreview: Boolean,
    /**
     * Testable demo build (a client preview that starts on the Welcome screen with an "Explore demo"
     * path, shows the local OTP and adds the demo menu). Always false outside the `demo` build type.
     */
    val demoBuild: Boolean = false,
) {
    companion object {
        fun fromBuildConfig(): AppConfig = AppConfig(
            backendBaseUrl = BuildConfig.BACKEND_BASE_URL.takeIf { it.isNotBlank() },
            clientPreview = BuildConfig.CLIENT_PREVIEW,
            demoBuild = BuildConfig.DEMO_BUILD,
        )
    }
}
