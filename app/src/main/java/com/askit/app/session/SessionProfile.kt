package com.askit.app.session

import com.askit.app.listservice.ListServiceDraft
import com.askit.app.auth.SignupDraft
import com.askit.app.profile.ProfileGalleryItem
import com.askit.app.profile.ProfileReview
import com.askit.app.profile.SavedProfessional
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable

@Serializable
data class ServiceListing(
    val title: String,
    val category: String,
    val categoryId: String? = null,
    val description: String,
    val quoteLabel: String,
    val coverage: String,
    val coverageHint: String,
    val hours: String,
    val hoursHint: String,
    val response: String,
    val responseHint: String,
    val tags: List<String>,
    val experience: String,
    val live: Boolean = true,
    val phoneNumber: String = "",
    val email: String = "",
    val materials: String? = null,
    val certificate: String? = null,
    val deliveryModes: Set<com.askit.app.listservice.ListServiceDeliveryMode> = emptySet(),
)

@Serializable
data class ProfileAvailability(
    val available: Boolean = true,
    val fromMillis: Long? = null,
    val toMillis: Long? = null,
    val message: String = "",
    val muteNotifications: Boolean = false,
)

@Serializable
data class SessionProfile(
    val displayName: String = "Meera Raman",
    val username: String = "meera.raman",
    val bio: String = "Homeowner · Hiring local pros for home care.",
    val about: String = "Homeowner looking after a small independent house — mostly plumbing, electrical, and the occasional carpentry job. I like booking providers who show up on time and explain the work before starting.",
    val city: String = "Gandhipuram, Coimbatore",
    val joinedYear: String = "2023",
    val avatarUrl: String? = null,
    val lookingFor: List<String> = listOf(
        "Plumbing",
        "Electrical repair",
        "Carpentry",
        "Home painting",
        "AC service",
    ),
    val skills: List<String> = emptyList(),
    val languages: List<Pair<String, String>> = listOf("Tamil" to "Fluent", "English" to "Fluent"),
    val profileStrengthPercent: Int = 72,
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
    val phoneNumber: String = "9876543298",
    val activeRole: String = "Both",
    val pushNotificationsEnabled: Boolean = true,
    val jobAlertsEnabled: Boolean = true,
    val selectedLanguage: String = "English",
    val locationServicesEnabled: Boolean = true,
    val whoCanMessage: String = "Everyone",
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
    val showsServiceCard: Boolean get() = hasListedService && listing != null
    val maskedPhoneNumber: String
        get() = if (phoneNumber.length >= 4) {
            "${phoneNumber.take(2)}••••••${phoneNumber.takeLast(2)}"
        } else {
            phoneNumber
        }
}

