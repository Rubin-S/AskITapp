package com.askit.app.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.askit.app.R
import com.askit.designsystem.navigation.AskITSectionTabs
import com.askit.designsystem.navigation.SectionTab
import com.askit.designsystem.profile.ProfileActionConfig
import com.askit.designsystem.profile.ProfileActionRow
import com.askit.designsystem.profile.ProfileAvatar
import com.askit.designsystem.profile.ProfileIdentityBlock
import com.askit.designsystem.profile.ProfileMetricsBar
import com.askit.designsystem.profile.ProfilePreviewBanner
import kotlinx.coroutines.launch

/**
 * Shared layout for every profile: header, highlights, metrics, actions, sticky section tabs and
 * the selected section. Owner-only controls appear only when their callbacks are given.
 */
@Composable
fun UniversalProfileScaffold(
    uiState: ProfileUiState,
    actionConfig: ProfileActionConfig,
    onSectionSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    people: ProfilePeopleState = ProfilePeopleState(),
    viewerAccountId: String? = null,
    authorizedPostIds: Set<String> = emptySet(),
    publicProfileUrl: String? = null,
    showTopBar: Boolean = true,
    onOpenProfile: (String) -> Unit = {},
    onFollowPerson: (String, Boolean) -> Boolean = { _, _ -> false },
    onOpenPost: (String) -> Unit = {},
    onModerate: ((Boolean) -> Boolean)? = null,
    onBack: (() -> Unit)? = null,
    onOpenSettings: (() -> Unit)? = null,
    onCameraClick: (() -> Unit)? = null,
    onSaveBanner: (suspend (String?) -> Boolean)? = null,
    highlightsContent: (@Composable () -> Unit)? = null,
    content: @Composable (sectionId: String) -> Unit,
) {
    var metricDialog by rememberSaveable(uiState.userId) { mutableStateOf<String?>(null) }
    metricDialog?.let {
        ProfileMetricSheet(it, uiState, people, onDismiss = {
            metricDialog = null
        }, onOpenProfile = onOpenProfile, onFollow = onFollowPerson)
    }
    val activeTabId = uiState.selectedSectionId.takeIf { it in uiState.tabs } ?: uiState.tabs.firstOrNull().orEmpty()
    val listState = rememberSaveable(uiState.userId, saver = LazyListState.Saver) { LazyListState() }
    val scope = rememberCoroutineScope()
    var tabHeight by rememberSaveable { mutableIntStateOf(0) }
    val density = LocalDensity.current
    Box(modifier.fillMaxSize().testTag("profile_scaffold")) {
        Column(Modifier.fillMaxSize()) {
            if (showTopBar) {
                ProfileOverflowTopBar(
                    uiState,
                    viewerAccountId,
                    publicProfileUrl,
                    onBack = onBack,
                    onSettings = onOpenSettings,
                    onModerate = onModerate,
                )
            }
            BoxWithConstraints(Modifier.weight(1f)) {
                val minimumContentHeight = (maxHeight - with(density) { tabHeight.toDp() }).coerceAtLeast(0.dp)
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = if (uiState.isPublicPreview) 80.dp else 24.dp),
                ) {
                    item(key = "identity") {
                        ProfileHeader(
                            uiState,
                            actionConfig,
                            viewerAccountId,
                            authorizedPostIds,
                            onOpenPost,
                            onCameraClick,
                            onSaveBanner,
                            highlightsContent,
                        ) {
                            metricDialog = it
                        }
                    }
                    stickyHeader(key = "sections") {
                        Surface(
                            color = MaterialTheme.colorScheme.background,
                            modifier = Modifier.fillMaxWidth().onSizeChanged { tabHeight = it.height },
                        ) {
                            AskITSectionTabs(
                                tabs = uiState.tabs.map { SectionTab(it, stringResource(tabLabel(it, uiState.isOwnerView))) },
                                selectedId = activeTabId,
                                onSelect = { id ->
                                    onSectionSelect(id)
                                    // Bring the tabs to the top only when the header has scrolled past them.
                                    if (listState.firstVisibleItemIndex >= 1) scope.launch { listState.animateScrollToItem(1) }
                                },
                                tabTagPrefix = "profile_tab_",
                                modifier = Modifier.testTag("profile_tabs"),
                            )
                        }
                    }
                    item(key = "content:$activeTabId") {
                        Box(
                            Modifier.fillMaxWidth().heightIn(min = minimumContentHeight).padding(16.dp)
                                .testTag("profile_content_section"),
                        ) {
                            Box(Modifier.fillMaxWidth().testTag("profile_content_$activeTabId")) { content(activeTabId) }
                        }
                    }
                }
            }
        }
        if (uiState.isPublicPreview && onBack != null) {
            ProfilePreviewBanner(onExitPreview = onBack, modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp))
        }
    }
}

