package mx.virgo.app.ui.studio

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import mx.virgo.app.R
import mx.virgo.core.designsystem.components.LoadingState
import mx.virgo.core.designsystem.components.VirGoBadge
import mx.virgo.core.designsystem.components.VirGoOutlinedButton
import mx.virgo.core.designsystem.components.VirGoPageBody
import mx.virgo.core.designsystem.components.VirGoTextField
import mx.virgo.core.designsystem.theme.VirGoTone

@Composable
fun ClientStudio(
    onAssistant: () -> Unit,
    viewModel: StudioViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    StudioContent(
        state,
        StudioActions(
            onTab = viewModel::selectTab,
            onGuests = viewModel::changeGuests,
            onLayout = viewModel::selectLayout,
            onStaff = viewModel::adjustStaff,
            onResetStaff = viewModel::resetStaff,
            onIdeas = viewModel::loadIdeas,
            onAssistant = onAssistant,
        ),
    )
}

@Composable
internal fun StudioContent(
    state: StudioUiState,
    actions: StudioActions,
) {
    VirGoPageBody {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(R.string.studio_intro),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
        }
        VirGoOutlinedButton(onClick = actions.onAssistant, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.studio_open_assistant))
        }
        if (state.loading) {
            LoadingState(message = stringResource(R.string.studio_loading))
            return@VirGoPageBody
        }
        GuestsField(state, actions.onGuests)
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StudioTab.entries.forEach { tab ->
                FilterChip(
                    selected = state.tab == tab,
                    onClick = { actions.onTab(tab) },
                    label = { Text(stringResource(tab.label())) },
                )
            }
        }
        val guests = state.guests
        when {
            guests == null ->
                Text(
                    stringResource(R.string.studio_guests_invalid),
                    color = MaterialTheme.colorScheme.error,
                )
            state.tab == StudioTab.TABLES -> TablesTab(state, guests, actions.onLayout)
            state.tab == StudioTab.STAFF -> StaffTab(state, actions)
            else -> IdeasTab(state, actions.onIdeas)
        }
    }
}

@Composable
private fun GuestsField(
    state: StudioUiState,
    onGuests: (String) -> Unit,
) {
    val label = stringResource(R.string.studio_guests)
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.titleSmall)
        VirGoTextField(
            value = state.guestsInput,
            onValueChange = onGuests,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = state.guests == null,
            modifier = Modifier.semantics { contentDescription = label },
        )
        state.event?.let {
            Text(
                stringResource(R.string.studio_from_event, it.name),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * Etiqueta de sugerencia (`012` RF-09 v2.1): «Sugerencia de IA» solo en lo que responde la IA;
 * los cálculos con reglas fijas (personal, presupuesto sugerido) dicen «Sugerencia».
 */
@Composable
internal fun AiLabel(
    modifier: Modifier = Modifier,
    fromAi: Boolean = true,
) {
    Row(modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
        if (fromAi) {
            androidx.compose.material3.Icon(
                Icons.Outlined.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        }
        VirGoBadge(
            text = stringResource(if (fromAi) R.string.studio_ai_label else R.string.studio_rule_label),
            tone = VirGoTone.VIOLET,
        )
    }
}

private fun StudioTab.label(): Int =
    when (this) {
        StudioTab.TABLES -> R.string.studio_tab_tables
        StudioTab.STAFF -> R.string.studio_tab_staff
        StudioTab.IDEAS -> R.string.studio_tab_ideas
    }
