package com.askit.app.posttask

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.math.BigDecimal

enum class PostTaskWorkMode {
    AT_MY_LOCATION,
    AT_PROVIDER_LOCATION,
    REMOTE,
}

enum class PostTaskTimingMode {
    ASAP,
    DATE,
    FLEXIBLE,
}

enum class PostTaskBudgetMode {
    REQUEST_QUOTES,
    FIXED,
    RANGE,
}

enum class PostTaskMaterialsPolicy {
    REQUESTER_HAS_MATERIALS,
    PROVIDER_BRINGS_MATERIALS,
    DECIDE_AFTER_INSPECTION,
}

enum class PostTaskOptionalSection {
    BUDGET,
    PHOTOS,
    ITEM_DETAILS,
    ACCESS_INSTRUCTIONS,
}

enum class PostTaskScreenMode {
    FORM,
    REVIEW,
}

enum class PostTaskValidationField {
    CATEGORY,
    TITLE,
    DETAILS,
    WORK_MODE,
    LOCATION,
    TIMING,
    DATE,
    FIXED_BUDGET,
    BUDGET_RANGE,
}

const val POST_TASK_OTHER_CATEGORY_ID = "other"

data class PostTaskFormState(
    val categoryId: String? = null,
    val customCategory: String = "",
    val title: String = "",
    val details: String = "",
    val workMode: PostTaskWorkMode? = null,
    val publicAreaLabel: String = "",
    val placeId: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val privateAddressOrLandmark: String = "",
    val timingMode: PostTaskTimingMode? = null,
    val selectedDateMillis: Long? = null,
    val budgetMode: PostTaskBudgetMode? = null,
    val fixedBudget: String = "",
    val minimumBudget: String = "",
    val maximumBudget: String = "",
    val photoUris: List<String> = emptyList(),
    val quantityOrMeasurements: String = "",
    val brandOrModel: String = "",
    val materialsPolicy: PostTaskMaterialsPolicy? = null,
    val accessInstructions: String = "",
    val expandedOptionalSections: Set<PostTaskOptionalSection> = emptySet(),
    val hasAttemptedReview: Boolean = false,
    val reviewAttempt: Int = 0,
    val screenMode: PostTaskScreenMode = PostTaskScreenMode.FORM,
)

data class PostTaskLocation(
    val publicAreaLabel: String,
    val placeId: String?,
    val latitude: Double,
    val longitude: Double,
    val privateAddressOrLandmark: String?,
)

sealed interface PostTaskBudget {
    data object RequestQuotes : PostTaskBudget

    data class Fixed(val amount: String) : PostTaskBudget

    data class Range(val minimum: String, val maximum: String) : PostTaskBudget
}

data class PostTaskItemDetails(
    val quantityOrMeasurements: String?,
    val brandOrModel: String?,
    val materialsPolicy: PostTaskMaterialsPolicy?,
)

data class PostTaskDraft(
    val categoryId: String,
    val customCategory: String? = null,
    val title: String,
    val details: String,
    val workMode: PostTaskWorkMode,
    val location: PostTaskLocation?,
    val timingMode: PostTaskTimingMode,
    val selectedDateMillis: Long?,
    val budget: PostTaskBudget?,
    val photos: List<String>,
    val itemDetails: PostTaskItemDetails?,
    val accessInstructions: String?,
)

