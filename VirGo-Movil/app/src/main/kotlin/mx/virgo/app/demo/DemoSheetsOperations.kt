package mx.virgo.app.demo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import mx.virgo.app.R

@Composable
internal fun DemoSheetsBlock(onConfirm: () -> Unit) {
    var from by rememberSaveable { mutableIntStateOf(1) }
    var to by rememberSaveable { mutableIntStateOf(1) }
    var reason by rememberSaveable { mutableIntStateOf(R.string.demo_sheet_maintenance) }
    DemoNotice(stringResource(R.string.demo_sheet_block_hint))
    DemoSection(stringResource(R.string.demo_sheet_from))
    DemoSheetsCalendar(from, { from = it })
    DemoSection(stringResource(R.string.demo_sheet_to))
    DemoSheetsCalendar(to, { to = it })
    DemoSheetsOptions(
        R.string.demo_sheet_reason,
        listOf(R.string.demo_sheet_maintenance, R.string.demo_sheet_private_event, R.string.demo_sheet_other),
        reason,
        { reason = it },
    )
    val conflict = demoSheetRangeConflicts(from, to)
    DemoNotice(
        stringResource(
            when {
                to < from -> R.string.demo_sheet_range_invalid
                conflict -> R.string.demo_sheet_range_conflict
                else -> R.string.demo_sheet_range_free
            },
        ),
    )
    DemoSheetsSubmit(
        stringResource(R.string.demo_sheet_block_summary, from, to, stringResource(reason)),
        to >= from && !conflict,
        onConfirm,
        section = WorkspaceSection.BLOCKS,
        record = WorkspaceRecord(day = from, endDay = to, selection = reason),
    )
}

internal fun demoSheetRangeConflicts(
    from: Int,
    to: Int,
): Boolean = SHEET_OCCUPIED_DAY in from..to || SHEET_SECOND_OCCUPIED_DAY in from..to

@Composable
internal fun DemoSheetsAgenda(onConfirm: () -> Unit) {
    var event by rememberSaveable { mutableStateOf("") }
    var client by rememberSaveable { mutableStateOf("") }
    var place by rememberSaveable { mutableStateOf("") }
    var day by rememberSaveable { mutableIntStateOf(1) }
    var start by rememberSaveable { mutableIntStateOf(R.string.demo_sheet_start_early) }
    var end by rememberSaveable { mutableIntStateOf(R.string.demo_sheet_end_late) }
    DemoNotice(stringResource(R.string.demo_sheet_agenda_hint))
    DemoField(stringResource(R.string.demo_sheet_event_name), event, { event = it })
    DemoField(stringResource(R.string.demo_sheet_client_alias), client, { client = it })
    DemoField(stringResource(R.string.demo_sheet_place), place, { place = it })
    DemoSheetsCalendar(day, { day = it })
    DemoSheetsOptions(
        R.string.demo_sheet_start,
        listOf(R.string.demo_sheet_start_early, R.string.demo_sheet_start_late),
        start,
        { start = it },
    )
    DemoSheetsOptions(
        R.string.demo_sheet_end,
        listOf(R.string.demo_sheet_end_early, R.string.demo_sheet_end_late),
        end,
        { end = it },
    )
    val timeValid = start != R.string.demo_sheet_start_late || end != R.string.demo_sheet_end_early
    if (!timeValid) DemoNotice(stringResource(R.string.demo_sheet_time_invalid))
    DemoSheetsSubmit(
        stringResource(
            R.string.demo_sheet_agenda_summary,
            event,
            client,
            place,
            day,
            stringResource(start),
            stringResource(end),
        ),
        event.isNotBlank() && client.isNotBlank() && place.isNotBlank() && timeValid,
        onConfirm,
        section = WorkspaceSection.AGENDA,
        record = WorkspaceRecord(event, day = day, selection = start),
    )
}
