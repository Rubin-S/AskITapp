package com.askit.app.home.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.askit.app.profile.ProfileUiState
import com.askit.app.profile.UniversalProfileScaffold
import com.askit.app.profile.toUiState
import com.askit.designsystem.profile.ProfileActionConfig
import com.askit.designsystem.profile.ProfileMetricItem
import com.askit.designsystem.profile.ProfileTabSpec
import com.askit.designsystem.R as DsR
import kotlinx.coroutines.launch

data class UserReviewData(
    val reviewerName: String,
    val rating: Double,
    val date: String,
    val comment: String,
)

data class UserProfileData(
    val id: String,
    val username: String,
    val name: String,
    val trade: String = "",
    val avatarUrl: String? = null,
    val rating: Double = 0.0,
    val completedJobsCount: Int = 0,
    val location: String = "Local District",
    val memberSince: String = "Member since 2024",
    val bio: String = "",
    val skills: List<String> = emptyList(),
    val reviews: List<UserReviewData> = emptyList(),
    val isProvider: Boolean = true,
    val activityCount: Int = 12,
    val followerCount: Int = 42,
    val followingCount: Int = 18,
)

fun getUserProfileById(userId: String): UserProfileData = when (userId) {
    "member-1", "user-1" -> UserProfileData(
        id = userId,
        username = "rahul_verma",
        name = "Rahul Verma",
        trade = "",
        avatarUrl = null,
        isProvider = false,
        rating = 0.0,
        completedJobsCount = 0,
        activityCount = 15,
        followerCount = 84,
        followingCount = 31,
        location = "Indiranagar, Bengaluru",
        memberSince = "Member since 2023",
        bio = "Active community member seeking home renovation and electronics assistance.",
        skills = emptyList(),
        reviews = emptyList(),
    )
    "pro-1" -> UserProfileData(
        id = userId,
        username = "priya_electric",
        name = "Priya Sharma",
        trade = "Certified Electrician",
        avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300",
        rating = 4.9,
        completedJobsCount = 48,
        location = "1.2 km away",
        memberSince = "Member since 2022",
        bio = "Certified electrician with 8+ years of residential and commercial wiring experience. Specializing in inverter repairs, fuse box upgrades, ceiling fan installation, and safety audits.",
        skills = listOf("Inverter Repair", "Ceiling Fan Installation", "Wiring & Rewiring", "Switchboard Upgrades", "Emergency Diagnostics"),
        reviews = listOf(
            UserReviewData(
                reviewerName = "Karthik V.",
                rating = 5.0,
                date = "2 days ago",
                comment = "Priya fixed our inverter and installed 2 new ceiling fans in under an hour. Super clean work!",
            ),
            UserReviewData(
                reviewerName = "Meera R.",
                rating = 4.9,
                date = "1 week ago",
                comment = "Diagnosed the tripping circuit issue promptly. Highly recommended.",
            ),
        ),
    )
    "pro-2" -> UserProfileData(
        id = userId,
        username = "arun_plumbing",
        name = "Arun Kumar",
        trade = "Master Plumber",
        avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=300",
        rating = 4.8,
        completedJobsCount = 62,
        location = "2.5 km away",
        memberSince = "Member since 2021",
        bio = "Licensed plumber handling leak detection, pipeline replacements, sanitary fittings, and water heater servicing across Gandhipuram.",
        skills = listOf("Pipe Fitting", "Leak Detection", "Geyser Repair", "Drain Cleaning", "Bathroom Fixtures"),
        reviews = listOf(
            UserReviewData(
                reviewerName = "Suresh K.",
                rating = 5.0,
                date = "Yesterday",
                comment = "Arun found the hidden pipe leakage in 15 minutes. Transparent pricing.",
            ),
        ),
    )
    else -> UserProfileData(
        id = userId,
        username = "user_$userId",
        name = "Community Professional",
        trade = "Verified Specialist",
        avatarUrl = null,
        rating = 4.9,
        completedJobsCount = 25,
        location = "Local District",
        memberSince = "Member since 2024",
        bio = "Professional member of the AskIT community offering verified services and top-rated work.",
        skills = listOf("Consultation", "Repairs", "Installation"),
        reviews = listOf(
            UserReviewData(
                reviewerName = "Verified Client",
                rating = 5.0,
                date = "Recently",
                comment = "Great service, communicated clearly, and completed the work on schedule.",
            ),
        ),
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun UserProfileScreen(
    userId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onMessage: () -> Unit = {},
    onRequestService: () -> Unit = {},
) {
    val user = remember(userId) { getUserProfileById(userId) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var isFollowing by rememberSaveable { mutableStateOf(false) }
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }

    val uiState = user.toUiState(isFollowing = isFollowing, selectedTabIndex = selectedTabIndex)

    val actionConfig = ProfileActionConfig.Visitor(
        onMessage = {
            onMessage()
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Direct message opened with ${user.name}")
            }
        },
        isFollowing = isFollowing,
        onToggleFollow = { isFollowing = !isFollowing },
        onRequestService = if (user.isProvider) {
            {
                onRequestService()
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Booking inquiry sent to ${user.name}!")
                }
            }
        } else null,
    )

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("user_profile_screen"),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "@${user.username}",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(DsR.drawable.ic_arrow_back),
                            contentDescription = "Back",
                        )
                    }
                },
            )
        },
    ) { padding ->
        UniversalProfileScaffold(
            uiState = uiState,
            actionConfig = actionConfig,
            showTopBar = false,
            isVisitor = true,
            modifier = Modifier.padding(padding),
            onTabSelected = { selectedTabIndex = it },
            content = { tabId ->
                when (tabId) {
                    "about" -> {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            if (user.bio.isNotBlank()) {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = "About",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                    Text(
                                        text = user.bio,
                                        style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 22.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                            if (user.skills.isNotEmpty()) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = "Skills & Specializations",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth(),
                                    ) {
                                        user.skills.forEach { skill ->
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                            ) {
                                                Text(
                                                    text = skill,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    "reviews" -> {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            if (user.reviews.isEmpty()) {
                                Text(
                                    text = "No reviews yet.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 16.dp),
                                )
                            } else {
                                user.reviews.forEach { rev ->
                                    Card(
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                                        ),
                                        modifier = Modifier.fillMaxWidth(),
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(14.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp),
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically,
                                            ) {
                                                Text(
                                                    text = rev.reviewerName,
                                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                )
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                                                ) {
                                                    Icon(
                                                        painter = painterResource(DsR.drawable.ic_star_filled),
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(14.dp),
                                                    )
                                                    Text(
                                                        text = "%.1f".format(rev.rating),
                                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                                        color = MaterialTheme.colorScheme.onSurface,
                                                    )
                                                }
                                            }
                                            Text(
                                                text = rev.comment,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                            Text(
                                                text = rev.date,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    "services", "gallery", "activity" -> {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                            ),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text(
                                    text = if (user.isProvider) "${user.name}'s verified service profile" else "Community member activity",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            },
        )
    }
}
