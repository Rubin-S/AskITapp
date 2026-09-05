package com.askit.app.profile

import com.askit.app.home.details.UserProfileData
import com.askit.app.home.details.getUserProfileById
import com.askit.app.jobs.Job
import com.askit.app.jobs.JobKind
import com.askit.app.jobs.JobParty
import com.askit.app.jobs.JobStatus
import com.askit.app.jobs.JobWorkMode
import com.askit.app.session.ServiceListing
import com.askit.app.session.SessionProfile
import com.askit.designsystem.R as DsR
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit & Contract test suite for [ProfileUiState] and state projection engine.
 * Tests Form A vs Form B state projection, tab lists, metric counts, and preview state.
 */
class ProfileUiStateTest {

    private fun sampleJob(id: String, status: JobStatus = JobStatus.Completed): Job {
        return Job(
            id = id,
            title = "Repair Task $id",
            counterpartName = "Client A",
            kind = JobKind.TaskApplication,
            localParty = JobParty.Receiver,
            status = status,
            workMode = JobWorkMode.OnSite,
            locationLabel = "Indiranagar",
            otp = "1234",
            inHistory = false,
        )
    }

    // =========================================================================
    // 1. SessionProfile -> Form A Projection Tests
    // =========================================================================

    @Test
    fun sessionProfile_formA_projection_hasThreeMetricsThreeTabsAndNoTradeHeadline() {
        val jobs = listOf(sampleJob("j1"), sampleJob("j2"), sampleJob("j3"))
        val session = SessionProfile(
            displayName = "Rahul Verma",
            username = "rahul_v",
            bio = "Homeowner in Indiranagar",
            city = "Bengaluru",
            joinedYear = "2023",
            hasListedService = false,
            listing = null,
            followerCount = 84,
            followingCount = 31,
        )

        val uiState = session.toUiState(jobs = jobs)

        assertFalse("Form A must not be a provider", uiState.isProvider)
        assertNull("Form A trade headline must be null", uiState.tradeHeadline)
        assertEquals("Rahul Verma", uiState.displayName)
        assertEquals("rahul_v", uiState.username)
        assertEquals("Bengaluru · Joined 2023", uiState.localityLine)

        // Metrics: exactly 3 (Activity, Followers, Following)
        assertEquals("Form A must have exactly 3 metrics", 3, uiState.metrics.size)
        assertEquals("activity", uiState.metrics[0].id)
        assertEquals("3", uiState.metrics[0].value)
        assertEquals("Activity", uiState.metrics[0].label)

        assertEquals("followers", uiState.metrics[1].id)
        assertEquals("84", uiState.metrics[1].value)
        assertEquals("Followers", uiState.metrics[1].label)

        assertEquals("following", uiState.metrics[2].id)
        assertEquals("31", uiState.metrics[2].value)
        assertEquals("Following", uiState.metrics[2].label)

        // Tabs: exactly 3 (Activity, About, Reviews)
        assertEquals("Form A must have exactly 3 tabs", 3, uiState.tabs.size)
        assertEquals("activity", uiState.tabs[0].id)
        assertEquals("Activity", uiState.tabs[0].label)
        assertEquals(DsR.drawable.ic_work, uiState.tabs[0].iconRes)

        assertEquals("about", uiState.tabs[1].id)
        assertEquals("About", uiState.tabs[1].label)
        assertEquals(DsR.drawable.ic_person, uiState.tabs[1].iconRes)

        assertEquals("reviews", uiState.tabs[2].id)
        assertEquals("Reviews", uiState.tabs[2].label)
        assertEquals(DsR.drawable.ic_star_outline, uiState.tabs[2].iconRes)
    }

    // =========================================================================
    // 2. SessionProfile -> Form B Projection Tests
    // =========================================================================

