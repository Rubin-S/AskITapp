package com.askit.app.jobs

import androidx.annotation.StringRes
import com.askit.app.R
import com.askit.designsystem.jobs.JobBannerTone
import com.askit.designsystem.jobs.JobStepState

@StringRes
fun jobStatusLabelRes(status: JobStatus, party: JobParty): Int = when (status) {
    JobStatus.Applied -> if (party == JobParty.Receiver) {
        R.string.job_status_incoming
    } else {
        R.string.job_status_applied
    }
    JobStatus.Accepted -> R.string.job_status_accepted
    JobStatus.OnTheWay -> R.string.job_status_on_the_way
    JobStatus.Started -> R.string.job_status_started
    JobStatus.AwaitingSeekerComplete -> R.string.job_status_awaiting_provider
    JobStatus.Completed -> R.string.job_status_completed
    JobStatus.Canceled -> if (party == JobParty.Applicant) {
        R.string.job_status_you_canceled
    } else {
        R.string.job_status_they_canceled
    }
    JobStatus.Rejected -> if (party == JobParty.Receiver) {
        R.string.job_status_you_rejected
    } else {
        R.string.job_status_canceled
    }
}

@StringRes
fun jobStatusLabelRes(job: Job, party: JobParty): Int = when (job.status) {
    JobStatus.AwaitingSeekerComplete -> if (job.isSeeker(party)) {
        R.string.job_status_awaiting_seeker
    } else {
        R.string.job_status_awaiting_provider
    }
    JobStatus.Applied,
    JobStatus.Accepted,
    JobStatus.OnTheWay,
    JobStatus.Started,
    JobStatus.Completed,
    JobStatus.Canceled,
    JobStatus.Rejected,
    -> jobStatusLabelRes(job.status, party)
}

@StringRes
fun jobBannerSupportingRes(job: Job, party: JobParty): Int = when (job.status) {
    JobStatus.Applied -> if (party == JobParty.Receiver) {
        R.string.job_banner_applied_receiver
    } else {
        R.string.job_banner_applied_applicant
    }
    JobStatus.Accepted -> if (job.isSeeker(party)) {
        R.string.job_banner_accepted_seeker
    } else {
        R.string.job_banner_accepted_provider
    }
    JobStatus.OnTheWay -> R.string.job_banner_on_the_way
    JobStatus.Started -> R.string.job_banner_started
    JobStatus.AwaitingSeekerComplete -> if (job.isSeeker(party)) {
        R.string.job_banner_awaiting_seeker
    } else {
        R.string.job_banner_awaiting_provider
    }
    JobStatus.Completed -> R.string.job_banner_completed
    JobStatus.Canceled -> R.string.job_banner_canceled
    JobStatus.Rejected -> R.string.job_banner_rejected
}

fun jobBannerTone(status: JobStatus): JobBannerTone = when (status) {
    JobStatus.Applied,
    JobStatus.OnTheWay,
    JobStatus.AwaitingSeekerComplete,
    -> JobBannerTone.Neutral
    JobStatus.Accepted,
    JobStatus.Started,
    JobStatus.Completed,
    -> JobBannerTone.Progress
    JobStatus.Canceled,
    JobStatus.Rejected,
    -> JobBannerTone.Warning
}

@StringRes
fun jobBannerTitleRes(job: Job, party: JobParty): Int = when {
    job.status == JobStatus.Applied && party == JobParty.Receiver ->
        R.string.job_badge_new_request
    else -> jobStatusLabelRes(job, party)
}

@StringRes
fun jobStepLabelRes(step: JobStep): Int = when (step) {
    JobStep.Applied -> R.string.job_step_applied
    JobStep.Accepted -> R.string.job_step_accepted
    JobStep.OnTheWay -> R.string.job_step_on_the_way
    JobStep.Started -> R.string.job_step_started
    JobStep.Complete -> R.string.job_step_complete
}

fun jobStepState(
    step: JobStep,
    steps: List<JobStep>,
    current: JobStep,
    completed: Boolean,
): JobStepState {
    if (completed) return JobStepState.Done
    val stepIndex = steps.indexOf(step)
    val currentIndex = steps.indexOf(current)
    return when {
        stepIndex < currentIndex -> JobStepState.Done
        stepIndex == currentIndex -> JobStepState.Current
        else -> JobStepState.Upcoming
    }
}

fun formatElapsed(millis: Long): String {
    val totalSeconds = millis / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return "%d:%02d:%02d".format(hours, minutes, seconds)
}
