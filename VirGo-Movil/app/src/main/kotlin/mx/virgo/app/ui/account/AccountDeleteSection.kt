package mx.virgo.app.ui.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import mx.virgo.app.R

/** `021` RF-01 y RF-06: acción destructiva separada de «Cerrar sesión» y su error. */
@Composable
internal fun DeleteAccountSection(
    state: AccountProfileUiState,
    actions: DeleteAccountActions,
) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        TextButton(
            onClick = actions.onRequest,
            enabled = !state.deleting,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(
                Icons.Outlined.DeleteForever,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(18.dp),
            )
            Text(
                stringResource(R.string.account_delete_action),
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
        if (state.deleteFailed) {
            Text(
                stringResource(R.string.account_delete_failed),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
            )
        }
    }
    if (state.confirmingDelete) {
        DeleteAccountDialog(deleting = state.deleting, actions = actions)
    }
}

/** `021` RF-02 y RF-03: «Cancelar» recibe el foco; mientras borra no se puede cerrar ni repetir. */
@Composable
private fun DeleteAccountDialog(
    deleting: Boolean,
    actions: DeleteAccountActions,
) {
    val cancelFocus = remember { FocusRequester() }
    AlertDialog(
        onDismissRequest = { if (!deleting) actions.onCancel() },
        properties = DialogProperties(dismissOnBackPress = !deleting, dismissOnClickOutside = !deleting),
        title = { Text(stringResource(R.string.account_delete_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.account_delete_body))
                if (deleting) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                        Text(stringResource(R.string.account_delete_progress))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = actions.onConfirm, enabled = !deleting) {
                Text(stringResource(R.string.account_delete_confirm), color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(
                onClick = actions.onCancel,
                enabled = !deleting,
                modifier = Modifier.focusRequester(cancelFocus),
            ) {
                Text(stringResource(R.string.account_delete_cancel))
            }
        },
    )
    LaunchedEffect(Unit) { runCatching { cancelFocus.requestFocus() } }
}
