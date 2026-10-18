package com.askit.app.entry

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.askit.app.R
import com.askit.designsystem.R as DsR
import kotlin.math.cos
import kotlin.math.sin

private enum class WelcomeService(
    @DrawableRes val portrait: Int,
    val angle: Double,
    @DrawableRes val icon: Int? = null,
    val badgeX: Float = 0.85f,
    val badgeY: Float = 0.83f,
) {
    Electrical(R.drawable.welcome_portrait_1, -90.0, DsR.drawable.ic_bolt),
    Home(R.drawable.welcome_portrait_2, -30.0, DsR.drawable.ic_home_filled, badgeX = 0.866f, badgeY = 0.841f),
    Painting(R.drawable.welcome_portrait_3, 30.0, badgeX = 0.8125f, badgeY = 0.805f),
    Gardening(R.drawable.welcome_portrait_4, 90.0, badgeX = 0.8765f, badgeY = 0.8086f),
    Cleaning(R.drawable.welcome_portrait_5, 150.0, DsR.drawable.ic_sparkle, badgeY = 0.78f),
    Repairs(R.drawable.welcome_portrait_6, 210.0, DsR.drawable.ic_wrench, badgeX = 0.86875f, badgeY = 0.8125f),
}

/** Normalized geometry keeps the illustration intact at every available size. */
@Composable
internal fun WelcomeArtwork(colors: WelcomeColors, modifier: Modifier = Modifier) {
    // Compose's animation clock respects the system animator duration scale, including zero.
    val transition = rememberInfiniteTransition(label = "welcome ambience")
    val breath by transition.animateFloat(
        initialValue = 0.65f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(4200), RepeatMode.Reverse), label = "logo glow",
    )
    val orbit by transition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(90000, easing = LinearEasing)), label = "orbit lights",
    )
    BoxWithConstraints(modifier, contentAlignment = Alignment.Center) {
        val edge = maxWidth
        Canvas(Modifier.fillMaxSize()) {
            val unit = size.minDimension
            drawCircle(
                Brush.radialGradient(listOf(WelcomeLime.copy(alpha = colors.glowAlpha * breath), Color.Transparent), radius = unit * 0.32f),
                radius = unit * 0.32f,
            )
            drawCircle(colors.ring, radius = unit * 0.30f, style = Stroke(width = 1.dp.toPx()))
            drawCircle(colors.innerRing, radius = unit * 0.215f, style = Stroke(width = 1.dp.toPx()))
            drawCircle(colors.ring, radius = unit * 0.42f, style = Stroke(width = 0.8.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(2.dp.toPx(), 3.dp.toPx()))))
            repeat(8) { index ->
                val radians = Math.toRadians(index * 45.0 + orbit)
                drawCircle(
                    color = WelcomeLime,
                    radius = unit * 0.007f,
                    center = center + androidx.compose.ui.geometry.Offset((cos(radians) * unit * 0.42f).toFloat(), (sin(radians) * unit * 0.42f).toFloat()),
                )
            }
        }
        Box(
            Modifier.size(edge * 0.22f).clip(RoundedCornerShape(28))
                .background(WelcomeLime).testTag("entry_center_logo_card"),
            contentAlignment = Alignment.Center,
        ) {
            Icon(painterResource(R.drawable.ic_welcome_brand), stringResource(R.string.auth_logo), tint = Color(0xFF131811), modifier = Modifier.fillMaxSize())
        }
        WelcomeService.entries.forEach { service ->
            val radians = Math.toRadians(service.angle)
            val portraitSize = edge * 0.23f
            Box(
                Modifier.offset(x = edge * (cos(radians) * 0.385f).toFloat(), y = edge * (sin(radians) * 0.385f).toFloat())
                    .size(portraitSize)
                    .graphicsLayer { translationY = (breath - 0.825f) * edge.toPx() * 0.018f },
            ) {
                Image(
                    painterResource(service.portrait), contentDescription = null,
                    modifier = Modifier.fillMaxSize().clip(CircleShape).border(1.2.dp, colors.portraitBorder, CircleShape),
                    contentScale = ContentScale.Crop,
                )
                Box(
                    Modifier.align(Alignment.BottomEnd).offset(portraitSize * (service.badgeX - 0.77f), portraitSize * (service.badgeY - 0.77f))
                        .size(portraitSize * 0.46f).background(colors.badge, CircleShape)
                        .border(1.2.dp, colors.portraitBorder, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    val iconModifier = Modifier.size(portraitSize * 0.25f)
                    when (service) {
                        WelcomeService.Gardening -> Icon(Icons.Filled.Eco, null, iconModifier, tint = colors.badgeIcon)
                        WelcomeService.Painting -> Icon(Icons.Filled.FormatPaint, null, iconModifier, tint = colors.badgeIcon)
                        else -> Icon(painterResource(checkNotNull(service.icon)), null, iconModifier, tint = colors.badgeIcon)
                    }
                }
            }
        }
    }
}
