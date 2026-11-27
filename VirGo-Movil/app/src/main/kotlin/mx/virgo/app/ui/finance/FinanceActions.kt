package mx.virgo.app.ui.finance

import mx.virgo.core.domain.BudgetCategory
import mx.virgo.core.domain.BudgetPreference

/** Navegación desde «Presupuesto» y «Pagos». */
data class FinanceLinks(
    val onEvent: () -> Unit = {},
    val onSuggestion: () -> Unit = {},
    val onPayments: () -> Unit = {},
)

/** Callbacks del presupuesto sugerido (`015` RF-04–RF-06). */
data class SuggestionActions(
    val onPreference: (BudgetPreference) -> Unit = {},
    val onEdit: (BudgetCategory, String) -> Unit = { _, _ -> },
    val onReset: () -> Unit = {},
    val onApply: () -> Unit = {},
    val onEvent: () -> Unit = {},
    val onRetry: () -> Unit = {},
)

/** Callbacks de «Pagos» (`018`). */
data class PaymentsActions(
    val onForm: (PaymentForm?) -> Unit = {},
    val onAdd: () -> Unit = {},
    val onRemove: (String) -> Unit = {},
    val onEvent: () -> Unit = {},
    val onRetry: () -> Unit = {},
)
