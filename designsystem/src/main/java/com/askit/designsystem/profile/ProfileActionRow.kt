package com.askit.designsystem.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

data class ProfileAction(val id: String, val label: String, val onClick: () -> Unit, val enabled: Boolean = true)
data class ProfileActionConfig(val actions: List<ProfileAction>)

/** Wrapping labels and minimum touch targets also work on narrow screens and with large text. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileActionRow(config: ProfileActionConfig, modifier: Modifier = Modifier) {
    FlowRow(modifier.fillMaxWidth().testTag("profile_action_row"),
        horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        config.actions.forEach { action ->
            OutlinedButton(onClick = action.onClick, enabled = action.enabled,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                modifier = Modifier.widthIn(min = 112.dp).weight(1f).heightIn(min = 48.dp).testTag("profile_action_${action.id}")) {
                Text(action.label)
            }
        }
    }
}
