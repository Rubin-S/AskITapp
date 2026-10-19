package com.askit.app.auth

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Location
import android.location.LocationManager
import android.os.CancellationSignal
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import com.askit.app.location.reverseGeocode
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

data class GeoLocationResult(val city: String, val pincode: String, val latitude: Double, val longitude: Double, val area: String = "")

object LocationHelper {

    fun hasLocationPermission(context: Context): Boolean {
        val fineLocationGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        val coarseLocationGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        return fineLocationGranted || coarseLocationGranted
    }

    suspend fun fetchCurrentLocation(context: Context): Result<GeoLocationResult> = withContext(Dispatchers.IO) {
        if (!hasLocationPermission(context)) {
            return@withContext Result.failure(SecurityException("Location permission not granted"))
        }
        // The Settings "Location services" switch is honoured wherever the app reads the device location.
        if (!com.askit.app.location.LocationPreference.isEnabled(context)) {
            return@withContext Result.failure(IllegalStateException("Location services are off in AskIT settings"))
        }

        withTimeoutOrNull(15_000) {
            try {
                val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                    ?: return@withTimeoutOrNull Result.failure(IllegalStateException("Location service unavailable"))
                val provider = listOf(LocationManager.NETWORK_PROVIDER, LocationManager.GPS_PROVIDER)
                    .firstOrNull { locationManager.isProviderEnabled(it) }
                    ?: return@withTimeoutOrNull Result.failure(IllegalStateException("Location is disabled"))
                val location = suspendCancellableCoroutine<Location?> { continuation ->
                    val cancellationSignal = CancellationSignal()
                    continuation.invokeOnCancellation { cancellationSignal.cancel() }
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED
                    ) {
                        continuation.resume(null)
                    } else {
                        LocationManagerCompat.getCurrentLocation(
                            locationManager,
                            provider,
                            cancellationSignal,
                            { it.run() },
                        ) { continuation.resume(it) }
                    }
                } ?: return@withTimeoutOrNull Result.failure(IllegalStateException("Location unavailable"))

                val lat = location.latitude
                val lon = location.longitude

                reverseGeocode(context, lat, lon)?.toGeoLocationResult(lat, lon)
                    ?.let { Result.success(it) }
                    ?: Result.failure(IllegalStateException("City lookup unavailable"))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(e)
            }
        } ?: Result.failure(IllegalStateException("Location lookup timed out"))
    }
}

/** City, pincode and area for sign-up; null when the geocoder could not name a city. */
private fun Address.toGeoLocationResult(latitude: Double, longitude: Double): GeoLocationResult? {
    val city = locality ?: subAdminArea ?: adminArea
    if (city.isNullOrBlank()) return null
    return GeoLocationResult(
        city = city,
        pincode = postalCode.orEmpty(),
        latitude = latitude,
        longitude = longitude,
        area = subLocality ?: thoroughfare.orEmpty(),
    )
}
