package com.askit.designsystem.profile

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import com.askit.designsystem.R

data class ProfileMenuItem(val id: String, val label: String, val onClick: () -> Unit, val enabled: Boolean = true)

/** Profile title bar: optional back, a settings gear (owner only) and an overflow menu when it has items. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileTopBar(
    username: String,
    moreContentDescription: String,
    menuItems: List<ProfileMenuItem>,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    backContentDescription: String = "",
    onSettingsClick: (() -> Unit)? = null,
    settingsContentDescription: String = "",
) {
    var expanded by rememberSaveable(username) { mutableStateOf(false) }
    TopAppBar(
        title = { Text(username) },
        modifier = modifier,
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) { Icon(painterResource(R.drawable.ic_arrow_back), backContentDescription) }
            }
        },
        actions = {
            if (onSettingsClick != null) {
                IconButton(onClick = onSettingsClick, modifier = Modifier.testTag("profile_settings")) {
                    Icon(painterResource(R.drawable.ic_settings), settingsContentDescription)
                }
            }
            if (menuItems.isNotEmpty()) {
                Box {
                    IconButton(onClick = { expanded = true }, modifier = Modifier.testTag("profile_more")) {
                        Icon(painterResource(R.drawable.ic_more), moreContentDescription)
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        menuItems.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item.label) },
                                enabled = item.enabled,
                                onClick = {
                                    expanded = false
                                    item.onClick()
                                },
                                modifier = Modifier.testTag("profile_menu_${item.id}"),
                            )
                        }
                    }
                }
            }
        },
    )
}
