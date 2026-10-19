package com.askit.app.posttask

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.askit.app.R
import com.askit.app.forms.FormSummaryRow
import com.askit.app.task.TaskDetailContent
import com.askit.app.task.presentation
import com.askit.app.task.toPublicTask
import com.askit.designsystem.tasks.TaskResultItem
import com.askit.designsystem.R as DsR

@Composable
internal fun PostTaskReview(modifier: Modifier = Modifier, state: PostTaskFormState, onEdit: () -> Unit) {
    val context = LocalContext.current
    val draft = state.toDraft()
    val task = draft.toPublicTask("review", stringResource(R.string.post_task_you_creator))
    val display = task.presentation(context)
    val scopeHighlights = display.requirements.take(2)
    val privateAddress = state.privateAddressOrLandmark.trim()
    val accessInstructions = state.accessInstructions.trim()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = 640.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Review header
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = stringResource(R.string.post_task_review_title),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(R.string.post_task_review_helper),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // Top Hero: Feed Card Preview (How it appears in Explore & Home)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    painter = painterResource(DsR.drawable.ic_visibility),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = stringResource(R.string.post_task_feed_preview),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            TaskResultItem(
                title = state.title.trim(),
                category = display.category,
                summary = state.details.trim().takeIf(String::isNotEmpty),
                budgetLabel = display.budget,
                locationLabel = display.location,
                timingLabel = display.timing,
                posterName = stringResource(R.string.post_task_you_creator),
                postedLabel = stringResource(R.string.post_task_review_draft),
                status = null,
                photoModels = state.photoUris,
                scopeHighlights = scopeHighlights,
                modifier = Modifier.testTag("post_task_provider_preview_card"),
            )
        }

        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant,
            modifier = Modifier.padding(vertical = 4.dp),
        )

        // Full Task Details: the same composable the task detail screen renders.
        Text(
            text = stringResource(R.string.post_task_full_details),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.semantics { heading() },
        )
        TaskDetailContent(
            task = task,
            display = display,
            posterInitial = stringResource(R.string.post_task_you_initial),
            modifier = Modifier.testTag("post_task_detail_preview"),
        )

        // Dedicated Private Information Card (with lock icon & reassurance)
        if (state.workMode == PostTaskWorkMode.AT_MY_LOCATION &&
            (privateAddress.isNotBlank() || accessInstructions.isNotBlank())
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            painter = painterResource(DsR.drawable.ic_lock),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp),
                        )
                        Text(
                            text = stringResource(R.string.post_task_private_information),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }

                    Text(
                        text = stringResource(R.string.post_task_private_visibility),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    if (privateAddress.isNotBlank()) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        FormSummaryRow(R.string.post_task_address_optional, privateAddress)
                    }

                    if (accessInstructions.isNotBlank()) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        FormSummaryRow(R.string.post_task_access_instructions, accessInstructions)
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Edit button
        OutlinedButton(
            onClick = onEdit,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp)
                .testTag("post_task_edit"),
        ) {
            Text(stringResource(R.string.post_task_edit))
        }
    }
}
