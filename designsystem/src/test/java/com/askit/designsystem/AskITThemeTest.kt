package com.askit.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.askit.designsystem.theme.AskITTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AskITThemeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun lightTheme_exposesStableMonochromeRolePairs() {
        var primary = Color.Unspecified
        var onPrimary = Color.Unspecified
        var surface = Color.Unspecified
        var onSurface = Color.Unspecified
        var secondaryContainer = Color.Unspecified
        var onSecondaryContainer = Color.Unspecified
        var outline = Color.Unspecified

        composeTestRule.setContent {
            AskITTheme(darkTheme = false) {
                primary = MaterialTheme.colorScheme.primary
                onPrimary = MaterialTheme.colorScheme.onPrimary
                surface = MaterialTheme.colorScheme.surface
                onSurface = MaterialTheme.colorScheme.onSurface
                secondaryContainer = MaterialTheme.colorScheme.secondaryContainer
                onSecondaryContainer = MaterialTheme.colorScheme.onSecondaryContainer
                outline = MaterialTheme.colorScheme.outline
            }
        }
        composeTestRule.waitForIdle()

        assertEquals(Color(0xFF000000), primary)
        assertEquals(Color(0xFFFFFFFF), onPrimary)
        assertEquals(Color(0xFFFFFFFF), surface)
        assertEquals(Color(0xFF000000), onSurface)
        assertEquals(Color(0xFFF5F5F5), secondaryContainer)
        assertEquals(Color(0xFF000000), onSecondaryContainer)
        assertEquals(Color(0xFF8A8A8A), outline)
    }

    @Test
    fun darkTheme_exposesStableMonochromeRolePairs() {
        var primary = Color.Unspecified
        var onPrimary = Color.Unspecified
        var surface = Color.Unspecified
        var onSurface = Color.Unspecified
        var primaryContainer = Color.Unspecified
        var onPrimaryContainer = Color.Unspecified

        composeTestRule.setContent {
            AskITTheme(darkTheme = true) {
                primary = MaterialTheme.colorScheme.primary
                onPrimary = MaterialTheme.colorScheme.onPrimary
                surface = MaterialTheme.colorScheme.surface
                onSurface = MaterialTheme.colorScheme.onSurface
                primaryContainer = MaterialTheme.colorScheme.primaryContainer
                onPrimaryContainer = MaterialTheme.colorScheme.onPrimaryContainer
            }
        }
        composeTestRule.waitForIdle()

        assertEquals(Color(0xFFFFFFFF), primary)
        assertEquals(Color(0xFF000000), onPrimary)
        assertEquals(Color(0xFF000000), surface)
        assertEquals(Color(0xFFFFFFFF), onSurface)
        assertEquals(Color(0xFF212121), primaryContainer)
        assertEquals(Color(0xFFFFFFFF), onPrimaryContainer)
    }
}
