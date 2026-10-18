package com.askit.app.demo

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import com.askit.app.R
import com.askit.app.creatordashboard.CreatorActivityItem
import com.askit.app.creatordashboard.CreatorActivityType
import com.askit.app.creatordashboard.CreatorAiInsight
import com.askit.app.creatordashboard.CreatorCategoryMetrics
import com.askit.app.creatordashboard.CreatorDashboardSample
import com.askit.app.creatordashboard.CreatorMetricItem
import com.askit.app.creatordashboard.CreatorTimeRange
import com.askit.app.creatordashboard.FollowerMilestone
import com.askit.app.creatordashboard.HeroKpiSummary
import com.askit.app.creatordashboard.MetricTrend

/**
 * Made-up creator analytics for debug/preview builds. Not compiled into release. Activity targets
 * use a `demo-` prefix so they never collide with real local IDs.
 */
internal object CreatorDashboardFixtures {
    fun sample(timeRange: CreatorTimeRange, isService: Boolean): CreatorDashboardSample {
        val heroKpi = getHeroKpi(timeRange)
        val metrics = getCategoryMetrics(timeRange)
        val aiInsight = CreatorAiInsight(
            headerRes = R.string.creator_ai_insight_header,
            titleRes = R.string.creator_ai_insight_timing_title,
            bodyRes = R.string.creator_ai_insight_timing_body,
            timeWindowArg = "7:30–8:30 PM",
            topicArg = if (isService) "AC maintenance" else "Community updates",
            percentageArg = "+40%",
            actionLabelRes = R.string.creator_ai_cta_create_post,
            trendingTopic = if (isService) "AC maintenance" else "Community help",
        )

        val milestone = FollowerMilestone(
            currentFollowers = 3420,
            targetFollowers = 3500,
            progressPercent = 0.84f,
            remainingCount = 80,
            weeklyGrowthText = "+210",
            momGrowthText = "+8.3%",
        )

        val activities = listOf(
            CreatorActivityItem(
                id = "act-1",
                type = CreatorActivityType.NewFollower,
                titleText = "Priya M. started following you",
                subtitleText = "From AC maintenance post",
                timeAgo = "2m",
                targetId = "demo-priya-m",
            ),
            CreatorActivityItem(
                id = "act-2",
                type = CreatorActivityType.DirectMessage,
                titleText = "New inquiry from Arun K.",
                subtitleText = "“Need AC service for 2 units...”",
                timeAgo = "8m",
                targetId = "demo-conv-arun-k",
            ),
            CreatorActivityItem(
                id = "act-3",
                type = CreatorActivityType.PostLike,
                titleText = "Your post got 48 new likes",
                subtitleText = "Post: Wiring safety tips",
                timeAgo = "14m",
                targetId = "demo-post-wiring-1",
            ),
            CreatorActivityItem(
                id = "act-4",
                type = CreatorActivityType.PostComment,
                titleText = "Karthik S. commented on your post",
                subtitleText = "“Very helpful tips for home electricals!”",
                timeAgo = "32m",
                targetId = "demo-post-wiring-1",
            ),
        )

        return CreatorDashboardSample(heroKpi, metrics, aiInsight, milestone, activities)
    }

