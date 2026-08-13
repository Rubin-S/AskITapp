package com.askit.app.listservice

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.Density
import androidx.lifecycle.SavedStateHandle
import com.askit.designsystem.theme.AskITTheme
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w360dp-h800dp-normal-long-notround-any-xxxhdpi-keyshidden-nonav")
class ListServiceScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun list_service_form_light_360() {
        setApp()
        capture("list_service_form_light_360")
    }

    @Test
    fun list_service_form_dark_360() {
        setApp(darkTheme = true)
        capture("list_service_form_dark_360")
    }

    @Test
    fun list_service_other_category_form_360() {
        val viewModel = ListServiceViewModel(SavedStateHandle()).also {
            it.selectCategory(LIST_SERVICE_OTHER_CATEGORY_ID)
            it.updateCustomCategory("Furniture assembly")
            it.updateTitle("Assemble a wardrobe")
        }
        setApp(viewModel)
        capture("list_service_other_category_form_360")
    }

    @Test
    fun list_service_review_rich_customer_light_360() {
        setApp(richCustomerDraft())
        captureReview("list_service_review_rich_customer_light_360")
    }

    @Test
    fun list_service_review_rich_customer_dark_360() {
        setApp(richCustomerDraft(), darkTheme = true)
        captureReview("list_service_review_rich_customer_dark_360")
    }

    @Test
    fun list_service_review_remote_sparse_light_360() {
        setApp(remoteSparseDraft())
        captureReview("list_service_review_remote_sparse_light_360")
    }

    @Test
    @Config(qualifiers = "ta-rIN-w360dp-h800dp-normal-long-notround-any-xxxhdpi-keyshidden-nonav")
    fun list_service_form_tamil_360() {
        setApp()
        capture("list_service_form_tamil_360")
    }

    @Test
    @Config(qualifiers = "w320dp-h800dp-normal-long-notround-any-xxxhdpi-keyshidden-nonav")
    fun list_service_form_large_text_320() {
        setApp(fontScale = 1.5f)
        capture("list_service_form_large_text_320")
    }

    @Test
    @Config(qualifiers = "w412dp-h800dp-normal-long-notround-any-xxxhdpi-keyshidden-nonav")
    fun list_service_form_wide_412() {
        setApp(fontScale = 1.3f)
        capture("list_service_form_wide_412")
    }

    @Test
    @Config(qualifiers = "w800dp-h360dp-normal-long-notround-any-xxxhdpi-keyshidden-nonav")
    fun list_service_form_landscape() {
        setApp(fontScale = 2f)
        capture("list_service_form_landscape")
    }

    private fun setApp(
        viewModel: ListServiceViewModel = ListServiceViewModel(SavedStateHandle()),
        darkTheme: Boolean = false,
        fontScale: Float = 1f,
    ) {
        composeTestRule.setContent {
            val currentDensity = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(currentDensity.density, fontScale),
            ) {
                AskITTheme(darkTheme = darkTheme) {
                    ListServiceRoute(
                        viewModel = viewModel,
                        onBack = {},
                        onCompleteDraft = {},
                    )
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    private fun richCustomerDraft(): ListServiceViewModel =
        ListServiceViewModel(SavedStateHandle()).also { viewModel ->
            viewModel.selectCategory(LIST_SERVICE_OTHER_CATEGORY_ID)
            viewModel.updateCustomCategory("Furniture assembly")
            viewModel.updateTitle("Assemble the new wardrobe")
            viewModel.updateDescription("I assemble flat-pack wardrobes and align the doors carefully.")
            viewModel.toggleDeliveryMode(ListServiceDeliveryMode.AT_CUSTOMER_LOCATION)
            viewModel.toggleDeliveryMode(ListServiceDeliveryMode.AT_PROVIDER_LOCATION)
            viewModel.selectCustomerLocation("place-anna-nagar", "Anna Nagar", 13.0850, 80.2101)
            viewModel.selectCoverageRadius(10)
            viewModel.selectProviderLocation("place-adyar", "Adyar", 13.0067, 80.2572)
            viewModel.toggleOptionalSection(ListServiceOptionalSection.PRICING)
            viewModel.selectPricingMode(ListServicePricingMode.RANGE)
            viewModel.updateMinimumPrice("800")
            viewModel.updateMaximumPrice("1500")
            viewModel.toggleOptionalSection(ListServiceOptionalSection.MORE_DETAILS)
            viewModel.updateAvailability("Weekday evenings")
            viewModel.updateExperience("8 years")
            viewModel.updateTypicalDuration("2–3 hours")
            viewModel.updateMaterials("I bring hand tools")
            viewModel.updateCredentials("ITI certified")
            viewModel.updateWarranty("30-day workmanship support")
            viewModel.updateAdvanceNotice("Two days")
            check(viewModel.review())
        }

    private fun remoteSparseDraft(): ListServiceViewModel =
        ListServiceViewModel(SavedStateHandle()).also { viewModel ->
            viewModel.selectCategory("plumber")
            viewModel.updateTitle("Remote tap advice")
            viewModel.updateDescription("I help customers diagnose tap problems over a video call.")
            viewModel.toggleDeliveryMode(ListServiceDeliveryMode.REMOTE)
            check(viewModel.review())
        }

    @OptIn(ExperimentalRoborazziApi::class)
    private fun capture(name: String) {
        composeTestRule.onRoot().captureRoboImage(
            filePath = "src/test/screenshots/$name.png",
        )
    }

    private fun captureReview(name: String) {
        composeTestRule
            .onNodeWithTag("list_service_customer_preview_card", useUnmergedTree = true)
            .performScrollTo()
        composeTestRule.waitForIdle()
        capture(name)
    }
}
