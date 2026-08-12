package com.askit.designsystem.services

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.askit.designsystem.R
import com.askit.designsystem.people.AskITAvatar

/**
 * The single customer-facing service result representation.
 *
 * The component accepts service-owned values and a separately owned provider identity. It does
 * not accept rating, distance, verification, or other values that would need to be invented for
 * a newly created service.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ServiceResultItem(
    serviceTitle: String,
    category: String,
    description: String,
    modifier: Modifier = Modifier,
    providerName: String? = null,
    providerAvatarUrl: String? = null,
    priceLabel: String? = null,
    coverageLabel: String? = null,
    deliveryModes: List<String> = emptyList(),
    portfolioModels: List<Any> = emptyList(),
    onClick: (() -> Unit)? = null,
) {
    val title = serviceTitle.trim()
    val displayCategory = category.trim()
    val displayDescription = description.trim()
    val displayProviderName = providerName?.trim()?.takeIf(String::isNotEmpty)
    val displayPrice = priceLabel?.trim()?.takeIf(String::isNotEmpty)
    val displayCoverage = coverageLabel?.trim()?.takeIf(String::isNotEmpty)
    val displayModes = deliveryModes.map(String::trim).filter(String::isNotEmpty)
    val accessibilitySummary = buildList {
        if (title.isNotEmpty()) add(title)
        if (displayCategory.isNotEmpty()) add(displayCategory)
        displayProviderName?.let { add(stringResource(R.string.service_result_provided_by, it)) }
        if (displayDescription.isNotEmpty()) add(displayDescription)
        displayPrice?.let(::add)
        displayCoverage?.let(::add)
        addAll(displayModes)
        if (portfolioModels.isNotEmpty()) {
            add(
                pluralStringResource(
                    R.plurals.service_result_portfolio_count,
                    portfolioModels.size,
                    portfolioModels.size,
                ),
            )
        }
    }.joinToString(", ")
    val cardModifier = modifier
        .fillMaxWidth()
        .heightIn(min = 48.dp)
        .testTag("service_result_card")
        .semantics(mergeDescendants = true) {
            contentDescription = accessibilitySummary
        }
        .then(
            if (onClick == null) {
                Modifier
            } else {
                Modifier.clickable(
                    role = Role.Button,
                    onClickLabel = stringResource(R.string.service_result_view_service, title),
                    onClick = onClick,
                )
            },
        )

    Card(
        modifier = cardModifier,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (portfolioModels.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.75f),
                ) {
                    AsyncImage(
                        model = portfolioModels.first(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize(),
                    )
                    if (portfolioModels.size > 1) {
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
                                    R.plurals.service_result_portfolio_count,
                                    portfolioModels.size,
                                    portfolioModels.size,
                                ),
                                color = MaterialTheme.colorScheme.inverseOnSurface,
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier.padding(
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 16.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (displayCategory.isNotEmpty()) {
                    Text(
                        text = displayCategory,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleLarge,
                )
                if (displayProviderName != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Box(
                            modifier = Modifier.size(40.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CompositionLocalProvider(
                                LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant,
                            ) {
                                AskITAvatar(
                                    avatarUrl = providerAvatarUrl,
                                    avatarSize = 40.dp,
                                    fallbackIconSize = 28.dp,
                                )
                            }
                        }
                        Text(
                            text = stringResource(
                                R.string.service_result_provided_by,
                                displayProviderName,
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
                if (displayDescription.isNotEmpty()) {
                    Text(
                        text = displayDescription,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 5,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                val metadata = buildList {
                    displayPrice?.let(::add)
                    displayCoverage?.let(::add)
                    addAll(displayModes)
                }
                if (metadata.isNotEmpty()) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        metadata.forEach { value ->
                            Text(
                                text = value,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
            }
        }
    }
}
