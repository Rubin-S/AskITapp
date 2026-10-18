package com.askit.app.profile

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.selection.toggleable
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.askit.app.BuildConfig
import com.askit.app.R
import com.askit.app.auth.AccountFormFields
import com.askit.app.session.SessionProfile
import com.askit.designsystem.inputs.AskITTextField
import com.askit.designsystem.people.AskITAvatar
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    profile: SessionProfile,
    onBack: () -> Unit,
    onSave: (EditProfileFormState) -> Boolean,
    modifier: Modifier = Modifier,
) {
    val original = EditProfileFormState(profile.displayName, profile.username.removePrefix("@"), profile.city, profile.bio,
        profile.avatarUrl, title = profile.title, email = profile.email.orEmpty(), pincode = profile.pincode,
        phoneNumber = profile.phoneNumber, language = profile.selectedLanguage, discloseApproximateLocation = profile.discloseApproximateLocation)
    var form by rememberSaveable(profile.accountId, stateSaver = EditProfileFormSaver) { mutableStateOf(original) }
    var showDiscard by rememberSaveable { mutableStateOf(false) }
    var showPhoto by rememberSaveable { mutableStateOf(false) }
    var saveFailed by rememberSaveable { mutableStateOf(false) }
    val photoActions = key(profile.accountId) { rememberProfilePhotoActions { form = form.copy(avatarUrl = it) } }
    val dirty = form.isDirty(original)
    val phoneNeedsVerification = !BuildConfig.CLIENT_PREVIEW && form.phoneNumber != profile.phoneNumber
    fun back() { if (dirty) showDiscard = true else onBack() }
    fun save() { saveFailed = !onSave(form) }
    BackHandler(onBack = ::back)
    Scaffold(
        modifier = modifier.fillMaxSize().testTag("edit_profile_screen"),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.profile_edit_title)) },
                navigationIcon = { TextButton(onClick = ::back) { Text(stringResource(R.string.explore_back)) } })
        },
        bottomBar = {
            Surface {
                Button(onClick = ::save, enabled = dirty && form.isValid && !phoneNeedsVerification,
                    modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp).testTag("edit_profile_save")) {
                    Text(stringResource(R.string.profile_save))
                }
            }
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(Modifier.fillMaxWidth().clickable { showPhoto = true }.padding(vertical = 8.dp)) {
                AskITAvatar(form.avatarUrl, 56.dp, 32.dp)
                Text(stringResource(R.string.profile_photo_tap), Modifier.padding(16.dp))
            }
            AccountFormFields(form.accountDraft(), onChange = {
                form = form.copy(displayName = it.fullName, username = it.username, city = it.city,
                    phoneNumber = it.phoneNumber, email = it.email.orEmpty(), pincode = it.pincode,
                    language = it.preferredLanguage)
                saveFailed = false
            }, tag = "edit_profile")
            Row(Modifier.fillMaxWidth().toggleable(value = form.discloseApproximateLocation, role = Role.Checkbox,
                onValueChange = { form = form.copy(discloseApproximateLocation = it) }),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Checkbox(checked = form.discloseApproximateLocation, onCheckedChange = null)
                Text(stringResource(R.string.profile_distance_consent))
            }
            Text(stringResource(R.string.profile_distance_consent_hint), style = MaterialTheme.typography.bodySmall)
            if (phoneNeedsVerification) Text(stringResource(R.string.profile_phone_verification_needed))
            if (profile.listing == null) {
                AskITTextField(value = form.title, onValueChange = { if (it.length <= 100) form = form.copy(title = it) },
                    label = { Text(stringResource(R.string.profile_optional_title)) }, modifier = Modifier.fillMaxWidth())
                AskITTextField(value = form.bio, onValueChange = { if (it.length <= BioMaxLength) form = form.copy(bio = it) },
                    label = { Text(stringResource(R.string.profile_bio)) }, minLines = 3, modifier = Modifier.fillMaxWidth())
            }
            if (saveFailed) Text(stringResource(R.string.profile_save_unavailable), color = MaterialTheme.colorScheme.error)
        }
    }
    if (showDiscard) DiscardChangesDialog(onDismiss = { showDiscard = false }, onDiscard = onBack)
    if (showPhoto) PhotoSourceSheet(onDismiss = { showPhoto = false },
        onCamera = { showPhoto = false; photoActions.launchCamera() },
        onLibrary = { showPhoto = false; photoActions.launchLibrary() },
        onRemove = { showPhoto = false; form = form.copy(avatarUrl = null) })
}

private val EditProfileFormSaver = Saver<EditProfileFormState, String>(
    save = { Json.encodeToString(it) }, restore = { Json.decodeFromString<EditProfileFormState>(it) },
)
