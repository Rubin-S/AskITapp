package com.askit.app.createpost.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.askit.app.R
import com.askit.app.createpost.CreatePostViewModel
import com.askit.app.createpost.PollOptionError
import com.askit.app.createpost.PostPollClosingRule
import com.askit.app.createpost.PostPollDraft
import com.askit.app.createpost.PostValidationField
import com.askit.designsystem.R as DesignR
import java.text.DateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CreatePostPollSection(
    viewModel: CreatePostViewModel,
    content: PostPollDraft?,
    validationErrors: Set<PostValidationField>,
    questionBringIntoViewRequester: BringIntoViewRequester,
    optionsBringIntoViewRequester: BringIntoViewRequester,
    questionFocusRequester: FocusRequester,
    onOpenClosingDatePicker: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (content == null) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
            ),
            modifier = modifier.fillMaxWidth(),
        ) {
            TextButton(
                onClick = viewModel::addPoll,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .testTag("create_post_add_poll"),
            ) {
                Icon(
                    painter = painterResource(DesignR.drawable.ic_service),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.create_post_add_poll),
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    } else {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
            ),
            modifier = modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.create_post_add_poll),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    TextButton(
                        onClick = viewModel::removePoll,
                        modifier = Modifier
                            .heightIn(min = 36.dp)
                            .testTag("create_post_remove_poll"),
                    ) {
                        Text(
                            text = stringResource(R.string.create_post_remove_poll),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                            ),
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }

                OutlinedTextField(
                    value = content.question,
                    onValueChange = viewModel::updatePollQuestion,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .bringIntoViewRequester(questionBringIntoViewRequester)
                        .focusRequester(questionFocusRequester)
                        .testTag("create_post_poll_question"),
                    label = { Text(stringResource(R.string.create_post_question)) },
                    supportingText = {
                        if (PostValidationField.POLL_QUESTION in validationErrors) {
                            Text(stringResource(R.string.create_post_error_poll_question))
                        }
                    },
                    isError = PostValidationField.POLL_QUESTION in validationErrors,
                    minLines = 2,
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .bringIntoViewRequester(optionsBringIntoViewRequester)
                        .testTag("create_post_poll_options"),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    content.options.forEachIndexed { index, option ->
                        val optionError = viewModel.pollOptionError(index)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top,
                        ) {
                            OutlinedTextField(
                                value = option,
                                onValueChange = { viewModel.updatePollOption(index, it) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("create_post_poll_option_$index"),
                                label = { Text(stringResource(R.string.create_post_poll_option, index + 1)) },
                                supportingText = {
                                    when (optionError) {
                                        PollOptionError.EMPTY -> Text(stringResource(R.string.create_post_error_poll_answer))
                                        PollOptionError.DUPLICATE -> Text(stringResource(R.string.create_post_error_poll_duplicate))
                                        null -> Unit
                                    }
                                },
                                isError = optionError != null && PostValidationField.POLL_OPTIONS in validationErrors,
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    imeAction = ImeAction.Next,
                                    keyboardType = KeyboardType.Text,
                                ),
                            )
                            if (content.options.size > 2) {
                                IconButton(
                                    onClick = { viewModel.removePollOption(index) },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .testTag("create_post_remove_poll_option_$index"),
                                ) {
                                    Icon(
                                        painter = painterResource(DesignR.drawable.ic_close),
                                        contentDescription = stringResource(
                                            R.string.create_post_remove_poll_option,
                                            index + 1,
                                        ),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }

                if (content.options.size < CreatePostViewModel.MAX_POLL_OPTIONS) {
                    TextButton(
                        onClick = viewModel::addPollOption,
                        modifier = Modifier
                            .heightIn(min = 44.dp)
                            .testTag("create_post_add_poll_option"),
                    ) {
                        Icon(
                            painter = painterResource(DesignR.drawable.ic_add),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.create_post_add_option),
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = pollClosingSummary(content),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    TextButton(
                        onClick = onOpenClosingDatePicker,
                        modifier = Modifier
                            .heightIn(min = 40.dp)
                            .testTag("create_post_change_closing_time"),
                    ) {
                        Text(
                            text = stringResource(R.string.create_post_change_closing_time),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                            ),
                        )
                    }
                }

                if (PostValidationField.POLL_CLOSING in validationErrors) {
                    val text = stringResource(R.string.create_post_error_poll_closing)
                    Text(
                        text = text,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.semantics { error(text) },
                    )
                }
            }
        }
    }
}

@Composable
fun pollClosingSummary(content: PostPollDraft): String = when (content.closingRule) {
    PostPollClosingRule.AFTER_24_HOURS -> stringResource(R.string.create_post_closes_24_hours)
    PostPollClosingRule.CUSTOM_DATE -> content.closingAtMillis?.let { millis ->
        stringResource(R.string.create_post_closes_on, formatDate(millis))
    } ?: stringResource(R.string.create_post_closes_24_hours)
}

private fun formatDate(millis: Long): String =
    DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault()).format(Date(millis))
