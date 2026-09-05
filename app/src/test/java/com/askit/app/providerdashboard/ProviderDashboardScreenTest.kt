package com.askit.app.providerdashboard

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.askit.app.session.ServiceListing
import com.askit.app.session.SessionProfile
import com.askit.app.session.SessionProfileStore
import com.askit.designsystem.theme.AskITTheme
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ProviderDashboardScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun createStore(): SessionProfileStore {
        return SessionProfileStore(
            initial = SessionProfile(
                displayName = "Rajesh Electrical",
                username = "rajeshelectrical",
                hasListedService = true,
                listing = ServiceListing(
                    title = "Residential Electrical Services",
                    category = "Electrician Pro",
                    experience = "8 years",
                    description = "Specializing in home wiring and emergency repairs",
                    quoteLabel = "₹350 visit",
                    coverage = "Coimbatore",
                    coverageHint = "",
                    hours = "9 AM - 6 PM",
                    hoursHint = "",
                    response = "Under 1 hr",
                    responseHint = "",
                    tags = listOf("Wiring", "AC"),
                    live = true,
                ),
                profileStrengthPercent = 85,
            ),
        )
    }

    @Test
    fun screenRenders_allSectionsAndInteractions() {
        val viewModel = ProviderDashboardViewModel(createStore())
        var backClicked = false
        var openedJobId: String? = null
        var openedChatId: String? = null
        var editProfileClicked = false
        var uploadWorkClicked = false
        val listState = LazyListState()

        composeTestRule.setContent {
            AskITTheme(darkTheme = false) {
                ProviderDashboardRoute(
                    viewModel = viewModel,
                    onBack = { backClicked = true },
                    onOpenJob = { openedJobId = it },
                    onOpenChat = { openedChatId = it },
                    onEditProfile = { editProfileClicked = true },
                    onUploadWork = { uploadWorkClicked = true },
                    lazyListState = listState,
                )
            }
        }

        // Top bar & Header
        composeTestRule.onNodeWithText("Provider Dashboard").assertIsDisplayed()
        composeTestRule.onNodeWithText("Rajesh Electrical").assertIsDisplayed()
        composeTestRule.onNodeWithText("Electrician Pro").assertIsDisplayed()
        composeTestRule.onNodeWithTag("provider_online_switch").assertIsDisplayed()

        // Incoming Dispatch Request
        composeTestRule.runOnIdle { runBlocking { listState.scrollToItem(1) } }
        composeTestRule.onNodeWithText("Incoming Requests").assertIsDisplayed()
        composeTestRule.onNodeWithText("AC Repair Service").assertIsDisplayed()
        composeTestRule.runOnIdle { runBlocking { listState.scrollToItem(2) } }
        composeTestRule.onNodeWithTag("provider_accept_btn_req-1").assertIsDisplayed()

        // Active Job
        composeTestRule.runOnIdle { runBlocking { listState.scrollToItem(3) } }
        composeTestRule.onNodeWithText("Active Jobs").assertIsDisplayed()
        composeTestRule.onNodeWithText("Plumbing — RS Puram").assertIsDisplayed()
        composeTestRule.onNodeWithTag("provider_job_chat_job-101").assertIsDisplayed()
        composeTestRule.onNodeWithTag("provider_job_chat_job-101").performClick()
        assertEquals("conv-ravi-kumar", openedChatId)

        // Operational KPIs
        composeTestRule.runOnIdle { runBlocking { listState.scrollToItem(5) } }
        composeTestRule.onNodeWithTag("provider_kpi_todays_jobs").assertIsDisplayed()
        composeTestRule.onAllNodesWithText("6")[0].assertIsDisplayed()
        composeTestRule.onNodeWithTag("provider_kpi_pending_live").assertIsDisplayed()
        composeTestRule.onAllNodesWithText("3")[0].assertIsDisplayed()

        // Trust Card & Tier Progress
        composeTestRule.runOnIdle { runBlocking { listState.scrollToItem(7) } }
        composeTestRule.onNodeWithTag("provider_trust_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("provider_action_improve").performClick()
        assertEquals(true, editProfileClicked)
        composeTestRule.onNodeWithTag("provider_action_upload").performClick()
        assertEquals(true, uploadWorkClicked)

        // Priority Alerts
        composeTestRule.runOnIdle { runBlocking { listState.scrollToItem(8) } }
        composeTestRule.onNodeWithTag("provider_alerts_card").assertIsDisplayed()
        composeTestRule.onNodeWithText("New request: Electrical work near Ganapathy").assertIsDisplayed()

        // Pro Banner
        composeTestRule.runOnIdle { runBlocking { listState.scrollToItem(9) } }
        composeTestRule.onNodeWithTag("provider_pro_banner").assertIsDisplayed()
        composeTestRule.onNodeWithText("Boost Your Business").assertIsDisplayed()

        // Back button
        composeTestRule.onNodeWithTag("provider_dashboard_back_button").performClick()
        assertEquals(true, backClicked)
    }

    @Test
    fun onlineSwitch_togglesVisibilityOfIncomingRequests() {
        val viewModel = ProviderDashboardViewModel(createStore())
        val listState = LazyListState()

        composeTestRule.setContent {
            AskITTheme(darkTheme = false) {
                ProviderDashboardRoute(
                    viewModel = viewModel,
                    onBack = {},
                    lazyListState = listState,
                )
            }
        }

        composeTestRule.runOnIdle { runBlocking { listState.scrollToItem(2) } }
        composeTestRule.onNodeWithText("AC Repair Service").assertIsDisplayed()

        // Toggle to offline
        composeTestRule.runOnIdle { runBlocking { listState.scrollToItem(0) } }
        composeTestRule.onNodeWithTag("provider_online_switch").performClick()
        composeTestRule.waitForIdle()

        // Incoming requests are now hidden while offline
        composeTestRule.onNodeWithText("Offline · Paused").assertIsDisplayed()
        composeTestRule.onNodeWithText("AC Repair Service").assertDoesNotExist()

        // Toggle back to online
        composeTestRule.onNodeWithTag("provider_online_switch").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.runOnIdle { runBlocking { listState.scrollToItem(2) } }
        composeTestRule.onNodeWithText("AC Repair Service").assertIsDisplayed()
    }

    @Test
    fun acceptingRequest_movesToActiveJobs() {
        val viewModel = ProviderDashboardViewModel(createStore())
        val listState = LazyListState()

        composeTestRule.setContent {
            AskITTheme(darkTheme = false) {
                ProviderDashboardRoute(
                    viewModel = viewModel,
                    onBack = {},
                    lazyListState = listState,
                )
            }
        }

        composeTestRule.runOnIdle { runBlocking { listState.scrollToItem(2) } }
        composeTestRule.onNodeWithTag("provider_accept_btn_req-1").performClick()
        composeTestRule.waitForIdle()

        // Request card is gone from incoming, now in active jobs
        composeTestRule.onNodeWithTag("provider_accept_btn_req-1").assertDoesNotExist()
        composeTestRule.onNodeWithTag("provider_active_job_job-req-1").assertIsDisplayed()
    }

    @Test
    @Config(qualifiers = "ta-rIN-w360dp-h800dp-normal-long-notround-any-xxxhdpi-keyshidden-nonav")
    fun screenRenders_tamilNativeCopywritingAccurately() {
        val viewModel = ProviderDashboardViewModel(createStore())
        val listState = LazyListState()

        composeTestRule.setContent {
            AskITTheme(darkTheme = false) {
                ProviderDashboardRoute(
                    viewModel = viewModel,
                    onBack = {},
                    lazyListState = listState,
                )
            }
        }

        composeTestRule.onNodeWithText("வழங்குநர் டாஷ்போர்டு").assertIsDisplayed()
        composeTestRule.onNodeWithText("ஆன்லைன் · கோரிக்கைகள் ஏற்கப்படுகின்றன").assertIsDisplayed()
        composeTestRule.onNodeWithText("உள்வரும் கோரிக்கைகள்").assertIsDisplayed()
        composeTestRule.onNodeWithText("நடப்பு பணிகள்").assertIsDisplayed()

        // Scroll to Tamil KPIs
        composeTestRule.runOnIdle { runBlocking { listState.scrollToItem(5) } }
        composeTestRule.onNodeWithText("இன்றைய பணிகள்").assertIsDisplayed()
        composeTestRule.onNodeWithText("பதில் விகிதம்").assertIsDisplayed()

        // Scroll to Tamil Trust card
        composeTestRule.runOnIdle { runBlocking { listState.scrollToItem(7) } }
        composeTestRule.onNodeWithText("நம்பிக்கை மதிப்பெண்").assertIsDisplayed()
        composeTestRule.onNodeWithText("மேம்படுத்து").assertIsDisplayed()
        composeTestRule.onNodeWithText("பதிவேற்று").assertIsDisplayed()
    }
}
