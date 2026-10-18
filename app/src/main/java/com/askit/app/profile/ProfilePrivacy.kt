package com.askit.app.profile

import com.askit.app.auth.validAccountPostcode
import kotlin.math.roundToInt

/**
 * Rounded distance to show on a profile, or null when it must stay private.
 * Distance is server-supplied between postcode centroids; never derive it from exact coordinates.
 */
internal fun approximateProfileDistance(consent: Boolean, viewerPostcode: String?, distanceKm: Double?): Int? = distanceKm?.takeIf {
    consent && validAccountPostcode(viewerPostcode.orEmpty()) && it.isFinite() && it >= 0
}?.roundToInt()
