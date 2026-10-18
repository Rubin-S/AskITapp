package com.askit.designsystem.posts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.askit.designsystem.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostShareSheet(
    onDismiss: () -> Unit,
    onCopyLink: () -> Unit,
    onShareExternal: () -> Unit,
    onWhatsApp: () -> Unit,
    onWhatsAppStatus: () -> Unit,
    onAddToStory: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurface,
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
        modifier = Modifier
            .fillMaxWidth()
            .testTag("post_share_sheet"),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
        ) {
            // Bottom action row: Add to story, WhatsApp, WhatsApp Status, Share, Copy link
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                verticalAlignment = Alignment.Top,
            ) {
                ShareActionButton(
                    iconRes = R.drawable.ic_add,
                    label = stringResource(R.string.share_add_to_story),
                    onClick = onAddToStory,
                    tag = "share_action_add_to_story",
                )
                ShareActionButton(
                    iconRes = R.drawable.ic_whatsapp,
                    label = stringResource(R.string.share_whatsapp),
                    onClick = onWhatsApp,
                    tag = "share_action_whatsapp",
                )
                ShareActionButton(
                    iconRes = R.drawable.ic_whatsapp,
                    label = stringResource(R.string.share_whatsapp_status),
                    onClick = onWhatsAppStatus,
                    tag = "share_action_whatsapp_status",
                )
                ShareActionButton(
                    iconRes = R.drawable.ic_share,
                    label = stringResource(R.string.share_external),
                    onClick = onShareExternal,
                    tag = "share_action_external",
                )
                ShareActionButton(
                    iconRes = R.drawable.ic_link,
                    label = stringResource(R.string.share_copy_link),
                    onClick = onCopyLink,
                    tag = "share_action_copy_link",
                )
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun ShareActionButton(iconRes: Int, label: String, onClick: () -> Unit, tag: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(68.dp)
            .clickable(onClick = onClick)
            .testTag(tag),
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = label,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp),
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
