package com.askit.app.listservice

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.math.BigDecimal

enum class ListServiceDeliveryMode {
    AT_CUSTOMER_LOCATION,
    AT_PROVIDER_LOCATION,
    REMOTE,
}

enum class ListServicePricingMode {
    CONTACT_FOR_QUOTE,
    STARTING_AT,
    FIXED,
    HOURLY,
    PER_VISIT,
    RANGE,
}

enum class ListServiceOptionalSection {
    PRICING,
    MORE_DETAILS,
}

enum class ListServiceScreenMode {
    FORM,
    REVIEW,
}

enum class ListServiceValidationField {
    CATEGORY,
    TITLE,
    DESCRIPTION,
    DELIVERY_MODE,
    CUSTOMER_LOCATION,
    COVERAGE_RADIUS,
    PROVIDER_LOCATION,
    PRICE,
    PRICE_RANGE,
}

const val LIST_SERVICE_OTHER_CATEGORY_ID = "other"
val LIST_SERVICE_COVERAGE_RADII_KM = listOf(1, 3, 5, 10, 25, 50)

data class ListServiceFormState(
    val categoryId: String? = null,
    val customCategory: String = "",
    val title: String = "",
    val description: String = "",
    val deliveryModes: Set<ListServiceDeliveryMode> = emptySet(),
    val customerAreaLabel: String = "",
    val customerPlaceId: String? = null,
    val customerLatitude: Double? = null,
    val customerLongitude: Double? = null,
    val coverageRadiusKm: Int? = null,
    val providerAreaLabel: String = "",
    val providerPlaceId: String? = null,
    val providerLatitude: Double? = null,
    val providerLongitude: Double? = null,
    val pricingMode: ListServicePricingMode? = null,
    val priceAmount: String = "",
    val minimumPrice: String = "",
    val maximumPrice: String = "",
    val portfolioUris: List<String> = emptyList(),
    val availability: String = "",
    val experience: String = "",
    val typicalDuration: String = "",
    val materials: String = "",
    val credentials: String = "",
    val warranty: String = "",
    val advanceNotice: String = "",
    val expandedOptionalSections: Set<ListServiceOptionalSection> = emptySet(),
    val hasAttemptedReview: Boolean = false,
    val reviewAttempt: Int = 0,
    val screenMode: ListServiceScreenMode = ListServiceScreenMode.FORM,
)

data class ListServiceCoverage(
    val publicAreaLabel: String,
    val placeId: String?,
    val latitude: Double,
    val longitude: Double,
    val radiusKm: Int,
)

data class ListServicePublicLocation(
    val publicAreaLabel: String,
    val placeId: String?,
    val latitude: Double,
    val longitude: Double,
)

sealed interface ListServicePricing {
    data object ContactForQuote : ListServicePricing

    data class StartingAt(val amount: String) : ListServicePricing

    data class Fixed(val amount: String) : ListServicePricing

    data class Hourly(val amount: String) : ListServicePricing

    data class PerVisit(val amount: String) : ListServicePricing

    data class Range(val minimum: String, val maximum: String) : ListServicePricing
}

data class ListServiceDetails(
    val availability: String?,
    val experience: String?,
    val typicalDuration: String?,
    val materials: String?,
    val credentials: String?,
    val warranty: String?,
    val advanceNotice: String?,
)

data class ListServiceDraft(
    val categoryId: String,
    val customCategory: String? = null,
    val title: String,
    val description: String,
    val deliveryModes: Set<ListServiceDeliveryMode>,
    val customerCoverage: ListServiceCoverage? = null,
    val providerLocation: ListServicePublicLocation? = null,
    val pricing: ListServicePricing? = null,
    val portfolioUris: List<String> = emptyList(),
    val details: ListServiceDetails,
)

