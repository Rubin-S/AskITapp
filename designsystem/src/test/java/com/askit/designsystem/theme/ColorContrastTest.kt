package com.askit.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** WCAG AA contrast checks for the AskIT colour roles that carry text and for the story scrim. */
class ColorContrastTest {
    private val schemes = listOf("light" to askITColorScheme(darkTheme = false), "dark" to askITColorScheme(darkTheme = true))

    private fun ColorScheme.surfaces() = listOf(
        "surface" to surface,
        "background" to background,
        "surfaceVariant" to surfaceVariant,
        "surfaceContainerLowest" to surfaceContainerLowest,
        "surfaceContainerLow" to surfaceContainerLow,
        "surfaceContainer" to surfaceContainer,
        "surfaceContainerHigh" to surfaceContainerHigh,
        "surfaceContainerHighest" to surfaceContainerHighest,
    )

    private fun assertContrast(label: String, fg: Color, bg: Color, minimum: Float) {
        val ratio = contrastRatio(fg, bg)
        assertTrue("$label is %.2f:1, needs %.1f:1".format(ratio, minimum), ratio >= minimum)
    }

    @Test fun ratioMatchesKnownValues() {
        assertEquals(21f, contrastRatio(Color.Black, Color.White), 0.01f)
        assertEquals(1f, contrastRatio(Color.White, Color.White), 0.01f)
        // #616161 on white is 6.19:1.
        assertEquals(6.19f, contrastRatio(Color(0xFF616161), Color.White), 0.02f)
    }

    @Test fun bodyAndSecondaryTextMeetAaOnEverySurface() {
        schemes.forEach { (name, scheme) ->
            scheme.surfaces().forEach { (surfaceName, bg) ->
                assertContrast("$name onSurface on $surfaceName", scheme.onSurface, bg, WCAG_AA_TEXT_CONTRAST)
                assertContrast("$name onSurfaceVariant on $surfaceName", scheme.onSurfaceVariant, bg, WCAG_AA_TEXT_CONTRAST)
            }
        }
    }

    @Test fun containerPairsMeetAa() {
        schemes.forEach { (name, s) ->
            assertContrast("$name onPrimary", s.onPrimary, s.primary, WCAG_AA_TEXT_CONTRAST)
            assertContrast("$name onSecondary", s.onSecondary, s.secondary, WCAG_AA_TEXT_CONTRAST)
            assertContrast("$name onSecondaryContainer", s.onSecondaryContainer, s.secondaryContainer, WCAG_AA_TEXT_CONTRAST)
            assertContrast("$name onTertiaryContainer", s.onTertiaryContainer, s.tertiaryContainer, WCAG_AA_TEXT_CONTRAST)
            assertContrast("$name onErrorContainer", s.onErrorContainer, s.errorContainer, WCAG_AA_TEXT_CONTRAST)
            assertContrast("$name onError", s.onError, s.error, WCAG_AA_TEXT_CONTRAST)
            assertContrast("$name secondary text on surface", s.secondary, s.surface, WCAG_AA_TEXT_CONTRAST)
        }
    }

    @Test fun outlineMeetsNonTextContrastOnSurface() {
        schemes.forEach { (name, s) ->
            assertContrast("$name outline on surface", s.outline, s.surface, WCAG_AA_NON_TEXT_CONTRAST)
        }
    }

    @Test fun whiteTextOverStoryScrimMeetsAaEvenOnAWhiteFrame() {
        val scrimOverWhite = StoryScrimColor.compositeOverOpaque(Color.White)
        assertContrast("white on story scrim over white", Color.White, scrimOverWhite, WCAG_AA_TEXT_CONTRAST)
    }

    private fun Color.compositeOverOpaque(bg: Color): Color = Color(
        red = red * alpha + bg.red * (1 - alpha),
        green = green * alpha + bg.green * (1 - alpha),
        blue = blue * alpha + bg.blue * (1 - alpha),
    )
}
