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
internal fun DemoSheetsSearch(onConfirm: () -> Unit) {
    val saved = workspaceRecord(WorkspaceSection.FILTERS)
    var query by rememberSaveable { mutableStateOf(saved?.name.orEmpty()) }
    var guests by rememberSaveable { mutableIntStateOf(saved?.guests ?: SHEET_MIN_GUESTS) }
    var budget by rememberSaveable { mutableIntStateOf(saved?.filters?.budget ?: R.string.demo_sheet_any) }
    var distance by rememberSaveable { mutableIntStateOf(saved?.filters?.distance ?: R.string.demo_sheet_any) }
    var sort by rememberSaveable { mutableIntStateOf(saved?.filters?.sort ?: R.string.demo_sheet_recommended) }
    var day by rememberSaveable { mutableIntStateOf(saved?.day ?: 1) }
    DemoField(stringResource(R.string.demo_sheet_query), query, { query = it })
    DemoSheetsGuests(guests) { guests = it }
    DemoSheetsOptions(
        R.string.demo_sheet_budget_filter,
        listOf(R.string.demo_sheet_any, R.string.demo_sheet_budget_small, R.string.demo_sheet_budget_large),
        budget,
        { budget = it },
    )
    DemoSheetsOptions(
        R.string.demo_sheet_distance,
        listOf(R.string.demo_sheet_any, R.string.demo_sheet_distance_near, R.string.demo_sheet_distance_far),
        distance,
        { distance = it },
    )
    DemoSheetsCalendar(day, { day = it })
    DemoSheetsOptions(
        R.string.demo_sheet_sort,
        listOf(R.string.demo_sheet_recommended, R.string.demo_sheet_price, R.string.demo_sheet_nearest),
        sort,
        { sort = it },
    )
    DemoAction(stringResource(R.string.demo_sheet_clear)) {
        query = ""
        guests = SHEET_MIN_GUESTS
        budget = R.string.demo_sheet_any
        distance = R.string.demo_sheet_any
        sort = R.string.demo_sheet_recommended
        day = 1
    }
    DemoSheetsSubmit(
        stringResource(
            R.string.demo_sheet_search_summary,
            query,
            guests,
            stringResource(budget),
            stringResource(distance),
            day,
            stringResource(sort),
        ),
        onConfirm = onConfirm,
        section = WorkspaceSection.FILTERS,
        record = WorkspaceRecord(query, day = day, guests = guests, filters = WorkspaceFilters(budget, distance, sort)),
    )
}

@Composable
internal fun DemoSheetsBudget(onConfirm: () -> Unit) {
    val saved = workspaceRecord(WorkspaceSection.BUDGET)
    var scenario by rememberSaveable { mutableIntStateOf(saved?.selection ?: R.string.demo_sheet_budget_balanced) }
    DemoStats(
        listOf(
            stringResource(R.string.demo_sheet_total) to stringResource(R.string.demo_sheet_budget_total),
            stringResource(R.string.demo_sheet_remaining) to stringResource(R.string.demo_sheet_budget_remaining),
        ),
    )
    DemoCard(stringResource(R.string.demo_sheet_venue_line), stringResource(R.string.demo_sheet_venue_cost))
    DemoCard(stringResource(R.string.demo_sheet_food_line), stringResource(R.string.demo_sheet_food_cost))
    DemoCard(stringResource(R.string.demo_sheet_team_line), stringResource(R.string.demo_sheet_team_cost))
    DemoSheetsOptions(
        R.string.demo_sheet_scenario,
        listOf(R.string.demo_sheet_budget_balanced, R.string.demo_sheet_budget_reduced),
        scenario,
        { scenario = it },
    )
    DemoNotice(
        stringResource(
            if (scenario == R.string.demo_sheet_budget_reduced) {
                R.string.demo_sheet_budget_tip_reduced
            } else {
                R.string.demo_sheet_budget_tip_balanced
            },
        ),
    )
    DemoSheetsSubmit(
        stringResource(scenario),
        onConfirm = onConfirm,
        section = WorkspaceSection.BUDGET,
        record = WorkspaceRecord(selection = scenario),
    )
}
