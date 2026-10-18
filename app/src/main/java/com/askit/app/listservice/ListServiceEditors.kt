package com.askit.app.listservice

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.askit.app.R
import com.askit.app.forms.FormFieldError
import com.askit.app.forms.FormRadioRow
import com.askit.designsystem.inputs.AskITTextField

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ListServicePricingEditor(
    state: ListServiceFormState,
    validationErrors: Set<ListServiceValidationField>,
    viewModel: ListServiceViewModel,
    priceBringIntoView: BringIntoViewRequester,
    priceRangeBringIntoView: BringIntoViewRequester,
    priceFocusRequester: FocusRequester,
    minimumPriceFocusRequester: FocusRequester,
) {
    Column(
        modifier = Modifier
            .selectableGroup()
            .testTag("list_service_pricing_options"),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        FormRadioRow(
            modifier = Modifier.testTag("list_service_pricing_contact"),
            label = stringResource(R.string.list_service_contact_for_quote),
            selected = state.pricingMode == ListServicePricingMode.CONTACT_FOR_QUOTE,
            onClick = { viewModel.selectPricingMode(ListServicePricingMode.CONTACT_FOR_QUOTE) },
        )
        FormRadioRow(
            modifier = Modifier.testTag("list_service_pricing_starting"),
            label = stringResource(R.string.list_service_starting_at),
            selected = state.pricingMode == ListServicePricingMode.STARTING_AT,
            onClick = { viewModel.selectPricingMode(ListServicePricingMode.STARTING_AT) },
        )
        FormRadioRow(
            modifier = Modifier.testTag("list_service_pricing_fixed"),
            label = stringResource(R.string.list_service_fixed_price),
            selected = state.pricingMode == ListServicePricingMode.FIXED,
            onClick = { viewModel.selectPricingMode(ListServicePricingMode.FIXED) },
        )
        FormRadioRow(
            modifier = Modifier.testTag("list_service_pricing_hourly"),
            label = stringResource(R.string.list_service_hourly),
            selected = state.pricingMode == ListServicePricingMode.HOURLY,
            onClick = { viewModel.selectPricingMode(ListServicePricingMode.HOURLY) },
        )
        FormRadioRow(
            modifier = Modifier.testTag("list_service_pricing_visit"),
            label = stringResource(R.string.list_service_per_visit),
            selected = state.pricingMode == ListServicePricingMode.PER_VISIT,
            onClick = { viewModel.selectPricingMode(ListServicePricingMode.PER_VISIT) },
        )
        FormRadioRow(
            modifier = Modifier.testTag("list_service_pricing_range"),
            label = stringResource(R.string.list_service_range),
            selected = state.pricingMode == ListServicePricingMode.RANGE,
            onClick = { viewModel.selectPricingMode(ListServicePricingMode.RANGE) },
        )
    }

    when (state.pricingMode) {
        ListServicePricingMode.STARTING_AT,
        ListServicePricingMode.FIXED,
        ListServicePricingMode.HOURLY,
        ListServicePricingMode.PER_VISIT,
        -> AskITTextField(
            value = state.priceAmount,
            onValueChange = viewModel::updatePriceAmount,
            modifier = Modifier
                .fillMaxWidth()
                .bringIntoViewRequester(priceBringIntoView)
                .focusRequester(priceFocusRequester)
                .testTag("list_service_price_amount"),
            label = { Text(stringResource(R.string.list_service_price_amount)) },
            supportingText = {
                if (ListServiceValidationField.PRICE in validationErrors) {
                    FormFieldError(R.string.list_service_error_price)
                }
            },
            isError = ListServiceValidationField.PRICE in validationErrors,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        )

        ListServicePricingMode.RANGE -> Column(
            modifier = Modifier
                .bringIntoViewRequester(priceRangeBringIntoView)
                .testTag("list_service_price_range_fields"),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AskITTextField(
                value = state.minimumPrice,
                onValueChange = viewModel::updateMinimumPrice,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(minimumPriceFocusRequester)
                    .testTag("list_service_minimum_price"),
                label = { Text(stringResource(R.string.list_service_minimum_price)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            )
            AskITTextField(
                value = state.maximumPrice,
                onValueChange = viewModel::updateMaximumPrice,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("list_service_maximum_price"),
                label = { Text(stringResource(R.string.list_service_maximum_price)) },
                supportingText = {
                    if (ListServiceValidationField.PRICE_RANGE in validationErrors) {
                        FormFieldError(R.string.list_service_error_price_range)
                    }
                },
                isError = ListServiceValidationField.PRICE_RANGE in validationErrors,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            )
        }

        ListServicePricingMode.CONTACT_FOR_QUOTE,
        null,
        -> Unit
    }
}

@Composable
internal fun ListServiceMoreDetailsEditor(state: ListServiceFormState, viewModel: ListServiceViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        ListServiceOptionalTextField(
            value = state.typicalDuration,
            labelRes = R.string.list_service_typical_duration,
            testTag = "list_service_typical_duration",
            onValueChange = viewModel::updateTypicalDuration,
        )
        ListServiceOptionalTextField(
            value = state.materials,
            labelRes = R.string.list_service_materials,
            testTag = "list_service_materials",
            onValueChange = viewModel::updateMaterials,
        )
        ListServiceOptionalTextField(
            value = state.credentials,
            labelRes = R.string.list_service_credentials,
            testTag = "list_service_credentials",
            onValueChange = viewModel::updateCredentials,
        )
        ListServiceOptionalTextField(
            value = state.warranty,
            labelRes = R.string.list_service_warranty,
            testTag = "list_service_warranty",
            onValueChange = viewModel::updateWarranty,
        )
        ListServiceOptionalTextField(
            value = state.advanceNotice,
            labelRes = R.string.list_service_advance_notice,
            testTag = "list_service_advance_notice",
            onValueChange = viewModel::updateAdvanceNotice,
        )
    }
}

@Composable
internal fun ListServiceOptionalTextField(value: String, @StringRes labelRes: Int, testTag: String, onValueChange: (String) -> Unit) {
    AskITTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag),
        label = { Text(stringResource(labelRes)) },
        singleLine = false,
        minLines = 2,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ListServiceCoverageChips(selectedRadius: Int?, onSelectRadius: (Int) -> Unit) {
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("list_service_coverage_options"),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        LIST_SERVICE_COVERAGE_RADII_KM.forEach { radius ->
            FilterChip(
                selected = selectedRadius == radius,
                onClick = { onSelectRadius(radius) },
                label = { Text(stringResource(R.string.list_service_coverage_option, radius)) },
                leadingIcon = if (selectedRadius == radius) {
                    {
                        Icon(
                            painter = painterResource(R.drawable.ic_check),
                            contentDescription = null,
                        )
                    }
                } else {
                    null
                },
                modifier = Modifier.testTag("list_service_coverage_$radius"),
            )
        }
    }
}

@Composable
internal fun ListServiceAreaButton(areaLabel: String, onClick: () -> Unit, testTag: String) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .testTag(testTag),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.Start,
        ) {
            Text(
                text = areaLabel.ifBlank { stringResource(R.string.list_service_choose_area) },
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = if (areaLabel.isBlank()) {
                    stringResource(R.string.list_service_location_helper)
                } else {
                    stringResource(R.string.list_service_change_area)
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun PortfolioPhotoList(uris: List<String>, onRemovePhoto: (String) -> Unit) {
    if (uris.isEmpty()) return
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("list_service_portfolio_photos"),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        uris.forEachIndexed { index, uri ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.widthIn(min = 88.dp, max = 96.dp),
            ) {
                Box(modifier = Modifier.size(88.dp)) {
                    AsyncImage(
                        model = uri,
                        contentDescription = stringResource(
                            R.string.list_service_photo_description,
                            index + 1,
                        ),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(MaterialTheme.shapes.medium)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                    )
                    IconButton(
                        onClick = { onRemovePhoto(uri) },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(48.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                shape = CircleShape,
                            ),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_clear),
                            contentDescription = stringResource(
                                R.string.list_service_remove_photo,
                                index + 1,
                            ),
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        }
    }
}
