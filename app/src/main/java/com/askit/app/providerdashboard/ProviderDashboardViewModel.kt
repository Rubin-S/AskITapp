package com.askit.app.providerdashboard

import androidx.lifecycle.ViewModel
import com.askit.app.session.SessionProfile
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
    /** Demo operating data; only debug/preview builds supply one. Release has no dashboard backend yet. */
    private val sampleState: (SessionProfile) -> ProviderDashboardUiState? = { null },
) : ViewModel() {

    private val _uiState = MutableStateFlow(createInitialState())
    val uiState: StateFlow<ProviderDashboardUiState> = _uiState.asStateFlow()

    private fun createInitialState(): ProviderDashboardUiState {
        val profile = profileStore.profile.value
        return sampleState(profile) ?: ProviderDashboardUiState(
            businessName = profile.displayName,
            tradeTitle = profile.listing?.category.orEmpty(),
            isVerified = profile.isVerified,
            metricsAvailable = false,
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
                id = "dashboard-job-${target.id}",
                serviceTitle = target.serviceTitle,
                customerName = target.customerName,
                locality = target.locality,
                progressPercent = 10,
                otpVerified = false,
                statusLabel = "Dispatched",
                conversationId = "dashboard-conv-${target.id}",
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
