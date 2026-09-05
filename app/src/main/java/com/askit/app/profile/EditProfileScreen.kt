package com.askit.app.profile

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.askit.app.R
import com.askit.app.session.SessionProfile
import com.askit.designsystem.people.AskITAvatar
import com.askit.designsystem.R as DsR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    profile: SessionProfile,
    onBack: () -> Unit,
    onSave: (EditProfileFormState) -> Unit,
    modifier: Modifier = Modifier,
) {
    val original = EditProfileFormState(
        displayName = profile.displayName,
        username = profile.username,
        city = profile.city,
        bio = profile.bio,
        avatarUrl = profile.avatarUrl,
    )
    var form by rememberSaveable(stateSaver = EditProfileFormSaver) { mutableStateOf(original) }
    var showDiscard by rememberSaveable { mutableStateOf(false) }
    var showPhoto by rememberSaveable { mutableStateOf(false) }
    val photoActions = rememberProfilePhotoActions { uri -> form = form.copy(avatarUrl = uri) }
    val dirty = form.isDirty(original)
    fun attemptBack() {
        if (dirty) showDiscard = true else onBack()
    }
    BackHandler(onBack = ::attemptBack)
    Scaffold(
        modifier = modifier.fillMaxSize().testTag("edit_profile_screen"),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.profile_edit_title)) },
                navigationIcon = {
                    IconButton(onClick = ::attemptBack, modifier = Modifier.size(48.dp).testTag("edit_profile_back")) {
                        Icon(
                            painter = painterResource(DsR.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.explore_back),
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = { onSave(form) },
                        enabled = dirty && form.isValid,
                        modifier = Modifier.testTag("edit_profile_save"),
                    ) {
                        Text(
                            text = stringResource(R.string.profile_save),
                            fontWeight = FontWeight.Bold,
                            color = if (dirty && form.isValid) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            },
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
            ) {
                Column(Modifier.navigationBarsPadding()) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Button(
                        onClick = { onSave(form) },
                        enabled = dirty && form.isValid,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .heightIn(min = 52.dp)
                            .testTag("edit_profile_bottom_save"),
                    ) {
                        Text(
                            text = stringResource(R.string.profile_save),
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.profile_edit_helper),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(role = Role.Button) { showPhoto = true }
                        .testTag("edit_profile_photo"),
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        AskITAvatar(form.avatarUrl, 56.dp, 32.dp)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.profile_photo), style = MaterialTheme.typography.titleSmall)
                            Text(
                                stringResource(R.string.profile_photo_tap),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Icon(
                            painter = painterResource(DsR.drawable.ic_chevron_right),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Text(
                    text = stringResource(R.string.profile_basic_info).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        OutlinedTextField(
                            value = form.displayName,
                            onValueChange = { form = form.copy(displayName = it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { if (!it.isFocused && form.displayName.isNotEmpty()) form = form.copy(displayNameTouched = true) }
                                .testTag("edit_profile_display_name"),
                            label = { Text(stringResource(R.string.profile_display_name) + " *") },
                            isError = form.displayNameError,
                            supportingText = if (form.displayNameError) {
                                { Text(stringResource(R.string.profile_display_name_error)) }
                            } else {
                                null
                            },
                        )
                        OutlinedTextField(
                            value = form.username,
                            onValueChange = { form = form.copy(username = it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { if (!it.isFocused) form = form.copy(usernameTouched = true) }
                                .testTag("edit_profile_username"),
                            label = { Text(stringResource(R.string.profile_username) + " *") },
                            isError = form.usernameError,
                            supportingText = {
                                Text(
                                    if (form.usernameError) {
                                        stringResource(R.string.profile_username_error)
                                    } else {
                                        stringResource(R.string.profile_username_helper)
                                    },
                                )
                            },
                        )
                        OutlinedTextField(
                            value = form.city,
                            onValueChange = { form = form.copy(city = it) },
                            modifier = Modifier.fillMaxWidth().testTag("edit_profile_city"),
                            label = { Text(stringResource(R.string.profile_city)) },
                        )
                        OutlinedTextField(
                            value = form.bio,
                            onValueChange = { if (it.length <= BioMaxLength) form = form.copy(bio = it) },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp).testTag("edit_profile_bio"),
                            label = { Text(stringResource(R.string.profile_bio)) },
                            minLines = 4,
                            supportingText = { Text(stringResource(R.string.profile_bio_count, form.bio.length)) },
                        )
                    }
                }
        }
    }
    if (showDiscard) {
        DiscardChangesDialog(
            onDismiss = { showDiscard = false },
            onDiscard = onBack,
        )
    }
    if (showPhoto) {
        PhotoSourceSheet(
            onDismiss = { showPhoto = false },
            onCamera = {
                photoActions.launchCamera()
                showPhoto = false
            },
            onLibrary = {
                photoActions.launchLibrary()
                showPhoto = false
            },
            onRemove = {
                form = form.copy(avatarUrl = null)
                showPhoto = false
            },
        )
    }
}

private val EditProfileFormSaver = listSaver<EditProfileFormState, Any?>(
    save = {
        listOf(
            it.displayName,
            it.username,
            it.city,
            it.bio,
            it.avatarUrl,
            it.usernameTouched,
            it.displayNameTouched,
        )
    },
    restore = {
        EditProfileFormState(
            displayName = it[0] as String,
            username = it[1] as String,
            city = it[2] as String,
            bio = it[3] as String,
            avatarUrl = it[4] as String?,
            usernameTouched = it[5] as Boolean,
            displayNameTouched = it[6] as Boolean,
        )
    },
)
