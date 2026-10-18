package com.askit.app.auth

import android.text.Annotation
import android.text.Spanned
import androidx.annotation.StringRes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp

@Composable
internal fun LegalDisclaimer(
    @StringRes textRes: Int,
    onOpenTerms: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.outline,
    style: TextStyle = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, lineHeight = 15.sp),
) {
    val text = LocalResources.current.getText(textRes) as Spanned
    Text(
        text = buildAnnotatedString {
            append(text.toString())
            for (annotation in text.getSpans(0, text.length, Annotation::class.java).filter { it.key == "link" }) {
                addLink(
                    LinkAnnotation.Clickable(
                        annotation.value,
                        TextLinkStyles(SpanStyle(textDecoration = TextDecoration.Underline)),
                    ) {
                        if (annotation.value == "terms") onOpenTerms() else onOpenPrivacyPolicy()
                    },
                    text.getSpanStart(annotation),
                    text.getSpanEnd(annotation),
                )
            }
        },
        style = style,
        color = color,
        textAlign = TextAlign.Center,
        modifier = modifier,
    )
}