@Composable
private fun ProfileHeader(
    uiState: ProfileUiState,
    actionConfig: ProfileActionConfig,
    viewerAccountId: String?,
    authorizedPostIds: Set<String>,
    onOpenPost: (String) -> Unit,
    onCameraClick: (() -> Unit)?,
    onSaveBanner: (suspend (String?) -> Boolean)?,
    highlightsContent: (@Composable () -> Unit)?,
    onOpenMetric: (String) -> Unit,
) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(Modifier.fillMaxWidth().testTag("profile_header_box")) {
            key(uiState.userId) { ProfileBanner(uiState.bannerUrl, onSave = onSaveBanner) }
            ProfileAvatar(
                avatarUrl = uiState.avatarUrl,
                cameraContentDescription = stringResource(R.string.profile_update_photo),
                onCamera = onCameraClick,
                modifier = Modifier.padding(start = 16.dp).offset(y = 72.dp).testTag("profile_avatar"),
            )
        }
        ProfileIdentityBlock(
            displayName = uiState.displayName,
            localityLine = listOfNotNull(
                uiState.localityLine.takeIf { it.isNotBlank() },
                uiState.joinedYear.takeIf { it.isNotBlank() }?.let { stringResource(R.string.profile_joined_year, it) },
                uiState.approximateDistanceKm?.let { stringResource(R.string.profile_approximate_distance, it) },
            ).joinToString(" · "),
            tradeHeadline = uiState.tradeHeadline,
            isVerified = uiState.isVerified,
            bio = uiState.bio,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 44.dp),
        )
        highlightsContent?.invoke() ?: ProfileHighlights(
            ownerId = uiState.userId,
            viewerId = viewerAccountId,
            collections = uiState.highlights,
            stories = emptyList(),
            canManage = false,
            authorizedPostIds = authorizedPostIds,
            onOpenPost = onOpenPost,
        )
        ProfileMetricsBar(
            metrics = uiState.metrics.map { metric ->
                metric.copy(
                    label = stringResource(metricLabel(metric.id)),
                    onClick = if (metric.id == ProfileMetric.COMPLETED) null else ({ onOpenMetric(metric.id) }),
                )
            },
            modifier = Modifier.testTag("profile_metrics_bar"),
        )
        ProfileActionRow(config = actionConfig, modifier = Modifier.padding(horizontal = 16.dp))
    }
}

/** Only these four ids exist; the scaffold opens sheets for rating, followers and following. */
internal fun metricLabel(id: String) = when (id) {
    ProfileMetric.RATING -> R.string.job_review_rating
    ProfileMetric.COMPLETED -> R.string.profile_completed
    ProfileMetric.FOLLOWERS -> R.string.profile_followers
    else -> R.string.profile_following
}

/** Owners read "My services"; visitors and "View as" read the neutral "Services". */
private fun tabLabel(id: String, owner: Boolean) = when (id) {
    ProfileTab.ABOUT -> R.string.profile_about
    ProfileTab.SHOWCASE -> R.string.profile_showcase
    ProfileTab.SERVICES -> if (owner) R.string.profile_my_services else R.string.profile_services
    ProfileTab.POSTS -> if (owner) R.string.profile_my_post else R.string.profile_posts
    else -> if (owner) R.string.profile_post_your_task else R.string.profile_tasks
}
