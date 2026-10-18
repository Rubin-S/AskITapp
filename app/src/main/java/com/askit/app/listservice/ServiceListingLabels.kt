package com.askit.app.listservice

import android.content.Context
import androidx.annotation.StringRes
import com.askit.app.R
import com.askit.app.session.ServiceListing

/** Display text for a [ServiceListing], resolved from string resources at display time. */
data class ServiceListingLabels(val quote: String, val coverageHint: String, val responseHint: String)

/**
 * Builds the listing's display labels from its typed fields ([ServiceListing.pricing],
 * [ServiceListing.coverageRadiusKm], [ServiceListing.deliveryModes]) in the current locale.
 * Listings saved before those fields existed fall back to their stored English labels.
 */
fun ServiceListing.labels(string: (resId: Int, args: Array<out Any>) -> String): ServiceListingLabels {
    fun get(@StringRes id: Int, vararg args: Any) = string(id, args)
    val quote = when (val value = pricing) {
        null -> quoteLabel
        ListServicePricing.ContactForQuote -> get(R.string.list_service_quote_contact)
        is ListServicePricing.StartingAt -> get(R.string.list_service_quote_starting_at, value.amount)
        is ListServicePricing.Fixed -> get(R.string.post_task_budget_fixed_value, value.amount)
        is ListServicePricing.Hourly -> get(R.string.list_service_quote_hourly, value.amount)
        is ListServicePricing.PerVisit -> get(R.string.list_service_quote_per_visit, value.amount)
        is ListServicePricing.Range -> get(R.string.post_task_budget_range_display, value.minimum, value.maximum)
    }
    val coverage = coverageRadiusKm?.let { get(R.string.list_service_coverage_up_to_km, it) } ?: coverageHint
    val response = if (deliveryModes.isEmpty()) responseHint else deliveryModeLabel(deliveryModes) { id -> get(id) }
    return ServiceListingLabels(quote = quote, coverageHint = coverage, responseHint = response)
}

/** Delivery modes in form order: travels to the customer, then at the provider, then remote. */
private fun deliveryModeLabel(modes: Set<ListServiceDeliveryMode>, get: (Int) -> String): String =
    ListServiceDeliveryMode.entries.filter { it in modes }.joinToString(" · ") { mode ->
        get(
            when (mode) {
                ListServiceDeliveryMode.AT_CUSTOMER_LOCATION -> R.string.list_service_mode_travels_to_site
                ListServiceDeliveryMode.AT_PROVIDER_LOCATION -> R.string.list_service_mode_at_provider
                ListServiceDeliveryMode.REMOTE -> R.string.list_service_remote
            },
        )
    }

fun ServiceListing.labels(context: Context): ServiceListingLabels = labels { id, args -> context.getString(id, *args) }
