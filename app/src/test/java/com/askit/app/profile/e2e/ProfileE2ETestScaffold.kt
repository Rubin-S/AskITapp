package com.askit.app.profile.e2e

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.askit.designsystem.profile.ProfileActionConfig
import com.askit.designsystem.profile.ProfileActionRow
import com.askit.designsystem.profile.ProfileAvatar
import com.askit.designsystem.profile.ProfileCover
import com.askit.designsystem.profile.ProfileIdentityBlock
import com.askit.designsystem.profile.ProfileMetricItem
import com.askit.designsystem.profile.ProfileMetricsBar
import com.askit.designsystem.profile.ProfilePreviewBanner
import com.askit.designsystem.profile.ProfileSectionTabs
import com.askit.designsystem.profile.ProfileTabSpec
import com.askit.designsystem.R as DsR

/**
 * UI state model for Universal Profile E2E opaque-box testing.
 * Encapsulates dual-identity community model (Form A vs Form B)
 * and view modes (Owner, Visitor, Public Preview).
 */
data class ProfileE2EState(
    val isOwner: Boolean = true,
    val isPublicPreview: Boolean = false,
    val isProvider: Boolean = false,
    val displayName: String = "Alex Chen",
    val localityLine: String = "T. Nagar, Chennai · Joined 2024",
    val tradeHeadline: String? = null,
    val isVerified: Boolean = false,
    val bio: String = "",
    val avatarUrl: String? = null,
    val followerCount: Int = 120,
    val followingCount: Int = 45,
    val activityCount: Int = 12,
    val rating: Double? = 4.9,
    val completedJobsCount: Int? = 48,
    val isFollowing: Boolean = false,
    val selectedTabIndex: Int = 0,
)

/**
 * Action callbacks for ProfileE2ETestScaffold.
 */
data class ProfileE2EActions(
    val onEditProfile: () -> Unit = {},
    val onShare: () -> Unit = {},
    val onViewAsPublic: () -> Unit = {},
    val onExitPreview: () -> Unit = {},
    val onMessage: () -> Unit = {},
    val onToggleFollow: () -> Unit = {},
    val onRequestService: (() -> Unit)? = null,
    val onCompleteFormB: () -> Unit = {},
    val onTabSelected: (Int) -> Unit = {},
    val onCameraClick: () -> Unit = {},
)

/**
 * Fallback / test-level motivational banner for Form A community members.
 */
@Composable
fun TestCompleteFormBBanner(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("profile_complete_form_b_banner"),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Offer services in your neighborhood",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Text(
                    text = "Complete Form B to start getting client requests",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                )
            }
            Button(
                onClick = onClick,
                modifier = Modifier.testTag("profile_form_b_cta"),
            ) {
                Text("Complete Form B")
            }
        }
    }
}

/**
 * Universal Profile Test Scaffold implementing the universal spatial layout and
 * dual-identity behavior defined in PROJECT.md and TEST_INFRA.md:
 *
 * Universal Spatial Ordering:
 * 1. Cover (120dp)
 * 2. Avatar (96dp, 4dp border, camera affordance in owner mode)
 * 3. Identity Block (Name, Verified Badge, Trade Headline, Locality)
 * 4. Metrics Bar (Flexible ProfileMetricsBar: 3 or 4 metrics)
 * 5. Primary Action Row (Contextual: Owner vs Visitor)
 * 6. Optional Form B Motivational Banner (Form A Owner only)
 * 7. ProfileSectionTabs (Dynamic tabs based on Form A / Form B)
 * 8. Content Section (Pane matching active tab)
 * 9. Floating Exit Preview Banner (When isPublicPreview == true)
 */
