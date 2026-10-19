package com.askit.app.posttask

import androidx.annotation.StringRes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.askit.app.R
import com.askit.designsystem.components.AskITDisclosureRow

@Composable
internal fun PostTaskSectionHeading(@StringRes titleRes: Int) {
    Text(
        text = stringResource(titleRes),
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.semantics { heading() },
    )
}

@Composable
internal fun OptionalSectionRow(@StringRes titleRes: Int, expanded: Boolean, onClick: () -> Unit, testTag: String) {
    AskITDisclosureRow(
        title = stringResource(titleRes),
        expanded = expanded,
        actionLabel = stringResource(if (expanded) R.string.post_task_collapse else R.string.post_task_expand),
        onClick = onClick,
        modifier = Modifier.testTag(testTag),
    )
}
