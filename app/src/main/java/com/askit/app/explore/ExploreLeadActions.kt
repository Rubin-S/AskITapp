package com.askit.app.explore

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.askit.app.R

data class ExploreLeadActions(
    val onApplyToTask: ((ExploreTaskResult) -> Unit)? = null,
    val onRequestService: ((ExplorePersonResult) -> Unit)? = null,
)

val LocalExploreLeadActions = compositionLocalOf { ExploreLeadActions() }

@Composable
fun ExploreApplyButton(task: ExploreTaskResult, modifier: Modifier = Modifier) {
    val onApply = LocalExploreLeadActions.current.onApplyToTask ?: return
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Button(
            onClick = { onApply(task) },
            modifier = Modifier
                .heightIn(min = 48.dp)
                .testTag("explore_apply_${task.id}"),
        ) {
            Text(stringResource(R.string.job_apply))
        }
    }
}

@Composable
fun ExploreRequestButton(person: ExplorePersonResult, modifier: Modifier = Modifier) {
    val onRequest = LocalExploreLeadActions.current.onRequestService ?: return
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Button(
            onClick = { onRequest(person) },
            modifier = Modifier
                .heightIn(min = 48.dp)
                .testTag("explore_request_${person.id}"),
        ) {
            Text(stringResource(R.string.job_request))
        }
    }
}
