package com.askit.app.explore

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.ContentScale
import com.askit.app.R
import java.util.Locale

private val BROWSE_CATEGORY_SPACING = 12.dp

@Composable
internal fun ActiveSearchContent(
    recentSearches: List<String>,
    onRecentSearchSelected: (String) -> Unit,
    onRecentSearchRemoved: (String) -> Unit,
    onRecentSearchesCleared: () -> Unit,
    onCategorySelected: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
    ) {
        if (recentSearches.isNotEmpty()) {
            RecentSearches(
                recentSearches = recentSearches,
                onQuerySelected = onRecentSearchSelected,
                onQueryRemoved = onRecentSearchRemoved,
                onClearAll = onRecentSearchesCleared,
            )
            Spacer(Modifier.height(24.dp))
        }
        SuggestedCategories(onCategorySelected)
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
internal fun BrowseServices(
    status: ExploreBrowseStatus,
    onCategorySelected: (String) -> Unit,
    onChangeArea: () -> Unit,
    onRetry: ((ExploreBrowseSection) -> Unit)?,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
            .testTag("explore_browse_services"),
    ) {
        Text(
            text = stringResource(R.string.explore_browse_services),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.semantics { heading() },
        )
        Spacer(Modifier.height(12.dp))
        when (status) {
            ExploreBrowseStatus.Available -> {
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("explore_browse_categories"),
                    maxItemsInEachRow = 3,
                    horizontalArrangement = Arrangement.spacedBy(BROWSE_CATEGORY_SPACING),
                    verticalArrangement = Arrangement.spacedBy(BROWSE_CATEGORY_SPACING),
                ) {
                    EXPLORE_CATEGORIES.forEach { categoryDefinition ->
                        val category = stringResource(categoryDefinition.labelRes)
                        ServiceCategoryTile(
                            modifier = Modifier.weight(1f),
                            category = category,
                            artworkRes = categoryDefinition.artworkRes,
                            onClickLabel = stringResource(R.string.explore_search_category, category),
                            onClick = { onCategorySelected(category) },
                        )
                    }
                }
            }

            ExploreBrowseStatus.Loading -> BrowseSectionSkeleton(ExploreBrowseSection.Services)

            ExploreBrowseStatus.Empty,
            ExploreBrowseStatus.Offline,
            ExploreBrowseStatus.ServerUnavailable,
            -> BrowseSectionStateCard(
                section = ExploreBrowseSection.Services,
                status = status,
                onChangeArea = onChangeArea,
                onRetry = onRetry?.let { retry -> { retry(ExploreBrowseSection.Services) } },
            )
            }
    }
}

@Composable
private fun ServiceCategoryTile(
    modifier: Modifier = Modifier,
    category: String,
    @DrawableRes artworkRes: Int,
    onClickLabel: String,
    onClick: () -> Unit,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 136.dp)
            .clickable(
                role = Role.Button,
                onClickLabel = onClickLabel,
                onClick = onClick,
            ),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier.size(72.dp),
                contentAlignment = Alignment.Center,
            ) {
                androidx.compose.foundation.Image(
                    painter = painterResource(artworkRes),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = category,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium,
                minLines = 2,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

private data class BrowseSectionStateCopy(
    @DrawableRes val iconRes: Int,
    @StringRes val titleRes: Int,
    @StringRes val supportingTextRes: Int,
)

@Composable
internal fun BrowseSectionStateCard(
    section: ExploreBrowseSection,
    status: ExploreBrowseStatus,
    onChangeArea: (() -> Unit)?,
    onRetry: (() -> Unit)?,
) {
    val effectiveStatus = status.takeUnless { it == ExploreBrowseStatus.Available }
        ?: ExploreBrowseStatus.Empty
    if (effectiveStatus == ExploreBrowseStatus.Loading) {
        BrowseSectionSkeleton(section)
        return
    }

    val copy = browseSectionStateCopy(section, effectiveStatus)
    val isOffline = effectiveStatus == ExploreBrowseStatus.Offline
    val isServerUnavailable = effectiveStatus == ExploreBrowseStatus.ServerUnavailable
    val containerColor = when {
        isOffline -> MaterialTheme.colorScheme.tertiaryContainer
        isServerUnavailable -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.surfaceContainerHigh
    }
    val contentColor = when {
        isOffline -> MaterialTheme.colorScheme.onTertiaryContainer
        isServerUnavailable -> MaterialTheme.colorScheme.onErrorContainer
        else -> MaterialTheme.colorScheme.onSurface
    }
    val supportingTextColor = when {
        isOffline -> MaterialTheme.colorScheme.onTertiaryContainer
        isServerUnavailable -> MaterialTheme.colorScheme.onErrorContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val iconTint = when {
        isOffline -> MaterialTheme.colorScheme.tertiary
        isServerUnavailable -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val action = if (effectiveStatus == ExploreBrowseStatus.Empty) {
        onChangeArea
    } else {
        onRetry
    }
    val actionLabel = if (effectiveStatus == ExploreBrowseStatus.Empty) {
        R.string.explore_change_area
    } else {
        R.string.explore_retry
    }
    val sectionKey = section.name.lowercase(Locale.US)
    val statusKey = effectiveStatus.name.lowercase(Locale.US)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("explore_browse_${sectionKey}_$statusKey")
            .semantics { liveRegion = LiveRegionMode.Polite },
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(copy.iconRes),
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(64.dp),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = stringResource(copy.titleRes),
                    color = contentColor,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = stringResource(copy.supportingTextRes),
                    color = supportingTextColor,
                    style = MaterialTheme.typography.bodyMedium,
                )
                action?.let { onAction ->
                    TextButton(
                        onClick = onAction,
                        modifier = Modifier.testTag("explore_browse_${sectionKey}_action"),
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = contentColor,
                        ),
                    ) {
                        Text(stringResource(actionLabel))
                    }
                }
            }
        }
    }
}

