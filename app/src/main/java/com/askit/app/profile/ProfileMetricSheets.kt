package com.askit.app.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.askit.app.R
import com.askit.app.format.appLocale
import com.askit.designsystem.navigation.AskITSectionTabs
import com.askit.designsystem.navigation.SectionTab
import com.askit.designsystem.people.AskITAvatar
import com.askit.designsystem.profile.MetricSpec
import com.askit.designsystem.profile.MetricTrio
import com.askit.designsystem.profile.ProfileSectionCard
import com.askit.designsystem.profile.ReviewRow

/** Rating, followers or following sheet opened from the metrics bar. */
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
    var failed by remember(metric) { mutableStateOf(false) }
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Text(stringResource(metricLabel(metric)), style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(16.dp))
        if (metric == ProfileMetric.RATING) {
            Column(Modifier.fillMaxWidth().heightIn(max = 560.dp).verticalScroll(rememberScrollState())) { ProfileRatingsContent(profile) }
        } else {
            val entries = if (metric == ProfileMetric.FOLLOWERS) people.followers else people.following
            when {
                entries == null -> Text(stringResource(R.string.profile_people_unavailable), Modifier.padding(24.dp))

                entries.isEmpty() -> Text(stringResource(R.string.profile_people_empty), Modifier.padding(24.dp))

                else -> LazyColumn(Modifier.fillMaxWidth().heightIn(max = 560.dp)) {
                    items(entries, key = { it.id }) { person ->
                        Row(
                            Modifier.fillMaxWidth().clickable(role = Role.Button) {
                                onDismiss()
                                onOpenProfile(person.id)
                            }.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            AskITAvatar(person.avatarUrl, 40.dp, 24.dp)
                            Text(person.name, Modifier.weight(1f))
                            if (people.viewerId != null && person.id != people.viewerId) {
                                val following = person.id in people.viewerFollowing
                                TextButton(enabled = people.canMutate, onClick = {
                                    // A later success clears an earlier failure.
                                    failed = !onFollow(person.id, !following)
                                }) {
                                    Text(stringResource(if (following) R.string.profile_unfollow else R.string.story_follow))
                                }
                            }
                        }
                    }
                }
            }
        }
        if (failed) {
            Text(
                stringResource(R.string.profile_action_error),
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(16.dp),
            )
        }
        Spacer(Modifier.navigationBarsPadding().height(16.dp))
    }
}

@Composable
private fun ProfileRatingsContent(profile: ProfileUiState) {
    val default = if (profile.isProvider) ReviewDirection.SEEKER_TO_PROVIDER else ReviewDirection.PROVIDER_TO_SEEKER
    var direction by rememberSaveable(profile.userId, profile.isProvider) { mutableStateOf(default) }
    if (profile.isProvider) {
        AskITSectionTabs(
            tabs = ReviewDirection.entries.map {
                SectionTab(
                    it.name,
                    stringResource(
                        if (it ==
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
            onSelect = { id -> direction = ReviewDirection.valueOf(id) },
            modifier = Modifier.testTag("profile_rating_categories"),
            tabTagPrefix = "profile_rating_",
        )
    } else {
        Text(stringResource(R.string.profile_received_seeker), Modifier.padding(16.dp))
    }
    ReviewsPane(profile.reviews.receivedBy(profile.userId, direction))
}

@Composable
private fun ReviewsPane(reviews: List<ProfileReview>) {
    Column(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp).testTag("profile_reviews_pane"),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (reviews.isEmpty()) {
            Column(
                Modifier.fillMaxWidth().padding(24.dp).testTag("profile_reviews_empty"),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(stringResource(R.string.profile_reviews_empty_title), style = MaterialTheme.typography.titleLarge)
                Text(
                    stringResource(R.string.profile_reviews_empty_supporting),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            return@Column
        }
        val average = String.format(appLocale(), "%.1f", reviews.map { it.rating }.average())
        ProfileSectionCard(title = stringResource(R.string.profile_reviews)) {
            MetricTrio(
                first = MetricSpec(average, stringResource(R.string.profile_metric_rating)),
                second = MetricSpec(reviews.size.toString(), stringResource(R.string.profile_metric_reviews)),
                third = MetricSpec(
                    reviews.count {
                        it.rating >= POSITIVE_RATING
                    }.toString(),
                    stringResource(R.string.profile_metric_positive),
                ),
            )
            reviews.forEachIndexed { index, review ->
                ReviewRow(
                    name = review.name,
                    meta = review.meta,
                    rating = review.rating,
                    body = review.body,
                    avatarUrl = review.avatarUrl,
                    ratingDescription = stringResource(R.string.profile_rating_description, review.rating),
                    showDivider = index != reviews.lastIndex,
                )
            }
        }
    }
}

private const val POSITIVE_RATING = 4f
