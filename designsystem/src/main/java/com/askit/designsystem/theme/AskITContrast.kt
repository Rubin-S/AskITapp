package com.askit.designsystem.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance

/**
 * Scrim behind white text and icons drawn over arbitrary photos and video (story overlays,
 * media badges). At 60% black, white text keeps at least 4.5:1 (WCAG AA) even over a pure white
 * frame; the previous 55% gave 4.1:1.
 */
val StoryScrimColor: Color = Color.Black.copy(alpha = 0.6f)

/** WCAG AA minimum contrast for normal-size text. */
const val WCAG_AA_TEXT_CONTRAST: Float = 4.5f

/** WCAG AA minimum contrast for large text and non-text UI (icons, focus rings, borders). */
const val WCAG_AA_NON_TEXT_CONTRAST: Float = 3f

/**
 * WCAG 2.x contrast ratio between [foreground] and [background]. A translucent foreground is
 * composited over the background first, which is how it is actually seen on screen.
 */
fun contrastRatio(foreground: Color, background: Color): Float {
    val opaqueBackground = background.copy(alpha = 1f)
    val fg = foreground.compositeOver(opaqueBackground).luminance()
    val bg = opaqueBackground.luminance()
    val lighter = maxOf(fg, bg)
    val darker = minOf(fg, bg)
    return (lighter + LUMINANCE_FLARE) / (darker + LUMINANCE_FLARE)
}

/** The 0.05 viewing-flare term in the WCAG contrast formula. */
private const val LUMINANCE_FLARE = 0.05f
