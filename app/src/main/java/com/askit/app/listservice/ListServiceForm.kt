package com.askit.app.listservice

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.askit.app.R
import com.askit.app.category.ASKIT_SERVICE_CATEGORIES
import com.askit.app.forms.FormFieldError
import com.askit.designsystem.components.AskITDisclosureRow
import com.askit.designsystem.components.AskITPhotoThumbnails
import com.askit.designsystem.inputs.AskITTextField

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
                    FormFieldError(R.string.list_service_error_title)
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
                    FormFieldError(R.string.list_service_error_description)
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
            FormFieldError(R.string.list_service_error_delivery)
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
                    FormFieldError(R.string.list_service_error_customer_location)
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
                    FormFieldError(R.string.list_service_error_coverage_radius)
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
                    FormFieldError(R.string.list_service_error_provider_location)
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
        val pricingExpanded = ListServiceOptionalSection.PRICING in state.expandedOptionalSections
        AskITDisclosureRow(
            title = stringResource(R.string.list_service_pricing),
            expanded = pricingExpanded,
            actionLabel = stringResource(if (pricingExpanded) R.string.list_service_hide else R.string.list_service_show),
            onClick = { viewModel.toggleOptionalSection(ListServiceOptionalSection.PRICING) },
            modifier = Modifier.testTag("list_service_pricing_toggle"),
            supportingLabel = stringResource(R.string.list_service_optional),
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
        AskITPhotoThumbnails(
            photos = state.portfolioUris,
            photoDescription = { stringResource(R.string.list_service_photo_description, it) },
            removeDescription = { stringResource(R.string.list_service_remove_photo, it) },
            onRemovePhoto = viewModel::removePortfolioPhoto,
            modifier = Modifier.testTag("list_service_portfolio_photos"),
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
        val moreDetailsExpanded = ListServiceOptionalSection.MORE_DETAILS in state.expandedOptionalSections
        AskITDisclosureRow(
            title = stringResource(R.string.list_service_more_about),
            expanded = moreDetailsExpanded,
            actionLabel = stringResource(if (moreDetailsExpanded) R.string.list_service_hide else R.string.list_service_show),
            onClick = { viewModel.toggleOptionalSection(ListServiceOptionalSection.MORE_DETAILS) },
            modifier = Modifier.testTag("list_service_more_about_toggle"),
            supportingLabel = stringResource(R.string.list_service_optional),
        )
        if (ListServiceOptionalSection.MORE_DETAILS in state.expandedOptionalSections) {
            ListServiceMoreDetailsEditor(state = state, viewModel = viewModel)
        }
    }
}
