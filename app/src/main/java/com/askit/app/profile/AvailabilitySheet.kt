package com.askit.app.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
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
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.askit.app.R
import com.askit.app.format.appLocale
import com.askit.app.format.formatCalendarDate
import com.askit.app.format.formatPickerDate
import com.askit.app.format.pickerDate
import com.askit.app.format.toPickerMillis
import com.askit.app.session.ProfileAvailability
import com.askit.designsystem.inputs.AskITTextField
import java.time.LocalDate

/**
 * Available or away. Away needs a From and To day (both inclusive); a future From only schedules the
 * pause, and the listing goes live again by itself the day after To (computed when read, see
 * [com.askit.app.session.statusOn]). Every field survives rotation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvailabilitySheet(availability: ProfileAvailability, onDismiss: () -> Unit, onSave: (ProfileAvailability) -> Unit) {
    var away by rememberSaveable { mutableStateOf(!availability.available) }
    var fromMillis by rememberSaveable { mutableStateOf(availability.fromMillis) }
    var toMillis by rememberSaveable { mutableStateOf(availability.toMillis) }
    var message by rememberSaveable { mutableStateOf(availability.message) }
    var mute by rememberSaveable { mutableStateOf(availability.muteNotifications) }
    var picking by rememberSaveable { mutableStateOf<Boolean?>(null) } // true = From, false = To
    val from = fromMillis
    val to = toMillis
    val rangeError = from != null && to != null && from > to
    val canSave = !away || (from != null && to != null && !rangeError)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp).testTag("profile_availability_sheet"),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(stringResource(R.string.profile_availability_title), style = MaterialTheme.typography.headlineSmall)
            Text(
                stringResource(R.string.profile_availability_helper),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                listOf(false to R.string.settings_availability_available, true to R.string.settings_availability_away).forEachIndexed {
                        index,
                        (value, label),
                    ->
                    SegmentedButton(
                        selected = away == value,
                        onClick = { away = value },
                        shape = SegmentedButtonDefaults.itemShape(index, 2),
                        modifier = Modifier.testTag(if (value) "profile_avail_away" else "profile_avail_available"),
                    ) { Text(stringResource(label)) }
                }
            }
            if (away) {
                AwayFields(
                    from = from,
                    to = to,
                    rangeError = rangeError,
                    message = message,
                    mute = mute,
                    onPick = { picking = it },
                    onMessageChange = { if (it.length <= AVAILABILITY_MESSAGE_MAX) message = it },
                    onMuteChange = { mute = it },
                )
            }
            Button(
                onClick = {
                    onSave(
                        if (away) {
                            ProfileAvailability(
                                available = false,
                                fromMillis = from,
                                toMillis = to,
                                message = message.trim(),
                                muteNotifications = mute,
                            )
                        } else {
                            ProfileAvailability()
                        },
                    )
                },
                enabled = canSave,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("profile_avail_save"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onSurface,
                    contentColor = MaterialTheme.colorScheme.surface,
                ),
            ) { Text(stringResource(R.string.profile_save)) }
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                Text(stringResource(R.string.profile_cancel))
            }
        }
    }
    picking?.let { pickingFrom ->
        AvailabilityDatePicker(
            initial = if (pickingFrom) from else to,
            onPick = { selected ->
                if (pickingFrom) fromMillis = selected else toMillis = selected
                picking = null
            },
            onDismiss = { picking = null },
        )
    }
}

@Composable
private fun AwayFields(
    from: Long?,
    to: Long?,
    rangeError: Boolean,
    message: String,
    mute: Boolean,
    onPick: (Boolean) -> Unit,
    onMessageChange: (String) -> Unit,
    onMuteChange: (Boolean) -> Unit,
) {
    val locale = appLocale()
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            stringResource(R.string.profile_availability_warning),
            modifier = Modifier.fillMaxWidth().background(
                MaterialTheme.colorScheme.errorContainer,
                RoundedCornerShape(12.dp),
            ).padding(12.dp),
            color = MaterialTheme.colorScheme.onErrorContainer,
            style = MaterialTheme.typography.bodySmall,
        )
        Text(
            stringResource(R.string.profile_availability_when).uppercase(locale),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            DateField(from, R.string.profile_availability_from, "profile_avail_from", { onPick(true) }, Modifier.weight(1f))
            DateField(to, R.string.profile_availability_to, "profile_avail_to", { onPick(false) }, Modifier.weight(1f))
        }
        if (rangeError) {
            Text(
                stringResource(R.string.profile_availability_range_error),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.testTag("profile_avail_range_error"),
            )
        }
        AskITTextField(
            value = message,
            onValueChange = onMessageChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.profile_availability_message)) },
            placeholder = { Text(stringResource(R.string.profile_availability_message_hint)) },
            supportingText = { Text("${message.length}/$AVAILABILITY_MESSAGE_MAX") },
            minLines = 3,
        )
        Row(
            Modifier.fillMaxWidth().toggleable(
                value = mute,
                role = Role.Checkbox,
                onValueChange = onMuteChange,
            ).testTag("profile_avail_mute"),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(checked = mute, onCheckedChange = null)
            Text(stringResource(R.string.profile_availability_mute), Modifier.padding(start = 8.dp))
        }
        if (to != null) {
            Text(
                stringResource(R.string.profile_availability_auto, formatCalendarDate(pickerDate(to).plusDays(1), locale)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** The whole field opens the picker; the text field itself is read-only. */
@Composable
private fun DateField(millis: Long?, label: Int, tag: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val text = stringResource(label)
    Box(modifier.testTag(tag)) {
        AskITTextField(
            value = millis?.let { formatPickerDate(it, appLocale()) }.orEmpty(),
            onValueChange = {},
            readOnly = true,
            label = { Text(text) },
            modifier = Modifier.fillMaxWidth(),
        )
        Box(
            Modifier.matchParentSize().clickable(
                role = Role.Button,
                onClickLabel = stringResource(R.string.profile_pick_date),
                onClick = onClick,
            )
                .testTag("${tag}_pick"),
        )
    }
}

/** Past days can't be picked: a pause starts today at the earliest. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AvailabilityDatePicker(initial: Long?, onPick: (Long) -> Unit, onDismiss: () -> Unit) {
    val today = LocalDate.now().toPickerMillis()
    val state = rememberDatePickerState(
        initialSelectedDateMillis = initial?.takeIf { it >= today },
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis >= today
            override fun isSelectableYear(year: Int) = year >= LocalDate.now().year
        },
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(enabled = state.selectedDateMillis != null, onClick = { state.selectedDateMillis?.let(onPick) }) {
                Text(stringResource(android.R.string.ok))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.profile_cancel)) } },
    ) { DatePicker(state = state) }
}

private const val AVAILABILITY_MESSAGE_MAX = 140
