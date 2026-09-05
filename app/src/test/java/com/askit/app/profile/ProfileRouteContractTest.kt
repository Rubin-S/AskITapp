package com.askit.app.profile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import com.askit.app.jobs.Job
import com.askit.app.jobs.JobKind
import com.askit.app.jobs.JobParty
import com.askit.app.jobs.JobStatus
import com.askit.app.jobs.JobWorkMode
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
        setProfile(SessionProfile(hasListedService = true))
        composeTestRule.onNodeWithTag("profile_tab_services").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_tab_gallery").performClick()
        composeTestRule.onNodeWithTag("profile_tab_about").assertIsDisplayed()
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
    fun overflowMenu_hasSettings_andNavigatesToSettings() {
        var settingsOpened = false
        composeTestRule.setContent {
            AskITTheme {
                ProfileRoute(
                    profile = SessionProfile(),
                    jobs = emptyList(),
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
                    onOpenSettings = { settingsOpened = true },
                )
            }
        }
        composeTestRule.onNodeWithTag("profile_more").performClick()
        composeTestRule.onNodeWithTag("profile_settings").assertIsDisplayed().performClick()
        assertTrue(settingsOpened)
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
                hasListedService = true,
                gallery = listOf(ProfileGalleryItem(id = "p1", uri = "content://askit/photo1")),
            ),
        )
        composeTestRule.onNodeWithTag("profile_tab_gallery").performClick()
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

    @Test
    fun spatialOrdering_ownerFormA_verifiesUniversalOrderOnProfileRoute() {
        setProfile(
            SessionProfile(
                displayName = "Rahul Verma",
                username = "rahul_v",
                bio = "Community member",
                city = "Bengaluru",
                joinedYear = "2023",
                hasListedService = false,
                followerCount = 84,
                followingCount = 31,
            ),
        )

        val topBarY = composeTestRule.onNodeWithTag("profile_top_bar").getUnclippedBoundsInRoot().top
        val coverY = composeTestRule.onNodeWithTag("profile_cover").getUnclippedBoundsInRoot().top
        val avatarY = composeTestRule.onNodeWithTag("profile_avatar").getUnclippedBoundsInRoot().top
        val identityY = composeTestRule.onNodeWithTag("profile_identity_block").getUnclippedBoundsInRoot().top
        val metricsY = composeTestRule.onNodeWithTag("profile_metrics_bar").getUnclippedBoundsInRoot().top
        val actionsY = composeTestRule.onNodeWithTag("profile_action_row").getUnclippedBoundsInRoot().top
        val bannerY = composeTestRule.onNodeWithTag("profile_complete_form_b_banner").getUnclippedBoundsInRoot().top
        val tabsY = composeTestRule.onNodeWithTag("profile_tabs").getUnclippedBoundsInRoot().top
        val contentY = composeTestRule.onNodeWithTag("profile_content_section").getUnclippedBoundsInRoot().top

        assertTrue("TopBar <= Cover", topBarY <= coverY)
        assertTrue("Cover <= Avatar", coverY <= avatarY)
        assertTrue("Avatar < Identity", avatarY < identityY)
        assertTrue("Identity < Metrics", identityY < metricsY)
        assertTrue("Metrics < Actions", metricsY < actionsY)
        assertTrue("Actions < Banner", actionsY < bannerY)
        assertTrue("Banner < Tabs", bannerY < tabsY)
        assertTrue("Tabs < Content", tabsY < contentY)
    }

    @Test
    fun spatialOrdering_providerFormB_verifiesUniversalOrderOnProfileRoute() {
        setProfile(
            SessionProfile(
                displayName = "Priya Sharma",
                username = "priya_e",
                bio = "Certified electrician",
                city = "Chennai",
                joinedYear = "2022",
                hasListedService = true,
                listing = DefaultCarpentryListing,
                followerCount = 120,
                followingCount = 45,
            ),
        )

        val topBarY = composeTestRule.onNodeWithTag("profile_top_bar").getUnclippedBoundsInRoot().top
        val coverY = composeTestRule.onNodeWithTag("profile_cover").getUnclippedBoundsInRoot().top
        val avatarY = composeTestRule.onNodeWithTag("profile_avatar").getUnclippedBoundsInRoot().top
        val identityY = composeTestRule.onNodeWithTag("profile_identity_block").getUnclippedBoundsInRoot().top
        val metricsY = composeTestRule.onNodeWithTag("profile_metrics_bar").getUnclippedBoundsInRoot().top
        val actionsY = composeTestRule.onNodeWithTag("profile_action_row").getUnclippedBoundsInRoot().top
        val tabsY = composeTestRule.onNodeWithTag("profile_tabs").getUnclippedBoundsInRoot().top
        val contentY = composeTestRule.onNodeWithTag("profile_content_section").getUnclippedBoundsInRoot().top

        assertTrue("TopBar <= Cover", topBarY <= coverY)
        assertTrue("Cover <= Avatar", coverY <= avatarY)
        assertTrue("Avatar < Identity", avatarY < identityY)
        assertTrue("Identity < Metrics", identityY < metricsY)
        assertTrue("Metrics < Actions", metricsY < actionsY)
        assertTrue("Actions < Tabs", actionsY < tabsY)
        assertTrue("Tabs < Content", tabsY < contentY)

        composeTestRule.onNodeWithTag("profile_complete_form_b_banner").assertDoesNotExist()
    }

    @Test
    fun formADynamicMetricsAndTabs_ownerView_showsThreeMetricsThreeTabsAndCompleteFormBBanner() {
        val jobs = listOf(sampleJob("j1"), sampleJob("j2"))
        setProfile(
            SessionProfile(
                displayName = "Rahul Verma",
                username = "rahul_v",
                hasListedService = false,
                followerCount = 84,
                followingCount = 31,
            ),
            jobs = jobs,
        )

        composeTestRule.onNodeWithTag("profile_metric_activity").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_metric_followers").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_metric_following").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_metric_rating").assertDoesNotExist()
        composeTestRule.onNodeWithTag("profile_metric_completed_jobs").assertDoesNotExist()

        composeTestRule.onNodeWithTag("profile_tab_activity").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_tab_about").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_tab_reviews").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_tab_services").assertDoesNotExist()
        composeTestRule.onNodeWithTag("profile_tab_gallery").assertDoesNotExist()

        composeTestRule.onNodeWithTag("profile_complete_form_b_banner").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_camera").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_action_edit").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_action_share").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_action_view_as_public").assertIsDisplayed()
    }

    @Test
    fun formBDynamicMetricsAndTabs_ownerView_showsFourMetricsFourTabsAndNoMotivationalBanner() {
        setProfile(
            SessionProfile(
                displayName = "Priya Sharma",
                username = "priya_e",
                hasListedService = true,
                listing = DefaultCarpentryListing,
                followerCount = 120,
                followingCount = 45,
            ),
        )

        composeTestRule.onNodeWithTag("profile_metric_rating").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_metric_completed_jobs").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_metric_followers").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_metric_following").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_metric_activity").assertDoesNotExist()

        composeTestRule.onNodeWithTag("profile_tab_services").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_tab_gallery").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_tab_reviews").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_tab_about").assertIsDisplayed()

        composeTestRule.onNodeWithTag("profile_complete_form_b_banner").assertDoesNotExist()
        composeTestRule.onNodeWithTag("profile_camera").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_action_view_as_public").assertIsDisplayed()
    }

    @Test
    fun viewAsPublic_toggle_showsFloatingExitBannerAndVisitorActions() {
        setProfile(
            SessionProfile(
                displayName = "Priya Sharma",
                username = "priya_e",
                hasListedService = true,
                listing = DefaultCarpentryListing,
            ),
        )

        composeTestRule.onNodeWithTag("profile_preview_banner").assertDoesNotExist()
        composeTestRule.onNodeWithTag("profile_action_view_as_public").performClick()

        composeTestRule.onNodeWithTag("profile_preview_banner").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_exit_preview").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_action_message").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_action_follow").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_action_request_service").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_action_edit").assertDoesNotExist()
        composeTestRule.onNodeWithTag("profile_action_view_as_public").assertDoesNotExist()
        composeTestRule.onNodeWithTag("profile_camera").assertDoesNotExist()
    }

    @Test
    fun exitPreview_bannerClick_restoresOwnerMode() {
        setProfile(
            SessionProfile(
                displayName = "Priya Sharma",
                username = "priya_e",
                hasListedService = true,
                listing = DefaultCarpentryListing,
            ),
        )

        composeTestRule.onNodeWithTag("profile_action_view_as_public").performClick()
        composeTestRule.onNodeWithTag("profile_preview_banner").assertIsDisplayed()

        composeTestRule.onNodeWithTag("profile_exit_preview").performClick()
        composeTestRule.onNodeWithTag("profile_preview_banner").assertDoesNotExist()
        composeTestRule.onNodeWithTag("profile_action_edit").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_action_view_as_public").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_camera").assertIsDisplayed()
    }

    @Test
    fun formA_viewAsPublic_hidesMotivationalBannerAndCamera_andEnforcesVisitorProtection() {
        setProfile(
            SessionProfile(
                displayName = "Rahul Verma",
                username = "rahul_v",
                hasListedService = false,
            ),
        )

        composeTestRule.onNodeWithTag("profile_complete_form_b_banner").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_camera").assertIsDisplayed()

        composeTestRule.onNodeWithTag("profile_action_view_as_public").performClick()

        composeTestRule.onNodeWithTag("profile_preview_banner").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_complete_form_b_banner").assertDoesNotExist()
        composeTestRule.onNodeWithTag("profile_camera").assertDoesNotExist()
        composeTestRule.onNodeWithTag("profile_action_request_service").assertDoesNotExist()
        composeTestRule.onNodeWithTag("profile_action_message").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_action_follow").assertIsDisplayed()
    }

    @Test
    fun formB_viewAsPublic_suppressesServiceCardEditButton_andScopingToServicesTab() {
        setProfile(
            SessionProfile(
                displayName = "Priya Sharma",
                username = "priya_e",
                hasListedService = true,
                listing = DefaultCarpentryListing,
            ),
        )

        composeTestRule.onNodeWithTag("profile_your_service").assertExists()
        composeTestRule.onNodeWithText("Edit listing").assertExists()

        composeTestRule.onNodeWithTag("profile_action_view_as_public").performClick()

        composeTestRule.onNodeWithTag("profile_your_service").assertExists()
        composeTestRule.onNodeWithText("Edit listing").assertDoesNotExist()

        composeTestRule.onNodeWithTag("profile_tab_gallery").performClick()
        composeTestRule.onNodeWithTag("profile_your_service").assertDoesNotExist()

        composeTestRule.onNodeWithTag("profile_tab_reviews").performClick()
        composeTestRule.onNodeWithTag("profile_your_service").assertDoesNotExist()
    }

    @Test
    fun followSimulation_togglesInPublicPreviewMode() {
        setProfile(SessionProfile(hasListedService = false))

        composeTestRule.onNodeWithTag("profile_action_view_as_public").performClick()
        composeTestRule.onNode(hasTestTag("profile_action_follow") and hasText("Follow")).assertIsDisplayed()

        composeTestRule.onNodeWithTag("profile_action_follow").performClick()
        composeTestRule.onNode(hasTestTag("profile_action_follow") and hasText("Following")).assertIsDisplayed()

        composeTestRule.onNodeWithTag("profile_action_follow").performClick()
        composeTestRule.onNode(hasTestTag("profile_action_follow") and hasText("Follow")).assertIsDisplayed()
    }

    @Test
    fun inPlacePreviewToggling_preservesTabSelectionAndFollowStateAcrossTransitions() {
        setProfile(
            SessionProfile(
                displayName = "Priya Sharma",
                username = "priya_e",
                hasListedService = true,
                listing = DefaultCarpentryListing,
            ),
        )

        // 1. Initial owner mode: select "reviews" tab (tab index 2)
        composeTestRule.onNodeWithTag("profile_tab_reviews").performClick()
        composeTestRule.onNodeWithTag("profile_reviews_empty").assertIsDisplayed()

        // 2. Enter in-place preview via profile_action_view_as_public
        composeTestRule.onNodeWithTag("profile_action_view_as_public").performClick()
        composeTestRule.onNodeWithTag("profile_preview_banner").assertIsDisplayed()

        // 3. Verify tab selection is preserved in preview mode (reviews empty state still displayed)
        composeTestRule.onNodeWithTag("profile_reviews_empty").assertIsDisplayed()

        // 4. In preview mode, toggle follow state to "Following"
        composeTestRule.onNode(hasTestTag("profile_action_follow") and hasText("Follow")).assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_action_follow").performClick()
        composeTestRule.onNode(hasTestTag("profile_action_follow") and hasText("Following")).assertIsDisplayed()

        // 5. Exit preview mode via floating exit banner (profile_exit_preview)
        composeTestRule.onNodeWithTag("profile_exit_preview").performClick()
        composeTestRule.onNodeWithTag("profile_preview_banner").assertDoesNotExist()
        composeTestRule.onNodeWithTag("profile_action_edit").assertIsDisplayed()

        // 6. Verify tab selection is preserved after exiting preview (reviews tab still active)
        composeTestRule.onNodeWithTag("profile_reviews_empty").assertIsDisplayed()

        // 7. Re-enter preview mode and verify follow state was preserved (remains "Following")
        composeTestRule.onNodeWithTag("profile_action_view_as_public").performClick()
        composeTestRule.onNodeWithTag("profile_preview_banner").assertIsDisplayed()
        composeTestRule.onNode(hasTestTag("profile_action_follow") and hasText("Following")).assertIsDisplayed()
    }

    @Test
    fun formAVisitorProtection_noRequestServiceOrProviderTabsInVisitorOrPreview() {
        setProfile(
            SessionProfile(
                displayName = "Rahul Verma",
                username = "rahul_v",
                hasListedService = false,
            ),
        )

        // Enter preview
        composeTestRule.onNodeWithTag("profile_action_view_as_public").performClick()
        composeTestRule.onNodeWithTag("profile_preview_banner").assertIsDisplayed()

        // In preview mode:
        // No Request Service CTA
        composeTestRule.onNodeWithTag("profile_action_request_service").assertDoesNotExist()
        // No provider tabs
        composeTestRule.onNodeWithTag("profile_tab_services").assertDoesNotExist()
        composeTestRule.onNodeWithTag("profile_tab_gallery").assertDoesNotExist()
        // Strictly 3 member tabs
        composeTestRule.onNodeWithTag("profile_tab_activity").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_tab_about").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_tab_reviews").assertIsDisplayed()
    }

    @Test
    fun cameraAffordanceAndMotivationalBanner_strictlyHiddenInPreviewAndVisitorModes() {
        // Form A owner
        setProfile(
            SessionProfile(
                displayName = "Rahul Verma",
                username = "rahul_v",
                hasListedService = false,
            ),
        )
        composeTestRule.onNodeWithTag("profile_camera").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_complete_form_b_banner").assertIsDisplayed()

        // Enter preview
        composeTestRule.onNodeWithTag("profile_action_view_as_public").performClick()
        composeTestRule.onNodeWithTag("profile_camera").assertDoesNotExist()
        composeTestRule.onNodeWithTag("profile_complete_form_b_banner").assertDoesNotExist()

        // Exit preview
        composeTestRule.onNodeWithTag("profile_exit_preview").performClick()
        composeTestRule.onNodeWithTag("profile_camera").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_complete_form_b_banner").assertIsDisplayed()
    }

    private fun sampleJob(id: String, status: JobStatus = JobStatus.Completed): Job {
        return Job(
            id = id,
            title = "Repair Task $id",
            counterpartName = "Client A",
            kind = JobKind.TaskApplication,
            localParty = JobParty.Receiver,
            status = status,
            workMode = JobWorkMode.OnSite,
            locationLabel = "Indiranagar",
            otp = "1234",
            inHistory = false,
        )
    }

    private fun setProfile(profile: SessionProfile = SessionProfile(), jobs: List<Job> = emptyList()) {
        composeTestRule.setContent {
            AskITTheme {
                TestProfileRoute(profile = profile, jobs = jobs)
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
    override fun updateActiveRole(role: String) = error("failed")
    override fun updatePhoneNumber(phone: String) = error("failed")
    override fun updatePushNotifications(enabled: Boolean) = error("failed")
    override fun updateJobAlerts(enabled: Boolean) = error("failed")
    override fun updateLanguage(lang: String) = error("failed")
    override fun updateLocationServices(enabled: Boolean) = error("failed")
    override fun updateWhoCanMessage(option: String) = error("failed")
    override fun resetAppData() = error("failed")
}
