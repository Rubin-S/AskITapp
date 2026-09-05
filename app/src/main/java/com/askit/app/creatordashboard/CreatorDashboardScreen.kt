package com.askit.app.creatordashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.askit.app.R
import com.askit.app.creatordashboard.components.CreatorAiInsightCard
import com.askit.app.creatordashboard.components.CreatorMetricCard
import com.askit.app.creatordashboard.components.CreatorTrendGraph
import com.askit.designsystem.people.AskITAvatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatorDashboardRoute(
    viewModel: CreatorDashboardViewModel,
    onBack: () -> Unit,
    onCreatePost: (String?) -> Unit,
    onOpenChat: (String) -> Unit,
    onOpenPostDetail: (String) -> Unit,
    onOpenProfile: (String) -> Unit,
    modifier: Modifier = Modifier,
    lazyListState: androidx.compose.foundation.lazy.LazyListState = androidx.compose.foundation.lazy.rememberLazyListState(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CreatorDashboardScreen(
        uiState = uiState,
        onBack = onBack,
        onSelectTimeRange = viewModel::selectTimeRange,
        onDismissAiInsight = viewModel::dismissAiInsight,
        onCreatePost = onCreatePost,
        onOpenChat = onOpenChat,
        onOpenPostDetail = onOpenPostDetail,
        onOpenProfile = onOpenProfile,
        lazyListState = lazyListState,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatorDashboardScreen(
    uiState: CreatorDashboardUiState,
    onBack: () -> Unit,
    onSelectTimeRange: (CreatorTimeRange) -> Unit,
    onDismissAiInsight: () -> Unit,
    onCreatePost: (String?) -> Unit,
    onOpenChat: (String) -> Unit,
    onOpenPostDetail: (String) -> Unit,
    onOpenProfile: (String) -> Unit,
    modifier: Modifier = Modifier,
    lazyListState: androidx.compose.foundation.lazy.LazyListState = androidx.compose.foundation.lazy.rememberLazyListState(),
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.creator_dashboard_title),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.creator_dashboard_back),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        },
    ) { padding ->
        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            // 1. Identity Spotlight
            item(key = "identity_header") {
                CreatorIdentityHeader(
                    displayName = uiState.displayName,
                    username = uiState.username,
                    avatarUrl = uiState.avatarUrl,
                    isServiceProvider = uiState.isServiceProvider,
                    professionOrRole = uiState.professionOrRole,
                )
            }

            // 2. Time Range Selector
            item(key = "time_range_selector") {
                val ranges = CreatorTimeRange.entries
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    ranges.forEachIndexed { index, range ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = ranges.size),
                            onClick = { onSelectTimeRange(range) },
                            selected = uiState.selectedTimeRange == range,
                            icon = {},
                            colors = SegmentedButtonDefaults.colors(
                                activeContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                activeContentColor = MaterialTheme.colorScheme.onSurface,
                                inactiveContainerColor = MaterialTheme.colorScheme.surface,
                                inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                        ) {
                            Text(
                                text = stringResource(range.labelRes),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (uiState.selectedTimeRange == range) FontWeight.Bold else FontWeight.Medium,
                                ),
                                maxLines = 1,
                            )
                        }
                    }
                }
            }

            // 3. Hero KPI Spotlight Card with Trend Graph
            item(key = "hero_kpi_card") {
                HeroKpiCard(
                    hero = uiState.heroKpi,
                )
            }

            // 4. Discovery & Reach Section
            item(key = "discovery_section_title") {
                SectionHeader(title = stringResource(R.string.creator_section_discovery))
            }

            item(key = "discovery_metrics_grid") {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(uiState.categoryMetrics.discovery) { metric ->
                        CreatorMetricCard(
                            item = metric,
                            modifier = Modifier.width(156.dp),
                        )
                    }
                }
            }

            // 5. Community & Interactions Section
            item(key = "community_section_title") {
                SectionHeader(title = stringResource(R.string.creator_section_community))
            }

            item(key = "community_metrics_grid_1") {
                val items = uiState.categoryMetrics.community
                val firstRow = items.take(2)
                if (firstRow.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        firstRow.forEach { metric ->
                            Box(modifier = Modifier.weight(1f)) {
                                CreatorMetricCard(item = metric)
                            }
                        }
                    }
                }
            }

            item(key = "community_metrics_grid_2") {
                val items = uiState.categoryMetrics.community
                val secondRow = items.drop(2)
                if (secondRow.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        secondRow.forEach { metric ->
                            Box(modifier = Modifier.weight(1f)) {
                                CreatorMetricCard(item = metric)
                            }
                        }
                    }
                }
            }

            // 6. Actionable ASKit AI Insight
            uiState.aiInsight?.let { insight ->
                item(key = "ai_insight_card") {
                    CreatorAiInsightCard(
                        insight = insight,
                        onActionClick = { tag -> onCreatePost(tag) },
                        onDismiss = onDismissAiInsight,
                    )
                }
            }

            // 7. Audience Growth Milestone
            item(key = "milestone_card") {
                MilestoneCard(
                    milestone = uiState.milestone,
                )
            }

            // 8. Recent Activity Feed
            item(key = "recent_activity_title") {
                SectionHeader(title = stringResource(R.string.creator_recent_activity_title))
            }

            items(
                items = uiState.recentActivities,
                key = { it.id },
            ) { activity ->
                ActivityRow(
                    activity = activity,
                    onClick = {
                        when (activity.type) {
                            CreatorActivityType.DirectMessage -> onOpenChat(activity.targetId)
                            CreatorActivityType.PostLike,
                            CreatorActivityType.PostComment -> onOpenPostDetail(activity.targetId)
                            CreatorActivityType.NewFollower -> onOpenProfile(activity.targetId)
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun CreatorIdentityHeader(
    displayName: String,
    username: String,
    avatarUrl: String?,
    isServiceProvider: Boolean,
    professionOrRole: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            AskITAvatar(
                avatarUrl = avatarUrl,
                avatarSize = 56.dp,
                fallbackIconSize = 28.dp,
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                    ) {
                        Text(
                            text = stringResource(
                                if (isServiceProvider) R.string.creator_badge_verified_pro
                                else R.string.creator_badge_community_creator,
                            ),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        )
                    }
                }

                Text(
                    text = "@$username · $professionOrRole",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(MaterialTheme.colorScheme.secondary, CircleShape),
                    )
                    Text(
                        text = stringResource(R.string.creator_status_active_growing),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroKpiCard(
    hero: HeroKpiSummary,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                // Primary KPI: Total Reach
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = stringResource(R.string.creator_hero_total_reach),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = hero.reachValue,
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowDropUp,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.secondary,
                            )
                            Text(
                                text = hero.reachDelta,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.secondary,
                            )
                        }
                    }
                }

                // Secondary KPI: Engagement Rate
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = stringResource(R.string.creator_hero_engagement_rate),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = hero.engagementRate,
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.secondary,
                    )
                    Text(
                        text = stringResource(R.string.creator_vs_previous_period),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Dynamic Trend Sparkline with Envelope
            CreatorTrendGraph(
                points = hero.sparklinePoints,
                lowerBounds = hero.envelopeLowerBounds,
                upperBounds = hero.envelopeUpperBounds,
                height = 70.dp,
            )
        }
    }
}

