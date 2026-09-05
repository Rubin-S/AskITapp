package com.askit.app.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.askit.app.jobs.JobStatus
import com.askit.app.session.ServiceListing
import com.askit.app.session.SessionProfile
import com.askit.designsystem.profile.CompleteFormBBanner
import com.askit.designsystem.profile.ProfileActionConfig
import com.askit.designsystem.profile.ProfileActionRow
import com.askit.designsystem.profile.ProfileAvatar
import com.askit.designsystem.profile.ProfileCover
import com.askit.designsystem.profile.ProfileIdentityBlock
import com.askit.designsystem.profile.ProfileMetricsBar
import com.askit.designsystem.profile.ProfilePreviewBanner
import com.askit.designsystem.profile.ProfileSectionTabs
import com.askit.designsystem.profile.ProfileTopBar
import com.askit.designsystem.profile.ProviderDashboardBanner
import com.askit.designsystem.profile.YourServiceCard

/**
 * Universal Profile Scaffold implementing the unified layout structure and spatial hierarchy
 * shared across owner view (ProfileRoute) and visitor view (UserProfileScreen):
 *
 * Universal Spatial Ordering:
 * 1. ProfileTopBar: Centered/left username, back button (in visitor/preview mode), overflow options.
 * 2. ProfileCover (120dp).
 * 3. ProfileAvatar (96dp, 4dp border, camera affordance ONLY in owner edit mode).
 * 4. ProfileIdentityBlock (Display name, verified badge, trade headline, locality line).
 * 5. ProfileMetricsBar (Dynamic 3 metrics for Form A, 4 metrics for Form B).
 * 6. ProfileActionRow (Owner: Edit/Share/View as Public vs Visitor: Message/Follow/Request Service).
 * 7. Motivational Banner: CompleteFormBBanner (Form A owner only, hidden in preview).
 * 8. ProfileSectionTabs (Dynamic tabs based on Form A / Form B).
 * 9. Content Section: Active pane based on selected tab.
 * 10. Floating ProfilePreviewBanner: Floating surface banner when isPublicPreview == true.
 */
