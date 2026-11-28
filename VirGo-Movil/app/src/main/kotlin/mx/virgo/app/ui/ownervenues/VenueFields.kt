package mx.virgo.app.ui.ownervenues

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import mx.virgo.app.R
import mx.virgo.core.designsystem.components.VirGoTextField
import mx.virgo.core.domain.FieldProblem
import mx.virgo.core.domain.VenueDraftValidator
import mx.virgo.core.domain.VenueField

/**
 * Campo con nombre visible y persistente (022 RNF-02): el nombre se queda arriba aunque haya
 * texto, y el error se anuncia como error de ese campo.
 */
@Composable
internal fun LabeledField(
    @StringRes label: Int,
    value: String,
    problem: FieldProblem?,
    field: VenueField,
    @StringRes hint: Int?,
    singleLine: Boolean = true,
    keyboard: KeyboardOptions = KeyboardOptions.Default,
    onValueChange: (String) -> Unit,
) {
    val name = stringResource(label)
    val errorText = problem?.let { fieldErrorText(field, it) }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        FieldLabel(name)
        VirGoTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = singleLine,
            isError = errorText != null,
            keyboardOptions = keyboard,
            modifier =
                Modifier.semantics {
                    contentDescription = name
                    if (errorText != null) error(errorText)
                },
        )
        when {
            errorText != null -> FieldMessage(errorText, isError = true)
            hint != null -> FieldMessage(stringResource(hint), isError = false)
        }
    }
}

/** Selección con chips; con una sola opción posible actúa como grupo de radio. */
@Composable
internal fun ChoiceGroup(
    @StringRes label: Int,
    options: List<String>,
    selected: Set<String>,
    @StringRes error: Int?,
    labelOf: @Composable (String) -> String,
    onSelect: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        FieldLabel(stringResource(label))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { option ->
                FilterChip(
                    selected = option in selected,
                    onClick = { onSelect(option) },
                    label = { Text(labelOf(option)) },
                )
            }
        }
        error?.let { FieldMessage(stringResource(it), isError = true) }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(text = text, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
}

@Composable
private fun FieldMessage(
    text: String,
    isError: Boolean,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
internal fun fieldErrorText(
    field: VenueField,
    problem: FieldProblem,
): String =
    when {
        field == VenueField.SERVICES -> stringResource(R.string.venue_error_services)
        field in setOf(VenueField.CAPACITY, VenueField.MIN_HOURS) -> stringResource(R.string.venue_error_whole_number)
        field == VenueField.PRICE_BASE -> stringResource(R.string.venue_error_price)
        problem == FieldProblem.REQUIRED -> stringResource(R.string.venue_error_required)
        problem == FieldProblem.TOO_SHORT ->
            stringResource(
                R.string.venue_error_too_short,
                VenueDraftValidator.DESCRIPTION_MIN,
            )
        problem == FieldProblem.TOO_LONG -> stringResource(R.string.venue_error_too_long, maxLength(field))
        else -> stringResource(R.string.venue_error_invalid)
    }

private fun maxLength(field: VenueField): Int =
    when (field) {
        VenueField.SHORT_NAME -> VenueDraftValidator.SHORT_NAME_MAX
        VenueField.DESCRIPTION -> VenueDraftValidator.DESCRIPTION_MAX
        VenueField.ADDRESS -> VenueDraftValidator.ADDRESS_MAX
        else -> VenueDraftValidator.NAME_MAX
    }
