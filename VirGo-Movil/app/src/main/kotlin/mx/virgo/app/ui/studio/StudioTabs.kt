package mx.virgo.app.ui.studio

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.TableRestaurant
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import mx.virgo.app.R
import mx.virgo.app.ui.venuedetail.formatMxn
import mx.virgo.core.designsystem.components.ErrorBanner
import mx.virgo.core.designsystem.components.LoadingState
import mx.virgo.core.designsystem.components.VirGoCard
import mx.virgo.core.designsystem.components.VirGoRow
import mx.virgo.core.designsystem.components.VirGoRowDivider
import mx.virgo.core.designsystem.theme.VirGoTone
import mx.virgo.core.domain.StaffRole
import mx.virgo.core.domain.TableLayout

/** E43 (`012` RF-02 y RF-03): mesas por distribución y aviso si no caben. */
@Composable
internal fun TablesTab(
    state: StudioUiState,
    guests: Int,
    onLayout: (TableLayout) -> Unit,
) {
    VirGoCard {
        Column(Modifier.padding(horizontal = 16.dp)) {
            TableLayout.entries.forEachIndexed { index, layout ->
                if (index > 0) VirGoRowDivider()
                VirGoRow(
                    title = stringResource(layout.label()),
                    subtitle = stringResource(R.string.studio_tables_value, layout.tables(guests), layout.perTable),
                    icon = Icons.Outlined.TableRestaurant,
                    tone = if (layout == state.layout) VirGoTone.GREEN else VirGoTone.GRAY,
                    standalone = false,
                    onClick = { onLayout(layout) },
                    subtitleMaxLines = Int.MAX_VALUE,
                )
            }
        }
    }
    val capacity = state.venueCapacity?.let(state.layout::capacity)
    when {
        capacity == null ->
            Text(
                stringResource(R.string.studio_no_capacity),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        guests > capacity ->
            VirGoCard {
                VirGoRow(
                    stringResource(R.string.studio_does_not_fit, capacity),
                    icon = Icons.Outlined.WarningAmber,
                    tone = VirGoTone.AMBER,
                    standalone = false,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
        else -> Text(stringResource(R.string.studio_fits, capacity), color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** C16 (`012` RF-04 y RF-05): personal sugerido con su regla y ajuste a mano. */
@Composable
internal fun StaffTab(
    state: StudioUiState,
    actions: StudioActions,
) {
    AiLabel(fromAi = false)
    VirGoCard {
        Column(Modifier.padding(horizontal = 16.dp)) {
            StaffRole.entries.forEachIndexed { index, role ->
                if (index > 0) VirGoRowDivider()
                StaffRow(role, state.staff[role] ?: 0, actions.onStaff)
            }
        }
    }
    if (state.staffEdited) {
        TextButton(onClick = actions.onResetStaff) { Text(stringResource(R.string.studio_staff_reset)) }
    }
    Text(stringResource(R.string.studio_staff_hint), style = MaterialTheme.typography.bodySmall)
}

@Composable
private fun StaffRow(
    role: StaffRole,
    count: Int,
    onStaff: (StaffRole, Int) -> Unit,
) {
    val name = stringResource(role.label())
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(name, fontWeight = FontWeight.SemiBold)
            Text(
                stringResource(R.string.studio_staff_rule, role.per),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        IconButton(onClick = { onStaff(role, -1) }, enabled = count > 0) {
            Icon(Icons.Outlined.Remove, stringResource(R.string.studio_staff_less, name))
        }
        Text(count.toString(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        IconButton(onClick = { onStaff(role, 1) }) {
            Icon(Icons.Outlined.Add, stringResource(R.string.studio_staff_more, name))
        }
    }
}

/** C17 (`012` RF-06–RF-08): ideas de la IA del servidor y su efecto en el presupuesto. */
@Composable
internal fun IdeasTab(
    state: StudioUiState,
    onIdeas: () -> Unit,
) {
    when (state.ideasStatus) {
        IdeasStatus.IDLE, IdeasStatus.LOADING -> LoadingState(message = stringResource(R.string.studio_ideas_loading))
        IdeasStatus.UNAVAILABLE -> Notice(stringResource(R.string.studio_ideas_unavailable))
        IdeasStatus.BUSY ->
            ErrorBanner(
                stringResource(R.string.ai_busy),
                actionLabel = stringResource(R.string.booking_retry),
                onAction = onIdeas,
            )
        IdeasStatus.FAILED ->
            ErrorBanner(
                stringResource(R.string.studio_ideas_error),
                actionLabel = stringResource(R.string.booking_retry),
                onAction = onIdeas,
            )
        IdeasStatus.READY -> IdeasList(state)
    }
}

@Composable
private fun IdeasList(state: StudioUiState) {
    AiLabel()
    if (state.ideas.isEmpty()) {
        Notice(stringResource(R.string.studio_ideas_empty))
        return
    }
    VirGoCard {
        Column(Modifier.padding(horizontal = 16.dp)) {
            state.ideas.forEachIndexed { index, idea ->
                if (index > 0) VirGoRowDivider()
                VirGoRow(
                    title = idea.title,
                    subtitle = listOfNotNull(idea.description, formatMxn(idea.cost.toDouble())).joinToString(" · "),
                    standalone = false,
                    subtitleMaxLines = Int.MAX_VALUE,
                )
            }
        }
    }
    val limit = state.decorationBudget ?: return
    val over = state.ideasTotal > limit
    Text(
        stringResource(
            if (over) R.string.studio_ideas_over else R.string.studio_ideas_within,
            formatMxn(state.ideasTotal.toDouble()),
            formatMxn(limit.toDouble()),
        ),
        color = if (over) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
    )
}

@Composable
private fun Notice(text: String) {
    Text(
        text,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier =
            Modifier.semantics {
                liveRegion =
                    LiveRegionMode.Polite
            },
    )
}

private fun TableLayout.label(): Int =
    when (this) {
        TableLayout.CENTER_DANCE_FLOOR -> R.string.studio_layout_center
        TableLayout.MAX_CAPACITY -> R.string.studio_layout_max
        TableLayout.IMPERIAL -> R.string.studio_layout_imperial
    }

private fun StaffRole.label(): Int =
    when (this) {
        StaffRole.WAITERS -> R.string.studio_staff_waiters
        StaffRole.SECURITY -> R.string.studio_staff_security
        StaffRole.CLEANING -> R.string.studio_staff_cleaning
    }
