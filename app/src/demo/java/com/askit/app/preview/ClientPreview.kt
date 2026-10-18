package com.askit.app.preview

import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.res.stringResource
import com.askit.app.R
import com.askit.app.BuildConfig
import com.askit.app.createpost.PostDraft
import com.askit.app.home.posts.PostStore
import com.askit.app.jobs.*
import com.askit.app.listservice.*
import com.askit.app.posttask.*
import com.askit.app.session.SessionProfile
import com.askit.app.task.*

internal fun previewProfile(id: String): SessionProfile {
    val draft = ListServiceDraft(
        categoryId = "electrician", title = "Electrical repairs and maintenance",
        description = "Home wiring, lighting installation and electrical troubleshooting.",
        deliveryModes = setOf(ListServiceDeliveryMode.REMOTE),
        pricing = ListServicePricing.Hourly("350"), portfolioUris = emptyList(),
        contactPhone = "+919876543298", contactEmail = "meera@example.com",
        details = ListServiceDetails("Weekdays, 9 AM - 6 PM", "5", "1-2 hours", "Standard tools", "Electrical technician", "30 days", "One day"),
    )
    return SessionProfile(
        accountId = id, pincode = "641001", email = "meera@example.com", phoneNumber = "+919876543298",
        displayName = if (id != "preview-owner") "Kavin Kumar" else "Meera Raman",
        username = if (id != "preview-owner") "kavin.kumar" else "meera.raman",
        hasListedService = id != "preview-seeker", activeRole = if (id == "preview-seeker") "Seeker" else "Both",
        listing = draft.toServiceListing("Electrician").takeUnless { id == "preview-seeker" }, listingDraft = draft.takeUnless { id == "preview-seeker" },
        skills = listOf("Wiring", "Lighting", "Troubleshooting"),
        licenses = listOf("Demo electrical technician certificate"),
    )
}

internal fun seedPreviewContent(tasks: TaskRepository, posts: PostStore, jobs: JobsStore) {
    check(BuildConfig.CLIENT_PREVIEW)
    val profile = previewProfile("preview-owner")
    tasks.createTask(PostTaskDraft(
        categoryId = "electrician", title = "Demo: plan the lighting for my home office",
        details = "Suggest lighting positions and fittings for a home office. This editable sample lets you explore the owner actions and task discussion.",
        workMode = PostTaskWorkMode.REMOTE, location = null,
        timingMode = PostTaskTimingMode.FLEXIBLE, selectedDateMillis = null,
        budget = PostTaskBudget.Fixed("1500"), photos = emptyList(), itemDetails = null, accessInstructions = null,
    ), TaskCommentAuthor("preview-owner", profile.displayName, profile.avatarUrl))
    posts.activateAccount("preview-owner")
    posts.publish(PostDraft(body = "Demo project update: explore this post's Edit, Save, Archive, Pin and commenting controls."), "preview-owner", profile)
    jobs.addClientPreviewJobs(com.askit.app.demo.clientPreviewJobs())
}

@Composable
internal fun PreviewProfileControls(onAccount: (String) -> Unit, onView: (String) -> Unit) {
    if (!BuildConfig.CLIENT_PREVIEW) return
    Column {
        Text(stringResource(R.string.profile_preview_controls))
        listOf("preview-owner" to R.string.profile_preview_provider, "preview-seeker" to R.string.profile_preview_seeker).forEach { (id, label) ->
            TextButton(onClick = { onAccount(id) }) { Text(stringResource(label)) }
        }
        listOf("preview-owner" to R.string.profile_preview_view_provider, "preview-seeker" to R.string.profile_preview_view_seeker).forEach { (id, label) ->
            TextButton(onClick = { onView(id) }) { Text(stringResource(label)) }
        }
    }
}
