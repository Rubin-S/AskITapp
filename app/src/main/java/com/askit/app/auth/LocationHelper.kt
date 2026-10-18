package com.askit.app.auth

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.CancellationSignal
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
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

                val geocodeResult = reverseGeocode(context, lat, lon)
                if (geocodeResult.second.isBlank()) {
                    return@withTimeoutOrNull Result.failure(IllegalStateException("City lookup unavailable"))
                }
                Result.success(
                    GeoLocationResult(
                        city = geocodeResult.second,
                        pincode = geocodeResult.third,
                        latitude = lat,
                        longitude = lon,
                        area = geocodeResult.first,
                    ),
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(e)
            }
        } ?: Result.failure(IllegalStateException("Location lookup timed out"))
    }

    @Suppress("DEPRECATION")
    private suspend fun reverseGeocode(context: Context, latitude: Double, longitude: Double): Triple<String, String, String> =
        withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, context.resources.configuration.locales[0])
                if (!Geocoder.isPresent()) {
                    return@withContext Triple("", "", "")
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    suspendCancellableCoroutine { continuation ->
                        geocoder.getFromLocation(
                            latitude,
                            longitude,
                            1,
                            object : Geocoder.GeocodeListener {
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
                            },
                        )
                    }
                } else {
                    val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                    val addr = addresses?.firstOrNull()
                    val area = addr?.subLocality ?: addr?.thoroughfare.orEmpty()
                    val city = addr?.locality ?: addr?.subAdminArea ?: addr?.adminArea.orEmpty()
                    val pincode = addr?.postalCode.orEmpty()
                    Triple(area, city, pincode)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                Triple("", "", "")
            }
        }
}
