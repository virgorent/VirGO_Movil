package mx.virgo.app.ui.bookings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import mx.virgo.app.R
import mx.virgo.core.designsystem.components.VirGoButton
import mx.virgo.core.designsystem.components.VirGoCard
import mx.virgo.core.designsystem.components.VirGoRow
import mx.virgo.core.designsystem.components.VirGoTextField
import mx.virgo.core.designsystem.theme.VirGoTone

/** Acciones del buscador por folio (`010` RF-01–RF-05). */
internal data class FolioSearchActions(
    val onChange: (String) -> Unit = {},
    val onSearch: () -> Unit = {},
)

/** C32: campo «Buscar por folio» arriba de la lista. */
@Composable
internal fun FolioSearch(
    state: BookingsUiState,
    actions: FolioSearchActions,
) {
    val label = stringResource(R.string.bookings_folio_label)
    val errorText = state.folioError?.let { stringResource(it.messageRes()) }
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, style = MaterialTheme.typography.titleSmall)
        VirGoTextField(
            value = state.folioQuery,
            onValueChange = actions.onChange,
            placeholder = stringResource(R.string.bookings_folio_hint),
            leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
            keyboardOptions =
                KeyboardOptions(capitalization = KeyboardCapitalization.Characters, imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { actions.onSearch() }),
            isError = errorText != null,
            modifier =
                Modifier.semantics {
                    contentDescription = label
                    if (errorText != null) error(errorText)
                },
        )
        if (errorText != null) {
            Text(
                errorText,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
            )
        }
        VirGoButton(
            onClick = actions.onSearch,
            enabled = !state.searching,
            text =
                stringResource(
                    if (state.searching) R.string.bookings_folio_searching else R.string.bookings_folio_search,
                ),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/** C33 (`010` RF-07): aviso informativo; no hay botones para cancelar, reprogramar ni reembolsar. */
@Composable
internal fun ChangesNotice() {
    VirGoCard {
        VirGoRow(
            title = stringResource(R.string.bookings_changes_title),
            subtitle = stringResource(R.string.bookings_changes_text),
            icon = Icons.Outlined.Info,
            tone = VirGoTone.VIOLET,
            standalone = false,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
    }
}

private fun FolioError.messageRes(): Int =
    when (this) {
        FolioError.FORMAT -> R.string.bookings_folio_format
        FolioError.NOT_FOUND -> R.string.bookings_folio_not_found
        FolioError.FAILED -> R.string.bookings_folio_failed
    }
