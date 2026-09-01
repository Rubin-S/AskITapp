package com.askit.app.story.editor

import androidx.compose.ui.text.font.FontFamily

internal val STORY_FONT_OPTIONS = listOf("Default", "Serif", "Monospace", "Sans")

internal fun storyFontFamily(name: String): FontFamily = when (name) {
    "Serif" -> FontFamily.Serif
    "Monospace" -> FontFamily.Monospace
    "Sans" -> FontFamily.SansSerif
    else -> FontFamily.Default
}
