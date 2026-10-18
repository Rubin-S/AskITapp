package com.askit.app.home

import android.content.ClipData
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.testTag
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.askit.app.R
import com.askit.app.home.components.HomePeopleSection
import com.askit.app.home.components.HomePostItem
import com.askit.app.home.components.HomeServicesSection
import com.askit.app.home.components.HomeTasksSection
import com.askit.app.home.components.StoriesRail
import com.askit.app.home.model.FeedItem
import com.askit.app.home.model.FeedPost
import com.askit.app.home.model.PersonPreview
import com.askit.app.home.model.ServicePreview
import com.askit.app.home.model.Story
import com.askit.app.home.model.TaskPreview
import com.askit.designsystem.posts.CommentsBottomSheet
import com.askit.designsystem.posts.PostShareSheet
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier,
    onAddStoryClick: (FeedPost?) -> Unit = {},
    onStoryClick: (Story) -> Unit = {},
    onTaskClick: (TaskPreview) -> Unit = {},
    onServiceClick: (ServicePreview) -> Unit = {},
    onPersonClick: (PersonPreview) -> Unit = {},
    followingIds: Set<String> = emptySet(),
    followingAccountId: String? = null,
    onTogglePersonFollow: (PersonPreview) -> Unit = {},
    currentAccountId: () -> String? = { null },
    onEditPost: (String) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val postRecords by viewModel.posts.posts.collectAsStateWithLifecycle()
    val postAccount by viewModel.posts.account.collectAsStateWithLifecycle()
    var menuPostId by androidx.compose.runtime.saveable.rememberSaveable(postAccount.accountId) { mutableStateOf<String?>(null) }
    val postContext = LocalContext.current
    fun postAction(result: com.askit.app.home.posts.PostResult) {
        if (result is com.askit.app.home.posts.PostResult.Failure) Toast.makeText(postContext, result.messageRes, Toast.LENGTH_SHORT).show()
    }
    var activeCommentPost by remember { mutableStateOf<FeedPost?>(null) }
    var activeSharePost by remember { mutableStateOf<FeedPost?>(null) }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        when (val state = uiState) {
            HomeUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            is HomeUiState.Success -> {
                if (state.feedItems.isEmpty() && state.stories.isEmpty()) {
                    HomeEmptyState(
                        onRefresh = { viewModel.refresh() },
                    )
                } else {
                    val listState = rememberLazyListState()

                    val shouldLoadMore by remember {
                        derivedStateOf {
                            val totalItemsCount = listState.layoutInfo.totalItemsCount
                            val lastVisibleItemIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                            totalItemsCount > 0 && lastVisibleItemIndex >= totalItemsCount - 2
                        }
                    }

                    LaunchedEffect(shouldLoadMore) {
                        if (shouldLoadMore) {
                            viewModel.loadMore()
                        }
                    }

                    PullToRefreshBox(
                        isRefreshing = state.isRefreshing,
                        onRefresh = { viewModel.refresh() },
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 80.dp),
                            verticalArrangement = Arrangement.spacedBy(20.dp),
                        ) {
                            item(key = "home_header") {
                                Column {
                                    HomeTopBar()
                                    StoriesRail(
                                        stories = state.stories,
                                        onAddStoryClick = { onAddStoryClick(null) },
                                        onStoryClick = onStoryClick,
                                    )
                                }
                            }
                            items(
                                items = state.feedItems,
                                key = { item ->
                                    when (item) {
                                        is FeedItem.PostItem -> item.id
                                        is FeedItem.TaskSection -> item.id
                                        is FeedItem.ServiceSection -> item.id
                                        is FeedItem.PeopleSection -> item.id
                                    }
                                },
                            ) { item ->
                                when (item) {
                                    is FeedItem.PostItem -> {
                                        HomePostItem(
                                            post = item.post,
                                            modifier = Modifier.fillMaxWidth(),
                                            onCommentClick = { activeCommentPost = item.post },
                                            onShareClick = { activeSharePost = item.post },
                                            onMoreClick = { menuPostId = item.post.id },
                                            isLiked = item.post.id in postAccount.liked,
                                            isSaved = item.post.id in postAccount.saved,
                                            onLikeClick = { postAction(viewModel.posts.toggleLiked(item.post.id, currentAccountId())) },
                                            onSaveClick = { postAction(viewModel.posts.toggleSaved(item.post.id, currentAccountId())) },
                                        )
                                    }
                                    is FeedItem.TaskSection -> {
                                        HomeTasksSection(
                                            section = item,
                                            modifier = Modifier.fillMaxWidth(),
                                            onTaskClick = onTaskClick,
                                        )
                                    }
                                    is FeedItem.ServiceSection -> {
                                        HomeServicesSection(
                                            section = item,
                                            modifier = Modifier.fillMaxWidth(),
                                            onServiceClick = onServiceClick,
                                        )
                                    }
                                    is FeedItem.PeopleSection -> {
                                        HomePeopleSection(
                                            section = item,
                                            modifier = Modifier.fillMaxWidth(),
                                            onPersonClick = onPersonClick,
                                            followingIds = followingIds,
                                            accountId = followingAccountId,
                                            onToggleFollow = onTogglePersonFollow,
                                        )
                                    }
                                }
                            }

                            if (state.isLoadingMore) {
                                item(key = "loading_more_indicator") {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(28.dp),
                                            strokeWidth = 2.5.dp,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            is HomeUiState.Error -> {
                HomeErrorState(
                    message = state.message ?: "Something went wrong loading your feed.",
                    onRetry = { viewModel.refresh() },
                )
            }
        }
    }

    menuPostId?.let { id -> com.askit.app.home.posts.PostActionsSheet(id, viewModel.posts, currentAccountId,
        onDismiss = { menuPostId = null }, onEdit = onEditPost) }
    activeCommentPost?.let { selected ->
        val live = postRecords[selected.id]?.takeUnless { it.archived }
        if (live != null) CommentsBottomSheet(
            onDismiss = { activeCommentPost = null },
            canComment = live.commentsEnabled && viewModel.posts.isCurrentAccount(currentAccountId()),
            disabledMessage = stringResource(if (!live.commentsEnabled) R.string.post_comments_disabled else R.string.post_action_sign_in),
        )
    }

    activeSharePost?.let { post ->
        val context = LocalContext.current
        val clipboard = LocalClipboard.current
        val clipboardScope = rememberCoroutineScope()
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
                val link = "https://askit.app/p/${post.id}"
                clipboardScope.launch { clipboard.setClipEntry(ClipEntry(ClipData.newPlainText(link, link))) }
                Toast.makeText(
                    context,
                    linkCopiedMsg,
                    Toast.LENGTH_SHORT,
                ).show()
                activeSharePost = null
            },
            onShareExternal = {
                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    putExtra(Intent.EXTRA_TEXT, "${post.authorName}: ${post.content}\nhttps://askit.app/p/${post.id}")
                    type = "text/plain"
                }
                val shareIntent = Intent.createChooser(sendIntent, null)
                context.startActivity(shareIntent)
                activeSharePost = null
            },
            onWhatsApp = {
                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    putExtra(Intent.EXTRA_TEXT, "${post.authorName}: ${post.content}\nhttps://askit.app/p/${post.id}")
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
                    putExtra(Intent.EXTRA_TEXT, "${post.authorName}: ${post.content}\nhttps://askit.app/p/${post.id}")
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
                onAddStoryClick(post)
                activeSharePost = null
            },
        )
    }
}

@Composable
private fun HomeTopBar(
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 16.dp, end = 16.dp, top = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(R.drawable.ic_askit_wordmark),
            contentDescription = stringResource(R.string.auth_logo),
            modifier = Modifier
                .width(108.dp)
                .height(28.dp)
                .testTag("home_wordmark"),
        )
    }
}

@Composable
private fun HomeEmptyState(
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.home_empty_title),
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
            ),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.home_empty_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(onClick = onRefresh) {
            Text(stringResource(R.string.explore_refresh))
        }
    }
}

@Composable
private fun HomeErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.home_error_title),
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
            ),
            color = MaterialTheme.colorScheme.error,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text(stringResource(R.string.explore_retry))
        }
    }
}
