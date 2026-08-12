package com.askit.app.explore

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import androidx.annotation.StringRes
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.askit.app.R
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.AddressComponent
import com.google.android.libraries.places.api.model.CircularBounds
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.PlaceTypes
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.PlaceAutocomplete
import com.google.android.libraries.places.widget.model.AutocompleteListDensity
import com.google.android.libraries.places.widget.model.AutocompleteUiCustomization
import com.google.android.libraries.places.widget.model.AutocompleteUiIcon
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

private val SEARCH_RADII_KM = listOf(5, 10, 25, 50)

@Composable
fun SearchAreaRoute(
    viewModel: ExploreViewModel,
    onBack: () -> Unit,
    availableFilterOptions: Map<ExploreResultScope, List<ExploreFilterOption>> = emptyMap(),
    appliedFilterOptions: Map<ExploreResultScope, Set<ExploreFilterOption>> = emptyMap(),
    onFiltersChanged: ((ExploreResultScope, Set<ExploreFilterOption>) -> Unit)? = null,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val filterScope by viewModel.filterScope.collectAsStateWithLifecycle()
    val initialAvailableOptions = normalizeAvailableExploreFilterOptions(
        scope = filterScope,
        options = availableFilterOptions[filterScope].orEmpty(),
    )
    val initialAppliedOptions = normalizeAppliedExploreFilterOptions(
        scope = filterScope,
        availableOptions = initialAvailableOptions,
        appliedOptions = appliedFilterOptions[filterScope].orEmpty(),
    )
    SearchAreaScreen(
        confirmedArea = uiState.searchArea,
        filterScope = filterScope,
        availableFilterOptions = availableFilterOptions,
        appliedFilterOptions = appliedFilterOptions,
        onBack = onBack,
        onApply = { searchArea, optionalFilterOptions ->
            viewModel.onSearchAreaApplied(searchArea)
            if (optionalFilterOptions != initialAppliedOptions) {
                onFiltersChanged?.invoke(filterScope, optionalFilterOptions)
            }
            onBack()
        },
    )
}

