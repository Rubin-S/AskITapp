package com.askit.app.listservice

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.askit.app.R
import com.askit.app.category.ASKIT_SERVICE_CATEGORIES
import com.askit.app.explore.ExploreLocationSource
import com.askit.app.explore.ExploreSearchArea
import com.askit.app.explore.SearchAreaScreen
import com.askit.app.media.persistPhotoPickerReadAccess
import com.askit.designsystem.services.ServiceResultItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListServiceRoute(
    viewModel: ListServiceViewModel,
    onBack: () -> Unit,
    onCompleteDraft: (ListServiceDraft) -> Unit = {},
) {
    val state by viewModel.formState.collectAsStateWithLifecycle()
    var showCustomerLocationPicker by rememberSaveable { mutableStateOf(false) }
    var showProviderLocationPicker by rememberSaveable { mutableStateOf(false) }
    var showDiscardDialog by rememberSaveable { mutableStateOf(false) }

    fun requestBack() {
        if (viewModel.isDirty) {
            showDiscardDialog = true
        } else {
            onBack()
        }
    }

    BackHandler {
        when {
            showDiscardDialog -> showDiscardDialog = false
            showCustomerLocationPicker -> showCustomerLocationPicker = false
            showProviderLocationPicker -> showProviderLocationPicker = false
            else -> requestBack()
        }
    }

    when {
        showCustomerLocationPicker -> {
            val confirmedArea = ExploreSearchArea(
                placeId = state.customerPlaceId,
                displayName = state.customerAreaLabel.ifBlank {
                    stringResource(R.string.explore_selected_area)
                },
                supportingText = null,
                latitude = state.customerLatitude,
                longitude = state.customerLongitude,
                radiusKm = state.coverageRadiusKm ?: 10,
                source = ExploreLocationSource.SAVED,
            )
            SearchAreaScreen(
                confirmedArea = confirmedArea,
                onBack = { showCustomerLocationPicker = false },
                titleRes = R.string.list_service_choose_area_title,
                showFilterControls = false,
                onApply = { area, _ ->
                    if (area.isUsable) {
                        viewModel.selectCustomerLocation(
                            placeId = area.placeId,
                            publicAreaLabel = area.displayName,
                            latitude = requireNotNull(area.latitude),
                            longitude = requireNotNull(area.longitude),
                        )
                    }
                    showCustomerLocationPicker = false
                },
            )
        }

        showProviderLocationPicker -> {
            val confirmedArea = ExploreSearchArea(
                placeId = state.providerPlaceId,
                displayName = state.providerAreaLabel.ifBlank {
                    stringResource(R.string.explore_selected_area)
                },
                supportingText = null,
                latitude = state.providerLatitude,
                longitude = state.providerLongitude,
                radiusKm = 1,
                source = ExploreLocationSource.SAVED,
            )
            SearchAreaScreen(
                confirmedArea = confirmedArea,
                onBack = { showProviderLocationPicker = false },
                titleRes = R.string.list_service_choose_area_title,
                showFilterControls = false,
                onApply = { area, _ ->
                    if (area.isUsable) {
                        viewModel.selectProviderLocation(
                            placeId = area.placeId,
                            publicAreaLabel = area.displayName,
                            latitude = requireNotNull(area.latitude),
                            longitude = requireNotNull(area.longitude),
                        )
                    }
                    showProviderLocationPicker = false
                },
            )
        }

        else -> {
            ListServiceScreen(
                viewModel = viewModel,
                state = state,
                validationErrors = viewModel.validationErrors(),
                onBack = ::requestBack,
                onOpenCustomerLocationPicker = { showCustomerLocationPicker = true },
                onOpenProviderLocationPicker = { showProviderLocationPicker = true },
                onCompleteDraft = onCompleteDraft,
            )
        }
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text(stringResource(R.string.list_service_discard_title)) },
            text = { Text(stringResource(R.string.list_service_discard_message)) },
            confirmButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text(stringResource(R.string.list_service_keep_editing))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDiscardDialog = false
                        onBack()
                    },
                ) {
                    Text(stringResource(R.string.list_service_discard))
                }
            },
        )
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun ListServiceScreen(
    viewModel: ListServiceViewModel,
    state: ListServiceFormState,
    validationErrors: Set<ListServiceValidationField>,
    onBack: () -> Unit,
    onOpenCustomerLocationPicker: () -> Unit,
    onOpenProviderLocationPicker: () -> Unit,
    onCompleteDraft: (ListServiceDraft) -> Unit,
) {
    val listState = rememberLazyListState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val contentResolver = LocalContext.current.contentResolver
    val categoryBringIntoView = remember { BringIntoViewRequester() }
    val titleBringIntoView = remember { BringIntoViewRequester() }
    val descriptionBringIntoView = remember { BringIntoViewRequester() }
    val deliveryBringIntoView = remember { BringIntoViewRequester() }
    val customerLocationBringIntoView = remember { BringIntoViewRequester() }
    val providerLocationBringIntoView = remember { BringIntoViewRequester() }
    val priceBringIntoView = remember { BringIntoViewRequester() }
    val priceRangeBringIntoView = remember { BringIntoViewRequester() }
    val titleFocusRequester = remember { FocusRequester() }
    val descriptionFocusRequester = remember { FocusRequester() }
    val priceFocusRequester = remember { FocusRequester() }
    val minimumPriceFocusRequester = remember { FocusRequester() }

    LaunchedEffect(state.reviewAttempt) {
        if (state.screenMode == ListServiceScreenMode.FORM && validationErrors.isNotEmpty()) {
            when (firstInvalidField(validationErrors)) {
                ListServiceValidationField.CATEGORY -> categoryBringIntoView.bringIntoView()
                ListServiceValidationField.TITLE -> {
                    titleBringIntoView.bringIntoView()
                    titleFocusRequester.requestFocus()
                }
                ListServiceValidationField.DESCRIPTION -> {
                    descriptionBringIntoView.bringIntoView()
                    descriptionFocusRequester.requestFocus()
                }
                ListServiceValidationField.DELIVERY_MODE -> deliveryBringIntoView.bringIntoView()
                ListServiceValidationField.CUSTOMER_LOCATION,
                ListServiceValidationField.COVERAGE_RADIUS,
                -> customerLocationBringIntoView.bringIntoView()
                ListServiceValidationField.PROVIDER_LOCATION -> providerLocationBringIntoView.bringIntoView()
                ListServiceValidationField.PRICE -> {
                    priceBringIntoView.bringIntoView()
                    priceFocusRequester.requestFocus()
                }
                ListServiceValidationField.PRICE_RANGE -> {
                    priceRangeBringIntoView.bringIntoView()
                    minimumPriceFocusRequester.requestFocus()
                }
            }
        }
    }

    LaunchedEffect(state.screenMode) {
        if (state.screenMode == ListServiceScreenMode.REVIEW) listState.scrollToItem(0)
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
    ) { uris ->
        if (uris.isNotEmpty()) {
                persistPhotoPickerReadAccess(contentResolver, uris)
            viewModel.setPortfolioUris(
                viewModel.formState.value.portfolioUris + uris.map(Uri::toString),
            )
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.list_service_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.explore_back),
                        )
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .imePadding(),
            contentPadding = PaddingValues(
                start = 16.dp,
                top = padding.calculateTopPadding() + 16.dp,
                end = 16.dp,
                bottom = padding.calculateBottomPadding() + 24.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    if (state.screenMode == ListServiceScreenMode.REVIEW) {
                        ListServiceReview(
                            state = state,
                            onEdit = viewModel::edit,
                            onComplete = {
                                viewModel.buildValidatedDraft()?.let(onCompleteDraft)
                            },
                        )
                    } else {
                        ListServiceForm(
                            viewModel = viewModel,
                            state = state,
                            validationErrors = validationErrors,
                            categoryBringIntoView = categoryBringIntoView,
                            titleBringIntoView = titleBringIntoView,
                            descriptionBringIntoView = descriptionBringIntoView,
                            deliveryBringIntoView = deliveryBringIntoView,
                            customerLocationBringIntoView = customerLocationBringIntoView,
                            providerLocationBringIntoView = providerLocationBringIntoView,
                            priceBringIntoView = priceBringIntoView,
                            priceRangeBringIntoView = priceRangeBringIntoView,
                            titleFocusRequester = titleFocusRequester,
                            descriptionFocusRequester = descriptionFocusRequester,
                            priceFocusRequester = priceFocusRequester,
                            minimumPriceFocusRequester = minimumPriceFocusRequester,
                            onOpenCustomerLocationPicker = onOpenCustomerLocationPicker,
                            onOpenProviderLocationPicker = onOpenProviderLocationPicker,
                            onPickPhotos = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                                )
                            },
                            onReview = {
                                keyboardController?.hide()
                                viewModel.review()
                            },
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun ListServiceForm(
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
    onReview: () -> Unit,
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
            OutlinedTextField(
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
            OutlinedTextField(
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

        OutlinedTextField(
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

        OutlinedTextField(
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

        Button(
            onClick = onReview,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp)
                .testTag("list_service_review"),
        ) {
            Text(stringResource(R.string.list_service_review))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ListServicePricingEditor(
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
        ListServicePricingChoiceRow(
            modifier = Modifier.testTag("list_service_pricing_contact"),
            label = stringResource(R.string.list_service_contact_for_quote),
            selected = state.pricingMode == ListServicePricingMode.CONTACT_FOR_QUOTE,
            onClick = { viewModel.selectPricingMode(ListServicePricingMode.CONTACT_FOR_QUOTE) },
        )
        ListServicePricingChoiceRow(
            modifier = Modifier.testTag("list_service_pricing_starting"),
            label = stringResource(R.string.list_service_starting_at),
            selected = state.pricingMode == ListServicePricingMode.STARTING_AT,
            onClick = { viewModel.selectPricingMode(ListServicePricingMode.STARTING_AT) },
        )
        ListServicePricingChoiceRow(
            modifier = Modifier.testTag("list_service_pricing_fixed"),
            label = stringResource(R.string.list_service_fixed_price),
            selected = state.pricingMode == ListServicePricingMode.FIXED,
            onClick = { viewModel.selectPricingMode(ListServicePricingMode.FIXED) },
        )
        ListServicePricingChoiceRow(
            modifier = Modifier.testTag("list_service_pricing_hourly"),
            label = stringResource(R.string.list_service_hourly),
            selected = state.pricingMode == ListServicePricingMode.HOURLY,
            onClick = { viewModel.selectPricingMode(ListServicePricingMode.HOURLY) },
        )
        ListServicePricingChoiceRow(
            modifier = Modifier.testTag("list_service_pricing_visit"),
            label = stringResource(R.string.list_service_per_visit),
            selected = state.pricingMode == ListServicePricingMode.PER_VISIT,
            onClick = { viewModel.selectPricingMode(ListServicePricingMode.PER_VISIT) },
        )
        ListServicePricingChoiceRow(
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
        -> OutlinedTextField(
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
                    ListServiceError(R.string.list_service_error_price)
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
            OutlinedTextField(
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
            OutlinedTextField(
                value = state.maximumPrice,
                onValueChange = viewModel::updateMaximumPrice,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("list_service_maximum_price"),
                label = { Text(stringResource(R.string.list_service_maximum_price)) },
                supportingText = {
                    if (ListServiceValidationField.PRICE_RANGE in validationErrors) {
                        ListServiceError(R.string.list_service_error_price_range)
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
private fun ListServiceMoreDetailsEditor(
    state: ListServiceFormState,
    viewModel: ListServiceViewModel,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        ListServiceOptionalTextField(
            value = state.availability,
            labelRes = R.string.list_service_availability,
            testTag = "list_service_availability",
            onValueChange = viewModel::updateAvailability,
        )
        ListServiceOptionalTextField(
            value = state.experience,
            labelRes = R.string.list_service_experience,
            testTag = "list_service_experience",
            onValueChange = viewModel::updateExperience,
        )
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
private fun ListServiceOptionalTextField(
    value: String,
    @StringRes labelRes: Int,
    testTag: String,
    onValueChange: (String) -> Unit,
) {
    OutlinedTextField(
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
private fun ListServiceCoverageChips(
    selectedRadius: Int?,
    onSelectRadius: (Int) -> Unit,
) {
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
private fun ListServiceAreaButton(
    areaLabel: String,
    onClick: () -> Unit,
    testTag: String,
) {
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
private fun PortfolioPhotoList(
    uris: List<String>,
    onRemovePhoto: (String) -> Unit,
) {
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ListServiceReview(
    modifier: Modifier = Modifier,
    state: ListServiceFormState,
    onEdit: () -> Unit,
    onComplete: () -> Unit,
) {
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
        Button(
            onClick = onComplete,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp)
                .testTag("list_service_complete"),
        ) {
            Text(stringResource(R.string.list_service_complete_draft))
        }
    }
}

@Composable
private fun serviceCategoryLabel(state: ListServiceFormState): String = when {
    state.categoryId == LIST_SERVICE_OTHER_CATEGORY_ID -> state.customCategory.ifBlank {
        stringResource(R.string.list_service_other_category)
    }

    else -> ASKIT_SERVICE_CATEGORIES
        .firstOrNull { it.id == state.categoryId }
        ?.let { category -> stringResource(category.labelRes) }
        ?: stringResource(R.string.list_service_select_category)
}

@Composable
private fun serviceDeliveryLabels(state: ListServiceFormState): List<String> = buildList {
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
private fun servicePricingLabel(state: ListServiceFormState): String? = when (state.pricingMode) {
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
private fun ListServiceSectionHeading(@StringRes titleRes: Int) {
    Text(
        text = stringResource(titleRes),
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.semantics { heading() },
    )
}

@Composable
private fun ListServiceSubheading(@StringRes titleRes: Int) {
    Text(
        text = stringResource(titleRes),
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.semantics { heading() },
    )
}

@Composable
private fun ListServiceSummaryRow(@StringRes labelRes: Int, value: String) {
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
private fun ListServiceSummaryBlock(@StringRes labelRes: Int, value: String) {
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
private fun ListServiceCheckboxRow(
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
private fun ListServicePricingChoiceRow(
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
private fun ListServiceOptionalSectionRow(
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
private fun ListServiceError(@StringRes messageRes: Int) {
    val message = stringResource(messageRes)
    Text(
        text = message,
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.semantics { error(message) },
    )
}

private fun firstInvalidField(
    errors: Set<ListServiceValidationField>,
): ListServiceValidationField = listOf(
    ListServiceValidationField.CATEGORY,
    ListServiceValidationField.TITLE,
    ListServiceValidationField.DESCRIPTION,
    ListServiceValidationField.DELIVERY_MODE,
    ListServiceValidationField.CUSTOMER_LOCATION,
    ListServiceValidationField.COVERAGE_RADIUS,
    ListServiceValidationField.PROVIDER_LOCATION,
    ListServiceValidationField.PRICE,
    ListServiceValidationField.PRICE_RANGE,
).first { it in errors }
