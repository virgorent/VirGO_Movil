package mx.virgo.app.ui.explore

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import mx.virgo.core.designsystem.theme.Slate200
import mx.virgo.core.designsystem.theme.Slate700
import mx.virgo.core.designsystem.theme.VirGoPurple

private val ChipShape = RoundedCornerShape(percent = 50)
private val ChipHPadding = 14.dp
private val ChipMinHeight = 48.dp
private val ChipBorder = 1.dp

/**
 * Chip seleccionable de la maqueta. El área táctil mide 48 dp (RNF-02 del spec `005`).
 */
@Composable
internal fun ExploreChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .defaultMinSize(minHeight = ChipMinHeight)
                .clip(ChipShape)
                .background(if (selected) VirGoPurple else Color.White)
                .border(
                    width = ChipBorder,
                    color = if (selected) VirGoPurple else Slate200,
                    shape = ChipShape,
                ).clickable(role = Role.Button, onClick = onClick)
                .padding(horizontal = ChipHPadding),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) Color.White else Slate700,
        )
    }
}
