package com.askit.app.listservice

import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue

import androidx.annotation.StringRes
import androidx.compose.foundation.ExperimentalFoundationApi

import androidx.compose.foundation.layout.Arrangement

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn

import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup

import androidx.compose.foundation.text.KeyboardOptions

import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuDefaults

import androidx.compose.material3.HorizontalDivider

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import com.askit.designsystem.inputs.AskITTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults

import androidx.compose.material3.Text

import androidx.compose.runtime.Composable

import androidx.compose.runtime.mutableStateOf

import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester

import androidx.compose.ui.platform.testTag

import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester

import com.askit.app.R
import com.askit.app.category.ASKIT_SERVICE_CATEGORIES

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
internal fun ListServiceForm(
    modifier: Modifier = Modifier,
    viewModel: ListServiceViewModel,
    state: ListServiceFormState,
    validationErrors: Set<ListServiceValidationField>,
    categoryBringIntoView: BringIntoViewRequester,
    titleBringIntoView: BringIntoViewRequester,
    descriptionBringIntoView: BringIntoViewRequester,
    deliveryBringIntoView: BringIntoViewRequester,
    customerLocationBringIntoView: BringIntoViewRequester,
    providerLocationBringIntoView: BringIntoViewRequester,
    priceBringIntoView: BringIntoViewRequester,
    priceRangeBringIntoView: BringIntoViewRequester,
    titleFocusRequester: FocusRequester,
    descriptionFocusRequester: FocusRequester,
    priceFocusRequester: FocusRequester,
    minimumPriceFocusRequester: FocusRequester,
    onOpenCustomerLocationPicker: () -> Unit,
    onOpenProviderLocationPicker: () -> Unit,
    onPickPhotos: () -> Unit,
) {
    val contentModifier = Modifier
        .then(modifier)
        .fillMaxWidth()
        .widthIn(max = 640.dp)

    Column(
        modifier = contentModifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.list_service_intro),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(8.dp))
        ListServiceSectionHeading(R.string.list_service_identity_heading)
        Text(
            text = stringResource(R.string.list_service_identity_summary),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.testTag("list_service_identity_summary"),
        )

        ServiceProfessionalFields(state, viewModel, ListServiceValidationField.PROFESSIONAL_DETAILS in validationErrors)
        Spacer(Modifier.height(8.dp))
        ListServiceSectionHeading(R.string.list_service_what_you_offer)
        var categoryMenuExpanded by rememberSaveable { mutableStateOf(false) }
        val selectedCategoryLabel = serviceCategoryLabel(state)
        ExposedDropdownMenuBox(
            expanded = categoryMenuExpanded,
            onExpandedChange = { categoryMenuExpanded = !categoryMenuExpanded },
            modifier = Modifier
                .fillMaxWidth()
                .bringIntoViewRequester(categoryBringIntoView)
                .testTag("list_service_category_options"),
        ) {
            AskITTextField(
                value = selectedCategoryLabel,
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.list_service_category)) },
                supportingText = {
                    if (ListServiceValidationField.CATEGORY in validationErrors &&
                        state.categoryId != LIST_SERVICE_OTHER_CATEGORY_ID
                    ) {
                        Text(stringResource(R.string.list_service_error_category))
                    } else {
                        Text(stringResource(R.string.list_service_category_helper))
                    }
                },
                isError = ListServiceValidationField.CATEGORY in validationErrors &&
                    state.categoryId != LIST_SERVICE_OTHER_CATEGORY_ID,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryMenuExpanded)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    .testTag("list_service_category_dropdown"),
            )
            ExposedDropdownMenu(
                expanded = categoryMenuExpanded,
                onDismissRequest = { categoryMenuExpanded = false },
                modifier = Modifier.testTag("list_service_category_menu"),
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            ) {
                ASKIT_SERVICE_CATEGORIES.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(stringResource(category.labelRes)) },
                        onClick = {
                            viewModel.selectCategory(category.id)
                            categoryMenuExpanded = false
                        },
                        modifier = Modifier.testTag("list_service_category_${category.id}"),
                    )
                }
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.list_service_other_category)) },
                    onClick = {
                        viewModel.selectCategory(LIST_SERVICE_OTHER_CATEGORY_ID)
                        categoryMenuExpanded = false
                    },
                    modifier = Modifier.testTag("list_service_category_other"),
                )
            }
        }
        if (state.categoryId == LIST_SERVICE_OTHER_CATEGORY_ID) {
            AskITTextField(
                value = state.customCategory,
                onValueChange = viewModel::updateCustomCategory,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("list_service_custom_category"),
                label = { Text(stringResource(R.string.list_service_custom_category_label)) },
                supportingText = {
                    if (ListServiceValidationField.CATEGORY in validationErrors) {
                        Text(stringResource(R.string.list_service_error_other_category))
                    } else {
                        Text(stringResource(R.string.list_service_custom_category_helper))
                    }
                },
                isError = ListServiceValidationField.CATEGORY in validationErrors,
                singleLine = true,
            )
        }

        AskITTextField(
            value = state.title,
            onValueChange = viewModel::updateTitle,
            modifier = Modifier
                .fillMaxWidth()
                .bringIntoViewRequester(titleBringIntoView)
                .focusRequester(titleFocusRequester)
                .testTag("list_service_title_field"),
            label = { Text(stringResource(R.string.list_service_service_title)) },
            supportingText = {
                if (ListServiceValidationField.TITLE in validationErrors) {
                    ListServiceError(R.string.list_service_error_title)
                } else {
                    Text(stringResource(R.string.list_service_title_helper))
                }
            },
            isError = ListServiceValidationField.TITLE in validationErrors,
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        )

        AskITTextField(
            value = state.description,
            onValueChange = viewModel::updateDescription,
            modifier = Modifier
                .fillMaxWidth()
                .bringIntoViewRequester(descriptionBringIntoView)
                .focusRequester(descriptionFocusRequester)
                .testTag("list_service_description_field"),
            label = { Text(stringResource(R.string.list_service_description)) },
            supportingText = {
                if (ListServiceValidationField.DESCRIPTION in validationErrors) {
                    ListServiceError(R.string.list_service_error_description)
                } else {
                    Text(stringResource(R.string.list_service_description_helper))
                }
            },
            isError = ListServiceValidationField.DESCRIPTION in validationErrors,
            minLines = 4,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default),
        )

        Spacer(Modifier.height(8.dp))
        ListServiceSectionHeading(R.string.list_service_how_provide)
        Text(
            text = stringResource(R.string.list_service_delivery_helper),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Column(
            modifier = Modifier
                .bringIntoViewRequester(deliveryBringIntoView)
                .selectableGroup()
                .testTag("list_service_delivery_modes"),
        ) {
            ListServiceCheckboxRow(
                modifier = Modifier.testTag("list_service_delivery_customer"),
                label = stringResource(R.string.list_service_at_customer_location),
                selected = ListServiceDeliveryMode.AT_CUSTOMER_LOCATION in state.deliveryModes,
                onClick = {
                    viewModel.toggleDeliveryMode(ListServiceDeliveryMode.AT_CUSTOMER_LOCATION)
                },
            )
            ListServiceCheckboxRow(
                modifier = Modifier.testTag("list_service_delivery_provider"),
                label = stringResource(R.string.list_service_at_provider_location),
                selected = ListServiceDeliveryMode.AT_PROVIDER_LOCATION in state.deliveryModes,
                onClick = {
                    viewModel.toggleDeliveryMode(ListServiceDeliveryMode.AT_PROVIDER_LOCATION)
                },
            )
            ListServiceCheckboxRow(
                modifier = Modifier.testTag("list_service_delivery_remote"),
                label = stringResource(R.string.list_service_remote),
                selected = ListServiceDeliveryMode.REMOTE in state.deliveryModes,
                onClick = { viewModel.toggleDeliveryMode(ListServiceDeliveryMode.REMOTE) },
            )
        }
        if (ListServiceValidationField.DELIVERY_MODE in validationErrors) {
            ListServiceError(R.string.list_service_error_delivery)
        }

        if (ListServiceDeliveryMode.AT_CUSTOMER_LOCATION in state.deliveryModes) {
            Column(
                modifier = Modifier
                    .bringIntoViewRequester(customerLocationBringIntoView)
                    .testTag("list_service_customer_location_section"),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ListServiceSubheading(R.string.list_service_customer_area)
                ListServiceAreaButton(
                    areaLabel = state.customerAreaLabel,
                    onClick = onOpenCustomerLocationPicker,
                    testTag = "list_service_choose_customer_area",
                )
                if (ListServiceValidationField.CUSTOMER_LOCATION in validationErrors) {
                    ListServiceError(R.string.list_service_error_customer_location)
                }
                Text(
                    text = if (state.customerAreaLabel.isBlank()) {
                        stringResource(R.string.list_service_location_helper)
                    } else {
                        stringResource(R.string.list_service_customer_location_privacy)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                ListServiceSubheading(R.string.list_service_travel_coverage)
                Text(
                    text = stringResource(R.string.list_service_coverage_helper),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                ListServiceCoverageChips(
                    selectedRadius = state.coverageRadiusKm,
                    onSelectRadius = viewModel::selectCoverageRadius,
                )
                if (ListServiceValidationField.COVERAGE_RADIUS in validationErrors) {
                    ListServiceError(R.string.list_service_error_coverage_radius)
                }
            }
        }

        if (ListServiceDeliveryMode.AT_PROVIDER_LOCATION in state.deliveryModes) {
            Column(
                modifier = Modifier
                    .bringIntoViewRequester(providerLocationBringIntoView)
                    .testTag("list_service_provider_location_section"),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ListServiceSubheading(R.string.list_service_provider_location)
                ListServiceAreaButton(
                    areaLabel = state.providerAreaLabel,
                    onClick = onOpenProviderLocationPicker,
                    testTag = "list_service_choose_provider_area",
                )
                if (ListServiceValidationField.PROVIDER_LOCATION in validationErrors) {
                    ListServiceError(R.string.list_service_error_provider_location)
                }
                Text(
                    text = if (state.providerAreaLabel.isBlank()) {
                        stringResource(R.string.list_service_provider_location_helper)
                    } else {
                        stringResource(R.string.list_service_provider_location_privacy)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (state.deliveryModes == setOf(ListServiceDeliveryMode.REMOTE)) {
            Text(
                text = stringResource(R.string.list_service_remote_helper),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.testTag("list_service_remote_helper"),
            )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
        ListServiceOptionalSectionRow(
            titleRes = R.string.list_service_pricing,
            expanded = ListServiceOptionalSection.PRICING in state.expandedOptionalSections,
            onClick = { viewModel.toggleOptionalSection(ListServiceOptionalSection.PRICING) },
            testTag = "list_service_pricing_toggle",
        )
        if (ListServiceOptionalSection.PRICING in state.expandedOptionalSections) {
            ListServicePricingEditor(
                state = state,
                validationErrors = validationErrors,
                viewModel = viewModel,
                priceBringIntoView = priceBringIntoView,
                priceRangeBringIntoView = priceRangeBringIntoView,
                priceFocusRequester = priceFocusRequester,
                minimumPriceFocusRequester = minimumPriceFocusRequester,
            )
        }

        ListServiceSectionHeading(R.string.list_service_show_work)
        Text(
            text = stringResource(R.string.list_service_optional),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = stringResource(R.string.list_service_show_work_helper),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OutlinedButton(
            onClick = onPickPhotos,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp)
                .testTag("list_service_add_photos"),
        ) {
            Text(
                stringResource(
                    if (state.portfolioUris.isEmpty()) {
                        R.string.list_service_add_photos
                    } else {
                        R.string.list_service_add_more_photos
                    },
                ),
            )
        }
        PortfolioPhotoList(
            uris = state.portfolioUris,
            onRemovePhoto = viewModel::removePortfolioPhoto,
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
        ListServiceOptionalSectionRow(
            titleRes = R.string.list_service_more_about,
            expanded = ListServiceOptionalSection.MORE_DETAILS in state.expandedOptionalSections,
            onClick = {
                viewModel.toggleOptionalSection(ListServiceOptionalSection.MORE_DETAILS)
            },
            testTag = "list_service_more_about_toggle",
        )
        if (ListServiceOptionalSection.MORE_DETAILS in state.expandedOptionalSections) {
            ListServiceMoreDetailsEditor(state = state, viewModel = viewModel)
        }

    }
}

@Composable
internal fun serviceCategoryLabel(state: ListServiceFormState): String = when {
    state.categoryId == LIST_SERVICE_OTHER_CATEGORY_ID -> state.customCategory.ifBlank {
        stringResource(R.string.list_service_other_category)
    }

    else -> ASKIT_SERVICE_CATEGORIES
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

@Composable
internal fun ListServiceSummaryRow(@StringRes labelRes: Int, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = stringResource(labelRes),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(text = value, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
internal fun ListServiceSummaryBlock(@StringRes labelRes: Int, value: String) {
    if (value.isBlank()) return
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = stringResource(labelRes),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(text = value, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
internal fun ListServiceCheckboxRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
internal fun ListServicePricingChoiceRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton,
            )
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.secondary,
                unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant,
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
internal fun ListServiceOptionalSectionRow(
    @StringRes titleRes: Int,
    expanded: Boolean,
    onClick: () -> Unit,
    testTag: String,
) {
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

@Composable
internal fun ListServiceError(@StringRes messageRes: Int) {
    val message = stringResource(messageRes)
    Text(
        text = message,
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.semantics { error(message) },
    )
}

internal fun firstInvalidField(
    errors: Set<ListServiceValidationField>,
): ListServiceValidationField = listOf(
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
    AskITTextField(value = state.contactPhone, onValueChange = { viewModel.updateContact(it, state.contactEmail) },
        label = { Text(stringResource(R.string.settings_phone_number) + " *") }, singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), modifier = Modifier.fillMaxWidth())
    AskITTextField(value = state.contactEmail, onValueChange = { viewModel.updateContact(state.contactPhone, it) },
        label = { Text(stringResource(R.string.account_email) + " *") }, singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), modifier = Modifier.fillMaxWidth())
    AskITTextField(value = state.availability, onValueChange = viewModel::updateAvailability,
        label = { Text(stringResource(R.string.list_service_availability) + " *") }, modifier = Modifier.fillMaxWidth())
    AskITTextField(value = state.experience, onValueChange = viewModel::updateExperience,
        label = { Text(stringResource(R.string.service_experience_years) + " *") }, singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
    val experienceLabel = stringResource(R.string.service_experience_years)
    androidx.compose.material3.Slider(value = (state.experience.toIntOrNull() ?: 0).coerceIn(0, 60).toFloat(),
        onValueChange = { viewModel.updateExperience(kotlin.math.round(it).toInt().toString()) },
        valueRange = 0f..60f, steps = 59,
        modifier = Modifier.semantics { contentDescription = experienceLabel })
    if (invalid) Text(stringResource(R.string.service_required_details_error), color = MaterialTheme.colorScheme.error)
}
