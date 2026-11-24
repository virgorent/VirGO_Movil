package mx.virgo.app.demo

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import mx.virgo.app.R

internal const val SHEET_MONTH_DAYS = 30
internal const val SHEET_WEEK_DAYS = 7
internal const val SHEET_OCCUPIED_DAY = 14
internal const val SHEET_SECOND_OCCUPIED_DAY = 21

@Composable
internal fun DemoSheetsCalendar(
    selected: Int,
    onSelect: (Int) -> Unit,
    showOccupied: Boolean = false,
) {
    Text(stringResource(R.string.demo_sheet_month))
    Text(stringResource(R.string.demo_sheet_calendar_key))
    Column(Modifier.horizontalScroll(rememberScrollState())) {
        Row(horizontalArrangement = Arrangement.spacedBy(DemoMetrics.toolbarSpacing)) {
            stringArrayResource(R.array.demo_sheet_weekdays).forEach { weekday ->
                Text(weekday, modifier = Modifier.width(DemoMetrics.minimumCard))
            }
        }
        for (week in (1..SHEET_MONTH_DAYS).chunked(SHEET_WEEK_DAYS)) {
            Row(horizontalArrangement = Arrangement.spacedBy(DemoMetrics.toolbarSpacing)) {
                week.forEach { day ->
                    val occupied = showOccupied && day in setOf(SHEET_OCCUPIED_DAY, SHEET_SECOND_OCCUPIED_DAY)
                    FilterChip(
                        selected = day == selected,
                        onClick = { onSelect(day) },
                        enabled = !occupied,
                        label = {
                            val label = if (occupied) R.string.demo_sheet_busy_day else R.string.demo_sheet_day
                            Text(stringResource(label, day))
                        },
                        modifier = Modifier.width(DemoMetrics.minimumCard).heightIn(min = DemoMetrics.touchTarget),
                    )
                }
            }
        }
    }
    Text(stringResource(R.string.demo_sheet_selected_date, selected))
}

@Composable
internal fun DemoSheetsVenue(onConfirm: () -> Unit) {
    val saved = workspaceRecord(WorkspaceSection.VENUE_DATE)
    var day by rememberSaveable { mutableIntStateOf(saved?.day ?: 1) }
    DemoNotice(stringResource(R.string.demo_sheet_availability))
    DemoSheetsCalendar(day, { day = it }, showOccupied = true)
    DemoSheetsSubmit(
        stringResource(R.string.demo_sheet_selected_date, day),
        onConfirm = onConfirm,
        section = WorkspaceSection.VENUE_DATE,
        record = WorkspaceRecord(day = day),
    )
}
