package com.askit.app.listservice

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.askit.app.R
import com.askit.designsystem.services.ServiceResultItem

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ListServiceReview(modifier: Modifier = Modifier, state: ListServiceFormState, onEdit: () -> Unit) {
    val category = serviceCategoryLabel(state)
    val deliveryLabels = serviceDeliveryLabels(state)
    val priceLabel = servicePricingLabel(state)
    val servedAreaLabel = state.customerAreaLabel.trim().takeIf(String::isNotEmpty)?.let {
        stringResource(R.string.list_service_serves_area, it)
    }
    val coverageLabel = servedAreaLabel
    val details = listOf(
        R.string.list_service_availability to state.availability,
        R.string.list_service_experience to state.experience,
        R.string.list_service_typical_duration to state.typicalDuration,
        R.string.list_service_materials to state.materials,
        R.string.list_service_credentials to state.credentials,
        R.string.list_service_warranty to state.warranty,
        R.string.list_service_advance_notice to state.advanceNotice,
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = 640.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.list_service_review_title),
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            text = stringResource(R.string.list_service_review_helper),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(8.dp))
        ListServiceSectionHeading(R.string.list_service_what_you_offer)
        ListServiceSummaryRow(R.string.list_service_category, category)
        ListServiceSummaryRow(R.string.list_service_service_title, state.title.trim())
        ListServiceSummaryBlock(R.string.list_service_description, state.description.trim())

        Spacer(Modifier.height(8.dp))
        ListServiceSectionHeading(R.string.list_service_how_provide)
        ListServiceSummaryBlock(
            R.string.list_service_delivery,
            deliveryLabels.joinToString(", "),
        )
        if (state.customerAreaLabel.isNotBlank()) {
            ListServiceSummaryRow(R.string.list_service_customer_area, state.customerAreaLabel.trim())
            state.coverageRadiusKm?.let { radius ->
                ListServiceSummaryRow(
                    R.string.list_service_travel_coverage,
                    stringResource(R.string.list_service_up_to, radius),
                )
            }
        }
        if (state.providerAreaLabel.isNotBlank()) {
            ListServiceSummaryRow(
                R.string.list_service_provider_location,
                state.providerAreaLabel.trim(),
            )
        }

        priceLabel?.let {
            Spacer(Modifier.height(8.dp))
            ListServiceSectionHeading(R.string.list_service_pricing)
            ListServiceSummaryRow(R.string.list_service_pricing, it)
        }

        val nonEmptyDetails = details.filter { (_, value) -> value.isNotBlank() }
        if (nonEmptyDetails.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            ListServiceSectionHeading(R.string.list_service_more_about)
            nonEmptyDetails.forEach { (labelRes, value) ->
                ListServiceSummaryBlock(labelRes, value.trim())
            }
        }

        Spacer(Modifier.height(8.dp))
        ListServiceSectionHeading(R.string.list_service_customer_preview)
        Text(
            text = stringResource(R.string.list_service_customer_preview_helper),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = stringResource(R.string.list_service_customer_preview_privacy),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.testTag("list_service_privacy_summary"),
        )
        ServiceResultItem(
            serviceTitle = state.title.trim(),
            category = category,
            description = state.description.trim(),
            providerName = null,
            providerAvatarUrl = null,
            priceLabel = priceLabel,
            coverageLabel = coverageLabel,
            deliveryModes = deliveryLabels + listOfNotNull(
                state.coverageRadiusKm
                    ?.takeIf { state.customerAreaLabel.isNotBlank() }
                    ?.let { radius -> stringResource(R.string.list_service_up_to, radius) },
            ),
            portfolioModels = state.portfolioUris,
            modifier = Modifier.testTag("list_service_customer_preview_card"),
        )

        OutlinedButton(
            onClick = onEdit,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp)
                .testTag("list_service_edit"),
        ) {
            Text(stringResource(R.string.list_service_edit))
        }
    }
}
