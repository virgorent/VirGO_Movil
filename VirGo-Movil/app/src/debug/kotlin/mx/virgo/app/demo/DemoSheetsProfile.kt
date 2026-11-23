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
internal fun DemoSheetsProfile(onConfirm: () -> Unit) {
    val sampleAlias = stringResource(R.string.demo_sheet_sample_alias)
    val sampleCity = stringResource(R.string.demo_sheet_sample_city)
    val sampleEmail = stringResource(R.string.demo_sheet_sample_email)
    var alias by rememberSaveable { mutableStateOf(sampleAlias) }
    var city by rememberSaveable { mutableStateOf(sampleCity) }
    var email by rememberSaveable { mutableStateOf(sampleEmail) }
    DemoNotice(stringResource(R.string.demo_sheet_fictional_only))
    DemoField(stringResource(R.string.demo_sheet_alias), alias, { alias = it })
    DemoField(stringResource(R.string.demo_sheet_demo_email), email, { email = it })
    DemoField(stringResource(R.string.demo_sheet_city), city, { city = it })
    DemoSheetsSubmit(
        stringResource(R.string.demo_sheet_profile_summary, alias, email, city),
        alias.isNotBlank() &&
            city.isNotBlank() &&
            email.endsWith("@example.invalid") &&
            email.removeSuffix("@example.invalid").isNotBlank(),
        onConfirm,
    )
}

@Composable
internal fun DemoSheetsPreferences(onConfirm: () -> Unit) {
    var guests by rememberSaveable { mutableIntStateOf(SHEET_MIN_GUESTS) }
    var distance by rememberSaveable { mutableIntStateOf(R.string.demo_sheet_any) }
    var style by rememberSaveable { mutableIntStateOf(R.string.demo_sheet_garden) }
    DemoSheetsGuests(guests) { guests = it }
    DemoSheetsOptions(
        R.string.demo_sheet_distance,
        listOf(R.string.demo_sheet_any, R.string.demo_sheet_distance_near, R.string.demo_sheet_distance_far),
        distance,
        { distance = it },
    )
    DemoSheetsOptions(
        R.string.demo_sheet_style,
        listOf(R.string.demo_sheet_garden, R.string.demo_sheet_indoor),
        style,
        { style = it },
    )
    DemoNotice(stringResource(R.string.demo_sheet_preferences_hint))
    DemoSheetsSubmit(
        stringResource(
            R.string.demo_sheet_preferences_summary,
            guests,
            stringResource(distance),
            stringResource(style),
        ),
        onConfirm = onConfirm,
    )
}

@Composable
internal fun DemoSheetsMethods(onConfirm: () -> Unit) {
    var method by rememberSaveable { mutableIntStateOf(R.string.demo_sheet_mock_card) }
    DemoNotice(stringResource(R.string.demo_sheet_no_card_data))
    DemoCard(stringResource(R.string.demo_sheet_mock_card), stringResource(R.string.demo_sheet_mock_card_hint))
    DemoCard(stringResource(R.string.demo_sheet_mock_transfer), stringResource(R.string.demo_sheet_mock_transfer_hint))
    DemoSheetsOptions(
        R.string.demo_sheet_default_method,
        listOf(R.string.demo_sheet_mock_card, R.string.demo_sheet_mock_transfer),
        method,
        { method = it },
    )
    DemoSheetsSubmit(stringResource(method), onConfirm = onConfirm)
}