class PostTaskViewModel(
    private val savedStateHandle: SavedStateHandle? = null,
) : ViewModel() {

    private val _formState = MutableStateFlow(restoreState())
    val formState: StateFlow<PostTaskFormState> = _formState.asStateFlow()

    fun startNewDraft() {
        setState(PostTaskFormState())
    }

    fun updateTitle(value: String) = update { copy(title = value) }

    fun updateDetails(value: String) = update { copy(details = value) }

    fun selectCategory(categoryId: String) = update {
        copy(
            categoryId = categoryId,
            customCategory = if (categoryId == POST_TASK_OTHER_CATEGORY_ID) {
                customCategory
            } else {
                ""
            },
        )
    }

    fun updateCustomCategory(value: String) = update {
        copy(
            categoryId = POST_TASK_OTHER_CATEGORY_ID,
            customCategory = value,
        )
    }

    fun selectWorkMode(workMode: PostTaskWorkMode) = update { copy(workMode = workMode) }

    fun selectTiming(timingMode: PostTaskTimingMode) = update { copy(timingMode = timingMode) }

    fun selectDate(dateMillis: Long?) = update { copy(selectedDateMillis = dateMillis) }

    fun selectLocation(
        placeId: String?,
        publicAreaLabel: String,
        latitude: Double,
        longitude: Double,
    ) = update {
        copy(
            placeId = placeId,
            publicAreaLabel = publicAreaLabel,
            latitude = latitude,
            longitude = longitude,
        )
    }

    fun updatePrivateAddressOrLandmark(value: String) =
        update { copy(privateAddressOrLandmark = value) }

    fun selectBudgetMode(budgetMode: PostTaskBudgetMode) = update { copy(budgetMode = budgetMode) }

    fun updateFixedBudget(value: String) = update { copy(fixedBudget = value) }

    fun updateMinimumBudget(value: String) = update { copy(minimumBudget = value) }

    fun updateMaximumBudget(value: String) = update { copy(maximumBudget = value) }

    fun setPhotoUris(uris: List<String>) = update { copy(photoUris = uris.distinct()) }

    fun removePhoto(uri: String) = update { copy(photoUris = photoUris.filterNot { it == uri }) }

    fun updateQuantityOrMeasurements(value: String) =
        update { copy(quantityOrMeasurements = value) }

    fun updateBrandOrModel(value: String) = update { copy(brandOrModel = value) }

    fun selectMaterialsPolicy(policy: PostTaskMaterialsPolicy) =
        update { copy(materialsPolicy = policy) }

    fun updateAccessInstructions(value: String) = update { copy(accessInstructions = value) }

    fun toggleOptionalSection(section: PostTaskOptionalSection) = update {
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

        setState(attemptedState.copy(screenMode = PostTaskScreenMode.REVIEW))
        return true
    }

    fun edit() = update { copy(screenMode = PostTaskScreenMode.FORM) }

    fun validationErrors(): Set<PostTaskValidationField> =
        if (_formState.value.hasAttemptedReview) validate(_formState.value) else emptySet()

    fun buildValidatedDraft(): PostTaskDraft? {
        val state = _formState.value
        if (validate(state).isNotEmpty()) return null
        return state.toDraft()
    }

    val isDirty: Boolean
        get() = _formState.value.hasMeaningfulChanges

    private fun update(transform: PostTaskFormState.() -> PostTaskFormState) {
        setState(_formState.value.transform())
    }

    private fun setState(next: PostTaskFormState) {
        _formState.value = next
        persist(next)
    }

    private fun persist(state: PostTaskFormState) {
        val handle = savedStateHandle ?: return
        handle[CATEGORY_KEY] = state.categoryId
        handle[CUSTOM_CATEGORY_KEY] = state.customCategory
        handle[TITLE_KEY] = state.title
        handle[DETAILS_KEY] = state.details
        handle[WORK_MODE_KEY] = state.workMode?.name
        handle[PUBLIC_AREA_LABEL_KEY] = state.publicAreaLabel
        handle[PLACE_ID_KEY] = state.placeId
        handle[LATITUDE_KEY] = state.latitude
        handle[LONGITUDE_KEY] = state.longitude
        handle[PRIVATE_ADDRESS_KEY] = state.privateAddressOrLandmark
        handle[TIMING_MODE_KEY] = state.timingMode?.name
        handle[DATE_KEY] = state.selectedDateMillis
        handle[BUDGET_MODE_KEY] = state.budgetMode?.name
        handle[FIXED_BUDGET_KEY] = state.fixedBudget
        handle[MINIMUM_BUDGET_KEY] = state.minimumBudget
        handle[MAXIMUM_BUDGET_KEY] = state.maximumBudget
        handle[PHOTOS_KEY] = ArrayList(state.photoUris)
        handle[QUANTITY_KEY] = state.quantityOrMeasurements
        handle[BRAND_KEY] = state.brandOrModel
        handle[MATERIALS_KEY] = state.materialsPolicy?.name
        handle[ACCESS_KEY] = state.accessInstructions
        handle[EXPANDED_KEY] = ArrayList(state.expandedOptionalSections.map { it.name })
        handle[ATTEMPTED_REVIEW_KEY] = state.hasAttemptedReview
        handle[REVIEW_ATTEMPT_KEY] = state.reviewAttempt
        handle[SCREEN_MODE_KEY] = state.screenMode.name
    }

    private fun restoreState(): PostTaskFormState {
        val handle = savedStateHandle ?: return PostTaskFormState()
        return PostTaskFormState(
            categoryId = handle.get<String>(CATEGORY_KEY),
            customCategory = handle.get<String>(CUSTOM_CATEGORY_KEY).orEmpty(),
            title = handle.get<String>(TITLE_KEY).orEmpty(),
            details = handle.get<String>(DETAILS_KEY).orEmpty(),
            workMode = handle.get<String>(WORK_MODE_KEY).toEnumOrNull<PostTaskWorkMode>(),
            publicAreaLabel = handle.get<String>(PUBLIC_AREA_LABEL_KEY).orEmpty(),
            placeId = handle.get<String>(PLACE_ID_KEY),
            latitude = handle.get<Double>(LATITUDE_KEY),
            longitude = handle.get<Double>(LONGITUDE_KEY),
            privateAddressOrLandmark = handle.get<String>(PRIVATE_ADDRESS_KEY).orEmpty(),
            timingMode = handle.get<String>(TIMING_MODE_KEY).toEnumOrNull<PostTaskTimingMode>(),
            selectedDateMillis = handle.get<Long>(DATE_KEY),
            budgetMode = handle.get<String>(BUDGET_MODE_KEY).toEnumOrNull<PostTaskBudgetMode>(),
            fixedBudget = handle.get<String>(FIXED_BUDGET_KEY).orEmpty(),
            minimumBudget = handle.get<String>(MINIMUM_BUDGET_KEY).orEmpty(),
            maximumBudget = handle.get<String>(MAXIMUM_BUDGET_KEY).orEmpty(),
            photoUris = handle.get<ArrayList<String>>(PHOTOS_KEY)?.toList().orEmpty(),
            quantityOrMeasurements = handle.get<String>(QUANTITY_KEY).orEmpty(),
            brandOrModel = handle.get<String>(BRAND_KEY).orEmpty(),
            materialsPolicy = handle.get<String>(MATERIALS_KEY)
            .toEnumOrNull<PostTaskMaterialsPolicy>(),
            accessInstructions = handle.get<String>(ACCESS_KEY).orEmpty(),
            expandedOptionalSections = handle.get<ArrayList<String>>(EXPANDED_KEY)
            ?.mapNotNull { it.toEnumOrNull<PostTaskOptionalSection>() }
            ?.toSet()
            .orEmpty(),
            hasAttemptedReview = handle.get<Boolean>(ATTEMPTED_REVIEW_KEY) ?: false,
            reviewAttempt = handle.get<Int>(REVIEW_ATTEMPT_KEY) ?: 0,
            screenMode = handle.get<String>(SCREEN_MODE_KEY)
            .toEnumOrNull<PostTaskScreenMode>()
            ?: PostTaskScreenMode.FORM,
        )
    }

    private companion object {
        const val CATEGORY_KEY = "post_task_category"
        const val CUSTOM_CATEGORY_KEY = "post_task_custom_category"
        const val TITLE_KEY = "post_task_title"
        const val DETAILS_KEY = "post_task_details"
        const val WORK_MODE_KEY = "post_task_work_mode"
        const val PUBLIC_AREA_LABEL_KEY = "post_task_public_area_label"
        const val PLACE_ID_KEY = "post_task_place_id"
        const val LATITUDE_KEY = "post_task_latitude"
        const val LONGITUDE_KEY = "post_task_longitude"
        const val PRIVATE_ADDRESS_KEY = "post_task_private_address"
        const val TIMING_MODE_KEY = "post_task_timing_mode"
        const val DATE_KEY = "post_task_date"
        const val BUDGET_MODE_KEY = "post_task_budget_mode"
        const val FIXED_BUDGET_KEY = "post_task_fixed_budget"
        const val MINIMUM_BUDGET_KEY = "post_task_minimum_budget"
        const val MAXIMUM_BUDGET_KEY = "post_task_maximum_budget"
        const val PHOTOS_KEY = "post_task_photos"
        const val QUANTITY_KEY = "post_task_quantity"
        const val BRAND_KEY = "post_task_brand"
        const val MATERIALS_KEY = "post_task_materials"
        const val ACCESS_KEY = "post_task_access"
        const val EXPANDED_KEY = "post_task_expanded_sections"
        const val ATTEMPTED_REVIEW_KEY = "post_task_attempted_review"
        const val REVIEW_ATTEMPT_KEY = "post_task_review_attempt"
        const val SCREEN_MODE_KEY = "post_task_screen_mode"
    }
}

