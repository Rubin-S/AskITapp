package com.askit.app.jobs.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.askit.app.R
import com.askit.app.jobs.Job
import com.askit.app.jobs.JobParty
import com.askit.app.jobs.JobStatus
import com.askit.app.jobs.JobWorkMode
import com.askit.app.jobs.jobStatusLabelRes
import com.askit.app.jobs.visibleParty
import com.askit.designsystem.R as DsR
import com.askit.designsystem.empty.AskITEmptyState
import com.askit.designsystem.jobs.JobCardActions
import com.askit.designsystem.jobs.JobCardBadgeTone
import com.askit.designsystem.jobs.JobLeadCard

@Composable
fun JobsHub(
    jobs: List<Job>,
    viewAsOtherParty: Boolean,
    onOpenJob: (String) -> Unit,
    modifier: Modifier = Modifier,
    onAccept: (String) -> Unit = {},
    onDecline: (String) -> Unit = {},
    onCancel: (String) -> Unit = {},
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
) {
    val active = jobs.filter { !it.inHistory }
    val history = jobs.filter { it.inHistory }
    if (jobs.isEmpty()) {
        AskITEmptyState(
            iconRes = DsR.drawable.ic_work,
            title = stringResource(R.string.jobs_empty_title),
            supporting = stringResource(R.string.jobs_empty_supporting),
            modifier = modifier.testTag("jobs_empty"),
        )
        return
    }
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("jobs_list"),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(active, key = { it.id }) { job ->
            JobCardItem(
                job = job,
                viewAsOtherParty = viewAsOtherParty,
                onOpenJob = onOpenJob,
                onAccept = onAccept,
                onDecline = onDecline,
                onCancel = onCancel,
            )
        }
        if (history.isNotEmpty()) {
            item(key = "history_header") {
                Text(
                    text = stringResource(R.string.jobs_filter_history),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = if (active.isEmpty()) 0.dp else 8.dp)
                        .testTag("jobs_history_header"),
                )
            }
            items(history, key = { it.id }) { job ->
                JobCardItem(
                    job = job,
                    viewAsOtherParty = viewAsOtherParty,
                    onOpenJob = onOpenJob,
                    onAccept = onAccept,
                    onDecline = onDecline,
                    onCancel = onCancel,
                )
            }
        }
    }
}

@Composable
private fun JobCardItem(
    job: Job,
    viewAsOtherParty: Boolean,
    onOpenJob: (String) -> Unit,
    onAccept: (String) -> Unit,
    onDecline: (String) -> Unit,
    onCancel: (String) -> Unit,
) {
    val party = job.visibleParty(viewAsOtherParty)
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
        actions = jobCardActions(
            job = job,
            party = party,
            acceptLabel = stringResource(R.string.job_accept),
            declineLabel = stringResource(R.string.job_decline),
            cancelLabel = stringResource(R.string.job_cancel),
            trackLabel = stringResource(R.string.job_track),
            onOpenJob = onOpenJob,
            onAccept = onAccept,
            onDecline = onDecline,
            onCancel = onCancel,
        ),
        onClick = { onOpenJob(job.id) },
        modifier = Modifier.testTag("job_row_${job.id}"),
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

private fun jobCardActions(
    job: Job,
    party: JobParty,
    acceptLabel: String,
    declineLabel: String,
    cancelLabel: String,
    trackLabel: String,
    onOpenJob: (String) -> Unit,
    onAccept: (String) -> Unit,
    onDecline: (String) -> Unit,
    onCancel: (String) -> Unit,
): JobCardActions = when (job.status) {
    JobStatus.Applied -> when (party) {
        JobParty.Receiver -> JobCardActions.Incoming(
            acceptLabel = acceptLabel,
            declineLabel = declineLabel,
            onAccept = { onAccept(job.id) },
            onDecline = { onDecline(job.id) },
        )
        JobParty.Applicant -> JobCardActions.OutgoingPending(
            cancelLabel = cancelLabel,
            onCancel = { onCancel(job.id) },
        )
    }
    JobStatus.Accepted,
    JobStatus.OnTheWay,
    JobStatus.Started,
    JobStatus.AwaitingSeekerComplete,
    -> JobCardActions.Track(
        trackLabel = trackLabel,
        onTrack = { onOpenJob(job.id) },
    )
    JobStatus.Completed,
    JobStatus.Canceled,
    JobStatus.Rejected,
    -> JobCardActions.None
}
