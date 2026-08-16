package com.askit.app.jobs.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.askit.app.R
import com.askit.app.jobs.JobsStore
import com.askit.designsystem.R as DsR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobReview(
    jobId: String,
    store: JobsStore,
    onFinished: () -> Unit,
    onReviewSubmitted: (rating: Int, comment: String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier,
) {
    var wouldAgain by rememberSaveable { mutableStateOf<Boolean?>(null) }
    var rating by rememberSaveable { mutableIntStateOf(0) }
    var comment by rememberSaveable { mutableStateOf("") }
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.job_review_title)) },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Text(
                text = stringResource(R.string.job_review_helper),
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = stringResource(R.string.job_review_would_again),
                style = MaterialTheme.typography.titleMedium,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = wouldAgain == true,
                    onClick = { wouldAgain = true },
                    label = { Text(stringResource(R.string.job_review_yes)) },
                    modifier = Modifier.testTag("job_review_yes"),
                )
                FilterChip(
                    selected = wouldAgain == false,
                    onClick = { wouldAgain = false },
                    label = { Text(stringResource(R.string.job_review_no)) },
                    modifier = Modifier.testTag("job_review_no"),
                )
            }
            Text(
                text = stringResource(R.string.job_review_rating),
                style = MaterialTheme.typography.titleMedium,
            )
            Row {
                (1..5).forEach { star ->
                    IconButton(
                        onClick = { rating = star },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("job_review_star_$star"),
                    ) {
                        Icon(
                            painter = painterResource(
                                if (star <= rating) {
                                    DsR.drawable.ic_star_filled
                                } else {
                                    DsR.drawable.ic_star_outline
                                },
                            ),
                            contentDescription = stringResource(R.string.job_review_star, star),
                        )
                    }
                }
            }
            OutlinedTextField(
                value = comment,
                onValueChange = { comment = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.job_review_comment)) },
                minLines = 3,
                shape = MaterialTheme.shapes.small,
            )
            Button(
                onClick = {
                    store.complete(jobId)
                    onReviewSubmitted(rating, comment)
                    onFinished()
                },
                enabled = rating > 0,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .testTag("job_review_submit"),
            ) { Text(stringResource(R.string.job_review_submit)) }
            OutlinedButton(
                onClick = {
                    store.completeWithReviewDeferred(jobId)
                    onFinished()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .testTag("job_review_later"),
            ) { Text(stringResource(R.string.job_review_later)) }
        }
    }
}
