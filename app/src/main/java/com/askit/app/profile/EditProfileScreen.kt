package com.askit.app.profile

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.askit.app.BuildConfig
import com.askit.app.R
import com.askit.app.auth.AccountFormFields
import com.askit.app.auth.AccountFormRules
import com.askit.app.session.SessionProfile
import com.askit.designsystem.dialogs.AskITAlertDialog
import com.askit.designsystem.inputs.AskITTextField
import com.askit.designsystem.people.AskITAvatar
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import com.askit.designsystem.R as DsR

/**
 * Edit profile. Only reachable where the build can save (see [ProfileCapabilities.editProfile]).
 * The form starts from a snapshot taken when the screen opens, so a change made elsewhere meanwhile
 * neither marks it dirty nor gets silently mixed into it.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    profile: SessionProfile,
    appLanguage: String,
    onBack: () -> Unit,
    onSave: suspend (EditProfileFormState) -> Boolean,
    modifier: Modifier = Modifier,
) {
    val original by rememberSaveable(profile.accountId, stateSaver = FormSaver) { mutableStateOf(profile.toForm(appLanguage)) }
    var form by rememberSaveable(profile.accountId, stateSaver = FormSaver) { mutableStateOf(original) }
    var showDiscard by rememberSaveable { mutableStateOf(false) }
    var showPhoto by rememberSaveable { mutableStateOf(false) }
    var saveFailed by rememberSaveable { mutableStateOf(false) }
    var saving by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val dirty = form != original
    val phoneNeedsVerification = !BuildConfig.CLIENT_PREVIEW && form.phoneNumber != original.phoneNumber
    fun back() {
        if (dirty) showDiscard = true else onBack()
    }
    BackHandler(onBack = ::back)
    Scaffold(
        modifier = modifier.fillMaxSize().testTag("edit_profile_screen"),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.profile_edit_title)) },
                navigationIcon = {
                    IconButton(onClick = ::back) {
                        Icon(painterResource(DsR.drawable.ic_arrow_back), stringResource(R.string.explore_back))
                    }
                },
            )
        },
        bottomBar = {
            Surface {
                Button(
                    onClick = {
                        scope.launch {
                            saving = true
                            saveFailed = !onSave(form)
                            saving = false
                        }
                    },
                    enabled = dirty && form.isValid && !phoneNeedsVerification && !saving,
                    modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp).testTag("edit_profile_save"),
                ) {
                    Text(stringResource(R.string.profile_save))
                }
            }
        },
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                Modifier.fillMaxWidth().clickable(role = Role.Button) { showPhoto = true }.padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AskITAvatar(form.avatarUrl, 56.dp, 32.dp)
                Text(stringResource(R.string.profile_photo_tap), Modifier.padding(16.dp))
            }
            AccountFormFields(form.accountDraft(), onChange = {
                form = form.copy(
                    displayName = it.fullName,
                    username = it.username,
                    city = it.city,
                    phoneNumber = it.phoneNumber,
                    email = it.email.orEmpty(),
                    pincode = it.pincode,
                    language = it.preferredLanguage,
                )
                saveFailed = false
            }, tag = "edit_profile", rules = AccountFormRules.Edit)
            if (phoneNeedsVerification) Text(stringResource(R.string.profile_phone_verification_needed))
            if (profile.listing == null) {
                AskITTextField(
                    value = form.title,
                    onValueChange = { if (it.length <= TITLE_MAX_LENGTH) form = form.copy(title = it) },
                    label = { Text(stringResource(R.string.profile_optional_title)) },
                    modifier = Modifier.fillMaxWidth(),
                )
                AskITTextField(
                    value = form.bio,
                    onValueChange = { if (it.length <= BIO_MAX_LENGTH) form = form.copy(bio = it) },
                    label = { Text(stringResource(R.string.profile_bio)) },
                    supportingText = { Text("${form.bio.length}/$BIO_MAX_LENGTH") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                // Providers' headline and bio come from the service listing (one record, profile contract).
                Text(stringResource(R.string.profile_listing_identity_hint), style = MaterialTheme.typography.bodySmall)
            }
            if (saveFailed) Text(stringResource(R.string.profile_action_error), color = MaterialTheme.colorScheme.error)
        }
    }
    if (showDiscard) DiscardChangesDialog(onDismiss = { showDiscard = false }, onDiscard = onBack)
    ProfilePhotoSource(
        visible = showPhoto,
        hasPhoto = form.avatarUrl != null,
        onDismiss = { showPhoto = false },
        onPick = { form = form.copy(avatarUrl = it) },
        onRemove = { form = form.copy(avatarUrl = null) },
    )
}

@Composable
private fun DiscardChangesDialog(onDismiss: () -> Unit, onDiscard: () -> Unit) {
    AskITAlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.profile_discard_title)) },
        text = { Text(stringResource(R.string.profile_discard_message)) },
        dismissButton = {
            TextButton(onClick = onDiscard, modifier = Modifier.testTag("profile_discard_confirm")) {
                Text(stringResource(R.string.profile_discard), color = MaterialTheme.colorScheme.error)
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.profile_keep_editing)) } },
    )
}

/** The language field shows the app language: the device preference is the one source of truth. */
private fun SessionProfile.toForm(appLanguage: String) = EditProfileFormState(
    displayName, username.removePrefix("@"), city, bio, avatarUrl, title = title, email = email.orEmpty(), pincode = pincode,
    phoneNumber = phoneNumber, language = appLanguage, discloseApproximateLocation = discloseApproximateLocation,
)

private val FormSaver = Saver<EditProfileFormState, String>(
    save = { Json.encodeToString(EditProfileFormState.serializer(), it) },
    restore = { Json.decodeFromString(EditProfileFormState.serializer(), it) },
)
