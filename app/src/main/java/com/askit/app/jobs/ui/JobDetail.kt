package com.askit.app.jobs.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.askit.app.R
import com.askit.app.jobs.Job
import com.askit.app.jobs.JobStatus
import com.askit.app.jobs.JobWorkMode
import com.askit.app.jobs.JobsStore
import com.askit.app.jobs.currentStep
import com.askit.app.jobs.formatElapsed
import com.askit.app.jobs.jobBannerSupportingRes
import com.askit.app.jobs.jobBannerTitleRes
import com.askit.app.jobs.jobBannerTone
import com.askit.app.jobs.jobStepLabelRes
import com.askit.app.jobs.jobStepState
import com.askit.app.jobs.stepperSteps
import com.askit.app.jobs.visibleParty
import com.askit.designsystem.R as DsR
import com.askit.designsystem.jobs.JobStatusBanner
import com.askit.designsystem.jobs.JobStepper
import com.askit.designsystem.jobs.JobStepperItem
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobDetail(
    job: Job,
    store: JobsStore,
    viewAsOtherParty: Boolean,
    clock: () -> Long,
    onBack: () -> Unit,
    onShareCode: () -> Unit,
    onEnterCode: () -> Unit,
    onReview: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val party = job.visibleParty(viewAsOtherParty)
    var now by rememberSaveable { mutableLongStateOf(clock()) }
    LaunchedEffect(job.startedAtMillis) {
        if (job.startedAtMillis == null) return@LaunchedEffect
        while (true) {
            now = clock()
            delay(1_000)
        }
    }
    val steps = job.stepperSteps()
    val stepperItems = steps.map { step ->
        JobStepperItem(
            label = stringResource(jobStepLabelRes(step)),
            state = jobStepState(
                step = step,
                steps = steps,
                current = job.currentStep(),
                completed = job.status == JobStatus.Completed,
            ),
        )
    }
    val currentIndex = steps.indexOf(job.currentStep()).coerceAtLeast(0) + 1
    val hasActions = job.hasDetailActions(party)
    val location = job.locationLabel.ifBlank {
        if (job.workMode == JobWorkMode.Remote) {
            stringResource(R.string.job_location_remote)
        } else {
            ""
        }
    }
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surface,
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = { Text(job.title, style = MaterialTheme.typography.titleMedium) },
                windowInsets = WindowInsets(0),
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                        Icon(
                            painter = painterResource(DsR.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.messages_back),
                        )
                    }
                },
            )
        },
        bottomBar = {
            if (hasActions) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    tonalElevation = 0.dp,
                ) {
                    JobDetailActions(
                        job = job,
                        party = party,
                        store = store,
                        onShareCode = onShareCode,
                        onEnterCode = onEnterCode,
                        onReview = onReview,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            JobDetailHeader(
                counterpartName = job.counterpartName,
                locationLabel = location,
                onViewAsOther = { store.toggleViewAsOtherParty(job.id) },
            )
            JobStatusBanner(
                title = stringResource(jobBannerTitleRes(job, party)),
                supporting = stringResource(jobBannerSupportingRes(job, party)),
                tone = jobBannerTone(job.status),
            )
            JobStepper(
                steps = stepperItems,
                stepOfDescription = stringResource(R.string.job_step_of, currentIndex, steps.size),
            )
            val startedAt = job.startedAtMillis
            if (startedAt != null) {
                Text(
                    text = stringResource(R.string.job_elapsed, formatElapsed(now - startedAt)),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.testTag("job_elapsed"),
                )
            }
        }
    }
}

@Composable
private fun JobDetailHeader(
    counterpartName: String,
    locationLabel: String,
    onViewAsOther: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = counterpartName,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (locationLabel.isNotBlank()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(
                    painter = painterResource(DsR.drawable.ic_location),
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = locationLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp),
                )
            }
        }
        AssistChip(
            onClick = onViewAsOther,
            label = { Text(stringResource(R.string.messages_view_as_other)) },
            modifier = Modifier.testTag("job_view_as_other"),
        )
    }
}
