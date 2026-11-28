package mx.virgo.core.designsystem.components

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.virgo.core.designsystem.theme.VirGoTheme
import mx.virgo.core.designsystem.theme.VirGoTone
import mx.virgo.core.designsystem.theme.colors

private val RowShape = RoundedCornerShape(13.dp)
private val IconBubbleShape = RoundedCornerShape(10.dp)

/**
 * Fila del prototipo (`.ui-row`): burbuja de ícono, título, subtítulo (2 líneas),
 * contenido a la derecha y flecha si es tocable. Alto mínimo 48 dp.
 * `standalone = false` la usa dentro de una tarjeta, sin borde propio.
 */
@Composable
fun VirGoRow(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    tone: VirGoTone = VirGoTone.VIOLET,
    standalone: Boolean = true,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    /** Datos que no deben cortarse (nombres, fechas) usan `Int.MAX_VALUE`. */
    subtitleMaxLines: Int = 2,
    /** Sustituye la burbuja del ícono, p. ej. por una miniatura (`036` RF-03). */
    leading: (@Composable () -> Unit)? = null,
) {
    val container =
        if (standalone) {
            Modifier
                .clip(RowShape)
                .background(MaterialTheme.colorScheme.surface)
                .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), RowShape)
        } else {
            Modifier
        }
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .then(container)
                .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
                .heightIn(min = 56.dp)
                .padding(horizontal = if (standalone) 12.dp else 0.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        when {
            leading != null -> leading()
            icon != null -> VirGoIconBubble(icon, tone)
        }
        // Con letra grande el contenido de la derecha baja bajo el título para no aplastarlo.
        val stackTrailing = trailing != null && LocalDensity.current.fontScale >= STACK_TRAILING_SCALE
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = subtitleMaxLines,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (stackTrailing) trailing?.invoke()
        }
        when {
            trailing != null && !stackTrailing -> trailing()
            trailing != null -> Unit
            onClick != null ->
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
        }
    }
}

/** Burbuja de 32 dp con el ícono en el tono indicado (`.ui-row-icon`). Decorativa. */
@Composable
fun VirGoIconBubble(
    icon: ImageVector,
    tone: VirGoTone = VirGoTone.VIOLET,
    size: Int = ICON_BUBBLE_DP,
) {
    val colors = tone.colors()
    Box(
        modifier = Modifier.size(size.dp).background(colors.container, IconBubbleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colors.content,
            modifier = Modifier.size((size / 2).dp),
        )
    }
}

/** Separador entre filas dentro de una tarjeta. */
@Composable
fun VirGoRowDivider() {
    HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
}

private const val ICON_BUBBLE_DP = 32
private const val STACK_TRAILING_SCALE = 1.3f

@Preview(name = "Claro", widthDp = 393)
@Preview(name = "Oscuro", widthDp = 393, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun VirGoRowPreview() {
    VirGoTheme {
        Surface {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                VirGoRow(title = "Tu cuenta", subtitle = "Datos y sesión", icon = Icons.Outlined.Person, onClick = {})
                VirGoRow(
                    title = "Mensajes",
                    icon = Icons.Outlined.Person,
                    tone = VirGoTone.GRAY,
                    trailing = { VirGoBadge(text = "Próximamente", tone = VirGoTone.GRAY) },
                )
            }
        }
    }
}
