package com.askit.app.profile

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.askit.app.R
import com.askit.designsystem.dialogs.AskITAlertDialog
import com.askit.designsystem.people.AskITAvatar
import com.askit.designsystem.R as DsR

@Composable
internal fun SettingsProfileHeaderCard(displayName: String, username: String, avatarUrl: String?, onClick: (() -> Unit)?) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
            .testTag("settings_profile_header_card"),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            AskITAvatar(avatarUrl = avatarUrl, avatarSize = 56.dp, fallbackIconSize = 48.dp)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (username.isNotBlank()) {
                    Text(
                        text = "@${username.removePrefix("@")}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            if (onClick != null) Chevron(20)
        }
    }
}

/** A section title and its card of rows, divided by inset lines. */
@Composable
internal fun SettingsSection(title: String, rows: List<SettingsRow>) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 12.dp, bottom = 8.dp),
        )
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            rows.forEachIndexed { index, row ->
                if (index > 0) {
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 66.dp),
                        thickness = 0.75.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    )
                }
                SettingsRowItem(row)
            }
        }
    }
}

/**
 * The whole row is the touch target and one TalkBack item: a switch row is `toggleable` (the
 * Switch itself takes no clicks), a navigation row is a button with its value merged in.
 */
@Composable
private fun SettingsRowItem(row: SettingsRow) {
    val action = row.onClick
    val interaction = when {
        action == null -> Modifier.semantics(mergeDescendants = true) {}
        row.checked != null -> Modifier.toggleable(value = row.checked, role = Role.Switch, onValueChange = { action() })
        else -> Modifier.clickable(role = Role.Button, onClick = action)
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).then(interaction).testTag(row.tag)
            .padding(horizontal = 16.dp, vertical = if (row.checked != null) 10.dp else 14.dp),
    ) {
        Box(Modifier.size(36.dp).background(row.accent, RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
            Icon(painterResource(row.icon), contentDescription = null, tint = row.tint, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(14.dp))
        Text(
            text = stringResource(row.title),
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        when {
            row.checked != null -> Switch(checked = row.checked, onCheckedChange = null)

            else -> {
                if (!row.value.isNullOrBlank()) {
                    Text(
                        text = row.value,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(end = 6.dp),
                    )
                }
                if (action != null) Chevron(18)
            }
        }
    }
}

@Composable
private fun Chevron(size: Int) {
    Icon(
        painter = painterResource(DsR.drawable.ic_chevron_right),
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.size(size.dp),
    )
}

/** Single-choice sheet (language, who can message): one selectable group of radio rows. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsChoiceSheet(
    title: String,
    options: List<Pair<String, String>>,
    selected: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
    hint: String? = null,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 40.dp).selectableGroup()) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 16.dp),
            )
            if (hint != null) Text(hint, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            options.forEach { (value, label) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                        .selectable(selected = value == selected, role = Role.RadioButton, onClick = { onSelect(value) })
                        .padding(vertical = 12.dp),
                ) {
                    RadioButton(selected = value == selected, onClick = null)
                    Spacer(Modifier.width(12.dp))
                    Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}

/** A titled list sheet (blocked accounts) with an empty-state message. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsListSheet(title: String, items: List<String>, emptyMessage: String, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Spacer(Modifier.height(16.dp))
            if (items.isEmpty()) {
                Text(
                    emptyMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
            items.forEach {
                Text(it, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp))
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

/** Log out and clear-data buttons at the bottom of settings. */
@Composable
internal fun SettingsAccountActions(onLogoutClick: () -> Unit, onClearDataClick: () -> Unit) {
    Column {
        SettingsActionButton(
            R.string.settings_logout,
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.onSurface,
            "settings_btn_logout",
            onLogoutClick,
        )
        Spacer(Modifier.height(12.dp))
        SettingsActionButton(
            R.string.settings_clear_app_data,
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            "settings_btn_clear_data",
            onClearDataClick,
        )
    }
}

@Composable
private fun SettingsActionButton(@StringRes label: Int, container: Color, content: Color, tag: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = container,
        shadowElevation = if (container == MaterialTheme.colorScheme.surface) 1.dp else 0.dp,
        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp).testTag(tag),
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 14.dp)) {
            Text(
                stringResource(label),
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp),
                color = content,
            )
        }
    }
}

@Composable
internal fun SettingsTermsDialog(onOpenTerms: () -> Unit, onOpenPrivacyPolicy: () -> Unit, onDismiss: () -> Unit) {
    AskITAlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_terms_privacy)) },
        text = {
            Column {
                TextButton(onClick = onOpenTerms, modifier = Modifier.testTag("settings_open_terms")) {
                    Text(stringResource(R.string.settings_terms_link))
                }
                TextButton(onClick = onOpenPrivacyPolicy, modifier = Modifier.testTag("settings_open_privacy")) {
                    Text(stringResource(R.string.settings_privacy_policy))
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.settings_close)) } },
    )
}

/** Confirmation for a destructive settings action (log out, clear app data). */
@Composable
internal fun SettingsConfirmDialog(
    @StringRes titleRes: Int,
    @StringRes messageRes: Int,
    @StringRes confirmRes: Int,
    confirmTestTag: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AskITAlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(titleRes)) },
        text = { Text(stringResource(messageRes)) },
        confirmButton = {
            TextButton(onClick = {
                onDismiss()
                onConfirm()
            }, modifier = Modifier.testTag(confirmTestTag)) {
                Text(stringResource(confirmRes), color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.profile_cancel)) } },
    )
}
