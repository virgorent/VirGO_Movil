package mx.virgo.app.demo

import mx.virgo.app.R

private val profileRoles = DemoRole.entries.toSet()

val demoSheetDefinitions: List<DemoSheetDefinition> =
    listOf(
        DemoSheetDefinition("H01", "explore", R.string.demo_sheet_search),
        DemoSheetDefinition("H02", "venue", R.string.demo_sheet_dates),
        DemoSheetDefinition("H03", "studio", R.string.demo_sheet_budget),
        DemoSheetDefinition("H04", "event", R.string.demo_sheet_event),
        DemoSheetDefinition("H05", "event", R.string.demo_sheet_team),
        DemoSheetDefinition("H06", "profile", R.string.demo_sheet_profile, profileRoles),
        DemoSheetDefinition("H07", "profile", R.string.demo_sheet_preferences, profileRoles),
        DemoSheetDefinition("H08", "profile", R.string.demo_sheet_methods, profileRoles),
        DemoSheetDefinition("H09", "payments", R.string.demo_sheet_payment),
        DemoSheetDefinition("H10", "guests", R.string.demo_sheet_guest),
        DemoSheetDefinition("H11", "tasks", R.string.demo_sheet_task),
        DemoSheetDefinition("H12", "reminders", R.string.demo_sheet_reminder),
        DemoSheetDefinition("H13", "share", R.string.demo_sheet_collaborator),
        DemoSheetDefinition("H14", "ownerCalendar", R.string.demo_sheet_block, setOf(DemoRole.OWNER)),
        DemoSheetDefinition("H15", "providerAgenda", R.string.demo_sheet_agenda, setOf(DemoRole.PROVIDER)),
    )

fun demoSheetsFor(
    role: DemoRole,
    route: String,
): List<DemoSheetDefinition> = demoSheetDefinitions.filter { role in it.roles && it.route == route }
