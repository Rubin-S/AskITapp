package com.askit.app.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.askit.app.R
import com.askit.designsystem.profile.ProfileActionConfig
import com.askit.designsystem.profile.ProfileActionRow
import com.askit.designsystem.profile.ProfileAvatar
import com.askit.designsystem.profile.ProfileIdentityBlock
import com.askit.designsystem.profile.ProfileMetricsBar
import com.askit.designsystem.profile.ProfilePreviewBanner
import com.askit.designsystem.navigation.AskITSectionTabs

@Composable
fun UniversalProfileScaffold(
    uiState: ProfileUiState,
    modifier: Modifier = Modifier,
    isVisitor: Boolean = false,
    people: ProfilePeopleState = ProfilePeopleState(),
    onOpenProfile: (String) -> Unit = {},
    onFollowPerson: (String, Boolean) -> Boolean = { _, _ -> false },
    viewerAccountId: String? = null,
    authorizedPostIds: Set<String> = emptySet(),
    onOpenPost: (String) -> Unit = {},
    actionConfig: ProfileActionConfig,
    publicProfileUrl: String? = null,
    onModerate: (Boolean) -> Boolean = { false },
    showTopBar: Boolean = true,
    onBack: (() -> Unit)? = null,
    onExitPreview: () -> Unit = {},
    onSectionSelected: (String) -> Unit,
    onCameraClick: () -> Unit = {},
    onSaveBanner: (String?) -> Boolean = { false },
    onOpenSettings: () -> Unit = {},
    highlightsContent: (@Composable () -> Unit)? = null,
    content: @Composable (sectionId: String) -> Unit,
) {
    var metricDialog by rememberSaveable(uiState.userId) { mutableStateOf<String?>(null) }
    metricDialog?.let { ProfileMetricSheet(it, uiState, people, onDismiss = { metricDialog = null },
        onOpenProfile = onOpenProfile, onFollow = onFollowPerson) }
    val activeTabId = uiState.tabs.firstOrNull { it.id == uiState.selectedSectionId }?.id ?: uiState.tabs.firstOrNull()?.id.orEmpty()
    val listState = rememberSaveable(uiState.userId, saver = LazyListState.Saver) { LazyListState() }
    val scope = rememberCoroutineScope()
    var tabHeight by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current
    Box(modifier.fillMaxSize().testTag("profile_scaffold")) {
        Column(Modifier.fillMaxSize()) {
            if (showTopBar) ProfileOverflowTopBar(uiState, viewerAccountId, publicProfileUrl,
                onBack = if (isVisitor || uiState.isPublicPreview) (onBack ?: onExitPreview) else null,
                onSettings = onOpenSettings, onModerate = onModerate)
            BoxWithConstraints(Modifier.weight(1f)) {
                val minimumContentHeight = (maxHeight - with(density) { tabHeight.toDp() }).coerceAtLeast(0.dp)
                LazyColumn(state = listState, modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = if (uiState.isPublicPreview) 80.dp else 24.dp)) {
                    item(key = "identity") {
                        Column(Modifier.fillMaxWidth()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("profile_header_box"),
                            ) {
                                androidx.compose.runtime.key(uiState.userId) {
                                    ProfileBanner(uiState.bannerUrl,
                                        canEdit = uiState.canEditMedia && !isVisitor,
                                        onSave = onSaveBanner)
                                }
                                ProfileAvatar(
                                    avatarUrl = uiState.avatarUrl,
                                    cameraContentDescription = stringResource(R.string.profile_update_photo),
                                    onCamera = if (!isVisitor && !uiState.isPublicPreview) onCameraClick else null,
                                    modifier = Modifier
                                        .padding(start = 16.dp)
                                        .offset(y = 72.dp)
                                        .testTag("profile_avatar"),
                                )
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 56.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                ProfileIdentityBlock(
                                    displayName = uiState.displayName,
                                    localityLine = listOfNotNull(
                                        uiState.localityLine.takeIf { it.isNotBlank() },
                                        uiState.joinedYear.takeIf { it.isNotBlank() }?.let {
                                            stringResource(R.string.profile_joined_year, it)
                                        },
                                    ).joinToString(" · "),
                                    tradeHeadline = uiState.tradeHeadline,
                                    isVerified = uiState.isVerified,
                                    bio = uiState.bio,
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                )

                                if (highlightsContent != null) highlightsContent() else ProfileHighlights(
                                    uiState.userId, viewerAccountId, uiState.highlights, emptyList(), canManage = false,
                                    authorizedPostIds = authorizedPostIds, onSave = { false }, onDelete = { false }, onOpenPost = onOpenPost)
                                uiState.approximateDistanceKm?.let { distance ->
                                    Text(stringResource(R.string.profile_approximate_distance, distance), Modifier.padding(horizontal = 16.dp))
                                }
                                ProfileMetricsBar(
                                    metrics = uiState.metrics.map { metric ->
                                        val label = when (metric.id) {
                                            "rating" -> stringResource(R.string.job_review_rating)
                                            "completed_jobs" -> stringResource(R.string.profile_completed)
                                            "followers" -> stringResource(R.string.profile_followers)
                                            "following" -> stringResource(R.string.profile_following)
                                            "activity" -> stringResource(R.string.story_activity)
                                            else -> metric.label
                                        }
                                        metric.copy(label = label, onClick = if (metric.id in setOf("rating", "followers", "following")) ({ metricDialog = metric.id }) else metric.onClick)
                                    },
                                    modifier = Modifier.testTag("profile_metrics_bar"),
                                )

                                ProfileActionRow(
                                    config = actionConfig,
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                )

                            }
                        }
                    }
                    stickyHeader(key = "sections") {
                        Surface(color = MaterialTheme.colorScheme.background,
                            modifier = Modifier.fillMaxWidth().onSizeChanged { tabHeight = it.height }) {
                            AskITSectionTabs(
                                tabs = uiState.tabs.map { tab ->
                                    val label = when (tab.id) {
                                        "services" -> stringResource(R.string.profile_my_services)
                                        "posts" -> stringResource(R.string.profile_my_post)
                                        "showcase" -> stringResource(R.string.profile_showcase)
                                        "tasks" -> stringResource(R.string.profile_post_your_task)
                                        "about" -> stringResource(R.string.profile_about)
                                        else -> tab.label
                                    }
                                    tab.copy(label = label)
                                },
                                selectedId = activeTabId,
                                onSelect = { id ->
                                    onSectionSelected(id)
                                    scope.launch { listState.animateScrollToItem(1) }
                                },
                                tabTagPrefix = "profile_tab_",
                                modifier = Modifier.testTag("profile_tabs"),
                            )
                        }
                    }
                    item(key = "content:$activeTabId") {
                        Box(Modifier.fillMaxWidth().heightIn(min = minimumContentHeight)
                            .padding(16.dp).testTag("profile_content_section")) {
                            Box(Modifier.fillMaxWidth().testTag("profile_content_$activeTabId")) {
                                content(activeTabId)
                            }
                        }
                    }
                }
            }
        }
        if (uiState.isPublicPreview) ProfilePreviewBanner(onExitPreview = onExitPreview,
            modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp))
    }
}