class ListServiceViewModel(
    private val savedStateHandle: SavedStateHandle? = null,
) : ViewModel() {

    private val _formState = MutableStateFlow(restoreState())
    val formState: StateFlow<ListServiceFormState> = _formState.asStateFlow()

    fun startNewDraft() {
        setState(ListServiceFormState())
    }

    fun updateTitle(value: String) = update { copy(title = value) }

    fun updateDescription(value: String) = update { copy(description = value) }

    fun selectCategory(categoryId: String) = update {
        copy(
            categoryId = categoryId,
            customCategory = if (categoryId == LIST_SERVICE_OTHER_CATEGORY_ID) {
                customCategory
            } else {
                ""
            },
        )
    }

    fun updateCustomCategory(value: String) = update {
        copy(
            categoryId = LIST_SERVICE_OTHER_CATEGORY_ID,
            customCategory = value,
        )
    }

    fun toggleDeliveryMode(mode: ListServiceDeliveryMode) = update {
        copy(
            deliveryModes = if (mode in deliveryModes) {
                deliveryModes - mode
            } else {
                deliveryModes + mode
            },
        )
    }

    fun selectCustomerLocation(
        placeId: String?,
        publicAreaLabel: String,
        latitude: Double,
        longitude: Double,
    ) = update {
        copy(
            customerPlaceId = placeId,
            customerAreaLabel = publicAreaLabel,
            customerLatitude = latitude,
            customerLongitude = longitude,
        )
    }

    fun selectCoverageRadius(radiusKm: Int) {
        if (radiusKm !in LIST_SERVICE_COVERAGE_RADII_KM) return
        update { copy(coverageRadiusKm = radiusKm) }
    }

    fun selectProviderLocation(
        placeId: String?,
        publicAreaLabel: String,
        latitude: Double,
        longitude: Double,
    ) = update {
        copy(
            providerPlaceId = placeId,
            providerAreaLabel = publicAreaLabel,
            providerLatitude = latitude,
            providerLongitude = longitude,
        )
    }

    fun selectPricingMode(mode: ListServicePricingMode) = update { copy(pricingMode = mode) }

    fun updatePriceAmount(value: String) = update { copy(priceAmount = value) }

    fun updateMinimumPrice(value: String) = update { copy(minimumPrice = value) }

    fun updateMaximumPrice(value: String) = update { copy(maximumPrice = value) }

    fun setPortfolioUris(uris: List<String>) = update { copy(portfolioUris = uris.distinct()) }

    fun removePortfolioPhoto(uri: String) = update {
        copy(portfolioUris = portfolioUris.filterNot { it == uri })
    }

    fun updateAvailability(value: String) = update { copy(availability = value) }

    fun updateExperience(value: String) = update { copy(experience = value) }

    fun updateTypicalDuration(value: String) = update { copy(typicalDuration = value) }

    fun updateMaterials(value: String) = update { copy(materials = value) }

    fun updateCredentials(value: String) = update { copy(credentials = value) }

    fun updateWarranty(value: String) = update { copy(warranty = value) }

    fun updateAdvanceNotice(value: String) = update { copy(advanceNotice = value) }

    fun toggleOptionalSection(section: ListServiceOptionalSection) = update {
        copy(
            expandedOptionalSections = if (section in expandedOptionalSections) {
                expandedOptionalSections - section
            } else {
                expandedOptionalSections + section
            },
        )
    }

    fun review(): Boolean {
        val attemptedState = _formState.value.copy(
            hasAttemptedReview = true,
            reviewAttempt = _formState.value.reviewAttempt + 1,
        )
        setState(attemptedState)
        if (validate(attemptedState).isNotEmpty()) return false

        setState(attemptedState.copy(screenMode = ListServiceScreenMode.REVIEW))
        return true
    }

    fun edit() = update { copy(screenMode = ListServiceScreenMode.FORM) }

    fun validationErrors(): Set<ListServiceValidationField> =
        if (_formState.value.hasAttemptedReview) validate(_formState.value) else emptySet()

    fun buildValidatedDraft(): ListServiceDraft? {
        val state = _formState.value
        if (validate(state).isNotEmpty()) return null
        return state.toDraft()
    }

    val isDirty: Boolean
        get() = _formState.value.hasMeaningfulChanges

    private fun update(transform: ListServiceFormState.() -> ListServiceFormState) {
        setState(_formState.value.transform())
    }

    private fun setState(next: ListServiceFormState) {
        _formState.value = next
        persist(next)
    }

    private fun persist(state: ListServiceFormState) {
        val handle = savedStateHandle ?: return
        handle[CATEGORY_KEY] = state.categoryId
        handle[CUSTOM_CATEGORY_KEY] = state.customCategory
        handle[TITLE_KEY] = state.title
        handle[DESCRIPTION_KEY] = state.description
        handle[DELIVERY_MODES_KEY] = ArrayList(state.deliveryModes.map { it.name })
        handle[CUSTOMER_AREA_KEY] = state.customerAreaLabel
        handle[CUSTOMER_PLACE_ID_KEY] = state.customerPlaceId
        handle[CUSTOMER_LATITUDE_KEY] = state.customerLatitude
        handle[CUSTOMER_LONGITUDE_KEY] = state.customerLongitude
        handle[COVERAGE_RADIUS_KEY] = state.coverageRadiusKm
        handle[PROVIDER_AREA_KEY] = state.providerAreaLabel
        handle[PROVIDER_PLACE_ID_KEY] = state.providerPlaceId
        handle[PROVIDER_LATITUDE_KEY] = state.providerLatitude
        handle[PROVIDER_LONGITUDE_KEY] = state.providerLongitude
        handle[PRICING_MODE_KEY] = state.pricingMode?.name
        handle[PRICE_AMOUNT_KEY] = state.priceAmount
        handle[MINIMUM_PRICE_KEY] = state.minimumPrice
        handle[MAXIMUM_PRICE_KEY] = state.maximumPrice
        handle[PORTFOLIO_KEY] = ArrayList(state.portfolioUris)
        handle[AVAILABILITY_KEY] = state.availability
        handle[EXPERIENCE_KEY] = state.experience
        handle[TYPICAL_DURATION_KEY] = state.typicalDuration
        handle[MATERIALS_KEY] = state.materials
        handle[CREDENTIALS_KEY] = state.credentials
        handle[WARRANTY_KEY] = state.warranty
        handle[ADVANCE_NOTICE_KEY] = state.advanceNotice
        handle[EXPANDED_KEY] = ArrayList(state.expandedOptionalSections.map { it.name })
        handle[ATTEMPTED_REVIEW_KEY] = state.hasAttemptedReview
        handle[REVIEW_ATTEMPT_KEY] = state.reviewAttempt
        handle[SCREEN_MODE_KEY] = state.screenMode.name
    }

    private fun restoreState(): ListServiceFormState {
        val handle = savedStateHandle ?: return ListServiceFormState()
        return ListServiceFormState(
            categoryId = handle.get<String>(CATEGORY_KEY),
            customCategory = handle.get<String>(CUSTOM_CATEGORY_KEY).orEmpty(),
            title = handle.get<String>(TITLE_KEY).orEmpty(),
            description = handle.get<String>(DESCRIPTION_KEY).orEmpty(),
            deliveryModes = handle.get<ArrayList<String>>(DELIVERY_MODES_KEY)
                .orEmpty()
                .mapNotNull { it.toEnumOrNull<ListServiceDeliveryMode>() }
                .toSet(),
            customerAreaLabel = handle.get<String>(CUSTOMER_AREA_KEY).orEmpty(),
            customerPlaceId = handle.get<String>(CUSTOMER_PLACE_ID_KEY),
            customerLatitude = handle.get<Double>(CUSTOMER_LATITUDE_KEY),
            customerLongitude = handle.get<Double>(CUSTOMER_LONGITUDE_KEY),
            coverageRadiusKm = handle.get<Int>(COVERAGE_RADIUS_KEY),
            providerAreaLabel = handle.get<String>(PROVIDER_AREA_KEY).orEmpty(),
            providerPlaceId = handle.get<String>(PROVIDER_PLACE_ID_KEY),
            providerLatitude = handle.get<Double>(PROVIDER_LATITUDE_KEY),
            providerLongitude = handle.get<Double>(PROVIDER_LONGITUDE_KEY),
            pricingMode = handle.get<String>(PRICING_MODE_KEY)
                .toEnumOrNull<ListServicePricingMode>(),
            priceAmount = handle.get<String>(PRICE_AMOUNT_KEY).orEmpty(),
            minimumPrice = handle.get<String>(MINIMUM_PRICE_KEY).orEmpty(),
            maximumPrice = handle.get<String>(MAXIMUM_PRICE_KEY).orEmpty(),
            portfolioUris = handle.get<ArrayList<String>>(PORTFOLIO_KEY)?.toList().orEmpty(),
            availability = handle.get<String>(AVAILABILITY_KEY).orEmpty(),
            experience = handle.get<String>(EXPERIENCE_KEY).orEmpty(),
            typicalDuration = handle.get<String>(TYPICAL_DURATION_KEY).orEmpty(),
            materials = handle.get<String>(MATERIALS_KEY).orEmpty(),
            credentials = handle.get<String>(CREDENTIALS_KEY).orEmpty(),
            warranty = handle.get<String>(WARRANTY_KEY).orEmpty(),
            advanceNotice = handle.get<String>(ADVANCE_NOTICE_KEY).orEmpty(),
            expandedOptionalSections = handle.get<ArrayList<String>>(EXPANDED_KEY)
                ?.mapNotNull { it.toEnumOrNull<ListServiceOptionalSection>() }
                ?.toSet()
                .orEmpty(),
            hasAttemptedReview = handle.get<Boolean>(ATTEMPTED_REVIEW_KEY) ?: false,
            reviewAttempt = handle.get<Int>(REVIEW_ATTEMPT_KEY) ?: 0,
            screenMode = handle.get<String>(SCREEN_MODE_KEY)
                .toEnumOrNull<ListServiceScreenMode>()
                ?: ListServiceScreenMode.FORM,
        )
    }

    private companion object {
        const val CATEGORY_KEY = "list_service_category"
        const val CUSTOM_CATEGORY_KEY = "list_service_custom_category"
        const val TITLE_KEY = "list_service_title"
        const val DESCRIPTION_KEY = "list_service_description"
        const val DELIVERY_MODES_KEY = "list_service_delivery_modes"
        const val CUSTOMER_AREA_KEY = "list_service_customer_area"
        const val CUSTOMER_PLACE_ID_KEY = "list_service_customer_place_id"
        const val CUSTOMER_LATITUDE_KEY = "list_service_customer_latitude"
        const val CUSTOMER_LONGITUDE_KEY = "list_service_customer_longitude"
        const val COVERAGE_RADIUS_KEY = "list_service_coverage_radius"
        const val PROVIDER_AREA_KEY = "list_service_provider_area"
        const val PROVIDER_PLACE_ID_KEY = "list_service_provider_place_id"
        const val PROVIDER_LATITUDE_KEY = "list_service_provider_latitude"
        const val PROVIDER_LONGITUDE_KEY = "list_service_provider_longitude"
        const val PRICING_MODE_KEY = "list_service_pricing_mode"
        const val PRICE_AMOUNT_KEY = "list_service_price_amount"
        const val MINIMUM_PRICE_KEY = "list_service_minimum_price"
        const val MAXIMUM_PRICE_KEY = "list_service_maximum_price"
        const val PORTFOLIO_KEY = "list_service_portfolio"
        const val AVAILABILITY_KEY = "list_service_availability"
        const val EXPERIENCE_KEY = "list_service_experience"
        const val TYPICAL_DURATION_KEY = "list_service_typical_duration"
        const val MATERIALS_KEY = "list_service_materials"
        const val CREDENTIALS_KEY = "list_service_credentials"
        const val WARRANTY_KEY = "list_service_warranty"
        const val ADVANCE_NOTICE_KEY = "list_service_advance_notice"
        const val EXPANDED_KEY = "list_service_expanded_sections"
        const val ATTEMPTED_REVIEW_KEY = "list_service_attempted_review"
        const val REVIEW_ATTEMPT_KEY = "list_service_review_attempt"
        const val SCREEN_MODE_KEY = "list_service_screen_mode"
    }
}

