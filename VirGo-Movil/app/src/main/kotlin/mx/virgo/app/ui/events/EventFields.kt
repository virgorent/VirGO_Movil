package mx.virgo.app.ui.events

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import mx.virgo.app.R
import mx.virgo.core.designsystem.components.VirGoOutlinedButton
import mx.virgo.core.designsystem.components.VirGoTextField

/** Campo con nombre persistente, error asociado y nota opcional (RNF-01/RNF-02). */
@Composable
internal fun LabeledInput(
    label: String,
    value: String,
    errorText: String?,
    hint: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    // «Siguiente» mueve el foco al próximo campo; el último del formulario usa «Listo».
    imeAction: ImeAction = if (singleLine) ImeAction.Next else ImeAction.Default,
    onValueChange: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        VirGoTextField(
            value = value,
            onValueChange = onValueChange,
            isError = errorText != null,
            singleLine = singleLine,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
            modifier =
                Modifier.semantics {
                    contentDescription = label
                    if (errorText != null) error(errorText)
                },
        )
        FieldNote(errorText, hint)
    }
}

/** Opciones excluyentes como chips, con su error (tipo de evento, estado del invitado). */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun <T> ChipChoice(
    label: String,
    options: List<T>,
    selected: T?,
    errorText: String?,
    optionLabel: @Composable (T) -> String,
    onSelect: (T) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.semantics { if (errorText != null) error(errorText) },
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { option ->
                FilterChip(
                    selected = option == selected,
                    onClick = { onSelect(option) },
                    label = { Text(optionLabel(option)) },
                )
            }
        }
        FieldNote(errorText, null)
    }
}

@Composable
internal fun FieldNote(
    errorText: String?,
    hint: String?,
) {
    val note = errorText ?: hint ?: return
    Text(
        note,
        style = MaterialTheme.typography.bodySmall,
        color = if (errorText != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

/** Botón secundario de ancho completo para volver o cancelar. */
@Composable
internal fun SecondaryAction(
    text: String,
    onClick: () -> Unit,
) {
    VirGoOutlinedButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) { Text(text) }
}

/** Confirmación antes de eliminar (016 RF-04, 017 RF-03). */
@Composable
internal fun ConfirmDelete(
    title: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        confirmButton = { TextButton(onClick = onConfirm) { Text(stringResource(R.string.guests_delete_confirm)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.guests_form_cancel)) } },
    )
}

internal fun eventErrorText(error: EventError): Int =
    when (error) {
        EventError.NETWORK -> R.string.event_error_network
        EventError.NOT_FOUND -> R.string.event_error_not_found
        EventError.REJECTED -> R.string.event_error_rejected
        EventError.SERVER -> R.string.event_error_server
    }
