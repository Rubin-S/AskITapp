package com.askit.app.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.askit.app.home.model.Story
import com.askit.designsystem.R

// Instagram-grade continuous rainbow ring gradient
val InstagramStoryGradient = Brush.sweepGradient(
    colors = listOf(
        Color(0xFFFEDA75), // Yellow
        Color(0xFFFA7E1E), // Orange
        Color(0xFFD62976), // Pink
        Color(0xFF962FBF), // Purple
        Color(0xFF4F5BD5), // Indigo
        Color(0xFFD62976), // Pink
        Color(0xFFFEDA75), // Wrap
    ),
)

@Composable
fun StoryAvatar(
    story: Story,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val ringModifier = if (story.isSeen) {
        Modifier.border(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant,
            shape = CircleShape,
        )
    } else {
        Modifier.border(
            width = 2.5.dp,
            brush = InstagramStoryGradient,
            shape = CircleShape,
        )
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .width(82.dp)
            .clickable(role = Role.Button, onClick = onClick),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(80.dp)
                .then(ringModifier)
                .padding(3.dp),
        ) {
            if (!story.authorAvatarUrl.isNullOrBlank()) {
                AsyncImage(
                    model = story.authorAvatarUrl,
                    contentDescription = story.authorName,
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

    val ringModifier = when {
        myStory == null -> Modifier
        myStory.isSeen -> Modifier.border(
            width = 1.2.dp,
            color = MaterialTheme.colorScheme.outlineVariant,
            shape = CircleShape,
        )
        else -> Modifier.border(
            width = 2.5.dp,
            brush = InstagramStoryGradient,
            shape = CircleShape,
        )
    }

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
                    .then(ringModifier)
                    .padding(if (myStory != null) 3.dp else 0.dp)
                    .then(
                        if (myStory != null) {
                            Modifier
                                .clip(CircleShape)
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
                    .size(36.dp)
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
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Your story",
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

