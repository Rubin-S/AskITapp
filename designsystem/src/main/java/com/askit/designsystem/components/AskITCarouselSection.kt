package com.askit.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A titled, horizontally scrolling row of fixed-width cards (the Home feed's services and tasks
 * rows). The title is a heading. [itemContent] receives the modifier that sizes each card.
 */
@Composable
fun <T> AskITCarouselSection(
    title: String,
    items: List<T>,
    key: (T) -> Any,
    modifier: Modifier = Modifier,
    itemWidth: Dp = 280.dp,
    itemContent: @Composable (item: T, itemModifier: Modifier) -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.2).sp,
            ),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .semantics { heading() },
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            items(items = items, key = key) { item ->
                itemContent(item, Modifier.width(itemWidth))
            }
        }
    }
}
