package com.askit.app.profile

import com.askit.app.home.stories.StorySocialStore
import com.askit.app.jobs.*
import org.junit.Assert.*
import org.junit.Test

class ProfileMetricsTest {
    private fun review(id: String, direction: ReviewDirection, rating: Float, recipient: String = "owner") =
        ProfileReview(id, "Same display name", "", rating, "Review", 1,
            reviewerAccountId = "other", recipientAccountId = recipient, direction = direction)
    @Test fun receivedRatingsStayDirectionalAndUseIds() {
        val reviews = listOf(review("provider", ReviewDirection.SEEKER_TO_PROVIDER, 5f),
            review("seeker", ReviewDirection.PROVIDER_TO_SEEKER, 2f),
            review("wrong-account", ReviewDirection.SEEKER_TO_PROVIDER, 1f, "elsewhere"))
        assertEquals(5f, reviews.receivedBy("owner", ReviewDirection.SEEKER_TO_PROVIDER).single().rating)
        assertEquals(2f, reviews.receivedBy("owner", ReviewDirection.PROVIDER_TO_SEEKER).single().rating)
        assertTrue(reviews.receivedBy("", ReviewDirection.SEEKER_TO_PROVIDER).isEmpty())
    }
    @Test fun invalidAndDuplicateReviewsDoNotInflateRatings() {
        val review = review("one", ReviewDirection.SEEKER_TO_PROVIDER, 4f)
        assertEquals(1, listOf(review, review, review.copy(id = "nan", rating = Float.NaN),
            review.copy(id = "self", reviewerAccountId = "owner")).receivedBy("owner", ReviewDirection.SEEKER_TO_PROVIDER).size)
    }
    @Test fun completedCountsUseParticipantIdsAndRole() {
        val job = Job("job", "Work", "Owner", kind = JobKind.ServiceRequest, localParty = JobParty.Applicant,
            status = JobStatus.Completed, workMode = JobWorkMode.Remote, otp = "1234",
            providerAccountId = "owner", seekerAccountId = "other")
        assertEquals(1, listOf(job).profileCompletedCount("owner", true))
        assertEquals(0, listOf(job).profileCompletedCount("owner", false))
        assertEquals(0, listOf(job.copy(providerAccountId = null)).profileCompletedCount("owner", true))
    }
    @Test fun followerListsAndCountsFollowTheSameRelationshipsAcrossAccounts() {
        val social = StorySocialStore()
        social.activateAccount("owner")
        social.setFollowing("owner", "other", true)
        assertEquals(setOf("other"), social.followingIds("owner"))
        assertEquals(setOf("owner"), social.followerIds("other"))
        social.activateAccount("other")
        assertEquals(setOf("owner"), social.followerIds("other"))
        social.setFollowing("other", "owner", true)
        assertEquals(setOf("other"), social.followerIds("owner"))
        social.setFollowing("other", "owner", false)
        assertTrue(social.followerIds("owner").isEmpty())
    }
}
