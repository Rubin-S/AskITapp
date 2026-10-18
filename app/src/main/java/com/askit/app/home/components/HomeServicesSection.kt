package com.askit.app.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.askit.app.home.model.FeedItem
import com.askit.app.home.model.ServicePreview
import com.askit.designsystem.services.ServiceResultItem
import com.askit.app.R as AppR

@Composable
fun HomeServicesSection(section: FeedItem.ServiceSection, modifier: Modifier = Modifier, onServiceClick: (ServicePreview) -> Unit = {}) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(AppR.string.home_services_title),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.2).sp,
                ),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            items(
                items = section.services,
                key = { it.id },
            ) { service ->
                HomeServiceCard(
                    service = service,
                    onClick = { onServiceClick(service) },
                )
            }
        }
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
        modifier = modifier.width(280.dp),
        onClick = onClick,
    )
}
