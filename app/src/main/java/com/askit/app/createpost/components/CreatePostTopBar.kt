package com.askit.app.createpost.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.askit.app.R
import com.askit.app.createpost.CreatePostScreenMode
import com.askit.designsystem.R as DesignR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostTopBar(
    screenMode: CreatePostScreenMode,
    isPostValid: Boolean,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onPreview: () -> Unit,
    onPost: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        modifier = modifier,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
        ),
        title = {
            Text(
                text = stringResource(
                    if (screenMode == CreatePostScreenMode.PREVIEW) {
                        R.string.create_post_preview_title
                    } else {
                        R.string.create_post_screen_title
                    },
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    letterSpacing = (-0.2).sp,
                ),
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        navigationIcon = {
            IconButton(
                onClick = if (screenMode == CreatePostScreenMode.PREVIEW) onEdit else onBack,
                modifier = Modifier.size(48.dp),
            ) {
                Icon(
                    painter = painterResource(
                        if (screenMode == CreatePostScreenMode.PREVIEW) {
                            R.drawable.ic_arrow_back
                        } else {
                            DesignR.drawable.ic_close
                        },
                    ),
                    contentDescription = stringResource(
                        if (screenMode == CreatePostScreenMode.PREVIEW) {
                            R.string.create_post_edit
                        } else {
                            R.string.explore_back
                        },
                    ),
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        },
        actions = {
            if (screenMode == CreatePostScreenMode.EDITING) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilledTonalButton(
                        onClick = onPreview,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .heightIn(min = 36.dp)
                            .testTag("create_post_preview_top"),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                        ),
                    ) {
                        Icon(
                            painter = painterResource(DesignR.drawable.ic_visibility),
                            contentDescription = null,
                            modifier = Modifier.size(15.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.create_post_preview),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                            ),
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Button(
                        onClick = onPost,
                        enabled = isPostValid,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.heightIn(min = 36.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f),
                            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                        ),
                    ) {
                        Text(
                            text = "Post",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                            ),
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))
                }
            }
        },
    )
}
