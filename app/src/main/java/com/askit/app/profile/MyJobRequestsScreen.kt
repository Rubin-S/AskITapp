package com.askit.app.profile

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.askit.app.R
import com.askit.app.jobs.Job
import com.askit.app.jobs.isSeeker
import com.askit.app.jobs.ui.JobsHub
import com.askit.designsystem.R as DsR

/** Jobs where this account is the seeker: services it requested and tasks it posted that got applicants. */
internal fun myJobRequests(jobs: List<Job>): List<Job> = jobs.filter { it.isSeeker(it.localParty) }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MyJobRequestsScreen(
    jobs: List<Job>,
    onBack: () -> Unit,
    onOpenJob: (String) -> Unit,
    onCancelJob: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.testTag("my_job_requests_screen"),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_my_job_requests)) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("my_job_requests_back")) {
                        Icon(
                            painter = painterResource(DsR.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.explore_back),
                            modifier = Modifier.size(20.dp),
                        )
                    }
                },
            )
        },
    ) { padding ->
        JobsHub(
            jobs = myJobRequests(jobs),
            onOpenJob = onOpenJob,
            modifier = Modifier.padding(padding),
            onCancel = onCancelJob,
        )
    }
}