private val PostTaskFormState.hasMeaningfulChanges: Boolean
    get() = categoryId != null || customCategory.isNotBlank() || title.isNotBlank() || details.isNotBlank() ||
        workMode != null || publicAreaLabel.isNotBlank() || placeId != null ||
        latitude != null || longitude != null || privateAddressOrLandmark.isNotBlank() ||
        timingMode != null || selectedDateMillis != null || budgetMode != null ||
        fixedBudget.isNotBlank() || minimumBudget.isNotBlank() || maximumBudget.isNotBlank() ||
        photoUris.isNotEmpty() || quantityOrMeasurements.isNotBlank() || brandOrModel.isNotBlank() ||
        materialsPolicy != null || accessInstructions.isNotBlank()

private fun PostTaskFormState.toDraft(): PostTaskDraft {
    val selectedWorkMode = requireNotNull(workMode)
    val selectedTiming = requireNotNull(timingMode)
    val location = if (selectedWorkMode == PostTaskWorkMode.AT_MY_LOCATION) {
        PostTaskLocation(
            publicAreaLabel = publicAreaLabel.trim(),
            placeId = placeId,
            latitude = requireNotNull(latitude),
            longitude = requireNotNull(longitude),
            privateAddressOrLandmark = privateAddressOrLandmark.trim().ifBlank { null },
        )
    } else {
        null
    }
    val budget = when (budgetMode) {
        PostTaskBudgetMode.REQUEST_QUOTES -> PostTaskBudget.RequestQuotes
        PostTaskBudgetMode.FIXED -> PostTaskBudget.Fixed(fixedBudget.trim())
        PostTaskBudgetMode.RANGE -> PostTaskBudget.Range(
            minimum = minimumBudget.trim(),
            maximum = maximumBudget.trim(),
        )
        null -> null
    }
    val itemDetails = PostTaskItemDetails(
        quantityOrMeasurements = quantityOrMeasurements.trim().ifBlank { null },
        brandOrModel = brandOrModel.trim().ifBlank { null },
        materialsPolicy = materialsPolicy,
    ).takeIf { it.quantityOrMeasurements != null || it.brandOrModel != null || it.materialsPolicy != null }
    return PostTaskDraft(
        categoryId = requireNotNull(categoryId),
        customCategory = customCategory.trim().takeIf {
            categoryId == POST_TASK_OTHER_CATEGORY_ID && it.isNotBlank()
        },
        title = title.trim(),
        details = details.trim(),
        workMode = selectedWorkMode,
        location = location,
        timingMode = selectedTiming,
        selectedDateMillis = selectedDateMillis.takeIf { selectedTiming == PostTaskTimingMode.DATE },
        budget = budget,
        photos = photoUris.toList(),
        itemDetails = itemDetails,
        accessInstructions = accessInstructions.trim()
            .takeIf { selectedWorkMode == PostTaskWorkMode.AT_MY_LOCATION && it.isNotBlank() },
    )
}

