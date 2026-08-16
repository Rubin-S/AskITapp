package com.askit.app.listservice

import com.askit.app.R
import com.askit.app.category.ASKIT_SERVICE_CATEGORIES
import com.askit.app.session.ServiceListing

fun ListServiceDraft.categoryLabel(getString: (Int) -> String): String {
    if (categoryId == LIST_SERVICE_OTHER_CATEGORY_ID) {
        return customCategory?.takeIf { it.isNotBlank() }
            ?: getString(R.string.list_service_other_category)
    }
    val labelRes = ASKIT_SERVICE_CATEGORIES.firstOrNull { it.id == categoryId }?.labelRes
    return labelRes?.let(getString) ?: categoryId
}

fun ListServiceDraft.quoteLabel(): String = when (val value = pricing) {
    ListServicePricing.ContactForQuote, null -> "Contact for quote"
    is ListServicePricing.StartingAt -> "Starting at ₹${value.amount}"
    is ListServicePricing.Fixed -> "₹${value.amount}"
    is ListServicePricing.Hourly -> "₹${value.amount} per hour"
    is ListServicePricing.PerVisit -> "₹${value.amount} per visit"
    is ListServicePricing.Range -> "₹${value.minimum}–₹${value.maximum}"
}

fun ListServiceDraft.toServiceListing(categoryLabel: String): ServiceListing {
    val coverage = customerCoverage?.publicAreaLabel
        ?: providerLocation?.publicAreaLabel
        ?: ""
    val coverageHint = customerCoverage?.let { "Up to ${it.radiusKm} km" }.orEmpty()
    val responseHint = buildList {
        if (ListServiceDeliveryMode.AT_CUSTOMER_LOCATION in deliveryModes) add("Travels to site")
        if (ListServiceDeliveryMode.AT_PROVIDER_LOCATION in deliveryModes) add("At your location")
        if (ListServiceDeliveryMode.REMOTE in deliveryModes) add("Remote")
    }.joinToString(" · ")
    val tags = buildList {
        add(categoryLabel)
        details.materials
            ?.split(',')
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            ?.let(::addAll)
    }.distinct()
    return ServiceListing(
        title = title,
        category = categoryLabel,
        categoryId = categoryId,
        description = description,
        quoteLabel = quoteLabel(),
        coverage = coverage,
        coverageHint = coverageHint,
        hours = details.availability.orEmpty(),
        hoursHint = details.typicalDuration.orEmpty(),
        response = details.advanceNotice.orEmpty(),
        responseHint = responseHint,
        tags = tags,
        experience = details.experience.orEmpty(),
        live = true,
    )
}

fun ListServiceDraft.toFormState(): ListServiceFormState {
    val pricingMode = when (pricing) {
        ListServicePricing.ContactForQuote -> ListServicePricingMode.CONTACT_FOR_QUOTE
        is ListServicePricing.StartingAt -> ListServicePricingMode.STARTING_AT
        is ListServicePricing.Fixed -> ListServicePricingMode.FIXED
        is ListServicePricing.Hourly -> ListServicePricingMode.HOURLY
        is ListServicePricing.PerVisit -> ListServicePricingMode.PER_VISIT
        is ListServicePricing.Range -> ListServicePricingMode.RANGE
        null -> null
    }
    val amount = when (val value = pricing) {
        is ListServicePricing.StartingAt -> value.amount
        is ListServicePricing.Fixed -> value.amount
        is ListServicePricing.Hourly -> value.amount
        is ListServicePricing.PerVisit -> value.amount
        else -> ""
    }
    val range = pricing as? ListServicePricing.Range
    val hasDetails = listOf(
        details.availability,
        details.experience,
        details.typicalDuration,
        details.materials,
        details.credentials,
        details.warranty,
        details.advanceNotice,
    ).any { !it.isNullOrBlank() }
    val expanded = buildSet {
        if (pricing != null) add(ListServiceOptionalSection.PRICING)
        if (hasDetails) add(ListServiceOptionalSection.MORE_DETAILS)
    }
    return ListServiceFormState(
        categoryId = categoryId,
        customCategory = customCategory.orEmpty(),
        title = title,
        description = description,
        deliveryModes = deliveryModes,
        customerAreaLabel = customerCoverage?.publicAreaLabel.orEmpty(),
        customerPlaceId = customerCoverage?.placeId,
        customerLatitude = customerCoverage?.latitude,
        customerLongitude = customerCoverage?.longitude,
        coverageRadiusKm = customerCoverage?.radiusKm,
        providerAreaLabel = providerLocation?.publicAreaLabel.orEmpty(),
        providerPlaceId = providerLocation?.placeId,
        providerLatitude = providerLocation?.latitude,
        providerLongitude = providerLocation?.longitude,
        pricingMode = pricingMode,
        priceAmount = amount,
        minimumPrice = range?.minimum.orEmpty(),
        maximumPrice = range?.maximum.orEmpty(),
        portfolioUris = portfolioUris,
        availability = details.availability.orEmpty(),
        experience = details.experience.orEmpty(),
        typicalDuration = details.typicalDuration.orEmpty(),
        materials = details.materials.orEmpty(),
        credentials = details.credentials.orEmpty(),
        warranty = details.warranty.orEmpty(),
        advanceNotice = details.advanceNotice.orEmpty(),
        expandedOptionalSections = expanded,
        screenMode = ListServiceScreenMode.FORM,
    )
}