private fun browseSectionStateCopy(
    section: ExploreBrowseSection,
    status: ExploreBrowseStatus,
): BrowseSectionStateCopy = when (status) {
    ExploreBrowseStatus.Empty -> when (section) {
        ExploreBrowseSection.Services -> BrowseSectionStateCopy(
            iconRes = R.drawable.explore_services_empty,
            titleRes = R.string.explore_browse_services_empty_title,
            supportingTextRes = R.string.explore_browse_services_empty_supporting_text,
        )

        ExploreBrowseSection.Professionals -> BrowseSectionStateCopy(
            iconRes = R.drawable.explore_professionals_empty,
            titleRes = R.string.explore_browse_professionals_empty_title,
            supportingTextRes = R.string.explore_browse_professionals_empty_supporting_text,
        )

        ExploreBrowseSection.Tasks -> BrowseSectionStateCopy(
            iconRes = R.drawable.explore_tasks_empty,
            titleRes = R.string.explore_browse_tasks_empty_title,
            supportingTextRes = R.string.explore_browse_tasks_empty_supporting_text,
        )
    }

    ExploreBrowseStatus.Offline -> BrowseSectionStateCopy(
        iconRes = when (section) {
            ExploreBrowseSection.Services -> R.drawable.explore_services_offline
            ExploreBrowseSection.Professionals -> R.drawable.explore_professionals_offline
            ExploreBrowseSection.Tasks -> R.drawable.explore_tasks_offline
        },
        titleRes = R.string.explore_browse_offline_title,
        supportingTextRes = R.string.explore_browse_offline_supporting_text,
    )

    ExploreBrowseStatus.ServerUnavailable -> BrowseSectionStateCopy(
        iconRes = when (section) {
            ExploreBrowseSection.Services -> R.drawable.explore_services_server
            ExploreBrowseSection.Professionals -> R.drawable.explore_professionals_server
            ExploreBrowseSection.Tasks -> R.drawable.explore_tasks_server
        },
        titleRes = R.string.explore_browse_server_title,
        supportingTextRes = R.string.explore_browse_server_supporting_text,
    )

    ExploreBrowseStatus.Available,
    ExploreBrowseStatus.Loading,
    -> error("A browse state card cannot render $status")
}

@Composable
internal fun BrowseSectionSkeleton(
    section: ExploreBrowseSection,
    tagPrefix: String = "explore_browse",
) {
    val sectionKey = section.name.lowercase(Locale.US)
    val loadingTag = "${tagPrefix}_${sectionKey}_loading"
    val skeletonColor = MaterialTheme.colorScheme.surfaceContainerHigh
    val placeholderColor = MaterialTheme.colorScheme.outlineVariant
    when (section) {
        ExploreBrowseSection.Services -> {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(loadingTag),
                maxItemsInEachRow = 3,
                horizontalArrangement = Arrangement.spacedBy(BROWSE_CATEGORY_SPACING),
                verticalArrangement = Arrangement.spacedBy(BROWSE_CATEGORY_SPACING),
            ) {
                repeat(EXPLORE_CATEGORIES.size) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 136.dp),
                        shape = MaterialTheme.shapes.medium,
                        color = skeletonColor,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .background(placeholderColor, RoundedCornerShape(12.dp)),
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.72f)
                                    .height(16.dp)
                                    .background(placeholderColor, RoundedCornerShape(8.dp)),
                            )
                        }
                    }
                }
            }
        }

        ExploreBrowseSection.Professionals,
        ExploreBrowseSection.Tasks,
        -> Column(
            modifier = Modifier
                .fillMaxWidth()
                .testTag(loadingTag),
            verticalArrangement = Arrangement.spacedBy(1.dp),
        ) {
            repeat(2) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = skeletonColor,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(placeholderColor, CircleShape),
                        )
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.62f)
                                    .height(16.dp)
                                    .background(placeholderColor, RoundedCornerShape(8.dp)),
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.4f)
                                    .height(12.dp)
                                    .background(placeholderColor, RoundedCornerShape(6.dp)),
                            )
                        }
                    }
                }
            }
        }
    }
}