class SessionProfileStore(
    initial: SessionProfile = SessionProfile(),
    private val persist: (SessionProfile) -> SessionProfile = { it },
) {
    private val _profile = MutableStateFlow(initial)
    val profile: StateFlow<SessionProfile> = _profile.asStateFlow()

    fun replaceAccount(profile: SessionProfile) {
        commit(profile)
    }

    fun completeSignup(draft: SignupDraft) {
        commit(SessionProfile(
            accountId = _profile.value.accountId,
            displayName = draft.fullName,
            username = draft.username,
            bio = "",
            about = "",
            city = if (draft.area.isNotBlank() && !draft.city.contains(draft.area, ignoreCase = true)) {
                "${draft.area}, ${draft.city}"
            } else {
                draft.city
            },
            joinedYear = java.time.Year.now().toString(),
            avatarUrl = draft.avatarUri,
            lookingFor = emptyList(),
            languages = emptyList(),
            profileStrengthPercent = 0,
            phoneNumber = draft.phoneNumber,
            activeRole = "Seeker",
            selectedLanguage = draft.preferredLanguage,
            pincode = draft.pincode,
            email = draft.email,
        ))
    }

    fun applyListing(listing: ServiceListing, draft: ListServiceDraft? = null) {
        update {
            it.copy(
                hasListedService = true,
                phoneNumber = draft?.contactPhone?.takeIf(String::isNotBlank) ?: it.phoneNumber,
                email = draft?.contactEmail?.takeIf(String::isNotBlank) ?: it.email,
                listing = listing,
                listingDraft = draft,
                skills = listing.tags,
                profileStrengthPercent = maxOf(it.profileStrengthPercent, 90),
            )
        }
    }

    fun updateIdentity(state: com.askit.app.profile.EditProfileFormState) {
        require(state.isValid)
        val account = state.accountDraft().normalized()
        update { it.copy(displayName = account.fullName,
            username = if (it.username.removePrefix("@") == account.username) it.username else account.username, city = account.city,
            bio = state.bio.trim(), title = state.title.trim(), avatarUrl = account.avatarUri,
            email = account.email, pincode = account.pincode, phoneNumber = account.phoneNumber,
            selectedLanguage = account.preferredLanguage, discloseApproximateLocation = state.discloseApproximateLocation,
            listing = it.listing?.copy(phoneNumber = account.phoneNumber, email = account.email.orEmpty()),
            listingDraft = it.listingDraft?.copy(contactPhone = account.phoneNumber, contactEmail = account.email.orEmpty())) }
    }

    fun saveHighlight(actorId: String, highlight: com.askit.app.profile.ProfileHighlight) = update { current ->
        require(actorId == current.accountId)
        highlight.validate(actorId)
        current.copy(highlights = if (current.highlights.any { it.id == highlight.id }) {
            current.highlights.map { if (it.id == highlight.id) highlight else it }
        } else current.highlights + highlight)
    }

    fun deleteHighlight(actorId: String, id: String) = update { current ->
        require(actorId.isNotBlank() && actorId == current.accountId)
        current.copy(highlights = current.highlights.filterNot { it.id == id })
    }

    @Synchronized
    private fun update(transform: (SessionProfile) -> SessionProfile) = commit(transform(_profile.value))

    @Synchronized
    private fun commit(next: SessionProfile) { _profile.value = persist(next) }

    fun setBanner(actorId: String, uri: String?) = update {
        require(actorId.isNotBlank() && actorId == it.accountId)
        it.copy(bannerUrl = uri)
    }

    fun updateAvailability(availability: ProfileAvailability) {
        update {
            it.copy(
                availability = availability,
                listing = it.listing?.copy(live = availability.available),
            )
        }
    }

    fun setAvatar(actorId: String, avatarUrl: String?) = update {
        require(actorId.isNotBlank() && actorId == it.accountId)
        it.copy(avatarUrl = avatarUrl)
    }

    fun appendReview(review: ProfileReview) {
        update { current ->
            require(review.recipientAccountId == current.accountId && review.reviewerAccountId != current.accountId && review.direction != null)
            if (current.reviews.any { it.id == review.id }) current else {
                current.copy(reviews = listOf(review) + current.reviews)
            }
        }
    }

    fun updateActiveRole(role: String) {
        update { it.copy(activeRole = role) }
    }

    fun updatePushNotifications(enabled: Boolean) {
        update { it.copy(pushNotificationsEnabled = enabled) }
    }

    fun updateJobAlerts(enabled: Boolean) {
        update { it.copy(jobAlertsEnabled = enabled) }
    }

    fun updateLanguage(lang: String) {
        update { it.copy(selectedLanguage = lang) }
    }

    fun updateLocationServices(enabled: Boolean) {
        update { it.copy(locationServicesEnabled = enabled) }
    }

    fun updateWhoCanMessage(option: String) {
        require(option in setOf("Everyone", "Contacts only", "Verified users only"))
        update { it.copy(whoCanMessage = option) }
    }

    fun resetAppData() {
        commit(SessionProfile(accountId = _profile.value.accountId))
    }
}

val DefaultCarpentryListing = ServiceListing(
    title = "Custom carpentry & woodwork",
    category = "Carpentry",
    description = "Quality woodworking from custom furniture and cabinets to repairs. Clear quotes before work starts.",
    quoteLabel = "Quote on visit",
    coverage = "Gandhipuram",
    coverageHint = "Up to 10 km",
    hours = "9:00 AM – 7:00 PM",
    hoursHint = "6 days / week",
    response = "within 1 hour",
    responseHint = "Travels to site",
    tags = listOf("Custom furniture", "Cabinets", "Repairs"),
    experience = "3 yrs exp",
    live = true,
)
