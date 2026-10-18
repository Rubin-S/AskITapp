package com.askit.app.profile

import android.content.ContentResolver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.askit.app.R
import com.askit.app.auth.SignupDraft
import com.askit.app.auth.isSignupPhotoReadable
import com.askit.app.session.ProfileAvailability
import com.askit.app.session.SessionProfile
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed interface ProfileLoadState {
    data object Loading : ProfileLoadState
    data object Ready : ProfileLoadState
    data class Failed(val messageRes: Int) : ProfileLoadState
}

class ProfileViewModel(
    private val repository: ProfileRepository,
) : ViewModel() {
    val profile: StateFlow<SessionProfile> = repository.profile

    /** Follows the profile, so a later sign-in or signup moves a blank profile to Ready. */
    val loadState: StateFlow<ProfileLoadState> = repository.profile
        .map(::loadStateFor)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), loadStateFor(repository.profile.value))

    private val _messages = MutableSharedFlow<Int>(extraBufferCapacity = 1)
    val messages: SharedFlow<Int> = _messages.asSharedFlow()

    suspend fun completeSignup(draft: SignupDraft, contentResolver: ContentResolver): Boolean {
        if (!com.askit.app.BuildConfig.CLIENT_PREVIEW) { emitError(); return false }
        val photoReadable = withContext(Dispatchers.IO) {
            isSignupPhotoReadable(contentResolver, draft.avatarUri)
        }
        if (!draft.validation(photoReadable).isValid) return false
        return runCatching {
            repository.completeSignup(draft.normalized())
        }.isSuccess
    }

    fun saveIdentity(state: EditProfileFormState): Boolean {
        if (!state.isValid) return false
        if (!com.askit.app.BuildConfig.CLIENT_PREVIEW) {
            emitError()
            return false // Profile mutation/phone verification requires the backend contract.
        }
        return runCatching { repository.updateIdentity(state) }.onFailure { emitError() }.isSuccess
    }

    fun updateAvailability(availability: ProfileAvailability) =
        runCatching { repository.updateAvailability(availability) }.onFailure { emitError() }

    fun saveHighlight(actorId: String, highlight: ProfileHighlight): Boolean =
        com.askit.app.BuildConfig.CLIENT_PREVIEW && runCatching { repository.saveHighlight(actorId, highlight) }.isSuccess

    fun deleteHighlight(actorId: String, id: String): Boolean =
        com.askit.app.BuildConfig.CLIENT_PREVIEW && runCatching { repository.deleteHighlight(actorId, id) }.isSuccess

    fun setBanner(actorId: String, uri: String?): Boolean {
        if (!com.askit.app.BuildConfig.CLIENT_PREVIEW) return false
        return runCatching { repository.setBanner(actorId, uri) }.isSuccess
    }

    fun setAvatar(actorId: String, avatarUrl: String?) {
        if (!com.askit.app.BuildConfig.CLIENT_PREVIEW) { emitError(); return }
        runCatching { repository.setAvatar(actorId, avatarUrl) }.onFailure { emitError() }
    }

    fun updateActiveRole(role: String) =
        runCatching { repository.updateActiveRole(role) }.onFailure { emitError() }

    fun updatePushNotifications(enabled: Boolean) =
        runCatching { repository.updatePushNotifications(enabled) }.onFailure { emitError() }

    fun updateJobAlerts(enabled: Boolean) =
        runCatching { repository.updateJobAlerts(enabled) }.onFailure { emitError() }

    fun updateLanguage(lang: String) =
        runCatching { repository.updateLanguage(lang) }.onFailure { emitError() }

    fun updateLocationServices(enabled: Boolean) =
        runCatching { repository.updateLocationServices(enabled) }.onFailure { emitError() }

    fun updateWhoCanMessage(option: String): Boolean {
        if (!com.askit.app.BuildConfig.CLIENT_PREVIEW) return false
        return runCatching { repository.updateWhoCanMessage(option) }.isSuccess
    }

    fun resetAppData() =
        runCatching {
            repository.resetAppData()
            _messages.tryEmit(R.string.settings_data_cleared)
        }.onFailure { emitError() }

    private fun emitError() {
        _messages.tryEmit(R.string.profile_action_error)
    }
}

internal fun loadStateFor(profile: SessionProfile): ProfileLoadState =
    if (profile.displayName.isNotBlank()) ProfileLoadState.Ready else ProfileLoadState.Failed(R.string.profile_load_error)
