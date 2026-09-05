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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.askit.designsystem.R
import com.askit.designsystem.people.AskITAvatar

data class ShareContact(
    val id: String,
    val name: String,
    val avatarUrl: String? = null,
)

val defaultSampleShareContacts: List<ShareContact> = listOf(
    ShareContact(
        id = "sweetlin",
        name = "Sweetlin! \uD83C\uDF37",
        avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150",
    ),
    ShareContact(
        id = "kavibharathi",
        name = "kavibharathi .D.R",
        avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150",
    ),
    ShareContact(
        id = "jd",
        name = "jd",
        avatarUrl = "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=150",
    ),
    ShareContact(
        id = "eeea",
        name = "euıəəəa",
        avatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150",
    ),
    ShareContact(
        id = "raagesh",
        name = "Raagesh",
        avatarUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=150",
    ),
    ShareContact(
        id = "priya",
        name = "Priya Sharma",
        avatarUrl = "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=150",
    ),
    ShareContact(
        id = "suresh",
        name = "Suresh Kumar",
        avatarUrl = "https://images.unsplash.com/photo-1522075469751-3a6694fb2f61?w=150",
    ),
    ShareContact(
        id = "ananya",
        name = "Ananya Roy",
        avatarUrl = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=150",
    ),
    ShareContact(
        id = "karthik",
        name = "Karthik Raja",
        avatarUrl = "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=150",
    ),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostShareSheet(
    onDismiss: () -> Unit,
    contacts: List<ShareContact> = defaultSampleShareContacts,
    onSendToContact: (ShareContact) -> Unit,
    onCopyLink: () -> Unit,
    onShareExternal: () -> Unit,
    onWhatsApp: () -> Unit,
    onWhatsAppStatus: () -> Unit,
    onAddToStory: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    var searchQuery by remember { mutableStateOf("") }
    val filteredContacts = remember(searchQuery, contacts) {
        if (searchQuery.isBlank()) contacts else {
            val needle = searchQuery.trim().lowercase()
            contacts.filter { it.name.lowercase().contains(needle) }
        }
    }

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
            // Search field
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        text = stringResource(R.string.share_search_placeholder),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_explore_outlined),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp),
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("share_search_field"),
                shape = RoundedCornerShape(16.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
                singleLine = true,
            )

            Spacer(Modifier.height(8.dp))

            // Grid of DM Contacts
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(filteredContacts, key = { it.id }) { contact ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSendToContact(contact) }
                            .padding(8.dp),
                    ) {
                        AskITAvatar(
                            avatarUrl = contact.avatarUrl,
                            avatarSize = 64.dp,
                            fallbackIconSize = 36.dp,
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = contact.name,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            HorizontalDivider(
                color = DividerDefaults.color.copy(alpha = 0.5f),
                modifier = Modifier.padding(vertical = 8.dp),
            )

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
private fun ShareActionButton(
    iconRes: Int,
    label: String,
    onClick: () -> Unit,
    tag: String,
) {
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
