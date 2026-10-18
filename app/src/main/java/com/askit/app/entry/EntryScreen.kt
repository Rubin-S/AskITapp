package com.askit.app.entry

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.askit.app.R
import com.askit.app.auth.LegalDisclaimer

/** Insets are consumed by the app shell; content remains scrollable at large font scales. */
@Composable
fun EntryScreen(
    onGetStarted: () -> Unit,
    onLogin: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenTerms: () -> Unit = {},
    onOpenPrivacyPolicy: () -> Unit = {},
) {
    val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val colors = welcomeColors(dark)
    val tamil = LocalConfiguration.current.locales[0].language == "ta"
    Surface(modifier.fillMaxSize().testTag("entry_screen"), color = colors.background) {
        BoxWithConstraints(contentAlignment = Alignment.TopCenter) {
            val contentWidth = maxWidth.coerceAtMost(520.dp)
            val shortViewport = maxHeight < 700.dp
            // Preserve the actions on short phones; larger accessibility text can still scroll.
            val artworkHeight = (maxHeight * if (shortViewport) 0.34f else 0.49f).coerceAtLeast(160.dp)
            val artworkSize = minOf((contentWidth - 48.dp) * 0.92f, artworkHeight)
            val compact = contentWidth <= 360.dp
            Column(
                Modifier.widthIn(max = 520.dp).fillMaxWidth().verticalScroll(rememberScrollState())
                    .heightIn(min = maxHeight).padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(Modifier.height(8.dp))
                    WelcomeArtwork(colors, Modifier.size(artworkSize).testTag("entry_orbital_constellation"))
                    Spacer(Modifier.height(if (shortViewport) 20.dp else 28.dp))
                    Text(
                        stringResource(R.string.entry_headline),
                        Modifier.fillMaxWidth().testTag("entry_headline"),
                        color = colors.headline,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize = when {
                                tamil -> 24.sp
                                compact -> 28.sp
                                else -> 32.sp
                            },
                            lineHeight = when {
                                tamil -> 34.sp
                                compact -> 36.sp
                                else -> 40.sp
                            },
                            fontWeight = if (tamil) FontWeight.SemiBold else FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif,
                            letterSpacing = if (tamil) 0.sp else (-1).sp,
                        ),
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        stringResource(R.string.entry_subtitle),
                        Modifier.fillMaxWidth().padding(horizontal = 12.dp).testTag("entry_subtitle"),
                        color = colors.secondary,
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp, lineHeight = 23.sp),
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(if (shortViewport) 24.dp else 32.dp))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Button(
                        onClick = onGetStarted,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).testTag("entry_btn_get_started"),
                        shape = RoundedCornerShape(100),
                        colors = ButtonDefaults.buttonColors(containerColor = WelcomeLime, contentColor = Color(0xFF10140B)),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                    ) {
                        Text(
                            stringResource(R.string.entry_get_started),
                            style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                            textAlign = TextAlign.Center,
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    TextButton(
                        onClick = onLogin,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("entry_btn_login"),
                    ) {
                        Text(
                            stringResource(R.string.entry_already_have_account),
                            color = colors.secondary,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    LegalDisclaimer(
                        textRes = R.string.entry_legal_disclaimer,
                        onOpenTerms = onOpenTerms,
                        onOpenPrivacyPolicy = onOpenPrivacyPolicy,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).testTag("entry_legal_disclaimer"),
                        color = colors.legal,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 16.sp),
                    )
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}

internal val WelcomeLime = Color(0xFF8DFF08)

internal data class WelcomeColors(
    val background: Color,
    val headline: Color,
    val secondary: Color,
    val legal: Color,
    val ring: Color,
    val innerRing: Color,
    val portraitBorder: Color,
    val badge: Color,
    val badgeIcon: Color,
    val glowAlpha: Float,
)

internal fun welcomeColors(dark: Boolean) = if (dark) {
    WelcomeColors(
        background = Color.Black, headline = Color(0xFFFAFAFA), secondary = Color(0xFFB4B8BF),
        legal = Color(0xFF969A9F), ring = Color(0xFF29343B), innerRing = Color(0xFF42632D),
        portraitBorder = Color(0xFF465960), badge = Color(0xFF050A08), badgeIcon = WelcomeLime, glowAlpha = 0.16f,
    )
} else {
    WelcomeColors(
        background = Color.White, headline = Color(0xFF111512), secondary = Color(0xFF535C57),
        legal = Color(0xFF616A65), ring = Color(0xFFCAD4CD), innerRing = Color(0xFFA5C889),
        portraitBorder = Color(0xFFB9C9C0), badge = Color.White, badgeIcon = Color(0xFF417800), glowAlpha = 0.13f,
    )
}
