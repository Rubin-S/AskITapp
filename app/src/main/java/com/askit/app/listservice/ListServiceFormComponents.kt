package com.askit.app.listservice

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.askit.app.R
import com.askit.app.category.ASKIT_SERVICE_CATEGORIES
import com.askit.app.forms.FormSummaryRow
import com.askit.designsystem.inputs.AskITTextField

@Composable
internal fun serviceCategoryLabel(state: ListServiceFormState): String = when {
    state.categoryId == LIST_SERVICE_OTHER_CATEGORY_ID -> state.customCategory.ifBlank {
        stringResource(R.string.list_service_other_category)
    }

    else ->
        ASKIT_SERVICE_CATEGORIES
            .firstOrNull { it.id == state.categoryId }
            ?.let { category -> stringResource(category.labelRes) }
            ?: stringResource(R.string.list_service_select_category)
}

@Composable
internal fun serviceDeliveryLabels(state: ListServiceFormState): List<String> = buildList {
    if (ListServiceDeliveryMode.AT_CUSTOMER_LOCATION in state.deliveryModes) {
        add(stringResource(R.string.list_service_at_customer_location))
    }
    if (ListServiceDeliveryMode.AT_PROVIDER_LOCATION in state.deliveryModes) {
        add(
            state.providerAreaLabel.trim().takeIf(String::isNotEmpty)?.let { area ->
                stringResource(R.string.list_service_provider_mode_with_area, area)
            } ?: stringResource(R.string.list_service_at_provider_location),
        )
    }
    if (ListServiceDeliveryMode.REMOTE in state.deliveryModes) {
        add(stringResource(R.string.list_service_remote))
    }
}

@Composable
internal fun servicePricingLabel(state: ListServiceFormState): String? = when (state.pricingMode) {
    ListServicePricingMode.CONTACT_FOR_QUOTE -> stringResource(R.string.list_service_contact_for_quote)

    ListServicePricingMode.STARTING_AT -> state.priceAmount.trim().takeIf(String::isNotEmpty)?.let {
        stringResource(R.string.list_service_price_starting_value, it)
    }

    ListServicePricingMode.FIXED -> state.priceAmount.trim().takeIf(String::isNotEmpty)?.let {
        stringResource(R.string.list_service_price_fixed_value, it)
    }

    ListServicePricingMode.HOURLY -> state.priceAmount.trim().takeIf(String::isNotEmpty)?.let {
        stringResource(R.string.list_service_price_hourly_value, it)
    }

    ListServicePricingMode.PER_VISIT -> state.priceAmount.trim().takeIf(String::isNotEmpty)?.let {
        stringResource(R.string.list_service_price_visit_value, it)
    }

    ListServicePricingMode.RANGE -> if (
        state.minimumPrice.isNotBlank() && state.maximumPrice.isNotBlank()
    ) {
        stringResource(
            R.string.list_service_price_range_value,
            state.minimumPrice.trim(),
            state.maximumPrice.trim(),
        )
    } else {
        null
    }

    null -> null
}

@Composable
internal fun ListServiceSectionHeading(@StringRes titleRes: Int) {
    Text(
        text = stringResource(titleRes),
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.semantics { heading() },
    )
}

@Composable
internal fun ListServiceSubheading(@StringRes titleRes: Int) {
    Text(
        text = stringResource(titleRes),
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.semantics { heading() },
    )
}

/** A review row that is left out when the user did not fill the field in. */
@Composable
internal fun ListServiceSummaryBlock(@StringRes labelRes: Int, value: String) {
    if (value.isNotBlank()) FormSummaryRow(labelRes, value)
}

@Composable
internal fun ListServiceCheckboxRow(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.Checkbox,
            )
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = selected,
            onCheckedChange = null,
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.secondary,
                uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        )
        Spacer(Modifier.size(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
internal fun ListServiceOptionalSectionRow(@StringRes titleRes: Int, expanded: Boolean, onClick: () -> Unit, testTag: String) {
    val stateText = stringResource(
        if (expanded) R.string.list_service_hide else R.string.list_service_show,
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .testTag(testTag)
            .semantics { stateDescription = stateText }
            .selectable(
                selected = expanded,
                onClick = onClick,
                role = Role.Button,
            )
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(titleRes),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = stringResource(R.string.list_service_optional),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.size(8.dp))
        Text(
            text = stateText,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.secondary,
        )
    }
}

internal fun firstInvalidField(errors: Set<ListServiceValidationField>): ListServiceValidationField = listOf(
    ListServiceValidationField.CATEGORY,
    ListServiceValidationField.TITLE,
    ListServiceValidationField.DESCRIPTION,
    ListServiceValidationField.PROFESSIONAL_DETAILS,
    ListServiceValidationField.DELIVERY_MODE,
    ListServiceValidationField.CUSTOMER_LOCATION,
    ListServiceValidationField.COVERAGE_RADIUS,
    ListServiceValidationField.PROVIDER_LOCATION,
    ListServiceValidationField.PRICE,
    ListServiceValidationField.PRICE_RANGE,
).first { it in errors }

@Composable
internal fun ServiceProfessionalFields(state: ListServiceFormState, viewModel: ListServiceViewModel, invalid: Boolean) {
    ListServiceSectionHeading(R.string.service_required_details)
    AskITTextField(
        value = state.contactPhone,
        onValueChange = { viewModel.updateContact(it, state.contactEmail) },
        label = { Text(stringResource(R.string.settings_phone_number) + " *") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        modifier = Modifier.fillMaxWidth(),
    )
    AskITTextField(
        value = state.contactEmail,
        onValueChange = { viewModel.updateContact(state.contactPhone, it) },
        label = { Text(stringResource(R.string.account_email) + " *") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        modifier = Modifier.fillMaxWidth(),
    )
    Text(
        text = stringResource(R.string.list_service_contact_private),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.fillMaxWidth().testTag("list_service_contact_private"),
    )
    AskITTextField(
        value = state.availability,
        onValueChange = viewModel::updateAvailability,
        label = { Text(stringResource(R.string.list_service_availability) + " *") },
        modifier = Modifier.fillMaxWidth(),
    )
    AskITTextField(
        value = state.experience,
        onValueChange = viewModel::updateExperience,
        label = { Text(stringResource(R.string.service_experience_years) + " *") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth(),
    )
    val experienceLabel = stringResource(R.string.service_experience_years)
    androidx.compose.material3.Slider(
        value = (state.experience.toIntOrNull() ?: 0).coerceIn(0, 60).toFloat(),
        onValueChange = { viewModel.updateExperience(kotlin.math.round(it).toInt().toString()) },
        valueRange = 0f..60f,
        steps = 59,
        modifier = Modifier.semantics { contentDescription = experienceLabel },
    )
    if (invalid) Text(stringResource(R.string.service_required_details_error), color = MaterialTheme.colorScheme.error)
}
