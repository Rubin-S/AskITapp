package com.askit.app.createpost.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.askit.designsystem.R as DesignR

@Composable
fun CreatePostAccessoryBar(
    bodyLength: Int,
    maxBodyLength: Int = 1000,
    hasPhotos: Boolean,
    isBeforeAfter: Boolean,
    hasPoll: Boolean,
    hasLocation: Boolean,
    onAddPhotos: () -> Unit,
    onToggleBeforeAfter: () -> Unit,
    onTogglePoll: () -> Unit,
    onAddLocation: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding(),
    ) {
        Column {
            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        onClick = onAddPhotos,
                        modifier = Modifier.size(40.dp),
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = if (hasPhotos) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        ),
                    ) {
                        Icon(
                            painter = painterResource(DesignR.drawable.ic_photo),
                            contentDescription = "Add photos",
                            modifier = Modifier.size(20.dp),
                        )
                    }

                    IconButton(
                        onClick = onToggleBeforeAfter,
                        modifier = Modifier.size(40.dp),
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = if (isBeforeAfter) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        ),
                    ) {
                        Icon(
                            painter = painterResource(DesignR.drawable.ic_share_work),
                            contentDescription = "Toggle Before/After",
                            modifier = Modifier.size(20.dp),
                        )
                    }

                    IconButton(
                        onClick = onTogglePoll,
                        modifier = Modifier.size(40.dp),
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = if (hasPoll) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        ),
                    ) {
                        Icon(
                            painter = painterResource(DesignR.drawable.ic_service),
                            contentDescription = "Add poll",
                            modifier = Modifier.size(20.dp),
                        )
                    }

                    IconButton(
                        onClick = onAddLocation,
                        modifier = Modifier.size(40.dp),
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = if (hasLocation) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        ),
                    ) {
                        Icon(
                            painter = painterResource(DesignR.drawable.ic_location),
                            contentDescription = "Add location",
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }

                Text(
                    text = "$bodyLength / $maxBodyLength",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.padding(end = 4.dp),
                )
            }
        }
    }
}
