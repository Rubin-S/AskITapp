package com.askit.app.session

import com.askit.app.auth.SignupDraft
import com.askit.app.format.pickerDate
import com.askit.app.listservice.ListServiceDraft
import com.askit.app.profile.ProfileGalleryItem
import com.askit.app.profile.ProfileReview
import com.askit.app.profile.SavedProfessional
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class ServiceListing(
    val title: String,
    val category: String,
    val categoryId: String? = null,
    val description: String,
    /** Legacy English label; new listings carry [pricing] instead (see ServiceListing.labels). */
    val quoteLabel: String = "",
    val coverage: String,
    /** Legacy English label; new listings carry [coverageRadiusKm] instead. */
    val coverageHint: String = "",
    val hours: String,
    val hoursHint: String,
    val response: String,
    /** Legacy English label; derived from [deliveryModes] when they are present. */
    val responseHint: String = "",
    val tags: List<String>,
    val experience: String,
    val live: Boolean = true,
    val phoneNumber: String = "",
    val email: String = "",
    val materials: String? = null,
    val certificate: String? = null,
    val deliveryModes: Set<com.askit.app.listservice.ListServiceDeliveryMode> = emptySet(),
    val pricing: com.askit.app.listservice.ListServicePricing? = null,
    val coverageRadiusKm: Int? = null,
)

/**
 * Availability window. `available = false` pauses the listing from [fromMillis] to [toMillis]
 * (both inclusive date-picker days, stored as UTC midnight). The effective status is computed at
 * read time from the window, so a future start only schedules the pause and the listing comes back
 * on its own the day after [toMillis]; no background job is needed.
 */
@Serializable
data class ProfileAvailability(
    val available: Boolean = true,
    val fromMillis: Long? = null,
    val toMillis: Long? = null,
    val message: String = "",
    val muteNotifications: Boolean = false,
)

/** What the availability window means on [today] (the device's calendar day). */
sealed interface AvailabilityStatus {
    data object Available : AvailabilityStatus
    data class Scheduled(val from: LocalDate) : AvailabilityStatus
    data class Away(val until: LocalDate?) : AvailabilityStatus
}

fun ProfileAvailability.statusOn(today: LocalDate): AvailabilityStatus {
    if (available) return AvailabilityStatus.Available
    val from = fromMillis?.let(::pickerDate)
    val until = toMillis?.let(::pickerDate)
    return when {
        until != null && today.isAfter(until) -> AvailabilityStatus.Available
        from != null && today.isBefore(from) -> AvailabilityStatus.Scheduled(from)
        else -> AvailabilityStatus.Away(until)
    }
}

/** A scheduled pause has not started yet, so the provider is still available until `from`. */
fun ProfileAvailability.isAvailableOn(today: LocalDate): Boolean = statusOn(today) !is AvailabilityStatus.Away

@Serializable
data class SessionProfile(
    // Neutral defaults: no demo persona. An unloaded/cleared profile is blank, never "someone".
    val displayName: String = "",
    val username: String = "",
    val bio: String = "",
    val about: String = "",
    val city: String = "",
    val joinedYear: String = "",
    val avatarUrl: String? = null,
    val lookingFor: List<String> = emptyList(),
    val skills: List<String> = emptyList(),
    val languages: List<Pair<String, String>> = emptyList(),
    val profileStrengthPercent: Int = 0,
    val hasListedService: Boolean = false,
    val listing: ServiceListing? = null,
    val listingDraft: ListServiceDraft? = null,
    val availability: ProfileAvailability = ProfileAvailability(),
    val gallery: List<ProfileGalleryItem> = emptyList(),
    val reviews: List<ProfileReview> = emptyList(),
    val licenses: List<String> = emptyList(),
    val savedProfessionals: List<SavedProfessional> = emptyList(),
    val followingCount: Int = 0,
    val followerCount: Int = 0,
    val phoneNumber: String = "",
    val activeRole: String = AccountRole.Seeker.storedValue,
    val pushNotificationsEnabled: Boolean = true,
    val jobAlertsEnabled: Boolean = true,
    val selectedLanguage: String = AppLanguage.English.storedValue,
    val whoCanMessage: String = MessagingAudience.Everyone.storedValue,
    val blockedAccountsCount: Int = 0,
    val pincode: String = "",
    val isVerified: Boolean = false,
    val email: String? = null,
    val accountId: String = "",
    val title: String = "",
    val bannerUrl: String? = null,
    val highlights: List<com.askit.app.profile.ProfileHighlight> = emptyList(),
    val discloseApproximateLocation: Boolean = false,
) {
    /** True when the account has a published service listing, so it can apply to tasks as a provider. */
    val hasActiveServiceListing: Boolean get() = hasListedService && listing != null

    /** Country code (for E.164) and the last two digits only, e.g. "+91 ••••••98". */
    val maskedPhoneNumber: String
        get() = when {
            phoneNumber.length < 4 -> phoneNumber
            phoneNumber.startsWith("+91") -> "+91 ••••••${phoneNumber.takeLast(2)}"
            else -> "${phoneNumber.take(2)}••••••${phoneNumber.takeLast(2)}"
        }
}

