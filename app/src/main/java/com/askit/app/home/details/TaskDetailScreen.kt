package com.askit.app.home.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.askit.app.R
import com.askit.app.task.Task
import com.askit.app.task.TaskActionDialogs
import com.askit.app.task.TaskActionDraft
import com.askit.app.task.TaskActionPolicy
import com.askit.app.task.TaskActionsViewModel
import com.askit.app.task.TaskCommentAuthor
import com.askit.app.task.TaskDiscussionSheet
import com.askit.app.task.TaskDiscussionViewModel
import com.askit.app.task.TaskPhotoGallery
import com.askit.app.task.TaskPreferencesSummary
import com.askit.app.task.TaskProviderPreferences
import com.askit.app.task.TaskRepository
import com.askit.app.task.actionPolicy
import com.askit.app.task.presentation
import com.askit.designsystem.R as DsR

@Composable
fun TaskDetailScreen(
    taskId: String,
    taskRepository: TaskRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onApply: (Task) -> Unit = {},
    onMessage: (Task) -> Unit = {},
    discussionViewModel: TaskDiscussionViewModel? = null,
    discussionAuthor: TaskCommentAuthor? = null,
    currentDiscussionAuthor: () -> TaskCommentAuthor? = { discussionAuthor },
    actionsViewModel: TaskActionsViewModel? = null,
    accountId: String? = null,
    currentAccountId: () -> String? = { accountId },
    hasServiceListing: Boolean = false,
    hasApplied: Boolean = false,
    onEdit: (Task) -> Unit = {},
    onDeleted: (String) -> Unit = {},
) {
    val tasks by taskRepository.tasks.collectAsStateWithLifecycle()
    val task = tasks.firstOrNull { it.id == taskId } ?: taskRepository.getTask(taskId)

    if (task == null) {
        TaskNotFoundContent(
            taskId = taskId,
            onBack = onBack,
            modifier = modifier,
        )
    } else {
        val likes by taskRepository.likes.collectAsStateWithLifecycle()
        val likedBy = likes[task.id].orEmpty()
        val policy = task.actionPolicy(accountId, hasServiceListing, hasApplied)
        val snackbar = remember { SnackbarHostState() }
        val resources = LocalResources.current
        val actionDraft = if (actionsViewModel != null) {
            val drafts by actionsViewModel.drafts.collectAsStateWithLifecycle()
            if (actionsViewModel.isActiveAccount(accountId)) drafts[task.id] ?: TaskActionDraft() else TaskActionDraft()
        } else {
            TaskActionDraft()
        }
        LaunchedEffect(actionDraft.feedbackRes) {
            actionDraft.feedbackRes?.let {
                snackbar.showSnackbar(resources.getString(it))
                actionsViewModel?.consumeFeedback(task.id)
            }
        }
        TaskDetailScreen(
            task = task,
            onBack = onBack,
            modifier = modifier,
            onApply = onApply,
            onMessage = onMessage,
            onComment = { discussionViewModel?.open(task.id) },
            policy = policy,
            likeCount = likedBy.size,
            isLiked = accountId != null && accountId in likedBy,
            hasApplied = hasApplied,
            onLike = { actionsViewModel?.like(task.id, currentAccountId()) },
            onEdit = { onEdit(task) },
            onDelete = { actionsViewModel?.openDelete(task.id, currentAccountId()) },
            onReport = { actionsViewModel?.openReport(task.id, currentAccountId()) },
            snackbar = snackbar,
        )
        discussionViewModel?.let { TaskDiscussionSheet(task, it, discussionAuthor, currentDiscussionAuthor) }
        actionsViewModel?.let { vm ->
            TaskActionDialogs(
                draft = actionDraft,
                policy = policy,
                taskTitle = task.title,
                onCancelReport = { vm.cancelReport(task.id) },
                onSelectReason = { vm.selectReason(task.id, it) },
                onReport = { vm.report(task.id, currentAccountId()) },
                onCancelDelete = { vm.cancelDelete(task.id) },
                onDelete = { if (vm.delete(task.id, currentAccountId())) onDeleted(task.id) },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    task: Task,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onApply: (Task) -> Unit = {},
    onMessage: (Task) -> Unit = {},
    onComment: () -> Unit = {},
    policy: TaskActionPolicy = task.actionPolicy(null),
    likeCount: Int = 0,
    isLiked: Boolean = false,
    hasApplied: Boolean = false,
    onLike: () -> Unit = {},
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {},
    onReport: () -> Unit = {},
    snackbar: SnackbarHostState = remember { SnackbarHostState() },
) {
    val display = task.presentation(LocalContext.current)
    var showMenu by rememberSaveable(task.id, policy.signedIn, policy.isOwner) { mutableStateOf(false) }
    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.task_detail_title),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(DsR.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.explore_back),
                        )
                    }
                },
                actions = {
                    if (!policy.isOwner) {
                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(painterResource(DsR.drawable.ic_more_horiz), stringResource(R.string.task_more_actions))
                            }
                            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.task_report)) },
                                    enabled = policy.canReport,
                                    onClick = {
                                        showMenu = false
                                        onReport()
                                    },
                                )
                            }
                        }
                    }
                },
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 4.dp,
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (policy.isOwner) {
                        OutlinedButton(onClick = onDelete, modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.task_delete), color = MaterialTheme.colorScheme.error)
                        }
                        Button(onClick = onEdit, modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.task_edit))
                        }
                    } else {
                        OutlinedButton(
                            onClick = { onMessage(task) },
                            enabled = !task.posterId.isNullOrBlank(),
                            modifier = Modifier.weight(1f),
                        ) { Text(stringResource(DsR.string.profile_action_message)) }
                        if (policy.showApply) {
                            Button(onClick = { onApply(task) }, enabled = policy.canApply, modifier = Modifier.weight(1.5f)) {
                                Text(stringResource(if (hasApplied) R.string.task_applied else R.string.task_detail_apply))
                            }
                        }
                    }
                }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Text(
                        text = display.category,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }

                Text(
                    text = display.posted,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Text(
                text = task.title,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = stringResource(R.string.task_detail_budget),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = display.budget,
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.task_detail_urgency),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = display.timing,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = if (display.timing.contains("Urgent", ignoreCase = true)) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.primary
                            },
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = task.posterName.firstOrNull()?.toString() ?: "?",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    task.posterAvatarUrl?.takeIf(String::isNotBlank)?.let { avatar ->
                        AsyncImage(
                            model = avatar,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = task.posterName,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = display.location,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onLike, enabled = policy.canLike, modifier = Modifier.weight(1f)) {
                    Icon(
                        painterResource(if (isLiked) DsR.drawable.ic_heart_filled else DsR.drawable.ic_heart_outline),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        stringResource(if (isLiked) R.string.task_liked_count else R.string.task_like_count, likeCount),
                        modifier = Modifier.padding(start = 6.dp),
                    )
                }
                OutlinedButton(onClick = onComment, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.task_discussion_comment))
                }
            }

            TaskPhotoGallery(task.photos)
            TaskPreferencesSummary(task.publicDetails?.preferences ?: TaskProviderPreferences())

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.list_service_description),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 22.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (display.requirements.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.task_detail_requirements),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    display.requirements.forEach { req ->
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                text = "•",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Text(
                                text = req,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskNotFoundContent(taskId: String, onBack: () -> Unit, modifier: Modifier = Modifier) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.task_detail_title),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(DsR.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.explore_back),
                        )
                    }
                },
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = stringResource(R.string.task_detail_not_found),
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(R.string.task_detail_not_found_body, taskId),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Button(onClick = onBack) {
                    Text(stringResource(R.string.task_detail_go_back))
                }
            }
        }
    }
}
