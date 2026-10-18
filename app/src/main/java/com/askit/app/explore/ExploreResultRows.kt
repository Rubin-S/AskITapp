package com.askit.app.explore

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.askit.app.R
import com.askit.app.task.presentation
import com.askit.designsystem.people.PersonResultItem
import com.askit.designsystem.services.ServiceResultItem
import com.askit.designsystem.tasks.TaskResultItem
import com.askit.designsystem.tasks.TaskResultStatus

internal fun LazyListScope.addPersonResultRows(
    itemKeyPrefix: String,
    people: List<ExplorePersonResult>,
    onClick: ((String) -> Unit)?,
    firstRowTag: String?,
    addTopPadding: Boolean,
) {
    buildExplorePersonRenderRows(people).forEachIndexed { index, row ->
        val person = row.result
        item(
            key = "person:$itemKeyPrefix:${row.uiKey}",
            contentType = "person",
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (index == 0 && addTopPadding) Modifier.padding(top = 24.dp) else Modifier)
                    .then(if (index == 0 && firstRowTag != null) Modifier.testTag(firstRowTag) else Modifier),
            ) {
                if (index > 0) HorizontalDivider()
                PersonResultItem(
                    name = person.name,
                    avatarUrl = person.avatarUrl,
                    primaryService = person.primaryService,
                    additionalServices = person.additionalServices,
                    rating = person.rating,
                    reviewCount = person.reviewCount,
                    locationLabel = person.locationLabel,
                    priceLabel = person.priceLabel,
                    statusLabel = person.statusLabel,
                    onClick = onClick
                        ?.takeIf { row.stableId.isNotEmpty() }
                        ?.let { click -> { click(row.stableId) } },
                )
            }
        }
    }
}

internal fun LazyListScope.addServiceResultRows(
    people: List<ExplorePersonResult>,
    onClick: ((String) -> Unit)?,
) {
    buildExplorePersonRenderRows(distinctPeopleById(people)).forEachIndexed { index, row ->
        val person = row.result
        item(
            key = "service:person:${row.uiKey}",
            contentType = "service",
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = if (index == 0) 24.dp else 12.dp)
                    .then(
                        if (index == 0) {
                            Modifier.testTag("explore_submitted_services")
                        } else {
                            Modifier
                        },
                    ),
            ) {
                ServiceResultItem(
                    serviceTitle = person.primaryService.orEmpty(),
                    category = "",
                    description = person.additionalServices
                        .map(String::trim)
                        .filter(String::isNotEmpty)
                        .joinToString(", "),
                    providerName = person.name,
                    providerAvatarUrl = person.avatarUrl,
                    priceLabel = person.priceLabel,
                    coverageLabel = person.locationLabel,
                    modifier = Modifier.testTag("explore_service_result_card"),
                    onClick = onClick
                        ?.takeIf { row.stableId.isNotEmpty() }
                        ?.let { click -> { click(row.stableId) } },
                )
            }
        }
    }
}

internal fun LazyListScope.addTaskResultRows(
    itemKeyPrefix: String,
    tasks: List<ExploreTaskResult>,
    onClick: ((String) -> Unit)?,
    firstRowTag: String?,
    addTopPadding: Boolean,
) {
    buildExploreTaskRenderRows(tasks).forEachIndexed { index, row ->
        val task = row.result
        item(
            key = "task:$itemKeyPrefix:${row.uiKey}",
            contentType = "task",
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .then(if (index == 0 && addTopPadding) Modifier.padding(top = 24.dp) else Modifier)
                    .then(if (index == 0 && firstRowTag != null) Modifier.testTag(firstRowTag) else Modifier),
            ) {
                val display = task.sourceTask?.presentation(LocalContext.current)
                TaskResultItem(
                    title = task.title,
                    category = display?.category ?: task.category,
                    summary = task.summary,
                    budgetLabel = display?.budget ?: task.budgetLabel,
                    locationLabel = display?.location ?: task.locationLabel,
                    timingLabel = display?.timing ?: task.timingLabel,
                    posterName = task.posterName,
                    postedLabel = display?.posted ?: task.postedLabel,
                    status = task.status,
                    photoModels = task.photoModels,
                    distanceLabel = task.distanceLabel,
                    scopeHighlights = display?.requirements ?: task.scopeHighlights,
                    onClick = onClick
                        ?.takeIf {
                            task.status != TaskResultStatus.Unavailable && row.stableId.isNotEmpty()
                        }
                        ?.let { click -> { click(row.stableId) } },
                )
            }
        }
    }
}

@Composable
internal fun ExploreResultSectionHeading(
    heading: String,
    testTag: String,
    onViewMore: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp, bottom = 4.dp)
            .testTag(testTag),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = heading,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.semantics { heading() },
        )
        if (onViewMore != null) {
            Row(
                modifier = Modifier
                    .clickable(
                        role = androidx.compose.ui.semantics.Role.Button,
                        onClickLabel = "View more $heading",
                        onClick = onViewMore,
                    )
                    .padding(vertical = 4.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = stringResource(R.string.profile_view_all),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Icon(
                    painter = painterResource(com.askit.designsystem.R.drawable.ic_chevron_right),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}
