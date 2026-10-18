package com.askit.designsystem.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties

/** Shared proportions; Material retains accessible typography and wrapping action buttons. */
@Composable
fun AskITAlertDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    dismissButton: (@Composable () -> Unit)? = null,
    title: (@Composable () -> Unit)? = null,
    text: (@Composable () -> Unit)? = null,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = confirmButton,
        dismissButton = dismissButton,
        title = title,
        text = text?.let { content ->
            {
                // Keep actions available when translations, text scaling or the IME reduce space.
                Column(Modifier.verticalScroll(rememberScrollState())) { content() }
            }
        },
        modifier = modifier
            .safeDrawingPadding()
            .padding(24.dp)
            .widthIn(max = 560.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        properties = DialogProperties(usePlatformDefaultWidth = false),
    )
}
