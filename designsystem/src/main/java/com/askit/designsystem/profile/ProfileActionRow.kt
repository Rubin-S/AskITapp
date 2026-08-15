package com.askit.designsystem.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.askit.designsystem.jobs.AskITSecondaryButton

@Composable
fun ProfileActionRow(
    editLabel: String,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
    availabilityLabel: String? = null,
    onAvailability: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AskITSecondaryButton(
            onClick = onEdit,
            modifier = Modifier
                .weight(1f)
                .testTag("profile_edit"),
        ) {
            Text(editLabel)
        }
        if (availabilityLabel != null && onAvailability != null) {
            AskITSecondaryButton(
                onClick = onAvailability,
                modifier = Modifier
                    .weight(1f)
                    .testTag("profile_availability"),
            ) {
                Text(availabilityLabel)
            }
        }
    }
}
