package com.askit.app.profile

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.onNodeWithTag
import com.askit.app.session.DefaultCarpentryListing
import com.askit.app.session.ProfileAvailability
import com.askit.app.session.SessionProfile
import com.askit.designsystem.theme.AskITTheme
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
class ProfileScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun seekerGallery_light() {
        setProfile()
        capture("profile_seeker_gallery_light")
    }

    @Test
    fun seekerAbout_light() {
        setProfile()
        composeTestRule.onNodeWithTag("profile_tab_about").performClick()
        capture("profile_seeker_about_light")
    }

    @Test
    fun seekerActivity_light() {
        setProfile()
        composeTestRule.onNodeWithTag("profile_tab_activity").performClick()
        capture("profile_seeker_activity_light")
    }

    @Test
    fun seekerReviews_light() {
        setProfile()
        composeTestRule.onNodeWithTag("profile_tab_reviews").performClick()
        capture("profile_seeker_reviews_light")
    }

    @Test
    fun listedServiceChrome_light() {
        setProfile(
            SessionProfile(
                hasListedService = true,
                listing = DefaultCarpentryListing,
                profileStrengthPercent = 90,
                skills = DefaultCarpentryListing.tags,
            ),
        )
        capture("profile_listed_service_light")
    }

    @Test
    fun listedServiceChrome_dark() {
        composeTestRule.setContent {
            AskITTheme(darkTheme = true) {
                ProfileRoute(
                    profile = SessionProfile(
                        hasListedService = true,
                        listing = DefaultCarpentryListing,
                        profileStrengthPercent = 90,
                    ),
                    jobs = emptyList(),
                    viewAsOtherParty = false,
                    onEditProfile = {},
                    onEditListing = {},
                    onUploadWork = {},
                    onOpenJob = {},
                    onViewAllJobs = {},
                    onSaveAbout = {},
                    onSaveLookingFor = {},
                    onSaveSkills = {},
                    onSaveAvailability = {},
                    onSetAvatar = {},
                    onAddLicense = {},
                    onUsernameCopied = {},
                )
            }
        }
        capture("profile_listed_service_dark")
    }

    @Test
    fun editProfile_light() {
        composeTestRule.setContent {
            AskITTheme(darkTheme = false) {
                EditProfileScreen(profile = SessionProfile(), onBack = {}, onSave = {})
            }
        }
        capture("profile_edit_light")
    }

    @Test
    fun availabilitySheet_light() {
        composeTestRule.setContent {
            AskITTheme(darkTheme = false) {
                val open = remember { mutableStateOf(true) }
                if (open.value) {
                    AvailabilitySheet(
                        availability = ProfileAvailability(),
                        onDismiss = { open.value = false },
                        onSave = {},
                    )
                }
            }
        }
        capture("profile_availability_sheet_light")
    }

    @Test
    @Config(qualifiers = "ta-rIN-w360dp-h800dp-normal-long-notround-any-xxxhdpi-keyshidden-nonav")
    fun seekerAbout_tamil() {
        setProfile()
        composeTestRule.onNodeWithTag("profile_tab_about").performClick()
        capture("profile_seeker_about_tamil")
    }

    private fun setProfile(profile: SessionProfile = SessionProfile()) {
        composeTestRule.setContent {
            AskITTheme(darkTheme = false) {
                ProfileRoute(
                    profile = profile,
                    jobs = emptyList(),
                    viewAsOtherParty = false,
                    onEditProfile = {},
                    onEditListing = {},
                    onUploadWork = {},
                    onOpenJob = {},
                    onViewAllJobs = {},
                    onSaveAbout = {},
                    onSaveLookingFor = {},
                    onSaveSkills = {},
                    onSaveAvailability = {},
                    onSetAvatar = {},
                    onAddLicense = {},
                    onUsernameCopied = {},
                )
            }
        }
    }

    private fun capture(fileName: String) {
        composeTestRule.onRoot().captureRoboImage(
            filePath = "src/test/screenshots/$fileName.png",
        )
    }
}
