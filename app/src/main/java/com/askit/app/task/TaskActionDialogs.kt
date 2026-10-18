package com.askit.app.task

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import com.askit.designsystem.dialogs.AskITAlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.askit.app.R

@Composable
fun TaskActionDialogs(
    draft: TaskActionDraft,
    policy: TaskActionPolicy,
    taskTitle: String,
    onCancelReport: () -> Unit,
    onSelectReason: (TaskReportReason) -> Unit,
    onReport: () -> Unit,
    onCancelDelete: () -> Unit,
    onDelete: () -> Unit,
) {
    if (draft.reportOpen && policy.canReport) {
        AskITAlertDialog(
            onDismissRequest = onCancelReport,
            title = { Text(stringResource(R.string.task_report)) },
            text = {
                Column {
                    Text(taskTitle, style = MaterialTheme.typography.titleSmall)
                    Text(stringResource(R.string.task_report_local_notice), modifier = Modifier.padding(vertical = 12.dp))
                    Column(Modifier.selectableGroup()) {
                        TaskReportReason.entries.forEach { reason ->
                            Row(
                                Modifier.fillMaxWidth().selectable(draft.reason == reason, role = Role.RadioButton,
                                    onClick = { onSelectReason(reason) }).padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                RadioButton(selected = draft.reason == reason, onClick = null)
                                Text(stringResource(reason.labelRes), modifier = Modifier.padding(start = 8.dp))
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = onReport, enabled = draft.reason != null) { Text(stringResource(R.string.task_report_confirm)) } },
            dismissButton = { TextButton(onClick = onCancelReport) { Text(stringResource(R.string.task_action_cancel)) } },
        )
    }
    if (draft.deleteOpen && policy.isOwner) {
        AskITAlertDialog(
            onDismissRequest = onCancelDelete,
            title = { Text(stringResource(R.string.task_delete_title)) },
            text = { Text(stringResource(R.string.task_delete_message, taskTitle)) },
            confirmButton = {
                TextButton(onClick = onDelete) { Text(stringResource(R.string.task_delete), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = onCancelDelete) { Text(stringResource(R.string.task_action_cancel)) } },
        )
    }
}