    @Test
    fun sessionProfile_formB_projection_hasFourMetricsFourTabsAndTradeHeadline() {
        val listing = ServiceListing(
            title = "Electrical Safety & Repair",
            category = "Certified Electrician",
            description = "Complete home wiring and appliance maintenance.",
            quoteLabel = "Free Estimate",
            coverage = "10 km",
            coverageHint = "Around Indiranagar",
            hours = "9 AM - 6 PM",
            hoursHint = "Mon - Sat",
            response = "< 1 hr",
            responseHint = "Quick reply",
            tags = listOf("Wiring", "Inverter", "MCB Repair"),
            experience = "8+ years",
            live = true,
        )
        val reviews = listOf(
            ProfileReview(id = "r1", name = "Priya", meta = "1d ago", rating = 5.0f, body = "Great work", createdAtMillis = 1000L),
            ProfileReview(id = "r2", name = "Amit", meta = "3d ago", rating = 4.8f, body = "Very clean", createdAtMillis = 2000L),
        )
        val jobs = listOf(
            sampleJob("j1", JobStatus.Completed),
            sampleJob("j2", JobStatus.Completed),
            sampleJob("j3", JobStatus.Applied),
        )
        val session = SessionProfile(
            displayName = "Priya Sharma",
            username = "priya_e",
            bio = "Master technician",
            city = "Chennai",
            joinedYear = "2022",
            hasListedService = true,
            listing = listing,
            reviews = reviews,
            followerCount = 120,
            followingCount = 45,
        )

        val uiState = session.toUiState(jobs = jobs)

        assertTrue("Form B must be a provider", uiState.isProvider)
        assertEquals("Certified Electrician", uiState.tradeHeadline)
        assertEquals("Chennai · Joined 2022", uiState.localityLine)

        // Metrics: exactly 4 (Rating, Completed Jobs, Followers, Following)
        assertEquals("Form B must have exactly 4 metrics", 4, uiState.metrics.size)
        assertEquals("rating", uiState.metrics[0].id)
        assertEquals("4.9 (2)", uiState.metrics[0].value)
        assertEquals("Rating", uiState.metrics[0].label)
        assertEquals(DsR.drawable.ic_star_filled, uiState.metrics[0].iconRes)

        assertEquals("completed_jobs", uiState.metrics[1].id)
        assertEquals("2", uiState.metrics[1].value)
        assertEquals("Completed", uiState.metrics[1].label)

        assertEquals("followers", uiState.metrics[2].id)
        assertEquals("120", uiState.metrics[2].value)
        assertEquals("Followers", uiState.metrics[2].label)

        assertEquals("following", uiState.metrics[3].id)
        assertEquals("45", uiState.metrics[3].value)
        assertEquals("Following", uiState.metrics[3].label)

        // Tabs: exactly 4 (Services, Showcase, Reviews, About)
        assertEquals("Form B must have exactly 4 tabs", 4, uiState.tabs.size)
        assertEquals("services", uiState.tabs[0].id)
        assertEquals("Services", uiState.tabs[0].label)
        assertEquals(DsR.drawable.ic_wrench, uiState.tabs[0].iconRes)

        assertEquals("gallery", uiState.tabs[1].id)
        assertEquals("Showcase", uiState.tabs[1].label)
        assertEquals(DsR.drawable.ic_photo, uiState.tabs[1].iconRes)

        assertEquals("reviews", uiState.tabs[2].id)
        assertEquals("Reviews", uiState.tabs[2].label)
        assertEquals(DsR.drawable.ic_star_outline, uiState.tabs[2].iconRes)

        assertEquals("about", uiState.tabs[3].id)
        assertEquals("About", uiState.tabs[3].label)
        assertEquals(DsR.drawable.ic_person, uiState.tabs[3].iconRes)
    }

    // =========================================================================
    // 3. Public Preview State Preservation
    // =========================================================================

    @Test
    fun sessionProfile_publicPreview_preservesPreviewFlag() {
        val session = SessionProfile(hasListedService = false)
        val stateDefault = session.toUiState(isPublicPreview = false)
        assertFalse("Default preview flag must be false", stateDefault.isPublicPreview)

        val statePreview = session.toUiState(isPublicPreview = true)
        assertTrue("isPublicPreview must be preserved as true", statePreview.isPublicPreview)
    }

