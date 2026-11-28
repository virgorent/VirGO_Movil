package mx.virgo.app.ui.ownertools

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import mx.virgo.app.R
import mx.virgo.app.ui.bookings.formatDate
import mx.virgo.app.ui.finance.SaveNoticeText
import mx.virgo.app.ui.finance.cleanDigits
import mx.virgo.app.ui.venuedetail.formatMxn
import mx.virgo.core.designsystem.components.EmptyState
import mx.virgo.core.designsystem.components.ErrorBanner
import mx.virgo.core.designsystem.components.LoadingState
import mx.virgo.core.designsystem.components.VirGoButton
import mx.virgo.core.designsystem.components.VirGoPageBody
import mx.virgo.core.designsystem.components.VirGoRow
import mx.virgo.core.designsystem.components.VirGoTextField
import mx.virgo.core.domain.VenuePricing
import mx.virgo.core.domain.VenueSetups
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.util.Locale

@Composable
fun OwnerVenueTool(
    tool: VenueTool,
    // Uno por herramienta: el aviso de guardar de «Precios» no debe aparecer en «Montajes».
    viewModel: VenueToolsViewModel = hiltViewModel(key = "venue-tool-$tool"),
) {
    val state by viewModel.state.collectAsState()
    VenueToolContent(
        tool,
        state,
        VenueToolActions(
            onVenue = viewModel::select,
            onPricing = viewModel::editPricing,
            onSetups = viewModel::editSetups,
            onSave = if (tool == VenueTool.PRICING) viewModel::savePricing else viewModel::saveSetups,
            onRetry = viewModel::refresh,
        ),
        today = LocalDate.now(),
    )
}

@Composable
internal fun VenueToolContent(
    tool: VenueTool,
    state: VenueToolsUiState,
    actions: VenueToolActions,
    today: LocalDate,
) {
    VirGoPageBody {
        when {
            state.loading -> LoadingState()
            state.failed ->
                ErrorBanner(
                    stringResource(R.string.tools_error),
                    actionLabel = stringResource(R.string.booking_retry),
                    onAction = actions.onRetry,
                )
            state.venues.isEmpty() ->
                EmptyState(
                    title = stringResource(R.string.owner_calendar_empty_title),
                    text = stringResource(R.string.tools_no_venues),
                    iconVector = Icons.Outlined.Storefront,
                )
            else -> {
                VenuePicker(state.venues.map { it.id to it.name }, state.venueId, actions.onVenue)
                when (tool) {
                    VenueTool.PRICING -> state.pricing?.let { PricingForm(it, actions.onPricing, today) }
                    VenueTool.SETUPS -> SetupsForm(state.setups, actions.onSetups)
                }
                state.notice?.let { SaveNoticeText(it) }
                VirGoButton(
                    onClick = actions.onSave,
                    enabled = !state.saving && state.pricing?.valid != false,
                    text = stringResource(R.string.tools_save),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
internal fun VenuePicker(
    venues: List<Pair<String, String>>,
    selected: String?,
    onVenue: (String) -> Unit,
) {
    if (venues.size < 2) return
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        venues.forEach { (id, name) ->
            FilterChip(selected = id == selected, onClick = { onVenue(id) }, label = { Text(name) })
        }
    }
}

@Composable
private fun PricingForm(
    pricing: VenuePricing,
    onPricing: (VenuePricing) -> Unit,
    today: LocalDate,
) {
    Text(
        stringResource(
            R.string.tools_base_price,
            formatMxn(pricing.base.toDouble()),
            stringResource(unitLabel(pricing.unit)),
        ),
        style = MaterialTheme.typography.titleSmall,
    )
    NumberField(
        stringResource(R.string.tools_weekend_pct),
        pricing.weekendPct,
    ) { onPricing(pricing.copy(weekendPct = it)) }
    NumberField(stringResource(R.string.tools_season_pct), pricing.highSeasonPct) {
        onPricing(pricing.copy(highSeasonPct = it))
    }
    Text(stringResource(R.string.tools_season_months), style = MaterialTheme.typography.titleSmall)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        for (month in 1..VenuePricing.MONTHS) {
            val selected = month in pricing.highSeasonMonths
            FilterChip(
                selected = selected,
                onClick = {
                    val months = if (selected) pricing.highSeasonMonths - month else pricing.highSeasonMonths + month
                    onPricing(pricing.copy(highSeasonMonths = months))
                },
                label = {
                    Text(
                        java.time.Month
                            .of(month)
                            .getDisplayName(TextStyle.SHORT, SPANISH),
                    )
                },
            )
        }
    }
    if (!pricing.valid) Text(stringResource(R.string.tools_pct_invalid), color = MaterialTheme.colorScheme.error)
    Text(stringResource(R.string.tools_preview), style = MaterialTheme.typography.titleSmall)
    previewDates(pricing, today).forEach { date ->
        VirGoRow(
            title = formatDate(date),
            subtitle = formatMxn(pricing.priceOn(date).toDouble()),
            standalone = true,
        )
    }
}

@Composable
private fun SetupsForm(
    setups: VenueSetups,
    onSetups: (VenueSetups) -> Unit,
) {
    NumberField(stringResource(R.string.tools_setup_banquet), setups.banquet) { onSetups(setups.copy(banquet = it)) }
    NumberField(stringResource(R.string.tools_setup_auditorium), setups.auditorium) {
        onSetups(setups.copy(auditorium = it))
    }
    NumberField(stringResource(R.string.tools_setup_cocktail), setups.cocktail) { onSetups(setups.copy(cocktail = it)) }
    Text(stringResource(R.string.tools_floor_plan_soon), color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
internal fun NumberField(
    label: String,
    value: Int,
    onValue: (Int) -> Unit,
) {
    // El texto vive aquí para poder borrarlo por completo: vacío cuenta como 0 sin reescribir el campo.
    var text by remember { mutableStateOf(value.toString()) }
    LaunchedEffect(value) {
        if ((text.toIntOrNull() ?: 0) != value) text = value.toString()
    }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(label, modifier = Modifier.weight(1f))
        VirGoTextField(
            value = text,
            onValueChange = { typed ->
                text = cleanDigits(typed).take(MAX_DIGITS)
                onValue(text.toIntOrNull() ?: 0)
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f).semantics { contentDescription = label },
        )
    }
}

/** Próximo martes, próximo sábado y el primer martes de temporada alta (`025` RF-01). */
internal fun previewDates(
    pricing: VenuePricing,
    today: LocalDate,
): List<LocalDate> {
    val tuesday = today.with(TemporalAdjusters.next(DayOfWeek.TUESDAY))
    val saturday = today.with(TemporalAdjusters.next(DayOfWeek.SATURDAY))
    val season =
        generateSequence(tuesday) { it.plusWeeks(1) }
            .take(WEEKS_IN_YEAR)
            .firstOrNull { it.monthValue in pricing.highSeasonMonths }
    return listOfNotNull(tuesday, saturday, season).distinct()
}

internal fun unitLabel(unit: String): Int = if (unit == "day") R.string.tools_per_day else R.string.tools_per_hour

private const val MAX_DIGITS = 5
private const val WEEKS_IN_YEAR = 53
private val SPANISH = Locale.forLanguageTag("es-MX")
