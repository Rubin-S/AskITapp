package com.askit.designsystem.profile

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import com.askit.designsystem.R

data class ProfileMenuItem(val id: String, val label: String, val onClick: () -> Unit, val enabled: Boolean = true)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileTopBar(username: String, moreContentDescription: String, menuItems: List<ProfileMenuItem>,
    onBack: (() -> Unit)? = null, backContentDescription: String = "", onSettingsClick: (() -> Unit)? = null) {
    var expanded by remember(username) { mutableStateOf(false) }
    TopAppBar(title = { Text(username) }, navigationIcon = {
        if (onBack != null) IconButton(onClick = onBack) {
            Icon(painterResource(R.drawable.ic_arrow_back), backContentDescription)
        }
    }, actions = {
        Box {
            IconButton(onClick = { if (onSettingsClick != null) onSettingsClick() else expanded = true }, modifier = Modifier.testTag("profile_more")) {
                Icon(painterResource(R.drawable.ic_more), moreContentDescription)
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                menuItems.forEach { item ->
                    DropdownMenuItem(text = { Text(item.label) }, enabled = item.enabled,
                        onClick = { expanded = false; item.onClick() }, modifier = Modifier.testTag("profile_menu_${item.id}"))
                }
            }
        }
    })
}
