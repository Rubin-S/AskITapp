package com.askit.app.config

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.askit.app.network.BackendClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import java.io.IOException

data class AppLinks(
    val privacyPolicyUrl: String = FALLBACK_WEBSITE,
    val termsUrl: String = FALLBACK_WEBSITE,
    val helpCenterUrl: String = FALLBACK_WEBSITE,
    val supportUrl: String = FALLBACK_WEBSITE,
    val supportEmail: String? = null,
    val publicProfileBaseUrl: String? = null,
) {
    companion object {
        const val FALLBACK_WEBSITE = "https://askit.in"
    }
}

class AppLinksViewModel(
    backendUrl: String? = null,
    client: OkHttpClient = OkHttpClient(),
    initialLinks: AppLinks = AppLinks(),
) : ViewModel() {
    private val backend = backendUrl?.let { BackendClient(it, client) }
    private val mutableLinks = MutableStateFlow(initialLinks)
    val links = mutableLinks.asStateFlow()
    private val json = Json { ignoreUnknownKeys = true }
    private var request: Job? = null

    fun refresh(): Job? {
        val backend = backend ?: return null
        if (request?.isActive == true) return request
        return viewModelScope.launch {
            try {
                val response = json.decodeFromString<AppLinksResponse>(backend.request("v1/app-config"))
                mutableLinks.value = AppLinks(
                    publicProfileBaseUrl = response.publicProfileBaseUrl?.toHttpUrlOrNull()?.takeIf {
                        it.isHttps && it.username.isEmpty() &&
                            it.password.isEmpty()
                    }?.toString(),
                    privacyPolicyUrl = websiteUrl(response.privacyPolicyUrl),
                    termsUrl = websiteUrl(response.termsUrl),
                    helpCenterUrl = websiteUrl(response.helpCenterUrl),
                    supportUrl = websiteUrl(response.supportUrl),
                    supportEmail = response.supportEmail?.trim()?.takeIf {
                        it.length <= 254 && Patterns.EMAIL_ADDRESS.matcher(it).matches()
                    },
                )
            } catch (_: IOException) {
                ensureActive()
                // A failed refresh keeps the last valid links, or the owner's fallback on first launch.
            } catch (_: SerializationException) {
                // Malformed configuration must not replace validated links.
            }
        }.also { request = it }
    }

    private fun websiteUrl(value: String?): String = value
        ?.takeIf { it.length <= 2_048 }
        ?.toHttpUrlOrNull()
        ?.takeIf { it.isHttps && it.username.isEmpty() && it.password.isEmpty() }
        ?.toString()
        ?: AppLinks.FALLBACK_WEBSITE

    @Serializable
    private data class AppLinksResponse(
        val privacyPolicyUrl: String? = null,
        val termsUrl: String? = null,
        val helpCenterUrl: String? = null,
        val supportUrl: String? = null,
        val supportEmail: String? = null,
        val publicProfileBaseUrl: String? = null,
    )
}

/** Only a server-configured profile route can produce a shareable profile URL. */
internal fun AppLinks.profileUrl(accountId: String): String? {
    if (accountId.isBlank()) return null
    val base = publicProfileBaseUrl?.toHttpUrlOrNull()?.takeIf {
        it.isHttps && it.username.isEmpty() && it.password.isEmpty() && it.query == null && it.fragment == null
    } ?: return null
    return base.newBuilder().addPathSegment(accountId).build().toString()
}
