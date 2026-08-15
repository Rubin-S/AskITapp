package com.askit.app.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.askit.app.R
import com.askit.app.session.ProfileAvailability
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private enum class AvailabilityDateField { From, To }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvailabilitySheet(
    availability: ProfileAvailability,
    onDismiss: () -> Unit,
    onSave: (ProfileAvailability) -> Unit,
) {
    var fromMillis by remember { mutableStateOf(availability.fromMillis) }
    var toMillis by remember { mutableStateOf(availability.toMillis) }
    var message by remember { mutableStateOf(availability.message) }
    var mute by remember { mutableStateOf(availability.muteNotifications) }
    var picking by remember { mutableStateOf<AvailabilityDateField?>(null) }
    val rangeError = fromMillis != null && toMillis != null && fromMillis!! > toMillis!!
    val canSave = fromMillis != null && toMillis != null && !rangeError
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .testTag("profile_availability_sheet"),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(stringResource(R.string.profile_availability_title), style = MaterialTheme.typography.headlineSmall)
            Text(
                text = stringResource(R.string.profile_availability_helper),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(R.string.profile_availability_warning),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.errorContainer, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                text = stringResource(R.string.profile_availability_when).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                DateField(
                    value = formatProfileDate(fromMillis),
                    label = stringResource(R.string.profile_availability_from),
                    tag = "profile_avail_from",
                    onClick = { picking = AvailabilityDateField.From },
                    modifier = Modifier.weight(1f),
                )
                DateField(
                    value = formatProfileDate(toMillis),
                    label = stringResource(R.string.profile_availability_to),
                    tag = "profile_avail_to",
                    onClick = { picking = AvailabilityDateField.To },
                    modifier = Modifier.weight(1f),
                )
            }
            if (rangeError) {
                Text(
                    text = stringResource(R.string.profile_availability_range_error),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.testTag("profile_avail_range_error"),
                )
            }
            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.profile_availability_message)) },
                placeholder = { Text(stringResource(R.string.profile_availability_message_hint)) },
                minLines = 3,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = mute, onCheckedChange = { mute = it }, modifier = Modifier.testTag("profile_avail_mute"))
                Text(stringResource(R.string.profile_availability_mute))
            }
            Text(
                text = stringResource(R.string.profile_availability_auto, formatProfileDate(toMillis).ifBlank { "—" }),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(
                onClick = {
                    onSave(
                        ProfileAvailability(
                            available = false,
                            fromMillis = fromMillis,
                            toMillis = toMillis,
                            message = message,
                            muteNotifications = mute,
                        ),
                    )
                },
                enabled = canSave,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("profile_avail_save"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onSurface,
                    contentColor = MaterialTheme.colorScheme.surface,
                ),
            ) {
                Text(stringResource(R.string.profile_save))
            }
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                Text(stringResource(R.string.profile_cancel))
            }
        }
    }
    val field = picking
    if (field != null) {
        val initial = when (field) {
            AvailabilityDateField.From -> fromMillis
            AvailabilityDateField.To -> toMillis
        }
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initial)
        DatePickerDialog(
            onDismissRequest = { picking = null },
            confirmButton = {
                TextButton(
                    enabled = datePickerState.selectedDateMillis != null,
                    onClick = {
                        val selected = datePickerState.selectedDateMillis
                        when (field) {
                            AvailabilityDateField.From -> fromMillis = selected
                            AvailabilityDateField.To -> toMillis = selected
                        }
                        picking = null
                    },
                ) {
                    Text(stringResource(R.string.profile_save))
                }
            },
            dismissButton = {
                TextButton(onClick = { picking = null }) {
                    Text(stringResource(R.string.profile_cancel))
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun DateField(
    value: String,
    label: String,
    tag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        modifier = modifier.testTag(tag),
        label = { Text(label) },
        trailingIcon = {
            TextButton(onClick = onClick, modifier = Modifier.heightIn(min = 48.dp).testTag("${tag}_pick")) {
                Text(stringResource(R.string.profile_pick_date))
            }
        },
    )
}

internal fun formatProfileDate(millis: Long?): String {
    if (millis == null) return ""
    return DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
        .withZone(ZoneId.systemDefault())
        .format(Instant.ofEpochMilli(millis))
}
