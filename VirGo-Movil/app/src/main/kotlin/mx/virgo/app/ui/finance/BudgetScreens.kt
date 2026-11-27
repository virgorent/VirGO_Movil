package mx.virgo.app.ui.finance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Payments
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import mx.virgo.app.R
import mx.virgo.app.ui.studio.AiLabel
import mx.virgo.app.ui.venuedetail.formatMxn
import mx.virgo.core.designsystem.components.VirGoButton
import mx.virgo.core.designsystem.components.VirGoCard
import mx.virgo.core.designsystem.components.VirGoOutlinedButton
import mx.virgo.core.designsystem.components.VirGoPageBody
import mx.virgo.core.designsystem.components.VirGoRow
import mx.virgo.core.designsystem.components.VirGoRowDivider
import mx.virgo.core.designsystem.components.VirGoTextField
import mx.virgo.core.designsystem.theme.VirGoTone
import mx.virgo.core.domain.BudgetCategory
import mx.virgo.core.domain.BudgetPreference
import mx.virgo.core.domain.BudgetRules
import mx.virgo.core.domain.EventFinance
import mx.virgo.core.domain.PaymentRules
import java.math.BigDecimal

@Composable
fun ClientBudget(
    links: FinanceLinks,
    viewModel: BudgetViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    BudgetContent(state, links, onRetry = viewModel::refresh)
}

@Composable
internal fun BudgetContent(
    state: BudgetUiState,
    links: FinanceLinks,
    onRetry: () -> Unit,
) {
    VirGoPageBody {
        FinanceGate(state.loading, state.failed, state.finance, links.onEvent, onRetry) { finance ->
            BudgetSummary(finance)
            PlanSection(finance)
            VirGoRow(
                stringResource(R.string.budget_go_suggestion),
                subtitle = stringResource(R.string.budget_go_suggestion_hint),
                icon = Icons.Outlined.AutoAwesome,
                onClick = links.onSuggestion,
            )
            VirGoRow(
                stringResource(R.string.budget_go_payments),
                icon = Icons.Outlined.Payments,
                tone = VirGoTone.GREEN,
                onClick = links.onPayments,
            )
        }
    }
}

@Composable
private fun BudgetSummary(finance: EventFinance) {
    val budget = finance.event.budget
    val planned = finance.plan.values.fold(BigDecimal.ZERO, BigDecimal::add)
    val paid = PaymentRules.paid(finance.payments)
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Stat(formatMxn(budget.toDouble()), stringResource(R.string.budget_total), Modifier.weight(1f))
        Stat(formatMxn(paid.toDouble()), stringResource(R.string.budget_paid), Modifier.weight(1f))
        Stat(
            formatMxn(budget.subtract(paid).toDouble()),
            stringResource(R.string.budget_available),
            Modifier.weight(1f),
        )
    }
    BudgetRules.overPlanned(finance.plan, budget)?.let {
        Alert(stringResource(R.string.budget_alert_over, formatMxn(it.toDouble())))
    }
    BudgetRules.paidWarning(paid, budget)?.let { Alert(stringResource(R.string.budget_alert_paid, it)) }
    if (planned.signum() > 0) {
        Text(
            stringResource(R.string.budget_planned, formatMxn(planned.toDouble())),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun PlanSection(finance: EventFinance) {
    if (finance.plan.isEmpty()) {
        Text(stringResource(R.string.budget_no_plan), color = MaterialTheme.colorScheme.onSurfaceVariant)
        return
    }
    VirGoCard {
        Column(Modifier.padding(horizontal = 16.dp)) {
            BudgetCategory.entries.filter { it in finance.plan }.forEachIndexed { index, category ->
                if (index > 0) VirGoRowDivider()
                val amount = finance.plan.getValue(category)
                VirGoRow(
                    title = stringResource(category.label()),
                    subtitle =
                        stringResource(
                            R.string.budget_line,
                            formatMxn(amount.toDouble()),
                            percent(amount, finance.event.budget),
                        ),
                    standalone = false,
                    subtitleMaxLines = Int.MAX_VALUE,
                )
            }
        }
    }
}

@Composable
fun ClientBudgetSuggestion(
    onEvent: () -> Unit,
    viewModel: BudgetViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    SuggestionContent(
        state,
        SuggestionActions(
            viewModel::togglePreference,
            viewModel::edit,
            viewModel::reset,
            viewModel::apply,
            onEvent,
            viewModel::refresh,
        ),
    )
}

@Composable
internal fun SuggestionContent(
    state: BudgetUiState,
    actions: SuggestionActions,
) {
    VirGoPageBody {
        FinanceGate(state.loading, state.failed, state.finance, actions.onEvent, actions.onRetry) { finance ->
            AiLabel()
            Text(
                stringResource(R.string.budget_suggestion_for, formatMxn(finance.event.budget.toDouble())),
                style = MaterialTheme.typography.bodyMedium,
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BudgetPreference.entries.forEach { preference ->
                    FilterChip(
                        selected = preference in state.preferences,
                        onClick = { actions.onPreference(preference) },
                        label = { Text(stringResource(preference.label())) },
                    )
                }
            }
            SuggestionFields(state.suggestion, actions.onEdit)
            val planned = state.suggestion.values.fold(BigDecimal.ZERO, BigDecimal::add)
            Text(
                stringResource(R.string.budget_planned, formatMxn(planned.toDouble())),
                fontWeight = FontWeight.SemiBold,
                color =
                    if (planned >
                        finance.event.budget
                    ) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
            )
            state.notice?.let { SaveNoticeText(it) }
            VirGoButton(
                onClick = actions.onApply,
                enabled = !state.saving,
                text = stringResource(R.string.budget_apply),
                modifier = Modifier.fillMaxWidth(),
            )
            VirGoOutlinedButton(onClick = actions.onReset, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.budget_reset))
            }
        }
    }
}

@Composable
private fun SuggestionFields(
    suggestion: Map<BudgetCategory, BigDecimal>,
    onEdit: (BudgetCategory, String) -> Unit,
) {
    BudgetCategory.entries.forEach { category ->
        val label = stringResource(category.label())
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(label, modifier = Modifier.weight(1f))
            VirGoTextField(
                value = suggestion[category]?.toPlainString().orEmpty(),
                onValueChange = { onEdit(category, it) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f).semantics { contentDescription = label },
            )
        }
    }
}

private fun BudgetPreference.label(): Int =
    when (this) {
        BudgetPreference.PREMIUM_FOOD -> R.string.budget_pref_food
        BudgetPreference.LOTS_OF_DECOR -> R.string.budget_pref_decor
        BudgetPreference.LIVE_MUSIC -> R.string.budget_pref_music
        BudgetPreference.SAVE_ON_VENUE -> R.string.budget_pref_venue
    }
