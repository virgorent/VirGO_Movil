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
internal fun DemoSheetsEvent(onConfirm: () -> Unit) {
    val sampleName = stringResource(R.string.demo_sheet_sample_event)
    var name by rememberSaveable { mutableStateOf(sampleName) }
    var budget by rememberSaveable { mutableStateOf("") }
    var guests by rememberSaveable { mutableIntStateOf(SHEET_MIN_GUESTS) }
    var day by rememberSaveable { mutableIntStateOf(1) }
    var type by rememberSaveable { mutableIntStateOf(R.string.demo_sheet_wedding) }
    var mode by rememberSaveable { mutableIntStateOf(R.string.demo_sheet_create) }
    DemoSheetsOptions(
        R.string.demo_sheet_mode,
        listOf(R.string.demo_sheet_create, R.string.demo_sheet_edit),
        mode,
        { mode = it },
    )
    DemoSheetsOptions(
        R.string.demo_sheet_event_type,
        listOf(R.string.demo_sheet_wedding, R.string.demo_sheet_birthday, R.string.demo_sheet_meeting),
        type,
        { type = it },
    )
    DemoField(stringResource(R.string.demo_sheet_name), name, { name = it })
    DemoField(stringResource(R.string.demo_sheet_amount), budget, { budget = it })
    DemoSheetsGuests(guests) { guests = it }
    DemoSheetsCalendar(day, { day = it })
    DemoSheetsSubmit(
        stringResource(
            R.string.demo_sheet_event_summary,
            stringResource(mode),
            name,
            stringResource(type),
            budget,
            guests,
            day,
        ),
        name.isNotBlank() && demoSheetAmountValid(budget),
        onConfirm,
    )
}

@Composable
internal fun DemoSheetsTeam(onConfirm: () -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    var amount by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableIntStateOf(R.string.demo_sheet_music) }
    var status by rememberSaveable { mutableIntStateOf(R.string.demo_sheet_pending) }
    DemoCard(stringResource(R.string.demo_sheet_sample_team), stringResource(R.string.demo_sheet_team_hint))
    DemoField(stringResource(R.string.demo_sheet_alias), name, { name = it })
    DemoSheetsOptions(
        R.string.demo_sheet_category,
        listOf(R.string.demo_sheet_music, R.string.demo_sheet_flowers, R.string.demo_sheet_food_line),
        category,
        { category = it },
    )
    DemoField(stringResource(R.string.demo_sheet_amount), amount, { amount = it })
    DemoSheetsOptions(
        R.string.demo_sheet_status,
        listOf(R.string.demo_sheet_pending, R.string.demo_sheet_partial, R.string.demo_sheet_completed),
        status,
        { status = it },
    )
    DemoSheetsSubmit(
        stringResource(
            R.string.demo_sheet_team_summary,
            name,
            stringResource(category),
            amount,
            stringResource(status),
        ),
        name.isNotBlank() && demoSheetAmountValid(amount),
        onConfirm,
    )
}
