package com.askit.designsystem.tasks

import com.askit.designsystem.listings.ListingBriefCard
import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.askit.designsystem.R
import com.askit.designsystem.theme.AskITTheme

enum class TaskResultStatus {
    Open,
    Applied,
    Filled,
    Closed,
    Expired,
    Unavailable,
}

/**
 * The canonical public Task representation used by both Explore and Post Task Review.
 *
 * [photoModels] intentionally accepts Coil's existing model input seam. Production callers
 * currently pass URI/URL strings; tests can pass an in-memory bitmap without adding an app
 * dependency to the design system.
 */
@Composable
fun TaskResultItem(
    title: String,
    category: String,
    summary: String?,
    budgetLabel: String,
    locationLabel: String,
    timingLabel: String,
    posterName: String,
    postedLabel: String,
    modifier: Modifier = Modifier,
    status: TaskResultStatus? = null,
    photoModels: List<Any> = emptyList(),
    distanceLabel: String? = null,
    scopeHighlights: List<String> = emptyList(),
    onClick: (() -> Unit)? = null,
) {
    ListingBriefCard(
        title = title,
        category = category,
        summary = summary,
        priceLabel = budgetLabel,
        locationLabel = listOfNotNull(locationLabel, distanceLabel).filter(String::isNotBlank).joinToString(" · "),
        authorName = posterName,
        metadata = listOf(timingLabel, postedLabel) + scopeHighlights.take(2) + listOfNotNull(
            status?.takeIf { it != TaskResultStatus.Open }?.let { stringResource(taskStatusString(it)) },
        ),
        photoModels = photoModels,
        onClickLabel = stringResource(R.string.task_result_view_task, title.trim()),
        onClick = onClick,
        modifier = modifier,
    )
}

@Composable
fun TaskPhotoMedia(
    photoModels: List<Any>,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    showCount: Boolean = true,
    description: String? = null,
) {
    if (photoModels.isEmpty()) return
    val context = LocalContext.current
    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(context)
            .data(photoModels.first())
            .size(1280, 1280)
            .crossfade(false)
            .build(),
    )
    val photoCount = photoModels.size
    val imageState = painter.state.collectAsState().value
    val imageUnavailable = imageState is AsyncImagePainter.State.Error
    val mediaDescription = if (imageUnavailable) {
        stringResource(R.string.task_result_photo_unavailable)
    } else {
        description ?: pluralStringResource(R.plurals.task_result_photo_count, photoCount, photoCount)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(3f / 2f)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .semantics { contentDescription = mediaDescription },
        contentAlignment = Alignment.Center,
    ) {
        if (imageUnavailable) {
            Text(
                text = stringResource(R.string.task_result_photo_unavailable),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
        } else if (imageState is AsyncImagePainter.State.Loading || imageState is AsyncImagePainter.State.Empty) {
            Text(
                text = stringResource(R.string.task_result_photo_loading),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
        } else {
            Image(
                painter = painter,
                contentDescription = null,
                contentScale = contentScale,
                modifier = Modifier.fillMaxSize(),
            )
        }

        if (showCount) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp)
                    .background(
                        color = MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.94f),
                        shape = MaterialTheme.shapes.small,
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .clearAndSetSemantics {},
            ) {
                Text(
                    text = pluralStringResource(
                        R.plurals.task_result_photo_count,
                        photoCount,
                        photoCount,
                    ),
                    color = MaterialTheme.colorScheme.inverseOnSurface,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
    }
}

private fun taskStatusString(status: TaskResultStatus): Int = when (status) {
    TaskResultStatus.Open -> R.string.task_result_status_open
    TaskResultStatus.Applied -> R.string.task_result_status_applied
    TaskResultStatus.Filled -> R.string.task_result_status_filled
    TaskResultStatus.Closed -> R.string.task_result_status_closed
    TaskResultStatus.Expired -> R.string.task_result_status_expired
    TaskResultStatus.Unavailable -> R.string.task_result_status_unavailable
}

@Preview(showBackground = true, widthDp = 360)
@Preview(
    showBackground = true,
    widthDp = 360,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun PreviewTaskResultItems() {
    AskITTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            TaskResultItem(
                title = "Repair laptop charging port",
                category = "Computer repair",
                summary = "Laptop only charges when the cable is held at an angle.",
                budgetLabel = "₹800–₹1,500",
                locationLabel = "Kallakurichi",
                timingLabel = "Needed Monday",
                posterName = "Arun P.",
                postedLabel = "Posted 2h ago",
                status = TaskResultStatus.Open,
                onClick = {},
            )
            TaskResultItem(
                title = "Translate a two-page document",
                category = "Translation",
                summary = "Translate the supplied English document into Tamil.",
                budgetLabel = "Quote required",
                locationLabel = "Remote",
                timingLabel = "Flexible",
                posterName = "Suresh K.",
                postedLabel = "Posted 40m ago",
                status = TaskResultStatus.Applied,
                onClick = {},
            )
        }
    }
}
