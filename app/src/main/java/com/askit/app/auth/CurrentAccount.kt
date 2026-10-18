package com.askit.app.auth

import com.askit.app.listservice.ListServiceDraft
import com.askit.app.listservice.toServiceListing
import com.askit.app.profile.ProfileHighlight
import com.askit.app.profile.ProfileGalleryItem
import com.askit.app.profile.ProfileReview
import com.askit.app.profile.SavedProfessional
import com.askit.app.session.ProfileAvailability
import com.askit.app.session.ServiceListing
import com.askit.app.session.SessionProfile
import kotlinx.serialization.Serializable
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

data class CurrentAccount(
    val userId: String,
    val phoneNumber: String,
    val profile: SessionProfile?,
)

@Serializable
internal data class AccountResponse(val userId: String, val phoneNumber: String, val profile: AccountProfile?)

@Serializable
internal data class AccountLanguage(val name: String, val proficiency: String)

@Serializable
internal data class AccountProfile(
    val displayName: String,
    val username: String,
    val city: String = "",
    val joinedYear: String,
    val bio: String = "",
    val about: String = "",
    val avatarUrl: String? = null,
    val lookingFor: List<String> = emptyList(),
    val skills: List<String> = emptyList(),
    val languages: List<AccountLanguage> = emptyList(),
    val profileStrengthPercent: Int = 0,
    val listing: ServiceListing? = null,
    val listingDraft: ListServiceDraft? = null,
    val highlights: List<ProfileHighlight> = emptyList(),
    val availability: ProfileAvailability = ProfileAvailability(),
    val gallery: List<ProfileGalleryItem> = emptyList(),
    val reviews: List<ProfileReview> = emptyList(),
    val licenses: List<String> = emptyList(),
    val savedProfessionals: List<SavedProfessional> = emptyList(),
    val followingCount: Int = 0,
    val followerCount: Int = 0,
    val activeRole: String = "Seeker",
    val pushNotificationsEnabled: Boolean = true,
    val jobAlertsEnabled: Boolean = true,
    val whoCanMessage: String = "Everyone",
    val blockedAccountsCount: Int = 0,
    val pincode: String = "",
    val isVerified: Boolean = false,
    val email: String = "",
    val selectedLanguage: String = "English",
    val title: String = "",
    val bannerUrl: String? = null,
    val discloseApproximateLocation: Boolean = false,
) {
    internal fun toSessionProfile(phoneNumber: String, accountId: String): SessionProfile {
        if (accountId.isBlank() || highlights.map { it.id }.distinct().size != highlights.size ||
            highlights.any { runCatching { it.validate(accountId) }.isFailure }) throw AuthApiException(AuthError.InvalidResponse)
        val canonicalListing = listingDraft?.toServiceListing(listing?.category ?: listingDraft.categoryId)
            ?.copy(live = listing?.live ?: availability.available) ?: listing
        if (listingDraft != null && (listing == null || listing.title != listingDraft.title ||
            listing.description != listingDraft.description || listing.phoneNumber != phoneNumber || listing.email != email ||
            listingDraft.contactPhone != phoneNumber || listingDraft.contactEmail != email)) throw AuthApiException(AuthError.InvalidResponse)
        if (displayName.isBlank() || !com.askit.app.profile.isUsernameValid(username.removePrefix("@")) || !validAccountEmail(email) || !validAccountPostcode(pincode) ||
            !validAccountPhone(phoneNumber) || selectedLanguage !in setOf("English", "Tamil") || !joinedYear.matches(Regex("[0-9]{4}")) ||
            profileStrengthPercent !in 0..100 || followingCount < 0 || followerCount < 0 || blockedAccountsCount < 0 ||
            activeRole !in setOf("Seeker", "Provider", "Both") ||
            whoCanMessage !in setOf("Everyone", "Contacts only", "Verified users only") ||
            reviews.any { it.rating !in 0f..5f }
        ) throw AuthApiException(AuthError.InvalidResponse)
        for (ids in listOf(gallery.map { it.id }, reviews.map { it.id }, savedProfessionals.map { it.id })) {
            if (ids.any(String::isBlank) || ids.distinct().size != ids.size) throw AuthApiException(AuthError.InvalidResponse)
        }
        val mediaUrls = listOfNotNull(avatarUrl, bannerUrl) + gallery.map { it.uri } +
            reviews.mapNotNull { it.avatarUrl } + savedProfessionals.mapNotNull { it.avatarUrl }
        if (mediaUrls.any { value ->
            val url = value.toHttpUrlOrNull()
            value.length > 2_048 || url == null || !url.isHttps || url.username.isNotEmpty() || url.password.isNotEmpty()
        }) throw AuthApiException(AuthError.InvalidResponse)
        return SessionProfile(
            accountId = accountId, highlights = highlights, listingDraft = listingDraft,
            displayName = displayName,
            username = username,
            bio = bio,
            title = title, email = email, selectedLanguage = selectedLanguage,
            about = about,
            city = city,
            joinedYear = joinedYear,
            avatarUrl = avatarUrl, bannerUrl = bannerUrl, discloseApproximateLocation = discloseApproximateLocation,
            lookingFor = lookingFor,
            skills = skills,
            languages = languages.map { it.name to it.proficiency },
            profileStrengthPercent = profileStrengthPercent,
            hasListedService = canonicalListing != null,
            listing = canonicalListing,
            availability = availability,
            gallery = gallery,
            reviews = reviews,
            licenses = licenses,
            savedProfessionals = savedProfessionals,
            followingCount = followingCount,
            followerCount = followerCount,
            phoneNumber = phoneNumber,
            activeRole = activeRole,
            pushNotificationsEnabled = pushNotificationsEnabled,
            jobAlertsEnabled = jobAlertsEnabled,
            whoCanMessage = whoCanMessage,
            blockedAccountsCount = blockedAccountsCount,
            pincode = pincode,
            isVerified = isVerified,
        )
    }
}
