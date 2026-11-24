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
internal fun DemoSheetsPayment(onConfirm: () -> Unit) {
    var concept by rememberSaveable { mutableStateOf("") }
    var amount by rememberSaveable { mutableStateOf("") }
    var status by rememberSaveable { mutableIntStateOf(R.string.demo_sheet_pending) }
    DemoNotice(stringResource(R.string.demo_sheet_no_transaction))
    DemoField(stringResource(R.string.demo_sheet_concept), concept, { concept = it })
    DemoField(stringResource(R.string.demo_sheet_amount), amount, { amount = it })
    DemoSheetsOptions(
        R.string.demo_sheet_status,
        listOf(R.string.demo_sheet_pending, R.string.demo_sheet_partial, R.string.demo_sheet_completed),
        status,
        { status = it },
    )
    DemoSheetsSubmit(
        stringResource(R.string.demo_sheet_payment_summary, concept, amount, stringResource(status)),
        concept.isNotBlank() && demoSheetAmountValid(amount),
        onConfirm,
        section = WorkspaceSection.PAYMENTS,
        record = WorkspaceRecord(concept, amount = amount.toDoubleOrNull(), selection = status),
    )
}

@Composable
internal fun DemoSheetsGuest(onConfirm: () -> Unit) {
    var alias by rememberSaveable { mutableStateOf("") }
    var group by rememberSaveable { mutableIntStateOf(R.string.demo_sheet_friends) }
    var status by rememberSaveable { mutableIntStateOf(R.string.demo_sheet_pending) }
    var companions by rememberSaveable { mutableIntStateOf(0) }
    DemoNotice(stringResource(R.string.demo_sheet_fictional_only))
    DemoField(stringResource(R.string.demo_sheet_alias), alias, { alias = it })
    DemoSheetsOptions(
        R.string.demo_sheet_group,
        listOf(R.string.demo_sheet_friends, R.string.demo_sheet_family, R.string.demo_sheet_work),
        group,
        { group = it },
    )
    DemoSheetsOptions(
        R.string.demo_sheet_rsvp,
        listOf(R.string.demo_sheet_pending, R.string.demo_sheet_attending, R.string.demo_sheet_declined),
        status,
        { status = it },
    )
    DemoAction(stringResource(R.string.demo_sheet_companions, companions)) {
        companions = (companions + 1) % SHEET_COMPANION_OPTIONS
    }
    DemoSheetsSubmit(
        stringResource(
            R.string.demo_sheet_guest_summary,
            alias,
            stringResource(group),
            stringResource(status),
            companions,
        ),
        alias.isNotBlank(),
        onConfirm,
        section = WorkspaceSection.GUESTS,
        record = WorkspaceRecord(alias, selection = status),
    )
}

@Composable
internal fun DemoSheetsTask(onConfirm: () -> Unit) {
    var title by rememberSaveable { mutableStateOf("") }
    var day by rememberSaveable { mutableIntStateOf(1) }
    var priority by rememberSaveable { mutableIntStateOf(R.string.demo_sheet_normal) }
    DemoField(stringResource(R.string.demo_sheet_task_title), title, { title = it })
    DemoSheetsOptions(
        R.string.demo_sheet_priority,
        listOf(R.string.demo_sheet_normal, R.string.demo_sheet_high),
        priority,
        { priority = it },
    )
    DemoSheetsCalendar(day, { day = it })
    DemoSheetsSubmit(
        stringResource(R.string.demo_sheet_task_summary, title, day, stringResource(priority)),
        title.isNotBlank(),
        onConfirm,
        section = WorkspaceSection.TASKS,
        record = WorkspaceRecord(title, day = day, selection = priority),
    )
}

private const val SHEET_COMPANION_OPTIONS = 6
