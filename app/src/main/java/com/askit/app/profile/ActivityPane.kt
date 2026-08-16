package com.askit.app.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.askit.app.R
import com.askit.app.jobs.Job
import com.askit.app.jobs.JobParty
import com.askit.app.jobs.JobStatus
import com.askit.app.jobs.JobWorkMode
import com.askit.app.jobs.isProvider
import com.askit.app.jobs.isSeeker
import com.askit.app.jobs.jobStatusLabelRes
import com.askit.designsystem.jobs.JobCardActions
import com.askit.designsystem.jobs.JobCardBadgeTone
import com.askit.designsystem.jobs.JobLeadCard
import com.askit.designsystem.people.AskITAvatar
import com.askit.designsystem.profile.ProfileSectionCard

@Composable
fun ActivityPane(
    jobs: List<Job>,
    saved: List<SavedProfessional>,
    hasListedService: Boolean,
    onOpenJob: (String) -> Unit,
    onViewAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val requests = jobs.filter { job ->
        !job.inHistory && job.isSeeker(job.localParty)
    }
    val activeJobs = jobs.filter { job ->
        !job.inHistory && job.isProvider(job.localParty)
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("profile_activity_pane"),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ProfileSectionCard(
            title = stringResource(R.string.profile_active_requests),
            actionLabel = stringResource(R.string.profile_view_all),
            onAction = onViewAll,
        ) {
            Text(
                text = stringResource(R.string.profile_active_requests_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (requests.isEmpty()) {
                Text(
                    text = stringResource(R.string.profile_activity_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.testTag("profile_requests_empty"),
                )
            } else {
                requests.forEach { CompactJobRow(it, onOpenJob) }
            }
        }
        if (saved.isNotEmpty()) {
            ProfileSectionCard(
                title = stringResource(R.string.profile_saved_professionals),
            ) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(saved, key = { it.id }) { pro ->
                        SavedProCard(pro)
                    }
                }
            }
        }
        if (hasListedService) {
            ProfileSectionCard(
                title = stringResource(R.string.profile_active_jobs),
                actionLabel = stringResource(R.string.profile_dashboard),
                onAction = onViewAll,
            ) {
                if (activeJobs.isEmpty()) {
                    Text(
                        text = stringResource(R.string.profile_activity_empty),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.testTag("profile_jobs_empty"),
                    )
                } else {
                    activeJobs.forEach { CompactJobRow(it, onOpenJob) }
                }
            }
        }
    }
}

@Composable
private fun CompactJobRow(job: Job, onOpenJob: (String) -> Unit) {
    val party = job.localParty
    val location = job.locationLabel.ifBlank {
        if (job.workMode == JobWorkMode.Remote) {
            stringResource(R.string.job_location_remote)
        } else {
            ""
        }
    }
    JobLeadCard(
        name = job.counterpartName,
        locationLabel = location,
        jobTitle = job.title,
        badgeLabel = stringResource(jobCardBadgeRes(job, party)),
        badgeTone = jobCardBadgeTone(job.status, party),
        actions = JobCardActions.None,
        onClick = { onOpenJob(job.id) },
        modifier = Modifier.testTag("profile_job_${job.id}"),
    )
}

private fun jobCardBadgeRes(job: Job, party: JobParty): Int = when (job.status) {
    JobStatus.Applied -> if (party == JobParty.Receiver) {
        R.string.job_badge_new_request
    } else {
        R.string.job_status_applied
    }
    JobStatus.Accepted,
    JobStatus.OnTheWay,
    JobStatus.Started,
    JobStatus.AwaitingSeekerComplete,
    -> R.string.job_badge_in_progress
    JobStatus.Completed,
    JobStatus.Canceled,
    JobStatus.Rejected,
    -> jobStatusLabelRes(job, party)
}

private fun jobCardBadgeTone(status: JobStatus, party: JobParty): JobCardBadgeTone = when (status) {
    JobStatus.Applied -> if (party == JobParty.Receiver) {
        JobCardBadgeTone.New
    } else {
        JobCardBadgeTone.Neutral
    }
    JobStatus.Accepted,
    JobStatus.OnTheWay,
    JobStatus.Started,
    JobStatus.AwaitingSeekerComplete,
    -> JobCardBadgeTone.Live
    JobStatus.Completed,
    JobStatus.Canceled,
    JobStatus.Rejected,
    -> JobCardBadgeTone.Neutral
}

@Composable
private fun SavedProCard(pro: SavedProfessional) {
    Surface(
        modifier = Modifier.width(120.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            AskITAvatar(avatarUrl = pro.avatarUrl, avatarSize = 48.dp, fallbackIconSize = 28.dp)
            Text(pro.name, fontWeight = FontWeight.Bold, maxLines = 1, color = MaterialTheme.colorScheme.onSurface)
            Text(pro.trade, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("★ ${pro.rating}", color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.labelMedium)
        }
    }
}
