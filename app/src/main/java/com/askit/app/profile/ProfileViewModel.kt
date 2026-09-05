package com.askit.app.profile

import androidx.lifecycle.ViewModel
import com.askit.app.R
import com.askit.app.session.ProfileAvailability
import com.askit.app.session.SessionProfile
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface ProfileLoadState {
    data object Loading : ProfileLoadState
    data object Ready : ProfileLoadState
    data class Failed(val messageRes: Int) : ProfileLoadState
}

class ProfileViewModel(
    private val repository: ProfileRepository,
) : ViewModel() {
    val profile: StateFlow<SessionProfile> = repository.profile

    private val _loadState = MutableStateFlow<ProfileLoadState>(ProfileLoadState.Loading)
    val loadState: StateFlow<ProfileLoadState> = _loadState.asStateFlow()

    private val _messages = MutableSharedFlow<Int>(extraBufferCapacity = 1)
    val messages: SharedFlow<Int> = _messages.asSharedFlow()

    init {
        _loadState.value = if (repository.profile.value.username.isNotBlank()) {
            ProfileLoadState.Ready
        } else {
            ProfileLoadState.Failed(R.string.profile_load_error)
        }
    }

    fun saveIdentity(state: EditProfileFormState) {
        runCatching {
            val current = repository.profile.value
            repository.updateIdentity(
                displayName = state.displayName.trim(),
                username = state.username.trim(),
                city = state.city.trim(),
                bio = state.bio.trim(),
                about = current.about,
                avatarUrl = state.avatarUrl,
            )
        }.onFailure { emitError() }
    }

    fun updateAbout(about: String) = runCatching { repository.updateAbout(about) }.onFailure { emitError() }

    fun updateLookingFor(lookingFor: List<String>) =
        runCatching { repository.updateLookingFor(lookingFor) }.onFailure { emitError() }

    fun updateSkills(skills: List<String>) =
        runCatching { repository.updateSkills(skills) }.onFailure { emitError() }

    fun updateAvailability(availability: ProfileAvailability) =
        runCatching { repository.updateAvailability(availability) }.onFailure { emitError() }

    fun setAvatar(avatarUrl: String?) =
        runCatching { repository.setAvatar(avatarUrl) }.onFailure { emitError() }

    fun appendGallery(uris: List<String>) =
        runCatching { repository.appendGallery(uris) }.onFailure { emitError() }

    fun appendReview(review: ProfileReview) =
        runCatching { repository.appendReview(review) }.onFailure { emitError() }

    fun addLicense(license: String) =
        runCatching { repository.addLicense(license) }.onFailure { emitError() }

    fun updateActiveRole(role: String) =
        runCatching { repository.updateActiveRole(role) }.onFailure { emitError() }

    fun updatePhoneNumber(phone: String) =
        runCatching { repository.updatePhoneNumber(phone) }.onFailure { emitError() }

    fun updatePushNotifications(enabled: Boolean) =
        runCatching { repository.updatePushNotifications(enabled) }.onFailure { emitError() }

    fun updateJobAlerts(enabled: Boolean) =
        runCatching { repository.updateJobAlerts(enabled) }.onFailure { emitError() }

    fun updateLanguage(lang: String) =
        runCatching { repository.updateLanguage(lang) }.onFailure { emitError() }

    fun updateLocationServices(enabled: Boolean) =
        runCatching { repository.updateLocationServices(enabled) }.onFailure { emitError() }

    fun updateWhoCanMessage(option: String) =
        runCatching { repository.updateWhoCanMessage(option) }.onFailure { emitError() }

    fun resetAppData() =
        runCatching {
            repository.resetAppData()
            _messages.tryEmit(R.string.settings_data_cleared)
        }.onFailure { emitError() }

    fun notifyCopied() {
        _messages.tryEmit(R.string.profile_username_copied)
    }

    private fun emitError() {
        _messages.tryEmit(R.string.profile_action_error)
    }
}

fun SessionProfile.locationLine(joinedLabel: String): String =
    "$city · $joinedLabel"