/**
 * The signed-in account's profile. It is also the profile repository: writes go straight to this
 * store and its persist hook (preview: durable snapshots; release: none). Call writes off the main
 * thread, since the preview hook copies media.
 */
class SessionProfileStore(initial: SessionProfile = SessionProfile(), private val persist: (SessionProfile) -> SessionProfile = { it }) :
    com.askit.app.profile.ProfileRepository {
    private val _profile = MutableStateFlow(initial)
    override val profile: StateFlow<SessionProfile> = _profile.asStateFlow()

    fun replaceAccount(profile: SessionProfile) {
        commit(profile)
    }

    override fun completeSignup(draft: SignupDraft) {
        commit(
            SessionProfile(
                accountId = _profile.value.accountId,
                displayName = draft.fullName,
                username = draft.username,
                city = if (draft.area.isNotBlank() && !draft.city.contains(draft.area, ignoreCase = true)) {
                    "${draft.area}, ${draft.city}"
                } else {
                    draft.city
                },
                joinedYear = java.time.Year.now().toString(),
                avatarUrl = draft.avatarUri,
                phoneNumber = draft.phoneNumber,
                activeRole = AccountRole.Seeker.storedValue,
                selectedLanguage = draft.preferredLanguage,
                pincode = draft.pincode,
                email = draft.email,
            ),
        )
    }

    override fun applyListing(listing: ServiceListing, draft: ListServiceDraft?) {
        update {
            it.copy(
                // Account phone/email change only through the verified identity flow, never as a
                // side effect of saving a service listing.
                hasListedService = true,
                listing = listing.copy(phoneNumber = "", email = ""),
                listingDraft = draft,
                skills = listing.tags,
                profileStrengthPercent = maxOf(it.profileStrengthPercent, LISTED_PROFILE_STRENGTH),
            )
        }
    }

    override fun updateIdentity(state: com.askit.app.profile.EditProfileFormState) {
        require(state.isValid)
        val account = state.accountDraft().normalized()
        update {
            it.copy(
                displayName = account.fullName,
                // Keeps the stored display prefix ("@") when only the prefix differs (profile contract).
                username = if (it.username.removePrefix("@") == account.username) it.username else account.username, city = account.city,
                bio = state.bio.trim(), title = state.title.trim(), avatarUrl = account.avatarUri,
                email = account.email, pincode = account.pincode, phoneNumber = account.phoneNumber,
                selectedLanguage = account.preferredLanguage, discloseApproximateLocation = state.discloseApproximateLocation,
                // Form B publishes the canonical account contact (profile contract), so it follows the account.
                listingDraft = it.listingDraft?.copy(contactPhone = account.phoneNumber, contactEmail = account.email.orEmpty()),
            )
        }
    }

    override fun saveHighlight(actorId: String, highlight: com.askit.app.profile.ProfileHighlight) = update { current ->
        require(actorId == current.accountId)
        highlight.validate(actorId)
        current.copy(
            highlights = if (current.highlights.any { it.id == highlight.id }) {
                current.highlights.map { if (it.id == highlight.id) highlight else it }
            } else {
                current.highlights + highlight
            },
        )
    }

    override fun deleteHighlight(actorId: String, id: String) = update { current ->
        require(actorId.isNotBlank() && actorId == current.accountId)
        current.copy(highlights = current.highlights.filterNot { it.id == id })
    }

    @Synchronized
    private fun update(transform: (SessionProfile) -> SessionProfile) = commit(transform(_profile.value))

    @Synchronized
    private fun commit(next: SessionProfile) {
        _profile.value = persist(next)
    }

    override fun setBanner(actorId: String, uri: String?) = update {
        require(actorId.isNotBlank() && actorId == it.accountId)
        it.copy(bannerUrl = uri)
    }

    /**
     * Stores the window only. `listing.live` stays the "published" flag (older builds cleared it when
     * pausing, so saving availability re-publishes); the effective status comes from [statusOn].
     */
    override fun updateAvailability(availability: ProfileAvailability) = update {
        require(availability.available || (availability.fromMillis != null && availability.toMillis != null))
        it.copy(availability = availability, listing = it.listing?.copy(live = true))
    }

    override fun setAvatar(actorId: String, avatarUrl: String?) = update {
        require(actorId.isNotBlank() && actorId == it.accountId)
        it.copy(avatarUrl = avatarUrl)
    }

    override fun updateNotifications(push: Boolean, jobAlerts: Boolean) = update {
        it.copy(pushNotificationsEnabled = push, jobAlertsEnabled = jobAlerts)
    }

    override fun updateLanguage(lang: String) = update {
        require(lang in AppLanguage.storedValues)
        it.copy(selectedLanguage = lang)
    }

    override fun updateWhoCanMessage(option: String) = update {
        require(option in MessagingAudience.storedValues)
        it.copy(whoCanMessage = option)
    }

    /**
     * Forgets the account in memory. The blank profile has no account id, so the preview persist
     * hook never writes it over the stored persona (that left "Couldn't load profile" behind).
     */
    override fun resetAppData() = commit(SessionProfile())

    private companion object {
        const val LISTED_PROFILE_STRENGTH = 90
    }
}
