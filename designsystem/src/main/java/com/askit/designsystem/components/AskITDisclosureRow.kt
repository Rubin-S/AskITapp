package com.askit.designsystem.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.collapse
import androidx.compose.ui.semantics.expand
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp

/**
 * A row that shows or hides an optional form section. [actionLabel] is the visible verb for the
 * current state ("Show"/"Hide", "Expand"/"Collapse") and is also the state screen readers hear;
 * the row exposes expand/collapse actions instead of pretending to be a selectable item.
 * [supportingLabel] is an optional quiet tag such as "Optional".
 */
@Composable
fun AskITDisclosureRow(
    title: String,
    expanded: Boolean,
    actionLabel: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    supportingLabel: String? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .semantics {
                stateDescription = actionLabel
                if (expanded) {
                    collapse { onClick(); true }
                } else {
                    expand { onClick(); true }
                }
            }
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f),
        )
        if (supportingLabel != null) {
            Text(
                text = supportingLabel,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.size(8.dp))
        }
        Text(
            text = actionLabel,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.secondary,
        )
    }
}
