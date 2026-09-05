package com.askit.app.entry

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.askit.app.R
import com.askit.designsystem.R as DsR
import kotlin.math.cos
import kotlin.math.sin

/**
 * Represents a community professional orbiting the AskIT brand core.
 */
data class EntryProfessional(
    val id: String,
    val name: String,
    val profession: String,
    val avatarUrl: String,
    val angleDegrees: Double,
    val radiusDp: Dp,
    val sizeDp: Dp,
    val iconRes: Int,
    val badgeColor: Color,
)

/**
 * Harmonious, balanced 6-point orbital distribution of diverse service professionals.
 */
val defaultEntryProfessionals = listOf(
    EntryProfessional(
        id = "pro-1",
        name = "Priya Sharma",
        profession = "Electrician",
        avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300",
        angleDegrees = -90.0,
        radiusDp = 126.dp,
        sizeDp = 52.dp,
        iconRes = DsR.drawable.ic_bolt,
        badgeColor = Color(0xFF7CB342),
    ),
    EntryProfessional(
        id = "pro-2",
        name = "Karthik Raja",
        profession = "AC Specialist",
        avatarUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=300",
        angleDegrees = -30.0,
        radiusDp = 128.dp,
        sizeDp = 50.dp,
        iconRes = DsR.drawable.ic_shield,
        badgeColor = Color(0xFF00897B),
    ),
    EntryProfessional(
        id = "pro-3",
        name = "Marcus Sterling",
        profession = "Carpenter",
        avatarUrl = "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=300",
        angleDegrees = 30.0,
        radiusDp = 128.dp,
        sizeDp = 52.dp,
        iconRes = DsR.drawable.ic_work,
        badgeColor = Color(0xFFFB8C00),
    ),
    EntryProfessional(
        id = "pro-4",
        name = "Divya Nair",
        profession = "Painter & Designer",
        avatarUrl = "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=300",
        angleDegrees = 90.0,
        radiusDp = 124.dp,
        sizeDp = 50.dp,
        iconRes = DsR.drawable.ic_edit,
        badgeColor = Color(0xFF7CE605),
    ),
    EntryProfessional(
        id = "pro-5",
        name = "Ananya Roy",
        profession = "Cleaning Pro",
        avatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=300",
        angleDegrees = 150.0,
        radiusDp = 128.dp,
        sizeDp = 50.dp,
        iconRes = DsR.drawable.ic_sparkle,
        badgeColor = Color(0xFF7C4DFF),
    ),
    EntryProfessional(
        id = "pro-6",
        name = "Suresh Kumar",
        profession = "Plumber",
        avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=300",
        angleDegrees = -150.0,
        radiusDp = 128.dp,
        sizeDp = 52.dp,
        iconRes = DsR.drawable.ic_wrench,
        badgeColor = Color(0xFF1E88E5),
    ),
)

