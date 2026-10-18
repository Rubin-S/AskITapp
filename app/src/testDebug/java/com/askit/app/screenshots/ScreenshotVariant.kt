package com.askit.app.screenshots

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.askit.designsystem.theme.AskITTheme
import com.github.takahirom.roborazzi.captureRoboImage
import org.robolectric.RuntimeEnvironment

/** Light/dark x English/Tamil: the app ships both locales and both themes. */
internal enum class ScreenshotVariant(val qualifiers: String, val suffix: String) {
    LightEn("+en-notnight", "light_en"),
    DarkEn("+en-night", "dark_en"),
    LightTa("+ta-notnight", "light_ta"),
    DarkTa("+ta-night", "dark_ta"),
}

/**
 * Captures [content] once per [ScreenshotVariant] into src/testDebug/screenshots.
 * Record: ./gradlew :app:recordRoborazziDebug   Verify: ./gradlew :app:verifyRoborazziDebug
 */
internal fun snapshot(name: String, content: @Composable () -> Unit) {
    for (variant in ScreenshotVariant.entries) {
        RuntimeEnvironment.setQualifiers(variant.qualifiers)
        captureRoboImage(filePath = "src/testDebug/screenshots/${name}_${variant.suffix}.png") {
            AskITTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    Box(Modifier.padding(16.dp)) { content() }
                }
            }
        }
    }
}
