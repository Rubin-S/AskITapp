package com.askit.app.profile

import com.askit.app.auth.validAccountPostcode
import com.askit.app.session.SessionProfile
import kotlin.math.roundToInt

/** Distance is server-supplied between postcode centroids; never derive it from exact coordinates. */
internal fun SessionProfile.approximateDistance(viewerPostcode: String?, distanceKm: Double?): Int? =
    approximateProfileDistance(discloseApproximateLocation, viewerPostcode, distanceKm)

internal fun approximateProfileDistance(consent: Boolean, viewerPostcode: String?, distanceKm: Double?): Int? =
    distanceKm?.takeIf {
        consent && validAccountPostcode(viewerPostcode.orEmpty()) && it.isFinite() && it >= 0
    }?.roundToInt()
