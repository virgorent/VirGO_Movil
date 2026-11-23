package mx.virgo.app.demo

import androidx.annotation.StringRes
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import mx.virgo.app.R

internal const val SHEET_MIN_GUESTS = 10
internal const val SHEET_MAX_GUESTS = 400
internal const val SHEET_GUEST_STEP = 10
internal const val SHEET_MAX_AMOUNT = 1_000_000.0

@Composable
internal fun DemoSheetsOptions(
    @StringRes label: Int,
    options: List<Int>,
    selected: Int,
    onSelect: (Int) -> Unit,
) {
    Text(stringResource(label))
    Row(
        Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(DemoMetrics.compactSpacing),
    ) {
        options.forEach { option ->
            FilterChip(
                selected = selected == option,
                onClick = { onSelect(option) },
                label = { Text(stringResource(option)) },
                modifier = Modifier.heightIn(min = DemoMetrics.touchTarget),
            )
        }
    }
}

@Composable
internal fun DemoSheetsGuests(
    guests: Int,
    onChange: (Int) -> Unit,
) {
    Text(stringResource(R.string.demo_sheet_guest_count, guests))
    Row(horizontalArrangement = Arrangement.spacedBy(DemoMetrics.compactSpacing)) {
        FilterChip(
            selected = false,
            onClick = { onChange((guests - SHEET_GUEST_STEP).coerceAtLeast(SHEET_MIN_GUESTS)) },
            label = { Text(stringResource(R.string.demo_sheet_less)) },
            modifier = Modifier.heightIn(min = DemoMetrics.touchTarget),
        )
        FilterChip(
            selected = false,
            onClick = { onChange((guests + SHEET_GUEST_STEP).coerceAtMost(SHEET_MAX_GUESTS)) },
            label = { Text(stringResource(R.string.demo_sheet_more)) },
            modifier = Modifier.heightIn(min = DemoMetrics.touchTarget),
        )
    }
}

@Composable
internal fun DemoSheetsSubmit(
    summary: String,
    valid: Boolean = true,
    onConfirm: () -> Unit,
) {
    var confirmed by rememberSaveable { mutableStateOf("") }
    val localSummary = stringResource(R.string.demo_sheet_summary, summary)
    val invalid = stringResource(R.string.demo_sheet_invalid)
    DemoAction(stringResource(R.string.demo_sheet_confirm)) {
        confirmed = if (valid) localSummary else invalid
        if (valid) onConfirm()
    }
    if (confirmed.isNotEmpty()) DemoNotice(confirmed)
}

internal fun demoSheetAmountValid(value: String): Boolean =
    value.toDoubleOrNull()?.let { it.isFinite() && it > 0 && it <= SHEET_MAX_AMOUNT } == true
