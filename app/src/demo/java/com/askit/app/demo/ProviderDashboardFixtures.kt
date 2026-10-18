package com.askit.app.demo

import com.askit.app.providerdashboard.IncomingRequestItem
import com.askit.app.providerdashboard.ProviderActiveJobItem
import com.askit.app.providerdashboard.ProviderAlertItem
import com.askit.app.providerdashboard.ProviderAvailabilityStatus
import com.askit.app.providerdashboard.ProviderDashboardUiState
import com.askit.app.providerdashboard.ProviderOperatingKpis
import com.askit.app.providerdashboard.ProviderTierProgress
import com.askit.app.providerdashboard.ProviderTrustScore
import com.askit.app.session.SessionProfile

/** Demo provider dashboard (made-up KPIs, requests and alerts). Debug/preview only. */
internal fun sampleProviderDashboard(profile: SessionProfile): ProviderDashboardUiState {
    val businessName = profile.displayName
    val category = profile.listing?.category ?: "Electrician Pro"
    val profileStrength = if (profile.profileStrengthPercent > 0) profile.profileStrengthPercent else 85

    return ProviderDashboardUiState(
        businessName = businessName,
        tradeTitle = category,
        // Verification is server-managed: never show a badge the account does not hold.
        isVerified = profile.isVerified,
        metricsAvailable = true,
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
            ),
        ),
        activeJobs = listOf(
            ProviderActiveJobItem(
                id = "demo-active-job",
                serviceTitle = "Plumbing — RS Puram",
                customerName = "Ravi Kumar",
                locality = "RS Puram",
                progressPercent = 50,
                otpVerified = true,
                statusLabel = "In progress",
                conversationId = "demo-conv-ravi",
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
