package com.askit.designsystem.jobs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.askit.designsystem.R

enum class JobStepState {
    Done,
    Current,
    Upcoming,
}

data class JobStepperItem(
    val label: String,
    val state: JobStepState,
)

@Composable
fun JobStepper(
    steps: List<JobStepperItem>,
    stepOfDescription: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = stepOfDescription },
    ) {
        steps.forEachIndexed { index, step ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .defaultMinSize(minHeight = 48.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier
                        .width(24.dp)
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    StepNode(state = step.state)
                    if (index < steps.lastIndex) {
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .weight(1f)
                                .background(MaterialTheme.colorScheme.outline),
                        )
                    }
                }
                Text(
                    text = step.label,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = if (step.state == JobStepState.Current) {
                            FontWeight.Medium
                        } else {
                            FontWeight.Normal
                        },
                    ),
                    color = if (step.state == JobStepState.Upcoming) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp),
                )
            }
        }
    }
}

@Composable
private fun StepNode(state: JobStepState) {
    val colors = MaterialTheme.colorScheme
    when (state) {
        JobStepState.Done -> Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(colors.secondaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_check),
                contentDescription = null,
                tint = colors.onSecondaryContainer,
                modifier = Modifier.size(16.dp),
            )
        }
        JobStepState.Current -> Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(colors.primary),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_radio_checked),
                contentDescription = null,
                tint = colors.onPrimary,
                modifier = Modifier.size(16.dp),
            )
        }
        JobStepState.Upcoming -> Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(colors.surfaceContainerHighest)
                .border(1.dp, colors.outline, CircleShape),
        )
    }
}
