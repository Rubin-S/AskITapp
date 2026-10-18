package com.askit.app.profile

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.askit.app.R
import com.askit.designsystem.dialogs.AskITAlertDialog

/** Log out and clear-data buttons at the bottom of settings. */
@Composable
internal fun SettingsAccountActions(onLogoutClick: () -> Unit, onClearDataClick: () -> Unit) {
    Column {
        // Bottom Buttons
        // Log Out Button
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp,
            shadowElevation = 1.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clickable(onClick = onLogoutClick)
                .testTag("settings_btn_logout"),
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.settings_logout),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Clear App Data Button
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.errorContainer,
            tonalElevation = 0.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clickable(onClick = onClearDataClick)
                .testTag("settings_btn_clear_data"),
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.settings_clear_app_data),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                    ),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
        }
    }
}

/** A bottom sheet with a title and an empty-state message (blocked accounts, history). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsEmptyStateSheet(@StringRes titleRes: Int, @StringRes messageRes: Int, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(titleRes),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(messageRes),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(24.dp))
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
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.settings_close))
            }
        },
    )
}

/** Confirmation for a destructive settings action (log out, clear app data). */
@Composable
internal fun SettingsConfirmDialog(
    @StringRes titleRes: Int,
    @StringRes messageRes: Int,
    @StringRes confirmRes: Int,
    confirmTestTag: String?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AskITAlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(titleRes)) },
        text = { Text(stringResource(messageRes)) },
        confirmButton = {
            TextButton(
                onClick = {
                    onDismiss()
                    onConfirm()
                },
                modifier = if (confirmTestTag != null) Modifier.testTag(confirmTestTag) else Modifier,
            ) {
                Text(stringResource(confirmRes), color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.profile_cancel))
            }
        },
    )
}
