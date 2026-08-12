package com.askit.designsystem.navigation

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.askit.designsystem.theme.AskITTheme
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w360dp-h800dp-normal-long-notround-any-xxxhdpi-keyshidden-nonav")
class AskITBottomBarScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun bottom_bar_home_selected_with_avatar() {
        captureBar(
            fileName = "bottom_bar_home_selected_with_avatar",
            selected = AskITDestination.Home,
            unreadCount = 0,
            withAvatar = true,
        )
    }

    @Test
    fun bottom_bar_explore_selected() {
        captureBar(
            fileName = "bottom_bar_explore_selected",
            selected = AskITDestination.Explore,
            unreadCount = 0,
            withAvatar = true,
        )
    }

    @Test
    fun bottom_bar_inbox_with_badge() {
        captureBar(
            fileName = "bottom_bar_inbox_with_badge",
            selected = AskITDestination.Inbox,
            unreadCount = 3,
            withAvatar = true,
        )
    }

    @Test
    fun bottom_bar_profile_selected() {
        captureBar(
            fileName = "bottom_bar_profile_selected",
            selected = AskITDestination.Profile,
            unreadCount = 0,
            withAvatar = true,
            settleCoil = true,
        )
    }

    @Test
    fun bottom_bar_profile_fallback() {
        captureBar(
            fileName = "bottom_bar_profile_fallback",
            selected = AskITDestination.Profile,
            unreadCount = 0,
            withAvatar = false,
        )
    }

    @Test
    fun create_sheet_open() {
        captureCreateSheet(fileName = "create_sheet_open")
    }

    @Test
    fun create_sheet_open_dark() {
        captureCreateSheet(fileName = "create_sheet_open_dark", darkTheme = true)
    }

    @Test
    @Config(qualifiers = "ta-rIN-w360dp-h800dp-normal-long-notround-any-xxxhdpi-keyshidden-nonav")
    fun create_sheet_open_tamil() {
        captureCreateSheet(fileName = "create_sheet_open_tamil")
    }

    @Test
    @Config(qualifiers = "w320dp-h360dp-normal-long-notround-any-xxxhdpi-keyshidden-nonav")
    fun create_sheet_open_compact_large_text() {
        captureCreateSheet(
            fileName = "create_sheet_open_compact_large_text",
            fontScale = 2f,
            scrollToPost = true,
        )
    }

    @Test
    @Config(qualifiers = "w412dp-h800dp-normal-long-notround-any-xxxhdpi-keyshidden-nonav")
    fun create_sheet_open_wide() {
        captureCreateSheet(fileName = "create_sheet_open_wide")
    }

    @Test
    @Config(qualifiers = "w800dp-h360dp-normal-long-notround-any-xxxhdpi-keyshidden-nonav")
    fun create_sheet_open_landscape() {
        captureCreateSheet(fileName = "create_sheet_open_landscape")
    }

    private fun captureCreateSheet(
        fileName: String,
        darkTheme: Boolean = false,
        fontScale: Float = 1f,
        scrollToPost: Boolean = false,
    ) {
        composeTestRule.setContent {
            AskITTheme(darkTheme = darkTheme) {
                val currentDensity = LocalDensity.current
                CompositionLocalProvider(
                    LocalDensity provides Density(currentDensity.density, fontScale),
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Surface(color = MaterialTheme.colorScheme.background) {
                            AskITCreateSheet(onDismiss = {}, onActionClick = {})
                        }
                    }
                }
            }
        }
        if (scrollToPost) {
            composeTestRule.onNodeWithText("Create a post").performScrollTo()
        }
        composeTestRule.onRoot().captureRoboImage(
            filePath = "src/test/screenshots/$fileName.png",
        )
    }

    private fun captureBar(
        fileName: String,
        selected: AskITDestination,
        unreadCount: Int,
        withAvatar: Boolean,
        settleCoil: Boolean = false,
    ) {
        composeTestRule.setContent {
            AskITTheme(darkTheme = false) {
                val avatar = if (withAvatar) localAvatarUri() else null
                Box(modifier = Modifier.width(360.dp).fillMaxWidth()) {
                    Surface(color = MaterialTheme.colorScheme.surface) {
                        AskITBottomBar(
                            selectedDestination = selected,
                            avatarUrl = avatar,
                            unreadCount = unreadCount,
                            onDestinationClick = {},
                            onCreateClick = {},
                        )
                    }
                }
            }
        }
        if (settleCoil) {
            composeTestRule.mainClock.advanceTimeBy(500)
            composeTestRule.waitForIdle()
        }
        composeTestRule.onRoot().captureRoboImage(
            filePath = "src/test/screenshots/$fileName.png",
        )
    }

    @Composable
    private fun localAvatarUri(): String {
        val context = LocalContext.current
        val file = File(context.cacheDir, "askit_avatar.png")
        if (!file.exists()) {
            val size = 56
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            Canvas(bitmap).drawCircle(
                size / 2f,
                size / 2f,
                size / 2f,
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = 0xFF424242.toInt()
                    style = Paint.Style.FILL
                },
            )
            file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            bitmap.recycle()
        }
        return file.toURI().toString()
    }
}