private val ListServiceFormState.hasMeaningfulChanges: Boolean
    get() = categoryId != null || customCategory.isNotBlank() || title.isNotBlank() ||
        description.isNotBlank() || deliveryModes.isNotEmpty() || customerAreaLabel.isNotBlank() ||
        customerPlaceId != null || customerLatitude != null || customerLongitude != null ||
        coverageRadiusKm != null || providerAreaLabel.isNotBlank() || providerPlaceId != null ||
        providerLatitude != null || providerLongitude != null || pricingMode != null ||
        priceAmount.isNotBlank() || minimumPrice.isNotBlank() || maximumPrice.isNotBlank() ||
        portfolioUris.isNotEmpty() || availability.isNotBlank() || experience.isNotBlank() ||
        typicalDuration.isNotBlank() || materials.isNotBlank() || credentials.isNotBlank() ||
        warranty.isNotBlank() || advanceNotice.isNotBlank()

private fun ListServiceFormState.toDraft(): ListServiceDraft {
    val customerCoverage = if (ListServiceDeliveryMode.AT_CUSTOMER_LOCATION in deliveryModes) {
        ListServiceCoverage(
            publicAreaLabel = customerAreaLabel.trim(),
            placeId = customerPlaceId,
            latitude = requireNotNull(customerLatitude),
            longitude = requireNotNull(customerLongitude),
            radiusKm = requireNotNull(coverageRadiusKm),
        )
    } else {
        null
    }
    val providerLocation = if (ListServiceDeliveryMode.AT_PROVIDER_LOCATION in deliveryModes) {
        ListServicePublicLocation(
            publicAreaLabel = providerAreaLabel.trim(),
            placeId = providerPlaceId,
            latitude = requireNotNull(providerLatitude),
            longitude = requireNotNull(providerLongitude),
        )
    } else {
        null
    }
    val pricing = when (pricingMode) {
        ListServicePricingMode.CONTACT_FOR_QUOTE -> ListServicePricing.ContactForQuote
        ListServicePricingMode.STARTING_AT -> ListServicePricing.StartingAt(priceAmount.trim())
        ListServicePricingMode.FIXED -> ListServicePricing.Fixed(priceAmount.trim())
        ListServicePricingMode.HOURLY -> ListServicePricing.Hourly(priceAmount.trim())
        ListServicePricingMode.PER_VISIT -> ListServicePricing.PerVisit(priceAmount.trim())
        ListServicePricingMode.RANGE -> ListServicePricing.Range(
            minimum = minimumPrice.trim(),
            maximum = maximumPrice.trim(),
        )
        null -> null
    }
    return ListServiceDraft(
        categoryId = requireNotNull(categoryId),
        customCategory = customCategory.trim().takeIf {
            categoryId == LIST_SERVICE_OTHER_CATEGORY_ID && it.isNotBlank()
        },
        title = title.trim(),
        description = description.trim(),
        deliveryModes = deliveryModes.toSet(),
        customerCoverage = customerCoverage,
        providerLocation = providerLocation,
        pricing = pricing,
        portfolioUris = portfolioUris.toList(),
        details = ListServiceDetails(
            availability = availability.trim().ifBlank { null },
            experience = experience.trim().ifBlank { null },
            typicalDuration = typicalDuration.trim().ifBlank { null },
            materials = materials.trim().ifBlank { null },
            credentials = credentials.trim().ifBlank { null },
            warranty = warranty.trim().ifBlank { null },
            advanceNotice = advanceNotice.trim().ifBlank { null },
        ),
    )
}

