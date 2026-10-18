package com.askit.app.listservice

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.askit.app.R
import com.askit.app.explore.ExploreLocationSource
import com.askit.app.explore.ExploreSearchArea
import com.askit.app.explore.SearchAreaScreen
import com.askit.app.media.persistPhotoPickerReadAccess
import com.askit.app.posttask.focusAfterFrame
import com.askit.designsystem.dialogs.AskITAlertDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListServiceRoute(
    viewModel: ListServiceViewModel,
    onBack: () -> Unit,
    profile: com.askit.app.session.SessionProfile,
    onCompleteDraft: (ListServiceDraft) -> Unit = {},
) {
    val state by viewModel.formState.collectAsStateWithLifecycle()
    LaunchedEffect(profile.accountId) {
        if (state.contactPhone.isBlank() && state.contactEmail.isBlank()) {
            viewModel.updateContact(profile.phoneNumber, profile.email.orEmpty())
        }
    }
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
        AskITAlertDialog(
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
                    Text(stringResource(R.string.list_service_discard), color = MaterialTheme.colorScheme.error)
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
            // review() may have just expanded the section that owns the error; let it lay out
            // before bringing it into view.
            withFrameNanos { }
            when (firstInvalidField(validationErrors)) {
                ListServiceValidationField.PROFESSIONAL_DETAILS -> listState.scrollToItem(0)

                ListServiceValidationField.CATEGORY -> categoryBringIntoView.bringIntoView()

                ListServiceValidationField.TITLE -> {
                    titleBringIntoView.bringIntoView()
                    focusAfterFrame(titleFocusRequester)
                }

                ListServiceValidationField.DESCRIPTION -> {
                    descriptionBringIntoView.bringIntoView()
                    focusAfterFrame(descriptionFocusRequester)
                }

                ListServiceValidationField.DELIVERY_MODE -> deliveryBringIntoView.bringIntoView()

                ListServiceValidationField.CUSTOMER_LOCATION,
                ListServiceValidationField.COVERAGE_RADIUS,
                -> customerLocationBringIntoView.bringIntoView()

                ListServiceValidationField.PROVIDER_LOCATION -> providerLocationBringIntoView.bringIntoView()

                ListServiceValidationField.PRICE -> {
                    priceBringIntoView.bringIntoView()
                    focusAfterFrame(priceFocusRequester)
                }

                ListServiceValidationField.PRICE_RANGE -> {
                    priceRangeBringIntoView.bringIntoView()
                    focusAfterFrame(minimumPriceFocusRequester)
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
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.surface) {
                Column(Modifier.navigationBarsPadding()) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Button(
                        onClick = {
                            if (state.screenMode == ListServiceScreenMode.REVIEW) {
                                viewModel.buildValidatedDraft()?.let(onCompleteDraft)
                            } else {
                                keyboardController?.hide()
                                viewModel.review()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .heightIn(min = 52.dp)
                            .testTag(
                                if (state.screenMode == ListServiceScreenMode.REVIEW) {
                                    "list_service_complete"
                                } else {
                                    "list_service_review"
                                },
                            ),
                    ) {
                        Text(
                            stringResource(
                                if (state.screenMode == ListServiceScreenMode.REVIEW) {
                                    R.string.list_service_complete_draft
                                } else {
                                    R.string.list_service_review
                                },
                            ),
                        )
                    }
                }
            }
        },
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding(),
            contentPadding = PaddingValues(16.dp),
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
                        )
                    }
                }
            }
        }
    }
}
