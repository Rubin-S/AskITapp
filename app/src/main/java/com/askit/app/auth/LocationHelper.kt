package com.askit.app.auth

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

data class GeoLocationResult(
    val city: String,
    val pincode: String,
    val latitude: Double,
    val longitude: Double,
    val area: String = "",
)

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

    @SuppressLint("MissingPermission")
    suspend fun fetchCurrentLocation(context: Context): Result<GeoLocationResult> = withContext(Dispatchers.IO) {
        if (!hasLocationPermission(context)) {
            return@withContext Result.failure(SecurityException("Location permission not granted"))
        }

        try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                ?: return@withContext Result.failure(IllegalStateException("Location service unavailable"))

            val providers = locationManager.getProviders(true)
            var bestLocation: Location? = null

            for (provider in providers) {
                val l = locationManager.getLastKnownLocation(provider) ?: continue
                if (bestLocation == null || l.accuracy < bestLocation.accuracy) {
                    bestLocation = l
                }
            }

            if (bestLocation == null) {
                // Fallback for emulators/devices without cached location
                return@withContext Result.success(
                    GeoLocationResult(
                        city = "Coimbatore",
                        pincode = "641001",
                        latitude = 11.0168,
                        longitude = 76.9558,
                    ),
                )
            }

            val lat = bestLocation.latitude
            val lon = bestLocation.longitude

            val geocodeResult = reverseGeocode(context, lat, lon)
            Result.success(
                GeoLocationResult(
                    city = geocodeResult.second.ifBlank { "Coimbatore" },
                    pincode = geocodeResult.third.ifBlank { "641001" },
                    latitude = lat,
                    longitude = lon,
                    area = geocodeResult.first,
                ),
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    @Suppress("DEPRECATION")
    private suspend fun reverseGeocode(
        context: Context,
        latitude: Double,
        longitude: Double,
    ): Triple<String, String, String> = withContext(Dispatchers.IO) {
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            if (!Geocoder.isPresent()) {
                return@withContext Triple("", "", "")
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                suspendCancellableCoroutine { continuation ->
                    geocoder.getFromLocation(latitude, longitude, 1, object : Geocoder.GeocodeListener {
                        override fun onGeocode(addresses: MutableList<android.location.Address>) {
                            val addr = addresses.firstOrNull()
                            val area = addr?.subLocality ?: addr?.thoroughfare.orEmpty()
                            val city = addr?.locality ?: addr?.subAdminArea ?: addr?.adminArea.orEmpty()
                            val pincode = addr?.postalCode.orEmpty()
                            continuation.resume(Triple(area, city, pincode))
                        }

                        override fun onError(errorMessage: String?) {
                            continuation.resume(Triple("", "", ""))
                        }
                    })
                }
            } else {
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                val addr = addresses?.firstOrNull()
                val area = addr?.subLocality ?: addr?.thoroughfare.orEmpty()
                val city = addr?.locality ?: addr?.subAdminArea ?: addr?.adminArea.orEmpty()
                val pincode = addr?.postalCode.orEmpty()
                Triple(area, city, pincode)
            }
        } catch (_: Exception) {
            Triple("", "", "")
        }
    }
}
