package com.askit.designsystem.theme

import androidx.compose.ui.graphics.Color

/**
 * Scrim behind white text and icons drawn over arbitrary photos and video (story overlays,
 * media badges). At 60% black, white text keeps at least 4.5:1 (WCAG AA) even over a pure white
 * frame; the previous 55% gave 4.1:1.
 */
val StoryScrimColor: Color = Color.Black.copy(alpha = 0.6f)