@Composable
fun ProfileE2ETestScaffold(
    state: ProfileE2EState,
    modifier: Modifier = Modifier,
    actions: ProfileE2EActions = ProfileE2EActions(),
) {
    // Determine dynamic metrics based on identity model
    val metrics: List<ProfileMetricItem> = if (state.isProvider) {
        listOf(
            ProfileMetricItem(
                id = "rating",
                value = if (state.rating != null) "★ %.1f".format(state.rating) else "★ —",
                label = "Rating",
            ),
            ProfileMetricItem(
                id = "jobs",
                value = state.completedJobsCount?.toString() ?: "0",
                label = "Completed Jobs",
            ),
            ProfileMetricItem(
                id = "followers",
                value = state.followerCount.toString(),
                label = "Followers",
            ),
            ProfileMetricItem(
                id = "following",
                value = state.followingCount.toString(),
                label = "Following",
            ),
        )
    } else {
        listOf(
            ProfileMetricItem(
                id = "activity",
                value = state.activityCount.toString(),
                label = "Activity",
            ),
            ProfileMetricItem(
                id = "followers",
                value = state.followerCount.toString(),
                label = "Followers",
            ),
            ProfileMetricItem(
                id = "following",
                value = state.followingCount.toString(),
                label = "Following",
            ),
        )
    }

    // Determine dynamic tabs based on identity model
    val tabs: List<ProfileTabSpec> = if (state.isProvider) {
        listOf(
            ProfileTabSpec("services", "Services", DsR.drawable.ic_wrench),
            ProfileTabSpec("gallery", "Showcase", DsR.drawable.ic_photo),
            ProfileTabSpec("reviews", "Reviews", DsR.drawable.ic_star_outline),
            ProfileTabSpec("about", "About", DsR.drawable.ic_person),
        )
    } else {
        listOf(
            ProfileTabSpec("activity", "Activity", DsR.drawable.ic_work),
            ProfileTabSpec("about", "About", DsR.drawable.ic_person),
            ProfileTabSpec("reviews", "Reviews", DsR.drawable.ic_star_outline),
        )
    }

    // Contextual Action Configuration
    val actionConfig: ProfileActionConfig = if (state.isOwner && !state.isPublicPreview) {
        ProfileActionConfig.Owner(
            onEditProfile = actions.onEditProfile,
            onShare = actions.onShare,
            onViewAsPublic = actions.onViewAsPublic,
        )
    } else {
        // Visitor perspective (either actual visitor or owner viewing as public)
        ProfileActionConfig.Visitor(
            onMessage = actions.onMessage,
            isFollowing = state.isFollowing,
            onToggleFollow = actions.onToggleFollow,
            onRequestService = if (state.isProvider) {
                actions.onRequestService ?: {}
            } else {
                null // Form A visitor protection: null ensures no Request Service button is rendered
            },
        )
    }

    var currentTabIndex by remember(state.selectedTabIndex) {
        mutableIntStateOf(state.selectedTabIndex)
    }
    val activeTabIndex = currentTabIndex.coerceIn(0, tabs.lastIndex)
    val activeTabId = tabs[activeTabIndex].id

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("profile_scaffold"),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = if (state.isPublicPreview) 80.dp else 24.dp),
        ) {
            // 1 & 2: Header Cover and Overlapping Avatar
            Box(modifier = Modifier.fillMaxWidth().testTag("profile_header_box")) {
                ProfileCover(modifier = Modifier.testTag("profile_cover"))
                ProfileAvatar(
                    avatarUrl = state.avatarUrl,
                    cameraContentDescription = "Update photo",
                    onCamera = if (state.isOwner && !state.isPublicPreview) actions.onCameraClick else null,
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .offset(y = 72.dp)
                        .testTag("profile_avatar"),
                )
            }

            // Body Column below avatar offset
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 56.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // 3: Identity Block (Name, Verified Badge, Trade Headline, Locality)
                ProfileIdentityBlock(
                    displayName = state.displayName,
                    localityLine = state.localityLine,
                    tradeHeadline = if (state.isProvider) state.tradeHeadline else null,
                    isVerified = state.isVerified,
                    bio = state.bio,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )

                // 4: Flexible Metrics Bar
                ProfileMetricsBar(
                    metrics = metrics,
                    modifier = Modifier.testTag("profile_metrics_bar"),
                )

                // 5: Primary Action Row
                ProfileActionRow(
                    config = actionConfig,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )

                // 6: Optional Form B Motivational Banner (Form A Owner only, hidden in preview)
                if (state.isOwner && !state.isPublicPreview && !state.isProvider) {
                    TestCompleteFormBBanner(
                        onClick = actions.onCompleteFormB,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }

                // 7: Dynamic Profile Section Tabs
                ProfileSectionTabs(
                    tabs = tabs,
                    selectedIndex = activeTabIndex,
                    onSelect = { index ->
                        currentTabIndex = index
                        actions.onTabSelected(index)
                    },
                    modifier = Modifier.testTag("profile_tabs"),
                )

                // 8: Content Section (Pane matching active tab)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .testTag("profile_content_section"),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_content_$activeTabId"),
                    ) {
                        Text(
                            text = "Content for $activeTabId",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.testTag("profile_content_text_$activeTabId"),
                        )
                    }
                }
            }
        }

        // 9: Floating Exit Preview Banner (When isPublicPreview == true)
        if (state.isPublicPreview) {
            ProfilePreviewBanner(
                onExitPreview = actions.onExitPreview,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
            )
        }
    }
}
