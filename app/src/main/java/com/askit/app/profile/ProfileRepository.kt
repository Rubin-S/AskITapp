package com.askit.app.profile

import com.askit.app.auth.SignupDraft
import com.askit.app.listservice.ListServiceDraft
import com.askit.app.session.ProfileAvailability
import com.askit.app.session.ServiceListing
import com.askit.app.session.SessionProfile
import kotlinx.coroutines.flow.StateFlow

/**
 * Profile seam. [com.askit.app.session.SessionProfileStore] implements it today (local, with the
 * preview persist hook); an HTTP implementation maps the same domain types later. Calls may block
 * on disk, so callers run them off the main thread.
 */
interface ProfileRepository {
    val profile: StateFlow<SessionProfile>

    fun completeSignup(draft: SignupDraft)
    fun updateIdentity(state: EditProfileFormState)
    fun applyListing(listing: ServiceListing, draft: ListServiceDraft?)
    fun updateAvailability(availability: ProfileAvailability)
    fun saveHighlight(actorId: String, highlight: ProfileHighlight)
    fun deleteHighlight(actorId: String, id: String)
    fun setBanner(actorId: String, uri: String?)
    fun setAvatar(actorId: String, avatarUrl: String?)
    fun updateNotifications(push: Boolean, jobAlerts: Boolean)
    fun updateLanguage(lang: String)
    fun updateWhoCanMessage(option: String)
    fun resetAppData()
}

/**
 * What the profile backend can do in this build: the one gate every profile control reads, so the
 * app never offers an action that can't succeed. The client preview (local storage) can do all of
 * it. Release waits for the profile PATCH, listing, social and dashboard contracts, so its controls
 * are hidden until a server capability turns them on.
 */
data class ProfileCapabilities(
    /** Identity, photo, banner, highlights, service listing, availability, message privacy and notification settings. */
    val editProfile: Boolean,
    /** Follow, message, block and report other people, and other people's public profiles. */
    val social: Boolean,
    /** Provider and creator dashboards. */
    val dashboards: Boolean,
    /** Posting a task from the profile. */
    val postTask: Boolean,
) {
    companion object {
        fun forBuild(clientPreview: Boolean) = ProfileCapabilities(clientPreview, clientPreview, clientPreview, clientPreview)
    }
}

/** Deletes everything the app keeps on this device: preview storage, photos and media caches. */
fun interface LocalDataCleaner {
    fun clear()
}

/** Device-level settings that are not part of the account, so `/v1/me` never overwrites them. */
interface DeviceSettings {
    val locationServicesEnabled: StateFlow<Boolean>
    fun setLocationServicesEnabled(enabled: Boolean)
}
