package com.askit.app.providerdashboard

import androidx.compose.runtime.Immutable

/**
 * Provider operational status.
 */
enum class ProviderAvailabilityStatus {
    Online,
    Offline,
}

/**
 * An urgent incoming customer service request awaiting response.
 */
@Immutable
data class IncomingRequestItem(
    val id: String,
    val serviceTitle: String,
    val customerName: String,
    val locality: String,
    val distanceKm: Double,
    val remainingSeconds: Int,
    val priceQuote: String,
    val urgencyTag: String = "Urgent",
)

/**
 * An ongoing job currently in progress by the service provider.
 */
@Immutable
data class ProviderActiveJobItem(
    val id: String,
    val serviceTitle: String,
    val customerName: String,
    val locality: String,
    val progressPercent: Int,
    val otpVerified: Boolean,
    val statusLabel: String,
    val conversationId: String,
)

/**
 * Daily operational metrics for the provider command center.
 */
@Immutable
data class ProviderOperatingKpis(
    val todayJobsCount: Int,
    val todayJobsDelta: Int,
    val pendingLiveCount: Int,
    val responseRatePercent: Int,
    val avgResponseMinutes: Double,
)

/**
 * Provider trust and marketplace reputation metrics.
 */
@Immutable
data class ProviderTrustScore(
    val score: Int,
    val percentileRank: Int,
    val weeklyDelta: Int,
)

/**
 * Provider profile strength and verification tier progression.
 */
@Immutable
data class ProviderTierProgress(
    val strengthPercent: Int,
    val currentTier: String,
    val nextTier: String,
    val hint: String,
)

/**
 * Operational alert or priority notification for the provider.
 */
@Immutable
data class ProviderAlertItem(
    val id: String,
    val title: String,
    val timestampLabel: String,
)

/**
 * Comprehensive immutable UI state for the Provider Dashboard.
 */
@Immutable
data class ProviderDashboardUiState(
    val businessName: String = "",
    val tradeTitle: String = "",
    val isVerified: Boolean = false,
    val availability: ProviderAvailabilityStatus = ProviderAvailabilityStatus.Online,
    val kpis: ProviderOperatingKpis = ProviderOperatingKpis(
        todayJobsCount = 6,
        todayJobsDelta = 2,
        pendingLiveCount = 3,
        responseRatePercent = 92,
        avgResponseMinutes = 1.8,
    ),
    val incomingRequests: List<IncomingRequestItem> = emptyList(),
    val activeJobs: List<ProviderActiveJobItem> = emptyList(),
    val trustScore: ProviderTrustScore = ProviderTrustScore(
        score = 88,
        percentileRank = 8,
        weeklyDelta = 4,
    ),
    val tierProgress: ProviderTierProgress = ProviderTierProgress(
        strengthPercent = 85,
        currentTier = "Tier 2",
        nextTier = "Tier 3 Verified",
        hint = "Complete portfolio to unlock Tier 3 Verified",
    ),
    val alerts: List<ProviderAlertItem> = emptyList(),
    val showProUpgrade: Boolean = true,
    val acceptedJobMessage: String? = null,
)
