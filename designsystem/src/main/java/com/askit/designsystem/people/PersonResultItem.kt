package com.askit.designsystem.people

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.askit.designsystem.R
import com.askit.designsystem.theme.AskITTheme
import java.text.NumberFormat

@Composable
fun PersonResultItem(
    name: String,
    avatarUrl: String?,
    primaryService: String?,
    additionalServices: List<String>,
    rating: Double?,
    reviewCount: Int,
    locationLabel: String,
    priceLabel: String?,
    statusLabel: String?,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val displayName = name.trim()
    val status = statusLabel?.trim()?.takeIf(String::isNotEmpty)
    val service = primaryService?.trim()?.takeIf(String::isNotEmpty)
    val additionalSummary = service?.let {
        summarizeAdditionalServices(it, additionalServices)
    }
    val additionalAccessibility = additionalSummary?.let { (summary, remaining) ->
        if (remaining == 0) {
            null
        } else {
            pluralStringResource(
                id = R.plurals.person_result_additional_services_accessibility,
                count = remaining,
                summary.substringBeforeLast(" +$remaining"),
                remaining,
            )
        }
    }
    val price = priceLabel?.trim()?.takeIf(String::isNotEmpty)
    val profileAction = stringResource(R.string.person_result_view_profile, displayName)
    val itemModifier = modifier
        .fillMaxWidth()
        .heightIn(min = 48.dp)
        .then(
            if (onClick == null) {
                Modifier
            } else {
                Modifier.clickable(
                    role = Role.Button,
                    onClickLabel = profileAction,
                    onClick = onClick,
                )
            },
        )

    ListItem(
        modifier = itemModifier,
        leadingContent = {
            ResultAvatar(avatarUrl = avatarUrl)
        },
        headlineContent = {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = displayName,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                )
                if (status != null) {
                    Text(
                        text = status,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        },
        supportingContent = {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                if (service != null) {
                    Text(
                        text = service,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                if (additionalSummary != null) {
                    Text(
                        text = additionalSummary.first,
                        modifier = if (additionalAccessibility == null) {
                            Modifier
                        } else {
                            Modifier.semantics {
                                contentDescription = additionalAccessibility.orEmpty()
                            }
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                ResultMetadataLine(
                    rating = rating,
                    reviewCount = reviewCount,
                    locationLabel = locationLabel,
                    price = price.takeIf { service != null },
                    showServiceMetadata = service != null,
                )
            }
        },
    )
}

@Composable
fun AskITAvatar(
    avatarUrl: String?,
    avatarSize: Dp,
    fallbackIconSize: Dp,
    modifier: Modifier = Modifier,
) {
    val fallbackPainter = painterResource(R.drawable.ic_person)
    if (avatarUrl.isNullOrBlank()) {
        Icon(
            painter = fallbackPainter,
            contentDescription = null,
            modifier = modifier.size(fallbackIconSize),
        )
    } else {
        AsyncImage(
            model = ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                .data(avatarUrl)
                .crossfade(false)
                .build(),
            contentDescription = null,
            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
            placeholder = fallbackPainter,
            error = fallbackPainter,
            fallback = fallbackPainter,
            modifier = modifier
                .size(avatarSize)
                .clip(CircleShape),
        )
    }
}

@Composable
fun PersonResultMetadata(
    rating: Double?,
    reviewCount: Int,
    locationLabel: String,
    price: String?,
    modifier: Modifier = Modifier,
) {
    ResultMetadataLine(
        rating = rating,
        reviewCount = reviewCount,
        locationLabel = locationLabel,
        price = price,
        showServiceMetadata = true,
        modifier = modifier,
    )
}

@Composable
private fun ResultAvatar(avatarUrl: String?) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
        contentAlignment = Alignment.Center,
    ) {
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant) {
            AskITAvatar(
                avatarUrl = avatarUrl,
                avatarSize = 48.dp,
                fallbackIconSize = 32.dp,
            )
        }
    }
}

@Composable
private fun ResultMetadataLine(
    rating: Double?,
    reviewCount: Int,
    locationLabel: String,
    price: String?,
    showServiceMetadata: Boolean,
    modifier: Modifier = Modifier,
) {
    val locale = androidx.compose.ui.platform.LocalConfiguration.current.locales[0]
    val ratingFormatter = remember(locale) {
        NumberFormat.getNumberInstance(locale).apply {
            minimumFractionDigits = 0
            maximumFractionDigits = 1
        }
    }
    val countFormatter = remember(locale) {
        NumberFormat.getIntegerInstance(locale)
    }
    val hasReviews = rating != null && rating.isFinite() && rating > 0.0 && reviewCount > 0
    val formattedRating = if (hasReviews) ratingFormatter.format(rating) else null
    val formattedReviewCount = if (hasReviews) countFormatter.format(reviewCount) else null
    val location = locationLabel.trim().takeIf(String::isNotEmpty)

    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        if (showServiceMetadata) {
            MetadataSegment(showSeparator = false) {
                if (hasReviews && formattedRating != null && formattedReviewCount != null) {
                    val reviewAccessibility = pluralStringResource(
                        id = R.plurals.person_result_review_accessibility,
                        count = reviewCount,
                        formattedRating,
                        formattedReviewCount,
                    )
                    Row(
                        modifier = Modifier.semantics(mergeDescendants = true) {
                            contentDescription = reviewAccessibility
                        },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_star_filled),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = stringResource(
                                R.string.person_result_rating_display,
                                formattedRating,
                                formattedReviewCount,
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                } else {
                    Text(
                        text = stringResource(R.string.person_result_new),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
        if (location != null) {
            MetadataSegment(showSeparator = showServiceMetadata) {
                Text(
                    text = location,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
        if (showServiceMetadata && price != null) {
            MetadataSegment(showSeparator = true) {
                Text(
                    text = price,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun MetadataSegment(
    showSeparator: Boolean,
    content: @Composable () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (showSeparator) {
            Text(
                text = "·",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
            Spacer(modifier = Modifier.width(4.dp))
        }
        content()
    }
}

private fun summarizeAdditionalServices(
    primaryService: String,
    additionalServices: List<String>,
): Pair<String, Int>? {
    val primary = primaryService.trim()
    val validServices = additionalServices
        .map(String::trim)
        .filter { it.isNotEmpty() && !it.equals(primary, ignoreCase = true) }
    if (validServices.isEmpty()) return null

    val visibleServices = validServices.take(2)
    val remaining = validServices.size - visibleServices.size
    val visibleSummary = visibleServices.joinToString(", ")
    val displaySummary = if (remaining == 0) {
        visibleSummary
    } else {
        "$visibleSummary +$remaining"
    }
    return displaySummary to remaining
}

@Preview(showBackground = true, widthDp = 360)
@Preview(
    showBackground = true,
    widthDp = 360,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun PreviewPersonResultItems() {
    AskITTheme {
        Column(modifier = Modifier.fillMaxWidth()) {
            PersonResultItem(
                name = "Ravi Kumar",
                avatarUrl = null,
                primaryService = "Electrician",
                additionalServices = listOf("Fan installation", "Wiring", "Appliance repair"),
                rating = 4.8,
                reviewCount = 36,
                locationLabel = "2.4 km",
                priceLabel = "From ₹500",
                statusLabel = "Available today",
                onClick = {},
            )
            HorizontalDivider()
            PersonResultItem(
                name = "Priya S.",
                avatarUrl = "",
                primaryService = "Home tutor",
                additionalServices = listOf("Mathematics", "Science"),
                rating = null,
                reviewCount = 0,
                locationLabel = "Kallakurichi",
                priceLabel = null,
                statusLabel = null,
                onClick = {},
            )
            HorizontalDivider()
            PersonResultItem(
                name = "A very long professional name that should truncate cleanly",
                avatarUrl = null,
                primaryService = "A service with a long descriptive title",
                additionalServices = emptyList(),
                rating = 4.9,
                reviewCount = 18,
                locationLabel = "A long locality label for a narrow screen",
                priceLabel = "Quote required",
                statusLabel = null,
                modifier = Modifier.width(320.dp),
                onClick = {},
            )
        }
    }
}
