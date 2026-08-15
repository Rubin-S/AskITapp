package com.askit.designsystem.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.askit.designsystem.R
import com.askit.designsystem.people.AskITAvatar

@Composable
fun ProfileCover(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .testTag("profile_cover"),
    )
}

@Composable
fun ProfileAvatar(
    avatarUrl: String?,
    cameraContentDescription: String,
    onCamera: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.size(96.dp)) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .border(4.dp, MaterialTheme.colorScheme.surface, CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center,
        ) {
            AskITAvatar(
                avatarUrl = avatarUrl,
                avatarSize = 88.dp,
                fallbackIconSize = 48.dp,
            )
        }
        if (onCamera != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = (-2).dp, y = (-2).dp)
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurface)
                    .clickable(role = Role.Button, onClick = onCamera)
                    .testTag("profile_camera"),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_camera),
                    contentDescription = cameraContentDescription,
                    tint = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .size(16.dp)
                        .padding(1.dp),
                )
            }
        }
    }
}
