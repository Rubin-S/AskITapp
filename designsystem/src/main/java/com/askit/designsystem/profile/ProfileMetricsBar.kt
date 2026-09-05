package com.askit.designsystem.profile

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class ProfileMetricItem(
    val id: String,
    val value: String,
    val label: String,
    @DrawableRes val iconRes: Int? = null,
    val iconTint: Color? = null,
    val onClick: (() -> Unit)? = null,
)

/**
 * Universal Profile Metrics Bar supporting dynamic stats across dual-identity models:
 * - Form A (Community Member): 3 metrics (Activity, Followers, Following)
 * - Form B (Service Provider): 4 metrics (★ Rating & count, Completed Jobs, Followers, Following)
 */
@Composable
fun ProfileMetricsBar(
    metrics: List<ProfileMetricItem>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("profile_metrics_bar"),
    ) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            metrics.forEachIndexed { index, item ->
                if (index > 0) {
                    VerticalDivider(
                        modifier = Modifier
                            .height(28.dp)
                            .padding(vertical = 2.dp),
                        color = MaterialTheme.colorScheme.outlineVariant,
                    )
                }
                val clickableModifier = if (item.onClick != null) {
                    Modifier.clickable(
                        role = Role.Button,
                        onClick = item.onClick,
                    )
                } else {
                    Modifier
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .then(clickableModifier)
                        .testTag("profile_metric_${item.id}"),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        if (item.iconRes != null) {
                            Icon(
                                painter = painterResource(item.iconRes),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(16.dp)
                                    .padding(end = 4.dp),
                                tint = item.iconTint ?: MaterialTheme.colorScheme.primary,
                            )
                        }
                        Text(
                            text = item.value,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}
