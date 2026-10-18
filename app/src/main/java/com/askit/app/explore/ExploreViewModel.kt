package com.askit.app.explore

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private val DEFAULT_SEARCH_AREA = ExploreSearchArea(
    placeId = null,
    displayName = "Kallakurichi",
    supportingText = "Tamil Nadu", // i18n-ignore: fixture place name
    latitude = 11.7401,
    longitude = 78.9597,
    radiusKm = 10,
    source = ExploreLocationSource.SAVED,
)

enum class ExploreLocationSource {
    SAVED,
    GOOGLE_PLACES,
    CURRENT_LOCATION,
    ;

    companion object {
        fun fromName(value: String): ExploreLocationSource = entries.firstOrNull { it.name == value } ?: SAVED
    }
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
    private val contentSource: ExploreContentSource,
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
    private val filterScopeState = MutableStateFlow(
        savedStateHandle.get<String>(FILTER_SCOPE_KEY)
            ?.let { value -> ExploreResultScope.entries.firstOrNull { it.name == value } }
            ?: ExploreResultScope.All,
    )
    private val appliedFilterOptionsState = MutableStateFlow(
        ExploreResultScope.entries.associateWith { scope ->
            savedStateHandle.get<ArrayList<String>>(filterOptionsKey(scope))
                .orEmpty()
                .mapNotNull { name -> ExploreFilterOption.entries.firstOrNull { it.name == name } }
                .toSet()
        }.filterValues { it.isNotEmpty() },
    )

    val filterScope: StateFlow<ExploreResultScope> = filterScopeState.asStateFlow()
    val appliedFilterOptions: StateFlow<Map<ExploreResultScope, Set<ExploreFilterOption>>> =
        appliedFilterOptionsState.asStateFlow()

    private val priceFiltersState = MutableStateFlow(
        ExploreResultScope.entries.associateWith { scope ->
            ExplorePriceFilter(
                minimum = savedStateHandle.get<String>("explore_price_min_${scope.name}").orEmpty(),
                maximum = savedStateHandle.get<String>("explore_price_max_${scope.name}").orEmpty(),
                unit = savedStateHandle.get<String>("explore_price_unit_${scope.name}")
                    ?.let { name -> ExploreRateUnit.entries.firstOrNull { it.name == name } },
            )
        }.filterValues { it.isValid && it.isActive },
    )
    val priceFilters = priceFiltersState.asStateFlow()

    fun onPriceFilterChanged(scope: ExploreResultScope, filter: ExplorePriceFilter) {
        if (!filter.isValid) return
        val normalized = if (scope == ExploreResultScope.Tasks) filter.copy(unit = null) else filter
        priceFiltersState.value = priceFiltersState.value + (scope to normalized)
        savedStateHandle["explore_price_min_${scope.name}"] = normalized.minimum
        savedStateHandle["explore_price_max_${scope.name}"] = normalized.maximum
        savedStateHandle["explore_price_unit_${scope.name}"] = normalized.unit?.name
    }

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
            source = ExploreLocationSource.fromName(source),
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

    private val resultStateFlow = MutableStateFlow<ExploreResultState>(ExploreResultState.Loading)

    /** Remote search results (people and services). Local tasks are merged in by the screen. */
    val resultState: StateFlow<ExploreResultState> = resultStateFlow.asStateFlow()

    private val browseStateFlow = MutableStateFlow(
        ExploreBrowseState(
            services = ExploreBrowseStatus.Loading,
            professionals = ExploreBrowseStatus.Loading,
            tasks = ExploreBrowseStatus.Loading,
        ),
    )
    val browseState: StateFlow<ExploreBrowseState> = browseStateFlow.asStateFlow()

    /** The sort options each tab supports; tabs with fewer than two show no sort control. */
    val availableSortOptions: Map<ExploreResultScope, List<ExploreSortOption>> = exploreAvailableSortOptions()

    private val sortOptionsState = MutableStateFlow(
        ExploreResultScope.entries.mapNotNull { scope ->
            savedStateHandle.get<String>(sortKey(scope))
                ?.let { name -> ExploreSortOption.entries.firstOrNull { it.name == name } }
                ?.takeIf { it in availableSortOptions[scope].orEmpty() }
                ?.let { scope to it }
        }.toMap(),
    )

    /** The selected sort per tab. Survives rotation and process death; reset by a new query. */
    val selectedSortOptions: StateFlow<Map<ExploreResultScope, ExploreSortOption>> = sortOptionsState.asStateFlow()

    private var searchJob: Job? = null
    private val browseJobs = mutableMapOf<ExploreBrowseSection, Job>()

    init {
        search(showLoading = true)
        ExploreBrowseSection.entries.forEach(::loadBrowseSection)
    }