@Composable
fun SearchAreaScreen(
    confirmedArea: ExploreSearchArea,
    onBack: () -> Unit,
    onApply: (ExploreSearchArea) -> Unit,
) {
    SearchAreaScreen(
        confirmedArea = confirmedArea,
        onBack = onBack,
        onApply = { searchArea, _ -> onApply(searchArea) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchAreaScreen(
    confirmedArea: ExploreSearchArea,
    onBack: () -> Unit,
    filterScope: ExploreResultScope = ExploreResultScope.All,
    availableFilterOptions: Map<ExploreResultScope, List<ExploreFilterOption>> = emptyMap(),
    appliedFilterOptions: Map<ExploreResultScope, Set<ExploreFilterOption>> = emptyMap(),
    @StringRes titleRes: Int = R.string.explore_filters_title,
    showFilterControls: Boolean = true,
    onApply: (ExploreSearchArea, Set<ExploreFilterOption>) -> Unit,
) {
    var displayName by rememberSaveable(confirmedArea.displayName) { mutableStateOf(confirmedArea.displayName) }
    var supportingText by rememberSaveable(confirmedArea.displayName) { mutableStateOf(confirmedArea.supportingText) }
    var placeId by rememberSaveable(confirmedArea.displayName) { mutableStateOf(confirmedArea.placeId) }
    var latitude by rememberSaveable(confirmedArea.displayName) { mutableStateOf(confirmedArea.latitude) }
    var longitude by rememberSaveable(confirmedArea.displayName) { mutableStateOf(confirmedArea.longitude) }
    var radiusKm by rememberSaveable(confirmedArea.displayName) { mutableIntStateOf(confirmedArea.radiusKm) }
    var sourceName by rememberSaveable(confirmedArea.displayName) { mutableStateOf(confirmedArea.source.name) }
    val availableOptions = remember(filterScope, availableFilterOptions[filterScope], showFilterControls) {
        if (showFilterControls) {
            normalizeAvailableExploreFilterOptions(
                scope = filterScope,
                options = availableFilterOptions[filterScope].orEmpty(),
            )
        } else {
            emptyList()
        }
    }
    val initialOptionalOptions = remember(filterScope, availableOptions, appliedFilterOptions[filterScope]) {
        normalizeAppliedExploreFilterOptions(
            scope = filterScope,
            availableOptions = availableOptions,
            appliedOptions = appliedFilterOptions[filterScope].orEmpty(),
        )
    }
    val availableOptionNames = availableOptions.map(ExploreFilterOption::name)
    val availableOptionKey = availableOptionNames.joinToString(separator = "|")
    val initialOptionalOptionNames = availableOptions
        .filter { it in initialOptionalOptions }
        .map(ExploreFilterOption::name)
    var draftOptionalOptionNames by rememberSaveable(
        filterScope,
        availableOptionKey,
    ) {
        mutableStateOf(initialOptionalOptionNames)
    }
    val draftOptionalOptions = availableOptions
        .filter { it.name in draftOptionalOptionNames }
        .toSet()
    var isResolvingLocation by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val context = androidx.compose.ui.platform.LocalContext.current
    val resources = LocalResources.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var locationRequest by remember { mutableStateOf<CancellationTokenSource?>(null) }

    fun applyDraft(searchArea: ExploreSearchArea) {
        displayName = searchArea.displayName
        supportingText = searchArea.supportingText
        placeId = searchArea.placeId
        latitude = searchArea.latitude
        longitude = searchArea.longitude
        sourceName = searchArea.source.name
    }

    fun currentDraft(): ExploreSearchArea = ExploreSearchArea(
        placeId = placeId,
        displayName = displayName,
        supportingText = supportingText,
        latitude = latitude,
        longitude = longitude,
        radiusKm = radiusKm,
        source = ExploreLocationSource.fromName(sourceName),
    )

    @SuppressLint("MissingPermission")
    fun resolveCurrentLocation() {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            isResolvingLocation = false
            errorMessage = resources.getString(R.string.explore_location_permission_denied)
            return
        }
        locationRequest?.cancel()
        val cancellationTokenSource = CancellationTokenSource()
        locationRequest = cancellationTokenSource
        isResolvingLocation = true
        errorMessage = null
        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_BALANCED_POWER_ACCURACY,
            cancellationTokenSource.token,
        ).addOnSuccessListener { location ->
            if (location == null) {
                isResolvingLocation = false
                errorMessage = resources.getString(R.string.explore_current_location_unavailable)
                return@addOnSuccessListener
            }
            coroutineScope.launch {
                val locality = reverseGeocode(context, location)
                applyDraft(
                    ExploreSearchArea(
                        placeId = null,
                        displayName = locality?.displayName
                            ?: resources.getString(R.string.explore_current_area),
                        supportingText = locality?.supportingText,
                        latitude = location.latitude,
                        longitude = location.longitude,
                        radiusKm = radiusKm,
                        source = ExploreLocationSource.CURRENT_LOCATION,
                    ),
                )
                isResolvingLocation = false
            }
        }.addOnFailureListener {
            isResolvingLocation = false
            errorMessage = resources.getString(R.string.explore_current_location_unavailable)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            resolveCurrentLocation()
        } else {
            isResolvingLocation = false
            errorMessage = resources.getString(R.string.explore_location_permission_denied)
        }
    }

    fun requestCurrentLocation() {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            resolveCurrentLocation()
        } else {
            permissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
    }

    val placesClient = remember {
        if (Places.isInitialized()) {
            Places.createClient(context)
        } else {
            null
        }
    }
    val autocompleteLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode == Activity.RESULT_CANCELED) return@rememberLauncherForActivityResult
        val data = result.data ?: run {
            errorMessage = resources.getString(R.string.explore_location_search_unavailable)
            return@rememberLauncherForActivityResult
        }
        val prediction = PlaceAutocomplete.getPredictionFromIntent(data)
        val sessionToken = PlaceAutocomplete.getSessionTokenFromIntent(data)
        if (prediction == null) {
            errorMessage = resources.getString(R.string.explore_location_search_unavailable)
            return@rememberLauncherForActivityResult
        }
        fetchSelectedPlace(
            placesClient = placesClient,
            prediction = prediction,
            sessionToken = sessionToken,
            fallbackDisplayName = resources.getString(R.string.explore_selected_area),
            scope = coroutineScope,
            onSuccess = ::applyDraft,
            onFailure = { errorMessage = resources.getString(R.string.explore_could_not_load_area) },
        )
    }

    fun openPlaceSearch() {
        if (!Places.isInitialized()) {
            errorMessage = resources.getString(R.string.explore_location_search_unavailable)
            return
        }
        val builder = PlaceAutocomplete.IntentBuilder()
            .setCountries(listOf("IN"))
            .setTypesFilter(listOf(PlaceTypes.REGIONS))
            .setAutocompleteSessionToken(AutocompleteSessionToken.newInstance())
            .setAutocompleteUiCustomization(
                AutocompleteUiCustomization.Builder()
                    .listDensity(AutocompleteListDensity.MULTI_LINE)
                    .listItemIcon(AutocompleteUiIcon.noIcon())
                    .theme(R.style.AskITPlacesAutocompleteTheme)
                    .build(),
            )
        latitude?.let { currentLatitude ->
            longitude?.let { currentLongitude ->
                builder.setLocationBias(
                    CircularBounds.newInstance(
                        LatLng(currentLatitude, currentLongitude),
                        50_000.0,
                    ),
                )
            }
        }
        autocompleteLauncher.launch(builder.build(context))
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            errorMessage = null
        }
    }

    DisposableEffect(Unit) {
        onDispose { locationRequest?.cancel() }
    }

    val draft = currentDraft()
    val filterGroups = ExploreFilterGroup.entries.mapNotNull { group ->
        val options = availableOptions.filter { it.groupFor(filterScope) == group }
        options.takeIf { it.isNotEmpty() }?.let { group to it }
    }
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(titleRes)) },
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 0.dp,
            ) {
                Column {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (showFilterControls) {
                            TextButton(
                                onClick = { draftOptionalOptionNames = emptyList() },
                                enabled = draftOptionalOptions.isNotEmpty(),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                                    .testTag("explore_clear_filters"),
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = MaterialTheme.colorScheme.secondary,
                                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                ),
                            ) {
                                Text(stringResource(R.string.explore_clear_filters))
                            }
                            Spacer(Modifier.size(8.dp))
                        }
                        Button(
                            onClick = { onApply(draft, draftOptionalOptions) },
                            enabled = draft.isUsable && !isResolvingLocation,
                            modifier = if (showFilterControls) {
                                Modifier.heightIn(min = 48.dp)
                            } else {
                                Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp)
                            },
                        ) {
                            Text(stringResource(R.string.explore_apply))
                        }
                    }
                }
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("explore_filters_content"),
            contentPadding = PaddingValues(
                start = 16.dp,
                top = padding.calculateTopPadding() + 16.dp,
                end = 16.dp,
                bottom = padding.calculateBottomPadding() + 24.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            if (showFilterControls) {
                item {
                    Text(
                        text = stringResource(
                            R.string.explore_filter_scope_caption,
                            stringResource(filterScope.labelRes),
                        ),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.semantics { heading() },
                    )
                }
            }
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                ) {
                    Column {
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                text = stringResource(R.string.explore_selected_area),
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.semantics { heading() },
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = draft.displayName,
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            draft.supportingText?.let { areaSupportingText ->
                                Text(
                                    text = areaSupportingText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        ListItem(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 56.dp)
                                .clickable(
                                    role = Role.Button,
                                    onClickLabel = stringResource(R.string.explore_use_current_location),
                                    onClick = ::requestCurrentLocation,
                                ),
                            leadingContent = {
                                Icon(
                                    painter = painterResource(R.drawable.ic_my_location),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            },
                            headlineContent = { Text(stringResource(R.string.explore_use_current_location)) },
                            trailingContent = {
                                if (isResolvingLocation) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = MaterialTheme.colorScheme.secondary,
                                        strokeWidth = 2.dp,
                                    )
                                }
                            },
                            colors = ListItemDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            ),
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        ListItem(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 56.dp)
                                .clickable(
                                    role = Role.Button,
                                    onClickLabel = stringResource(R.string.explore_search_another_area),
                                    onClick = ::openPlaceSearch,
                                ),
                            leadingContent = {
                                Icon(
                                    painter = painterResource(R.drawable.ic_location_on),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            },
                            headlineContent = { Text(stringResource(R.string.explore_search_another_area)) },
                            colors = ListItemDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            ),
                        )
                    }
                }
            }
            if (showFilterControls) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                        ) {
                            Text(
                                text = stringResource(R.string.explore_search_distance),
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                                    .semantics { heading() },
                            )
                            Column(Modifier.selectableGroup()) {
                                SEARCH_RADII_KM.forEach { radius ->
                                    val radiusDescription = stringResource(R.string.explore_within_km, radius)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(min = 56.dp)
                                            .selectable(
                                                selected = radiusKm == radius,
                                                onClick = { radiusKm = radius },
                                                role = Role.RadioButton,
                                            )
                                            .semantics {
                                                contentDescription = radiusDescription
                                            }
                                            .padding(horizontal = 16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        RadioButton(
                                            selected = radiusKm == radius,
                                            onClick = null,
                                            colors = RadioButtonDefaults.colors(
                                                selectedColor = MaterialTheme.colorScheme.secondary,
                                                unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                            ),
                                        )
                                        Spacer(Modifier.size(12.dp))
                                        Text(
                                            text = radiusDescription,
                                            style = MaterialTheme.typography.bodyLarge,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            filterGroups.forEach { (group, options) ->
                item(key = "explore_filter_group_${group.name}") {
                    ExploreFilterGroupSection(
                        group = group,
                        options = options,
                        selectedOptions = draftOptionalOptions,
                        onOptionToggled = { option ->
                            draftOptionalOptionNames = if (option.name in draftOptionalOptionNames) {
                                draftOptionalOptionNames.filterNot { it == option.name }
                            } else {
                                draftOptionalOptionNames + option.name
                            }
                        },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ExploreFilterGroupSection(
    group: ExploreFilterGroup,
    options: List<ExploreFilterOption>,
    selectedOptions: Set<ExploreFilterOption>,
    onOptionToggled: (ExploreFilterOption) -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("explore_filter_group_${group.name.lowercase()}"),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(group.labelRes),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.semantics { heading() },
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                options.forEach { option ->
                    val isSelected = option in selectedOptions
                    FilterChip(
                        selected = isSelected,
                        onClick = { onOptionToggled(option) },
                        modifier = Modifier.testTag("explore_filter_option_${option.name.lowercase()}"),
                        label = { Text(stringResource(option.labelRes())) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = MaterialTheme.colorScheme.onSurface,
                            iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        ),
                        leadingIcon = if (isSelected) {
                            {
                                Icon(
                                    painter = painterResource(R.drawable.ic_check),
                                    contentDescription = null,
                                )
                            }
                        } else {
                            null
                        },
                    )
                }
            }
        }
    }
}

private fun fetchSelectedPlace(
    placesClient: PlacesClient?,
    prediction: AutocompletePrediction,
    sessionToken: AutocompleteSessionToken?,
    fallbackDisplayName: String,
    scope: CoroutineScope,
    onSuccess: (ExploreSearchArea) -> Unit,
    onFailure: () -> Unit,
) {
    if (placesClient == null) {
        onFailure()
        return
    }
    val requestBuilder = FetchPlaceRequest.builder(
        prediction.placeId,
        listOf(
            Place.Field.DISPLAY_NAME,
            Place.Field.FORMATTED_ADDRESS,
            Place.Field.ADDRESS_COMPONENTS,
            Place.Field.LOCATION,
        ),
    )
    sessionToken?.let(requestBuilder::setSessionToken)
    placesClient.fetchPlace(requestBuilder.build())
        .addOnSuccessListener { response ->
            val place = response.place
            val location = place.location
            if (location == null) {
                onFailure()
                return@addOnSuccessListener
            }
            scope.launch {
                onSuccess(place.toExploreSearchArea(prediction.placeId, location, fallbackDisplayName))
            }
        }
        .addOnFailureListener { onFailure() }
}

private fun Place.toExploreSearchArea(
    placeId: String,
    location: LatLng,
    fallbackDisplayName: String,
): ExploreSearchArea {
    val components = addressComponents?.asList().orEmpty()
    val locality = components.componentName("locality")
    val district = components.componentName("administrative_area_level_2")
    val state = components.componentName("administrative_area_level_1")
    val country = components.componentName("country")
    val displayName = locality ?: district ?: state ?: fallbackDisplayName
    val supportingText = listOf(district, state, country)
        .filterNotNull()
        .filter { it != displayName }
        .distinct()
        .joinToString(", ")
        .ifBlank { null }
    return ExploreSearchArea(
        placeId = placeId,
        displayName = displayName,
        supportingText = supportingText,
        latitude = location.latitude,
        longitude = location.longitude,
        radiusKm = 10,
        source = ExploreLocationSource.GOOGLE_PLACES,
    )
}

private fun List<AddressComponent>.componentName(type: String): String? =
    firstOrNull { type in it.types }?.name

private data class LocalityLabel(
    val displayName: String,
    val supportingText: String?,
)

private suspend fun reverseGeocode(context: Context, location: Location): LocalityLabel? {
    if (!Geocoder.isPresent()) return null
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            suspendCancellableCoroutine { continuation ->
                Geocoder(context).getFromLocation(
                    location.latitude,
                    location.longitude,
                    1,
                    object : Geocoder.GeocodeListener {
                        override fun onGeocode(addresses: MutableList<Address>) {
                            if (continuation.isActive) {
                                continuation.resume(addresses.firstOrNull().toLocalityLabel()) { _, _, _ -> }
                            }
                        }

                        override fun onError(errorMessage: String?) {
                            if (continuation.isActive) continuation.resume(null) { _, _, _ -> }
                        }
                    },
                )
            }
        } else {
            withContext(Dispatchers.IO) {
                @Suppress("DEPRECATION")
                Geocoder(context).getFromLocation(location.latitude, location.longitude, 1)
                    ?.firstOrNull()
                    .toLocalityLabel()
            }
        }
    } catch (_: Exception) {
        null
    }
}

private fun Address?.toLocalityLabel(): LocalityLabel? {
    if (this == null) return null
    val displayName = locality ?: subAdminArea ?: adminArea ?: return null
    val supportingText = listOf(adminArea, countryName)
        .filterNotNull()
        .filter { it != displayName }
        .distinct()
        .joinToString(", ")
        .ifBlank { null }
    return LocalityLabel(displayName, supportingText)
}
