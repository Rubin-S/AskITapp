package com.askit.app.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.askit.app.R
import com.askit.app.auth.SignupDraft
import com.askit.app.auth.SignupResult
import com.askit.app.listservice.ListServiceDraft
import com.askit.app.session.ProfileAvailability
import com.askit.app.session.ServiceListing
import com.askit.app.session.SessionProfile
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface ProfileLoadState {
    data object Loading : ProfileLoadState
    data object Ready : ProfileLoadState
    data class Failed(val messageRes: Int) : ProfileLoadState
}

/**
 * Profile state and every profile write. Writes run on [io] (the preview persist hook copies media)
 * and are refused up front when [capabilities] says this build can't make them.
 */
class ProfileViewModel(
    private val repository: ProfileRepository,
    val capabilities: ProfileCapabilities = ProfileCapabilities.forBuild(com.askit.app.BuildConfig.CLIENT_PREVIEW),
    private val deviceSettings: DeviceSettings = InMemoryDeviceSettings(),
    private val localData: LocalDataCleaner = LocalDataCleaner {},
    private val io: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {
    val profile: StateFlow<SessionProfile> = repository.profile
    val locationServicesEnabled: StateFlow<Boolean> = deviceSettings.locationServicesEnabled

    /** Follows the profile, so a later sign-in or signup moves a blank profile to Ready. */
    val loadState: StateFlow<ProfileLoadState> = repository.profile
        .map(::loadStateFor)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), loadStateFor(repository.profile.value))

    // Buffered, not dropped: the app shell collects these for every screen, Settings and Edit included.
    private val _messages = Channel<Int>(Channel.BUFFERED)
    val messages: Flow<Int> = _messages.receiveAsFlow()

    /** Only the client preview can create an account (docs/profile-backend-contract.md has no server signup yet). */
    val canSignUp: Boolean get() = capabilities.editProfile

    /**
     * Saves Form A. Release builds return [SignupResult.Unavailable] without touching the profile:
     * Android must not open Home on an in-memory substitute for a server account.
     */
    suspend fun completeSignup(draft: SignupDraft, isPhotoReadable: suspend (String?) -> Boolean): SignupResult = when {
        !canSignUp -> SignupResult.Unavailable
        !draft.validation(isPhotoReadable(draft.avatarUri)).isValid -> SignupResult.Invalid
        write { completeSignup(draft.normalized()) } -> SignupResult.Saved
        else -> SignupResult.Failed
    }

    /** Edit-profile Save. The screen shows its own inline error, so a failure is not also a snackbar. */
    suspend fun saveIdentity(state: EditProfileFormState): Boolean = state.isValid && write(report = false) { updateIdentity(state) }

    suspend fun saveListing(listing: ServiceListing, draft: ListServiceDraft): Boolean = write { applyListing(listing, draft) }

    suspend fun saveHighlight(highlight: ProfileHighlight): Boolean = write(report = false) { saveHighlight(accountId(), highlight) }

    suspend fun deleteHighlight(id: String): Boolean = write(report = false) { deleteHighlight(accountId(), id) }

    suspend fun setBanner(uri: String?): Boolean = write(report = false) { setBanner(accountId(), uri) }

    fun setAvatar(avatarUrl: String?) = launchWrite { setAvatar(accountId(), avatarUrl) }

    fun updateAvailability(availability: ProfileAvailability) = launchWrite { updateAvailability(availability) }

    fun updatePushNotifications(enabled: Boolean) = launchWrite { updateNotifications(enabled, profile.value.jobAlertsEnabled) }

    fun updateJobAlerts(enabled: Boolean) = launchWrite { updateNotifications(profile.value.pushNotificationsEnabled, enabled) }

    fun updateWhoCanMessage(option: String) = launchWrite { updateWhoCanMessage(option) }

    /** The app language is a device preference (current-account contract); the profile copy only follows it. */
    fun updateLanguage(lang: String) {
        viewModelScope.launch { write(gated = false) { updateLanguage(lang) } }
    }

    fun setLocationServicesEnabled(enabled: Boolean) = deviceSettings.setLocationServicesEnabled(enabled)

    /**
     * Clear app data: deletes local storage, photos and caches, then forgets the in-memory account.
     * [onCleared] then resets the locale and signs out, which restarts the app on a fresh session.
     */
    fun resetAppData(onCleared: () -> Unit) {
        viewModelScope.launch {
            val cleared = write(gated = false) {
                localData.clear()
                resetAppData()
            }
            if (cleared) onCleared()
        }
    }

    private fun accountId(): String = profile.value.accountId

    private fun launchWrite(block: ProfileRepository.() -> Unit) {
        viewModelScope.launch { write(block = block) }
    }

    private suspend fun write(gated: Boolean = true, report: Boolean = true, block: ProfileRepository.() -> Unit): Boolean {
        if (gated && !capabilities.editProfile) return false
        val saved = try {
            withContext(io) { repository.block() }
            true
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            false
        }
        if (!saved && report) _messages.trySend(R.string.profile_action_error)
        return saved
    }
}

internal fun loadStateFor(profile: SessionProfile): ProfileLoadState =
    if (profile.displayName.isNotBlank()) ProfileLoadState.Ready else ProfileLoadState.Failed(R.string.profile_load_error)

/** Used by tests and previews; the app passes the SharedPreferences-backed settings. */
class InMemoryDeviceSettings(locationEnabled: Boolean = true) : DeviceSettings {
    private val location = MutableStateFlow(locationEnabled)
    override val locationServicesEnabled: StateFlow<Boolean> = location
    override fun setLocationServicesEnabled(enabled: Boolean) {
        location.value = enabled
    }
}
