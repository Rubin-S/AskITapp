package com.askit.app.home.details

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.askit.app.R
import com.askit.app.home.components.HomePostItem
import com.askit.app.home.data.getFeedPostById
import com.askit.app.home.model.FeedPost
import com.askit.designsystem.posts.CommentsBottomSheet
import com.askit.designsystem.posts.PostShareSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    postId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val post = remember(postId) { getFeedPostById(postId) }
    var activeCommentPost by remember { mutableStateOf<FeedPost?>(null) }
    var activeSharePost by remember { mutableStateOf<FeedPost?>(null) }
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.explore_back),
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }

                Text(
                    text = stringResource(R.string.post_detail_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp),
                )

                IconButton(onClick = {}) {
                    Icon(
                        painter = painterResource(com.askit.designsystem.R.drawable.ic_more_horiz),
                        contentDescription = stringResource(com.askit.designsystem.R.string.post_feed_more_options),
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            // Post content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
            ) {
                HomePostItem(
                    post = post,
                    modifier = Modifier.fillMaxWidth(),
                    onCommentClick = { activeCommentPost = post },
                    onShareClick = { activeSharePost = post },
                )
            }
        }
    }

    activeCommentPost?.let { _ ->
        CommentsBottomSheet(
            onDismiss = { activeCommentPost = null },
            currentUserAvatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150",
        )
    }

    activeSharePost?.let { p ->
        val linkCopiedMsg = stringResource(com.askit.designsystem.R.string.share_link_copied)
        val sentToTemplate = stringResource(com.askit.designsystem.R.string.share_sent_to)

        PostShareSheet(
            onDismiss = { activeSharePost = null },
            onSendToContact = { contact ->
                Toast.makeText(
                    context,
                    java.lang.String.format(sentToTemplate, contact.name),
                    Toast.LENGTH_SHORT,
                ).show()
                activeSharePost = null
            },
            onCopyLink = {
                clipboardManager.setText(AnnotatedString("https://askit.app/p/${p.id}"))
                Toast.makeText(
                    context,
                    linkCopiedMsg,
                    Toast.LENGTH_SHORT,
                ).show()
                activeSharePost = null
            },
            onShareExternal = {
                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    putExtra(Intent.EXTRA_TEXT, "${p.authorName}: ${p.content}\nhttps://askit.app/p/${p.id}")
                    type = "text/plain"
                }
                val shareIntent = Intent.createChooser(sendIntent, null)
                context.startActivity(shareIntent)
                activeSharePost = null
            },
            onWhatsApp = {
                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    putExtra(Intent.EXTRA_TEXT, "${p.authorName}: ${p.content}\nhttps://askit.app/p/${p.id}")
                    type = "text/plain"
                    setPackage("com.whatsapp")
                }
                try {
                    context.startActivity(sendIntent)
                } catch (e: Exception) {
                    val shareIntent = Intent.createChooser(sendIntent, null)
                    context.startActivity(shareIntent)
                }
                activeSharePost = null
            },
            onWhatsAppStatus = {
                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    putExtra(Intent.EXTRA_TEXT, "${p.authorName}: ${p.content}\nhttps://askit.app/p/${p.id}")
                    type = "text/plain"
                    setPackage("com.whatsapp")
                }
                try {
                    context.startActivity(sendIntent)
                } catch (e: Exception) {
                    val shareIntent = Intent.createChooser(sendIntent, null)
                    context.startActivity(shareIntent)
                }
                activeSharePost = null
            },
            onAddToStory = {
                activeSharePost = null
            },
        )
    }
}