@Composable
private fun MilestoneCard(
    milestone: FollowerMilestone,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.creator_milestone_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(R.string.creator_milestone_target, milestone.targetFollowers),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "%,d".format(milestone.currentFollowers),
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(
                        R.string.creator_milestone_growth_pace,
                        milestone.weeklyGrowthText,
                        milestone.momGrowthText,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(bottom = 2.dp),
                )
            }

            LinearProgressIndicator(
                progress = { milestone.progressPercent },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.secondary,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            )

            Text(
                text = stringResource(R.string.creator_milestone_remaining, milestone.remainingCount),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ActivityRow(
    activity: CreatorActivityItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.size(36.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    val (icon, tint) = when (activity.type) {
                        CreatorActivityType.NewFollower -> Icons.Default.Person to MaterialTheme.colorScheme.primary
                        CreatorActivityType.DirectMessage -> Icons.AutoMirrored.Filled.Chat to MaterialTheme.colorScheme.secondary
                        CreatorActivityType.PostLike -> Icons.Default.Favorite to MaterialTheme.colorScheme.error
                        CreatorActivityType.PostComment -> Icons.Default.Bookmark to MaterialTheme.colorScheme.tertiary
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = tint,
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = activity.titleText,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${activity.subtitleText} · ${activity.timeAgo}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.outline,
            )
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier.semantics { heading() },
    )
}
