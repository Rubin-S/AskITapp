package com.askit.app.profile

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.askit.app.R

@Composable
fun DiscardChangesDialog(
    onDismiss: () -> Unit,
    onDiscard: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.profile_discard_title)) },
        text = { Text(stringResource(R.string.profile_discard_message)) },
        confirmButton = {
            TextButton(onClick = onDiscard, modifier = Modifier.testTag("profile_discard_confirm")) {
                Text(stringResource(R.string.profile_discard))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.profile_keep_editing))
            }
        },
    )
}