private fun validate(state: PostTaskFormState): Set<PostTaskValidationField> = buildSet {
    if (
        state.categoryId.isNullOrBlank() ||
        (state.categoryId == POST_TASK_OTHER_CATEGORY_ID && state.customCategory.isBlank())
    ) {
        add(PostTaskValidationField.CATEGORY)
    }
    if (state.title.isBlank()) add(PostTaskValidationField.TITLE)
    if (state.details.isBlank()) add(PostTaskValidationField.DETAILS)
    if (state.workMode == null) add(PostTaskValidationField.WORK_MODE)
    if (
        state.workMode == PostTaskWorkMode.AT_MY_LOCATION &&
        (state.publicAreaLabel.isBlank() || state.latitude == null || state.longitude == null)
    ) {
        add(PostTaskValidationField.LOCATION)
    }
    if (state.timingMode == null) add(PostTaskValidationField.TIMING)
    if (state.timingMode == PostTaskTimingMode.DATE && state.selectedDateMillis == null) {
        add(PostTaskValidationField.DATE)
    }
    when (state.budgetMode) {
        PostTaskBudgetMode.FIXED -> if (!state.fixedBudget.isPositiveAmount()) {
            add(PostTaskValidationField.FIXED_BUDGET)
        }
        PostTaskBudgetMode.RANGE -> {
            val minimum = state.minimumBudget.toPositiveAmount()
            val maximum = state.maximumBudget.toPositiveAmount()
            if (minimum == null || maximum == null || minimum > maximum) {
                add(PostTaskValidationField.BUDGET_RANGE)
            }
        }
        PostTaskBudgetMode.REQUEST_QUOTES, null -> Unit
    }
}

private fun String.isPositiveAmount(): Boolean = toPositiveAmount() != null

private fun String.toPositiveAmount(): BigDecimal? = trim()
    .toBigDecimalOrNull()
    ?.takeIf { it > BigDecimal.ZERO }

private inline fun <reified T : Enum<T>> String?.toEnumOrNull(): T? =
    this?.let { value -> enumValues<T>().firstOrNull { it.name == value } }