    // =========================================================================
    // 4. UserProfileData (Visitor) -> Form A & Form B Mapping
    // =========================================================================

    @Test
    fun userProfileData_formA_mapsToCommunityMemberState() {
        val member = getUserProfileById("member-1")
        assertFalse("member-1 must be Form A", member.isProvider)

        val uiState = member.toUiState()

        assertFalse(uiState.isProvider)
        assertNull(uiState.tradeHeadline)
        assertEquals(3, uiState.metrics.size)
        assertEquals("activity", uiState.metrics[0].id)
        assertEquals("15", uiState.metrics[0].value)
        assertEquals("followers", uiState.metrics[1].id)
        assertEquals("84", uiState.metrics[1].value)
        assertEquals("following", uiState.metrics[2].id)
        assertEquals("31", uiState.metrics[2].value)

        assertEquals(3, uiState.tabs.size)
        assertEquals(listOf("activity", "about", "reviews"), uiState.tabs.map { it.id })
    }

    @Test
    fun userProfileData_formB_mapsToServiceProviderState() {
        val pro = getUserProfileById("pro-1")
        assertTrue("pro-1 must be Form B", pro.isProvider)

        val uiState = pro.toUiState()

        assertTrue(uiState.isProvider)
        assertEquals("Certified Electrician", uiState.tradeHeadline)
        assertEquals(4, uiState.metrics.size)
        assertEquals("rating", uiState.metrics[0].id)
        assertEquals("4.9 (2)", uiState.metrics[0].value)
        assertEquals("completed_jobs", uiState.metrics[1].id)
        assertEquals("48", uiState.metrics[1].value)
        assertEquals("followers", uiState.metrics[2].id)
        assertEquals("42", uiState.metrics[2].value)
        assertEquals("following", uiState.metrics[3].id)
        assertEquals("18", uiState.metrics[3].value)

        assertEquals(4, uiState.tabs.size)
        assertEquals(listOf("services", "gallery", "reviews", "about"), uiState.tabs.map { it.id })
        assertNotNull(uiState.listing)
    }

    // =========================================================================
    // 5. Tab Index Coercion Tests
    // =========================================================================

    @Test
    fun tabIndexCoercion_handlesNegativeAndOutOfBoundsValues() {
        val sessionA = SessionProfile(hasListedService = false)
        val stateA = sessionA.toUiState(selectedTabIndex = 99)
        assertEquals("Index 99 must be clamped to last index (2)", 2, stateA.selectedTabIndex)

        val stateANegative = sessionA.toUiState(selectedTabIndex = -5)
        assertEquals("Negative index must be clamped to 0", 0, stateANegative.selectedTabIndex)

        val sessionB = SessionProfile(hasListedService = true)
        val stateB = sessionB.toUiState(selectedTabIndex = 10)
        assertEquals("Index 10 must be clamped to last index (3)", 3, stateB.selectedTabIndex)
    }

    // =========================================================================
    // 6. Content Collections Preservation
    // =========================================================================

    @Test
    fun contentCollections_arePreservedAcrossMappings() {
        val gallery = listOf(ProfileGalleryItem("g1", "uri1", false))
        val lookingFor = listOf("Plumbing", "Carpentry")
        val skills = listOf("Electrical", "Diagnostics")
        val languages = listOf("Tamil" to "Native", "English" to "Fluent")
        val licenses = listOf("Govt Certified Wireman")

        val session = SessionProfile(
            gallery = gallery,
            lookingFor = lookingFor,
            skills = skills,
            languages = languages,
            licenses = licenses,
            hasListedService = true,
        )

        val uiState = session.toUiState()

        assertEquals(gallery, uiState.gallery)
        assertEquals(lookingFor, uiState.lookingFor)
        assertEquals(skills, uiState.skills)
        assertEquals(languages, uiState.languages)
        assertEquals(licenses, uiState.licenses)
    }
}