@Composable
fun EntryScreen(
    onGetStarted: () -> Unit,
    onLogin: () -> Unit,
    modifier: Modifier = Modifier,
    professionals: List<EntryProfessional> = defaultEntryProfessionals,
) {
    val scrollState = rememberScrollState()
    val isDark = isSystemInDarkTheme()

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("entry_screen"),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            // Upper / Middle Content Area (Optical Center Composition)
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(36.dp))

                // Centerpiece: Orbital Constellation Illustration with Soft Float Lift
                OrbitalConstellation(
                    professionals = professionals,
                    isDark = isDark,
                    modifier = Modifier.testTag("entry_orbital_constellation"),
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Value Proposition Headline
                Text(
                    text = stringResource(R.string.entry_headline),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 28.sp,
                        lineHeight = 34.sp,
                        letterSpacing = (-0.5).sp,
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("entry_headline"),
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Subtitle
                Text(
                    text = stringResource(R.string.entry_subtitle),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .testTag("entry_subtitle"),
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Social Proof & Trust Pill with Subtle Float
                Surface(
                    shape = RoundedCornerShape(100.dp),
                    color = if (isDark) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    ),
                    shadowElevation = if (isDark) 2.dp else 4.dp,
                    modifier = Modifier.testTag("entry_trust_badge"),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            painter = painterResource(DsR.drawable.ic_verified),
                            contentDescription = null,
                            tint = Color(0xFF7CE605),
                            modifier = Modifier.size(15.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.entry_trust_badge),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(36.dp))
            }

            // Locked Bottom Actions Section (Thumb-Friendly Reach Zone)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Primary CTA: Get started with theme-aware colors & silky soft lift shadow
                com.askit.designsystem.components.AskITPrimaryButton(
                    onClick = onGetStarted,
                    modifier = Modifier.testTag("entry_btn_get_started"),
                ) {
                    Text(
                        text = stringResource(R.string.entry_get_started),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                        ),
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Secondary Action: Login with 48dp+ Accessible Hitbox
                TextButton(
                    onClick = onLogin,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .testTag("entry_btn_login"),
                ) {
                    Text(
                        text = stringResource(R.string.entry_already_have_account),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Legal & Compliance Disclaimer
                Text(
                    text = stringResource(R.string.entry_legal_disclaimer),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                    ),
                    color = MaterialTheme.colorScheme.outline,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .testTag("entry_legal_disclaimer"),
                )

                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun OrbitalConstellation(
    professionals: List<EntryProfessional>,
    isDark: Boolean,
    modifier: Modifier = Modifier,
) {
    val auraColors = if (isDark) {
        listOf(
            Color(0xFF7CE605).copy(alpha = 0.16f),
            Color(0xFF1E2E0A).copy(alpha = 0.06f),
            Color.Transparent,
        )
    } else {
        listOf(
            Color(0xFFE2E8F0).copy(alpha = 0.70f),
            Color(0xFFF1F5F9).copy(alpha = 0.35f),
            Color.Transparent,
        )
    }

    val orbitRingColor = if (isDark) {
        Color(0xFF334155).copy(alpha = 0.5f)
    } else {
        Color(0xFFCBD5E1).copy(alpha = 0.7f)
    }

    Box(
        modifier = modifier.size(310.dp),
        contentAlignment = Alignment.Center,
    ) {
        // Ambient Diffuse Aura Glow
        Box(
            modifier = Modifier
                .size(290.dp)
                .background(
                    brush = Brush.radialGradient(colors = auraColors),
                    shape = CircleShape,
                ),
        )

        // Outer Orbit Ring
        Box(
            modifier = Modifier
                .size(240.dp)
                .border(
                    width = 1.dp,
                    color = orbitRingColor,
                    shape = CircleShape,
                ),
        )

        // Inner Orbit Ring
        Box(
            modifier = Modifier
                .size(165.dp)
                .border(
                    width = 1.dp,
                    color = orbitRingColor.copy(alpha = 0.5f),
                    shape = CircleShape,
                ),
        )

        // Central AskIT Brand Core Card with Silky Soft Floating Lift Shadow
        Box(
            modifier = Modifier
                .size(92.dp)
                .shadow(
                    elevation = if (isDark) 16.dp else 22.dp,
                    shape = RoundedCornerShape(26.dp),
                    ambientColor = if (isDark) Color(0xFF7CE605).copy(alpha = 0.25f) else Color(0xFF0F172A).copy(alpha = 0.16f),
                    spotColor = if (isDark) Color(0xFF7CE605).copy(alpha = 0.40f) else Color(0xFF0F172A).copy(alpha = 0.28f),
                )
                .clip(RoundedCornerShape(26.dp))
                .testTag("entry_center_logo_card"),
            contentAlignment = Alignment.Center,
        ) {
            androidx.compose.foundation.Image(
                painter = painterResource(R.drawable.ic_askit_launcher),
                contentDescription = "AskIT Brand Logo",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize(),
            )
        }

        // Orbiting Working Professionals with Silky Soft Lift
        professionals.forEach { pro ->
            val angleRad = Math.toRadians(pro.angleDegrees)
            val offsetX = (pro.radiusDp.value * cos(angleRad)).dp
            val offsetY = (pro.radiusDp.value * sin(angleRad)).dp

            ProfessionalAvatarChip(
                professional = pro,
                isDark = isDark,
                modifier = Modifier.offset(x = offsetX, y = offsetY),
            )
        }
    }
}

@Composable
private fun ProfessionalAvatarChip(
    professional: EntryProfessional,
    isDark: Boolean,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val fallbackPainter = painterResource(DsR.drawable.ic_person)
    val surfaceColor = MaterialTheme.colorScheme.surface
    val borderColor = if (isDark) Color(0xFF334155) else Color.White

    Box(
        modifier = modifier
            .size(professional.sizeDp)
            .shadow(
                elevation = if (isDark) 8.dp else 12.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = if (isDark) Color.Black.copy(alpha = 0.50f) else Color(0xFF0F172A).copy(alpha = 0.10f),
                spotColor = if (isDark) Color.Black.copy(alpha = 0.70f) else Color(0xFF0F172A).copy(alpha = 0.18f),
            )
            .background(surfaceColor, RoundedCornerShape(18.dp))
            .border(1.5.dp, borderColor, RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp)),
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(professional.avatarUrl)
                .crossfade(true)
                .build(),
            contentDescription = "${professional.name} - ${professional.profession}",
            contentScale = ContentScale.Crop,
            placeholder = fallbackPainter,
            error = fallbackPainter,
            fallback = fallbackPainter,
            modifier = Modifier.fillMaxSize(),
        )

        // Crisp category mini-badge anchored at bottom-end
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(18.dp)
                .background(professional.badgeColor, RoundedCornerShape(6.dp))
                .border(1.dp, borderColor, RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(professional.iconRes),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(10.dp),
            )
        }
    }
}


