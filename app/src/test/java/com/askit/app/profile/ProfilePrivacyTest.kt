package com.askit.app.profile

import com.askit.app.auth.parseCurrentAccount
import com.askit.app.home.details.UserProfileData
import com.askit.app.session.ServiceListing
import com.askit.app.session.SessionProfile
import com.askit.app.session.SessionProfileStore
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** A profile shown to other people never carries the owner's phone, email or PIN code. */
class ProfilePrivacyTest {
    private val owner = SessionProfile(
        accountId = "owner",
        displayName = "Asha",
        city = "Karaikal",
        phoneNumber = "+919876543210",
        email = "asha@example.com",
        pincode = "609609",
        listing = listing(phone = "+919876543210", email = "asha@example.com"),
        hasListedService = true,
    )

    @Test fun thePublicProfileModelHasNoContactOrPostcodeFields() {
        val fields = UserProfileData::class.java.declaredFields.map { it.name.lowercase() }
        // postcodeDistanceKm is a server-computed distance between postcode centroids, not a postcode.
        val leaks = fields.filter { name ->
            name != "postcodedistancekm" && listOf("phone", "email", "pincode", "postcode").any { it in name }
        }
        assertEquals(emptyList<String>(), leaks)
    }

    @Test fun thePublicAdapterStripsListingContact() {
        val public = owner.toPublicProfile()
        assertEquals("", public.listing?.phoneNumber)
        assertEquals("", public.listing?.email)
        assertEquals("Asha", public.name)
    }

    @Test fun savingAListingNeverPublishesContactDetails() {
        val store = SessionProfileStore(owner.copy(listing = null, hasListedService = false))
        store.applyListing(listing(phone = "+919876543210", email = "asha@example.com"), draft = null)
        assertEquals("", store.profile.value.listing?.phoneNumber)
        assertEquals("", store.profile.value.listing?.email)
        assertEquals("+919876543210", store.profile.value.phoneNumber)
    }

    @Test fun contactSentByTheServerInAListingIsDropped() {
        val body = """
            {"userId":"owner","phoneNumber":"+919876543210","profile":{
              "displayName":"Asha","username":"asha","city":"Karaikal","joinedYear":"2026",
              "listing":{"title":"Carpentry","category":"carpentry","description":"Doors","coverage":"Karaikal",
                "hours":"9-5","hoursHint":"","response":"1h","tags":[],"experience":"5",
                "phoneNumber":"+919876543210","email":"asha@example.com"}}}
        """.trimIndent()
        val listing = parseCurrentAccount(body, Json { ignoreUnknownKeys = true }).profile?.listing
        assertEquals("", listing?.phoneNumber)
        assertEquals("", listing?.email)
    }

    @Test fun distanceIsShownOnlyWithConsentAndAValidViewerPostcode() {
        assertEquals(4, approximateProfileDistance(consent = true, viewerPostcode = "609609", distanceKm = 3.6))
        assertNull(approximateProfileDistance(consent = false, viewerPostcode = "609609", distanceKm = 3.6))
        assertNull(approximateProfileDistance(consent = true, viewerPostcode = null, distanceKm = 3.6))
        assertNull(approximateProfileDistance(consent = true, viewerPostcode = "12", distanceKm = 3.6))
        assertNull(approximateProfileDistance(consent = true, viewerPostcode = "609609", distanceKm = Double.NaN))
    }

    private fun listing(phone: String, email: String) = ServiceListing(
        title = "Carpentry",
        category = "carpentry",
        description = "Doors and windows",
        coverage = "Karaikal",
        hours = "9-5",
        hoursHint = "",
        response = "1h",
        tags = emptyList(),
        experience = "5",
        phoneNumber = phone,
        email = email,
    )
}
