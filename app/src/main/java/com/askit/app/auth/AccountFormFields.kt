package com.askit.app.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.askit.app.R
import com.askit.designsystem.inputs.AskITTextField

/** Controlled Form A fields shared by registration and identity editing. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun AccountFormFields(draft: SignupDraft, onChange: (SignupDraft) -> Unit, enabled: Boolean = true, tag: String = "account") {
    val validation = draft.validation(photoReadable = true)
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        AccountField(
            draft.fullName,
            { onChange(draft.copy(fullName = it)) },
            R.string.form_a_full_name,
            validation.nameValid,
            "${tag}_name",
            enabled,
        )
        AccountField(
            draft.username,
            { onChange(draft.copy(username = it)) },
            R.string.profile_username,
            validation.usernameValid,
            "${tag}_username",
            enabled,
        )
        AccountField(
            draft.phoneNumber,
            { onChange(draft.copy(phoneNumber = it)) },
            R.string.settings_phone_number,
            validation.phoneValid,
            "${tag}_phone",
            enabled,
            keyboard = KeyboardType.Phone,
        )
        AccountField(
            draft.email.orEmpty(),
            { onChange(draft.copy(email = it)) },
            R.string.account_email,
            validation.emailValid,
            "${tag}_email",
            enabled,
            keyboard = KeyboardType.Email,
        )
        AccountField(
            draft.pincode,
            { onChange(draft.copy(pincode = it)) },
            R.string.account_postcode,
            validation.postcodeValid,
            "${tag}_pincode",
            enabled,
            keyboard = KeyboardType.Number,
        )
        AccountField(
            draft.city,
            { onChange(draft.copy(city = it)) },
            R.string.profile_city,
            true,
            "${tag}_city",
            enabled,
            required = false,
        )
        Text(stringResource(R.string.form_a_app_language) + " *", style = MaterialTheme.typography.labelLarge)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            com.askit.app.session.AppLanguage.entries.forEach { language ->
                FilterChip(
                    selected = draft.preferredLanguage == language.storedValue,
                    enabled = enabled,
                    onClick = { onChange(draft.copy(preferredLanguage = language.storedValue)) },
                    label = { Text(stringResource(language.endonymRes)) },
                    modifier = Modifier.testTag("${tag}_language_${language.storedValue.lowercase()}"),
                )
            }
        }
    }
}

@Composable
private fun AccountField(
    value: String,
    onChange: (String) -> Unit,
    label: Int,
    valid: Boolean,
    tag: String,
    enabled: Boolean,
    required: Boolean = true,
    keyboard: KeyboardType = KeyboardType.Text,
) {
    var touched by rememberSaveable { mutableStateOf(false) }
    var focused by remember { mutableStateOf(false) }
    AskITTextField(
        value = value, onValueChange = onChange, enabled = enabled, singleLine = true,
        label = { Text(stringResource(label) + if (required) " *" else "") },
        isError = touched && !valid,
        supportingText = if (touched && !valid) {
            { Text(stringResource(R.string.account_invalid_field)) }
        } else {
            null
        },
        keyboardOptions = KeyboardOptions(keyboardType = keyboard),
        modifier = Modifier.fillMaxWidth().testTag(tag).onFocusChanged {
            if (!it.isFocused && focused) touched = true
            focused = it.isFocused
        },
    )
}
