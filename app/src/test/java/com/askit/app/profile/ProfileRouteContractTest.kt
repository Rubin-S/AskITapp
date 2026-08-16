package com.askit.app.profile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import com.askit.app.jobs.Job
import com.askit.app.listservice.ListServiceDeliveryMode
import com.askit.app.listservice.ListServiceDetails
import com.askit.app.listservice.ListServiceDraft
import com.askit.app.listservice.toServiceListing
import com.askit.app.session.DefaultCarpentryListing
import com.askit.app.session.ProfileAvailability
import com.askit.app.session.SessionProfile
import com.askit.app.session.SessionProfileStore
import kotlinx.coroutines.flow.StateFlow
import com.askit.designsystem.theme.AskITTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w360dp-h800dp-normal-long-notround-any-xxxhdpi-keyshidden-nonav")
class ProfileRouteContractTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun galleryIsDefaultTab_andShowsEmptyNotFakeCells() {
        setProfile()
        composeTestRule.onNodeWithTag("profile_tab_gallery").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_tab_about").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_tab_activity").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_tab_reviews").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_gallery_empty").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_photo_grid").assertDoesNotExist()
        composeTestRule.onNodeWithTag("profile_contact").assertDoesNotExist()
    }

    @Test
    fun overflowMenu_hasShareAndCopy() {
        setProfile()
        composeTestRule.onNodeWithTag("profile_more").performClick()
        composeTestRule.onNodeWithTag("profile_share").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_copy_username").assertIsDisplayed()
    }

    @Test
    fun yourService_hiddenWithoutListing_visibleWhenListed() {
        val profile = mutableStateOf(SessionProfile())
        composeTestRule.setContent {
            AskITTheme {
                TestProfileRoute(profile = profile.value)
            }
        }
        composeTestRule.onNodeWithTag("profile_your_service").assertDoesNotExist()
        profile.value = SessionProfile(
            hasListedService = true,
            listing = DefaultCarpentryListing,
            profileStrengthPercent = 90,
        )
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("profile_your_service").assertExists()
        composeTestRule.onNodeWithTag("profile_insights").assertDoesNotExist()
    }

    @Test
    fun galleryShowsUriAfterPost() {
        setProfile(
            SessionProfile(
                gallery = listOf(ProfileGalleryItem(id = "p1", uri = "content://askit/photo1")),
            ),
        )
        composeTestRule.onNodeWithTag("profile_photo_grid").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_photo_p1").assertExists()
    }

    @Test
    fun reviewsEmptyState_whenNoReviews() {
        setProfile()
        composeTestRule.onNodeWithTag("profile_tab_reviews").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("profile_reviews_empty").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun reviewsList_whenRepositoryHasReview() {
        setProfile(
            SessionProfile(
                reviews = listOf(
                    ProfileReview(
                        id = "rev-1",
                        name = "Priya",
                        meta = "Repair tap",
                        rating = 5f,
                        body = "On time.",
                        createdAtMillis = 1L,
                    ),
                ),
            ),
        )
        composeTestRule.onNodeWithTag("profile_tab_reviews").performScrollTo().performClick()
        composeTestRule.onNodeWithText("Priya").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun editProfile_saveDisabledUntilDirtyAndValid() {
        composeTestRule.setContent {
            AskITTheme {
                EditProfileScreen(
                    profile = SessionProfile(),
                    onBack = {},
                    onSave = {},
                )
            }
        }
        composeTestRule.onNodeWithTag("edit_profile_save").assertIsNotEnabled()
        composeTestRule.onNodeWithTag("edit_profile_display_name").performTextReplacement("Meera")
        composeTestRule.onNodeWithTag("edit_profile_save").assertIsEnabled()
    }

    @Test
    fun editProfile_usernameErrorOnBlur() {
        composeTestRule.setContent {
            AskITTheme {
                EditProfileScreen(
                    profile = SessionProfile(),
                    onBack = {},
                    onSave = {},
                )
            }
        }
        composeTestRule.onNodeWithTag("edit_profile_username").performTextReplacement("bad name!")
        composeTestRule.onNodeWithTag("edit_profile_city").performClick()
        composeTestRule.onNodeWithText("Use letters, numbers, periods, and underscores only").assertIsDisplayed()
    }

    @Test
    fun editProfile_discardDialogWhenDirty() {
        composeTestRule.setContent {
            AskITTheme {
                EditProfileScreen(
                    profile = SessionProfile(),
                    onBack = {},
                    onSave = {},
                )
            }
        }
        composeTestRule.onNodeWithTag("edit_profile_display_name").performTextReplacement("New name")
        composeTestRule.onNodeWithTag("edit_profile_back").performClick()
        composeTestRule.onNodeWithText("Discard changes?").assertIsDisplayed()
    }

    @Test
    fun usernameValidation() {
        assertTrue(isUsernameValid("meera.raman"))
        assertTrue(isUsernameValid("meera_raman"))
        assertFalse(isUsernameValid("bad name"))
        assertFalse(isUsernameValid(""))
        val original = EditProfileFormState("A", "user", "City", "Bio", null)
        val dirty = original.copy(displayName = "B")
        assertTrue(dirty.isDirty(original))
        assertFalse(original.isDirty(original))
        assertEquals(true, dirty.isValid)
    }

    @Test
    fun localRepository_appendsGalleryAndReview() {
        val repo = LocalProfileRepository(SessionProfileStore())
        repo.appendGallery(listOf("content://a", "content://a", ""))
        assertEquals(1, repo.profile.value.gallery.size)
        assertEquals("content://a", repo.profile.value.gallery.first().uri)
        val review = ProfileReview("r1", "A", "Job", 5f, "Good", 2L)
        repo.appendReview(review)
        repo.appendReview(review)
        assertEquals(1, repo.profile.value.reviews.size)
        repo.setAvatar(null)
        assertNull(repo.profile.value.avatarUrl)
        repo.addLicense("  ")
        repo.addLicense("GSTIN")
        assertEquals(listOf("GSTIN"), repo.profile.value.licenses)
    }

    @Test
    fun actionError_keepsProfileReady() {
        val viewModel = ProfileViewModel(ThrowingProfileRepository())
        assertEquals(ProfileLoadState.Ready, viewModel.loadState.value)
        viewModel.updateAbout("new about")
        assertEquals(ProfileLoadState.Ready, viewModel.loadState.value)
    }

    @Test
    fun applyListing_usesDraftNotDefaultCarpentry() {
        val store = SessionProfileStore()
        val draft = ListServiceDraft(
            categoryId = "plumber",
            title = "Repair kitchen taps",
            description = "I repair leaking taps.",
            deliveryModes = setOf(ListServiceDeliveryMode.REMOTE),
            details = ListServiceDetails(null, "4 yrs", null, null, null, null, null),
        )
        store.applyListing(draft.toServiceListing("Plumber"), draft)
        assertEquals("Repair kitchen taps", store.profile.value.listing?.title)
        assertEquals("plumber", store.profile.value.listing?.categoryId)
        assertEquals("Repair kitchen taps", store.profile.value.listingDraft?.title)
        assertTrue(store.profile.value.hasListedService)
    }

    private fun setProfile(profile: SessionProfile = SessionProfile()) {
        composeTestRule.setContent {
            AskITTheme {
                TestProfileRoute(profile = profile)
            }
        }
    }
}

@Composable
private fun TestProfileRoute(profile: SessionProfile, jobs: List<Job> = emptyList()) {
    ProfileRoute(
        profile = profile,
        jobs = jobs,
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

private class ThrowingProfileRepository : ProfileRepository {
    private val store = SessionProfileStore()
    override val profile: StateFlow<SessionProfile> = store.profile

    override fun updateIdentity(
        displayName: String,
        username: String,
        city: String,
        bio: String,
        about: String,
        avatarUrl: String?,
    ) = error("failed")

    override fun updateAbout(about: String) = error("failed")
    override fun updateLookingFor(lookingFor: List<String>) = error("failed")
    override fun updateSkills(skills: List<String>) = error("failed")
    override fun updateAvailability(availability: ProfileAvailability) = error("failed")
    override fun setAvatar(avatarUrl: String?) = error("failed")
    override fun appendGallery(uris: List<String>) = error("failed")
    override fun appendReview(review: ProfileReview) = error("failed")
    override fun addLicense(license: String) = error("failed")
}
