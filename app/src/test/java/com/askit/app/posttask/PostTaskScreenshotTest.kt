package com.askit.app.posttask

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.askit.designsystem.theme.AskITTheme
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.captureRoboImage
import androidx.lifecycle.SavedStateHandle
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import com.askit.app.R

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w360dp-h800dp-normal-long-notround-any-xxxhdpi-keyshidden-nonav")
class PostTaskScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun post_task_form_light_360() {
        setApp(darkTheme = false)
        capture("post_task_form_light_360")
    }

    @Test
    fun post_task_form_dark_360() {
        setApp(darkTheme = true)
        capture("post_task_form_dark_360")
    }

    @Test
    fun post_task_validation_light_360() {
        val viewModel = PostTaskViewModel(SavedStateHandle())
        viewModel.updateTitle("Repair the tap")
        viewModel.review()
        setApp(viewModel)
        capture("post_task_validation_light_360")
    }

    @Test
    fun post_task_review_light_360() {
        val viewModel = completeRemoteDraft()
        viewModel.review()
        setApp(viewModel)
        capture("post_task_review_light_360")
    }

    @Test
    fun post_task_provider_preview_sparse_light_360() {
        val viewModel = completeRemoteDraft()
        viewModel.review()
        setApp(viewModel)
        captureProviderPreview("post_task_provider_preview_sparse_light_360")
    }

    @Test
    fun post_task_provider_preview_rich_other_dark_360() {
        val viewModel = richPreviewDraft()
        viewModel.review()
        setApp(viewModel, darkTheme = true)
        captureProviderPreview("post_task_provider_preview_rich_other_dark_360")
    }

    @Test
    @Config(qualifiers = "ta-rIN-w360dp-h800dp-normal-long-notround-any-xxxhdpi-keyshidden-nonav")
    fun post_task_provider_preview_tamil_360() {
        val viewModel = completeRemoteDraft().also {
            it.selectCategory(POST_TASK_OTHER_CATEGORY_ID)
            it.updateCustomCategory("மரச்சாமான்கள் அமைத்தல்")
            it.updateTitle("புதிய அலமாரியை அமைக்க உதவி தேவை")
            it.updateDetails("பிளாட்-பேக் அலமாரியை படுக்கையறையில் அமைக்க வேண்டும்.")
        }
        viewModel.review()
        setApp(viewModel)
        captureProviderPreview("post_task_provider_preview_tamil_360")
    }

    @Test
    @Config(qualifiers = "w320dp-h800dp-normal-long-notround-any-xxxhdpi-keyshidden-nonav")
    fun post_task_provider_preview_large_text_320() {
        val viewModel = richPreviewDraft()
        viewModel.review()
        setApp(viewModel, fontScale = 1.5f)
        captureProviderPreview("post_task_provider_preview_large_text_320")
    }

    @Test
    fun post_task_category_dropdown_open_state_360() {
        setApp()
        composeTestRule.onNodeWithTag("post_task_category_dropdown").performClick()
        captureCategoryField("post_task_category_dropdown_other_360")
    }

    @Test
    fun post_task_other_category_form_360() {
        val viewModel = PostTaskViewModel(SavedStateHandle()).also {
            it.selectCategory(POST_TASK_OTHER_CATEGORY_ID)
            it.updateCustomCategory("Furniture assembly")
        }
        setApp(viewModel)
        capture("post_task_other_category_form_360")
    }

    @Test
    @Config(qualifiers = "w320dp-h800dp-normal-long-notround-any-xxxhdpi-keyshidden-nonav")
    fun post_task_form_narrow_320() {
        setApp()
        capture("post_task_form_narrow_320")
    }

    @Test
    @Config(qualifiers = "w412dp-h800dp-normal-long-notround-any-xxxhdpi-keyshidden-nonav")
    fun post_task_form_wide_412() {
        setApp()
        capture("post_task_form_wide_412")
    }

    @Test
    @Config(qualifiers = "w800dp-h360dp-normal-long-notround-any-xxxhdpi-keyshidden-nonav")
    fun post_task_form_landscape() {
        setApp()
        capture("post_task_form_landscape")
    }

    @Test
    @Config(qualifiers = "w320dp-h800dp-normal-long-notround-any-xxxhdpi-keyshidden-nonav")
    fun post_task_form_large_text_320() {
        setApp(fontScale = 1.5f)
        capture("post_task_form_large_text_320")
    }

    private fun completeRemoteDraft(): PostTaskViewModel =
        PostTaskViewModel(SavedStateHandle()).also { viewModel ->
            viewModel.selectCategory("plumber")
            viewModel.updateTitle("Repair the kitchen tap")
            viewModel.updateDetails("The tap is leaking and needs a replacement washer.")
            viewModel.selectWorkMode(PostTaskWorkMode.REMOTE)
            viewModel.selectTiming(PostTaskTimingMode.ASAP)
        }

    private fun richPreviewDraft(): PostTaskViewModel =
        PostTaskViewModel(SavedStateHandle()).also { viewModel ->
            viewModel.selectCategory(POST_TASK_OTHER_CATEGORY_ID)
            viewModel.updateCustomCategory("Furniture assembly")
            viewModel.updateTitle("Assemble the new wardrobe")
            viewModel.updateDetails("Please assemble the flat-pack wardrobe in the bedroom.")
            viewModel.selectWorkMode(PostTaskWorkMode.AT_MY_LOCATION)
            viewModel.selectLocation(
                placeId = "place-anna-nagar",
                publicAreaLabel = "Anna Nagar",
                latitude = 13.0850,
                longitude = 80.2101,
            )
            viewModel.updatePrivateAddressOrLandmark("Flat 7B, 12 Example Street")
            viewModel.selectTiming(PostTaskTimingMode.DATE)
            viewModel.selectDate(1_751_328_000_000)
            viewModel.selectBudgetMode(PostTaskBudgetMode.RANGE)
            viewModel.updateMinimumBudget("800")
            viewModel.updateMaximumBudget("1500")
            viewModel.setPhotoUris(
                listOf(
                    "android.resource://com.askit.app/${R.drawable.explore_results_empty}",
                    "android.resource://com.askit.app/${R.drawable.explore_results_server}",
                ),
            )
            viewModel.updateQuantityOrMeasurements("120 × 60 cm")
            viewModel.updateBrandOrModel("IKEA PAX")
            viewModel.selectMaterialsPolicy(PostTaskMaterialsPolicy.PROVIDER_BRINGS_MATERIALS)
            viewModel.updateAccessInstructions("Call when you arrive at the gate.")
        }

    private fun setApp(
        viewModel: PostTaskViewModel = PostTaskViewModel(SavedStateHandle()),
        darkTheme: Boolean = false,
        fontScale: Float = 1f,
    ) {
        composeTestRule.setContent {
            val currentDensity = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(currentDensity.density, fontScale),
            ) {
                AskITTheme(darkTheme = darkTheme) {
                    PostTaskRoute(
                        viewModel = viewModel,
                        onBack = {},
                        onCompleteDraft = {},
                    )
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    @OptIn(ExperimentalRoborazziApi::class)
    private fun capture(name: String) {
        composeTestRule.onRoot().captureRoboImage(
            filePath = "src/test/screenshots/$name.png",
        )
    }

    private fun captureProviderPreview(name: String) {
        composeTestRule
            .onNodeWithTag("post_task_provider_preview_card")
            .performScrollTo()
        composeTestRule.mainClock.advanceTimeBy(1_000)
        composeTestRule.waitForIdle()
        capture(name)
    }

    @OptIn(ExperimentalRoborazziApi::class)
    private fun captureCategoryField(name: String) {
        composeTestRule.onNodeWithTag("post_task_category_dropdown").captureRoboImage(
            filePath = "src/test/screenshots/$name.png",
        )
    }

}
