package mx.virgo.app.ui.studio

import mx.virgo.core.domain.StaffRole
import mx.virgo.core.domain.TableLayout

/** Callbacks de «IA Studio», agrupados para probar el contenido sin `ViewModel`. */
data class StudioActions(
    val onTab: (StudioTab) -> Unit = {},
    val onGuests: (String) -> Unit = {},
    val onLayout: (TableLayout) -> Unit = {},
    val onStaff: (StaffRole, Int) -> Unit = { _, _ -> },
    val onResetStaff: () -> Unit = {},
    val onIdeas: () -> Unit = {},
    val onAssistant: () -> Unit = {},
)
