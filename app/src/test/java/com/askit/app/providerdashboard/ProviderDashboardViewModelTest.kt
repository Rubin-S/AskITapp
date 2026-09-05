package com.askit.app.providerdashboard

import com.askit.app.session.ServiceListing
import com.askit.app.session.SessionProfile
import com.askit.app.session.SessionProfileStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProviderDashboardViewModelTest {

    private fun createStoreWithListing(): SessionProfileStore {
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
    fun initialState_loadsProviderProfileAndKpis() {
        val store = createStoreWithListing()
        val viewModel = ProviderDashboardViewModel(store)
        val state = viewModel.uiState.value

        assertEquals("Rajesh Electrical", state.businessName)
        assertEquals("Electrician Pro", state.tradeTitle)
        assertTrue(state.isVerified)
        assertEquals(ProviderAvailabilityStatus.Online, state.availability)
        assertEquals(6, state.kpis.todayJobsCount)
        assertEquals(3, state.kpis.pendingLiveCount)
        assertEquals(92, state.kpis.responseRatePercent)
        assertEquals(1, state.incomingRequests.size)
        assertEquals("AC Repair Service", state.incomingRequests[0].serviceTitle)
        assertEquals(1, state.activeJobs.size)
        assertEquals("Plumbing — RS Puram", state.activeJobs[0].serviceTitle)
        assertEquals(88, state.trustScore.score)
    }

    @Test
    fun toggleAvailability_switchesBetweenOnlineAndOffline() {
        val store = createStoreWithListing()
        val viewModel = ProviderDashboardViewModel(store)

        assertEquals(ProviderAvailabilityStatus.Online, viewModel.uiState.value.availability)

        viewModel.toggleAvailability()
        assertEquals(ProviderAvailabilityStatus.Offline, viewModel.uiState.value.availability)

        viewModel.toggleAvailability()
        assertEquals(ProviderAvailabilityStatus.Online, viewModel.uiState.value.availability)
    }

    @Test
    fun acceptRequest_movesRequestToActiveJobs_andUpdatesKpis() {
        val store = createStoreWithListing()
        val viewModel = ProviderDashboardViewModel(store)

        viewModel.acceptRequest("req-1")
        val state = viewModel.uiState.value

        assertTrue(state.incomingRequests.isEmpty())
        assertEquals(2, state.activeJobs.size)
        assertEquals("AC Repair Service", state.activeJobs[0].serviceTitle)
        assertEquals(7, state.kpis.todayJobsCount)
        assertEquals(2, state.kpis.pendingLiveCount)
        assertNotNull(state.acceptedJobMessage)

        viewModel.clearAcceptedJobMessage()
        assertNull(viewModel.uiState.value.acceptedJobMessage)
    }

    @Test
    fun declineRequest_removesRequest_andUpdatesKpis() {
        val store = createStoreWithListing()
        val viewModel = ProviderDashboardViewModel(store)

        viewModel.declineRequest("req-1")
        val state = viewModel.uiState.value

        assertTrue(state.incomingRequests.isEmpty())
        assertEquals(1, state.activeJobs.size)
        assertEquals(2, state.kpis.pendingLiveCount)
    }

    @Test
    fun dismissAlert_removesAlertFromList() {
        val store = createStoreWithListing()
        val viewModel = ProviderDashboardViewModel(store)

        assertEquals(3, viewModel.uiState.value.alerts.size)
        viewModel.dismissAlert("alt-1")
        assertEquals(2, viewModel.uiState.value.alerts.size)
        assertFalse(viewModel.uiState.value.alerts.any { it.id == "alt-1" })
    }
}
