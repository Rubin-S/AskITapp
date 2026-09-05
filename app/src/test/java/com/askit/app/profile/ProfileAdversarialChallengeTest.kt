package com.askit.app.profile

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.askit.app.home.details.getUserProfileById
import com.askit.app.jobs.Job
import com.askit.app.jobs.JobKind
import com.askit.app.jobs.JobParty
import com.askit.app.jobs.JobStatus
import com.askit.app.jobs.JobWorkMode
import com.askit.app.session.ServiceListing
import com.askit.app.session.SessionProfile
import com.askit.designsystem.theme.AskITTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Adversarial stress harness and empirical challenge suite for Milestone 2:
 * 1. Boundary values in ProfileUiState: zero jobs, extreme job counts, zero followers,
 *    null/empty avatars, blank display names, empty bio.
 * 2. Preview mode toggling: does toggling isPublicPreview alter underlying SessionProfile?
 *    Does it preserve active tab index?
 * 3. Tab bounds clamping: Form B (4 tabs, index 3) switching to Form A (3 tabs),
 *    negative indices, extreme indices, and empty tab lists.
 * 4. Form A visitor protection invariants.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w360dp-h800dp-normal-long-notround-any-xxxhdpi-keyshidden-nonav")
class ProfileAdversarialChallengeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun sampleJob(id: String, status: JobStatus = JobStatus.Completed): Job {
        return Job(
            id = id,
            title = "Task $id",
            counterpartName = "Test Client",
            kind = JobKind.TaskApplication,
            localParty = JobParty.Receiver,
            status = status,
            workMode = JobWorkMode.OnSite,
            locationLabel = "Bengaluru",
            otp = "1234",
            inHistory = false,
        )
    }

    private val baseFormASession = SessionProfile(
        displayName = "Rahul Verma",
        username = "rahul_v",
        bio = "Community member in Indiranagar",
        city = "Bengaluru",
        joinedYear = "2023",
        hasListedService = false,
        followerCount = 84,
        followingCount = 31,
    )

    private val baseFormBSession = SessionProfile(
        displayName = "Priya Sharma",
        username = "priya_e",
        bio = "Certified electrician",
        city = "Chennai",
        joinedYear = "2022",
        hasListedService = true,
        listing = ServiceListing(
            title = "Electrical Safety & Repair",
            category = "Certified Electrician",
            description = "Wiring repairs and installations.",
            quoteLabel = "Free Estimate",
            coverage = "10 km",
            coverageHint = "Service area",
            hours = "9 AM - 6 PM",
            hoursHint = "Working hours",
            response = "< 1 hr",
            responseHint = "Reply time",
            tags = listOf("Wiring", "Inverter"),
            experience = "8+ years",
            live = true,
        ),
        followerCount = 120,
        followingCount = 45,
    )

    // =========================================================================
    // 1. BOUNDARY VALUES IN ProfileUiState
    // =========================================================================

    @Test
    fun boundary_zeroJobs_formA_activityMetricIsZeroAndRendersSafely() {
        val uiState = baseFormASession.toUiState(jobs = emptyList())

        assertEquals("Form A with zero jobs must have 3 metrics", 3, uiState.metrics.size)
        val activityMetric = uiState.metrics.find { it.id == "activity" }
        assertNotNull("Activity metric must exist for Form A", activityMetric)
        assertEquals("0", activityMetric!!.value)

        composeTestRule.setContent {
            AskITTheme {
                UniversalProfileScaffold(uiState = uiState, isVisitor = false)
            }
        }
        composeTestRule.onNodeWithTag("profile_scaffold").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_metrics_bar").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_content_activity").assertExists()
        composeTestRule.onNodeWithTag("profile_requests_empty").assertExists()
    }

    @Test
    fun boundary_zeroJobs_formB_completedJobsMetricBehavior() {
        // When Form B has empty jobs list, completedCount is 0
        val uiStateEmptyJobs = baseFormBSession.toUiState(jobs = emptyList())
        val completedMetric = uiStateEmptyJobs.metrics.find { it.id == "completed_jobs" }
        assertNotNull("Completed jobs metric must exist for Form B", completedMetric)

        // When Form B has jobs but none are Completed
        val nonCompletedJobs = listOf(
            sampleJob("j1", JobStatus.Applied),
            sampleJob("j2", JobStatus.Started),
        )
        val uiStateNonCompleted = baseFormBSession.toUiState(jobs = nonCompletedJobs)
        val completedMetric2 = uiStateNonCompleted.metrics.find { it.id == "completed_jobs" }
        assertNotNull(completedMetric2)

        composeTestRule.setContent {
            AskITTheme {
                UniversalProfileScaffold(uiState = uiStateNonCompleted, isVisitor = false)
            }
        }
        composeTestRule.onNodeWithTag("profile_scaffold").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_metrics_bar").assertIsDisplayed()
    }

    @Test
    fun boundary_extremeJobCounts_scalesWithoutCrashOrOverflow() {
        val largeJobList = (1..10_000).map { sampleJob("job_$it", JobStatus.Completed) }

        // Form A scaling
        val uiStateA = baseFormASession.toUiState(jobs = largeJobList)
        assertEquals("10000", uiStateA.metrics.find { it.id == "activity" }?.value)

        // Form B scaling
        val uiStateB = baseFormBSession.toUiState(jobs = largeJobList)
        assertEquals("10000", uiStateB.metrics.find { it.id == "completed_jobs" }?.value)
    }

    @Test
    fun boundary_zeroFollowersAndFollowing_rendersZeroWithoutCrash() {
        val zeroFollowersSession = baseFormASession.copy(followerCount = 0, followingCount = 0)
        val uiState = zeroFollowersSession.toUiState()

        assertEquals("0", uiState.metrics.find { it.id == "followers" }?.value)
        assertEquals("0", uiState.metrics.find { it.id == "following" }?.value)

        composeTestRule.setContent {
            AskITTheme {
                UniversalProfileScaffold(uiState = uiState, isVisitor = false)
            }
        }
        composeTestRule.onNodeWithTag("profile_metrics_bar").assertIsDisplayed()
    }

    @Test
    fun boundary_nullAndEmptyAvatars_renderSafely() {
        val nullAvatarState = baseFormASession.copy(avatarUrl = null).toUiState()
        val emptyAvatarState = baseFormASession.copy(avatarUrl = "").toUiState()

        val avatarState = mutableStateOf(nullAvatarState)

        composeTestRule.setContent {
            AskITTheme {
                UniversalProfileScaffold(uiState = avatarState.value, isVisitor = false)
            }
        }
        composeTestRule.onNodeWithTag("profile_avatar").assertIsDisplayed()

        // Mutate to empty string avatar
        avatarState.value = emptyAvatarState
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("profile_avatar").assertIsDisplayed()
    }

    @Test
    fun boundary_blankDisplayNameAndEmptyBio_rendersSafelyWithoutCrashing() {
        val blankIdentitySession = baseFormASession.copy(displayName = "   ", bio = "")
        val uiState = blankIdentitySession.toUiState()

        composeTestRule.setContent {
            AskITTheme {
                UniversalProfileScaffold(uiState = uiState, isVisitor = false)
            }
        }
        composeTestRule.onNodeWithTag("profile_display_name").assertIsDisplayed()
        // Bio tag should NOT exist when bio is blank
        composeTestRule.onNodeWithTag("profile_bio").assertDoesNotExist()
    }

    // =========================================================================
    // 2. PREVIEW MODE TOGGLING PRESERVATION
    // =========================================================================

    @Test
    fun previewToggling_doesNotMutateUnderlyingSessionProfile() {
        val originalSession = baseFormASession.copy()

        // Generate preview state
        val previewUiState = originalSession.toUiState(isPublicPreview = true, selectedTabIndex = 1)
        assertTrue("isPublicPreview must be true", previewUiState.isPublicPreview)

        // Generate normal state
        val normalUiState = originalSession.toUiState(isPublicPreview = false, selectedTabIndex = 0)
        assertFalse("isPublicPreview must be false", normalUiState.isPublicPreview)

        // Verify underlying SessionProfile fields remain 100% identical and unmutated
        assertEquals("SessionProfile must remain completely unchanged", originalSession, baseFormASession)
        assertEquals("rahul_v", originalSession.username)
        assertEquals("Rahul Verma", originalSession.displayName)
        assertEquals(84, originalSession.followerCount)
        assertEquals(31, originalSession.followingCount)
        assertFalse(originalSession.hasListedService)
    }

    @Test
    fun previewToggling_preservesSelectedTabIndexInScaffold() {
        val uiStateHolder = mutableStateOf(
            baseFormASession.toUiState(isPublicPreview = false, selectedTabIndex = 1)
        )

        composeTestRule.setContent {
            AskITTheme {
                UniversalProfileScaffold(
                    uiState = uiStateHolder.value,
                    isVisitor = false,
                    onExitPreview = {
                        uiStateHolder.value = baseFormASession.toUiState(
                            isPublicPreview = false,
                            selectedTabIndex = uiStateHolder.value.selectedTabIndex,
                        )
                    },
                )
            }
        }

        // Selected tab index 1 is "About"
        composeTestRule.onNodeWithTag("profile_content_about").assertIsDisplayed()

        // Toggle preview mode ON while maintaining tab index 1
        uiStateHolder.value = baseFormASession.toUiState(isPublicPreview = true, selectedTabIndex = 1)
        composeTestRule.waitForIdle()

        // Floating preview banner must appear
        composeTestRule.onNodeWithTag("profile_preview_banner").assertIsDisplayed()
        // Active content pane must remain "About" (index 1 preserved)
        composeTestRule.onNodeWithTag("profile_content_about").assertIsDisplayed()

        // Exit preview via banner click
        composeTestRule.onNodeWithTag("profile_exit_preview").performClick()
        composeTestRule.waitForIdle()

        // Floating preview banner disappears
        composeTestRule.onNodeWithTag("profile_preview_banner").assertDoesNotExist()
        // Content pane must still be "About"
        composeTestRule.onNodeWithTag("profile_content_about").assertIsDisplayed()
    }

    // =========================================================================
    // 3. TAB BOUNDS CLAMPING (FORM B -> FORM A & OUT-OF-BOUNDS)
    // =========================================================================

    @Test
    fun tabBounds_switchingFormBIndex3ToFormA_clampsSafelyWithoutCrash() {
        // Form B has 4 tabs (indices 0..3: services, gallery, reviews, about).
        // Form A has 3 tabs (indices 0..2: activity, about, reviews).
        // When user is on tab index 3 in Form B and switches to Form A:
        val formBUiState = baseFormBSession.toUiState(selectedTabIndex = 3)
        assertEquals("Form B selected tab index should be 3", 3, formBUiState.selectedTabIndex)

        // Mapping to Form A with selectedTabIndex = 3 should safely clamp to 2
        val formAUiState = baseFormASession.toUiState(selectedTabIndex = 3)
        assertEquals("Form A selected tab index must clamp to 2", 2, formAUiState.selectedTabIndex)

        val scaffoldState = mutableStateOf(formBUiState)

        composeTestRule.setContent {
            AskITTheme {
                UniversalProfileScaffold(uiState = scaffoldState.value, isVisitor = false)
            }
        }

        // Form B tab index 3 is "About"
        composeTestRule.onNodeWithTag("profile_content_about").assertIsDisplayed()

        // Dynamically transition scaffold from Form B (4 tabs) to Form A (3 tabs) with index 3
        scaffoldState.value = formAUiState
        composeTestRule.waitForIdle()

        // Must safely display clamped tab content (index 2 in Form A is "Reviews") without crashing
        composeTestRule.onNodeWithTag("profile_tabs").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_content_reviews").assertIsDisplayed()
    }

    @Test
    fun tabBounds_extremeOutOfBoundsIndex_coercesToValidRange() {
        // Form A (3 tabs: indices 0..2)
        val stateAHigh = baseFormASession.toUiState(selectedTabIndex = 999)
        assertEquals(2, stateAHigh.selectedTabIndex)

        val stateALow = baseFormASession.toUiState(selectedTabIndex = -999)
        assertEquals(0, stateALow.selectedTabIndex)

        // Form B (4 tabs: indices 0..3)
        val stateBHigh = baseFormBSession.toUiState(selectedTabIndex = 999)
        assertEquals(3, stateBHigh.selectedTabIndex)

        val stateBLow = baseFormBSession.toUiState(selectedTabIndex = -999)
        assertEquals(0, stateBLow.selectedTabIndex)
    }

    @Test
    fun tabBounds_emptyTabList_rendersFallbackWithoutCrash() {
        val emptyTabsState = ProfileUiState(
            displayName = "No Tabs User",
            tabs = emptyList(),
            selectedTabIndex = 5,
        )

        composeTestRule.setContent {
            AskITTheme {
                UniversalProfileScaffold(uiState = emptyTabsState, isVisitor = false)
            }
        }
        composeTestRule.onNodeWithTag("profile_scaffold").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_tabs").assertDoesNotExist()
        composeTestRule.onNodeWithTag("profile_content_section").assertIsDisplayed()
    }

    // =========================================================================
    // 4. FORM A VISITOR PROTECTION & CAMERA AFFORDANCE INVARIANTS
    // =========================================================================

    @Test
    fun formAVisitorProtection_noRequestService_noProviderTabs() {
        val visitorData = getUserProfileById("member-1")
        assertFalse(visitorData.isProvider)

        val uiState = visitorData.toUiState()
        assertFalse(uiState.isProvider)
        assertNull(uiState.tradeHeadline)

        val tabIds = uiState.tabs.map { it.id }
        assertFalse("Form A visitor must not have services tab", tabIds.contains("services"))
        assertFalse("Form A visitor must not have gallery tab", tabIds.contains("gallery"))
        assertEquals(listOf("activity", "about", "reviews"), tabIds)

        composeTestRule.setContent {
            AskITTheme {
                UniversalProfileScaffold(uiState = uiState, isVisitor = true)
            }
        }

        // Request Service button MUST NOT exist
        composeTestRule.onNodeWithTag("profile_action_request_service").assertDoesNotExist()
        // Message and Follow buttons MUST exist
        composeTestRule.onNodeWithTag("profile_action_message").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_action_follow").assertIsDisplayed()
        // Motivational banner MUST NOT exist for visitors
        composeTestRule.onNodeWithTag("profile_complete_form_b_banner").assertDoesNotExist()
    }
}
