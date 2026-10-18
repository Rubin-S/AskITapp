package com.askit.app.creatordashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.askit.app.R
import com.askit.app.session.SessionProfileStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class CreatorDashboardViewModel(
    private val profileStore: SessionProfileStore,
    /** Demo metrics; only debug/preview builds supply them. Release has no analytics backend yet. */
    private val sampleMetrics: (CreatorTimeRange, Boolean) -> CreatorDashboardSample? = { _, _ -> null },
) : ViewModel() {

    private val selectedTimeRange = MutableStateFlow(CreatorTimeRange.ThisWeek)
    private val aiInsightDismissed = MutableStateFlow(false)

    val uiState: StateFlow<CreatorDashboardUiState> = combine(
        profileStore.profile,
        selectedTimeRange,
        aiInsightDismissed,
    ) { profile, timeRange, isAiDismissed ->
        val isService = profile.hasListedService
        // A user-entered title or skill when there is one; otherwise a localized role label.
        val userRole = profile.listing?.title ?: profile.skills.firstOrNull()
        val role = userRole.takeIf { isService }.orEmpty()
        val roleFallbackRes = when (isService) {
            true -> R.string.creator_role_service_professional
            false -> R.string.creator_role_community_neighbor
        }

        val sample = sampleMetrics(timeRange, isService)
        val base = CreatorDashboardUiState(
            displayName = profile.displayName,
            username = profile.username,
            avatarUrl = profile.avatarUrl,
            isServiceProvider = isService,
            professionOrRole = role,
            roleFallbackRes = roleFallbackRes,
            selectedTimeRange = timeRange,
            isLoading = false,
        )
        if (sample == null) {
            base
        } else {
            base.copy(
                heroKpi = sample.heroKpi,
                categoryMetrics = sample.categoryMetrics,
                aiInsight = sample.aiInsight.takeUnless { isAiDismissed },
                milestone = sample.milestone,
                recentActivities = sample.recentActivities,
                metricsAvailable = true,
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CreatorDashboardUiState(isLoading = true),
    )

    fun selectTimeRange(range: CreatorTimeRange) {
        selectedTimeRange.value = range
    }

    fun dismissAiInsight() {
        aiInsightDismissed.value = true
    }
}
