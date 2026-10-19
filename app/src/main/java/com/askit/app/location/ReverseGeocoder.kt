package com.askit.app.location

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

/**
 * The one reverse-geocoding call in the app: the best [Address] for a point, or null when no
 * geocoder is installed, it fails, or it finds nothing. Uses the app's UI locale, so place names
 * follow the chosen language (English/Tamil). Callers map the address to what they need.
 * Coroutine cancellation is passed through rather than swallowed.
 */
suspend fun reverseGeocode(context: Context, latitude: Double, longitude: Double): Address? {
    if (!Geocoder.isPresent()) return null
    val geocoder = Geocoder(context, context.resources.configuration.locales[0])
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            suspendCancellableCoroutine { continuation ->
                geocoder.getFromLocation(
                    latitude,
                    longitude,
                    1,
                    object : Geocoder.GeocodeListener {
                        override fun onGeocode(addresses: MutableList<Address>) {
                            if (continuation.isActive) continuation.resume(addresses.firstOrNull())
                        }

                        override fun onError(errorMessage: String?) {
                            if (continuation.isActive) continuation.resume(null)
                        }
                    },
                )
            }
        } else {
            withContext(Dispatchers.IO) {
                @Suppress("DEPRECATION")
                geocoder.getFromLocation(latitude, longitude, 1)?.firstOrNull()
            }
        }
    } catch (e: CancellationException) {
        throw e
    } catch (_: Exception) {
        null
    }
}