private fun validate(state: ListServiceFormState): Set<ListServiceValidationField> = buildSet {
    if (
        state.categoryId.isNullOrBlank() ||
        (state.categoryId == LIST_SERVICE_OTHER_CATEGORY_ID && state.customCategory.isBlank())
    ) {
        add(ListServiceValidationField.CATEGORY)
    }
    if (state.title.isBlank()) add(ListServiceValidationField.TITLE)
    if (state.description.isBlank()) add(ListServiceValidationField.DESCRIPTION)
    if (state.deliveryModes.isEmpty()) add(ListServiceValidationField.DELIVERY_MODE)
    if (ListServiceDeliveryMode.AT_CUSTOMER_LOCATION in state.deliveryModes) {
        if (
            state.customerAreaLabel.isBlank() ||
            state.customerLatitude == null ||
            state.customerLongitude == null
        ) {
            add(ListServiceValidationField.CUSTOMER_LOCATION)
        }
        if (state.coverageRadiusKm !in LIST_SERVICE_COVERAGE_RADII_KM) {
            add(ListServiceValidationField.COVERAGE_RADIUS)
        }
    }
    if (ListServiceDeliveryMode.AT_PROVIDER_LOCATION in state.deliveryModes && (
        state.providerAreaLabel.isBlank() ||
            state.providerLatitude == null ||
            state.providerLongitude == null
        )
    ) {
        add(ListServiceValidationField.PROVIDER_LOCATION)
    }
    when (state.pricingMode) {
        ListServicePricingMode.STARTING_AT,
        ListServicePricingMode.FIXED,
        ListServicePricingMode.HOURLY,
        ListServicePricingMode.PER_VISIT,
        -> if (!state.priceAmount.isPositiveAmount()) add(ListServiceValidationField.PRICE)

        ListServicePricingMode.RANGE -> {
            val minimum = state.minimumPrice.toPositiveAmount()
            val maximum = state.maximumPrice.toPositiveAmount()
            if (minimum == null || maximum == null || minimum > maximum) {
                add(ListServiceValidationField.PRICE_RANGE)
            }
        }

        ListServicePricingMode.CONTACT_FOR_QUOTE,
        null,
        -> Unit
    }
}

private fun String.isPositiveAmount(): Boolean = toPositiveAmount() != null

private fun String.toPositiveAmount(): BigDecimal? = trim()
    .toBigDecimalOrNull()
    ?.takeIf { it > BigDecimal.ZERO }

private inline fun <reified T : Enum<T>> String?.toEnumOrNull(): T? =
    this?.let { value -> enumValues<T>().firstOrNull { it.name == value } }
