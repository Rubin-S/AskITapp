package com.askit.app.explore

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.askit.app.R
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.AddressComponent
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ExploreFilterGroupSection(
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

internal fun fetchSelectedPlace(
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

internal fun Place.toExploreSearchArea(placeId: String, location: LatLng, fallbackDisplayName: String): ExploreSearchArea {
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

internal fun List<AddressComponent>.componentName(type: String): String? = firstOrNull { type in it.types }?.name

internal data class LocalityLabel(val displayName: String, val supportingText: String?)

internal suspend fun reverseGeocode(context: Context, location: Location): LocalityLabel? {
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

internal fun Address?.toLocalityLabel(): LocalityLabel? {
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