    private fun getHeroKpi(range: CreatorTimeRange): HeroKpiSummary = when (range) {
        CreatorTimeRange.Today -> HeroKpiSummary(
            reachValue = "1,240",
            reachDelta = "+14.2%",
            engagementRate = "5.2%",
            sparklinePoints = listOf(120f, 180f, 260f, 310f, 490f, 620f, 940f, 1240f),
            envelopeLowerBounds = listOf(80f, 120f, 190f, 240f, 350f, 480f, 700f, 950f),
            envelopeUpperBounds = listOf(150f, 220f, 320f, 380f, 560f, 750f, 1100f, 1400f),
        )

        CreatorTimeRange.ThisWeek -> HeroKpiSummary(
            reachValue = "18.4k",
            reachDelta = "+8.3%",
            engagementRate = "4.8%",
            sparklinePoints = listOf(2100f, 2400f, 2200f, 2800f, 3100f, 3600f, 4200f),
            envelopeLowerBounds = listOf(1800f, 1900f, 1700f, 2100f, 2300f, 2700f, 3200f),
            envelopeUpperBounds = listOf(2500f, 2700f, 2600f, 3200f, 3700f, 4200f, 4800f),
        )

        CreatorTimeRange.ThisMonth -> HeroKpiSummary(
            reachValue = "74.8k",
            reachDelta = "+16.5%",
            engagementRate = "4.6%",
            sparklinePoints = listOf(15_000f, 19_000f, 22_000f, 28_000f, 35_000f, 42_000f, 51_000f, 62_000f, 74_800f),
            envelopeLowerBounds = listOf(12_000f, 15_000f, 18_000f, 22_000f, 27_000f, 33_000f, 40_000f, 49_000f, 58_000f),
            envelopeUpperBounds = listOf(18_000f, 23_000f, 27_000f, 34_000f, 41_000f, 50_000f, 60_000f, 72_000f, 85_000f),
        )

        CreatorTimeRange.Last90Days -> HeroKpiSummary(
            reachValue = "210.5k",
            reachDelta = "+24.0%",
            engagementRate = "4.5%",
            sparklinePoints = listOf(30_000f, 45_000f, 60_000f, 82_000f, 105_000f, 130_000f, 160_000f, 185_000f, 210_500f),
            envelopeLowerBounds = listOf(24_000f, 36_000f, 48_000f, 65_000f, 82_000f, 102_000f, 125_000f, 145_000f, 168_000f),
            envelopeUpperBounds = listOf(38_000f, 55_000f, 74_000f, 98_000f, 125_000f, 155_000f, 190_000f, 220_000f, 250_000f),
        )
    }

