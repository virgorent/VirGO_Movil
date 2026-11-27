package mx.virgo.app.ui.ownercalendar

import java.time.LocalDate

/** Callbacks del calendario, agrupados para probar el contenido sin `ViewModel`. */
data class OwnerCalendarActions(
    val onRetry: () -> Unit = {},
    val onVenue: (String) -> Unit = {},
    val onMonth: (Long) -> Unit = {},
    val onDay: (LocalDate?) -> Unit = {},
    val onOpenBlock: () -> Unit = {},
    val onBlockForm: (BlockForm) -> Unit = {},
    val onCancelBlock: () -> Unit = {},
    val onConfirmBlock: () -> Unit = {},
    val onUnblock: () -> Unit = {},
    val onVenues: () -> Unit = {},
)
