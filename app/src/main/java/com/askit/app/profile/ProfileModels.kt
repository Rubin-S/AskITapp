package com.askit.app.profile

import androidx.annotation.DrawableRes
import com.askit.app.jobs.Job
import com.askit.app.jobs.JobStatus
import com.askit.app.jobs.isSeeker
import com.askit.app.jobs.visibleParty
import com.askit.app.session.ServiceListing
import com.askit.designsystem.R as DsR

enum class ProfilePane {
    Gallery,
    About,
    Activity,
    Reviews,
}

data class ProfileGalleryItem(
    val id: String,
    val uri: String,
    val isCarousel: Boolean = false,
)

data class SavedProfessional(
    val id: String,
    val name: String,
    val trade: String,
    val rating: String,
    val avatarUrl: String? = null,
)

data class ProfileReview(
    val id: String,
    val name: String,
    val meta: String,
    val rating: Float,
    val body: String,
    val createdAtMillis: Long,
    val jobId: String? = null,
    val avatarUrl: String? = null,
)

data class ProfileExperience(
    val id: String,
    val title: String,
    val subtitle: String,
    val detail: String,
    @DrawableRes val iconRes: Int,
)

fun experiencesFromListing(listing: ServiceListing): List<ProfileExperience> = listOf(
    ProfileExperience(
        id = "listing",
        title = listing.title,
        subtitle = listing.category,
        detail = listing.experience,
        iconRes = DsR.drawable.ic_work,
    ),
)

fun List<Job>.profileRequestCount(viewAsOtherParty: Boolean): Int = count { job ->
    !job.inHistory && job.isSeeker(job.visibleParty(viewAsOtherParty))
}

fun List<Job>.profileCompletedCount(): Int = count { it.status == JobStatus.Completed }
