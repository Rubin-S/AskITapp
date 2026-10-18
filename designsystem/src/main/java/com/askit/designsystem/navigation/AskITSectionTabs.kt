package com.askit.designsystem.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.askit.designsystem.R
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/** Stable IDs keep selection independent of translations, ordering and screen width. */
data class SectionTab(val id: String, val label: String)

/** A shared, text-first section rail. Labels fit naturally or scroll without being truncated. */
@Composable
fun AskITSectionTabs(
    tabs: List<SectionTab>,
    selectedId: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    tabTagPrefix: String = "section_tab_",
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
) {
    if (tabs.isEmpty()) return
    require(tabs.all { it.id.isNotBlank() && it.label.isNotBlank() } && tabs.map { it.id }.distinct().size == tabs.size)
    val activeId = selectedId.takeIf { id -> tabs.any { it.id == id } } ?: tabs.first().id
    val scroll = rememberScrollState()
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val direction = LocalLayoutDirection.current
    val measurer = rememberTextMeasurer()
    val textStyle = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
    val naturalWidths = tabs.map { tab ->
        with(density) { measurer.measure(AnnotatedString(tab.label), style = textStyle, softWrap = false, maxLines = 1).size.width.toDp() }
            .plus(24.dp).coerceAtLeast(64.dp)
    }
    BoxWithConstraints(modifier.fillMaxWidth().padding(contentPadding)) {
        val available = (maxWidth - 8.dp).coerceAtLeast(1.dp)
        val total = naturalWidths.fold(0.dp) { sum, width -> sum + width }
        val overflows = tabs.size > 1 && total > available
        val viewport = (available - if (overflows) 48.dp else 0.dp).coerceAtLeast(1.dp)
        val widths = when {
            overflows -> naturalWidths.map { it.coerceAtMost(viewport) }
            naturalWidths.all { it <= available / tabs.size } -> List(tabs.size) { available / tabs.size }
            else -> naturalWidths.map { it + (available - total) / tabs.size }
        }
        val selectedIndex = tabs.indexOfFirst { it.id == activeId }
        val selectedStart = widths.take(selectedIndex).fold(0.dp) { sum, width -> sum + width }
        val target = with(density) { (selectedStart + widths[selectedIndex] / 2 - viewport / 2).toPx().roundToInt() }
        LaunchedEffect(activeId, widths, viewport, scroll.maxValue) {
            if (scroll.maxValue != Int.MAX_VALUE) scroll.animateScrollTo(target.coerceIn(0, scroll.maxValue))
        }
        Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surfaceContainerLow,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = .5f))) {
            Row(Modifier.padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
                Row(Modifier.weight(1f).horizontalScroll(scroll).selectableGroup(), verticalAlignment = Alignment.CenterVertically) {
                    tabs.forEachIndexed { index, tab ->
                        val selected = tab.id == activeId
                        val container by animateColorAsState(
                            if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerLow,
                            label = "section_tab_container")
                        Surface(color = container, shape = RoundedCornerShape(13.dp),
                            modifier = Modifier.width(widths[index]).heightIn(min = 48.dp)) {
                            Box(Modifier.selectable(selected, onClick = { onSelect(tab.id) }, role = Role.Tab)
                                .testTag(tabTagPrefix + tab.id).padding(horizontal = 11.dp, vertical = 12.dp),
                                contentAlignment = Alignment.Center) {
                                Text(tab.label, style = textStyle, softWrap = true,
                                    color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
                if (overflows) {
                    val forward = scroll.canScrollForward
                    IconButton(onClick = {
                        scope.launch {
                            val distance = with(density) { viewport.toPx() } * .8f
                            val targetOffset = scroll.value + if (forward) distance else -distance
                            scroll.animateScrollTo(targetOffset.roundToInt().coerceIn(0, scroll.maxValue))
                        }
                    }, modifier = Modifier.size(48.dp).testTag(tabTagPrefix + "overflow")) {
                        Icon(painterResource(R.drawable.ic_chevron_right),
                            contentDescription = stringResource(if (forward) R.string.section_tabs_more else R.string.section_tabs_previous),
                            modifier = Modifier.size(20.dp).rotate(if (forward == (direction == LayoutDirection.Ltr)) 0f else 180f))
                    }
                }
            }
        }
    }
}
