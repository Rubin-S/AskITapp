package com.askit.app.location

import android.content.Context
import androidx.core.content.edit
import com.askit.app.profile.DeviceSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * The Settings "Location services" switch. It is a device preference, not an account field, so the
 * next `/v1/me` never overwrites it, and every place that reads the device location checks it.
 */
internal class LocationPreference(context: Context) : DeviceSettings {
    private val preferences = context.applicationContext.getSharedPreferences(FILE, Context.MODE_PRIVATE)
    private val enabled = MutableStateFlow(preferences.getBoolean(KEY, true))
    override val locationServicesEnabled: StateFlow<Boolean> = enabled.asStateFlow()

    override fun setLocationServicesEnabled(enabled: Boolean) {
        preferences.edit { putBoolean(KEY, enabled) }
        this.enabled.value = enabled
    }

    companion object {
        private const val FILE = "askit_device_settings"
        private const val KEY = "location_services"

        fun isEnabled(context: Context): Boolean = context.getSharedPreferences(FILE, Context.MODE_PRIVATE).getBoolean(KEY, true)

        fun clear(context: Context) = context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit(commit = true) { clear() }
    }
}