    /**
     * Runs the search again for the current query and area, keeping filters, price filters and
     * sort (they apply on the device). A blocking failure shows the loading body while it runs;
     * results with a partial-failure, offline or stale banner keep their rows and show the
     * refreshing indicator instead.
     */
    fun retryResults() {
        search(showLoading = resultStateFlow.value !is ExploreResultState.Results)
    }

    /** Reloads one browse section, showing its skeleton while it runs. */
    fun retryBrowseSection(section: ExploreBrowseSection) {
        loadBrowseSection(section)
    }

    fun onSortChanged(scope: ExploreResultScope, option: ExploreSortOption) {
        if (option !in availableSortOptions[scope].orEmpty()) return
        sortOptionsState.update { it + (scope to option) }
        savedStateHandle[sortKey(scope)] = option.name
    }

    private fun currentSearchArea() = ExploreSearchArea(
        placeId = searchAreaPlaceIdState.value,
        displayName = searchAreaNameState.value,
        supportingText = searchAreaSupportingTextState.value,
        latitude = searchAreaLatitudeState.value,
        longitude = searchAreaLongitudeState.value,
        radiusKm = searchAreaRadiusState.value,
        source = ExploreLocationSource.fromName(searchAreaSourceState.value),
    )

    private fun currentRequest() = ExploreSearchRequest(normalizeExploreQuery(queryState.value), currentSearchArea())

    private fun search(showLoading: Boolean) {
        val request = currentRequest()
        searchJob?.cancel()
        resultStateFlow.update { current ->
            if (showLoading || current !is ExploreResultState.Results) {
                ExploreResultState.Loading
            } else {
                current.copy(isRefreshing = true)
            }
        }
        searchJob = viewModelScope.launch {
            resultStateFlow.value = contentSource.search(request)
        }
    }

    private fun loadBrowseSection(section: ExploreBrowseSection) {
        val searchArea = currentSearchArea()
        browseJobs[section]?.cancel()
        browseStateFlow.update { it.withStatus(section, ExploreBrowseStatus.Loading) }
        browseJobs[section] = viewModelScope.launch {
            val status = contentSource.loadBrowseSection(section, searchArea)
            browseStateFlow.update { it.withStatus(section, status) }
        }
    }

    fun onQueryChanged(query: String) {
        savedStateHandle[QUERY_KEY] = query
    }

    fun onQueryCleared() {
        savedStateHandle[QUERY_KEY] = ""
    }

    fun submitQuery(query: String) {
        val normalizedQuery = normalizeExploreQuery(query)
        val previousSubmitted = savedStateHandle.get<String>(SUBMITTED_QUERY_KEY)
        savedStateHandle[QUERY_KEY] = normalizedQuery
        if (normalizedQuery.isEmpty()) return
        if (!normalizedQuery.equals(previousSubmitted, ignoreCase = true)) {
            // A new query starts from relevance, like the result tab does.
            savedStateHandle[SUBMITTED_QUERY_KEY] = normalizedQuery
            clearSortOptions()
            search(showLoading = true)
        }

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
        search(showLoading = true)
        ExploreBrowseSection.entries.forEach(::loadBrowseSection)
    }

    private fun clearSortOptions() {
        sortOptionsState.value = emptyMap()
        ExploreResultScope.entries.forEach { savedStateHandle.remove<String>(sortKey(it)) }
    }

    fun onFilterScopeSelected(scope: ExploreResultScope) {
        filterScopeState.value = scope
        savedStateHandle[FILTER_SCOPE_KEY] = scope.name
    }

    fun onFiltersChanged(scope: ExploreResultScope, options: Set<ExploreFilterOption>) {
        val normalized = normalizeAppliedExploreFilterOptions(scope, defaultExploreFilterOptions()[scope].orEmpty(), options)
        val updated = appliedFilterOptionsState.value.toMutableMap()
        if (normalized.isEmpty()) {
            updated.remove(scope)
        } else {
            updated[scope] = normalized
        }
        appliedFilterOptionsState.value = updated.toMap()
        savedStateHandle[filterOptionsKey(scope)] = ArrayList(normalized.map { it.name }.sorted())
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
        const val FILTER_SCOPE_KEY = "explore_filter_scope"
        const val SUBMITTED_QUERY_KEY = "explore_submitted_query"

        fun sortKey(scope: ExploreResultScope): String = "explore_sort_${scope.name.lowercase()}"

        fun filterOptionsKey(scope: ExploreResultScope): String = "explore_filter_options_${scope.name.lowercase()}"
    }
}

internal fun normalizeExploreQuery(query: String): String = query.trim().replace(Regex("\\s+"), " ")
