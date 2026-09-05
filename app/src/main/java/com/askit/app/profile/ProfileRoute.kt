package com.askit.app.profile

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.askit.app.R
import com.askit.app.jobs.Job
import com.askit.app.session.ProfileAvailability
import com.askit.app.session.SessionProfile
import com.askit.designsystem.profile.ProfileActionConfig
import com.askit.designsystem.profile.ProfileStrengthCard
import com.askit.designsystem.profile.ProfileTopBar
import com.askit.designsystem.profile.YourServiceCard
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@Composable
fun ProfileRoute(
    profile: SessionProfile,
    jobs: List<Job>,
    onEditProfile: () -> Unit,
    onEditListing: () -> Unit,
    onUploadWork: () -> Unit,
    onOpenJob: (String) -> Unit,
    onViewAllJobs: () -> Unit,
    onSaveAbout: (String) -> Unit,
    onSaveLookingFor: (List<String>) -> Unit,
    onSaveSkills: (List<String>) -> Unit,
    onSaveAvailability: (ProfileAvailability) -> Unit,
    onSetAvatar: (String?) -> Unit,
    onAddLicense: (String) -> Unit,
    onUsernameCopied: () -> Unit,
    modifier: Modifier = Modifier,
    loadState: ProfileLoadState = ProfileLoadState.Ready,
    messages: Flow<Int> = emptyFlow(),
    onOpenSettings: () -> Unit = {},
    onOpenProviderDashboard: () -> Unit = {},
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val photoActions = rememberProfilePhotoActions { onSetAvatar(it) }
    var paneIndex by rememberSaveable { mutableIntStateOf(0) }
    var isPublicPreview by rememberSaveable { mutableStateOf(false) }
    var isPreviewFollowing by rememberSaveable { mutableStateOf(false) }
    var showAvailability by rememberSaveable { mutableStateOf(false) }
    var showAboutSheet by rememberSaveable { mutableStateOf(false) }
    var showLookingForSheet by rememberSaveable { mutableStateOf(false) }
    var showSkillsSheet by rememberSaveable { mutableStateOf(false) }
    var showPhotoSheet by rememberSaveable { mutableStateOf(false) }
    var showLicenseSheet by rememberSaveable { mutableStateOf(false) }

    BackHandler(enabled = isPublicPreview) {
        isPublicPreview = false
    }

    val listing = profile.listing
    val completedCount = jobs.profileCompletedCount()
    val strengthLabel = if (profile.profileStrengthPercent >= 85) {
        stringResource(R.string.profile_strength_excellent)
    } else {
        stringResource(R.string.profile_strength_good)
    }
    val shareBody = stringResource(
        R.string.profile_share_body,
        profile.displayName,
        profile.username,
        profile.city,
    )

    LaunchedEffect(Unit) {
        messages.collect { res ->
            snackbarHostState.showSnackbar(context.getString(res))
        }
    }

    val uiState = profile.toUiState(
        jobs = jobs,
        isPublicPreview = isPublicPreview,
        selectedTabIndex = paneIndex,
        isFollowing = isPreviewFollowing,
    )

    val actionConfig = if (!isPublicPreview) {
        ProfileActionConfig.Owner(
            onEditProfile = onEditProfile,
            onShare = {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, shareBody)
                }
                context.startActivity(Intent.createChooser(intent, context.getString(R.string.profile_share)))
            },
            onViewAsPublic = { isPublicPreview = true },
        )
    } else {
        ProfileActionConfig.Visitor(
            onMessage = {},
            isFollowing = isPreviewFollowing,
            onToggleFollow = { isPreviewFollowing = !isPreviewFollowing },
            onRequestService = if (uiState.isProvider) onEditListing else null,
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("profile_route"),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        when (loadState) {
            ProfileLoadState.Loading -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(modifier = Modifier.testTag("profile_loading"))
            }
            is ProfileLoadState.Failed -> Box(
                modifier = Modifier.fillMaxSize().padding(padding).testTag("profile_failed"),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(24.dp),
                ) {
                    Text(stringResource(loadState.messageRes), color = MaterialTheme.colorScheme.onSurface)
                    Button(onClick = onEditProfile) { Text(stringResource(R.string.profile_edit)) }
                }
            }
            ProfileLoadState.Ready -> UniversalProfileScaffold(
                uiState = uiState,
                actionConfig = actionConfig,
                modifier = Modifier.fillMaxSize(),
                showTopBar = true,
                onShare = {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, shareBody)
                    }
                    context.startActivity(Intent.createChooser(intent, context.getString(R.string.profile_share)))
                },
                onCopyUsername = {
                    val clipboard = context.getSystemService(ClipboardManager::class.java)
                    clipboard.setPrimaryClip(ClipData.newPlainText("username", profile.username))
                    onUsernameCopied()
                },
                onOpenSettings = onOpenSettings,
                onCameraClick = { showPhotoSheet = true },
                onCompleteFormB = onEditListing,
                onOpenProviderDashboard = onOpenProviderDashboard,
                onTabSelected = { paneIndex = it },
                onExitPreview = { isPublicPreview = false },
                onToggleFollow = { isPreviewFollowing = !isPreviewFollowing },
                onEditListing = onEditListing,
                onUploadWork = onUploadWork,
                onOpenJob = onOpenJob,
                onViewAllJobs = onViewAllJobs,
                onAddLicense = { showLicenseSheet = true },
                onSaveAbout = { showAboutSheet = true },
                onSaveLookingFor = { showLookingForSheet = true },
                onSaveSkills = { showSkillsSheet = true },
                content = { tabId ->
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        if (!isPublicPreview && profile.profileStrengthPercent < 100 && (tabId == "activity" || tabId == "services")) {
                            ProfileStrengthCard(
                                percent = profile.profileStrengthPercent,
                                title = stringResource(R.string.profile_strength_title, strengthLabel),
                                supporting = if (profile.hasListedService) {
                                    stringResource(R.string.profile_strength_listed_hint)
                                } else {
                                    stringResource(R.string.profile_strength_hint)
                                },
                                onClick = onEditProfile,
                            )
                        }

                        when (tabId) {
                            "services" -> {
                                if (profile.showsServiceCard && listing != null) {
                                    YourServiceCard(
                                        eyebrow = stringResource(R.string.profile_your_service).uppercase(),
                                        liveLabel = if (listing.live) {
                                            stringResource(R.string.profile_live)
                                        } else {
                                            stringResource(R.string.profile_not_available)
                                        },
                                        live = listing.live,
                                        title = listing.title,
                                        category = listing.category,
                                        experience = listing.experience,
                                        description = listing.description,
                                        fromLabel = stringResource(R.string.profile_from).uppercase(),
                                        quoteKind = stringResource(R.string.profile_visit_quote),
                                        quoteValue = listing.quoteLabel,
                                        coverageLabel = stringResource(R.string.profile_coverage).uppercase(),
                                        coverageValue = listing.coverage,
                                        coverageHint = listing.coverageHint,
                                        hoursLabel = stringResource(R.string.profile_hours).uppercase(),
                                        hoursValue = listing.hours,
                                        hoursHint = listing.hoursHint,
                                        responseLabel = stringResource(R.string.profile_response).uppercase(),
                                        responseValue = listing.response,
                                        responseHint = listing.responseHint,
                                        tags = listing.tags,
                                        editLabel = if (!isPublicPreview) stringResource(R.string.profile_edit_listing) else "",
                                        onEdit = onEditListing,
                                        modifier = Modifier.testTag("profile_your_service"),
                                    )
                                }
                            }
                            "gallery" -> GalleryPane(
                                items = profile.gallery,
                                showUpload = !isPublicPreview && profile.hasListedService,
                                onUpload = onUploadWork,
                            )
                            "about" -> AboutPane(
                                profile = profile,
                                experiences = listing?.let { experiencesFromListing(it) }.orEmpty(),
                                completedCount = completedCount,
                                onEditAbout = { showAboutSheet = true },
                                onEditLookingFor = { showLookingForSheet = true },
                                onEditSkills = { showSkillsSheet = true },
                                onAddLicense = { showLicenseSheet = true },
                            )
                            "activity" -> ActivityPane(
                                jobs = jobs,
                                saved = profile.savedProfessionals,
                                hasListedService = profile.hasListedService,
                                onOpenJob = onOpenJob,
                                onViewAll = onViewAllJobs,
                            )
                            "reviews" -> ReviewsPane(reviews = profile.reviews)
                        }
                    }
                },
            )
            }
        }

    if (showAvailability) {
        AvailabilitySheet(
            availability = profile.availability,
            onDismiss = { showAvailability = false },
            onSave = {
                onSaveAvailability(it)
                showAvailability = false
            },
        )
    }
    if (showAboutSheet) {
        EditAboutSheet(
            about = profile.about,
            onDismiss = { showAboutSheet = false },
            onSave = {
                onSaveAbout(it)
                showAboutSheet = false
            },
        )
    }
    if (showLookingForSheet) {
        EditLookingForSheet(
            chips = profile.lookingFor,
            onDismiss = { showLookingForSheet = false },
            onSave = {
                onSaveLookingFor(it)
                showLookingForSheet = false
            },
        )
    }
    if (showSkillsSheet && profile.hasListedService) {
        EditLookingForSheet(
            chips = profile.skills.ifEmpty { listing?.tags.orEmpty() },
            onDismiss = { showSkillsSheet = false },
            onSave = {
                onSaveSkills(it)
                showSkillsSheet = false
            },
            titleRes = R.string.profile_skills,
        )
    }
    if (showLicenseSheet) {
        AddLicenseSheet(
            onDismiss = { showLicenseSheet = false },
            onSave = {
                onAddLicense(it)
                showLicenseSheet = false
            },
        )
    }
    if (showPhotoSheet) {
        PhotoSourceSheet(
            onDismiss = { showPhotoSheet = false },
            onCamera = {
                showPhotoSheet = false
                photoActions.launchCamera()
            },
            onLibrary = {
                showPhotoSheet = false
                photoActions.launchLibrary()
            },
            onRemove = {
                onSetAvatar(null)
                showPhotoSheet = false
            },
        )
    }
}
