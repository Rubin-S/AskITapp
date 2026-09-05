package com.askit.app.providerdashboard

import androidx.lifecycle.ViewModel
import com.askit.app.session.SessionProfileStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * ViewModel for Provider Dashboard, managing operational availability,
 * incoming dispatch queue, active jobs, KPIs, and alerts.
 */
class ProviderDashboardViewModel(
    private val profileStore: SessionProfileStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(createInitialState())
    val uiState: StateFlow<ProviderDashboardUiState> = _uiState.asStateFlow()

    private fun createInitialState(): ProviderDashboardUiState {
        val profile = profileStore.profile.value
        val businessName = profile.displayName.ifBlank { "Rajesh Electrical" }
        val category = profile.listing?.category ?: "Electrician Pro"
        val profileStrength = if (profile.profileStrengthPercent > 0) profile.profileStrengthPercent else 85

        return ProviderDashboardUiState(
            businessName = businessName,
            tradeTitle = category,
            isVerified = true,
            availability = ProviderAvailabilityStatus.Online,
            kpis = ProviderOperatingKpis(
                todayJobsCount = 6,
                todayJobsDelta = 2,
                pendingLiveCount = 3,
                responseRatePercent = 92,
                avgResponseMinutes = 1.8,
            ),
            incomingRequests = listOf(
                IncomingRequestItem(
                    id = "req-1",
                    serviceTitle = "AC Repair Service",
                    customerName = "Suresh R.",
                    locality = "Gandhipuram",
                    distanceKm = 2.3,
                    remainingSeconds = 272,
                    priceQuote = "₹450",
                    urgencyTag = "High Demand",
                ),
            ),
            activeJobs = listOf(
                ProviderActiveJobItem(
                    id = "job-101",
                    serviceTitle = "Plumbing — RS Puram",
                    customerName = "Ravi Kumar",
                    locality = "RS Puram",
                    progressPercent = 50,
                    otpVerified = true,
                    statusLabel = "In progress",
                    conversationId = "conv-ravi-kumar",
                ),
            ),
            trustScore = ProviderTrustScore(
                score = 88,
                percentileRank = 8,
                weeklyDelta = 4,
            ),
            tierProgress = ProviderTierProgress(
                strengthPercent = profileStrength,
                currentTier = "Tier 2",
                nextTier = "Tier 3 Verified",
                hint = "Complete portfolio to unlock Tier 3 Verified",
            ),
            alerts = listOf(
                ProviderAlertItem(
                    id = "alt-1",
                    title = "New request: Electrical work near Ganapathy",
                    timestampLabel = "2m ago",
                ),
                ProviderAlertItem(
                    id = "alt-2",
                    title = "Timer warning: AC Repair expires soon",
                    timestampLabel = "4m ago",
                ),
                ProviderAlertItem(
                    id = "alt-3",
                    title = "Trust score improved to 88",
                    timestampLabel = "Today",
                ),
            ),
            showProUpgrade = true,
        )
    }

    fun toggleAvailability() {
        _uiState.update { current ->
            val nextStatus = if (current.availability == ProviderAvailabilityStatus.Online) {
                ProviderAvailabilityStatus.Offline
            } else {
                ProviderAvailabilityStatus.Online
            }
            current.copy(availability = nextStatus)
        }
    }

    fun acceptRequest(requestId: String) {
        _uiState.update { current ->
            val target = current.incomingRequests.find { it.id == requestId }
            if (target == null) return@update current

            val newJob = ProviderActiveJobItem(
                id = "job-${target.id}",
                serviceTitle = target.serviceTitle,
                customerName = target.customerName,
                locality = target.locality,
                progressPercent = 10,
                otpVerified = false,
                statusLabel = "Dispatched",
                conversationId = "conv-${target.id}",
            )

            val updatedRequests = current.incomingRequests.filterNot { it.id == requestId }
            val updatedActiveJobs = listOf(newJob) + current.activeJobs
            val updatedKpis = current.kpis.copy(
                todayJobsCount = current.kpis.todayJobsCount + 1,
                pendingLiveCount = (current.kpis.pendingLiveCount - 1).coerceAtLeast(0),
            )

            current.copy(
                incomingRequests = updatedRequests,
                activeJobs = updatedActiveJobs,
                kpis = updatedKpis,
                acceptedJobMessage = "Job accepted: ${target.serviceTitle}",
            )
        }
    }

    fun declineRequest(requestId: String) {
        _uiState.update { current ->
            val updatedRequests = current.incomingRequests.filterNot { it.id == requestId }
            val updatedKpis = current.kpis.copy(
                pendingLiveCount = (current.kpis.pendingLiveCount - 1).coerceAtLeast(0),
            )
            current.copy(
                incomingRequests = updatedRequests,
                kpis = updatedKpis,
            )
        }
    }

    fun clearAcceptedJobMessage() {
        _uiState.update { it.copy(acceptedJobMessage = null) }
    }

    fun dismissAlert(alertId: String) {
        _uiState.update { current ->
            current.copy(alerts = current.alerts.filterNot { it.id == alertId })
        }
    }
}
