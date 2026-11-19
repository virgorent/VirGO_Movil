package mx.virgo.app.ui.explore

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import mx.virgo.app.R
import mx.virgo.core.designsystem.components.VirGoButton
import mx.virgo.core.designsystem.components.VirGoOutlinedButton
import mx.virgo.core.designsystem.components.VirGoTextField
import mx.virgo.core.designsystem.theme.ErrorRed
import mx.virgo.core.designsystem.theme.Slate400
import mx.virgo.core.designsystem.theme.Slate700
import mx.virgo.core.designsystem.theme.VirGoPurple

private val SmallGap = 8.dp
private val FieldGap = 14.dp
private val RowGap = 12.dp

/** C10: filtros por zona, invitados, presupuesto, distancia, fecha y orden (RF-12 a RF-20). */
@Composable
internal fun ExploreFiltersPanel(
    state: ExploreUiState,
    onFiltersChange: (ExploreFilters.() -> ExploreFilters) -> Unit,
    onApply: () -> Unit,
    onClear: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ExplorePanel(
        title = stringResource(R.string.explore_filters_title),
        onClose = onClose,
        modifier = modifier,
    ) {
        ExploreZoneField(filters = state.filters, onFiltersChange = onFiltersChange)
        ExploreGuestsBudgetRow(state = state, onFiltersChange = onFiltersChange)
        ExploreDateField(state = state, onFiltersChange = onFiltersChange)
        ExploreDistanceRow(filters = state.filters, onFiltersChange = onFiltersChange)
        ExploreOrderRow(order = state.filters.order, onFiltersChange = onFiltersChange)
        ExploreFilterActions(
            fieldError = state.fieldError,
            onClear = onClear,
            onApply = onApply,
        )
    }
}

@Composable
private fun ExploreZoneField(
    filters: ExploreFilters,
    onFiltersChange: (ExploreFilters.() -> ExploreFilters) -> Unit,
) {
    ExploreFilterField(label = stringResource(R.string.explore_filters_zone)) {
        VirGoTextField(
            value = filters.query,
            onValueChange = { value -> onFiltersChange { copy(query = value) } },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = null,
                    tint = Slate400,
                )
            },
        )
    }
}

/** RF-13 y RF-14: invitados y presupuesto en la misma fila. */
@Composable
private fun ExploreGuestsBudgetRow(
    state: ExploreUiState,
    onFiltersChange: (ExploreFilters.() -> ExploreFilters) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(RowGap)) {
        ExploreFilterField(
            label = stringResource(R.string.explore_filters_guests),
            error = fieldError(state, ExploreFieldError.GUESTS, R.string.explore_filters_guests_error),
            modifier = Modifier.weight(1f),
        ) {
            VirGoTextField(
                value = state.filters.guests,
                onValueChange = { value -> onFiltersChange { copy(guests = value) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = state.fieldError == ExploreFieldError.GUESTS,
            )
        }
        ExploreFilterField(
            label = stringResource(R.string.explore_filters_budget),
            error = fieldError(state, ExploreFieldError.BUDGET, R.string.explore_filters_budget_error),
            modifier = Modifier.weight(1f),
        ) {
            VirGoTextField(
                value = state.filters.budget,
                onValueChange = { value -> onFiltersChange { copy(budget = value) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = state.fieldError == ExploreFieldError.BUDGET,
            )
        }
    }
}

/** RF-16: fecha disponible. */
@Composable
private fun ExploreDateField(
    state: ExploreUiState,
    onFiltersChange: (ExploreFilters.() -> ExploreFilters) -> Unit,
) {
    ExploreFilterField(
        label = stringResource(R.string.explore_filters_date),
        error = fieldError(state, ExploreFieldError.DATE, R.string.explore_filters_date_error),
    ) {
        VirGoTextField(
            value = state.filters.date,
            onValueChange = { value -> onFiltersChange { copy(date = value) } },
            isError = state.fieldError == ExploreFieldError.DATE,
        )
    }
}

/** Texto de error del campo marcado, o `null` cuando el campo está bien. */
@Composable
private fun fieldError(
    state: ExploreUiState,
    field: ExploreFieldError,
    messageRes: Int,
): String? = if (state.fieldError == field) stringResource(messageRes) else null

@Composable
private fun ExploreFilterField(
    label: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    content: @Composable () -> Unit,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = Slate700,
        )
        Spacer(modifier = Modifier.height(SmallGap))
        content()
        if (error != null) {
            Spacer(modifier = Modifier.height(SmallGap))
            Text(
                text = error,
                style = MaterialTheme.typography.labelSmall,
                color = ErrorRed,
            )
        }
    }
}

/** RF-15: distancia máxima en kilómetros. */
@Composable
private fun ExploreDistanceRow(
    filters: ExploreFilters,
    onFiltersChange: (ExploreFilters.() -> ExploreFilters) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.explore_filters_distance),
            style = MaterialTheme.typography.labelMedium,
            color = Slate700,
        )
        Spacer(modifier = Modifier.height(SmallGap))
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(SmallGap),
        ) {
            ExploreRadiusOptions.forEach { km ->
                ExploreChip(
                    label = stringResource(R.string.guest_range_option, km),
                    selected = filters.radiusKm == km,
                    onClick = { onFiltersChange { copy(radiusKm = km) } },
                )
            }
        }
    }
}

/** RF-17: orden de resultados. */
@Composable
private fun ExploreOrderRow(
    order: ExploreOrder,
    onFiltersChange: (ExploreFilters.() -> ExploreFilters) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.explore_order_cd),
            style = MaterialTheme.typography.labelMedium,
            color = Slate700,
        )
        Spacer(modifier = Modifier.height(SmallGap))
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(SmallGap),
        ) {
            ExploreOrders.forEach { option ->
                ExploreChip(
                    label = option.label(),
                    selected = option == order,
                    onClick = { onFiltersChange { copy(order = option) } },
                )
            }
        }
    }
}

@Composable
private fun ExploreFilterActions(
    fieldError: ExploreFieldError,
    onClear: () -> Unit,
    onApply: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(FieldGap),
    ) {
        if (fieldError != ExploreFieldError.NONE) {
            Text(
                text = stringResource(R.string.explore_filters_error),
                style = MaterialTheme.typography.labelSmall,
                color = ErrorRed,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(RowGap)) {
            VirGoOutlinedButton(
                onClick = onClear,
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = stringResource(R.string.explore_filters_clean),
                    style = MaterialTheme.typography.labelLarge,
                    color = VirGoPurple,
                )
            }
            VirGoButton(
                onClick = onApply,
                modifier = Modifier.weight(1f),
                text = stringResource(R.string.explore_filters_apply),
            )
        }
    }
}
