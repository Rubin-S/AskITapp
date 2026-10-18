package com.askit.designsystem.services

import com.askit.designsystem.listings.ListingBriefCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.askit.designsystem.R

/**
 * The single customer-facing service result representation.
 *
 * The component accepts service-owned values and a separately owned provider identity. It does
 * only displays optional metadata when callers have it; it invents no provider facts.
 */
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
    ratingLabel: String? = null,
) {
    ListingBriefCard(
        title = serviceTitle,
        category = category,
        summary = description,
        priceLabel = priceLabel,
        locationLabel = coverageLabel,
        authorName = providerName?.takeIf(String::isNotBlank)?.let {
            stringResource(R.string.service_result_provided_by, it)
        },
        authorAvatarUrl = providerAvatarUrl,
        metadata = deliveryModes + listOfNotNull(ratingLabel),
        photoModels = portfolioModels,
        onClickLabel = stringResource(R.string.service_result_view_service, serviceTitle.trim()),
        onClick = onClick,
        modifier = modifier.testTag("service_result_card"),
    )
}
