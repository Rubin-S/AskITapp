package com.askit.app.creatordashboard

import com.askit.app.session.ServiceListing
import com.askit.app.session.SessionProfile
import com.askit.app.session.SessionProfileStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CreatorDashboardViewModelTest {

    @Test
    fun defaultState_loadsProfileAndWeeklyMetrics() = runTest {
        val profileStore = SessionProfileStore(
            initial = SessionProfile(
                displayName = "Rajesh Electrical",
                username = "rajeshelectrical",
                hasListedService = true,
                listing = ServiceListing(
                    title = "Electrician Pro",
                    category = "Electrical",
                    description = "Home electrical services",
                    quoteLabel = "₹500",
                    coverage = "Coimbatore",
                    coverageHint = "",
                    hours = "9 AM - 6 PM",
                    hoursHint = "",
                    response = "Under 1 hr",
                    responseHint = "",
                    tags = listOf("Wiring", "AC"),
                    experience = "5 yrs",
                ),
            ),
        )

        val viewModel = CreatorDashboardViewModel(profileStore)
        val state = viewModel.uiState.first { !it.isLoading }

        assertEquals("Rajesh Electrical", state.displayName)
        assertEquals("rajeshelectrical", state.username)
        assertTrue(state.isServiceProvider)
        assertEquals("Electrician Pro", state.professionOrRole)
        assertEquals(CreatorTimeRange.ThisWeek, state.selectedTimeRange)
        assertEquals("18.4k", state.heroKpi.reachValue)
        assertEquals("4.8%", state.heroKpi.engagementRate)
        assertEquals(3420, state.milestone.currentFollowers)
        assertEquals(3500, state.milestone.targetFollowers)
        assertNotNull(state.aiInsight)
        assertEquals("AC maintenance", state.aiInsight?.trendingTopic)
        assertEquals(4, state.recentActivities.size)
    }

    @Test
    fun timeRangeSelection_updatesKpisAndMetrics() = runTest {
        val viewModel = CreatorDashboardViewModel()

        // Switch to Today
        viewModel.selectTimeRange(CreatorTimeRange.Today)
        val todayState = viewModel.uiState.first { it.selectedTimeRange == CreatorTimeRange.Today }
        assertEquals("1,240", todayState.heroKpi.reachValue)
        assertEquals("5.2%", todayState.heroKpi.engagementRate)
        assertEquals(8, todayState.heroKpi.sparklinePoints.size)

        // Switch to This Month
        viewModel.selectTimeRange(CreatorTimeRange.ThisMonth)
        val monthState = viewModel.uiState.first { it.selectedTimeRange == CreatorTimeRange.ThisMonth }
        assertEquals("74.8k", monthState.heroKpi.reachValue)
        assertEquals("4.6%", monthState.heroKpi.engagementRate)

        // Switch to Last 90 Days
        viewModel.selectTimeRange(CreatorTimeRange.Last90Days)
        val days90State = viewModel.uiState.first { it.selectedTimeRange == CreatorTimeRange.Last90Days }
        assertEquals("210.5k", days90State.heroKpi.reachValue)
        assertEquals("4.5%", days90State.heroKpi.engagementRate)
    }

    @Test
    fun dismissAiInsight_removesInsightFromState() = runTest {
        val viewModel = CreatorDashboardViewModel()
        val initial = viewModel.uiState.first { !it.isLoading }
        assertNotNull(initial.aiInsight)

        viewModel.dismissAiInsight()
        val dismissed = viewModel.uiState.first { it.aiInsight == null }
        assertNull(dismissed.aiInsight)
    }

    @Test
    fun nonServiceProvider_adaptsRoleGracefully() = runTest {
        val nonServiceStore = SessionProfileStore(
            initial = SessionProfile(
                displayName = "Meera Raman",
                username = "meera.raman",
                hasListedService = false,
                listing = null,
            ),
        )

        val viewModel = CreatorDashboardViewModel(nonServiceStore)
        val state = viewModel.uiState.first { !it.isLoading }

        assertEquals("Meera Raman", state.displayName)
        assertFalse(state.isServiceProvider)
        assertEquals("Community Creator & Neighbor", state.professionOrRole)
        assertEquals("Community help", state.aiInsight?.trendingTopic)
    }
}
