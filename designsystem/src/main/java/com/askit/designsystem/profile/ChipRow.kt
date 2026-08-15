package com.askit.designsystem.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChipRow(
    chips: List<String>,
    modifier: Modifier = Modifier,
    addLabel: String? = null,
    onAdd: (() -> Unit)? = null,
) {
    FlowRow(
        modifier = modifier.testTag("profile_chips"),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        chips.forEach { chip ->
            FilterChip(
                selected = false,
                onClick = {},
                label = { Text(chip) },
                enabled = false,
            )
        }
        if (addLabel != null && onAdd != null) {
            FilterChip(
                selected = false,
                onClick = onAdd,
                label = { Text(addLabel) },
                modifier = Modifier.testTag("profile_chip_add"),
            )
        }
    }
}
