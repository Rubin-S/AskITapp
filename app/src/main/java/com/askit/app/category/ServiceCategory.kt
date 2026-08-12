package com.askit.app.category

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.askit.app.R

data class ServiceCategory(
    val id: String,
    @StringRes val labelRes: Int,
    @DrawableRes val artworkRes: Int,
)

val ASKIT_SERVICE_CATEGORIES = listOf(
    ServiceCategory("electrician", R.string.explore_category_electrician, R.drawable.service_electrician),
    ServiceCategory("plumber", R.string.explore_category_plumber, R.drawable.service_plumber),
    ServiceCategory("cleaning", R.string.explore_category_cleaning, R.drawable.service_cleaning),
    ServiceCategory("ac_repair", R.string.explore_category_ac_repair, R.drawable.service_ac_repair),
    ServiceCategory("home_tutor", R.string.explore_category_home_tutor, R.drawable.service_home_tutor),
    ServiceCategory("appliance_repair", R.string.explore_category_appliance_repair, R.drawable.service_appliance_repair),
)
