package com.askit.app.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.askit.app.R
import com.askit.designsystem.navigation.AskITSectionTabs
import com.askit.designsystem.navigation.SectionTab
import com.askit.designsystem.people.AskITAvatar

@Composable
internal fun ProfileRatingsContent(profile: ProfileUiState) {
    var direction by remember(profile.userId, profile.isProvider) {
        mutableStateOf(if (profile.isProvider) ReviewDirection.SEEKER_TO_PROVIDER else ReviewDirection.PROVIDER_TO_SEEKER)
    }
    if (profile.isProvider) {
        AskITSectionTabs(
            tabs = ReviewDirection.entries.map { category ->
                SectionTab(
                    category.name,
                    stringResource(
                        if (category ==
                            ReviewDirection.SEEKER_TO_PROVIDER
                        ) {
                            R.string.profile_received_provider
                        } else {
                            R.string.profile_received_seeker
                        },
                    ),
                )
            },
            selectedId = direction.name,
            onSelect = { id -> direction = ReviewDirection.entries.first { it.name == id } },
            modifier = Modifier.testTag("profile_rating_categories"),
            tabTagPrefix = "profile_rating_",
        )
    } else {
        Text(stringResource(R.string.profile_received_seeker), Modifier.padding(16.dp))
    }
    ReviewsPane(profile.reviews.receivedBy(profile.userId, direction))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ProfileMetricSheet(
    metric: String,
    profile: ProfileUiState,
    people: ProfilePeopleState,
    onDismiss: () -> Unit,
    onOpenProfile: (String) -> Unit,
    onFollow: (String, Boolean) -> Boolean,
) {
    var failed by remember { mutableStateOf(false) }
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Text(
            stringResource(
                when (metric) {
                    "rating" -> R.string.job_review_rating
                    "followers" -> R.string.profile_followers
                    else -> R.string.profile_following
                },
            ),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp),
        )
        if (metric == "rating") {
            Column(Modifier.fillMaxWidth().heightIn(max = 560.dp).verticalScroll(rememberScrollState())) {
                ProfileRatingsContent(profile)
            }
        } else {
            val entries = if (metric == "followers") people.followers else people.following
            when {
                entries == null -> Text(stringResource(R.string.profile_people_unavailable), Modifier.padding(24.dp))

                entries.isEmpty() -> Text(stringResource(R.string.profile_people_empty), Modifier.padding(24.dp))

                else -> LazyColumn(Modifier.fillMaxWidth().heightIn(max = 560.dp)) {
                    items(entries, key = { it.id }) { person ->
                        Row(
                            Modifier.fillMaxWidth().clickable {
                                onDismiss()
                                onOpenProfile(person.id)
                            }.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            AskITAvatar(person.avatarUrl, 40.dp, 24.dp)
                            Text(person.name, Modifier.weight(1f))
                            if (people.viewerId != null && person.id != people.viewerId) {
                                TextButton(enabled = people.canMutate, onClick = {
                                    failed = !onFollow(person.id, person.id !in people.viewerFollowing)
                                }) {
                                    Text(
                                        stringResource(
                                            if (person.id in
                                                people.viewerFollowing
                                            ) {
                                                R.string.profile_unfollow
                                            } else {
                                                R.string.story_follow
                                            },
                                        ),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        if (failed) {
            Text(
                stringResource(R.string.profile_save_unavailable),
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(16.dp),
            )
        }
        Spacer(Modifier.navigationBarsPadding().height(16.dp))
    }
}
