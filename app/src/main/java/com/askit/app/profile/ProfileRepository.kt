package com.askit.app.profile

import com.askit.app.session.ProfileAvailability
import com.askit.app.session.SessionProfile
import com.askit.app.session.SessionProfileStore
import kotlinx.coroutines.flow.StateFlow

/**
 * Frontend seam for profile. Local implementation today; HTTP later maps the same domain types.
 */
interface ProfileRepository {
    val profile: StateFlow<SessionProfile>

    fun updateIdentity(
        displayName: String,
        username: String,
        city: String,
        bio: String,
        about: String,
        avatarUrl: String?,
    )

    fun updateAbout(about: String)
    fun updateLookingFor(lookingFor: List<String>)
    fun updateSkills(skills: List<String>)
    fun updateAvailability(availability: ProfileAvailability)
    fun setAvatar(avatarUrl: String?)
    fun appendGallery(uris: List<String>)
    fun appendReview(review: ProfileReview)
    fun addLicense(license: String)
    fun updateActiveRole(role: String)
    fun updatePhoneNumber(phone: String)
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

    override fun updateIdentity(
        displayName: String,
        username: String,
        city: String,
        bio: String,
        about: String,
        avatarUrl: String?,
    ) {
        store.updateIdentity(displayName, username, city, bio, about, avatarUrl)
    }

    override fun updateAbout(about: String) = store.updateAbout(about)

    override fun updateLookingFor(lookingFor: List<String>) = store.updateLookingFor(lookingFor)

    override fun updateSkills(skills: List<String>) = store.updateSkills(skills)

    override fun updateAvailability(availability: ProfileAvailability) =
        store.updateAvailability(availability)

    override fun setAvatar(avatarUrl: String?) = store.setAvatar(avatarUrl)

    override fun appendGallery(uris: List<String>) = store.appendGallery(uris)

    override fun appendReview(review: ProfileReview) = store.appendReview(review)

    override fun addLicense(license: String) = store.addLicense(license)

    override fun updateActiveRole(role: String) = store.updateActiveRole(role)

    override fun updatePhoneNumber(phone: String) = store.updatePhoneNumber(phone)

    override fun updatePushNotifications(enabled: Boolean) = store.updatePushNotifications(enabled)

    override fun updateJobAlerts(enabled: Boolean) = store.updateJobAlerts(enabled)

    override fun updateLanguage(lang: String) = store.updateLanguage(lang)

    override fun updateLocationServices(enabled: Boolean) = store.updateLocationServices(enabled)

    override fun updateWhoCanMessage(option: String) = store.updateWhoCanMessage(option)

    override fun resetAppData() = store.resetAppData()
}
