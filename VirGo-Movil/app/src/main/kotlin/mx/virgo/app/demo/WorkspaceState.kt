package mx.virgo.app.demo

/** Presentation-only records. Dates refer to the explicitly labelled example month. */
enum class WorkspaceSection {
    FILTERS,
    VENUE_DATE,
    BUDGET,
    EVENT,
    TEAM,
    PROFILE,
    PREFERENCES,
    METHOD,
    PAYMENTS,
    GUESTS,
    TASKS,
    REMINDERS,
    COLLABORATORS,
    BLOCKS,
    AGENDA,
}

data class WorkspaceRecord(
    val name: String = "",
    val summary: String = "",
    val day: Int = 1,
    val guests: Int = SHEET_MIN_GUESTS,
    val amount: Double? = null,
    val selection: Int = 0,
    val endDay: Int = day,
    val filters: WorkspaceFilters? = null,
)

data class WorkspaceFilters(
    val budget: Int,
    val distance: Int,
    val sort: Int,
)

data class WorkspaceUiState(
    val records: Map<WorkspaceSection, List<WorkspaceRecord>> = emptyMap(),
    val favorite: Boolean = true,
    val paid: Boolean = false,
    val accountProfile: WorkspaceProfile? = null,
) {
    fun latest(section: WorkspaceSection): WorkspaceRecord? = records[section]?.lastOrNull()
}

data class WorkspaceProfile(
    val name: String,
    val email: String,
)

internal fun WorkspaceRecord.isValid(): Boolean =
    name.length <= WORKSPACE_MAX_INPUT &&
        summary.length <= WORKSPACE_MAX_SUMMARY &&
        day in 1..SHEET_MONTH_DAYS &&
        endDay in day..SHEET_MONTH_DAYS &&
        guests in SHEET_MIN_GUESTS..SHEET_MAX_GUESTS &&
        (amount == null || (amount.isFinite() && amount > 0 && amount <= SHEET_MAX_AMOUNT))

private const val WORKSPACE_MAX_INPUT = 500
private const val WORKSPACE_MAX_SUMMARY = 3000
