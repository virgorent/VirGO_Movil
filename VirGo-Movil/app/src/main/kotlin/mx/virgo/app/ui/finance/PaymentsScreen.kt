package mx.virgo.app.ui.finance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import mx.virgo.app.R
import mx.virgo.app.ui.bookings.formatDate
import mx.virgo.app.ui.venuedetail.formatMxn
import mx.virgo.core.designsystem.components.VirGoBadge
import mx.virgo.core.designsystem.components.VirGoButton
import mx.virgo.core.designsystem.components.VirGoCard
import mx.virgo.core.designsystem.components.VirGoOutlinedButton
import mx.virgo.core.designsystem.components.VirGoPageBody
import mx.virgo.core.designsystem.components.VirGoRow
import mx.virgo.core.designsystem.components.VirGoRowDivider
import mx.virgo.core.designsystem.components.VirGoTextField
import mx.virgo.core.designsystem.theme.VirGoTone
import mx.virgo.core.domain.EventFinance
import mx.virgo.core.domain.ExternalPayment
import mx.virgo.core.domain.PaymentProblem
import mx.virgo.core.domain.PaymentRules
import mx.virgo.core.domain.PaymentStatus

@Composable
fun ClientPayments(
    onEvent: () -> Unit,
    viewModel: PaymentsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    PaymentsContent(
        state,
        PaymentsActions(viewModel::updateForm, viewModel::add, viewModel::remove, onEvent, viewModel::refresh),
    )
}

@Composable
internal fun PaymentsContent(
    state: PaymentsUiState,
    actions: PaymentsActions,
) {
    VirGoPageBody {
        FinanceGate(state.loading, state.failed, state.finance, actions.onEvent, actions.onRetry) { finance ->
            VirGoCard {
                VirGoRow(
                    stringResource(R.string.payments_notice),
                    icon = Icons.Outlined.Info,
                    standalone = false,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    subtitleMaxLines = Int.MAX_VALUE,
                )
            }
            PaymentsSummary(finance)
            state.notice?.let { SaveNoticeText(it) }
            val form = state.form
            if (form == null) {
                VirGoButton(
                    onClick = { actions.onForm(PaymentForm()) },
                    text = stringResource(R.string.payments_add),
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                PaymentFormCard(form, state.problems, state.saving, actions)
            }
            PaymentList(finance.payments, state.saving, actions.onRemove)
        }
    }
}

@Composable
private fun PaymentsSummary(finance: EventFinance) {
    val paid = PaymentRules.paid(finance.payments)
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Stat(formatMxn(paid.toDouble()), stringResource(R.string.budget_paid), Modifier.weight(1f))
        Stat(
            formatMxn(PaymentRules.pending(finance.payments).toDouble()),
            stringResource(R.string.payments_pending),
            Modifier.weight(1f),
        )
        Stat(
            formatMxn(
                finance.event.budget
                    .subtract(paid)
                    .toDouble(),
            ),
            stringResource(R.string.payments_balance),
            Modifier.weight(1f),
        )
    }
}

@Composable
private fun PaymentFormCard(
    form: PaymentForm,
    problems: Set<PaymentProblem>,
    saving: Boolean,
    actions: PaymentsActions,
) {
    VirGoCard {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            val concept = stringResource(R.string.payments_concept)
            VirGoTextField(
                value = form.concept,
                onValueChange = { actions.onForm(form.copy(concept = it.take(PaymentRules.CONCEPT_MAX))) },
                placeholder = concept,
                isError = PaymentProblem.CONCEPT in problems,
                modifier = Modifier.semantics { contentDescription = concept },
            )
            val amount = stringResource(R.string.payments_amount)
            VirGoTextField(
                value = form.amount,
                onValueChange = { actions.onForm(form.copy(amount = it.filter { c -> c.isDigit() || c == '.' })) },
                placeholder = amount,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = PaymentProblem.AMOUNT in problems,
                modifier = Modifier.semantics { contentDescription = amount },
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(PaymentStatus.COMPLETED, PaymentStatus.PENDING).forEach { status ->
                    FilterChip(
                        selected = form.status == status,
                        onClick = { actions.onForm(form.copy(status = status)) },
                        label = { Text(stringResource(status.label())) },
                    )
                }
            }
            problems.forEach { Text(stringResource(it.message()), color = MaterialTheme.colorScheme.error) }
            VirGoButton(onClick = actions.onAdd, enabled = !saving, text = stringResource(R.string.payments_save))
            VirGoOutlinedButton(onClick = { actions.onForm(null) }, enabled = !saving) {
                Text(stringResource(R.string.payments_cancel))
            }
        }
    }
}

@Composable
private fun PaymentList(
    payments: List<ExternalPayment>,
    saving: Boolean,
    onRemove: (String) -> Unit,
) {
    if (payments.isEmpty()) {
        Text(stringResource(R.string.payments_empty), color = MaterialTheme.colorScheme.onSurfaceVariant)
        return
    }
    VirGoCard {
        Column(Modifier.padding(horizontal = 16.dp)) {
            payments.sortedByDescending { it.paidAt }.forEachIndexed { index, payment ->
                if (index > 0) VirGoRowDivider()
                val remove = stringResource(R.string.payments_remove, payment.concept)
                VirGoRow(
                    title = payment.concept,
                    subtitle =
                        listOfNotNull(
                            formatMxn(payment.amount.toDouble()),
                            payment.paidAt?.let(::formatDate),
                        ).joinToString(" · "),
                    standalone = false,
                    subtitleMaxLines = Int.MAX_VALUE,
                    trailing = {
                        Row {
                            VirGoBadge(stringResource(payment.status.label()), tone = payment.status.tone())
                            IconButton(onClick = { onRemove(payment.id) }, enabled = !saving) {
                                Icon(Icons.Outlined.DeleteOutline, remove)
                            }
                        }
                    },
                )
            }
        }
    }
}

private fun PaymentStatus.label(): Int =
    when (this) {
        PaymentStatus.COMPLETED -> R.string.payments_status_completed
        PaymentStatus.PENDING -> R.string.payments_status_pending
        PaymentStatus.FAILED -> R.string.payments_status_failed
        PaymentStatus.REFUNDED -> R.string.payments_status_refunded
    }

private fun PaymentStatus.tone(): VirGoTone =
    when (this) {
        PaymentStatus.COMPLETED -> VirGoTone.GREEN
        PaymentStatus.PENDING -> VirGoTone.AMBER
        PaymentStatus.FAILED -> VirGoTone.RED
        PaymentStatus.REFUNDED -> VirGoTone.GRAY
    }

private fun PaymentProblem.message(): Int =
    when (this) {
        PaymentProblem.CONCEPT -> R.string.payments_error_concept
        PaymentProblem.AMOUNT -> R.string.payments_error_amount
        PaymentProblem.FUTURE_DATE -> R.string.payments_error_date
    }
