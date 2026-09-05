package com.askit.app.home.stories

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.askit.designsystem.R
import com.askit.designsystem.people.AskITAvatar
import com.askit.designsystem.stories.StoryProgressRow

/**
 * Viewer top overlay: segmented progress bars plus the author row with a close button.
 */
@Composable
fun StoryTopOverlay(
    group: StoryGroup,
    itemIndex: Int,
    itemProgressFraction: Float,
    timeLabel: String,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    isOwnStory: Boolean = false,
    onAddStory: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .testTag("story_top_overlay"),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        StoryProgressRow(
            segmentCount = group.stories.size,
            activeSegmentIndex = itemIndex,
            activeSegmentFraction = itemProgressFraction,
            modifier = Modifier.padding(horizontal = 8.dp),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .then(
                        if (isOwnStory && onAddStory != null) {
                            Modifier
                                .clip(MaterialTheme.shapes.small)
                                .clickable(role = Role.Button, onClick = onAddStory)
                                .padding(vertical = 4.dp, horizontal = 2.dp)
                        } else {
                            Modifier
                        },
                    ),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    AskITAvatar(
                        avatarUrl = group.authorAvatarUrl,
                        avatarSize = 32.dp,
                        fallbackIconSize = 20.dp,
                    )
                    if (isOwnStory) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(13.dp)
                                .border(1.5.dp, Color.Black, CircleShape)
                                .clip(CircleShape)
                                .background(Color.White),
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_add),
                                contentDescription = stringResource(com.askit.app.R.string.story_add_new_title),
                                tint = Color.Black,
                                modifier = Modifier.size(8.dp),
                            )
                        }
                    }
                }
                Text(
                    text = if (isOwnStory) stringResource(com.askit.app.R.string.story_your_story) else group.authorName,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = timeLabel,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.8f),
                maxLines = 1,
            )
            IconButton(
                onClick = onClose,
                modifier = Modifier.testTag("story_close"),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_close),
                    contentDescription = stringResource(R.string.story_viewer_close),
                    tint = Color.White,
                )
            }
        }
    }
}