@Composable
fun UniversalProfileScaffold(
    uiState: ProfileUiState,
    modifier: Modifier = Modifier,
    isVisitor: Boolean = false,
    actionConfig: ProfileActionConfig? = null,
    showTopBar: Boolean = true,
    onBack: (() -> Unit)? = null,
    onEditProfile: () -> Unit = {},
    onShare: () -> Unit = {},
    onViewAsPublic: () -> Unit = {},
    onExitPreview: () -> Unit = {},
    onMessage: () -> Unit = {},
    onToggleFollow: () -> Unit = {},
    onRequestService: (() -> Unit)? = null,
    onCompleteFormB: () -> Unit = {},
    onOpenProviderDashboard: () -> Unit = {},
    onTabSelected: (Int) -> Unit = {},
    onCameraClick: () -> Unit = {},
    onCopyUsername: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onEditListing: () -> Unit = {},
    onUploadWork: () -> Unit = {},
    onOpenJob: (String) -> Unit = {},
    onViewAllJobs: () -> Unit = {},
    onAddLicense: (String) -> Unit = {},
    onSaveAbout: (String) -> Unit = {},
    onSaveLookingFor: (List<String>) -> Unit = {},
    onSaveSkills: (List<String>) -> Unit = {},
    content: (@Composable (tabId: String) -> Unit)? = null,
) {
    // Resolve contextual action configuration
    val effectiveActionConfig: ProfileActionConfig = actionConfig ?: if (!isVisitor && !uiState.isPublicPreview) {
        ProfileActionConfig.Owner(
            onEditProfile = onEditProfile,
            onShare = onShare,
            onViewAsPublic = onViewAsPublic,
        )
    } else {
        ProfileActionConfig.Visitor(
            onMessage = onMessage,
            isFollowing = uiState.isFollowing,
            onToggleFollow = onToggleFollow,
            onRequestService = if (uiState.isProvider) onRequestService else null,
        )
    }

    var currentTabIndex by remember(uiState.selectedTabIndex) {
        mutableIntStateOf(uiState.selectedTabIndex)
    }

    val activeTabIndex = if (uiState.tabs.isNotEmpty()) {
        currentTabIndex.coerceIn(0, uiState.tabs.lastIndex)
    } else {
        0
    }

    val activeTabId = if (uiState.tabs.isNotEmpty()) {
        uiState.tabs[activeTabIndex].id
    } else {
        ""
    }

    val sessionProfile = remember(uiState) {
        SessionProfile(
            displayName = uiState.displayName,
            username = uiState.username,
            bio = uiState.bio,
            about = uiState.bio,
            avatarUrl = uiState.avatarUrl,
            hasListedService = uiState.isProvider,
            listing = uiState.listing,
            gallery = uiState.gallery,
            reviews = uiState.reviews,
            lookingFor = uiState.lookingFor,
            skills = uiState.skills,
            languages = uiState.languages,
            licenses = uiState.licenses,
            savedProfessionals = uiState.savedProfessionals,
            followerCount = uiState.metrics.find { it.id == "followers" }?.value?.toIntOrNull() ?: 0,
            followingCount = uiState.metrics.find { it.id == "following" }?.value?.toIntOrNull() ?: 0,
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("profile_scaffold"),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = if (uiState.isPublicPreview) 80.dp else 24.dp),
        ) {
            // 1: Optional ProfileTopBar
            if (showTopBar) {
                ProfileTopBar(
                    username = if (uiState.username.startsWith("@")) uiState.username else "@${uiState.username}",
                    moreContentDescription = "More options",
                    shareLabel = "Share",
                    copyLabel = "Copy username",
                    onShare = onShare,
                    onCopyUsername = onCopyUsername,
                    onBack = if (isVisitor || uiState.isPublicPreview) (onBack ?: onExitPreview) else null,
                    backContentDescription = "Back",
                    settingsLabel = "Settings",
                    onSettingsClick = if (!isVisitor && !uiState.isPublicPreview) onOpenSettings else null,
                )
            }

            // 2 & 3: Header Cover and Overlapping Avatar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_header_box"),
            ) {
                ProfileCover(modifier = Modifier.testTag("profile_cover"))
                ProfileAvatar(
                    avatarUrl = uiState.avatarUrl,
                    cameraContentDescription = "Update photo",
                    onCamera = if (!isVisitor && !uiState.isPublicPreview) onCameraClick else null,
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
                // 4: ProfileIdentityBlock
                ProfileIdentityBlock(
                    displayName = uiState.displayName,
                    localityLine = uiState.localityLine,
                    tradeHeadline = if (uiState.isProvider) uiState.tradeHeadline else null,
                    isVerified = uiState.isVerified,
                    bio = uiState.bio,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )

                // 5: ProfileMetricsBar
                ProfileMetricsBar(
                    metrics = uiState.metrics,
                    modifier = Modifier.testTag("profile_metrics_bar"),
                )

                // 6: ProfileActionRow
                ProfileActionRow(
                    config = effectiveActionConfig,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )

                // 7: Motivational Banner (Form A) / Command Center Banner (Form B) (Owner only, hidden in visitor and public preview)
                if (!uiState.isProvider && !isVisitor && !uiState.isPublicPreview) {
                    CompleteFormBBanner(
                        onClick = onCompleteFormB,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                } else if (uiState.isProvider && !isVisitor && !uiState.isPublicPreview) {
                    ProviderDashboardBanner(
                        onClick = onOpenProviderDashboard,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }

                // 8: ProfileSectionTabs
                if (uiState.tabs.isNotEmpty()) {
                    ProfileSectionTabs(
                        tabs = uiState.tabs,
                        selectedIndex = activeTabIndex,
                        onSelect = { index ->
                            currentTabIndex = index
                            onTabSelected(index)
                        },
                        modifier = Modifier.testTag("profile_tabs"),
                    )
                }

                // 9: Content Section
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
                        if (content != null) {
                            content(activeTabId)
                        } else {
                            when (activeTabId) {
                                "services" -> ServicesPane(
                                    listing = uiState.listing,
                                    isOwner = !isVisitor && !uiState.isPublicPreview,
                                    onEditListing = onEditListing,
                                )
                                "gallery" -> GalleryPane(
                                    items = uiState.gallery,
                                    showUpload = !isVisitor && !uiState.isPublicPreview && uiState.isProvider,
                                    onUpload = onUploadWork,
                                )
                                "activity" -> ActivityPane(
                                    jobs = uiState.activeJobs,
                                    saved = uiState.savedProfessionals,
                                    hasListedService = uiState.isProvider,
                                    onOpenJob = onOpenJob,
                                    onViewAll = onViewAllJobs,
                                )
                                "about" -> AboutPane(
                                    profile = sessionProfile,
                                    experiences = emptyList(),
                                    completedCount = uiState.activeJobs.count { it.status == JobStatus.Completed },
                                    onEditAbout = { onSaveAbout(uiState.bio) },
                                    onEditLookingFor = { onSaveLookingFor(uiState.lookingFor) },
                                    onEditSkills = { onSaveSkills(uiState.skills) },
                                    onAddLicense = { onAddLicense("") },
                                )
                                "reviews" -> ReviewsPane(
                                    reviews = uiState.reviews,
                                )
                                else -> {
                                    Text(
                                        text = "Content for $activeTabId",
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier.testTag("profile_content_text_$activeTabId"),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 10: Floating ProfilePreviewBanner
        if (uiState.isPublicPreview) {
            ProfilePreviewBanner(
                onExitPreview = onExitPreview,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
            )
        }
    }
}

/**
 * Default Services Pane for service providers, displaying the active service card.
 */
@Composable
fun ServicesPane(
    listing: ServiceListing?,
    isOwner: Boolean,
    onEditListing: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (listing != null) {
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            YourServiceCard(
                eyebrow = "SERVICE OFFERING",
                liveLabel = if (listing.live) "Active" else "Unavailable",
                live = listing.live,
                title = listing.title,
                category = listing.category,
                experience = listing.experience,
                description = listing.description,
                fromLabel = "FROM",
                quoteKind = "Visit / Quote",
                quoteValue = listing.quoteLabel,
                coverageLabel = "SERVICE AREA",
                coverageValue = listing.coverage,
                coverageHint = listing.coverageHint,
                hoursLabel = "HOURS",
                hoursValue = listing.hours,
                hoursHint = listing.hoursHint,
                responseLabel = "RESPONSE",
                responseValue = listing.response,
                responseHint = listing.responseHint,
                tags = listing.tags,
                editLabel = if (isOwner) "Edit service" else "",
                onEdit = onEditListing,
            )
        }
    } else {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(24.dp)
                .testTag("profile_services_empty"),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "No services listed yet",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
