package com.askit.app.session

import com.askit.app.profile.ProfileGalleryItem
import com.askit.app.profile.ProfileReview
import com.askit.app.profile.SavedProfessional
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ServiceListing(
    val title: String,
    val category: String,
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
)

data class ProfileAvailability(
    val available: Boolean = true,
    val fromMillis: Long? = null,
    val toMillis: Long? = null,
    val message: String = "",
    val muteNotifications: Boolean = false,
)

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
    val availability: ProfileAvailability = ProfileAvailability(),
    val gallery: List<ProfileGalleryItem> = emptyList(),
    val reviews: List<ProfileReview> = emptyList(),
    val licenses: List<String> = emptyList(),
    val savedProfessionals: List<SavedProfessional> = emptyList(),
    val followingCount: Int = 0,
    val followerCount: Int = 0,
) {
    val showsServiceCard: Boolean get() = hasListedService && listing != null
}

class SessionProfileStore(
    initial: SessionProfile = SessionProfile(),
) {
    private val _profile = MutableStateFlow(initial)
    val profile: StateFlow<SessionProfile> = _profile.asStateFlow()

    fun markServiceListed() {
        _profile.update { current ->
            current.copy(
                hasListedService = true,
                listing = current.listing ?: DefaultCarpentryListing,
                skills = current.skills.ifEmpty { DefaultCarpentryListing.tags },
                profileStrengthPercent = 90,
            )
        }
    }

    fun applyListing(listing: ServiceListing) {
        _profile.update {
            it.copy(
                hasListedService = true,
                listing = listing,
                skills = listing.tags,
                profileStrengthPercent = maxOf(it.profileStrengthPercent, 90),
            )
        }
    }

    fun updateIdentity(
        displayName: String,
        username: String,
        city: String,
        bio: String,
        about: String = bio,
        avatarUrl: String?,
    ) {
        _profile.update {
            it.copy(
                displayName = displayName,
                username = username,
                city = city,
                bio = bio,
                about = about,
                avatarUrl = avatarUrl,
            )
        }
    }

    fun updateAbout(about: String) {
        _profile.update { it.copy(about = about, bio = it.bio) }
    }

    fun updateLookingFor(lookingFor: List<String>) {
        _profile.update { it.copy(lookingFor = lookingFor) }
    }

    fun updateSkills(skills: List<String>) {
        _profile.update { it.copy(skills = skills) }
    }

    fun updateAvailability(availability: ProfileAvailability) {
        _profile.update {
            it.copy(
                availability = availability,
                listing = it.listing?.copy(live = availability.available),
            )
        }
    }

    fun setAvatar(avatarUrl: String?) {
        _profile.update { it.copy(avatarUrl = avatarUrl) }
    }

    fun appendGallery(uris: List<String>) {
        if (uris.isEmpty()) return
        _profile.update { current ->
            val existing = current.gallery.map { it.uri }.toSet()
            val added = uris
                .filter { it.isNotBlank() }
                .distinct()
                .filter { it !in existing }
                .map { uri -> ProfileGalleryItem(id = uri, uri = uri) }
            if (added.isEmpty()) current else current.copy(gallery = current.gallery + added)
        }
    }

    fun appendReview(review: ProfileReview) {
        _profile.update { current ->
            if (current.reviews.any { it.id == review.id }) current else {
                current.copy(reviews = listOf(review) + current.reviews)
            }
        }
    }

    fun addLicense(license: String) {
        val trimmed = license.trim()
        if (trimmed.isEmpty()) return
        _profile.update { current ->
            if (trimmed in current.licenses) current else {
                current.copy(licenses = current.licenses + trimmed)
            }
        }
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
