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
internal fun DemoSheetsReminder(onConfirm: () -> Unit) {
    var title by rememberSaveable { mutableStateOf("") }
    var day by rememberSaveable { mutableIntStateOf(1) }
    var time by rememberSaveable { mutableIntStateOf(R.string.demo_sheet_morning) }
    var channel by rememberSaveable { mutableIntStateOf(R.string.demo_sheet_in_app) }
    var kind by rememberSaveable { mutableIntStateOf(R.string.demo_sheet_reminder_payment) }
    DemoNotice(stringResource(R.string.demo_sheet_no_alarm))
    DemoField(stringResource(R.string.demo_sheet_reminder_title), title, { title = it })
    DemoSheetsOptions(
        R.string.demo_sheet_reminder_kind,
        listOf(
            R.string.demo_sheet_reminder_payment,
            R.string.demo_sheet_confirmation,
            R.string.demo_sheet_delivery,
            R.string.demo_sheet_other,
        ),
        kind,
        { kind = it },
    )
    DemoSheetsCalendar(day, { day = it })
    DemoSheetsOptions(
        R.string.demo_sheet_time,
        listOf(R.string.demo_sheet_morning, R.string.demo_sheet_evening),
        time,
        { time = it },
    )
    DemoSheetsOptions(
        R.string.demo_sheet_channel,
        listOf(R.string.demo_sheet_in_app, R.string.demo_sheet_mock_email),
        channel,
        { channel = it },
    )
    DemoSheetsSubmit(
        stringResource(
            R.string.demo_sheet_reminder_summary,
            title,
            day,
            stringResource(time),
            stringResource(channel),
            stringResource(kind),
        ),
        title.isNotBlank(),
        onConfirm,
    )
}

@Composable
internal fun DemoSheetsCollaborator(onConfirm: () -> Unit) {
    val sampleEmail = stringResource(R.string.demo_sheet_team_email)
    var alias by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf(sampleEmail) }
    var permission by rememberSaveable { mutableIntStateOf(R.string.demo_sheet_view) }
    DemoNotice(stringResource(R.string.demo_sheet_no_invite))
    DemoField(stringResource(R.string.demo_sheet_alias), alias, { alias = it })
    DemoField(stringResource(R.string.demo_sheet_demo_email), email, { email = it })
    DemoSheetsOptions(
        R.string.demo_sheet_permission,
        listOf(R.string.demo_sheet_view, R.string.demo_sheet_edit),
        permission,
        { permission = it },
    )
    DemoCard(
        stringResource(permission),
        stringResource(
            if (permission == R.string.demo_sheet_view) {
                R.string.demo_sheet_view_hint
            } else {
                R.string.demo_sheet_edit_hint
            },
        ),
    )
    DemoSheetsSubmit(
        stringResource(R.string.demo_sheet_collaborator_summary, alias, email, stringResource(permission)),
        alias.isNotBlank() &&
            email.endsWith("@example.invalid") &&
            email.removeSuffix("@example.invalid").isNotBlank(),
        onConfirm,
    )
}
