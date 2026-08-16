package com.askit.app.profile

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.askit.designsystem.profile.ProfileActionRow
import com.askit.designsystem.profile.ProfileAvatar
import com.askit.designsystem.profile.ProfileCover
import com.askit.designsystem.profile.ProfileIdentityBlock
import com.askit.designsystem.profile.ProfileSectionTabs
import com.askit.designsystem.profile.ProfileStatRow
import com.askit.designsystem.profile.ProfileStrengthCard
import com.askit.designsystem.profile.ProfileTabSpec
import com.askit.designsystem.profile.ProfileTopBar
import com.askit.designsystem.profile.YourServiceCard
import com.askit.designsystem.R as DsR
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
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val photoActions = rememberProfilePhotoActions { onSetAvatar(it) }
    var paneIndex by rememberSaveable { mutableIntStateOf(0) }
    var showAvailability by rememberSaveable { mutableStateOf(false) }
    var showAboutSheet by rememberSaveable { mutableStateOf(false) }
    var showLookingForSheet by rememberSaveable { mutableStateOf(false) }
    var showSkillsSheet by rememberSaveable { mutableStateOf(false) }
    var showPhotoSheet by rememberSaveable { mutableStateOf(false) }
    var showLicenseSheet by rememberSaveable { mutableStateOf(false) }
    val panes = ProfilePane.entries
    val selected = panes[paneIndex]
    val tabs = listOf(
        ProfileTabSpec("gallery", stringResource(R.string.profile_tab_gallery), DsR.drawable.ic_photo),
        ProfileTabSpec("about", stringResource(R.string.profile_tab_about), DsR.drawable.ic_person),
        ProfileTabSpec("activity", stringResource(R.string.profile_tab_activity), DsR.drawable.ic_work),
        ProfileTabSpec("reviews", stringResource(R.string.profile_tab_reviews), DsR.drawable.ic_star_outline),
    )
    val strengthLabel = if (profile.profileStrengthPercent >= 85) {
        stringResource(R.string.profile_strength_excellent)
    } else {
        stringResource(R.string.profile_strength_good)
    }
    val listing = profile.listing
    val completedCount = jobs.profileCompletedCount()
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
            ProfileLoadState.Ready -> Column(
                modifier = Modifier.fillMaxSize(),
            ) {
                ProfileTopBar(
                    username = profile.username,
                    moreContentDescription = stringResource(R.string.profile_more),
                    shareLabel = stringResource(R.string.profile_share),
                    copyLabel = stringResource(R.string.profile_copy_username),
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
                )
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 24.dp),
                ) {
                    Column {
                        Box {
                            ProfileCover()
                            ProfileAvatar(
                                avatarUrl = profile.avatarUrl,
                                cameraContentDescription = stringResource(R.string.profile_camera),
                                onCamera = { showPhotoSheet = true },
                                modifier = Modifier
                                    .padding(start = 16.dp)
                                    .offset(y = 72.dp),
                            )
                        }
                        Column(
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 56.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            ProfileActionRow(
                                editLabel = stringResource(R.string.profile_edit),
                                onEdit = onEditProfile,
                                availabilityLabel = if (profile.hasListedService) {
                                    stringResource(R.string.profile_availability)
                                } else {
                                    null
                                },
                                onAvailability = if (profile.hasListedService) {
                                    { showAvailability = true }
                                } else {
                                    null
                                },
                            )
                            ProfileIdentityBlock(
                                displayName = profile.displayName,
                                bio = profile.bio,
                                locationLine = profile.locationLine(
                                    stringResource(R.string.profile_joined, profile.joinedYear),
                                ),
                            )
                            ProfileStatRow(
                                activityLabel = stringResource(R.string.profile_stat_activity),
                                activityValue = jobs.size.toString(),
                                followingLabel = stringResource(R.string.profile_stat_following),
                                followingValue = profile.followingCount.toString(),
                                followersLabel = stringResource(R.string.profile_stat_followers),
                                followersValue = profile.followerCount.toString(),
                            )
                        }
                    }
                    ProfileStrengthCard(
                        percent = profile.profileStrengthPercent,
                        title = stringResource(R.string.profile_strength_title, strengthLabel),
                        supporting = if (profile.hasListedService) {
                            stringResource(R.string.profile_strength_listed_hint)
                        } else {
                            stringResource(R.string.profile_strength_hint)
                        },
                        onClick = onEditProfile,
                        modifier = Modifier.padding(16.dp),
                    )
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
                            editLabel = stringResource(R.string.profile_edit_listing),
                            onEdit = onEditListing,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        )
                    }
                    ProfileSectionTabs(
                        tabs = tabs,
                        selectedIndex = paneIndex,
                        onSelect = { paneIndex = it },
                    )
                    when (selected) {
                        ProfilePane.Gallery -> GalleryPane(
                            items = profile.gallery,
                            showUpload = profile.hasListedService,
                            onUpload = onUploadWork,
                        )
                        ProfilePane.About -> AboutPane(
                            profile = profile,
                            experiences = listing?.let { experiencesFromListing(it) }.orEmpty(),
                            completedCount = completedCount,
                            onEditAbout = { showAboutSheet = true },
                            onEditLookingFor = { showLookingForSheet = true },
                            onEditSkills = { showSkillsSheet = true },
                            onAddLicense = { showLicenseSheet = true },
                        )
                        ProfilePane.Activity -> ActivityPane(
                            jobs = jobs,
                            saved = profile.savedProfessionals,
                            hasListedService = profile.hasListedService,
                            onOpenJob = onOpenJob,
                            onViewAll = onViewAllJobs,
                        )
                        ProfilePane.Reviews -> ReviewsPane(reviews = profile.reviews)
                    }
                }
            }
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
