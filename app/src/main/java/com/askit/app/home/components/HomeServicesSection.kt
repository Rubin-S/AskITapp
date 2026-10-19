package com.askit.app.home.components

import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import com.askit.app.home.model.FeedItem
import com.askit.app.home.model.ServicePreview
import com.askit.designsystem.components.AskITCarouselSection
import com.askit.designsystem.services.ServiceResultItem
import com.askit.app.R as AppR

@Composable
fun HomeServicesSection(section: FeedItem.ServiceSection, modifier: Modifier = Modifier, onServiceClick: (ServicePreview) -> Unit = {}) {
    AskITCarouselSection(
        title = stringResource(AppR.string.home_services_title),
        items = section.services,
        key = { it.id },
        modifier = modifier,
    ) { service, itemModifier ->
        HomeServiceCard(service = service, onClick = { onServiceClick(service) }, modifier = itemModifier)
    }
}

@Composable
private fun HomeServiceCard(service: ServicePreview, onClick: () -> Unit, modifier: Modifier = Modifier) {
    ServiceResultItem(
        serviceTitle = service.title,
        category = service.category,
        description = "",
        providerName = service.providerName,
        providerAvatarUrl = service.providerAvatarUrl,
        priceLabel = service.startingPriceLabel,
        ratingLabel = service.rating?.let {
            String.format(LocalConfiguration.current.locales[0], "★ %.1f", it)
        },
        modifier = modifier,
        onClick = onClick,
    )
}
