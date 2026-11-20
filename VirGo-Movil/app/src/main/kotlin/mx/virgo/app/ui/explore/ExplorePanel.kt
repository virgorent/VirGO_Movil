package mx.virgo.app.ui.explore

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import mx.virgo.app.R
import mx.virgo.core.designsystem.components.VirGoCard
import mx.virgo.core.designsystem.theme.Slate500
import mx.virgo.core.designsystem.theme.Slate900

private const val PANEL_MAX_HEIGHT = 0.9f

private val ScrimColor = Color.Black.copy(alpha = 0.45f)
private val PanelHPadding = 16.dp
private val PanelVPadding = 12.dp
private val CardPadding = 16.dp
private val ContentGap = 14.dp

/**
 * Hoja inferior con velo para las pantallas C10 (filtros) y C11 (recomendados):
 * título con botón de cierre y contenido desplazable sobre el borde inferior.
 */
@Composable
internal fun ExplorePanel(
    title: String,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(modifier = modifier.fillMaxSize()) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(ScrimColor)
                    .clickable(onClick = onClose),
        )
        VirGoCard(
            containerColor = Color.White,
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .fillMaxHeight(PANEL_MAX_HEIGHT)
                    .imePadding()
                    .navigationBarsPadding()
                    .padding(
                        horizontal = PanelHPadding,
                        vertical = PanelVPadding,
                    ),
        ) {
            Column(
                modifier =
                    Modifier
                        .padding(CardPadding)
                        .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(ContentGap),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = title,
                        style =
                            MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                            ),
                        color = Slate900,
                    )
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = stringResource(R.string.action_close),
                            tint = Slate500,
                        )
                    }
                }
                content()
            }
        }
    }
}
