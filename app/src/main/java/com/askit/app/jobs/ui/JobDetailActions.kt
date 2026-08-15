package com.askit.app.jobs.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Button
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
import com.askit.app.jobs.JobsStore
import com.askit.app.jobs.isProvider
import com.askit.app.jobs.isSeeker
import com.askit.designsystem.jobs.AskITSecondaryButton

internal fun Job.hasDetailActions(party: JobParty): Boolean = when (status) {
    JobStatus.Applied -> true
    JobStatus.Accepted, JobStatus.OnTheWay -> true
    JobStatus.Started -> isProvider(party)
    JobStatus.AwaitingSeekerComplete -> isSeeker(party)
    JobStatus.Completed,
    JobStatus.Canceled,
    JobStatus.Rejected,
    -> false
}

@Composable
internal fun JobDetailActions(
    job: Job,
    party: JobParty,
    store: JobsStore,
    onShareCode: () -> Unit,
    onEnterCode: () -> Unit,
    onReview: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        when (job.status) {
            JobStatus.Applied -> AppliedActions(job, party, store)
            JobStatus.Accepted, JobStatus.OnTheWay -> VerifyActions(
                job = job,
                party = party,
                onShareCode = onShareCode,
                onEnterCode = onEnterCode,
            )
            JobStatus.Started -> if (job.isProvider(party)) {
                Button(
                    onClick = { store.requestComplete(job.id) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .testTag("job_request_complete"),
                ) { Text(stringResource(R.string.job_request_complete)) }
            }
            JobStatus.AwaitingSeekerComplete -> if (job.isSeeker(party)) {
                Button(
                    onClick = onReview,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .testTag("job_mark_complete"),
                ) { Text(stringResource(R.string.job_mark_complete)) }
            }
            JobStatus.Completed,
            JobStatus.Canceled,
            JobStatus.Rejected,
            -> Unit
        }
    }
}

@Composable
private fun AppliedActions(job: Job, party: JobParty, store: JobsStore) {
    when (party) {
        JobParty.Applicant -> AskITSecondaryButton(
            onClick = { store.cancel(job.id) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("job_cancel"),
        ) { Text(stringResource(R.string.job_cancel)) }
        JobParty.Receiver -> Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Button(
                onClick = { store.accept(job.id) },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
                    .testTag("job_accept"),
            ) { Text(stringResource(R.string.job_accept)) }
            AskITSecondaryButton(
                onClick = { store.reject(job.id) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("job_reject"),
            ) { Text(stringResource(R.string.job_reject)) }
        }
    }
}

@Composable
private fun VerifyActions(
    job: Job,
    party: JobParty,
    onShareCode: () -> Unit,
    onEnterCode: () -> Unit,
) {
    val canShare = job.isSeeker(party) && when (job.status) {
        JobStatus.Accepted, JobStatus.OnTheWay, JobStatus.Started -> true
        JobStatus.Applied,
        JobStatus.AwaitingSeekerComplete,
        JobStatus.Completed,
        JobStatus.Canceled,
        JobStatus.Rejected,
        -> false
    }
    val canEnter = job.isProvider(party) && when (job.status) {
        JobStatus.Accepted, JobStatus.OnTheWay -> true
        JobStatus.Applied,
        JobStatus.Started,
        JobStatus.AwaitingSeekerComplete,
        JobStatus.Completed,
        JobStatus.Canceled,
        JobStatus.Rejected,
        -> false
    }
    if (canShare) {
        Button(
            onClick = onShareCode,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .testTag("job_show_code"),
        ) { Text(stringResource(R.string.job_show_code)) }
    }
    if (canEnter) {
        Button(
            onClick = onEnterCode,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .testTag("job_enter_code"),
        ) { Text(stringResource(R.string.job_enter_code)) }
    }
}
