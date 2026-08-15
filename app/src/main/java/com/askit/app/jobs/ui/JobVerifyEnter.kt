package com.askit.app.jobs.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.askit.app.R
import com.askit.app.jobs.JobsStore
import com.askit.designsystem.R as DsR
import com.askit.designsystem.jobs.OtpBoxes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobVerifyEnter(
    jobId: String,
    store: JobsStore,
    onBack: () -> Unit,
    onVerified: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var code by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf(false) }
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.job_verify_enter_title)) },
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
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.job_verify_enter_helper),
                style = MaterialTheme.typography.bodyLarge,
            )
            OtpBoxes(
                value = code,
                onValueChange = {
                    code = it
                    error = false
                },
            )
            if (error) {
                Text(
                    text = stringResource(R.string.job_verify_error),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.testTag("job_otp_error"),
                )
            }
            Button(
                onClick = {
                    if (store.verifyOtp(jobId, code)) {
                        onVerified()
                    } else {
                        error = true
                    }
                },
                enabled = code.length == 4,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .testTag("job_verify_start"),
            ) { Text(stringResource(R.string.job_verify_start)) }
            OutlinedButton(
                onClick = {
                    if (store.stubScan(jobId)) onVerified() else error = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .testTag("job_verify_scan"),
            ) { Text(stringResource(R.string.job_verify_scan)) }
        }
    }
}
