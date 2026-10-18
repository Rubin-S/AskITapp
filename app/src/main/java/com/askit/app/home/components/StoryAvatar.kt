package com.askit.app.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.askit.app.home.model.Story
import com.askit.designsystem.R
import com.askit.app.R as AppR

// Keep ring geometry in dp so the subtle spacing survives density changes.
@Composable
private fun Modifier.storyRing(isSeen: Boolean): Modifier {
    val ring = if (isSeen) {
        Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
    } else {
        Modifier.border(
            width = 2.dp,
            brush = Brush.sweepGradient(
                listOf(
                    MaterialTheme.colorScheme.secondary,
                    Color(0xFF159E92),
                    MaterialTheme.colorScheme.secondary,
                ),
            ),
            shape = CircleShape,
        )
    }
    // Unseen: 2dp stroke + 1dp gap. Seen: 1dp stroke + 2.5dp gap.
    return then(ring).padding(if (isSeen) 3.5.dp else 3.dp)
}

@Composable
fun StoryAvatar(story: Story, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val seenState = stringResource(if (story.isSeen) AppR.string.story_state_seen else AppR.string.story_state_unseen)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .width(82.dp)
            .clickable(role = Role.Button, onClick = onClick)
            // One TalkBack stop: the name once (from the label below), plus seen/unseen state.
            .semantics(mergeDescendants = true) { stateDescription = seenState },
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(80.dp)
                .storyRing(story.isSeen),
        ) {
            if (!story.authorAvatarUrl.isNullOrBlank()) {
                AsyncImage(
                    model = story.authorAvatarUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                )
            } else {
                val initial = story.authorName.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?"
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                ) {
                    Text(
                        text = initial,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = story.authorName,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                letterSpacing = (-0.2).sp,
            ),
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(80.dp),
        )
    }
}

@Composable
fun AddStoryAvatar(
    modifier: Modifier = Modifier,
    myStory: Story? = null,
    userAvatarUrl: String? = null,
    onClick: (() -> Unit)? = null,
    onStoryClick: ((Story) -> Unit)? = null,
    onAddStoryClick: () -> Unit = onClick ?: {},
) {
    val actualAddClick = onClick ?: onAddStoryClick
    val avatarUrl = myStory?.authorAvatarUrl ?: userAvatarUrl
    val yourStoryLabel = stringResource(AppR.string.story_your_story)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .width(82.dp)
            .then(
                if (myStory == null) {
                    Modifier.clickable(role = Role.Button, onClick = actualAddClick)
                } else {
                    Modifier
                },
            ),
    ) {
        Box(
            modifier = Modifier.size(80.dp),
            contentAlignment = Alignment.Center,
        ) {
            // Avatar circle with optional ring
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .then(if (myStory != null) Modifier.storyRing(myStory.isSeen) else Modifier)
                    .then(
                        if (myStory != null) {
                            Modifier
                                .clip(CircleShape)
                                .semantics { contentDescription = yourStoryLabel }
                                .clickable(role = Role.Button) {
                                    onStoryClick?.invoke(myStory)
                                }
                        } else {
                            Modifier
                        },
                    ),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                ) {
                    if (!avatarUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = avatarUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                        )
                    } else {
                        Icon(
                            painter = painterResource(R.drawable.ic_person),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(36.dp),
                        )
                    }
                }
            }

            // Plus icon badge at bottom-right with touch area
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 6.dp, y = 6.dp)
                    .size(48.dp)
                    .clickable(
                        role = Role.Button,
                        onClick = actualAddClick,
                    ),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(24.dp)
                        .border(2.5.dp, MaterialTheme.colorScheme.background, CircleShape)
                        .clip(CircleShape)
                        .background(Color.White),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_add),
                        contentDescription = stringResource(AppR.string.story_add_new_title),
                        tint = Color.Black,
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(AppR.string.story_your_story),
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                letterSpacing = (-0.2).sp,
            ),
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .width(80.dp)
                .then(
                    if (myStory != null) {
                        Modifier.clickable(role = Role.Button) {
                            onStoryClick?.invoke(myStory)
                        }
                    } else {
                        Modifier
                    },
                ),
        )
    }
}
