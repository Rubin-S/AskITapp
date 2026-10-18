package com.askit.app.profile

import com.askit.app.session.ProfileAvailability
import com.askit.app.auth.SignupDraft
import com.askit.app.session.SessionProfile
import com.askit.app.session.SessionProfileStore
import kotlinx.coroutines.flow.StateFlow

/**
 * Frontend seam for profile. Local implementation today; HTTP later maps the same domain types.
 */
interface ProfileRepository {
    val profile: StateFlow<SessionProfile>

    fun completeSignup(draft: SignupDraft)

    fun updateIdentity(state: EditProfileFormState)

    fun updateAvailability(availability: ProfileAvailability)
    fun saveHighlight(actorId: String, highlight: ProfileHighlight)
    fun deleteHighlight(actorId: String, id: String)
    fun setBanner(actorId: String, uri: String?)
    fun setAvatar(actorId: String, avatarUrl: String?)

    fun updateActiveRole(role: String)

    fun updatePushNotifications(enabled: Boolean)
    fun updateJobAlerts(enabled: Boolean)
    fun updateLanguage(lang: String)
    fun updateLocationServices(enabled: Boolean)
    fun updateWhoCanMessage(option: String)
    fun resetAppData()
}

class LocalProfileRepository(
    private val store: SessionProfileStore,
) : ProfileRepository {
    override val profile: StateFlow<SessionProfile> = store.profile

    override fun completeSignup(draft: SignupDraft) = store.completeSignup(draft)

    override fun updateIdentity(state: EditProfileFormState) = store.updateIdentity(state)

    override fun updateAvailability(availability: ProfileAvailability) =
        store.updateAvailability(availability)

    override fun saveHighlight(actorId: String, highlight: ProfileHighlight) = store.saveHighlight(actorId, highlight)
    override fun deleteHighlight(actorId: String, id: String) = store.deleteHighlight(actorId, id)

    override fun setBanner(actorId: String, uri: String?) = store.setBanner(actorId, uri)

    override fun setAvatar(actorId: String, avatarUrl: String?) = store.setAvatar(actorId, avatarUrl)

    override fun updateActiveRole(role: String) = store.updateActiveRole(role)

    override fun updatePushNotifications(enabled: Boolean) = store.updatePushNotifications(enabled)

    override fun updateJobAlerts(enabled: Boolean) = store.updateJobAlerts(enabled)

    override fun updateLanguage(lang: String) = store.updateLanguage(lang)

    override fun updateLocationServices(enabled: Boolean) = store.updateLocationServices(enabled)

    override fun updateWhoCanMessage(option: String) = store.updateWhoCanMessage(option)

    override fun resetAppData() = store.resetAppData()
}
