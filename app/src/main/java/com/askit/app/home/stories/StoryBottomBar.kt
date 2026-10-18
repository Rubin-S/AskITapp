package com.askit.app.home.stories

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.unit.dp
import com.askit.designsystem.R

@Composable
fun StoryBottomBar(
    isLiked: Boolean,
    onLikeClick: () -> Unit,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier,
    isOwnStory: Boolean = false,
) {
    Row(
        modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
    ) {
        if (!isOwnStory) {
            IconButton(onClick = onLikeClick, modifier = Modifier.size(48.dp).testTag("story_like")
                .semantics { selected = isLiked }) {
                Icon(painterResource(R.drawable.ic_favorite),
                    contentDescription = stringResource(if (isLiked) com.askit.app.R.string.story_unlike else R.string.story_viewer_like),
                    tint = if (isLiked) Color(0xFFFF3040) else Color.White)
            }
        }
        IconButton(onClick = onShareClick, modifier = Modifier.size(48.dp).testTag("story_share")) {
            Icon(painterResource(R.drawable.ic_share), contentDescription = stringResource(R.string.post_feed_share), tint = Color.White)
        }
    }
}
