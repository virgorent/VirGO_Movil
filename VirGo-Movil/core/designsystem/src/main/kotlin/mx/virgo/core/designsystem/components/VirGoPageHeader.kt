package mx.virgo.core.designsystem.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.virgo.core.designsystem.R
import mx.virgo.core.designsystem.theme.VirGoTheme
import mx.virgo.core.designsystem.theme.pageBackground

/**
 * Encabezado de página del prototipo (`.page-header`): regreso circular, *kicker*
 * violeta en mayúsculas, título y acciones. El botón mide 36 dp y su área táctil 48 dp.
 */
@Composable
fun VirGoPageHeader(
    title: String,
    modifier: Modifier = Modifier,
    kicker: String? = null,
    onBack: (() -> Unit)? = null,
    backContentDescription: String = stringResource(R.string.design_back),
    actions: @Composable RowScope.() -> Unit = {},
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(pageBackground())
                .heightIn(min = 64.dp)
                .padding(start = if (onBack != null) 6.dp else 16.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                Box(
                    modifier =
                        Modifier
                            .size(36.dp)
                            .shadow(2.dp, CircleShape)
                            .background(MaterialTheme.colorScheme.surface, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = backContentDescription,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            if (kicker != null) {
                Text(
                    text = kicker.uppercase(),
                    // Se ve en mayúsculas; TalkBack y las pruebas reciben el texto original.
                    modifier = Modifier.clearAndSetSemantics { text = AnnotatedString(kicker) },
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = KICKER_TRACKING,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                // Color explícito: sobre el fondo `soft` no hay contentColor del esquema que heredar.
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.semantics { heading() },
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically, content = actions)
    }
}

private val KICKER_TRACKING = 1.5.sp

@Preview(name = "Claro", widthDp = 393)
@Preview(name = "Oscuro", widthDp = 393, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun VirGoPageHeaderPreview() {
    VirGoTheme {
        Surface {
            Column {
                VirGoPageHeader(title = "Encuentra tu espacio", kicker = "VirGo · Beta")
                VirGoPageHeader(title = "Tu cuenta", kicker = "Cliente", onBack = {})
            }
        }
    }
}
