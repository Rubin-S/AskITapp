package com.askit.designsystem.dialogs

import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.askit.designsystem.R
import com.askit.designsystem.dialogs.AskITAlertDialog

@Composable
fun AskITDiscardDialog(onDismissRequest: () -> Unit, onDiscard: () -> Unit) {
    AskITAlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(R.string.discard_dialog_title)) },
        text = { Text(stringResource(R.string.discard_dialog_message)) },
        dismissButton = {
            TextButton(
                onClick = onDiscard,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error,
                ),
            ) {
                Text(stringResource(R.string.discard_dialog_discard))
            }
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.discard_dialog_keep_editing))
            }
        },
    )
}