    private fun getCategoryMetrics(range: CreatorTimeRange): CreatorCategoryMetrics = when (range) {
        CreatorTimeRange.Today -> CreatorCategoryMetrics(
            discovery = listOf(
                CreatorMetricItem("views", R.string.creator_metric_views, "1.2k", "+12%", MetricTrend.Positive, Icons.Default.Visibility),
                CreatorMetricItem("clicks", R.string.creator_metric_link_clicks, "42", "+8%", MetricTrend.Positive, Icons.Default.Link),
                CreatorMetricItem("shares", R.string.creator_metric_shares, "18", "+5%", MetricTrend.Positive, Icons.Default.Share),
            ),
            community = listOf(
                CreatorMetricItem("likes", R.string.creator_metric_likes, "340", "+15%", MetricTrend.Positive, Icons.Default.Favorite),
                CreatorMetricItem(
                    "comments",
                    R.string.creator_metric_comments,
                    "48",
                    "+9%",
                    MetricTrend.Positive,
                    Icons.Default.ChatBubbleOutline,
                ),
                CreatorMetricItem("dms", R.string.creator_metric_dms, "12", "+3%", MetricTrend.Positive, Icons.Default.MailOutline),
                CreatorMetricItem("saves", R.string.creator_metric_saves, "35", "+18%", MetricTrend.Positive, Icons.Default.BookmarkBorder),
                CreatorMetricItem(
                    "followers",
                    R.string.creator_metric_new_followers,
                    "+14",
                    "+7%",
                    MetricTrend.Positive,
                    Icons.Default.PersonAdd,
                ),
            ),
        )

        CreatorTimeRange.ThisWeek -> CreatorCategoryMetrics(
            discovery = listOf(
                CreatorMetricItem("views", R.string.creator_metric_views, "18.4k", "+8.3%", MetricTrend.Positive, Icons.Default.Visibility),
                CreatorMetricItem("clicks", R.string.creator_metric_link_clicks, "384", "+4.2%", MetricTrend.Positive, Icons.Default.Link),
                CreatorMetricItem("shares", R.string.creator_metric_shares, "720", "+11.0%", MetricTrend.Positive, Icons.Default.Share),
            ),
            community = listOf(
                CreatorMetricItem("likes", R.string.creator_metric_likes, "9.2k", "+9.4%", MetricTrend.Positive, Icons.Default.Favorite),
                CreatorMetricItem(
                    "comments",
                    R.string.creator_metric_comments,
                    "1.4k",
                    "+6.1%",
                    MetricTrend.Positive,
                    Icons.Default.ChatBubbleOutline,
                ),
                CreatorMetricItem("dms", R.string.creator_metric_dms, "148", "+12.5%", MetricTrend.Positive, Icons.Default.MailOutline),
                CreatorMetricItem(
                    "saves",
                    R.string.creator_metric_saves,
                    "1.1k",
                    "+14.3%",
                    MetricTrend.Positive,
                    Icons.Default.BookmarkBorder,
                ),
                CreatorMetricItem(
                    "followers",
                    R.string.creator_metric_new_followers,
                    "+210",
                    "+8.3%",
                    MetricTrend.Positive,
                    Icons.Default.PersonAdd,
                ),
            ),
        )

        CreatorTimeRange.ThisMonth -> CreatorCategoryMetrics(
            discovery = listOf(
                CreatorMetricItem(
                    "views",
                    R.string.creator_metric_views,
                    "74.8k",
                    "+16.5%",
                    MetricTrend.Positive,
                    Icons.Default.Visibility,
                ),
                CreatorMetricItem(
                    "clicks",
                    R.string.creator_metric_link_clicks,
                    "1.5k",
                    "+10.2%",
                    MetricTrend.Positive,
                    Icons.Default.Link,
                ),
                CreatorMetricItem("shares", R.string.creator_metric_shares, "2.8k", "+18.0%", MetricTrend.Positive, Icons.Default.Share),
            ),
            community = listOf(
                CreatorMetricItem("likes", R.string.creator_metric_likes, "36.5k", "+14.8%", MetricTrend.Positive, Icons.Default.Favorite),
                CreatorMetricItem(
                    "comments",
                    R.string.creator_metric_comments,
                    "5.2k",
                    "+11.4%",
                    MetricTrend.Positive,
                    Icons.Default.ChatBubbleOutline,
                ),
                CreatorMetricItem("dms", R.string.creator_metric_dms, "580", "+15.0%", MetricTrend.Positive, Icons.Default.MailOutline),
                CreatorMetricItem(
                    "saves",
                    R.string.creator_metric_saves,
                    "4.3k",
                    "+21.2%",
                    MetricTrend.Positive,
                    Icons.Default.BookmarkBorder,
                ),
                CreatorMetricItem(
                    "followers",
                    R.string.creator_metric_new_followers,
                    "+840",
                    "+12.4%",
                    MetricTrend.Positive,
                    Icons.Default.PersonAdd,
                ),
            ),
        )

        CreatorTimeRange.Last90Days -> CreatorCategoryMetrics(
            discovery = listOf(
                CreatorMetricItem(
                    "views",
                    R.string.creator_metric_views,
                    "210.5k",
                    "+24.0%",
                    MetricTrend.Positive,
                    Icons.Default.Visibility,
                ),
                CreatorMetricItem(
                    "clicks",
                    R.string.creator_metric_link_clicks,
                    "4.2k",
                    "+18.6%",
                    MetricTrend.Positive,
                    Icons.Default.Link,
                ),
                CreatorMetricItem("shares", R.string.creator_metric_shares, "8.1k", "+26.5%", MetricTrend.Positive, Icons.Default.Share),
            ),
            community = listOf(
                CreatorMetricItem("likes", R.string.creator_metric_likes, "98.0k", "+22.1%", MetricTrend.Positive, Icons.Default.Favorite),
                CreatorMetricItem(
                    "comments",
                    R.string.creator_metric_comments,
                    "14.8k",
                    "+17.9%",
                    MetricTrend.Positive,
                    Icons.Default.ChatBubbleOutline,
                ),
                CreatorMetricItem("dms", R.string.creator_metric_dms, "1.6k", "+19.4%", MetricTrend.Positive, Icons.Default.MailOutline),
                CreatorMetricItem(
                    "saves",
                    R.string.creator_metric_saves,
                    "12.4k",
                    "+28.0%",
                    MetricTrend.Positive,
                    Icons.Default.BookmarkBorder,
                ),
                CreatorMetricItem(
                    "followers",
                    R.string.creator_metric_new_followers,
                    "+2.4k",
                    "+16.8%",
                    MetricTrend.Positive,
                    Icons.Default.PersonAdd,
                ),
            ),
        )
    }
}
