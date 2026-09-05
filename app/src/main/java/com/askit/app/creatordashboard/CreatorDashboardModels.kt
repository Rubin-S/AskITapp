package com.askit.app.creatordashboard

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector

enum class CreatorTimeRange(@StringRes val labelRes: Int) {
    Today(com.askit.app.R.string.creator_time_range_today),
    ThisWeek(com.askit.app.R.string.creator_time_range_this_week),
    ThisMonth(com.askit.app.R.string.creator_time_range_this_month),
    Last90Days(com.askit.app.R.string.creator_time_range_last_90_days),
}

enum class MetricTrend {
    Positive,
    Negative,
    Neutral,
}

@Immutable
data class CreatorMetricItem(
    val id: String,
    @StringRes val titleRes: Int,
    val valueText: String,
    val deltaText: String,
    val trend: MetricTrend,
    val icon: ImageVector,
    val isHighlighted: Boolean = false,
)

@Immutable
data class CreatorCategoryMetrics(
    val discovery: List<CreatorMetricItem>,
    val community: List<CreatorMetricItem>,
)

@Immutable
data class HeroKpiSummary(
    val reachValue: String,
    val reachDelta: String,
    val reachTrend: MetricTrend,
    val engagementRate: String,
    val engagementDelta: String,
    val engagementTrend: MetricTrend,
    val sparklinePoints: List<Float>,
    val envelopeLowerBounds: List<Float>,
    val envelopeUpperBounds: List<Float>,
)

@Immutable
data class CreatorAiInsight(
    @StringRes val headerRes: Int,
    @StringRes val titleRes: Int,
    @StringRes val bodyRes: Int,
    val timeWindowArg: String,
    val topicArg: String,
    val percentageArg: String,
    @StringRes val actionLabelRes: Int,
    val trendingTopic: String,
)

@Immutable
data class FollowerMilestone(
    val currentFollowers: Int,
    val targetFollowers: Int,
    val progressPercent: Float,
    val remainingCount: Int,
    val weeklyGrowthText: String,
    val momGrowthText: String,
)

enum class CreatorActivityType {
    NewFollower,
    DirectMessage,
    PostLike,
    PostComment,
}

@Immutable
data class CreatorActivityItem(
    val id: String,
    val type: CreatorActivityType,
    val actorName: String,
    val avatarUrl: String? = null,
    val titleText: String,
    val subtitleText: String,
    val timeAgo: String,
    val targetId: String,
)

@Immutable
data class CreatorDashboardUiState(
    val displayName: String = "",
    val username: String = "",
    val avatarUrl: String? = null,
    val isServiceProvider: Boolean = false,
    val professionOrRole: String = "",
    val selectedTimeRange: CreatorTimeRange = CreatorTimeRange.ThisWeek,
    val heroKpi: HeroKpiSummary = HeroKpiSummary(
        reachValue = "0",
        reachDelta = "0%",
        reachTrend = MetricTrend.Neutral,
        engagementRate = "0%",
        engagementDelta = "0%",
        engagementTrend = MetricTrend.Neutral,
        sparklinePoints = emptyList(),
        envelopeLowerBounds = emptyList(),
        envelopeUpperBounds = emptyList(),
    ),
    val categoryMetrics: CreatorCategoryMetrics = CreatorCategoryMetrics(
        discovery = emptyList(),
        community = emptyList(),
    ),
    val aiInsight: CreatorAiInsight? = null,
    val milestone: FollowerMilestone = FollowerMilestone(
        currentFollowers = 0,
        targetFollowers = 100,
        progressPercent = 0f,
        remainingCount = 100,
        weeklyGrowthText = "0",
        momGrowthText = "0%",
    ),
    val recentActivities: List<CreatorActivityItem> = emptyList(),
    val isLoading: Boolean = false,
)
