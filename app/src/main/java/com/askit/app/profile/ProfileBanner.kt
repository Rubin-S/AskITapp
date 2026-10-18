package com.askit.app.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.askit.app.R
import com.askit.app.auth.isSignupPhotoReadable
import com.askit.designsystem.profile.ProfileCover
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.askit.designsystem.R as DsR

@Composable
internal fun ProfileBanner(uri: String?, canEdit: Boolean, onSave: (String?) -> Boolean) {
    var menu by rememberSaveable { mutableStateOf(false) }
    var failed by rememberSaveable(uri) { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val photos = rememberProfilePhotoActions { selected ->
        scope.launch {
            saving = true
            failed = !withContext(Dispatchers.IO) { onSave(selected) }
            saving = false
        }
    }
    Column {
        Box(Modifier.fillMaxWidth().height(120.dp).testTag("profile_banner")) {
            ProfileCover()
            if (uri != null) {
                AsyncImage(
                    model = uri,
                    contentDescription = stringResource(R.string.profile_banner),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    onError = { failed = true },
                )
            }
            if (canEdit) {
                Box(Modifier.align(Alignment.TopEnd)) {
                    IconButton(onClick = {
                        if (uri ==
                            null
                        ) {
                            photos.launchLibrary()
                        } else {
                            menu = true
                        }
                    }, enabled = !saving, modifier = Modifier.testTag("profile_banner_edit")) {
                        Icon(painterResource(DsR.drawable.ic_edit), stringResource(R.string.profile_edit_banner))
                    }
                    DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.profile_edit_banner)) },
                            onClick = {
                                menu = false
                                photos.launchLibrary()
                            },
                        )
                        if (uri != null) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.profile_remove_banner)) },
                                onClick = {
                                    menu = false
                                    failed = !onSave(null)
                                },
                            )
                        }
                    }
                }
            }
        }
        if (failed) {
            Text(
                stringResource(R.string.profile_banner_failed),
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 128.dp, end = 16.dp, top = 8.dp),
            )
        }
    }
}
