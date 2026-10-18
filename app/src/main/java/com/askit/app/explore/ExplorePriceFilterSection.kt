package com.askit.app.explore

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import com.askit.designsystem.inputs.AskITTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.askit.app.R

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ExplorePriceFilterSection(
    scope: ExploreResultScope,
    filter: ExplorePriceFilter,
    onChange: (ExplorePriceFilter) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                stringResource(R.string.explore_price_heading),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.semantics { heading() },
            )
            Text(
                stringResource(if (scope == ExploreResultScope.Tasks) R.string.explore_price_task_hint else R.string.explore_price_hint),
                style = MaterialTheme.typography.bodyMedium,
            )
            AskITTextField(
                value = filter.minimum,
                onValueChange = { onChange(filter.copy(minimum = it)) },
                label = { Text(stringResource(R.string.explore_price_minimum)) },
                prefix = { Text("₹") },
                singleLine = true,
                isError = !filter.isValid,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth().testTag("explore_price_minimum"),
            )
            AskITTextField(
                value = filter.maximum,
                onValueChange = { onChange(filter.copy(maximum = it)) },
                label = { Text(stringResource(R.string.explore_price_maximum)) },
                prefix = { Text("₹") },
                singleLine = true,
                isError = !filter.isValid,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth().testTag("explore_price_maximum"),
            )
            if (!filter.isValid) {
                Text(
                    stringResource(R.string.explore_price_error),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            if (scope != ExploreResultScope.Tasks) {
                Text(stringResource(R.string.explore_price_unit), style = MaterialTheme.typography.titleSmall)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = filter.unit == null,
                        onClick = { onChange(filter.copy(unit = null)) },
                        label = { Text(stringResource(R.string.explore_price_any_unit)) },
                    )
                    ExploreRateUnit.entries.forEach { unit ->
                        FilterChip(
                            selected = filter.unit == unit,
                            onClick = { onChange(filter.copy(unit = if (filter.unit == unit) null else unit)) },
                            label = { Text(stringResource(unit.labelRes)) },
                        )
                    }
                }
            }
        }
    }
}
