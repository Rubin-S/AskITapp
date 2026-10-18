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
data class ProviderTrustScore(val score: Int, val percentileRank: Int, val weeklyDelta: Int)

/**
 * Provider profile strength and verification tier progression.
 */
@Immutable
data class ProviderTierProgress(val strengthPercent: Int, val currentTier: String, val nextTier: String, val hint: String)

/**
 * Operational alert or priority notification for the provider.
 */
@Immutable
data class ProviderAlertItem(val id: String, val title: String, val timestampLabel: String)

/**
 * Comprehensive immutable UI state for the Provider Dashboard.
 */
@Immutable
data class ProviderDashboardUiState(
    val businessName: String = "",
    val tradeTitle: String = "",
    val isVerified: Boolean = false,
    val availability: ProviderAvailabilityStatus = ProviderAvailabilityStatus.Online,
    /**
     * False until a backend supplies real operating data. The screen then shows an honest
     * "not available yet" state instead of KPIs, trust score, requests and alerts.
     */
    val metricsAvailable: Boolean = false,
    val kpis: ProviderOperatingKpis = ProviderOperatingKpis(
        todayJobsCount = 0,
        todayJobsDelta = 0,
        pendingLiveCount = 0,
        responseRatePercent = 0,
        avgResponseMinutes = 0.0,
    ),
    val incomingRequests: List<IncomingRequestItem> = emptyList(),
    val activeJobs: List<ProviderActiveJobItem> = emptyList(),
    val trustScore: ProviderTrustScore = ProviderTrustScore(
        score = 0,
        percentileRank = 0,
        weeklyDelta = 0,
    ),
    val tierProgress: ProviderTierProgress = ProviderTierProgress(
        strengthPercent = 0,
        currentTier = "",
        nextTier = "",
        hint = "",
    ),
    val alerts: List<ProviderAlertItem> = emptyList(),
    val showProUpgrade: Boolean = false,
    val acceptedJobMessage: String? = null,
)
