package com.askit.app.profile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.askit.app.R
import com.askit.designsystem.profile.ProfileCover
import kotlinx.coroutines.launch
import com.askit.designsystem.R as DsR

/** Profile banner. [onSave] (null = this build can't change it) runs off the main thread in the view model. */
@Composable
internal fun ProfileBanner(uri: String?, onSave: (suspend (String?) -> Boolean)?) {
    var menu by rememberSaveable { mutableStateOf(false) }
    var error by rememberSaveable(uri) { mutableStateOf<Int?>(null) }
    var saving by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    fun save(value: String?) {
        val action = onSave ?: return
        scope.launch {
            saving = true
            error = if (action(value)) null else R.string.profile_action_error
            saving = false
        }
    }
    val pickPhoto = rememberPhotoLibrary(onPick = ::save)
    Column {
        Box(Modifier.fillMaxWidth().height(120.dp).testTag("profile_banner")) {
            ProfileCover()
            if (uri != null) {
                AsyncImage(
                    model = uri,
                    contentDescription = stringResource(R.string.profile_banner),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    onError = { error = R.string.profile_banner_failed },
                )
            }
            if (onSave != null) {
                Box(Modifier.align(Alignment.TopEnd)) {
                    IconButton(
                        onClick = { if (uri == null) pickPhoto() else menu = true },
                        enabled = !saving,
                        modifier = Modifier.testTag("profile_banner_edit"),
                    ) {
                        Icon(painterResource(DsR.drawable.ic_edit), stringResource(R.string.profile_edit_banner))
                    }
                    DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                        DropdownMenuItem(text = { Text(stringResource(R.string.profile_edit_banner)) }, onClick = {
                            menu = false
                            pickPhoto()
                        })
                        DropdownMenuItem(text = { Text(stringResource(R.string.profile_remove_banner)) }, onClick = {
                            menu = false
                            save(null)
                        })
                    }
                }
            }
        }
        error?.let {
            Text(
                stringResource(it),
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 128.dp, end = 16.dp, top = 8.dp),
            )
        }
    }
}
