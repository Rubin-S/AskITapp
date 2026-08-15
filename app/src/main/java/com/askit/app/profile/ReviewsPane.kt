package com.askit.app.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.askit.app.R
import com.askit.designsystem.profile.MetricSpec
import com.askit.designsystem.profile.MetricTrio
import com.askit.designsystem.profile.ProfileSectionCard
import com.askit.designsystem.profile.ReviewRow

@Composable
fun ReviewsPane(
    reviews: List<ProfileReview>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("profile_reviews_pane"),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (reviews.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .testTag("profile_reviews_empty"),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = stringResource(R.string.profile_reviews_empty_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(R.string.profile_reviews_empty_supporting),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            val average = reviews.map { it.rating }.average()
            val averageLabel = String.format("%.1f", average)
            ProfileSectionCard(
                title = stringResource(R.string.profile_reviews),
                actionLabel = stringResource(R.string.profile_reviews_summary, averageLabel, reviews.size),
            ) {
                MetricTrio(
                    first = MetricSpec(averageLabel, stringResource(R.string.profile_metric_rating)),
                    second = MetricSpec(reviews.size.toString(), stringResource(R.string.profile_metric_reviews)),
                    third = MetricSpec(
                        reviews.count { it.rating >= 4f }.toString(),
                        stringResource(R.string.profile_metric_positive),
                    ),
                )
                reviews.forEachIndexed { index, review ->
                    ReviewRow(
                        name = review.name,
                        meta = review.meta,
                        rating = review.rating,
                        body = review.body,
                        verifiedLabel = null,
                        avatarUrl = review.avatarUrl,
                        showDivider = index != reviews.lastIndex,
                    )
                }
            }
        }
    }
}
