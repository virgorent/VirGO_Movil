package mx.virgo.core.designsystem.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.virgo.core.designsystem.R
import mx.virgo.core.designsystem.theme.VirGoTheme
import mx.virgo.core.designsystem.theme.VirGoTone
import mx.virgo.core.designsystem.theme.colors

@Composable
fun VirGoBadge(
    text: String,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    leadingIcon: (@Composable () -> Unit)? = null,
) {
    BadgeContainer(text, modifier, containerColor, contentColor, leadingIcon)
}

/** Insignia de estado con tono del prototipo (`.ui-badge`); siempre con texto, no solo color. */
@Composable
fun VirGoBadge(
    text: String,
    tone: VirGoTone,
    modifier: Modifier = Modifier,
) {
    val colors = tone.colors()
    BadgeContainer(text, modifier, colors.container, colors.content, leadingIcon = null)
}

@Composable
private fun BadgeContainer(
    text: String,
    modifier: Modifier,
    containerColor: Color,
    contentColor: Color,
    leadingIcon: (@Composable () -> Unit)?,
) {
    Box(
        modifier =
            modifier
                .background(
                    color = containerColor,
                    shape = RoundedCornerShape(7.dp),
                ).padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            leadingIcon?.invoke()
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.ExtraBold,
                color = contentColor,
            )
        }
    }
}

@Preview(name = "Claro", widthDp = 393)
@Preview(name = "Oscuro", widthDp = 393, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun VirGoBadgePreview() {
    VirGoTheme {
        Surface {
            Column(modifier = Modifier.padding(16.dp)) {
                VirGoBadge(
                    text = stringResource(R.string.design_preview_badge),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(12.dp),
                        )
                    },
                )
            }
        }
    }
}
