package mx.virgo.app.ui.finance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EventNote
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import mx.virgo.app.R
import mx.virgo.core.designsystem.components.EmptyState
import mx.virgo.core.designsystem.components.ErrorBanner
import mx.virgo.core.designsystem.components.LoadingState
import mx.virgo.core.designsystem.components.VirGoButton
import mx.virgo.core.designsystem.components.VirGoCard
import mx.virgo.core.designsystem.components.VirGoRow
import mx.virgo.core.designsystem.theme.VirGoTone
import mx.virgo.core.domain.BudgetCategory
import mx.virgo.core.domain.EventFinance
import java.math.BigDecimal

/** Carga, error, sin evento o contenido: igual en presupuesto, sugerencia y pagos. */
@Composable
internal fun FinanceGate(
    loading: Boolean,
    failed: Boolean,
    finance: EventFinance?,
    onEvent: () -> Unit,
    onRetry: () -> Unit,
    content: @Composable (EventFinance) -> Unit,
) {
    when {
        loading && finance == null -> LoadingState(message = stringResource(R.string.finance_loading))
        failed ->
            ErrorBanner(
                stringResource(R.string.finance_error),
                actionLabel = stringResource(R.string.booking_retry),
                onAction = onRetry,
            )
        finance == null ->
            EmptyState(
                title = stringResource(R.string.finance_no_event),
                text = stringResource(R.string.finance_no_event_text),
                iconVector = Icons.Outlined.EventNote,
                action = { VirGoButton(onClick = onEvent, text = stringResource(R.string.finance_go_event)) },
            )
        else -> content(finance)
    }
}

@Composable
internal fun Stat(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    VirGoCard(modifier) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
internal fun Alert(text: String) {
    VirGoCard {
        VirGoRow(
            text,
            icon = Icons.Outlined.WarningAmber,
            tone = VirGoTone.AMBER,
            standalone = false,
            modifier = Modifier.padding(horizontal = 16.dp),
            subtitleMaxLines = Int.MAX_VALUE,
        )
    }
}

@Composable
internal fun SaveNoticeText(notice: SaveNotice) {
    val (text, color) =
        when (notice) {
            SaveNotice.SAVED -> R.string.finance_saved to MaterialTheme.colorScheme.onSurfaceVariant
            SaveNotice.UNAVAILABLE -> R.string.finance_unavailable to MaterialTheme.colorScheme.onSurfaceVariant
            SaveNotice.FAILED -> R.string.finance_failed to MaterialTheme.colorScheme.error
        }
    Text(stringResource(text), color = color, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
}

internal fun percent(
    amount: BigDecimal,
    budget: BigDecimal,
): Int =
    if (budget.signum() >
        0
    ) {
        amount.multiply(HUNDRED).divide(budget, 0, java.math.RoundingMode.HALF_UP).toInt()
    } else {
        0
    }

internal fun BudgetCategory.label(): Int =
    when (this) {
        BudgetCategory.VENUE -> R.string.budget_cat_venue
        BudgetCategory.CATERING -> R.string.budget_cat_catering
        BudgetCategory.DECORATION -> R.string.budget_cat_decoration
        BudgetCategory.MUSIC -> R.string.budget_cat_music
        BudgetCategory.PHOTOGRAPHY -> R.string.budget_cat_photography
        BudgetCategory.OTHER -> R.string.budget_cat_other
    }

private val HUNDRED = BigDecimal(100)

/** Solo dígitos y sin ceros a la izquierda («05» → «5»); vacío se respeta para poder reescribir. */
internal fun cleanDigits(typed: String): String {
    val digits = typed.filter(Char::isDigit)
    return digits.trimStart('0').ifEmpty { digits.take(1) }
}
