package com.askit.app.explore

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

private val DEFAULT_SEARCH_AREA = ExploreSearchArea(
    placeId = null,
    displayName = "Kallakurichi",
    supportingText = "Tamil Nadu",
    latitude = 11.7401,
    longitude = 78.9597,
    radiusKm = 10,
    source = ExploreLocationSource.SAVED,
)

enum class ExploreLocationSource {
    SAVED,
    GOOGLE_PLACES,
    CURRENT_LOCATION,
}

data class ExploreSearchArea(
    val placeId: String?,
    val displayName: String,
    val supportingText: String?,
    val latitude: Double?,
    val longitude: Double?,
    val radiusKm: Int,
    val source: ExploreLocationSource,
) {
    val isUsable: Boolean
        get() = displayName.isNotBlank() && latitude != null && longitude != null
}

data class ExploreUiState(
    val query: String = "",
    val searchArea: ExploreSearchArea = DEFAULT_SEARCH_AREA,
    val recentSearches: List<String> = emptyList(),
)

class ExploreViewModel(
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val queryState = savedStateHandle.getStateFlow(QUERY_KEY, "")
    private val recentSearchesState = savedStateHandle.getStateFlow(
        RECENT_SEARCHES_KEY,
        emptyList<String>(),
    )
    private val searchAreaNameState = savedStateHandle.getStateFlow(
        SEARCH_AREA_NAME_KEY,
        DEFAULT_SEARCH_AREA.displayName,
    )
    private val searchAreaSupportingTextState = savedStateHandle.getStateFlow<String?>(
        SEARCH_AREA_SUPPORTING_TEXT_KEY,
        DEFAULT_SEARCH_AREA.supportingText,
    )
    private val searchAreaPlaceIdState = savedStateHandle.getStateFlow<String?>(
        SEARCH_AREA_PLACE_ID_KEY,
        DEFAULT_SEARCH_AREA.placeId,
    )
    private val searchAreaLatitudeState = savedStateHandle.getStateFlow<Double?>(
        SEARCH_AREA_LATITUDE_KEY,
        DEFAULT_SEARCH_AREA.latitude,
    )
    private val searchAreaLongitudeState = savedStateHandle.getStateFlow<Double?>(
        SEARCH_AREA_LONGITUDE_KEY,
        DEFAULT_SEARCH_AREA.longitude,
    )
    private val searchAreaRadiusState = savedStateHandle.getStateFlow(
        SEARCH_AREA_RADIUS_KEY,
        DEFAULT_SEARCH_AREA.radiusKm,
    )
    private val searchAreaSourceState = savedStateHandle.getStateFlow(
        SEARCH_AREA_SOURCE_KEY,
        DEFAULT_SEARCH_AREA.source.name,
    )

    private val searchAreaBaseState = combine(
        searchAreaNameState,
        searchAreaSupportingTextState,
        searchAreaPlaceIdState,
        searchAreaLatitudeState,
        searchAreaLongitudeState,
    ) { name, supportingText, placeId, latitude, longitude ->
        ExploreSearchArea(
            placeId = placeId,
            displayName = name,
            supportingText = supportingText,
            latitude = latitude,
            longitude = longitude,
            radiusKm = DEFAULT_SEARCH_AREA.radiusKm,
            source = DEFAULT_SEARCH_AREA.source,
        )
    }
    private val searchAreaState = combine(
        searchAreaBaseState,
        searchAreaRadiusState,
        searchAreaSourceState,
    ) { searchArea, radiusKm, source ->
        searchArea.copy(
            radiusKm = radiusKm,
            source = source.toExploreLocationSource(),
        )
    }

    val uiState: StateFlow<ExploreUiState> = combine(
        queryState,
        searchAreaState,
        recentSearchesState,
        ::ExploreUiState,
    )
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = ExploreUiState(
                query = queryState.value,
                searchArea = DEFAULT_SEARCH_AREA,
                recentSearches = recentSearchesState.value,
            ),
        )

    fun onQueryChanged(query: String) {
        savedStateHandle[QUERY_KEY] = query
    }

    fun onQueryCleared() {
        savedStateHandle[QUERY_KEY] = ""
    }

    fun submitQuery(query: String) {
        val normalizedQuery = normalizeExploreQuery(query)
        savedStateHandle[QUERY_KEY] = normalizedQuery
        if (normalizedQuery.isEmpty()) return

        val updatedHistory = buildList {
            add(normalizedQuery)
            recentSearchesState.value
                .filterNot { it.equals(normalizedQuery, ignoreCase = true) }
                .take(MAX_RECENT_SEARCHES - 1)
                .let(::addAll)
        }
        savedStateHandle[RECENT_SEARCHES_KEY] = ArrayList(updatedHistory)
    }

    fun removeRecentSearch(query: String) {
        savedStateHandle[RECENT_SEARCHES_KEY] = ArrayList(
            recentSearchesState.value.filterNot { it == query },
        )
    }

    fun clearRecentSearches() {
        savedStateHandle[RECENT_SEARCHES_KEY] = arrayListOf<String>()
    }

    fun onSearchAreaApplied(searchArea: ExploreSearchArea) {
        savedStateHandle[SEARCH_AREA_NAME_KEY] = searchArea.displayName
        savedStateHandle[SEARCH_AREA_SUPPORTING_TEXT_KEY] = searchArea.supportingText
        savedStateHandle[SEARCH_AREA_PLACE_ID_KEY] = searchArea.placeId
        savedStateHandle[SEARCH_AREA_LATITUDE_KEY] = searchArea.latitude
        savedStateHandle[SEARCH_AREA_LONGITUDE_KEY] = searchArea.longitude
        savedStateHandle[SEARCH_AREA_RADIUS_KEY] = searchArea.radiusKm
        savedStateHandle[SEARCH_AREA_SOURCE_KEY] = searchArea.source.name
    }

    private companion object {
        const val QUERY_KEY = "explore_query"
        const val RECENT_SEARCHES_KEY = "explore_recent_searches"
        const val MAX_RECENT_SEARCHES = 4
        const val SEARCH_AREA_NAME_KEY = "explore_search_area_name"
        const val SEARCH_AREA_SUPPORTING_TEXT_KEY = "explore_search_area_supporting_text"
        const val SEARCH_AREA_PLACE_ID_KEY = "explore_search_area_place_id"
        const val SEARCH_AREA_LATITUDE_KEY = "explore_search_area_latitude"
        const val SEARCH_AREA_LONGITUDE_KEY = "explore_search_area_longitude"
        const val SEARCH_AREA_RADIUS_KEY = "explore_search_area_radius"
        const val SEARCH_AREA_SOURCE_KEY = "explore_search_area_source"

    }
}

internal fun normalizeExploreQuery(query: String): String =
    query.trim().replace(Regex("\\s+"), " ")

private fun String.toExploreLocationSource(): ExploreLocationSource =
    runCatching { ExploreLocationSource.valueOf(this) }
        .getOrDefault(ExploreLocationSource.SAVED)
