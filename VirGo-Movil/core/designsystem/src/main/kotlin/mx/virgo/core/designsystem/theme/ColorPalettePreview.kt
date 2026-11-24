package mx.virgo.core.designsystem.theme

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview(name = "Paleta clara", widthDp = 393, heightDp = 852)
@Preview(
    name = "Paleta oscura",
    widthDp = 393,
    heightDp = 852,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
internal fun ColorPalettePreview() {
    VirGoTheme {
        Surface {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                val scheme = MaterialTheme.colorScheme
                PaletteRow("primary", scheme.primary)
                PaletteRow("onPrimary", scheme.onPrimary)
                PaletteRow("primaryContainer", scheme.primaryContainer)
                PaletteRow("onPrimaryContainer", scheme.onPrimaryContainer)
                PaletteRow("secondary", scheme.secondary)
                PaletteRow("surface", scheme.surface)
                PaletteRow("onSurface", scheme.onSurface)
                PaletteRow("surfaceVariant", scheme.surfaceVariant)
                PaletteRow("onSurfaceVariant", scheme.onSurfaceVariant)
                PaletteRow("outline", scheme.outline)
                PaletteRow("error", scheme.error)
                PaletteRow("errorContainer", scheme.errorContainer)
            }
        }
    }
}

@Composable
private fun PaletteRow(
    name: String,
    color: Color,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(44.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier =
                Modifier
                    .size(40.dp)
                    .background(color, RoundedCornerShape(8.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp)),
        )
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = 12.dp),
        )
    }
}
